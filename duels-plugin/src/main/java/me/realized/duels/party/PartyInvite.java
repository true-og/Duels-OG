package me.realized.duels.party;

import java.util.UUID;
import lombok.Getter;
import org.bukkit.entity.Player;

public class PartyInvite {

    @Getter
    private final UUID sender;
    @Getter
    private final UUID receiver;
    @Getter
    private final Party party;
    @Getter
    private final long creation;

    public PartyInvite(final Player sender, final Player receiver, final Party party) {
        this.sender = sender.getUniqueId();
        this.receiver = receiver.getUniqueId();
        this.party = party;
        this.creation = System.currentTimeMillis();
    }
}
