package me.realized.duels.setting;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import me.realized.duels.DuelsPlugin;
import me.realized.duels.hook.hooks.DiamondBankHook;
import me.realized.duels.util.Loadable;
import me.realized.duels.util.NumberUtil;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class SettingsManager implements Loadable, Listener {

    private final DuelsPlugin plugin;
    private final Map<UUID, Settings> cache = new HashMap<>();
    private final Set<UUID> awaitingBetInput = ConcurrentHashMap.newKeySet();
    // Players who have already used their single "too high" retry for the current bet-input sequence.
    private final Set<UUID> betInputRetried = ConcurrentHashMap.newKeySet();

    public SettingsManager(final DuelsPlugin plugin) {
        this.plugin = plugin;
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    @Override
    public void handleLoad() {}

    @Override
    public void handleUnload() {
        cache.clear();
        awaitingBetInput.clear();
        betInputRetried.clear();
    }

    // Only one Settings instance stays in memory while player is online; no need for manual removal of gui from GuiListener
    public Settings getSafely(final Player player) {
        return cache.computeIfAbsent(player.getUniqueId(), result -> new Settings(plugin, player));
    }

    public void requestBetInput(final Player player) {
        // Clicking the bet button starts a fresh sequence, so the one-time retry is available again.
        betInputRetried.remove(player.getUniqueId());
        awaitingBetInput.add(player.getUniqueId());
        player.closeInventory();

        final Settings settings = getSafely(player);
        final Player target = settings.getTarget() != null ? Bukkit.getPlayer(settings.getTarget()) : null;
        final DiamondBankHook diamondBank = plugin.getHookManager().getHook(DiamondBankHook.class);
        // The maximum bet is limited by the opponent's balance, since a bet they cannot cover is rejected.
        final int max = (target != null && diamondBank != null) ? diamondBank.getDiamonds(target) : 0;
        final String name = target != null ? target.getName() : plugin.getLang().getMessage("GENERAL.none");
        plugin.getLang().sendMessage(player, "GUI.settings.buttons.diamond-bet.prompt", "name", name, "max", max);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void on(final AsyncPlayerChatEvent event) {
        final UUID uuid = event.getPlayer().getUniqueId();

        if (!awaitingBetInput.remove(uuid)) {
            return;
        }

        event.setCancelled(true);

        final String message = event.getMessage().trim();
        plugin.doSync(() -> handleBetInput(uuid, message));
    }

    @EventHandler
    public void on(final PlayerQuitEvent event) {
        final UUID uuid = event.getPlayer().getUniqueId();
        cache.remove(uuid);
        awaitingBetInput.remove(uuid);
        betInputRetried.remove(uuid);
    }

    private void handleBetInput(final UUID uuid, final String message) {
        final Player player = Bukkit.getPlayer(uuid);

        if (player == null) {
            return;
        }

        final Settings settings = getSafely(player);

        if (message.equalsIgnoreCase("cancel")) {
            betInputRetried.remove(uuid);
            plugin.getLang().sendMessage(player, "GUI.settings.buttons.diamond-bet.cancelled");
            settings.openGui(player);
            return;
        }

        final int bet = NumberUtil.parseInt(message).orElse(-1);

        if (bet < 0) {
            betInputRetried.remove(uuid);
            plugin.getLang().sendMessage(player, "GUI.settings.buttons.diamond-bet.invalid");
            settings.openGui(player);
            return;
        }

        if (bet > 0) {
            final DiamondBankHook diamondBank = plugin.getHookManager().getHook(DiamondBankHook.class);

            if (diamondBank == null || !diamondBank.has(bet, player)) {
                betTooHigh(player, () -> plugin.getLang().sendMessage(player, "ERROR.command.not-enough-money"));
                return;
            }

            final Player target = settings.getTarget() != null ? Bukkit.getPlayer(settings.getTarget()) : null;

            // Reject a bet the opponent cannot cover so the match is not doomed to fail on start.
            if (target != null && !diamondBank.has(bet, target)) {
                betTooHigh(player, () -> plugin.getLang().sendMessage(player, "ERROR.command.opponent-not-enough-money",
                    "name", target.getName(), "max", diamondBank.getDiamonds(target), "bet_amount", bet));
                return;
            }
        }

        betInputRetried.remove(uuid);
        settings.setBet(bet);
        plugin.getLang().sendMessage(player, "GUI.settings.buttons.diamond-bet.set", "bet_amount", bet);
        settings.openGui(player);
    }

    // A bet that exceeds either balance gives the player one more chance to type an amount in chat.
    // Once that single retry is used, they must send a new duel request rather than reopening the GUI.
    private void betTooHigh(final Player player, final Runnable reasonMessage) {
        final UUID uuid = player.getUniqueId();
        reasonMessage.run();

        if (betInputRetried.add(uuid)) {
            awaitingBetInput.add(uuid);
            plugin.getLang().sendMessage(player, "GUI.settings.buttons.diamond-bet.retry");
            return;
        }

        betInputRetried.remove(uuid);
        plugin.getLang().sendMessage(player, "GUI.settings.buttons.diamond-bet.retry-failed");
    }
}
