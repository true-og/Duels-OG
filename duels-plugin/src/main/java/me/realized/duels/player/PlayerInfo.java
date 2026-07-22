package me.realized.duels.player;

import com.google.common.collect.Lists;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.Getter;
import lombok.Setter;
import me.realized.duels.util.PlayerUtil;
import me.realized.duels.util.inventory.InventoryUtil;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;

public class PlayerInfo {

    @Getter
    private final Map<String, Map<Integer, ItemStack>> items = new HashMap<>();
    @Getter
    private final List<PotionEffect> effects;
    @Getter
    private final double health;
    @Getter
    private final int hunger;
    @Getter
    private final int level;
    @Getter
    private final float exp;
    @Getter
    private final int totalExperience;
    @Getter
    private final List<ItemStack> extra = new ArrayList<>();
    @Getter
    @Setter
    private Location location;
    @Getter
    private final GameMode gameMode;
    @Getter
    private final boolean restoreGameMode;
    @Getter
    private final boolean restoreFlight;
    @Getter
    private final boolean allowFlight;
    @Getter
    private final boolean flying;
    @Getter
    private final boolean forceReturnLocation;
    private final PlayerRideState rideState;

    public PlayerInfo(final List<PotionEffect> effects, final double health, final int hunger, final Location location) {
        this(effects, health, hunger, 0, 0F, 0, location, null, false, false, false, false);
    }

    public PlayerInfo(final List<PotionEffect> effects, final double health, final int hunger,
        final int level, final float exp, final int totalExperience, final Location location,
        final GameMode gameMode, final boolean restoreFlight, final boolean allowFlight, final boolean flying,
        final boolean forceReturnLocation) {
        this.effects = effects;
        this.health = health;
        this.hunger = hunger;
        this.level = level;
        this.exp = exp;
        this.totalExperience = totalExperience;
        this.location = location;
        this.gameMode = gameMode;
        this.restoreGameMode = gameMode != null;
        this.restoreFlight = restoreFlight;
        this.allowFlight = allowFlight;
        this.flying = flying;
        this.forceReturnLocation = forceReturnLocation;
        this.rideState = null;
    }

    public PlayerInfo(final Player player, final boolean excludeInventory) {
        this(player, excludeInventory, null, player.getAllowFlight(), player.isFlying(), player.isFlying(), player.getLocation());
    }

    public PlayerInfo(final Player player, final boolean excludeInventory, final GameMode gameMode, final boolean allowFlight,
        final boolean flying, final boolean forceReturnLocation, final Location location) {
        this.effects = Lists.newArrayList(player.getActivePotionEffects());
        this.health = player.getHealth();
        this.hunger = player.getFoodLevel();
        this.level = player.getLevel();
        this.exp = player.getExp();
        this.totalExperience = player.getTotalExperience();
        this.location = location != null ? location.clone() : player.getLocation().clone();
        this.gameMode = gameMode;
        this.restoreGameMode = gameMode != null;
        this.restoreFlight = allowFlight || flying;
        this.allowFlight = allowFlight;
        this.flying = flying;
        this.forceReturnLocation = forceReturnLocation;
        this.rideState = PlayerRideState.capture(player);

        if (!forceReturnLocation && rideState != null && rideState.getReturnLocation() != null) {
            this.location = rideState.getReturnLocation();
        }

        if (excludeInventory) {
            return;
        }
        
        InventoryUtil.addToMap(player.getInventory(), items);
    }

    public void restore(final Player player) {
        final double maxHealth = PlayerUtil.getMaxHealth(player);
        player.addPotionEffects(effects);
        player.setHealth(health > maxHealth ? maxHealth : health);
        player.setFoodLevel(hunger);
        player.setLevel(level);
        player.setExp(exp);
        player.setTotalExperience(totalExperience);
        InventoryUtil.fillFromMap(player.getInventory(), items);
        InventoryUtil.addOrDrop(player, extra);
        player.updateInventory();

        if (restoreGameMode && gameMode != null && player.getGameMode() != gameMode) {
            player.setGameMode(gameMode);
        }

        if (forceReturnLocation && location != null && location.getWorld() != null) {
            player.teleport(location);
        }

        if (restoreFlight) {
            if (allowFlight) {
                player.setAllowFlight(true);
            }

            player.setFlying(allowFlight && flying);
            player.setAllowFlight(allowFlight);
        }

        if (rideState != null) {
            rideState.restore(player);
        }
    }

}
