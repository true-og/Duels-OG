package me.realized.duels.request.party;

import org.bukkit.entity.Player;

import lombok.Getter;
import me.realized.duels.party.Party;
import me.realized.duels.request.DuelRequest;
import me.realized.duels.setting.Settings;

public class PartyDuelRequest extends DuelRequest {

    @Getter
    private final Party senderParty;
    @Getter
    private final Party targetParty;

    PartyDuelRequest(Player sender, Player target, Settings setting, final Party senderParty, final Party targeParty) {
        super(sender, target, setting);
        this.senderParty = senderParty;
        this.targetParty = targeParty;
    }
}
