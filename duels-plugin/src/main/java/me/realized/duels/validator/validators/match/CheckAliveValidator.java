package me.realized.duels.validator.validators.match;

import java.util.Collection;
import me.realized.duels.DuelsPlugin;
import me.realized.duels.setting.Settings;
import me.realized.duels.validator.BaseBiValidator;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

public class CheckAliveValidator extends BaseBiValidator<Collection<Player>, Settings> {

    public CheckAliveValidator(final DuelsPlugin plugin) {
        super(plugin);
    }

    @Override
    public boolean validate(final Collection<Player> players, final Settings settings) {
        if (players.stream().anyMatch(Entity::isDead)) {
            lang.sendMessage(players, "DUEL.start-failure.player-is-dead");
            return false;
        }

        return true;
    }
}
