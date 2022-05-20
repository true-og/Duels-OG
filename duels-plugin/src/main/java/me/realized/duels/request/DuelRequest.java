package me.realized.duels.request;

import java.util.UUID;
import lombok.Getter;
import me.realized.duels.api.arena.Arena;
import me.realized.duels.api.kit.Kit;
import me.realized.duels.api.request.Request;
import me.realized.duels.party.Party;
import me.realized.duels.setting.Settings;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

public class DuelRequest implements Request {

    @Getter
    private final long creation;
    @Getter
    private final UUID sender;
    @Getter
    private final UUID target;
    @Getter
    private final Party senderParty;
    @Getter
    private final Party targetParty;
    @Getter
    private final Settings settings;

    public DuelRequest(final Player sender, final Player target, final Party senderParty, final Party targeParty, final Settings setting) {
        this.creation = System.currentTimeMillis();
        this.sender = sender.getUniqueId();
        this.target = target.getUniqueId();
        this.senderParty = senderParty;
        this.targetParty = targeParty;
        this.settings = setting.lightCopy();
    }

    @Nullable
    @Override
    public Kit getKit() {
        return settings.getKit();
    }

    @Nullable
    @Override
    public Arena getArena() {
        return settings.getArena();
    }

    @Override
    public boolean canBetItems() {
        return settings.isItemBetting();
    }

    @Override
    public int getBet() {
        return settings.getBet();
    }
}
