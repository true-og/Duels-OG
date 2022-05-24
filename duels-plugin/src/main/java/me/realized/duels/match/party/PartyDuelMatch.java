package me.realized.duels.match.party;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import lombok.Getter;
import me.realized.duels.DuelsPlugin;
import me.realized.duels.arena.ArenaImpl;
import me.realized.duels.kit.KitImpl;
import me.realized.duels.match.DuelMatch;
import me.realized.duels.party.Party;
import me.realized.duels.queue.Queue;

public class PartyDuelMatch extends DuelMatch {

    // Track Party instances as player's party status could change during the match.
    @Getter
    private final Map<Player, Party> partyMap = new HashMap<>();
    private final Map<Party, Integer> alivePlayers = new HashMap<>();

    public PartyDuelMatch(final DuelsPlugin plugin, final ArenaImpl arena, final KitImpl kit, final Map<UUID, List<ItemStack>> items, final int bet, final Queue source) {
        super(plugin,arena, kit, items, bet, source);
    }
    
    private int getAlivePlayers(final Party party) {
        return alivePlayers.getOrDefault(party, 0);
    }
    
    @Override
    public void addPlayer(final Player player) {
        super.addPlayer(player);

        final Party party = partyManager.get(player);
        partyMap.put(player, party);

        final Integer count = alivePlayers.get(party);

        if (count == null) {
            alivePlayers.put(party, 1);
            return;
        }

        alivePlayers.put(party, count + 1);
    }

    @Override
    public void markAsDead(Player player) {
        super.markAsDead(player);

        final Party party = partyMap.get(player);

        if (party == null) {
            return;
        }

        final Integer count = alivePlayers.get(party);

        if (count == null) {
            return;
        }

        alivePlayers.put(party, count - 1);
    }

    @Override
    public int size() {
        return (int) alivePlayers.keySet().stream().filter(party -> getAlivePlayers(party) > 0).count();
    }
}
