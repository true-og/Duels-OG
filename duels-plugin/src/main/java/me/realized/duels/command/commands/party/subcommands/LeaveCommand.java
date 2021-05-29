package me.realized.duels.command.commands.party.subcommands;

import me.realized.duels.DuelsPlugin;
import me.realized.duels.Permissions;
import me.realized.duels.command.BaseCommand;
import org.bukkit.command.CommandSender;

public class LeaveCommand extends BaseCommand {

    public LeaveCommand(final DuelsPlugin plugin) {
        super(plugin, "leave", null, null, Permissions.PARTY, 1, true, "l");
    }

    @Override
    protected void execute(final CommandSender sender, final String label, final String[] args) {
        // TODO: 12/30/20 Implement leave
    }
}
