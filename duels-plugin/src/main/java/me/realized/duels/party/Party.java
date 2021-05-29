package me.realized.duels.party;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.entity.Player;

public class Party {

    @Getter
    private final long creation;

    @Getter
    @Setter(value = AccessLevel.PACKAGE)
    private boolean removed;

    private final Set<PartyMember> members = new HashSet<>();

    public Party(final Player owner) {
        this.creation = System.currentTimeMillis();
        add(owner, true);
    }

    public PartyMember getOwner() {
        return members.stream().filter(PartyMember::isOwner).findFirst().orElse(null);
    }

    public boolean isOwner(final Player player) {
        final PartyMember member = get(player);
        return member != null && member.isOwner();
    }

    public void transferOwnership(final PartyMember owner, final PartyMember newOwner) {
        if (!owner.isOwner()) {
            return;
        }

        owner.setOwner(false);
        newOwner.setOwner(true);
    }

    public PartyMember get(final Player player) {
        return members.stream().filter(member -> member.getUuid().equals(player.getUniqueId())).findFirst().orElse(null);
    }

    public boolean isMember(final Player player) {
        return get(player) != null;
    }

    private void add(final Player player, final boolean owner) {
        members.add(new PartyMember(player, this, owner));
    }

    public void add(final Player player) {
        add(player, false);
    }

    public int size() {
        return members.size();
    }

    public boolean isAllOnline() {
        return getOnlineMembers().size() == size();
    }

    public List<Player> getOnlineMembers() {
        return members.stream().map(PartyMember::getPlayer).filter(Objects::nonNull).collect(Collectors.toList());
    }

    public List<String> getMemberNames() {
        return members.stream().map(PartyMember::getName).collect(Collectors.toList());
    }
}
