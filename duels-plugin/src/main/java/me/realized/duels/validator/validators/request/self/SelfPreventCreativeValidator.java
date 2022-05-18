package me.realized.duels.validator.validators.request.self;

import org.bukkit.entity.Player;

import java.util.Collection;

import org.bukkit.GameMode;

import me.realized.duels.validator.BaseBiValidator;
import me.realized.duels.DuelsPlugin;

public class SelfPreventCreativeValidator extends BaseBiValidator<Player, Collection<Player>> {
    
    public SelfPreventCreativeValidator(final DuelsPlugin plugin) {
        super(plugin);
    }

    @Override
    public boolean shouldValidate() {
        return config.isPreventCreativeMode();
    }

    @Override
    public boolean validate(final Player sender, final Collection<Player> players) {
        if (players.stream().anyMatch(player -> player.getGameMode() == GameMode.CREATIVE)) {
            lang.sendMessage(sender, "ERROR.duel.in-creative-mode");
            return false;
        }

        return true;
    }
}
