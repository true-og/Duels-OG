package me.realized.duels.command.commands.party.subcommands;

import me.realized.duels.DuelsPlugin;
import me.realized.duels.Permissions;
import me.realized.duels.command.BaseCommand;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class DisbandCommand extends BaseCommand {

    public DisbandCommand(final DuelsPlugin plugin) {
        super(plugin, "disband", null, null, Permissions.PARTY, 1, true);
    }

    @Override
    protected void execute(final CommandSender sender, final String label, final String[] args) {
        final Player player = (Player) sender;
        // TODO: 12/30/20 Implement disband
        partyManager.disband(player);
    }
}
