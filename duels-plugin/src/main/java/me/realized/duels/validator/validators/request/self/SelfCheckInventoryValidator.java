package me.realized.duels.validator.validators.request.self;

import me.realized.duels.DuelsPlugin;
import me.realized.duels.util.inventory.InventoryUtil;
import me.realized.duels.validator.BaseValidator;
import org.bukkit.entity.Player;

public class SelfCheckInventoryValidator extends BaseValidator<Player> {

    public SelfCheckInventoryValidator(final DuelsPlugin plugin) {
        super(plugin);
    }

    @Override
    public boolean shouldValidate() {
        return config.isRequiresClearedInventory();
    }

    @Override
    public boolean validate(final Player player) {
        if (InventoryUtil.hasItem(player)) {
            lang.sendMessage(player, "ERROR.duel.inventory-not-empty");
            return false;
        }

        return true;
    }
}
