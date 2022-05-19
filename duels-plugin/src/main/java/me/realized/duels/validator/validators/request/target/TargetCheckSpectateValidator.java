package me.realized.duels.validator.validators.request.target;

import java.util.Collection;

import org.bukkit.entity.Player;

import me.realized.duels.DuelsPlugin;
import me.realized.duels.party.Party;
import me.realized.duels.util.function.Pair;
import me.realized.duels.validator.BaseTriValidator;

public class TargetCheckSpectateValidator extends BaseTriValidator<Pair<Player, Player>, Party, Collection<Player>> {
    
    public TargetCheckSpectateValidator(final DuelsPlugin plugin) {
        super(plugin);
    }

    @Override
    public boolean validate(final Pair<Player, Player> pair, final Party party, final Collection<Player> players) {
        if (players.stream().anyMatch(player -> spectateManager.isSpectating(player))) {
            lang.sendMessage(pair.getKey(), "ERROR.spectate.already-spectating.target");
            return false;
        }

        return true;
    }
}
