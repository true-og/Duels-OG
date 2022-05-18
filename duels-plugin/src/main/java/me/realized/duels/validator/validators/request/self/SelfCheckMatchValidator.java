package me.realized.duels.validator.validators.request.self;

import java.util.Collection;

import org.bukkit.entity.Player;

import me.realized.duels.DuelsPlugin;
import me.realized.duels.validator.BaseBiValidator;

public class SelfCheckMatchValidator extends BaseBiValidator<Player, Collection<Player>> {
    
    public SelfCheckMatchValidator(final DuelsPlugin plugin) {
        super(plugin);
    }

    @Override
    public boolean validate(final Player sender, final Collection<Player> players) {
        if (players.stream().anyMatch(player -> arenaManager.isInMatch(player))) {
            lang.sendMessage(sender, "ERROR.duel.already-in-match.sender");
            return false;
        }
        
        return true;
    }
}
