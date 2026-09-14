package me.realized.duels.hook.hooks.worldguard;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.bukkit.event.entity.DamageEntityEvent;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import java.util.Collection;
import java.util.function.BiPredicate;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;

public class WorldGuard7Handler implements WorldGuardHandler {

    @Override
    public String findRegion(final Player player, final Collection<String> regions) {
        final Location location = player.getLocation();
        final BlockVector3 vector = BlockVector3.at(location.getBlockX(), location.getBlockY(), location.getBlockZ());

        for (final ProtectedRegion region : WorldGuard.getInstance().getPlatform().getRegionContainer().get(BukkitAdapter.adapt(player.getWorld()))
            .getApplicableRegions(vector)) {
            if (regions.contains(region.getId())) {
                return region.getId();
            }
        }

        return null;
    }

    @Override
    public RegionBounds findSmallestRegionBounds(final Location location) {
        final ProtectedRegion smallest = findSmallestRegion(location);

        if (smallest == null) {
            return null;
        }

        final BlockVector3 min = smallest.getMinimumPoint();
        final BlockVector3 max = smallest.getMaximumPoint();
        return new RegionBounds(min.getX(), min.getY(), min.getZ(), max.getX(), max.getY(), max.getZ());
    }

    @Override
    public String findSmallestRegionId(final Location location) {
        final ProtectedRegion smallest = findSmallestRegion(location);
        return smallest != null ? smallest.getId() : null;
    }

    @Override
    public boolean isInRegion(final World world, final String regionId, final Location location) {
        if (world == null || regionId == null || location == null) {
            return false;
        }

        final RegionManager manager = getManager(world);

        if (manager == null) {
            return false;
        }

        final ProtectedRegion region = manager.getRegion(regionId);
        return region != null && region.contains(location.getBlockX(), location.getBlockY(), location.getBlockZ());
    }

    @Override
    public Listener registerDamageAllow(final Plugin plugin, final BiPredicate<Player, Player> allow) {
        final Listener listener = new Listener() {};

        // LOWEST runs before WorldGuard's region check, which skips events already set to ALLOW.
        Bukkit.getPluginManager().registerEvent(DamageEntityEvent.class, listener, EventPriority.LOWEST, (l, event) -> {
            final DamageEntityEvent damage = (DamageEntityEvent) event;

            if (!(damage.getEntity() instanceof Player)) {
                return;
            }

            final Player attacker = damage.getCause().getFirstPlayer();

            if (attacker != null && allow.test(attacker, (Player) damage.getEntity())) {
                damage.setResult(Event.Result.ALLOW);
            }
        }, plugin, true);

        return listener;
    }

    private RegionManager getManager(final World world) {
        return WorldGuard.getInstance().getPlatform().getRegionContainer().get(BukkitAdapter.adapt(world));
    }

    private ProtectedRegion findSmallestRegion(final Location location) {
        if (location == null || location.getWorld() == null) {
            return null;
        }

        final RegionManager manager = getManager(location.getWorld());

        if (manager == null) {
            return null;
        }

        final BlockVector3 vector = BlockVector3.at(location.getBlockX(), location.getBlockY(), location.getBlockZ());
        final ApplicableRegionSet applicable = manager.getApplicableRegions(vector);
        ProtectedRegion smallest = null;
        long smallestVolume = Long.MAX_VALUE;

        for (final ProtectedRegion region : applicable) {
            final BlockVector3 min = region.getMinimumPoint();
            final BlockVector3 max = region.getMaximumPoint();
            final long volume = (long) (max.getX() - min.getX() + 1) * (long) (max.getY() - min.getY() + 1) * (long) (max.getZ() - min.getZ() + 1);

            if (volume < smallestVolume) {
                smallest = region;
                smallestVolume = volume;
            }
        }

        return smallest;
    }
}
