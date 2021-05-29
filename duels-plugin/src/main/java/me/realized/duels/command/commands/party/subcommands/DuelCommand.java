package me.realized.duels.command.commands.party.subcommands;

import me.realized.duels.DuelsPlugin;
import me.realized.duels.Permissions;
import me.realized.duels.command.BaseCommand;
import me.realized.duels.party.Party;
import me.realized.duels.setting.CachedInfo;
import me.realized.duels.setting.Settings;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class DuelCommand extends BaseCommand {

    public DuelCommand(final DuelsPlugin plugin) {
        super(plugin, "duel", null, null, Permissions.PARTY, 3, true);
    }

    @Override
    protected void execute(final CommandSender sender, final String label, final String[] args) {
        final Player player = (Player) sender;
        final Party party = partyManager.get(player);

        if (party == null) {
            lang.sendMessage(sender, "ERROR.party.not-in-party.sender");
            return;
        }

        if (!party.isOwner(player)) {
            lang.sendMessage(sender, "ERROR.party.is-not-owner");
            return;
        }

        if (!party.isAllOnline()) {
            lang.sendMessage(sender, "ERROR.party.is-not-online.sender");
            return;
        }

        final Player target = Bukkit.getPlayerExact(args[1]);

        if (target == null || !player.canSee(target)) {
            lang.sendMessage(sender, "ERROR.player.not-found", "name", args[1]);
            return;
        }

        final Party targetParty = partyManager.get(target);

        if (targetParty == null) {
            lang.sendMessage(sender, "ERROR.party.not-in-party.target", "name", target.getName());
            return;
        }

        if (targetParty.equals(party)) {
            lang.sendMessage(sender, "ERROR.party.in-same-party", "name", target.getName());
            return;
        }

        if (targetParty.size() != party.size()) {
            lang.sendMessage(sender, "ERROR.party.is-not-same-size");
            return;
        }

        if (!targetParty.isAllOnline()) {
            lang.sendMessage(sender, "ERROR.party.is-not-online.target", "name", target.getName());
            return;
        }

        final Settings setting = new Settings(plugin);
        setting.setKit(kitManager.get(args[2]));
        party.getOnlineMembers().forEach(member -> setting.getCache().put(member.getUniqueId(), new CachedInfo(member.getLocation().clone(), null)));
        duelManager.startPartyMatch(party, targetParty, setting);
    }
}
