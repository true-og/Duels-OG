package me.realized.duels.player;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

final class PlayerRideState {

    private final Entity vehicle;
    private final UUID vehicleId;
    private final Location vehicleLocation;
    private final Location chairBlockLocation;

    private PlayerRideState(final Entity vehicle) {
        this.vehicle = vehicle;
        this.vehicleId = vehicle.getUniqueId();
        this.vehicleLocation = vehicle.getLocation().clone();
        this.chairBlockLocation = null;
    }

    private PlayerRideState(final Location chairBlockLocation) {
        this.vehicle = null;
        this.vehicleId = null;
        this.vehicleLocation = null;
        this.chairBlockLocation = chairBlockLocation;
    }

    static PlayerRideState capture(final Player player) {
        final Location chairBlockLocation = BetterChairsBridge.removeSeat(player);

        if (chairBlockLocation != null) {
            return new PlayerRideState(chairBlockLocation);
        }

        final Entity vehicle = player.getVehicle();
        return vehicle != null ? new PlayerRideState(vehicle) : null;
    }

    Location getReturnLocation() {
        if (chairBlockLocation != null) {
            return chairBlockLocation.clone().add(0.5, 1.0, 0.5);
        }

        return vehicleLocation != null ? vehicleLocation.clone() : null;
    }

    void restore(final Player player) {
        if (chairBlockLocation != null) {
            BetterChairsBridge.restoreSeat(player, chairBlockLocation);
            return;
        }

        final Entity target = findVehicle();

        if (target == null || target.isDead() || !target.isValid()) {
            return;
        }

        target.addPassenger(player);
    }

    private Entity findVehicle() {
        if (vehicle != null && vehicle.isValid() && !vehicle.isDead()) {
            return vehicle;
        }

        if (vehicleLocation == null || vehicleLocation.getWorld() == null || vehicleId == null) {
            return null;
        }

        vehicleLocation.getChunk().load();

        final World world = vehicleLocation.getWorld();
        for (final Entity entity : world.getChunkAt(vehicleLocation).getEntities()) {
            if (vehicleId.equals(entity.getUniqueId())) {
                return entity;
            }
        }

        return null;
    }

    private static final class BetterChairsBridge {

        private static Method getInstanceMethod;
        private static Method getChairMethod;
        private static Method destroyMethod;
        private static Method createMethod;
        private static Method getBlockMethod;
        private static Class<?> chairClass;
        private static Class<?> managerClass;

        private static boolean lookupComplete;

        private BetterChairsBridge() {}

        static Location removeSeat(final Player player) {
            final Object manager = getManager();

            if (manager == null) {
                return null;
            }

            final Object chair = invoke(getChairMethod, manager, player);

            if (chair == null) {
                return null;
            }

            final Object block = invoke(getBlockMethod, chair);

            if (!(block instanceof Block)) {
                return null;
            }

            invoke(destroyMethod, manager, chair, false, true);
            return ((Block) block).getLocation().clone();
        }

        static void restoreSeat(final Player player, final Location blockLocation) {
            final Object manager = getManager();

            if (manager == null || blockLocation.getWorld() == null) {
                return;
            }

            blockLocation.getChunk().load();
            invoke(createMethod, manager, player, blockLocation.getBlock());
        }

        private static Object getManager() {
            if (!isPluginEnabled()) {
                return null;
            }

            loadMethods();

            if (getInstanceMethod == null) {
                return null;
            }

            return invoke(getInstanceMethod, null);
        }

        private static void loadMethods() {
            if (lookupComplete) {
                return;
            }

            try {
                managerClass = Class.forName("de.sprax2013.betterchairs.ChairManager");
                chairClass = Class.forName("de.sprax2013.betterchairs.Chair");
                getInstanceMethod = managerClass.getMethod("getInstance");
                getChairMethod = managerClass.getMethod("getChair", Player.class);
                destroyMethod = managerClass.getMethod("destroy", chairClass, Boolean.TYPE, Boolean.TYPE);
                createMethod = managerClass.getMethod("create", Player.class, Block.class);
                getBlockMethod = chairClass.getMethod("getBlock");
            } catch (ClassNotFoundException | NoSuchMethodException ignored) {
                getInstanceMethod = null;
                getChairMethod = null;
                destroyMethod = null;
                createMethod = null;
                getBlockMethod = null;
            }

            lookupComplete = true;
        }

        private static Object invoke(final Method method, final Object target, final Object... args) {
            if (method == null) {
                return null;
            }

            try {
                return method.invoke(target, args);
            } catch (IllegalAccessException | InvocationTargetException ignored) {
                return null;
            }
        }

        private static boolean isPluginEnabled() {
            final Plugin betterChairs = Bukkit.getPluginManager().getPlugin("BetterChairs-OG");
            return betterChairs != null && betterChairs.isEnabled();
        }
    }
}
