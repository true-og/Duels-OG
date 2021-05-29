package me.realized.duels.match;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.Getter;
import lombok.Setter;
import me.realized.duels.DuelsPlugin;
import me.realized.duels.api.match.Match;
import me.realized.duels.arena.ArenaImpl;
import me.realized.duels.config.Config;
import me.realized.duels.config.Lang;
import me.realized.duels.kit.KitImpl;
import me.realized.duels.party.PartyManager;
import me.realized.duels.queue.Queue;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public abstract class AbstractMatch implements Match {

    protected final DuelsPlugin plugin;
    protected final Config config;
    protected final Lang lang;
    protected final PartyManager partyManager;

    @Getter
    private final ArenaImpl arena;
    @Getter
    private final KitImpl kit;
    @Getter
    private final int bet;
    @Getter
    private final Queue source;

    @Getter
    @Setter
    private MatchState state = MatchState.PRE_GAME;

    private final long start;
    private final Map<Player, MatchPlayer> players = new HashMap<>();

    public AbstractMatch(final DuelsPlugin plugin, final ArenaImpl arena, final KitImpl kit, final int bet, final Queue source) {
        this.plugin = plugin;
        this.config = plugin.getConfiguration();
        this.lang = plugin.getLang();
        this.partyManager = plugin.getPartyManager();
        this.arena = arena;
        this.kit = kit;
        this.bet = bet;
        this.source = source;
        this.start = System.currentTimeMillis();
    }

    public int size() {
        return getAlivePlayers().size();
    }

    public MatchPlayer getPlayer(final Player player) {
        return players.get(player);
    }

    public MatchPlayer addPlayer(final Player player) {
        final MatchPlayer matchPlayer = new MatchPlayer(player);
        players.put(player, matchPlayer);
        return matchPlayer;
    }

    public void removePlayer(final Player player) {
        final MatchPlayer matchPlayer = getPlayer(player);

        if (matchPlayer == null) {
            return;
        }

        matchPlayer.setDead();
    }

    public boolean hasAlivePlayer(final Player player) {
        final MatchPlayer matchPlayer = getPlayer(player);
        return matchPlayer != null && matchPlayer.isAlive();
    }

    public Set<Player> getAlivePlayers() {
        return players.values().stream().filter(MatchPlayer::isAlive).map(MatchPlayer::getPlayer).collect(Collectors.toSet());
    }

    public Set<Player> getAllPlayers() {
        return players.keySet();
    }

    public boolean isFromQueue() {
        return source != null;
    }

    public long getDuration() {
        return System.currentTimeMillis() - start;
    }

    @Override
    public long getStart() {
        return this.start;
    }

    @Override
    public boolean isFinished() {
        return getState() == MatchState.FINISHED;
    }

    @NotNull
    @Override
    public Set<Player> getPlayers() {
        return Collections.unmodifiableSet(getAlivePlayers());
    }

    @NotNull
    @Override
    public Set<Player> getStartingPlayers() {
        return Collections.unmodifiableSet(getAllPlayers());
    }
}
