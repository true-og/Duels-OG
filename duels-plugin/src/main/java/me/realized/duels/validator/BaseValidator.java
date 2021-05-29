package me.realized.duels.validator;

import me.realized.duels.DuelsPlugin;
import me.realized.duels.arena.ArenaManagerImpl;
import me.realized.duels.config.Config;
import me.realized.duels.config.Lang;
import me.realized.duels.party.PartyManager;
import me.realized.duels.spectate.SpectateManagerImpl;
import me.realized.duels.util.validate.Validator;

public abstract class BaseValidator<T> implements Validator<T> {

    protected final DuelsPlugin plugin;

    protected final Config config;
    protected final Lang lang;
    protected final ArenaManagerImpl arenaManager;
    protected final SpectateManagerImpl spectateManager;
    protected final PartyManager partyManager;

    public BaseValidator(final DuelsPlugin plugin) {
        this.plugin = plugin;
        this.config = plugin.getConfiguration();
        this.lang = plugin.getLang();
        this.arenaManager = plugin.getArenaManager();
        this.spectateManager = plugin.getSpectateManager();
        this.partyManager = plugin.getPartyManager();
    }

    @Override
    public boolean shouldValidate() {
        return true;
    }
}
