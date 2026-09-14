package me.realized.duels.arena;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import me.realized.duels.DuelsPlugin;
import me.realized.duels.Permissions;
import me.realized.duels.api.event.match.MatchStartEvent;
import me.realized.duels.config.Config;
import me.realized.duels.config.Lang;
import me.realized.duels.hook.hooks.worldguard.RegionBounds;
import me.realized.duels.hook.hooks.worldguard.WorldGuardHook;
import me.realized.duels.spectate.SpectateManagerImpl;
import me.realized.duels.teleport.Teleport;
import me.realized.duels.util.Loadable;
import me.realized.duels.util.Log;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;

// Keeps a running duel sealed: only the two match players may trade damage, and anyone who is
// neither fighting nor spectating is kept out of the arena's WorldGuard region while it is used.
public class ArenaGuard implements Loadable, Listener {

    private static final long NOTICE_INTERVAL_MILLIS = 2000L;
    private static final double EJECT_MARGIN = 1.5;

    private final DuelsPlugin plugin;
    private final Config config;
    private final ArenaManagerImpl arenaManager;

    private final Map<String, String> regionCache = new HashMap<>();
    private final Map<UUID, Long> lastNotice = new HashMap<>();

    private SpectateManagerImpl spectateManager;
    private Lang lang;
    private WorldGuardHook worldGuard;
    private Listener damageAllowListener;
    private boolean preventOutsiderDamage;
    private boolean ejectOutsiders;

    public ArenaGuard(final DuelsPlugin plugin) {
        this.plugin = plugin;
        this.config = plugin.getConfiguration();
        this.arenaManager = plugin.getArenaManager();
    }

    @Override
    public void handleLoad() {
        this.spectateManager = plugin.getSpectateManager();
        this.lang = plugin.getLang();
        this.worldGuard = plugin.getHookManager().getHook(WorldGuardHook.class);
        this.preventOutsiderDamage = config.isArenaPreventOutsiderDamage();
        this.ejectOutsiders = config.isArenaEjectOutsiders();
        this.regionCache.clear();

        if (config.isArenaAllowMatchDamage()) {
            if (worldGuard != null) {
                this.damageAllowListener = worldGuard.registerDamageAllow(this::isSameMatch);
            } else {
                Log.warn(this, "arena.protection.allow-match-damage is enabled but WorldGuard is not loaded; pvp:deny arenas will block duels.");
            }
        }

        if (ejectOutsiders && worldGuard == null) {
            Log.warn(this, "arena.protection.eject-outsiders is enabled but WorldGuard is not loaded; outsiders will not be kept out of arenas.");
            this.ejectOutsiders = false;
        }

        if (preventOutsiderDamage || ejectOutsiders) {
            plugin.registerListener(this);
        }
    }

    @Override
    public void handleUnload() {
        if (damageAllowListener != null) {
            HandlerList.unregisterAll(damageAllowListener);
            damageAllowListener = null;
        }

        regionCache.clear();
        lastNotice.clear();
    }

