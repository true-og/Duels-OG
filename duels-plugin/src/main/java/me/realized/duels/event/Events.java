package me.realized.duels.event;

import me.realized.duels.api.event.request.RequestAcceptEvent;
import me.realized.duels.api.event.request.RequestDenyEvent;
import me.realized.duels.api.event.request.RequestSendEvent;
import me.realized.duels.api.request.Request;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public final class Events {

    public static boolean callRequestSendEvent(final Player sender, final Player target, final Request request) {
        final RequestSendEvent event = new RequestSendEvent(sender, target, request);
        Bukkit.getPluginManager().callEvent(event);
        return event.isCancelled();
    }

    public static boolean callRequestAcceptEvent(final Player player, final Player target, final Request request) {
        final RequestAcceptEvent event = new RequestAcceptEvent(player, target, request);
        Bukkit.getPluginManager().callEvent(event);
        return event.isCancelled();
    }

    public static void callRequestDenyEvent(final Player player, final Player target, final Request request) {
        final RequestDenyEvent event = new RequestDenyEvent(player, target, request);
        Bukkit.getPluginManager().callEvent(event);
    }

    private Events() {}
}
