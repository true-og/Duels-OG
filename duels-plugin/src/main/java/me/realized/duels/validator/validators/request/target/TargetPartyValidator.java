package me.realized.duels.validator.validators.request.target;

import java.util.Collection;

import org.bukkit.entity.Player;

import me.realized.duels.DuelsPlugin;
import me.realized.duels.party.Party;
import me.realized.duels.util.function.Pair;
import me.realized.duels.validator.BaseTriValidator;

public class TargetPartyValidator extends BaseTriValidator<Pair<Player, Player>, Party, Collection<Player>> {

    public TargetPartyValidator(final DuelsPlugin plugin) {
        super(plugin);
    }

    @Override
    public boolean validate(final Pair<Player, Player> pair, final Party party, final Collection<Player> players) {
        // Skip for 1v1s
        if (party == null) {
            if (partyManager.isInParty(pair.getKey())) {
                lang.sendMessage(pair.getKey(), "ERROR.party.not-in-party.target", "name", pair.getValue().getName());
                return false;
            }

            return true;
        }

        if (!partyManager.isInParty(pair.getKey())) {
            lang.sendMessage(pair.getKey(), "ERROR.party.not-in-party.sender", "name", pair.getKey().getName());
            return false;
        }

        // If sender is in the same party as target
        if (party.isMember(pair.getKey())) {
            lang.sendMessage(pair.getKey(), "ERROR.party.in-same-party", "name", pair.getValue().getName());
            return false;
        }
        
        if (players.size() != party.size()) {
            lang.sendMessage(pair.getKey(), "ERROR.party.is-not-online.target", "name", pair.getValue().getName());
            return false;
        }
        
        return true;
    }

}
