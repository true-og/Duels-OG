package me.realized.duels.validator.validators.request.target;

import me.realized.duels.DuelsPlugin;
import me.realized.duels.validator.BaseBiValidator;
import org.bukkit.entity.Player;

public class TargetCheckMatchValidator extends BaseBiValidator<Player, Player> {

    public TargetCheckMatchValidator(final DuelsPlugin plugin) {
        super(plugin);
    }

    @Override
    public boolean validate(final Player player, final Player target) {
        if (arenaManager.isInMatch(target)) {
            lang.sendMessage(player, "ERROR.duel.already-in-match.target", "name", target.getName());
            return false;
        }

        return true;
    }
}
