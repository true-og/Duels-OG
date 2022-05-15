package me.realized.duels.match.party;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Multimap;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import me.realized.duels.arena.ArenaImpl;
import me.realized.duels.kit.KitImpl;
import me.realized.duels.match.DuelMatch;
import me.realized.duels.party.Party;
import me.realized.duels.queue.Queue;

public class PartyDuelMatch extends DuelMatch {

    private final Multimap<Party, Player> parties = HashMultimap.create();

    public PartyDuelMatch(final ArenaImpl arena, final KitImpl kit, final Map<UUID, List<ItemStack>> items, final int bet, final Queue source) {
        super(arena, kit, items, bet, source);
    }
    
    private boolean hasAlivePlayer(final Party party) {
        final Collection<Player> players;
        return (players = parties.asMap().get(party)) != null && players.stream().anyMatch(player -> !isDead(player));
    }

    public boolean isInMatch(final Party party) {
        return parties.containsKey(party);
    }

    // TODO add addPlayer to DuelMatch
    @Override
    public void addPlayer(final Player player) {
        super.addPlayer(player);
        parties.put(partyManager.get(player), player);
    }

    // TODO rename size() so that it has a clearer meaning
    @Override
    public int size() {
        return (int) parties.keySet().stream().filter(this::hasAlivePlayer).count();
    }
}
