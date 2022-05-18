package me.realized.duels.validator.validators.request.self;

import java.util.Collection;

import org.bukkit.entity.Player;

import me.realized.duels.DuelsPlugin;
import me.realized.duels.util.inventory.InventoryUtil;
import me.realized.duels.validator.BaseBiValidator;

public class SelfEmptyInventoryValidator extends BaseBiValidator<Player, Collection<Player>> {

    public SelfEmptyInventoryValidator(final DuelsPlugin plugin) {
        super(plugin);
    }

    @Override
    public boolean shouldValidate() {
        return config.isRequiresClearedInventory();
    }

    @Override
    public boolean validate(final Player sender, final Collection<Player> players) {
        if (players.stream().anyMatch(InventoryUtil::hasItem)) {
            lang.sendMessage(sender, "ERROR.duel.inventory-not-empty");
            return false;
        }

        return true;
    }
    

}
