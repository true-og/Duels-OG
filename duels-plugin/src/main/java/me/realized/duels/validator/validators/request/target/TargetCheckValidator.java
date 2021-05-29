package me.realized.duels.validator.validators.request.target;

import me.realized.duels.DuelsPlugin;
import me.realized.duels.validator.BaseBiValidator;
import org.bukkit.entity.Player;

public class TargetCheckValidator extends BaseBiValidator<Player, Player> {

    public TargetCheckValidator(final DuelsPlugin plugin) {
        super(plugin);
    }

    @Override
    public boolean validate(final Player player, final Player target) {
        if (target == null || !player.canSee(target)) {
            lang.sendMessage(player, "ERROR.player.not-found");
            return false;
        }

        return true;
    }
}
