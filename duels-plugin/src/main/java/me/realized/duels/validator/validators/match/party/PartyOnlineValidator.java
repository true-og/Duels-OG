package me.realized.duels.validator.validators.match.party;

import me.realized.duels.DuelsPlugin;
import me.realized.duels.party.Party;
import me.realized.duels.validator.BaseBiValidator;

public class PartyOnlineValidator extends BaseBiValidator<Party, Party> {

    public PartyOnlineValidator(final DuelsPlugin plugin) {
        super(plugin);
    }

    @Override
    public boolean validate(final Party first, final Party second) {
//        if (!party.isOwner(player)) {
//            lang.sendMessage(sender, "ERROR.party.is-not-owner");
//            return;
//        }
//
//        if (!party.isAllOnline()) {
//            lang.sendMessage(sender, "ERROR.party.is-not-online.sender");
//            return;
//        }

        return false;
    }
}
