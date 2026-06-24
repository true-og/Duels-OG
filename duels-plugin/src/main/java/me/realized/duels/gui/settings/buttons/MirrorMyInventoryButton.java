package me.realized.duels.gui.settings.buttons;

import me.realized.duels.DuelsPlugin;
import me.realized.duels.Permissions;
import me.realized.duels.gui.BaseButton;
import me.realized.duels.setting.Settings;
import me.realized.duels.util.compat.Items;
import me.realized.duels.util.inventory.ItemBuilder;
import org.bukkit.entity.Player;

public class MirrorMyInventoryButton extends BaseButton {

    public MirrorMyInventoryButton(final DuelsPlugin plugin) {
        super(plugin, ItemBuilder.of(Items.HEAD.clone()).name(plugin.getLang().getMessage("GUI.settings.buttons.use-mirror-my-inventory.name")).build());
    }

    @Override
    public void update(final Player player) {
        if (config.isMirrorInventoryUsePermission() && !player.hasPermission(Permissions.MIRROR_INVENTORY) && !player.hasPermission(Permissions.SETTING_ALL)) {
            setLore(lang.getMessage("GUI.settings.buttons.use-mirror-my-inventory.lore-no-permission").split("\n"));
            return;
        }

        // Display the request sender's (challenger's) head so it is obvious whose inventory is cloned.
        setOwner(player);

        final Settings settings = settingManager.getSafely(player);
        final String value = settings.isMirrorMyInventory() ? lang.getMessage("GENERAL.enabled") : lang.getMessage("GENERAL.disabled");
        final String lore = plugin.getLang().getMessage("GUI.settings.buttons.use-mirror-my-inventory.lore", "mirror_my_inventory", value);
        setLore(lore.split("\n"));
    }

    @Override
    public void onClick(final Player player) {
        if (config.isMirrorInventoryUsePermission() && !player.hasPermission(Permissions.MIRROR_INVENTORY) && !player.hasPermission(Permissions.SETTING_ALL)) {
            lang.sendMessage(player, "ERROR.no-permission", "permission", Permissions.MIRROR_INVENTORY);
            return;
        }

        final Settings settings = settingManager.getSafely(player);
        settings.setMirrorMyInventory(!settings.isMirrorMyInventory());
        settings.updateGui(player);
    }
}
