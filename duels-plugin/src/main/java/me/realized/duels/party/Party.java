package me.realized.duels.party;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

import org.bukkit.entity.Player;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

public class Party {
    
    @Getter
    private final long creation;

    @Getter
    @Setter(value = AccessLevel.PACKAGE)
    private boolean removed;

    @Getter
    private PartyMember owner;
    
    private final Map<UUID, PartyMember> members = new HashMap<>();

    public Party(final Player owner) {
        this.creation = System.currentTimeMillis();
        add(owner);
        this.owner = get(owner);
    }

    public PartyMember get(final Player player) {
        return members.get(player.getUniqueId());
    }

    public boolean isMember(final Player player) {
        return get(player) != null;
    }

    public boolean add(final Player player) {
        PartyMember member = get(player);

        if (member != null) {
            return false;
        }

        members.put(player.getUniqueId(), new PartyMember(player, this));
        return true;
    }

    public boolean isOwner(final Player player) {
        return owner != null && owner.equals(get(player));
    }

    public int size() {
        return members.size();
    }

    public List<Player> getOnlineMembers() {
        return members.values().stream().map(PartyMember::getPlayer).filter(Objects::nonNull).collect(Collectors.toList());
    }

    public List<String> getMemberNames() {
        return members.values().stream().map(PartyMember::getName).collect(Collectors.toList());
    }
}
