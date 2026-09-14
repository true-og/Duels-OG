package me.realized.duels.hook.hooks.worldguard;

import java.util.Collection;
import java.util.function.BiPredicate;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;

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

    // Id of the smallest region containing the location, or null when none does.
    String findSmallestRegionId(final Location location);

    // True when the named region exists in the world and contains the location's block.
    boolean isInRegion(final World world, final String regionId, final Location location);

    // Pre-allows WorldGuard PvP checks for attacker and victim pairs the predicate accepts.
    // Returns the registered listener so the caller can unregister it on reload.
    Listener registerDamageAllow(final Plugin plugin, final BiPredicate<Player, Player> allow);
}
