package me.realized.duels.command.commands.duel.subcommands;

import me.realized.duels.DuelsPlugin;
import me.realized.duels.command.BaseCommand;
import me.realized.duels.event.Events;
import me.realized.duels.hook.hooks.worldguard.WorldGuardHook;
import me.realized.duels.request.RequestImpl;
import me.realized.duels.setting.Settings;
import me.realized.duels.util.validate.Validators;
import me.realized.duels.validator.validators.request.target.TargetCheckToggleValidator;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class AcceptCommand extends BaseCommand {

    private final WorldGuardHook worldGuard;

    public AcceptCommand(final DuelsPlugin plugin) {
        super(plugin, "accept", "accept [player]", "Accepts a duel request.", 2, true);
        this.worldGuard = plugin.getHookManager().getHook(WorldGuardHook.class);
    }

    @Override
    protected void execute(final CommandSender sender, final String label, final String[] args) {
        final Player player = (Player) sender;

        if (!Validators.validate(requestManager.getSelfValidators(), player)) {
            return;
        }

        final Player target = Bukkit.getPlayerExact(args[1]);

        if (!Validators.validate(requestManager.getTargetValidators(), player, target, TargetCheckToggleValidator.class)) {
            return;
        }

        final RequestImpl request = requestManager.remove(target, player);

        if (Events.callRequestAcceptEvent(player, target, request)) {
            return;
        }

        final Settings settings = request.getSettings();
        final String kit = settings.getKit() != null ? settings.getKit().getName() : lang.getMessage("GENERAL.not-selected");
        final String arena = settings.getArena() != null ? settings.getArena().getName() : lang.getMessage("GENERAL.random");
        final double bet = settings.getBet();
        final String itemBetting = settings.isItemBetting() ? lang.getMessage("GENERAL.enabled") : lang.getMessage("GENERAL.disabled");
        lang.sendMessage(player, "COMMAND.duel.request.accept.receiver",
            "name", args[1], "kit", kit, "arena", arena, "bet_amount", bet, "item_betting", itemBetting);
        lang.sendMessage(target, "COMMAND.duel.request.accept.sender",
            "name", player.getName(), "kit", kit, "arena", arena, "bet_amount", bet, "item_betting", itemBetting);

        // If Item Betting GUI is displayed, there is a chance player could move before start of match.
        // Populates settings with player location info to prevent such case.
        if (settings.isItemBetting()) {
            settings.setBaseLoc(player);
            settings.setDuelzone(player, worldGuard != null ? worldGuard.findDuelZone(player) : null);
            bettingManager.open(settings, target, player);
            return;
        }

        duelManager.startDuelMatch(player, target, settings, null, null);
    }
}