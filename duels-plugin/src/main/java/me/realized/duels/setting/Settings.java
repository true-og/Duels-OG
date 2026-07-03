package me.realized.duels.setting;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;
import me.realized.duels.DuelsPlugin;
import me.realized.duels.arena.ArenaImpl;
import me.realized.duels.gui.settings.SettingsGui;
import me.realized.duels.kit.KitImpl;
import org.bukkit.Location;
import org.bukkit.entity.Player;

public class Settings {

    private final DuelsPlugin plugin;
    private final SettingsGui gui;

    @Getter
    private UUID target;
    @Getter
    private KitImpl kit;
    @Getter
    @Setter
    private ArenaImpl arena;
    @Getter
    @Setter
    private int bet;
    @Getter
    @Setter
    private boolean itemBetting;
    @Getter
    private boolean ownInventory;
    @Getter
    private boolean mirrorMyInventory;
    @Getter
    private boolean mirrorTheirInventory;
    @Getter
    private Map<UUID, CachedInfo> cache = new HashMap<>();

    // Snapshot of the shuffleable options as of the most recent Shuffle All, used to glint the Shuffle
    // button only while the current selection still matches what that shuffle produced. Diamond bet is
    // excluded because Shuffle All never touches it.
    private boolean shuffled;
    private KitImpl shuffledKit;
    private ArenaImpl shuffledArena;
    private boolean shuffledItemBetting;
    private boolean shuffledOwnInventory;
    private boolean shuffledMirrorMyInventory;
    private boolean shuffledMirrorTheirInventory;

    public Settings(final DuelsPlugin plugin, final Player player) {
        this.plugin = plugin;
        this.gui = player != null ? plugin.getGuiListener().addGui(player, new SettingsGui(plugin)) : null;
        // If kits are disabled, then ownInventory is enabled by default.
        this.ownInventory = !plugin.getConfiguration().isKitSelectingEnabled();
    }

    public Settings(final DuelsPlugin plugin) {
        this(plugin, null);
    }

    public void reset() {
        target = null;
        kit = null;
        arena = null;
        bet = 0;
        itemBetting = false;
        ownInventory = !plugin.getConfiguration().isKitSelectingEnabled();
        mirrorMyInventory = false;
        mirrorTheirInventory = false;
        shuffled = false;
    }

    // Record the current shuffleable options as the result of a Shuffle All click.
    public void markShuffled() {
        shuffled = true;
        shuffledKit = kit;
        shuffledArena = arena;
        shuffledItemBetting = itemBetting;
        shuffledOwnInventory = ownInventory;
        shuffledMirrorMyInventory = mirrorMyInventory;
        shuffledMirrorTheirInventory = mirrorTheirInventory;
    }

    // True while every shuffleable option still matches the most recent Shuffle All. Any manual change
    // to one of those options makes this false, so the Shuffle button stops glinting.
    public boolean isShuffleActive() {
        return shuffled
            && kit == shuffledKit
            && arena == shuffledArena
            && itemBetting == shuffledItemBetting
            && ownInventory == shuffledOwnInventory
            && mirrorMyInventory == shuffledMirrorMyInventory
            && mirrorTheirInventory == shuffledMirrorTheirInventory;
    }

    public void setTarget(final Player target) {
        if (this.target != null && !this.target.equals(target.getUniqueId())) {
            reset();
        }

        this.target = target.getUniqueId();
    }

    public void updateGui(final Player player) {
        if (gui != null) {
            gui.update(player);
        }
    }

    public void openGui(final Player player) {
        gui.open(player);
    }

    public void setBaseLoc(final Player player) {
        cache.computeIfAbsent(player.getUniqueId(), result -> new CachedInfo()).setLocation(player.getLocation().clone());
    }

    public Location getBaseLoc(final Player player) {
        final CachedInfo info = cache.get(player.getUniqueId());

        if (info == null) {
            return null;
        }

        return info.getLocation();
    }

    public void setDuelzone(final Player player, final String duelzone) {
        cache.computeIfAbsent(player.getUniqueId(), result -> new CachedInfo()).setDuelzone(duelzone);
    }

    public String getDuelzone(final Player player) {
        final CachedInfo info = cache.get(player.getUniqueId());

        if (info == null) {
            return null;
        }

        return info.getDuelzone();
    }

    public void setKit(final KitImpl kit) {
        this.kit = kit;
        this.ownInventory = false;
        this.mirrorMyInventory = false;
        this.mirrorTheirInventory = false;
    }

    // The four inventory-mode flags (kit, ownInventory, mirrorMyInventory, mirrorTheirInventory)
    // are mutually exclusive: turning one on must clear the others.
    public void setOwnInventory(final boolean ownInventory) {
        this.ownInventory = ownInventory;

        if (ownInventory) {
            this.kit = null;
            this.mirrorMyInventory = false;
            this.mirrorTheirInventory = false;
        }
    }

    public void setMirrorMyInventory(final boolean mirrorMyInventory) {
        this.mirrorMyInventory = mirrorMyInventory;

        if (mirrorMyInventory) {
            this.kit = null;
            this.ownInventory = false;
            this.mirrorTheirInventory = false;
        }
    }

    public void setMirrorTheirInventory(final boolean mirrorTheirInventory) {
        this.mirrorTheirInventory = mirrorTheirInventory;

        if (mirrorTheirInventory) {
            this.kit = null;
            this.ownInventory = false;
            this.mirrorMyInventory = false;
        }
    }

    // Don't copy the gui since it won't be required to start a match
    public Settings lightCopy() {
        final Settings copy = new Settings(plugin);
        copy.target = target;
        copy.kit = kit;
        copy.arena = arena;
        copy.bet = bet;
        copy.itemBetting = itemBetting;
        copy.ownInventory = ownInventory;
        copy.mirrorMyInventory = mirrorMyInventory;
        copy.mirrorTheirInventory = mirrorTheirInventory;
        copy.cache = new HashMap<>(cache);
        return copy;
    }
}
