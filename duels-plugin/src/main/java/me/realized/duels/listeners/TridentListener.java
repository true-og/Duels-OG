package me.realized.duels.listeners;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import java.lang.ref.WeakReference;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.UUID;
import me.realized.duels.DuelsPlugin;
import me.realized.duels.api.event.match.MatchStartEvent;
import me.realized.duels.arena.ArenaManagerImpl;
import me.realized.duels.player.PlayerInfo;
import me.realized.duels.player.PlayerInfoManager;
import me.realized.duels.util.reflect.ReflectionUtil;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.entity.AbstractArrow.PickupStatus;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Trident;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

/**
 * Returns a trident thrown right before a duel to its owner once the duel is over.
 *
 * A thrown trident lives on as an entity until it is picked back up, so a player that throws one and
 * immediately enters a duel is teleported away from it and loses it: Loyalty cannot follow them into
 * the arena world, and the entity despawns while the match is running. On match start any trident
 * the player still has out is taken out of the world and its item is stashed in their cached
 * {@link PlayerInfo}, which hands it back together with the rest of their inventory once the duel
 * ends (win, loss, tie, disconnect, or server restart - all of those go through PlayerInfo).
 *
 * The trident is moved, never copied: the world entity is removed first and the item is only stashed
 * if that entity was still alive and still pickup-able, so a trident that was already picked up,
 * despawned, or returned by Loyalty yields nothing. Throws that never consumed an item (creative
 * mode, which marks the entity {@link PickupStatus#CREATIVE_ONLY}) and throws made inside a match
 * (kit / cloned-inventory items, which are not the player's own) are not tracked at all.
 */
public class TridentListener implements Listener {

    private static final long TRIDENT_EXPIRY = 5 * 60 * 1000L;
    private static final String ITEM_METHOD_NAME = "getItemStack";

    private static Method itemMethod;
    private static boolean itemMethodResolved;

    private final ArenaManagerImpl arenaManager;
    private final PlayerInfoManager playerManager;

    // Maps a trident thrower to the tridents they have out.
    private final Multimap<UUID, ThrownTrident> tridents = HashMultimap.create();

    public TridentListener(final DuelsPlugin plugin) {
        this.arenaManager = plugin.getArenaManager();
        this.playerManager = plugin.getPlayerManager();
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    /**
     * Reads the item a trident entity is carrying, which is the exact stack that was thrown.
     * Only available on servers exposing AbstractArrow#getItemStack, hence the reflective lookup.
     *
     * @param trident Trident entity to read the item of
     * @return thrown item or null if this server does not expose it
     */
    private static ItemStack itemFrom(final Trident trident) {
        if (!itemMethodResolved) {
            itemMethodResolved = true;
            itemMethod = ReflectionUtil.getMethodUnsafe(trident.getClass(), ITEM_METHOD_NAME);
        }

        if (itemMethod == null) {
            return null;
        }

        try {
            final Object result = itemMethod.invoke(trident);
            return result instanceof ItemStack ? (ItemStack) result : null;
        } catch (final Throwable ignored) {
            return null;
        }
    }

    /**
     * Reads the trident still held by the thrower. At launch time the item has not been consumed
     * yet, so this is the stack that is about to become the entity.
     *
     * @param player Player that threw the trident
     * @return held trident or null if the player is not holding one
     */
    private static ItemStack itemFrom(final Player player) {
        final PlayerInventory inventory = player.getInventory();
        final ItemStack main = inventory.getItemInMainHand();

        if (isTrident(main)) {
            return main;
        }

        final ItemStack off = inventory.getItemInOffHand();
        return isTrident(off) ? off : null;
    }

    private static boolean isTrident(final ItemStack item) {
        return item != null && item.getType() == Material.TRIDENT;
    }

    private void removeExpired(final Player player) {
        final Collection<ThrownTrident> tridents = this.tridents.asMap().get(player.getUniqueId());

        if (tridents == null || tridents.isEmpty()) {
            return;
        }

        final long now = System.currentTimeMillis();
        tridents.removeIf(trident -> now - trident.creation > TRIDENT_EXPIRY || trident.trident.get() == null);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void on(final ProjectileLaunchEvent event) {
        if (event.getEntityType() != EntityType.TRIDENT) {
            return;
        }

        final Trident trident = (Trident) event.getEntity();

        if (!(trident.getShooter() instanceof Player)) {
            return;
        }

        final Player player = (Player) trident.getShooter();

        // Creative throws keep the item in the inventory, so returning one would duplicate it.
        if (player.getGameMode() == GameMode.CREATIVE) {
            return;
        }

        // Ignore tridents thrown in match: those belong to the kit or the inventory clone, not to the player.
        if (arenaManager.isInMatch(player)) {
            return;
        }

        ItemStack item = itemFrom(trident);

        if (!isTrident(item)) {
            item = itemFrom(player);
        }

        // Without the thrown item there is nothing to hand back - never fabricate one.
        if (!isTrident(item)) {
            return;
        }

        final ItemStack thrown = item.clone();
        // A throw only ever puts a single trident in the world, no matter what the source stack held.
        thrown.setAmount(1);

        removeExpired(player);
        tridents.put(player.getUniqueId(), new ThrownTrident(trident, thrown));
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void on(final MatchStartEvent event) {
        for (final Player player : event.getPlayers()) {
            final Collection<ThrownTrident> tridents = this.tridents.asMap().remove(player.getUniqueId());

            if (tridents == null || tridents.isEmpty()) {
                continue;
            }

            final PlayerInfo info = playerManager.get(player);

            // No cached data to stash the item into: leave the trident in the world instead.
            if (info == null) {
                continue;
            }

            for (final ThrownTrident thrown : tridents) {
                final Trident trident = thrown.trident.get();

                // Gone (picked up, despawned, or returned by Loyalty) or never the player's to keep.
                if (trident == null || !trident.isValid() || trident.getPickupStatus() != PickupStatus.ALLOWED) {
                    continue;
                }

                // Take the entity out of the world BEFORE handing out the item so the trident only
                // ever exists in one place. A removed entity is no longer valid, so a repeat pass
                // over the same trident cannot hand out a second copy.
                trident.remove();
                info.getExtra().add(thrown.item);
            }
        }
    }

    @EventHandler
    public void on(final PlayerQuitEvent event) {
        tridents.asMap().remove(event.getPlayer().getUniqueId());
    }

    private static class ThrownTrident {

        private final long creation;
        private final WeakReference<Trident> trident;
        private final ItemStack item;

        ThrownTrident(final Trident trident, final ItemStack item) {
            this.creation = System.currentTimeMillis();
            this.trident = new WeakReference<>(trident);
            this.item = item;
        }
    }
}
