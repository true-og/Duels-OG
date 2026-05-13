package me.realized.duels.hook.hooks;

import java.lang.reflect.Method;
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

public class PlayerBountiesOGHook extends PluginHook<DuelsPlugin> {

    public static final String NAME = "PlayerBounties-OG";

    private static final String CLAIM_EVENT_CLASS = "com.tcoded.playerbountiesplus.event.BountyClaimEvent";
    private static final String SET_EVENT_CLASS = "com.tcoded.playerbountiesplus.event.BountySetEvent";

    private final Config config;
    private final ArenaManagerImpl arenaManager;

    private final Class<? extends Event> claimEventClass;
    private final Class<? extends Event> setEventClass;
    private final Method getClaimantMethod;
    private final Method getVictimMethod;
    private final Method getTargetMethod;

    public PlayerBountiesOGHook(final DuelsPlugin plugin) {
        super(plugin, NAME);
        this.config = plugin.getConfiguration();
        this.arenaManager = plugin.getArenaManager();

        try {
            this.claimEventClass = Class.forName(CLAIM_EVENT_CLASS).asSubclass(Event.class);
            this.setEventClass = Class.forName(SET_EVENT_CLASS).asSubclass(Event.class);
            this.getClaimantMethod = this.claimEventClass.getMethod("getClaimant");
            this.getVictimMethod = this.claimEventClass.getMethod("getVictim");
            this.getTargetMethod = this.setEventClass.getMethod("getTarget");

            if (!Cancellable.class.isAssignableFrom(this.claimEventClass) || !Cancellable.class.isAssignableFrom(this.setEventClass)) {
                throw new IllegalStateException("PlayerBounties-OG events are not cancellable");
            }
        } catch (ClassNotFoundException | NoSuchMethodException | ClassCastException ex) {
            throw new RuntimeException("This version of " + getName() + " is not supported. Please try upgrading to the latest version.", ex);
        }

        final BountyListener listener = new BountyListener();

        Bukkit.getPluginManager().registerEvent(this.claimEventClass, listener, EventPriority.NORMAL, new EventExecutor() {

            @Override
            public void execute(final Listener listener, final Event event) throws EventException {
                onClaim(event);
            }
        }, plugin, true);

        Bukkit.getPluginManager().registerEvent(this.setEventClass, listener, EventPriority.NORMAL, new EventExecutor() {

            @Override
            public void execute(final Listener listener, final Event event) throws EventException {
                onSet(event);
            }
        }, plugin, true);
    }

    private void onClaim(final Event event) throws EventException {
        if (!config.isPreventBountyLoss()) {
            return;
        }

        final Player claimant;
        final Player victim;

        try {
            claimant = (Player) getClaimantMethod.invoke(event);
            victim = (Player) getVictimMethod.invoke(event);
        } catch (ReflectiveOperationException | ClassCastException ex) {
            throw new EventException(ex);
        }

        if ((claimant != null && arenaManager.isInMatch(claimant)) || (victim != null && arenaManager.isInMatch(victim))) {
            ((Cancellable) event).setCancelled(true);
        }
    }

    private void onSet(final Event event) throws EventException {
        if (!config.isPreventBountyLoss()) {
            return;
        }

        final Player target;

        try {
            target = (Player) getTargetMethod.invoke(event);
        } catch (ReflectiveOperationException | ClassCastException ex) {
            throw new EventException(ex);
        }

        if (target != null && arenaManager.isInMatch(target)) {
            ((Cancellable) event).setCancelled(true);
        }
    }

    public class BountyListener implements Listener {}
}
