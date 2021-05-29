package me.realized.duels.validator.validators.request.self;

import me.realized.duels.DuelsPlugin;
import me.realized.duels.validator.BaseValidator;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;

public class SelfCheckCreativeValidator extends BaseValidator<Player> {

    public SelfCheckCreativeValidator(final DuelsPlugin plugin) {
        super(plugin);
    }

    @Override
    public boolean shouldValidate() {
        return config.isPreventCreativeMode();
    }

    @Override
    public boolean validate(final Player player) {
        if (player.getGameMode() == GameMode.CREATIVE) {
            lang.sendMessage(player, "ERROR.duel.in-creative-mode");
            return false;
        }

        return true;
    }
}
