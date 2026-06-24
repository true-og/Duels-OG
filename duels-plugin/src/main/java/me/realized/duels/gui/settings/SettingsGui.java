package me.realized.duels.gui.settings;

import java.util.ArrayList;
import java.util.List;
import me.realized.duels.DuelsPlugin;
import me.realized.duels.config.Config;
import me.realized.duels.gui.BaseButton;
import me.realized.duels.gui.settings.buttons.ArenaSelectButton;
import me.realized.duels.gui.settings.buttons.CancelButton;
import me.realized.duels.gui.settings.buttons.DiamondBetButton;
import me.realized.duels.gui.settings.buttons.ItemBettingButton;
import me.realized.duels.gui.settings.buttons.KitSelectButton;
import me.realized.duels.gui.settings.buttons.MirrorMyInventoryButton;
import me.realized.duels.gui.settings.buttons.MirrorTheirInventoryButton;
import me.realized.duels.gui.settings.buttons.OwnInventoryButton;
import me.realized.duels.gui.settings.buttons.RequestDetailsButton;
import me.realized.duels.gui.settings.buttons.RequestSendButton;
import me.realized.duels.util.compat.Items;
import me.realized.duels.util.gui.SinglePageGui;
import me.realized.duels.util.inventory.Slots;
import org.bukkit.inventory.ItemStack;

public class SettingsGui extends SinglePageGui<DuelsPlugin> {

    // Option buttons are centered in the middle row (slots 9-16). Center is slot 13.
    // The bottom row is reserved for SEND / CANCEL, so options never collide with them.
    private static final int[][] PATTERNS = {
        {13},
        {12, 14},
        {12, 13, 14},
        {11, 12, 14, 15},
        {11, 12, 13, 14, 15},
        {10, 11, 12, 14, 15, 16},
        {10, 11, 12, 13, 14, 15, 16}
    };

    public SettingsGui(final DuelsPlugin plugin) {
        super(plugin, plugin.getLang().getMessage("GUI.settings.title"), 3);
        final Config config = plugin.getConfiguration();
        final ItemStack spacing = Items.from(config.getSettingsFillerType(), config.getSettingsFillerData());
        // Fill the whole window first, then paint buttons on top so nothing is left as a gap.
        Slots.run(0, 27, slot -> inventory.setItem(slot, spacing));

        // Top row: request details, centered.
        set(4, new RequestDetailsButton(plugin));

        final List<BaseButton> buttons = new ArrayList<>();

        if (config.isKitSelectingEnabled()) {
            buttons.add(new KitSelectButton(plugin));
        }

        if (config.isOwnInventoryEnabled()) {
            buttons.add(new OwnInventoryButton(plugin));
        }

        if (config.isMirrorInventoryEnabled()) {
            buttons.add(new MirrorMyInventoryButton(plugin));
            buttons.add(new MirrorTheirInventoryButton(plugin));
        }

        if (config.isArenaSelectingEnabled()) {
            buttons.add(new ArenaSelectButton(plugin));
        }

        if (config.isMoneyBettingEnabled()) {
            buttons.add(new DiamondBetButton(plugin));
        }

        if (config.isItemBettingEnabled()) {
            buttons.add(new ItemBettingButton(plugin));
        }

        // Middle row: option buttons, centered. PATTERNS supports up to 7 options (the max possible).
        if (!buttons.isEmpty()) {
            final int[] pattern = PATTERNS[buttons.size() - 1];

            for (int i = 0; i < buttons.size(); i++) {
                set(pattern[i], buttons.get(i));
            }
        }

        // Bottom row: SEND on the left half, CANCEL on the right half, divider in the center.
        set(18, 22, 1, new RequestSendButton(plugin));
        set(23, 27, 1, new CancelButton(plugin));
    }
}
