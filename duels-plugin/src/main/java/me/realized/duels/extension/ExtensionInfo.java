package me.realized.duels.extension;

import java.io.File;
import java.io.InputStreamReader;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

public class ExtensionInfo {

    private final String name;
    private final String version;
    private final String main;
    private final String apiVersion;
    private final String description;
    private final String website;
    private final List<String> depends;
    private final List<String> authors;

    public ExtensionInfo(final File file) throws Exception {
        try (JarFile jar = new JarFile(file)) {
            final JarEntry entry = jar.getJarEntry("extension.yml");

            if (entry == null) {
                throw new RuntimeException("No extension.yml found");
            }

            try (InputStreamReader reader = new InputStreamReader(jar.getInputStream(entry))) {
                final FileConfiguration config = YamlConfiguration.loadConfiguration(reader);
                this.name = getOrThrow(config, "name");
                this.version = getOrThrow(config, "version");
                this.main = getOrThrow(config, "main");
                this.apiVersion = config.getString("api-version");
                this.description = config.getString("description", "not specified");
                this.website = config.getString("website", "not specified");
                this.depends = config.getStringList("depends");
                this.authors = config.getStringList("authors");
            }
        }
    }

    private String getOrThrow(final FileConfiguration config, final String path) {
        final String result = config.getString(path);

        if (result == null) {
            throw new RuntimeException("No " + path + " specified in extension.yml");
        }

        return result;
    }

    public String getName() {
        return name;
    }

    public String getVersion() {
        return version;
    }

    public String getMain() {
        return main;
    }

    public String getApiVersion() {
        return apiVersion;
    }

    public String getDescription() {
        return description;
    }

    public String getWebsite() {
        return website;
    }

    public List<String> getDepends() {
        return depends;
    }

    public List<String> getAuthors() {
        return authors;
    }
}
