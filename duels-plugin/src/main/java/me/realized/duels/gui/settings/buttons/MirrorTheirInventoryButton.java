package me.realized.duels.gui.settings.buttons;

import me.realized.duels.DuelsPlugin;
import me.realized.duels.Permissions;
import me.realized.duels.gui.BaseButton;
import me.realized.duels.hook.hooks.LuckPermsHook;
import me.realized.duels.setting.Settings;
import me.realized.duels.util.compat.Items;
import me.realized.duels.util.inventory.ItemBuilder;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class MirrorTheirInventoryButton extends BaseButton {

    public MirrorTheirInventoryButton(final DuelsPlugin plugin) {
        super(plugin, ItemBuilder.of(Items.HEAD.clone()).name(plugin.getLang().getMessage("GUI.settings.buttons.use-mirror-their-inventory.name")).build());
    }

    @Override
    public void update(final Player player) {
        if (config.isMirrorInventoryUsePermission() && !player.hasPermission(Permissions.MIRROR_INVENTORY) && !player.hasPermission(Permissions.SETTING_ALL)) {
            setLore(lang.getMessage("GUI.settings.buttons.use-mirror-their-inventory.lore-no-permission").split("\n"));
            return;
        }

        final Settings settings = settingManager.getSafely(player);

        // Display the opponent's head and rank-colored name so it is obvious whose inventory is cloned.
        final UUID targetId = settings.getTarget();
        final Player target = targetId != null ? Bukkit.getPlayer(targetId) : null;
        final String name = target != null ? LuckPermsHook.coloredName(plugin, target) : lang.getMessage("GENERAL.none");

        if (target != null) {
            setOwner(target);
        }

        setDisplayName(lang.getMessage("GUI.settings.buttons.use-mirror-their-inventory.name", "name", name));

        final String value = settings.isMirrorTheirInventory() ? lang.getMessage("GENERAL.enabled") : lang.getMessage("GENERAL.disabled");
        final String lore = plugin.getLang().getMessage("GUI.settings.buttons.use-mirror-their-inventory.lore", "name", name, "mirror_their_inventory", value);
        setLore(lore.split("\n"));
    }

    @Override
    public void onClick(final Player player) {
        if (config.isMirrorInventoryUsePermission() && !player.hasPermission(Permissions.MIRROR_INVENTORY) && !player.hasPermission(Permissions.SETTING_ALL)) {
            lang.sendMessage(player, "ERROR.no-permission", "permission", Permissions.MIRROR_INVENTORY);
            return;
        }

        final Settings settings = settingManager.getSafely(player);
        settings.setMirrorTheirInventory(!settings.isMirrorTheirInventory());
        settings.updateGui(player);
    }
}
