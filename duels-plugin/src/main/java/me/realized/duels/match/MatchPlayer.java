package me.realized.duels.match;

import lombok.Getter;
import org.bukkit.entity.Player;

public class MatchPlayer {

    @Getter
    private final Player player;
    @Getter
    private boolean alive = true;

    public MatchPlayer(final Player player) {
        this.player = player;
    }

    public void setDead() {
        this.alive = false;
    }
}
