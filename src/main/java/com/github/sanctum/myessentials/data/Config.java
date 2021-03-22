package com.github.sanctum.myessentials.data;

import java.io.*;
import java.util.HashSet;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Encapsulates config file operations.
 */
public class Config {
    private static final Set<Config> INSTANCES = new HashSet<>();
    protected final String n;
    protected final String d;
    protected final File file;
    protected FileConfiguration fc;

    private Config(@NotNull final String n, @Nullable final String d) {
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
        INSTANCES.add(this);
    }

    /**
     * Copy an InputStream directly to a given File.
     * <p>
     * Useful for placing resources retrieved from a JavaPlugin
     * implementation at custom locations.
     *
     * @param in an InputStream, likely a plugin resource
     * @param file the desire file
     * @throws IllegalArgumentException if the file describes a directory
     * @throws IllegalStateException if write is unsuccessful
     */
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
        } catch (FileNotFoundException e) {
            throw new IllegalArgumentException("File is a directory!", e);
        } catch (IOException e) {
            throw new IllegalStateException("Unable to write to file! See log:", e);
        }
    }

    /**
     * Get the name of this Config.
     *
     * @return name of Config
     */
    public String getName() {
        return n;
    }

    /**
     * Get the description of the config if it has one.
     * <p>
     * Used to resolve subdirectory if present.
     *
     * @return an Optional describing this config's description field
     */
    public Optional<String> getDescription() {
        return Optional.ofNullable(d);
    }

    /**
     * Delete the backing file of this configuration.
     * <p>
     * Does not destroy backing YamlConfiguration; perform
     * {@link Config#reload()} if that is desired as well.
     *
     * @return true if file was successfully deleted
     */
    public boolean delete() {
        return file.delete();
    }

    /**
     * Check if the backing file currently exists.
     * <p>
     * Does interact whatsoever with the YamlConfiguration.
     *
     * @return true if file exists
     */
    public boolean exists() {
        return file.exists();
    }

    /**
     * Get the backing file for this Config; additionally creates
     * if not found.
     *
     * @return backing file File object
     */
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

    /**
     * Get the FileConfiguration managed by this Config object.
     *
     * @return a File (Yaml) Configuration object
     */
    synchronized public FileConfiguration getConfig() {
        if(this.fc == null) {
            // fast exit with new blank configuration in the case of nonexistent file
            if (!file.exists()) return new YamlConfiguration();
            // load configuration from file
            this.fc = YamlConfiguration.loadConfiguration(file);
        }
        return this.fc;
    }

    /**
     * Reload the file from disk.
     * <p>
     * If the backing file has been deleted, this method assigns a fresh,
     * blank configuration internally to this object. Otherwise, the file
     * is read from, directly replacing the existing configuration with
     * its values. No attempt is made to save the existing configuration
     * state, so keep that in mind when running this call.
     */
    synchronized public void reload() {
        if (!this.file.exists()) {
            this.fc = new YamlConfiguration();
        }
        this.fc = YamlConfiguration.loadConfiguration(this.file);
    }

    /**
     * Save the configuration to its backing file.
     *
     * @throws IllegalStateException if an error is encountered while saving
     */
    synchronized public void saveConfig() {
        try {
            getConfig().save(file);
        } catch (final IOException e) {
            throw new IllegalStateException("Unable to save configuration file!", e);
        }
    }

    /**
     * Retrieve a Config instance via its name and description.
     * <p>
     * This method only resolves config objects for myEssentials; if you
     * wish to manage your own configs see this method and the fields
     * above for a general format of a static backing.
     *
     * @param name Name of config file
     * @param desc Description of config file (designate subdirectory)
     * @return existing instance or create new Config
     * @throws IllegalArgumentException if name is empty
     */
    public static Config get(@NotNull final String name, final String desc) throws IllegalArgumentException {
        // move up to fail fast
        if (name.isEmpty()) {
            throw new IllegalArgumentException("Name cannot be empty!");
        }
        // switch to stream api
        final int hashCode = Objects.hash(name, desc);
        return INSTANCES.stream()
                .filter(c -> c.hashCode() == hashCode)
                .findAny()
                .orElseGet(() -> new Config(name, desc));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Config)) return false;
        Config config = (Config) o;
        return n.equals(config.n) &&
                Objects.equals(d, config.d) &&
                Objects.equals(fc, config.fc) &&
                Objects.equals(file, config.file);
    }

    @Override
    public int hashCode() {
        return Objects.hash(n, d);
    }
}

