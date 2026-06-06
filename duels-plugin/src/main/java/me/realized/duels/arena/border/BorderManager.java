/*
 * Derived from the EternalCombat (https://github.com/EternalCodeTeam/EternalCombat) border module
 * by the EternalCode Team, originally licensed under the Apache License, Version 2.0.
 *
 * Modifications by the TrueOG Network for Duels-OG:
 *   - Adapted from per-region "safe vs unsafe" PvP boundaries to per-arena cuboid bounds
 *     sourced from a WorldGuard region containing the arena's spawn position.
 *   - Replaced PacketEvents block updates with Bukkit's Player#sendBlockChange.
 *   - Replaced async chunk-snapshot caching with a synchronous per-tick refresh.
 *   - Hooked to Duels' match lifecycle (PlayerMove / PlayerTeleport / PlayerQuit /
 *     MatchEndEvent) and Duels' Config / Loadable layer instead of EternalCombat's own.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific language governing permissions
 * and limitations under the License.
 */
package me.realized.duels.arena.border;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import me.realized.duels.DuelsPlugin;
import me.realized.duels.api.event.match.MatchEndEvent;
import me.realized.duels.arena.ArenaImpl;
import me.realized.duels.arena.ArenaManagerImpl;
import me.realized.duels.config.Config;
import me.realized.duels.hook.hooks.worldguard.RegionBounds;
import me.realized.duels.hook.hooks.worldguard.WorldGuardHook;
import me.realized.duels.util.EnumUtil;
import me.realized.duels.util.Loadable;
import me.realized.duels.util.Log;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;

/**
 * Renders per-player glass walls along arena boundaries while the player is in a match. Mirrors the
 * EternalCombat-OG border logic: the arena is the "safe" region; a wall block sits on a safe column
 * (inside the arena, at the edge) only when an adjacent column is outside the arena bounds, so the
 * wall appears on the inner face of the arena boundary and is visible from inside.
 *
 * Arena bounds are derived automatically from the smallest WorldGuard region containing the arena's
 * spawn position 1. Arenas not enclosed by a WorldGuard region get no wall.
 */
public class BorderManager implements Loadable, Listener {

    private static final int[][] HORIZONTAL_NEIGHBOURS = {{1, 0}, {-1, 0}, {0, 1}, {0, -1}};

    private final DuelsPlugin plugin;
    private final Config config;
    private final ArenaManagerImpl arenaManager;

    private final Map<UUID, Map<BorderPoint, BlockData>> activePoints = new HashMap<>();
    private final Map<String, RegionBounds> boundsCache = new ConcurrentHashMap<>();

    private WorldGuardHook worldGuard;
    private BlockData wallBlockData;
    private boolean enabled;
    private int distance;
    private long updateIntervalTicks;
    private int updateTaskId = -1;

    public BorderManager(final DuelsPlugin plugin) {
        this.plugin = plugin;
        this.config = plugin.getConfiguration();
        this.arenaManager = plugin.getArenaManager();
    }

    @Override
    public void handleLoad() {
        this.enabled = config.isArenaBoundaryEnabled();
        this.distance = config.getArenaBoundaryDistance();
        this.updateIntervalTicks = config.getArenaBoundaryUpdateTicks();
        this.wallBlockData = resolveWallBlockData(config.getArenaBoundaryBlock());
        this.worldGuard = plugin.getHookManager().getHook(WorldGuardHook.class);
        this.boundsCache.clear();

        if (!this.enabled || wallBlockData == null) {
            return;
        }

        if (worldGuard == null) {
            Log.warn(this, "Arena boundary wall is enabled but WorldGuard is not loaded; the wall will stay inactive.");
            return;
        }

        plugin.registerListener(this);

        this.updateTaskId = plugin.doSyncRepeat(this::tickAll, updateIntervalTicks, updateIntervalTicks).getTaskId();
    }

