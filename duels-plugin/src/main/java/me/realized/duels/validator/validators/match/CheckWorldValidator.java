package me.realized.duels.validator.validators.match;

import java.util.Collection;
import me.realized.duels.DuelsPlugin;
import me.realized.duels.setting.Settings;
import me.realized.duels.validator.BaseBiValidator;
import org.bukkit.entity.Player;

public class CheckWorldValidator extends BaseBiValidator<Collection<Player>, Settings> {

    public CheckWorldValidator(final DuelsPlugin plugin) {
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
    public boolean validate(final Collection<Player> players, final Settings settings) {
        if (players.stream().anyMatch(this::isBlacklistedWorld)) {
            lang.sendMessage(players, "DUEL.start-failure.in-blacklisted-world");
            return false;
        }

        return true;
    }
}
