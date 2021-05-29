package me.realized.duels.match.matches;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import java.util.Collection;
import me.realized.duels.DuelsPlugin;
import me.realized.duels.arena.ArenaImpl;
import me.realized.duels.kit.KitImpl;
import me.realized.duels.match.AbstractMatch;
import me.realized.duels.match.MatchPlayer;
import me.realized.duels.party.Party;
import org.bukkit.entity.Player;

public class PartyDuelMatch extends AbstractMatch {

    private final Multimap<Party, MatchPlayer> parties = HashMultimap.create();

    public PartyDuelMatch(final DuelsPlugin plugin, final ArenaImpl arena, final KitImpl kit) {
        super(plugin, arena, kit, 0, null);
    }

    private boolean hasAlivePlayer(final Party party) {
        final Collection<MatchPlayer> players;
        return (players = parties.asMap().get(party)) != null && players.stream().anyMatch(MatchPlayer::isAlive);
    }

    public boolean isInMatch(final Party party) {
        return parties.containsKey(party);
    }

    public boolean isInSameParty(final Player first, final Player second) {
        return parties.asMap().values().stream().anyMatch(players -> {
            final MatchPlayer matchFirst = getPlayer(first);

            if (matchFirst == null) {
                return false;
            }

            final MatchPlayer matchSecond = getPlayer(second);

            if (matchSecond == null) {
                return false;
            }

            return players.contains(matchFirst) && players.contains(matchSecond);
        });
    }


    @Override
    public MatchPlayer addPlayer(final Player player) {
        final MatchPlayer matchPlayer = super.addPlayer(player);
        parties.put(partyManager.get(player), matchPlayer);
        return matchPlayer;
    }

    @Override
    public int size() {
        return (int) parties.keySet().stream().filter(this::hasAlivePlayer).count();
    }
}
