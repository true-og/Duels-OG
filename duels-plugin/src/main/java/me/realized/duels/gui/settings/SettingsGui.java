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
import me.realized.duels.gui.settings.buttons.ShuffleAllButton;
import me.realized.duels.util.compat.Items;
import me.realized.duels.util.gui.SinglePageGui;
import me.realized.duels.util.inventory.Slots;
import org.bukkit.inventory.ItemStack;

public class SettingsGui extends SinglePageGui<DuelsPlugin> {

    // Option buttons flank the middle row around the center slot 13, which is reserved for Shuffle All.
    // The bottom row is reserved for SEND / CANCEL, so options never collide with them.
    private static final int[][] PATTERNS = {
        {12},
        {12, 14},
        {11, 12, 14},
        {11, 12, 14, 15},
        {10, 11, 12, 14, 15},
        {10, 11, 12, 14, 15, 16},
        {9, 10, 11, 12, 14, 15, 16}
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

        // Swap the "Use a Kit" button with the "Both Use ... Inventory" pair so they trade positions.
        // The mirror pair takes Kit's (earlier) slot and Kit takes the opponent-inventory slot, keeping
        // the two "Both Use" buttons contiguous. Placing the pair on Kit's side avoids the center slot
        // (13, Shuffle All) landing between them, which would visually split them in some layouts.
        // All three are behind independent config flags, so only reorder when every one is present.
        final int kitIndex = indexOf(buttons, KitSelectButton.class);
        final int myIndex = indexOf(buttons, MirrorMyInventoryButton.class);
        final int theirIndex = indexOf(buttons, MirrorTheirInventoryButton.class);

        if (kitIndex != -1 && myIndex != -1 && theirIndex != -1) {
            final BaseButton kit = buttons.get(kitIndex);
            final BaseButton mirrorMy = buttons.get(myIndex);
            final BaseButton mirrorTheir = buttons.get(theirIndex);

            final List<BaseButton> reordered = new ArrayList<>(buttons.size());

            for (final BaseButton button : buttons) {
                if (button == kit) {
                    // Kit's slot becomes the mirror pair, kept together in My/Their order.
                    reordered.add(mirrorMy);
                    reordered.add(mirrorTheir);
                } else if (button == mirrorMy) {
                    // Already emitted as part of the pair at the Kit slot.
                    continue;
                } else if (button == mirrorTheir) {
                    // The opponent-inventory slot becomes the Kit button.
                    reordered.add(kit);
                } else {
                    reordered.add(button);
                }
            }

            buttons.clear();
            buttons.addAll(reordered);
        }

        // Middle row: option buttons flanking the center. PATTERNS supports up to 7 options (the max possible).
        if (!buttons.isEmpty()) {
            final int[] pattern = PATTERNS[buttons.size() - 1];

            for (int i = 0; i < buttons.size(); i++) {
                set(pattern[i], buttons.get(i));
            }
        }

        // Center of the window: Shuffle All (randomizes every option except the Diamond bet).
        set(13, new ShuffleAllButton(plugin));

        // Bottom row: SEND on the left half, CANCEL on the right half, divider in the center.
        set(18, 22, 1, new RequestSendButton(plugin));
        set(23, 27, 1, new CancelButton(plugin));
    }

    private static int indexOf(final List<BaseButton> buttons, final Class<? extends BaseButton> type) {
        for (int i = 0; i < buttons.size(); i++) {
            if (type.isInstance(buttons.get(i))) {
                return i;
            }
        }

        return -1;
    }
}
