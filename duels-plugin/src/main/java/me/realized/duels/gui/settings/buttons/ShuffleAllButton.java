package me.realized.duels.gui.settings.buttons;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import me.realized.duels.DuelsPlugin;
import me.realized.duels.Permissions;
import me.realized.duels.api.kit.Kit;
import me.realized.duels.gui.BaseButton;
import me.realized.duels.kit.KitImpl;
import me.realized.duels.setting.Settings;
import me.realized.duels.util.inventory.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.entity.Player;

public class ShuffleAllButton extends BaseButton {

    public ShuffleAllButton(final DuelsPlugin plugin) {
        super(plugin, ItemBuilder.of(Material.NETHER_STAR).name(plugin.getLang().getMessage("GUI.settings.buttons.shuffle-all.name")).build());
    }

    @Override
    public void update(final Player player) {
        // Glint the button only while the current selection still matches the most recent shuffle.
        setGlow(settingManager.getSafely(player).isShuffleActive());
        setLore(lang.getMessage("GUI.settings.buttons.shuffle-all.lore").split("\n"));
    }

    @Override
    public void onClick(final Player player) {
        final Settings settings = settingManager.getSafely(player);
        final ThreadLocalRandom random = ThreadLocalRandom.current();

        // Randomly pick one of the enabled and permitted inventory/kit modes. They are mutually exclusive.
        final List<Runnable> modes = new ArrayList<>();

        if (config.isKitSelectingEnabled() && allowed(player, config.isKitSelectingUsePermission(), Permissions.KIT_SELECTING)) {
            final KitImpl kit = randomKit(player, random);

            if (kit != null) {
                modes.add(() -> settings.setKit(kit));
            }
        }

        if (config.isOwnInventoryEnabled() && allowed(player, config.isOwnInventoryUsePermission(), Permissions.OWN_INVENTORY)) {
            modes.add(() -> settings.setOwnInventory(true));
        }

        if (config.isMirrorInventoryEnabled() && allowed(player, config.isMirrorInventoryUsePermission(), Permissions.MIRROR_INVENTORY)) {
            modes.add(() -> settings.setMirrorMyInventory(true));
            modes.add(() -> settings.setMirrorTheirInventory(true));
        }

        if (!modes.isEmpty()) {
            modes.get(random.nextInt(modes.size())).run();
        }

        // Random arena for the chosen mode (null = Random, chosen when the match starts).
        if (config.isArenaSelectingEnabled() && allowed(player, config.isArenaSelectingUsePermission(), Permissions.ARENA_SELECTING)) {
            settings.setArena(arenaManager.randomArena(settings.getKit()));
        }

        // Random item betting toggle. Diamond betting is intentionally left untouched.
        if (config.isItemBettingEnabled() && allowed(player, config.isItemBettingUsePermission(), Permissions.ITEM_BETTING)) {
            settings.setItemBetting(random.nextBoolean());
        }

        // Remember this shuffle result so the button glints until an option is changed away from it.
        settings.markShuffled();
        settings.updateGui(player);
        lang.sendMessage(player, "GUI.settings.buttons.shuffle-all.shuffled");
    }

    private boolean allowed(final Player player, final boolean usePermission, final String permission) {
        return !usePermission || player.hasPermission(permission) || player.hasPermission(Permissions.SETTING_ALL);
    }

    private KitImpl randomKit(final Player player, final ThreadLocalRandom random) {
        final List<KitImpl> usable = new ArrayList<>();

        for (final Kit kit : kitManager.getKits()) {
            final KitImpl impl = (KitImpl) kit;

            if (canUseKit(player, impl)) {
                usable.add(impl);
            }
        }

        return usable.isEmpty() ? null : usable.get(random.nextInt(usable.size()));
    }

    private boolean canUseKit(final Player player, final KitImpl kit) {
        if (!kit.isUsePermission()) {
            return true;
        }

        final String permission = String.format(Permissions.KIT, kit.getName().replace(" ", "-").toLowerCase());
        return player.hasPermission(Permissions.KIT_ALL) || player.hasPermission(permission);
    }
}