    // Both players alive in the same match, so WorldGuard may let them hit each other.
    private boolean isSameMatch(final Player attacker, final Player victim) {
        if (attacker.equals(victim)) {
            return false;
        }

        final ArenaImpl arena = arenaManager.get(attacker);
        return arena != null && arena.equals(arenaManager.get(victim));
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void on(final EntityDamageByEntityEvent event) {
        if (!preventOutsiderDamage || !(event.getEntity() instanceof Player)) {
            return;
        }

        final Player victim = (Player) event.getEntity();
        final Player attacker = resolveAttacker(event.getDamager());

        if (attacker == null || attacker.equals(victim)) {
            return;
        }

        final ArenaImpl victimArena = arenaManager.get(victim);
        final ArenaImpl attackerArena = arenaManager.get(attacker);

        if (victimArena == null && attackerArena == null) {
            return;
        }

        if (victimArena != null && victimArena.equals(attackerArena)) {
            return;
        }

        event.setCancelled(true);

        if (attackerArena == null) {
            notify(attacker, "DUEL.prevent.outsider-damage");
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void on(final PlayerMoveEvent event) {
        if (!ejectOutsiders) {
            return;
        }

        final Location from = event.getFrom();
        final Location to = event.getTo();

        if (to == null || (from.getBlockX() == to.getBlockX() && from.getBlockY() == to.getBlockY() && from.getBlockZ() == to.getBlockZ())) {
            return;
        }

        final Player player = event.getPlayer();

        if (isExempt(player)) {
            return;
        }

        final ArenaImpl arena = findUsedArenaAt(to);

        if (arena == null) {
            return;
        }

        // Already inside when the match started: move them out instead of freezing them.
        if (arena.equals(findUsedArenaAt(from))) {
            eject(player, arena);
            return;
        }

        event.setCancelled(true);
        notify(player, "DUEL.prevent.arena-in-use");
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void on(final PlayerTeleportEvent event) {
        if (!ejectOutsiders) {
            return;
        }

        final Player player = event.getPlayer();

        // Duels' own teleports carry this metadata (match start, spectate, return).
        if (player.hasMetadata(Teleport.METADATA_KEY) || isExempt(player)) {
            return;
        }

        if (findUsedArenaAt(event.getTo()) == null) {
            return;
        }

        event.setCancelled(true);
        notify(player, "DUEL.prevent.arena-in-use");
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void on(final MatchStartEvent event) {
        if (!ejectOutsiders || !(event.getMatch().getArena() instanceof ArenaImpl)) {
            return;
        }

        final ArenaImpl arena = (ArenaImpl) event.getMatch().getArena();
        final Location origin = arena.getPosition(1);

        if (origin == null || origin.getWorld() == null) {
            return;
        }

        for (final Player player : origin.getWorld().getPlayers()) {
            if (!isExempt(player) && isInside(arena, player.getLocation())) {
                eject(player, arena);
            }
        }
    }

    private boolean isExempt(final Player player) {
        return arenaManager.isInMatch(player) || spectateManager.isSpectating(player) || player.hasPermission(Permissions.ARENA_BYPASS);
    }

    private ArenaImpl findUsedArenaAt(final Location location) {
        if (location == null || location.getWorld() == null) {
            return null;
        }

        for (final ArenaImpl arena : arenaManager.getArenasImpl()) {
            if (arena.isUsed() && isInside(arena, location)) {
                return arena;
            }
        }

        return null;
    }

    private boolean isInside(final ArenaImpl arena, final Location location) {
        final Location origin = arena.getPosition(1);

        if (origin == null || origin.getWorld() == null || !origin.getWorld().equals(location.getWorld())) {
            return false;
        }

        final String regionId = regionFor(arena, origin);
        return regionId != null && worldGuard.isInRegion(origin.getWorld(), regionId, location);
    }

    // Arenas without an enclosing WorldGuard region cannot be guarded; that is logged once.
    private String regionFor(final ArenaImpl arena, final Location origin) {
        if (regionCache.containsKey(arena.getName())) {
            return regionCache.get(arena.getName());
        }

        final String regionId = worldGuard.findSmallestRegionId(origin);

        if (regionId == null) {
            Log.warn(this, "Arena '" + arena.getName() + "' has no enclosing WorldGuard region; outsiders cannot be kept out of it.");
        }

        regionCache.put(arena.getName(), regionId);
        return regionId;
    }

    // Moves the player just past the nearest wall of the arena's bounding box, keeping their Y.
    private void eject(final Player player, final ArenaImpl arena) {
        final Location origin = arena.getPosition(1);
        final RegionBounds bounds = origin != null ? worldGuard.findSmallestRegionBounds(origin) : null;
        final Location target = bounds != null ? ejectLocation(bounds, player.getLocation()) : origin;

        if (target == null) {
            return;
        }

        player.teleport(target, TeleportCause.PLUGIN);
        notify(player, "DUEL.prevent.arena-in-use");
    }

    private Location ejectLocation(final RegionBounds bounds, final Location location) {
        final Location target = location.clone();
        final double toMinX = location.getX() - bounds.minX();
        final double toMaxX = bounds.maxX() + 1 - location.getX();
        final double toMinZ = location.getZ() - bounds.minZ();
        final double toMaxZ = bounds.maxZ() + 1 - location.getZ();
        final double nearest = Math.min(Math.min(toMinX, toMaxX), Math.min(toMinZ, toMaxZ));

        if (nearest == toMinX) {
            target.setX(bounds.minX() - EJECT_MARGIN);
        } else if (nearest == toMaxX) {
            target.setX(bounds.maxX() + 1 + EJECT_MARGIN);
        } else if (nearest == toMinZ) {
            target.setZ(bounds.minZ() - EJECT_MARGIN);
        } else {
            target.setZ(bounds.maxZ() + 1 + EJECT_MARGIN);
        }

        return target;
    }

    private Player resolveAttacker(final Entity damager) {
        if (damager instanceof Player) {
            return (Player) damager;
        }

        if (damager instanceof Projectile && ((Projectile) damager).getShooter() instanceof Player) {
            return (Player) ((Projectile) damager).getShooter();
        }

        return null;
    }

    private void notify(final Player player, final String key) {
        final long now = System.currentTimeMillis();
        final Long last = lastNotice.get(player.getUniqueId());

        if (last != null && now - last < NOTICE_INTERVAL_MILLIS) {
            return;
        }

        lastNotice.put(player.getUniqueId(), now);
        lang.sendMessage(player, key);
    }
}
