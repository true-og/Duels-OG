package me.realized.duels.validator.validators.request.target;

import java.util.Collection;

import org.bukkit.entity.Player;

import me.realized.duels.DuelsPlugin;
import me.realized.duels.validator.BaseTriValidator;

public class TargetCheckMatchValidator extends BaseTriValidator<Player, Player, Collection<Player>> {
    
    public TargetCheckMatchValidator(final DuelsPlugin plugin) {
        super(plugin);
    }

    @Override
    public boolean validate(final Player sender, final Player target, final Collection<Player> players) {
        if (players.stream().anyMatch(player -> arenaManager.isInMatch(player))) {
            lang.sendMessage(sender, "ERROR.duel.already-in-match.target", "name", target.getName());
            return false;
        }
        
        return true;
    }
}
