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
    }

    // Only one Settings instance stays in memory while player is online; no need for manual removal of gui from GuiListener
    public Settings getSafely(final Player player) {
        return cache.computeIfAbsent(player.getUniqueId(), result -> new Settings(plugin, player));
    }

    public void requestBetInput(final Player player) {
        awaitingBetInput.add(player.getUniqueId());
        player.closeInventory();
        plugin.getLang().sendMessage(player, "GUI.settings.buttons.diamond-bet.prompt");
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
    }

    private void handleBetInput(final UUID uuid, final String message) {
        final Player player = Bukkit.getPlayer(uuid);

        if (player == null) {
            return;
        }

        final Settings settings = getSafely(player);

        if (message.equalsIgnoreCase("cancel")) {
            plugin.getLang().sendMessage(player, "GUI.settings.buttons.diamond-bet.cancelled");
            settings.openGui(player);
            return;
        }

        final int bet = NumberUtil.parseInt(message).orElse(-1);

        if (bet < 0) {
            plugin.getLang().sendMessage(player, "GUI.settings.buttons.diamond-bet.invalid");
            settings.openGui(player);
            return;
        }

        final DiamondBankHook diamondBank = plugin.getHookManager().getHook(DiamondBankHook.class);

        if (bet > 0 && (diamondBank == null || !diamondBank.has(bet, player))) {
            plugin.getLang().sendMessage(player, "ERROR.command.not-enough-money");
            settings.openGui(player);
            return;
        }

        settings.setBet(bet);
        plugin.getLang().sendMessage(player, "GUI.settings.buttons.diamond-bet.set", "bet_amount", bet);
        settings.openGui(player);
    }
}
