package me.realized.duels.command.commands.duel.subcommands;

import me.realized.duels.DuelsPlugin;
import me.realized.duels.command.BaseCommand;
import me.realized.duels.event.Events;
import me.realized.duels.request.RequestImpl;
import me.realized.duels.util.validate.Validators;
import me.realized.duels.validator.validators.request.target.TargetCheckMatchValidator;
import me.realized.duels.validator.validators.request.target.TargetCheckSpectateValidator;
import me.realized.duels.validator.validators.request.target.TargetCheckToggleValidator;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class DenyCommand extends BaseCommand {

    public DenyCommand(final DuelsPlugin plugin) {
        super(plugin, "deny", "deny [player]", "Declines a duel request.", 2, true);
    }

    @Override
    protected void execute(final CommandSender sender, final String label, final String[] args) {
        final Player player = (Player) sender;
        final Player target = Bukkit.getPlayerExact(args[1]);

        if (!Validators.validate(requestManager.getTargetValidators(), player, target,
            TargetCheckMatchValidator.class,
            TargetCheckSpectateValidator.class,
            TargetCheckToggleValidator.class
        )) {
            return;
        }

        final RequestImpl request = requestManager.remove(target, player);
        Events.callRequestDenyEvent(player, target, request);
        lang.sendMessage(player, "COMMAND.duel.request.deny.receiver", "name", args[1]);
        lang.sendMessage(target, "COMMAND.duel.request.deny.sender", "name", player.getName());
    }
}