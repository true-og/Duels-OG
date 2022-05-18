package me.realized.duels.validator.validators.request.self;

import java.util.Collection;

import org.bukkit.entity.Player;

import me.realized.duels.DuelsPlugin;
import me.realized.duels.party.Party;
import me.realized.duels.validator.BaseBiValidator;

public class SelfPartyOnlineValidator extends BaseBiValidator<Player, Collection<Player>> {

    public SelfPartyOnlineValidator(final DuelsPlugin plugin) {
        super(plugin);
    }

    @Override
    public boolean validate(final Player sender, final Collection<Player> players) {
        final Party party = partyManager.get(sender);

        // Skip for 1v1s
        if (party == null) {
            return true;
        }

        if (players.size() != party.size()) {
            lang.sendMessage(sender, "ERROR.party.is-not-online.sender");
            return false;
        }

        return true;
    }
    
}
