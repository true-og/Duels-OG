package me.realized.duels.gui.settings;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import me.realized.duels.DuelsPlugin;
import me.realized.duels.Permissions;
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
import me.realized.duels.setting.Settings;
import me.realized.duels.util.compat.Items;
import me.realized.duels.util.gui.SinglePageGui;
import me.realized.duels.util.inventory.ItemBuilder;
import me.realized.duels.util.inventory.Slots;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class SettingsGui extends SinglePageGui<DuelsPlugin> {

    private static final int SHUFFLE_SLOT = 22;

    // Option buttons flank the middle row around the center slot 22, which is reserved for Shuffle All.
    // Rows above and below the options are reserved for gray/lime selection frames.
    // The bottom row is reserved for SEND / CANCEL, so options never collide with them.
    private static final int[][] PATTERNS = {
        {21},
        {21, 23},
        {20, 21, 23},
        {20, 21, 23, 24},
        {19, 20, 21, 23, 24},
        {19, 20, 21, 23, 24, 25},
        {18, 19, 20, 21, 23, 24, 25}
    };

    private final Map<Integer, BaseButton> framedButtons = new LinkedHashMap<>();
    private final Config config;
    private final ItemStack selectedFrame;
    private final ItemStack unselectedFrame;

    public SettingsGui(final DuelsPlugin plugin) {
        super(plugin, plugin.getLang().getMessage("GUI.settings.title"), 5);
        this.config = plugin.getConfiguration();
        final ItemStack filler = Items.from(config.getSettingsFillerType(), config.getSettingsFillerData());
        this.selectedFrame = ItemBuilder.of(Items.ORANGE_PANE.clone()).name(" ").build();
        this.unselectedFrame = ItemBuilder.of(Items.GRAY_PANE.clone()).name(" ").build();

        // Fill the whole window first, then paint buttons on top so nothing is left as a gap.
        Slots.run(0, 45, slot -> inventory.setItem(slot, filler));

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
        // (22, Shuffle All) landing between them, which would visually split them in some layouts.
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
                setFramed(pattern[i], buttons.get(i));
            }
        }

        // Center of the window: Shuffle All (randomizes every option except the Diamond bet).
        setFramed(SHUFFLE_SLOT, new ShuffleAllButton(plugin));

        // Bottom row: SEND on the left half, CANCEL on the right half, divider in the center.
        set(36, 40, 1, new RequestSendButton(plugin));
        set(41, 45, 1, new CancelButton(plugin));
    }

    @Override
    public void update(final Player player) {
        super.update(player);
        updateSelectionFrames(player);
    }

    private void setFramed(final int slot, final BaseButton button) {
        set(slot, button);
        framedButtons.put(slot, button);
    }

    private void updateSelectionFrames(final Player player) {
        for (final Map.Entry<Integer, BaseButton> entry : framedButtons.entrySet()) {
            final int slot = entry.getKey();
            final ItemStack frame = isSelected(player, entry.getValue()) ? selectedFrame : unselectedFrame;

            inventory.setItem(slot - 9, frame.clone());
            inventory.setItem(slot + 9, frame.clone());
        }
    }

    private boolean isSelected(final Player player, final BaseButton button) {
        final Settings settings = plugin.getSettingManager().getSafely(player);

        if (button instanceof KitSelectButton) {
            return hasPermission(player, config.isKitSelectingUsePermission(), Permissions.KIT_SELECTING) && settings.getKit() != null;
        }

        if (button instanceof OwnInventoryButton) {
            return hasPermission(player, config.isOwnInventoryUsePermission(), Permissions.OWN_INVENTORY) && settings.isOwnInventory();
        }

        if (button instanceof MirrorMyInventoryButton || button instanceof MirrorTheirInventoryButton) {
            if (!hasPermission(player, config.isMirrorInventoryUsePermission(), Permissions.MIRROR_INVENTORY)) {
                return false;
            }

            return button instanceof MirrorMyInventoryButton ? settings.isMirrorMyInventory() : settings.isMirrorTheirInventory();
        }

        if (button instanceof ArenaSelectButton) {
            return hasPermission(player, config.isArenaSelectingUsePermission(), Permissions.ARENA_SELECTING) && settings.getArena() != null;
        }

        if (button instanceof DiamondBetButton) {
            return hasPermission(player, config.isMoneyBettingUsePermission(), Permissions.MONEY_BETTING) && settings.getBet() > 0;
        }

        if (button instanceof ItemBettingButton) {
            return hasPermission(player, config.isItemBettingUsePermission(), Permissions.ITEM_BETTING) && settings.isItemBetting();
        }

        if (button instanceof ShuffleAllButton) {
            return settings.isShuffleActive();
        }

        return false;
    }

    private boolean hasPermission(final Player player, final boolean usePermission, final String permission) {
        return !usePermission || player.hasPermission(permission) || player.hasPermission(Permissions.SETTING_ALL);
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
