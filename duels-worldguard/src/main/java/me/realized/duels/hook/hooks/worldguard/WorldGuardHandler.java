package me.realized.duels.hook.hooks.worldguard;

import java.util.Collection;
import org.bukkit.Location;
import org.bukkit.entity.Player;

public interface WorldGuardHandler {

    String findRegion(final Player player, final Collection<String> regions);

    /**
     * Returns the smallest WorldGuard region containing the given location, expressed as an
     * axis-aligned bounding box. Used by the arena boundary wall to derive arena bounds
     * directly from a WorldGuard region instead of requiring admins to set corner positions.
     *
     * @return bounding box of the smallest containing region, or null if none exists or the
     *         WorldGuard platform cannot resolve the location.
     */
    RegionBounds findSmallestRegionBounds(final Location location);
}
