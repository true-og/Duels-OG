package me.realized.duels.listeners;

import me.realized.duels.DuelsPlugin;
import me.realized.duels.arena.ArenaImpl;
import me.realized.duels.arena.ArenaManagerImpl;
import me.realized.duels.config.Config;
import me.realized.duels.util.EventUtil;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

/**
 * Restricts in-duel damage so a player in a match can only damage their match opponent. Also
 * un-cancels damage between the two duelists when 'force-allow-combat' is on, so other plugins
 * (factions, anti-pvp, etc.) cannot block the duel itself.
 */
public class DamageListener implements Listener {

    private final ArenaManagerImpl arenaManager;
    private final Config config;

    public DamageListener(final DuelsPlugin plugin) {
        this.arenaManager = plugin.getArenaManager();
        this.config = plugin.getConfiguration();

        plugin.doSyncAfter(() -> Bukkit.getPluginManager().registerEvents(this, plugin), 1L);
    }

    // Run before HIGHEST lethal-damage rescue handlers. In particular, PurpurExtras-OG
    // cancels lethal damage at HIGHEST after consuming a totem from the victim's hotbar.
    // Running at the same priority could undo that rescue when force-allow-combat is enabled.
    @EventHandler(priority = EventPriority.HIGH)
    public void on(final EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof Player)) {
            return;
        }

        final Player victim = (Player) event.getEntity();
        final Player damager = EventUtil.getDamager(event);

        if (damager == null || damager.equals(victim)) {
            return;
        }

        final ArenaImpl attackerArena = arenaManager.get(damager);

        if (attackerArena == null) {
            return;
        }

        final ArenaImpl victimArena = arenaManager.get(victim);

        // Attacker is in a duel; the only legal target is their match opponent (same arena).
        if (victimArena != attackerArena) {
            event.setCancelled(true);
            return;
        }

        // Same arena - allow the hit. Un-cancel only when the duel hasn't been decided yet,
        // so post-victory hits stay blocked.
        if (config.isForceAllowCombat() && event.isCancelled() && !attackerArena.isEndGame()) {
            event.setCancelled(false);
        }
    }
}
