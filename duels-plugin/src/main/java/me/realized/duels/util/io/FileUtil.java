package me.realized.duels.util.io;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import org.bukkit.plugin.java.JavaPlugin;

public final class FileUtil {

    public static boolean checkNonEmpty(final File file, final boolean create) throws IOException {
        if (!file.exists()) {
            if (create) {
                file.createNewFile();
            }
            return false;
        }

        return file.length() > 0;
    }

    public static boolean checkNonEmpty(final JavaPlugin plugin, final File file, final String resourceName) throws IOException {
        if (!file.exists()) {
            try (final InputStream in = plugin.getResource(resourceName)) {
                if (in != null) {
                    plugin.saveResource(resourceName, false);
                    return file.exists() && file.length() > 0;
                }
            }

            file.createNewFile();
            return false;
        }

        return file.length() > 0;
    }

    private FileUtil() {}
}