    @Override
    public void handleUnload() {
        if (updateTaskId != -1) {
            plugin.cancelTask(updateTaskId);
            updateTaskId = -1;
        }

        for (final Map.Entry<UUID, Map<BorderPoint, BlockData>> entry : new HashMap<>(activePoints).entrySet()) {
            final Player player = Bukkit.getPlayer(entry.getKey());
            if (player != null) {
                revert(player, entry.getValue().entrySet());
            }
        }

        activePoints.clear();
        boundsCache.clear();
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void on(final PlayerMoveEvent event) {
        if (!enabled) {
            return;
        }

        final Location from = event.getFrom();
        final Location to = event.getTo();
        if (to == null || (from.getBlockX() == to.getBlockX() && from.getBlockY() == to.getBlockY() && from.getBlockZ() == to.getBlockZ())) {
            return;
        }

        updateBorder(event.getPlayer(), to);
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    public void onPearlTeleport(final PlayerTeleportEvent event) {
        if (!enabled || worldGuard == null || event.getCause() != TeleportCause.ENDER_PEARL) {
            return;
        }

        final Player player = event.getPlayer();
        final ArenaImpl arena = arenaManager.get(player);
        if (arena == null) {
            return;
        }

        final Location to = event.getTo();
        if (to == null) {
            return;
        }

        final RegionBounds bounds = lookupBounds(arena, to.getWorld());
        // No WG region for this arena → no enforcement; honour vanilla pearl behaviour.
        if (bounds == null) {
            return;
        }

        if (containsBlock(bounds, to)) {
            return;
        }

        event.setCancelled(true);
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void on(final PlayerTeleportEvent event) {
        if (!enabled) {
            return;
        }

        final Location to = event.getTo();
        if (to == null) {
            return;
        }

        updateBorder(event.getPlayer(), to);
    }

    @EventHandler
    public void on(final PlayerQuitEvent event) {
        clearBorder(event.getPlayer());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void on(final MatchEndEvent event) {
        for (final Player player : event.getMatch().getStartingPlayers()) {
            clearBorder(player);
        }
    }

    public void clearBorder(final Player player) {
        final Map<BorderPoint, BlockData> saved = activePoints.remove(player.getUniqueId());
        if (saved == null || saved.isEmpty()) {
            return;
        }

        revert(player, saved.entrySet());
    }

    private void tickAll() {
        for (final Player player : Bukkit.getOnlinePlayers()) {
            updateBorder(player, player.getLocation());
        }
    }

    private void updateBorder(final Player player, final Location location) {
        if (!enabled || worldGuard == null) {
            return;
        }

        final ArenaImpl arena = arenaManager.get(player);
        if (arena == null) {
            clearBorder(player);
            return;
        }

        final RegionBounds bounds = lookupBounds(arena, location.getWorld());
        if (bounds == null) {
            clearBorder(player);
            return;
        }

        final Set<BorderPoint> next = resolvePoints(bounds, location);
        final Map<BorderPoint, BlockData> current = activePoints.get(player.getUniqueId());

        if (next.isEmpty() && (current == null || current.isEmpty())) {
            return;
        }

        final Map<BorderPoint, BlockData> updated = new HashMap<>();
        final World world = location.getWorld();

        if (current != null) {
            for (final Iterator<Map.Entry<BorderPoint, BlockData>> it = current.entrySet().iterator(); it.hasNext(); ) {
                final Map.Entry<BorderPoint, BlockData> entry = it.next();
                if (next.contains(entry.getKey())) {
                    updated.put(entry.getKey(), entry.getValue());
                } else {
                    sendBlock(player, world, entry.getKey(), entry.getValue());
                }
            }
        }

        for (final BorderPoint point : next) {
            if (updated.containsKey(point)) {
                continue;
            }

            final Block block = world.getBlockAt(point.x(), point.y(), point.z());
            if (block.getType().isSolid()) {
                continue;
            }

            updated.put(point, block.getBlockData());
            sendBlock(player, world, point, wallBlockData);
        }

        if (updated.isEmpty()) {
            activePoints.remove(player.getUniqueId());
        } else {
            activePoints.put(player.getUniqueId(), updated);
        }
    }

    private Set<BorderPoint> resolvePoints(final RegionBounds bounds, final Location playerLocation) {
        final int px = (int) Math.round(playerLocation.getX());
        final int py = (int) Math.round(playerLocation.getY());
        final int pz = (int) Math.round(playerLocation.getZ());

        final int minX = bounds.minX();
        final int maxX = bounds.maxX();
        final int minY = bounds.minY();
        final int maxY = bounds.maxY();
        final int minZ = bounds.minZ();
        final int maxZ = bounds.maxZ();

        // Cheap gate: player must be within `distance` of the arena bounding box (or inside it).
        if (px + distance < minX || px - distance > maxX || pz + distance < minZ || pz - distance > maxZ
            || py + distance < minY || py - distance > maxY) {
            return new HashSet<>();
        }

        final Set<BorderPoint> points = new HashSet<>();
        for (int dx = -distance; dx <= distance; dx++) {
            for (int dz = -distance; dz <= distance; dz++) {
                final int cx = px + dx;
                final int cz = pz + dz;

                final boolean inside = cx >= minX && cx <= maxX && cz >= minZ && cz <= maxZ;
                if (inside) {
                    continue;
                }

                for (final int[] neighbour : HORIZONTAL_NEIGHBOURS) {
                    final int nx = cx + neighbour[0];
                    final int nz = cz + neighbour[1];

                    final boolean neighbourInside = nx >= minX && nx <= maxX && nz >= minZ && nz <= maxZ;
                    if (!neighbourInside) {
                        continue;
                    }

                    final int loY = Math.max(py - distance, minY);
                    final int hiY = Math.min(py + distance, maxY);
                    for (int y = loY; y <= hiY; y++) {
                        if (isVisible(nx, y, nz, playerLocation)) {
                            points.add(new BorderPoint(nx, y, nz));
                        }
                    }
                }
            }
        }

        return points;
    }

    private static boolean containsBlock(final RegionBounds bounds, final Location location) {
        final int x = location.getBlockX();
        final int y = location.getBlockY();
        final int z = location.getBlockZ();
        return x >= bounds.minX() && x <= bounds.maxX()
            && y >= bounds.minY() && y <= bounds.maxY()
            && z >= bounds.minZ() && z <= bounds.maxZ();
    }

    private RegionBounds lookupBounds(final ArenaImpl arena, final World playerWorld) {
        final Location origin = arena.getPosition(1);
        if (origin == null || origin.getWorld() == null || !origin.getWorld().equals(playerWorld)) {
            return null;
        }

        final RegionBounds cached = boundsCache.get(arena.getName());
        if (cached != null) {
            return cached;
        }

        final RegionBounds resolved = worldGuard.findSmallestRegionBounds(origin);
        if (resolved == null) {
            return null;
        }

        boundsCache.put(arena.getName(), resolved);
        return resolved;
    }

    private boolean isVisible(final int x, final int y, final int z, final Location player) {
        final double dx = x - player.getX();
        final double dy = y - player.getY();
        final double dz = z - player.getZ();
        return (dx * dx + dy * dy + dz * dz) <= (double) distance * (double) distance;
    }

    private void sendBlock(final Player player, final World world, final BorderPoint point, final BlockData data) {
        if (!player.isOnline() || !world.equals(player.getWorld())) {
            return;
        }

        player.sendBlockChange(new Location(world, point.x(), point.y(), point.z()), data);
    }

    private void revert(final Player player, final Iterable<Map.Entry<BorderPoint, BlockData>> entries) {
        if (!player.isOnline()) {
            return;
        }

        final World world = player.getWorld();
        for (final Map.Entry<BorderPoint, BlockData> entry : entries) {
            player.sendBlockChange(new Location(world, entry.getKey().x(), entry.getKey().y(), entry.getKey().z()), entry.getValue());
        }
    }

    private BlockData resolveWallBlockData(final String name) {
        final Material material = EnumUtil.getByName(name, Material.class);
        if (material == null || !material.isBlock()) {
            Log.warn(this, "Invalid arena.boundary.block '" + name + "', boundary wall disabled.");
            return null;
        }

        return material.createBlockData();
    }
}
