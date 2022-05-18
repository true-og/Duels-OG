package me.realized.duels.validator.validators.request.self;

import java.util.Collection;

import org.bukkit.entity.Player;

import me.realized.duels.DuelsPlugin;
import me.realized.duels.validator.BaseBiValidator;

public class SelfCheckSpectateValidator extends BaseBiValidator<Player, Collection<Player>> {
    
    public SelfCheckSpectateValidator(final DuelsPlugin plugin) {
        super(plugin);
    }

    @Override
    public boolean validate(final Player sender, final Collection<Player> players) {
        if (players.stream().anyMatch(player -> spectateManager.isSpectating(player))) {
            lang.sendMessage(sender, "ERROR.spectate.already-spectating.sender");
            return false;
        }

        return true;
    }
}
