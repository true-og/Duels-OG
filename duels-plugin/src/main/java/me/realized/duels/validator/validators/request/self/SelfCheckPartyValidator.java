package me.realized.duels.validator.validators.request.self;

import me.realized.duels.DuelsPlugin;
import me.realized.duels.validator.BaseValidator;
import org.bukkit.entity.Player;

public class SelfCheckPartyValidator extends BaseValidator<Player> {

    public SelfCheckPartyValidator(final DuelsPlugin plugin) {
        super(plugin);
    }

    @Override
    public boolean validate(final Player player) {
        if (partyManager.isInParty(player)) {
            lang.sendMessage(player, "ERROR.party.already-in-party.sender");
            return false;
        }

        return true;
    }
}
