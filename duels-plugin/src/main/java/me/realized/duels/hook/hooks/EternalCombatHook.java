package me.realized.duels.hook.hooks;

import java.lang.reflect.Method;
import java.util.UUID;
import me.realized.duels.DuelsPlugin;
import me.realized.duels.arena.ArenaManagerImpl;
import me.realized.duels.config.Config;
import me.realized.duels.util.hook.PluginHook;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.EventException;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.plugin.EventExecutor;
import org.bukkit.plugin.Plugin;

public class EternalCombatHook extends PluginHook<DuelsPlugin> {

    public static final String NAME = "EternalCombat";

    private static final String API_CLASS = "com.eternalcode.combat.EternalCombatApi";
    private static final String FIGHT_MANAGER_CLASS = "com.eternalcode.combat.fight.FightManager";
    private static final String FIGHT_TAG_EVENT_CLASS = "com.eternalcode.combat.fight.event.FightTagEvent";

    private final Config config;
    private final ArenaManagerImpl arenaManager;
    private final Method getFightManagerMethod;
    private final Method isInCombatMethod;
    private final Method getPlayerMethod;
    private final Class<? extends Event> fightTagEventClass;

    public EternalCombatHook(final DuelsPlugin plugin) {
        super(plugin, NAME);
        this.config = plugin.getConfiguration();
        this.arenaManager = plugin.getArenaManager();

        try {
            final Class<?> apiClass = Class.forName(API_CLASS);
            final Plugin target = getPlugin();

            if (!apiClass.isInstance(target)) {
                throw new IllegalStateException(getName() + " does not expose " + API_CLASS);
            }

            this.getFightManagerMethod = apiClass.getMethod("getFightManager");
            this.isInCombatMethod = Class.forName(FIGHT_MANAGER_CLASS).getMethod("isInCombat", UUID.class);
            this.fightTagEventClass = Class.forName(FIGHT_TAG_EVENT_CLASS).asSubclass(Event.class);
            this.getPlayerMethod = this.fightTagEventClass.getMethod("getPlayer");

            if (!Cancellable.class.isAssignableFrom(this.fightTagEventClass)) {
                throw new IllegalStateException(FIGHT_TAG_EVENT_CLASS + " is not cancellable");
            }
        } catch (ClassNotFoundException | NoSuchMethodException | ClassCastException ex) {
            throw new RuntimeException("This version of " + getName() + " is not supported. Please try upgrading to the latest version.", ex);
        }

        Bukkit.getPluginManager().registerEvent(this.fightTagEventClass, new EternalCombatListener(), EventPriority.NORMAL, new EventExecutor() {

            @Override
            public void execute(final Listener listener, final Event event) throws EventException {
                onFightTag(event);
            }
        }, plugin, true);
    }

    public boolean isTagged(final Player player) {
        if (!config.isEcPreventDuel()) {
            return false;
        }

        final Plugin target = getPlugin();

        if (target == null || !target.isEnabled()) {
            return false;
        }

        try {
            final Object fightManager = getFightManagerMethod.invoke(target);
            return fightManager != null && (boolean) isInCombatMethod.invoke(fightManager, player.getUniqueId());
        } catch (ReflectiveOperationException | ClassCastException ex) {
            return false;
        }
    }

    private void onFightTag(final Event event) throws EventException {
        if (!config.isEcPreventTag()) {
            return;
        }

        final UUID playerId;

        try {
            playerId = (UUID) getPlayerMethod.invoke(event);
        } catch (ReflectiveOperationException | ClassCastException ex) {
            throw new EventException(ex);
        }

        final Player player = Bukkit.getPlayer(playerId);

        if (player == null || !arenaManager.isInMatch(player)) {
            return;
        }

        ((Cancellable) event).setCancelled(true);
    }

    public class EternalCombatListener implements Listener {}
}
