package me.realized.duels.validator.validators.request.target;

import me.realized.duels.DuelsPlugin;
import me.realized.duels.validator.BaseBiValidator;
import org.bukkit.entity.Player;

public class TargetCheckSpectateValidator extends BaseBiValidator<Player, Player> {

    public TargetCheckSpectateValidator(final DuelsPlugin plugin) {
        super(plugin);
    }

    @Override
    public boolean validate(final Player player, final Player target) {
        if (spectateManager.isSpectating(target)) {
            lang.sendMessage(player, "ERROR.spectate.already-spectating.target", "name", target.getName());
            return false;
        }

        return true;
    }
}
