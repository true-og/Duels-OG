package me.realized.duels.hook.hooks;

import me.realized.duels.DuelsPlugin;
import me.realized.duels.util.StringUtil;
import me.realized.duels.util.hook.PluginHook;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public class LuckPermsHook extends PluginHook<DuelsPlugin> {

    public static final String NAME = "LuckPerms";

    private final LuckPerms api;

    public LuckPermsHook(final DuelsPlugin plugin) {
        super(plugin, NAME);
        this.api = LuckPermsProvider.get();
    }

    /**
     * @return the player's name colored with the formatting of their LuckPerms prefix (e.g. their rank color).
     */
    public String getColoredName(final Player player) {
        return getColor(player) + player.getName();
    }

    private String getColor(final Player player) {
        try {
            final String prefix = api.getPlayerAdapter(Player.class).getMetaData(player).getPrefix();

            if (prefix == null || prefix.isEmpty()) {
                return "";
            }

            // Apply whatever color/format the prefix ends with to the name (e.g. "&c[Admin] " -> a red name).
            return ChatColor.getLastColors(StringUtil.color(prefix));
        } catch (final Exception ex) {
            return "";
        }
    }

    /**
     * Returns the player's rank-colored name if LuckPerms is hooked, otherwise their plain name. Null-safe.
     */
    public static String coloredName(final DuelsPlugin plugin, final Player player) {
        if (player == null) {
            return "";
        }

        final LuckPermsHook hook = plugin.getHookManager().getHook(LuckPermsHook.class);
        return hook != null ? hook.getColoredName(player) : player.getName();
    }
}
