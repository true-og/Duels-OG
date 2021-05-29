package me.realized.duels.validator.validators.match.party;

import me.realized.duels.DuelsPlugin;
import me.realized.duels.party.Party;
import me.realized.duels.validator.BaseBiValidator;

public class PartySizeValidator extends BaseBiValidator<Party, Party> {

    public PartySizeValidator(final DuelsPlugin plugin) {
        super(plugin);
    }

    @Override
    public boolean validate(final Party first, final Party second) {
        return false;
    }
}
