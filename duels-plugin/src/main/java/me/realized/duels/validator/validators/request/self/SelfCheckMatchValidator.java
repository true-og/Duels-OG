package me.realized.duels.validator.validators.request.self;

import java.util.Collection;

import org.bukkit.entity.Player;

import me.realized.duels.DuelsPlugin;
import me.realized.duels.party.Party;
import me.realized.duels.validator.BaseTriValidator;

public class SelfCheckMatchValidator extends BaseTriValidator<Player, Party, Collection<Player>> {
    
    public SelfCheckMatchValidator(final DuelsPlugin plugin) {
        super(plugin);
    }

    @Override
    public boolean validate(final Player sender, final Party party, final Collection<Player> players) {
        if (players.stream().anyMatch(player -> arenaManager.isInMatch(player))) {
            lang.sendMessage(sender, "ERROR.duel.already-in-match.sender");
            return false;
        }
        
        return true;
    }
}
