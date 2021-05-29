package me.realized.duels.validator.validators.request.target;

import me.realized.duels.DuelsPlugin;
import me.realized.duels.validator.BaseBiValidator;
import org.bukkit.entity.Player;

public class TargetCheckRequestValidator extends BaseBiValidator<Player, Player> {

    public TargetCheckRequestValidator(final DuelsPlugin plugin) {
        super(plugin);
    }

    @Override
    public boolean validate(final Player player, final Player target) {
        if (requestManager.has(target, player)) {
            lang.sendMessage(player, "ERROR.duel.no-request", "name", target.getName());
            return false;
        }

        return true;
    }
}
