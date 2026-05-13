package me.realized.duels.hook.hooks;

import me.realized.duels.DuelsPlugin;
import me.realized.duels.util.hook.PluginHook;
import org.bukkit.entity.Player;

public class GameModeInventoriesHook extends PluginHook<DuelsPlugin> {

    public static final String NAME = "GameModeInventories-OG";

    private static final String USE_PERMISSION = "gamemodeinventories.use";

    public GameModeInventoriesHook(final DuelsPlugin plugin) {
        super(plugin, NAME);
    }

    public boolean canSwitchInventories(final Player player) {
        return getPlugin() != null && getPlugin().isEnabled() && player.hasPermission(USE_PERMISSION);
    }
}
