package me.realized.duels.util.gui;

import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import me.realized.duels.util.StringUtil;
import me.realized.duels.util.compat.CompatUtil;
import me.realized.duels.util.compat.Items;
import me.realized.duels.util.compat.Skulls;
import me.realized.duels.util.inventory.ItemBuilder;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.plugin.java.JavaPlugin;

public class Button<P extends JavaPlugin> {

    protected final P plugin;
    private ItemStack displayed;

    public Button(final P plugin, final ItemStack displayed) {
        this.plugin = plugin;
        this.displayed = displayed;
    }

    public ItemStack getDisplayed() {
        return displayed;
    }

    public void setDisplayed(ItemStack displayed) {
        this.displayed = displayed;
    }

    protected void editMeta(final Consumer<ItemMeta> consumer) {
        final ItemMeta meta = getDisplayed().getItemMeta();
        consumer.accept(meta);
        getDisplayed().setItemMeta(meta);
    }

    protected void setDisplayName(final String name) {
        editMeta(meta -> {
            Component displayName = Component.text(StringUtil.color(name));
            meta.displayName(displayName);
        });
    }

    protected void setLore(final List<String> lore) {
        editMeta(meta -> {
            List<Component> components = lore.stream()
                    .map(line -> Component.text(StringUtil.color(line)))
                    .collect(Collectors.toList());
            meta.lore(components);
        });
    }

    protected void setLore(final String... lore) {
        setLore(Arrays.asList(lore));
    }

    protected void setOwner(final Player player) {
        if (Items.equals(displayed, Items.HEAD)) {
            editMeta(meta -> Skulls.setProfile((SkullMeta) meta, player));
        }
    }

    protected void setGlow(final boolean glow) {
        // Normal golden apples do not have enchantment glint even with an enchantment applied, so we change the item
        // type.
        if (displayed.getType().name().endsWith("GOLDEN_APPLE")) {
            final ItemStack item = glow
                    ? Items.ENCHANTED_GOLDEN_APPLE.clone()
                    : ItemBuilder.of(Material.GOLDEN_APPLE).build();
            item.setItemMeta(getDisplayed().getItemMeta());
            setDisplayed(item);
            return;
        }

        editMeta(meta -> {
            if (glow) {
                meta.addEnchant(Enchantment.DURABILITY, 1, false);

                if (CompatUtil.hasItemFlag()) {
                    meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
                }
            } else {
                meta.removeEnchant(Enchantment.DURABILITY);

                if (CompatUtil.hasItemFlag()) {
                    meta.removeItemFlags(ItemFlag.HIDE_ENCHANTS);
                }
            }
        });
    }

    public void update(final Player player) {}

    public void onClick(final Player player) {}
}
