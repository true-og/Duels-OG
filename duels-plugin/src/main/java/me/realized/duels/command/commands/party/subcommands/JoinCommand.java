package me.realized.duels.command.commands.party.subcommands;

import me.realized.duels.DuelsPlugin;
import me.realized.duels.Permissions;
import me.realized.duels.command.BaseCommand;
import me.realized.duels.party.Party;
import me.realized.duels.party.PartyInvite;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class JoinCommand extends BaseCommand {

    public JoinCommand(final DuelsPlugin plugin) {
        super(plugin, "join", null, null, Permissions.PARTY, 2, true, "j");
    }

    @Override
    protected void execute(final CommandSender sender, final String label, final String[] args) {
        final Player player = (Player) sender;

        if (partyManager.isInParty(player)) {
            lang.sendMessage(sender, "ERROR.party.already-in-party.sender");
            return;
        }

        final Player target = Bukkit.getPlayerExact(args[1]);

        if (target == null || !player.canSee(target)) {
            lang.sendMessage(sender, "ERROR.player.not-found", "name", args[1]);
            return;
        }

        final PartyInvite invite = partyManager.removeInvite(target, player);

        if (invite == null) {
            lang.sendMessage(sender, "ERROR.party.no-invite", "name", target.getName());
            return;
        }

        lang.sendMessage(player, "COMMAND.party.invite.accept.receiver", "name", target.getName());

        final Party party = invite.getParty();
        partyManager.join(player, party);
        lang.sendMessage(party.getOnlineMembers(), "COMMAND.party.invite.accept.members", "name", player.getName());
    }
}
