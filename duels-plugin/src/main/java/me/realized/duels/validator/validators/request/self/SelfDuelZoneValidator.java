package me.realized.duels.validator.validators.request.self;

import java.util.Collection;

import org.bukkit.entity.Player;

import me.realized.duels.DuelsPlugin;
import me.realized.duels.hook.hooks.worldguard.WorldGuardHook;
import me.realized.duels.validator.BaseBiValidator;

public class SelfDuelZoneValidator extends BaseBiValidator<Player, Collection<Player>> {
    
    private final WorldGuardHook worldGuard;

    public SelfDuelZoneValidator(final DuelsPlugin plugin) {
        super(plugin);
        this.worldGuard = plugin.getHookManager().getHook(WorldGuardHook.class);
    }

    @Override
    public boolean shouldValidate() {
        return config.isDuelzoneEnabled() && worldGuard != null;
    }

    @Override
    public boolean validate(final Player sender, final Collection<Player> players) {
        if (players.stream().anyMatch(player -> worldGuard.findDuelZone(player) == null)) {
            lang.sendMessage(sender, "ERROR.duel.not-in-duelzone", "regions", config.getDuelzones());
        }

        return true;
    }
}
