package me.realized.duels.validator.validators.request.self;

import me.realized.duels.DuelsPlugin;
import me.realized.duels.validator.BaseValidator;
import org.bukkit.entity.Player;

public class SelfCheckSpectateValidator extends BaseValidator<Player> {

    public SelfCheckSpectateValidator(final DuelsPlugin plugin) {
        super(plugin);
    }

    @Override
    public boolean validate(final Player player) {
        if (spectateManager.isSpectating(player)) {
            lang.sendMessage(player, "ERROR.spectate.already-spectating.sender");
            return false;
        }

        return true;
    }
}
