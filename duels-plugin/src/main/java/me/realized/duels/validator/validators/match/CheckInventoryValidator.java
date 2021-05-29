package me.realized.duels.validator.validators.match;

import java.util.Collection;
import me.realized.duels.DuelsPlugin;
import me.realized.duels.setting.Settings;
import me.realized.duels.util.inventory.InventoryUtil;
import me.realized.duels.validator.BaseBiValidator;
import org.bukkit.entity.Player;

public class CheckInventoryValidator extends BaseBiValidator<Collection<Player>, Settings> {

    public CheckInventoryValidator(final DuelsPlugin plugin) {
        super(plugin);
    }

    @Override
    public boolean shouldValidate() {
        return config.isRequiresClearedInventory();
    }

    @Override
    public boolean validate(final Collection<Player> players, final Settings settings) {
        if (players.stream().anyMatch(InventoryUtil::hasItem)) {
            lang.sendMessage(players, "DUEL.start-failure.inventory-not-empty");
            return false;
        }

        return true;
    }
}
