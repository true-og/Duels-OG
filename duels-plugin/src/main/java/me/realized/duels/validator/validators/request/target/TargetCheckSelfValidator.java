package me.realized.duels.validator.validators.request.target;

import java.util.Collection;

import org.bukkit.entity.Player;

import me.realized.duels.DuelsPlugin;
import me.realized.duels.validator.BaseTriValidator;

public class TargetCheckSelfValidator extends BaseTriValidator<Player, Player, Collection<Player>> {

    public TargetCheckSelfValidator(final DuelsPlugin plugin) {
        super(plugin);
    }

    @Override
    public boolean validate(final Player sender, final Player target, final Collection<Player> players) {
        if (sender.equals(target)) {
            lang.sendMessage(sender, "ERROR.duel.is-self");
            return false;
        }

        return true;
    }

    
}