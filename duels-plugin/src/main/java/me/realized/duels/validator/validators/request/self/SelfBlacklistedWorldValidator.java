package me.realized.duels.validator.validators.request.self;

import java.util.Collection;

import org.bukkit.entity.Player;

import me.realized.duels.DuelsPlugin;
import me.realized.duels.validator.BaseBiValidator;

public class SelfBlacklistedWorldValidator extends BaseBiValidator<Player, Collection<Player>> {
    
    public SelfBlacklistedWorldValidator(final DuelsPlugin plugin) {
        super(plugin);
    }

    @Override
    public boolean shouldValidate() {
        return !config.getBlacklistedWorlds().isEmpty();
    }

    private boolean isBlacklistedWorld(final Player player) {
        return config.getBlacklistedWorlds().contains(player.getWorld().getName());
    }

    @Override
    public boolean validate(final Player sender, final Collection<Player> players) {
        if (players.stream().anyMatch(this::isBlacklistedWorld)) {
            lang.sendMessage(sender, "ERROR.duel.in-blacklisted-world");
            return false;
        }

        return true;
    }
}
