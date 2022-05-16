package me.realized.duels.command.commands.party.subcommands;

import org.bukkit.command.CommandSender;

import me.realized.duels.DuelsPlugin;
import me.realized.duels.Permissions;
import me.realized.duels.command.BaseCommand;

public class LeaveCommand extends BaseCommand {

    public LeaveCommand(final DuelsPlugin plugin) {
        super(plugin, "leave", null, null, Permissions.PARTY, 1, true, "l");
    }

    @Override
    protected void execute(CommandSender sender, String label, String[] args) {
        // TODO implement leave
    }
}
