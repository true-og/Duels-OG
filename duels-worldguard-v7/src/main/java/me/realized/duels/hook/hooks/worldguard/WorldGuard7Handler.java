package me.realized.duels.hook.hooks.worldguard;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import java.util.Collection;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

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
        if (location == null || location.getWorld() == null) {
            return null;
        }

        final World world = location.getWorld();
        final RegionManager manager = WorldGuard.getInstance().getPlatform().getRegionContainer().get(BukkitAdapter.adapt(world));
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

        if (smallest == null) {
            return null;
        }

        final BlockVector3 min = smallest.getMinimumPoint();
        final BlockVector3 max = smallest.getMaximumPoint();
        return new RegionBounds(min.getX(), min.getY(), min.getZ(), max.getX(), max.getY(), max.getZ());
    }
}
