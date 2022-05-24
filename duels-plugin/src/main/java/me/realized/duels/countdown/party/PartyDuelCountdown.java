package me.realized.duels.countdown.party;

import java.util.stream.Collectors;

import org.bukkit.entity.Player;

import me.realized.duels.DuelsPlugin;
import me.realized.duels.arena.ArenaImpl;
import me.realized.duels.countdown.DuelCountdown;
import me.realized.duels.match.party.PartyDuelMatch;
import me.realized.duels.party.Party;
import me.realized.duels.party.PartyMember;
import me.realized.duels.util.StringUtil;
import me.realized.duels.util.compat.Titles;

public class PartyDuelCountdown extends DuelCountdown {

    private final PartyDuelMatch match;

    public PartyDuelCountdown(final DuelsPlugin plugin, final ArenaImpl arena, final PartyDuelMatch match) {
        super(plugin, arena, match, plugin.getConfiguration().getCdPartyDuelMessages(), plugin.getConfiguration().getCdPartyDuelTitles());
        this.match = match;
    }

    private Party getOpponent(final Party party) {
        return match.getPartyMap().values().stream().filter(other -> !party.equals(other)).findFirst().orElse(null);
    }
    
    @Override
    protected void sendMessage(final String rawMessage, final String message, final String title) {
        final String kitName = match.getKit() != null ? match.getKit().getName() : lang.getMessage("GENERAL.none");
        match.getPartyMap().entrySet().forEach(entry -> {
            final Player player = entry.getKey();
            config.playSound(player, rawMessage);
            player.sendMessage(message
                .replace("%opponents%", StringUtil.join(getOpponent(entry.getValue()).getMembers().stream().map(PartyMember::getName).collect(Collectors.toList()), ","))
                .replace("%kit%", kitName)
                .replace("%arena%", arena.getName())
            );
            
            if (title != null) {
                Titles.send(player, title, null, 0, 20, 50);
            }
        });
    }
}
