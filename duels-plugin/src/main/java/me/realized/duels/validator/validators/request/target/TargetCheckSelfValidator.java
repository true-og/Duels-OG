package me.realized.duels.validator.validators.request.target;

import me.realized.duels.DuelsPlugin;
import me.realized.duels.validator.BaseBiValidator;
import org.bukkit.entity.Player;

public class TargetCheckSelfValidator extends BaseBiValidator<Player, Player> {

    public TargetCheckSelfValidator(final DuelsPlugin plugin) {
        super(plugin);
    }

    @Override
    public boolean validate(final Player player, final Player target) {
        if (player.equals(target)) {
            lang.sendMessage(player, "ERROR.duel.is-self");
            return true;
        }

        return true;
    }
}
