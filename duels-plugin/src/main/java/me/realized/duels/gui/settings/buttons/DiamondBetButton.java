package me.realized.duels.gui.settings.buttons;

import me.realized.duels.DuelsPlugin;
import me.realized.duels.Permissions;
import me.realized.duels.gui.BaseButton;
import me.realized.duels.hook.hooks.DiamondBankHook;
import me.realized.duels.setting.Settings;
import me.realized.duels.util.inventory.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.entity.Player;

public class DiamondBetButton extends BaseButton {

    public DiamondBetButton(final DuelsPlugin plugin) {
        super(plugin, ItemBuilder.of(Material.DIAMOND).name(plugin.getLang().getMessage("GUI.settings.buttons.diamond-bet.name")).build());
    }

    @Override
    public void update(final Player player) {
        if (config.isMoneyBettingUsePermission() && !player.hasPermission(Permissions.MONEY_BETTING) && !player.hasPermission(Permissions.SETTING_ALL)) {
            setLore(lang.getMessage("GUI.settings.buttons.diamond-bet.lore-no-permission").split("\n"));
            return;
        }

        final Settings settings = settingManager.getSafely(player);
        final int bet = settings.getBet();
        getDisplayed().setAmount(Math.max(1, Math.min(64, bet)));
        setLore(lang.getMessage("GUI.settings.buttons.diamond-bet.lore", "bet_amount", bet).split("\n"));
    }

    @Override
    public void onClick(final Player player) {
        if (config.isMoneyBettingUsePermission() && !player.hasPermission(Permissions.MONEY_BETTING) && !player.hasPermission(Permissions.SETTING_ALL)) {
            lang.sendMessage(player, "ERROR.no-permission", "permission", Permissions.MONEY_BETTING);
            return;
        }

        final DiamondBankHook diamondBank = plugin.getHookManager().getHook(DiamondBankHook.class);

        if (diamondBank == null) {
            lang.sendMessage(player, "ERROR.setting.disabled-option", "option", lang.getMessage("GENERAL.betting"));
            return;
        }

        settingManager.requestBetInput(player);
    }
}
