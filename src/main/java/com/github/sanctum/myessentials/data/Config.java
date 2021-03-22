package com.github.sanctum.myessentials.data;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

/**
 * Encapsulates config file operations.
 */
public class Config {
    protected final String n;
    protected final String d;
    protected final File file;
    protected FileConfiguration fc;
    private static final List<Config> configs = new ArrayList<>();

    public Config(@NotNull final String n, final String d) {
        this.n = n;
        this.d = d;
        // Get the data directory of the plugin that is providing this Config implementation
        final File pluginDataDir = JavaPlugin.getProvidingPlugin(getClass()).getDataFolder();
        // If d is null or empty, use plugin's data folder. If not get the file describing the subdirectory.
        final File parent = (d == null || d.isEmpty()) ? pluginDataDir : new File(pluginDataDir, d);
        if (!parent.exists()) {
            //noinspection ResultOfMethodCallIgnored
            parent.mkdir();
        }
        this.file = new File(parent, n.concat(".yml"));
        configs.add(this);
    }

    public static void copy(InputStream in, File file) {
        try {
            OutputStream out = new FileOutputStream(file);
            byte[] buf = new byte[1024];
            int len;
            while((len=in.read(buf))>0){
                out.write(buf,0,len);
            }
            out.close();
            in.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String getName() {
        return (n == null) ? "" : n;
    }

    public String getDescription() {
        return (d == null) ? "" : d;
    }

    public static Config get(final String n, final String d) throws IllegalArgumentException {
        for (final Config c : Config.configs) {
            if (c.getName().equals(n) && c.getDescription().equals(d)) {
                return c;
            }
        }
        if (n == null || n.isEmpty()) {
            throw new IllegalArgumentException("Name cannot be null/empty!");
        }
        return new Config(n, d);
    }

    public boolean delete() {
        return file.delete();
    }

    public boolean exists() {
        return file.exists();
    }

    public File getFile() {
        if(!file.exists()) {
            try {
                //noinspection ResultOfMethodCallIgnored
                file.createNewFile();
            } catch(final IOException e) {
                throw new IllegalStateException("Unable to create file! See log:", e);
            }
        }
        return file;
    }

    public FileConfiguration getConfig() {
        if(this.fc == null) {
            // fast exit with new blank configuration in the case of nonexistent file
            if (!file.exists()) return new YamlConfiguration();
            // load configuration from file
            this.fc = YamlConfiguration.loadConfiguration(file);
        }
        return this.fc;
    }

    public void reload() {
        if (!this.file.exists()) {
            this.fc = new YamlConfiguration();
        }
        this.fc = YamlConfiguration.loadConfiguration(this.file);
    }

    public void saveConfig() {
        try {
            getConfig().save(file);
        } catch (final IOException e) {
            throw new IllegalStateException("Unable to save configuration file!", e);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Config)) return false;
        Config config = (Config) o;
        return n.equals(config.n) &&
                Objects.equals(d, config.d) &&
                Objects.equals(fc, config.fc) &&
                Objects.equals(getFile(), config.getFile());
    }

    @Override
    public int hashCode() {
        return Objects.hash(n, d);
    }
}

