package me.realized.duels.validator.validators.request.target;

import java.util.Collection;

import org.bukkit.entity.Player;

import me.realized.duels.DuelsPlugin;
import me.realized.duels.party.Party;
import me.realized.duels.validator.BaseTriValidator;

public class TargetCheckSpectateValidator extends BaseTriValidator<Player, Player, Collection<Player>> {
    
    public TargetCheckSpectateValidator(final DuelsPlugin plugin) {
        super(plugin);
    }

    @Override
    public boolean validate(final Player sender, final Player target, final Collection<Player> players) {
        final Party party = partyManager.get(target);

        // Skip for 1v1s
        if (party == null) {
            // If sender is but target is not in a party
            if (partyManager.isInParty(sender)) {
                // TODO send message
                return false;
            }
            return true;
        }

        // If sender is in the same party as target
        if (party.isMember(sender)) {

            // TODO send message
            return false;
        }

        if (players.size() != party.size()) {
            lang.sendMessage(sender, "ERROR.party.is-not-online.target", "name", target.getName());
            return false;
        }
        
        return true;
    }
}
