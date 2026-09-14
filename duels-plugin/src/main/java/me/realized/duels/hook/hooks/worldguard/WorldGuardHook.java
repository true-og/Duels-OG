package me.realized.duels.hook.hooks.worldguard;

import java.util.Collection;
import me.realized.duels.DuelsPlugin;
import me.realized.duels.config.Config;
import me.realized.duels.util.hook.PluginHook;
import me.realized.duels.util.reflect.ReflectionUtil;
import org.bukkit.Location;
import org.bukkit.event.Listener;
import org.bukkit.World;
import java.util.function.BiPredicate;
import org.bukkit.entity.Player;

public class WorldGuardHook extends PluginHook<DuelsPlugin> {

    public static final String NAME = "WorldGuard";

    private final Config config;
    private final WorldGuardHandler handler;

    public WorldGuardHook(final DuelsPlugin plugin) {
        super(plugin, NAME);
        this.config = plugin.getConfiguration();
        this.handler = ReflectionUtil.getClassUnsafe("com.sk89q.worldguard.WorldGuard") != null ? new WorldGuard7Handler() : new WorldGuard6Handler();
    }

    public String findDuelZone(final Player player) {
        if (!config.isDuelzoneEnabled()) {
            return null;
        }

        final Collection<String> allowedRegions = config.getDuelzones();

        if (allowedRegions.isEmpty()) {
            return null;
        }

        return handler.findRegion(player, allowedRegions);
    }

    public RegionBounds findSmallestRegionBounds(final Location location) {
        return handler.findSmallestRegionBounds(location);
    }

    public String findSmallestRegionId(final Location location) {
        return handler.findSmallestRegionId(location);
    }

    public boolean isInRegion(final World world, final String regionId, final Location location) {
        return handler.isInRegion(world, regionId, location);
    }

    // Lets WorldGuard allow PvP for the given attacker and victim pairs even in pvp:deny regions.
    public Listener registerDamageAllow(final BiPredicate<Player, Player> allow) {
        return handler.registerDamageAllow(plugin, allow);
    }
}
