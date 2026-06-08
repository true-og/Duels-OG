package me.realized.duels.data;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import me.realized.duels.player.PlayerInfo;
import me.realized.duels.util.Log;
import org.bukkit.GameMode;
import org.bukkit.inventory.ItemStack;

public class PlayerData {

    private static transient final String ITEM_LOAD_FAILURE = "Could not load item %s!";

    public static PlayerData fromPlayerInfo(final PlayerInfo info) {
        return new PlayerData(info);
    }

    private Map<String, Map<Integer, ItemData>> items = new HashMap<>();
    private Collection<PotionEffectData> effects = new ArrayList<>();
    private double health;
    private int hunger;
    private int level;
    private float exp;
    private int totalExperience;
    private LocationData location;
    private List<ItemData> extra = new ArrayList<>();
    private String restoreGameMode;
    private boolean restoreFlight;
    private boolean allowFlight;
    private boolean flying;
    private boolean forceReturnLocation;

    private PlayerData() {}

    private PlayerData(final PlayerInfo info) {
        this.health = info.getHealth();
        this.hunger = info.getHunger();
        this.level = info.getLevel();
        this.exp = info.getExp();
        this.totalExperience = info.getTotalExperience();
        this.location = LocationData.fromLocation(info.getLocation());
        this.restoreGameMode = info.isRestoreGameMode() && info.getGameMode() != null ? info.getGameMode().name() : null;
        this.restoreFlight = info.isRestoreFlight();
        this.allowFlight = info.isAllowFlight();
        this.flying = info.isFlying();
        this.forceReturnLocation = info.isForceReturnLocation();

        for (final Map.Entry<String, Map<Integer, ItemStack>> entry : info.getItems().entrySet()) {
            final Map<Integer, ItemData> data = new HashMap<>();
            entry.getValue().entrySet()
                .stream()
                .filter(value -> Objects.nonNull(value.getValue()))
                .forEach(value -> data.put(value.getKey(), ItemData.fromItemStack(value.getValue())));
            items.put(entry.getKey(), data);
        }

        info.getExtra().forEach(item -> extra.add(ItemData.fromItemStack(item)));
    }

    public PlayerInfo toPlayerInfo() {
        GameMode gameMode = null;

        if (restoreGameMode != null) {
            try {
                gameMode = GameMode.valueOf(restoreGameMode);
            } catch (IllegalArgumentException ignored) {}
        }

        final PlayerInfo info = new PlayerInfo(
            effects.stream().map(PotionEffectData::toPotionEffect).filter(Objects::nonNull).collect(Collectors.toList()),
            health,
            hunger,
            level,
            exp,
            totalExperience,
            location.toLocation(),
            gameMode,
            restoreFlight,
            allowFlight,
            flying,
            forceReturnLocation
        );

        for (final Map.Entry<String, Map<Integer, ItemData>> entry : items.entrySet()) {
            final Map<Integer, ItemStack> data = new HashMap<>();
            entry.getValue().forEach(((slot, itemData) -> {
                final ItemStack item = itemData.toItemStack(false);

                if (item == null) {
                    Log.warn(String.format(ITEM_LOAD_FAILURE, itemData.toString()));
                    return;
                }

                data.put(slot, item);
            }));
            info.getItems().put(entry.getKey(), data);
        }

        info.getExtra().addAll(extra.stream().map(data -> data.toItemStack(false)).filter(Objects::nonNull).collect(Collectors.toList()));
        return info;
    }
}
