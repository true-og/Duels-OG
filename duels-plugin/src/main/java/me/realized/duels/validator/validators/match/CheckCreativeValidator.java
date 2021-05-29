package me.realized.duels.validator.validators.match;

import java.util.Collection;
import me.realized.duels.DuelsPlugin;
import me.realized.duels.setting.Settings;
import me.realized.duels.validator.BaseBiValidator;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;

public class CheckCreativeValidator extends BaseBiValidator<Collection<Player>, Settings> {

    public CheckCreativeValidator(final DuelsPlugin plugin) {
        super(plugin);
    }

    @Override
    public boolean shouldValidate() {
        return config.isPreventCreativeMode();
    }

    @Override
    public boolean validate(final Collection<Player> players, final Settings settings) {
        if (players.stream().anyMatch(player -> player.getGameMode() == GameMode.CREATIVE)) {
            lang.sendMessage(players, "DUEL.start-failure.in-creative-mode");
            return false;
        }

        return true;
    }
}
