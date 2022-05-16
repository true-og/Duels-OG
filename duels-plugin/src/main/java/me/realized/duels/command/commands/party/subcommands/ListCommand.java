package me.realized.duels.command.commands.party.subcommands;

import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import me.realized.duels.DuelsPlugin;
import me.realized.duels.Permissions;
import me.realized.duels.command.BaseCommand;
import me.realized.duels.party.Party;
import me.realized.duels.util.StringUtil;

public class ListCommand extends BaseCommand {
    
    public ListCommand(final DuelsPlugin plugin) {
        super(plugin, "list", null, null, Permissions.PARTY, 1, true);
    }

    @Override
    protected void execute(final CommandSender sender, final String label, final String[] args) {
        final Player player = (Player) sender;
        final Party party;

        if (args.length > getLength()) {
            if (!sender.hasPermission(Permissions.PARTY_LIST_OTHERS)) {
                lang.sendMessage(sender, "ERROR.no-permission", "permission", Permissions.PARTY_LIST_OTHERS);
                return;
            }

            final Player target = Bukkit.getPlayerExact(args[1]);

            if (target == null || !player.canSee(target)) {
                lang.sendMessage(sender, "ERROR.player.not-found", "name", args[1]);
                return;
            }

            party = partyManager.get(target);

            if (party == null) {
                lang.sendMessage(sender, "ERROR.party.not-in-party.target");
                return;
            }

            showList(sender, party);
            return;
        }

        party = partyManager.get(player);

        if (party == null) {
            lang.sendMessage(sender, "ERROR.party.not-in-party.sender");
            return;
        }

        showList(sender, party);
    }

    private void showList(final CommandSender sender, final Party party) {
        if (party == null) {
            lang.sendMessage(sender, "ERROR.party.not-in-party.sender");
            return;
        }

        final List<String> memberNames = party.getMemberNames();
        lang.sendMessage(sender, "COMMAND.party.list",
            "members_count", memberNames.size(),
            "members", !memberNames.isEmpty() ? StringUtil.join(party.getMemberNames(), ", ") : lang.getMessage("GENERAL.none"),
            "owner", party.getOwner().getName()
        );
    }
}
