package me.realized.duels.command.commands.party.subcommands;

import org.bukkit.command.CommandSender;

import me.realized.duels.DuelsPlugin;
import me.realized.duels.Permissions;
import me.realized.duels.command.BaseCommand;

public class DisbandCommand extends BaseCommand {

    public DisbandCommand(final DuelsPlugin plugin) {
        super(plugin, "disband", null, null, Permissions.PARTY, 1, true);
    }

    @Override
    protected void execute(CommandSender sender, String label, String[] args) {
        // TODO implement disband
    }
}
