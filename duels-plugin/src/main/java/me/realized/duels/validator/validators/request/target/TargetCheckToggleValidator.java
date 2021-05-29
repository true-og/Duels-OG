package me.realized.duels.validator.validators.request.target;

import me.realized.duels.DuelsPlugin;
import me.realized.duels.Permissions;
import me.realized.duels.data.UserData;
import me.realized.duels.validator.BaseBiValidator;
import org.bukkit.entity.Player;

public class TargetCheckToggleValidator extends BaseBiValidator<Player, Player> {

    public TargetCheckToggleValidator(final DuelsPlugin plugin) {
        super(plugin);
    }

    @Override
    public boolean validate(final Player player, final Player target) {
        final UserData user = userManager.get(target);

        if (user == null) {
            lang.sendMessage(player, "ERROR.data.not-found", "name", target.getName());
            return false;
        }

        if (!player.hasPermission(Permissions.ADMIN) && !user.canRequest()) {
            lang.sendMessage(player, "ERROR.duel.requests-disabled", "name", target.getName());
            return false;
        }

        return true;
    }
}
