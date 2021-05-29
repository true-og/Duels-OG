package me.realized.duels.validator.validators.request.self;

import me.realized.duels.DuelsPlugin;
import me.realized.duels.validator.BaseValidator;
import org.bukkit.entity.Player;

public class SelfCheckMatchValidator extends BaseValidator<Player> {

    public SelfCheckMatchValidator(final DuelsPlugin plugin) {
        super(plugin);
    }

    @Override
    public boolean validate(final Player player) {
        if (arenaManager.isInMatch(player)) {
            lang.sendMessage(player, "ERROR.duel.already-in-match.sender");
            return false;
        }

        return true;
    }
}
