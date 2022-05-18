package me.realized.duels.validator.validators.request.target;

import java.util.Collection;

import org.bukkit.entity.Player;

import me.realized.duels.DuelsPlugin;
import me.realized.duels.validator.BaseTriValidator;

public class TargetPartyValidator extends BaseTriValidator<Player, Player, Collection<Player>> {

    public TargetPartyValidator(final DuelsPlugin plugin) {
        super(plugin);
    }

    @Override
    public boolean validate(final Player sender, final Player target, final Collection<Player> players) {
        
        
        return true;
    }

}
