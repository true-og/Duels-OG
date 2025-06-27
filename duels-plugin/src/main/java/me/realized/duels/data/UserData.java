package me.realized.duels.data;

import com.google.common.base.Charsets;
import com.google.common.collect.Lists;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import me.realized.duels.api.kit.Kit;
import me.realized.duels.api.user.MatchInfo;
import me.realized.duels.api.user.User;
import me.realized.duels.util.Log;
import me.realized.duels.util.json.JsonUtil;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class UserData implements User {

    private static final transient String ERROR_USER_SAVE = "An error occurred while saving userdata of %s!";

    private UUID uuid;
    private String name;
    private volatile int wins;
    private volatile int losses;
    private boolean requests = true;

    private ConcurrentHashMap<String, Integer> rating;
    private List<MatchData> matches = new ArrayList<>();

    transient File folder;
    transient int defaultRating;
    transient int matchesToDisplay;

    public UserData(final File folder, final int defaultRating, final int matchesToDisplay, final Player player) {
        this.folder = folder;
        this.defaultRating = defaultRating;
        this.matchesToDisplay = matchesToDisplay;
        this.uuid = player.getUniqueId();
        this.name = player.getName();
    }

    public UUID getUuid() {
        return uuid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getWins() {
        return wins;
    }

    public void setWins(final int wins) {
        this.wins = wins;

        if (!isOnline()) {
            trySave();
        }
    }

    public int getLosses() {
        return losses;
    }

    public void setLosses(final int losses) {
        this.losses = losses;

        if (!isOnline()) {
            trySave();
        }
    }

    @Override
    public boolean canRequest() {
        return requests;
    }

    @Override
    public void setRequests(final boolean requests) {
        this.requests = requests;

        if (!isOnline()) {
            trySave();
        }
    }

    @NotNull
    @Override
    public List<MatchInfo> getMatches() {
        return Collections.unmodifiableList(matches);
    }

    @Override
    public int getRating() {
        return getRatingUnsafe(null);
    }

    @Override
    public int getRating(@NotNull final Kit kit) {
        return getRatingUnsafe(kit);
    }

    @Override
    public void resetRating() {
        setRating(null, defaultRating);
    }

    @Override
    public void resetRating(@NotNull final Kit kit) {
        setRating(kit, defaultRating);
    }

    @Override
    public void reset() {
        wins = 0;
        losses = 0;
        matches.clear();
        rating.clear();

        if (!isOnline()) {
            trySave();
        }
    }

    private int getRatingUnsafe(final Kit kit) {
        return this.rating != null
                ? this.rating.getOrDefault(kit == null ? "-" : kit.getName(), defaultRating)
                : defaultRating;
    }

    public void setRating(final Kit kit, final int rating) {
        if (this.rating == null) {
            this.rating = new ConcurrentHashMap<>();
        }

        this.rating.put(kit == null ? "-" : kit.getName(), rating);

        if (!isOnline()) {
            trySave();
        }
    }

    private boolean isOnline() {
        return Bukkit.getPlayer(uuid) != null;
    }

    public void addWin() {
        final int wins = this.wins;
        this.wins = wins + 1;
    }

    public void addLoss() {
        final int losses = this.losses;
        this.losses = losses + 1;
    }

    public void addMatch(final MatchData matchData) {
        if (!matches.isEmpty() && matches.size() >= matchesToDisplay) {
            matches.remove(0);
        }

        matches.add(matchData);
    }

    void refreshMatches() {
        if (matches.size() < matchesToDisplay) {
            return;
        }

        final List<MatchData> division =
                Lists.newArrayList(matches.subList(matches.size() - matchesToDisplay, matches.size()));
        matches.clear();
        matches.addAll(division);
    }

    public void trySave() {
        final File file = new File(folder, uuid + ".json");

        try {
            if (!file.exists()) {
                file.createNewFile();
            }

            try (final Writer writer = new OutputStreamWriter(new FileOutputStream(file), Charsets.UTF_8)) {
                JsonUtil.getObjectWriter().writeValue(writer, this);
                writer.flush();
            }
        } catch (IOException ex) {
            Log.error(String.format(ERROR_USER_SAVE, name), ex);
        }
    }

    @Override
    public String toString() {
        return "UserData{" + "uuid="
                + uuid + ", name='"
                + name + '\'' + ", wins="
                + wins + ", losses="
                + losses + ", requests="
                + requests + ", matches="
                + matches + ", rating="
                + rating + '}';
    }
}
