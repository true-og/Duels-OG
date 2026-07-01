package me.realized.duels.hook.hooks;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import me.realized.duels.DuelsPlugin;
import me.realized.duels.util.Log;
import me.realized.duels.util.hook.PluginHook;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;

public class DiamondBankHook extends PluginHook<DuelsPlugin> {

    public static final String NAME = "DiamondBank-OG";

    private static final String API_CLASS = "net.trueog.diamondbankog.api.DiamondBankAPIJava";
    private static final String PLUGIN_NOTE = "Plugin: Duels-OG";

    private final Object api;
    private final Method diamondsToShards;
    private final Method shardsToDiamonds;
    private final Method getBankShards;
    private final Method getInventoryShards;
    private final Method consumeFromPlayer;
    private final Method addToPlayerBankShards;

    public DiamondBankHook(final DuelsPlugin plugin) {
        super(plugin, NAME);

        try {
            final Class<?> apiClass = Class.forName(API_CLASS);
            final RegisteredServiceProvider<?> provider = Bukkit.getServicesManager().getRegistration(apiClass);

            if (provider == null) {
                throw new IllegalStateException("DiamondBank-OG API service is not registered.");
            }

            api = provider.getProvider();
            diamondsToShards = apiClass.getMethod("diamondsToShards", Double.TYPE);
            shardsToDiamonds = apiClass.getMethod("shardsToDiamonds", Long.TYPE);
            getBankShards = apiClass.getMethod("getBankShards", java.util.UUID.class);
            getInventoryShards = apiClass.getMethod("getInventoryShards", java.util.UUID.class);
            consumeFromPlayer = apiClass.getMethod("consumeFromPlayer", java.util.UUID.class, Long.TYPE, String.class, String.class);
            addToPlayerBankShards = apiClass.getMethod("addToPlayerBankShards", java.util.UUID.class, Long.TYPE, String.class, String.class);
            Log.info("Using DiamondBank-OG for duel betting.");
        } catch (ReflectiveOperationException | IllegalStateException ex) {
            throw new IllegalStateException("Failed to hook DiamondBank-OG API: " + ex.getMessage(), ex);
        }
    }

    public boolean has(final int amount, final Player... players) {
        try {
            final long shards = toShards(amount);

            for (final Player player : players) {
                if (totalShards(player) < shards) {
                    return false;
                }
            }

            return true;
        } catch (ReflectiveOperationException | RuntimeException ex) {
            Log.warn("Failed to check DiamondBank-OG balance: " + ex.getMessage());
            return false;
        }
    }

    /**
     * @return the whole number of Diamonds the given player can currently spend (bank + inventory), or 0 on failure.
     */
    public int getDiamonds(final Player player) {
        try {
            return ((Number) invoke(shardsToDiamonds, totalShards(player))).intValue();
        } catch (ReflectiveOperationException | RuntimeException ex) {
            Log.warn("Failed to read DiamondBank-OG balance: " + ex.getMessage());
            return 0;
        }
    }

    public boolean add(final int amount, final Player... players) {
        try {
            final long shards = toShards(amount);

            for (final Player player : players) {
                addToBank(player, shards, "Duels-OG bet payout", "Paid " + format(shards) + " Diamonds to " + player.getName() + ".");
            }

            return true;
        } catch (ReflectiveOperationException | RuntimeException ex) {
            Log.warn("Failed to pay DiamondBank-OG duel bet: " + ex.getMessage());
            return false;
        }
    }

    public boolean remove(final int amount, final Player... players) {
        final List<Player> charged = new ArrayList<>();

        try {
            final long shards = toShards(amount);

            for (final Player player : players) {
                consume(player, shards, "Duels-OG bet placed", player.getName() + " wagered " + format(shards) + " Diamonds.");
                charged.add(player);
            }

            return true;
        } catch (ReflectiveOperationException | RuntimeException ex) {
            Log.warn("Failed to withdraw DiamondBank-OG duel bet: " + ex.getMessage());
            refundCharged(amount, charged);
            return false;
        }
    }

    private long toShards(final int amount) throws ReflectiveOperationException {
        return ((Number) invoke(diamondsToShards, (double) amount)).longValue();
    }

    private long totalShards(final Player player) throws ReflectiveOperationException {
        final long bankShards = ((Number) invoke(getBankShards, player.getUniqueId())).longValue();
        final long inventoryShards = ((Number) invoke(getInventoryShards, player.getUniqueId())).longValue();
        return bankShards + inventoryShards;
    }

    private void consume(final Player player, final long shards, final String reason, final String notes) throws ReflectiveOperationException {
        invoke(consumeFromPlayer, player.getUniqueId(), shards, reason, notes + " " + PLUGIN_NOTE);
    }

    private void addToBank(final Player player, final long shards, final String reason, final String notes) throws ReflectiveOperationException {
        invoke(addToPlayerBankShards, player.getUniqueId(), shards, reason, notes + " " + PLUGIN_NOTE);
    }

    private String format(final long shards) throws ReflectiveOperationException {
        return String.valueOf(invoke(shardsToDiamonds, shards));
    }

    private Object invoke(final Method method, final Object... args) throws ReflectiveOperationException {
        try {
            return method.invoke(api, args);
        } catch (InvocationTargetException ex) {
            final Throwable cause = ex.getCause();

            if (cause instanceof RuntimeException) {
                throw (RuntimeException) cause;
            }

            if (cause instanceof Error) {
                throw (Error) cause;
            }

            throw new IllegalStateException(cause != null ? cause.getMessage() : ex.getMessage(), cause);
        }
    }

    private void refundCharged(final int amount, final List<Player> charged) {
        if (charged.isEmpty()) {
            return;
        }

        try {
            final long shards = toShards(amount);

            for (final Player player : charged) {
                addToBank(player, shards, "Duels-OG bet rollback", "Refunded " + format(shards) + " Diamonds to " + player.getName() + ".");
            }
        } catch (ReflectiveOperationException | RuntimeException ex) {
            Log.warn("Failed to rollback DiamondBank-OG duel bet: " + ex.getMessage());
        }
    }
}
