package me.realized.duels.validator.validators.request.target;

import java.util.Collection;

import org.bukkit.entity.Player;

import me.realized.duels.DuelsPlugin;
import me.realized.duels.Permissions;
import me.realized.duels.data.UserData;
import me.realized.duels.validator.BaseTriValidator;

public class TargetCanRequestValidator extends BaseTriValidator<Player, Player, Collection<Player>> {
    
    public TargetCanRequestValidator(final DuelsPlugin plugin) {
        super(plugin);
    }

    @Override
    public boolean validate(final Player sender, final Player target, final Collection<Player> players) {
        final UserData user = userManager.get(target);

        if (user == null) {
            lang.sendMessage(sender, "ERROR.data.not-found", "name", target.getName());
            return false;
        }

        if (!sender.hasPermission(Permissions.ADMIN) && !user.canRequest()) {
            lang.sendMessage(sender, "ERROR.duel.requests-disabled", "name", target.getName());
            return false;
        }

        return true;
    }

}
