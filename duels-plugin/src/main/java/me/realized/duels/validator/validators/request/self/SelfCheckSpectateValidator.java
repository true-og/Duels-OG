package me.realized.duels.validator.validators.request.self;

import java.util.Collection;

import org.bukkit.entity.Player;

import me.realized.duels.DuelsPlugin;
import me.realized.duels.party.Party;
import me.realized.duels.validator.BaseTriValidator;

public class SelfCheckSpectateValidator extends BaseTriValidator<Player, Party, Collection<Player>> {
    
    public SelfCheckSpectateValidator(final DuelsPlugin plugin) {
        super(plugin);
    }

    @Override
    public boolean validate(final Player sender, final Party party, final Collection<Player> players) {
        if (players.stream().anyMatch(player -> spectateManager.isSpectating(player))) {
            lang.sendMessage(sender, "ERROR.spectate.already-spectating.sender");
            return false;
        }

        return true;
    }
}
