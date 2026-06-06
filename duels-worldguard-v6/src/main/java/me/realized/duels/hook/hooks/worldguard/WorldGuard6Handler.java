package me.realized.duels.hook.hooks.worldguard;

import com.sk89q.worldedit.BlockVector;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import java.util.Collection;
import org.bukkit.Location;
import org.bukkit.entity.Player;

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

        if (smallest == null) {
            return null;
        }

        final BlockVector min = smallest.getMinimumPoint();
        final BlockVector max = smallest.getMaximumPoint();
        return new RegionBounds(min.getBlockX(), min.getBlockY(), min.getBlockZ(), max.getBlockX(), max.getBlockY(), max.getBlockZ());
    }
}
