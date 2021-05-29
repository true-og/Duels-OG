package me.realized.duels.party;

import java.util.Objects;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class PartyMember {

    @Getter
    private final UUID uuid;
    @Getter
    private final Party party;

    @Getter
    @Setter
    private String name;
    @Getter
    @Setter(value = AccessLevel.PACKAGE)
    private boolean owner;

    public PartyMember(final Player player, final Party party, final boolean owner) {
        this.uuid = player.getUniqueId();
        this.party = party;
        this.name = player.getName();
        this.owner = owner;
    }

    public Player getPlayer() {
        return Bukkit.getPlayer(uuid);
    }

    @Override
    public boolean equals(final Object other) {
        if (this == other) {
            return true;
        }

        if (other == null || getClass() != other.getClass()) {
            return false;
        }

        return uuid.equals(((PartyMember) other).uuid);
    }

    @Override
    public int hashCode() {
        return Objects.hash(uuid);
    }
}
