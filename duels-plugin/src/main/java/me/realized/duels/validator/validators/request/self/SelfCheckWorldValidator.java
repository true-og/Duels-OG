package me.realized.duels.validator.validators.request.self;

import me.realized.duels.DuelsPlugin;
import me.realized.duels.validator.BaseValidator;
import org.bukkit.entity.Player;

public class SelfCheckWorldValidator extends BaseValidator<Player> {

    public SelfCheckWorldValidator(final DuelsPlugin plugin) {
        super(plugin);
    }

    @Override
    public boolean shouldValidate() {
        return !config.getBlacklistedWorlds().isEmpty();
    }

    private boolean isBlacklistedWorld(final Player player) {
        return config.getBlacklistedWorlds().contains(player.getWorld().getName());
    }

    @Override
    public boolean validate(final Player player) {
        if (isBlacklistedWorld(player)) {
            lang.sendMessage(player, "ERROR.duel.in-blacklisted-world");
            return false;
        }

        return true;
    }
}
