package me.realized.duels.util.compat;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.mojang.authlib.GameProfile;
import java.lang.reflect.InvocationTargetException;
import java.util.concurrent.TimeUnit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.meta.SkullMeta;
import org.jetbrains.annotations.NotNull;

public final class Skulls extends CompatBase {

    private static final LoadingCache<Player, GameProfile> cache = CacheBuilder.newBuilder()
        .maximumSize(1000)
        .weakKeys()
        .expireAfterAccess(1, TimeUnit.HOURS)
        .build(
            new CacheLoader<Player, GameProfile>() {

                public GameProfile load(@NotNull final Player player) {
                    return getProfile(player);
                }
            }
        );

    /**
     * Returns the GameProfile instance of the player that contains the skull texture.
     *
     * @param player Player to get GameProfile
     * @return GameProfile instance containing the skull texture
     */
    private static GameProfile getProfile(final Player player) {
        try {
            final Object nmsPlayer = GET_HANDLE.invoke(player);
            return (GameProfile) GET_PROFILE.invoke(nmsPlayer);
        } catch (IllegalAccessException | InvocationTargetException ex) {
            ex.printStackTrace();
            return new GameProfile(player.getUniqueId(), player.getName());
        }
    }

    /**
     * Sets the owner of the given skull to given player.
     *
     * @param meta SkullMeta of the skull to set profile
     * @param player Player to display on skull
     */
    public static void setProfile(final SkullMeta meta, final Player player) {
        final GameProfile cached = cache.getUnchecked(player);

        try {
            PROFILE.set(meta, cached);
        } catch (IllegalAccessException ex) {
            ex.printStackTrace();
        }
    }

    private Skulls() {}
}
