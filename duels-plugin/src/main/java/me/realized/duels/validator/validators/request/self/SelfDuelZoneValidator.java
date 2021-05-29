package me.realized.duels.validator.validators.request.self;

import me.realized.duels.DuelsPlugin;
import me.realized.duels.hook.hooks.worldguard.WorldGuardHook;
import me.realized.duels.validator.BaseValidator;
import org.bukkit.entity.Player;

public class SelfDuelZoneValidator extends BaseValidator<Player> {

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
    public boolean validate(final Player player) {
        if (worldGuard.findDuelZone(player) == null) {
            lang.sendMessage(player, "ERROR.duel.not-in-duelzone", "regions", config.getDuelzones());
            return false;
        }

        return true;
    }
}
