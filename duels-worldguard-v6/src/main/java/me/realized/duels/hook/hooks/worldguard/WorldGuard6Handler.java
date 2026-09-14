package me.realized.duels.hook.hooks.worldguard;

import com.sk89q.worldedit.BlockVector;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
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

public class WorldGuard6Handler implements WorldGuardHandler {

    @Override
    public String findRegion(final Player player, final Collection<String> regions) {
        for (final ProtectedRegion region : WorldGuardPlugin.inst().getRegionManager(player.getWorld()).getApplicableRegions(player.getLocation())) {
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

        final BlockVector min = smallest.getMinimumPoint();
        final BlockVector max = smallest.getMaximumPoint();
        return new RegionBounds(min.getBlockX(), min.getBlockY(), min.getBlockZ(), max.getBlockX(), max.getBlockY(), max.getBlockZ());
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

        final RegionManager manager = WorldGuardPlugin.inst().getRegionManager(world);

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

            final Object root = damage.getCause().getRootCause();

            if (root instanceof Player && allow.test((Player) root, (Player) damage.getEntity())) {
                damage.setResult(Event.Result.ALLOW);
            }
        }, plugin, true);

        return listener;
    }

    private ProtectedRegion findSmallestRegion(final Location location) {
        if (location == null || location.getWorld() == null) {
            return null;
        }

        final RegionManager manager = WorldGuardPlugin.inst().getRegionManager(location.getWorld());

        if (manager == null) {
            return null;
        }

        final ApplicableRegionSet applicable = manager.getApplicableRegions(location);
        ProtectedRegion smallest = null;
        long smallestVolume = Long.MAX_VALUE;

        for (final ProtectedRegion region : applicable) {
            final BlockVector min = region.getMinimumPoint();
            final BlockVector max = region.getMaximumPoint();
            final long volume = (long) (max.getBlockX() - min.getBlockX() + 1) * (long) (max.getBlockY() - min.getBlockY() + 1) * (long) (max.getBlockZ() - min.getBlockZ() + 1);

            if (volume < smallestVolume) {
                smallest = region;
                smallestVolume = volume;
            }
        }

        return smallest;
    }
}
