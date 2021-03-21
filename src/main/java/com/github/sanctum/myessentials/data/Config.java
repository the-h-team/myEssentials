package com.github.sanctum.myessentials.data;

import com.github.sanctum.myessentials.Essentials;
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

public class Config {
    private final String n;
    private final String d;
    private FileConfiguration fc;
    private File file;
    private static final List<Config> configs = new ArrayList<>();

    public Config(final String n, final String d) {
        this.n = n;
        this.d = d;
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
        Config.configs.removeIf(c -> c.equals(this));
        return this.getFile().delete();
    }

    public boolean exists() {
        if (this.file == null) {
            final File temp = new File(this.getDataFolder(), this.getName() + ".yml");
            if (!temp.exists()) {
                return false;
            } else {
                this.file = temp;
            }
        }
        return true;
    }

    public File getFile() {
        if(this.file == null) {
            this.file = new File(this.getDataFolder(), this.getName() + ".yml"); //create method get data folder
            if(!this.file.exists()) {
                try {
                    //noinspection ResultOfMethodCallIgnored
                    this.file.createNewFile();
                }catch(final IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return this.file;
    }

    public FileConfiguration getConfig() {
        if(this.fc == null) {
            this.fc = YamlConfiguration.loadConfiguration(this.getFile());
        }
        return this.fc;
    }

    public File getDataFolder() {
        final File dir = new File(Config.class.getProtectionDomain().getCodeSource().getLocation().getPath().replaceAll("%20", " "));
        File d;
        if (this.d != null) {
            d = new File(dir.getParentFile().getPath(), Essentials.getInstance().getName() + "/" + this.d + "/");
        } else {
            d = new File(dir.getParentFile().getPath(), Essentials.getInstance().getName());
        }
        if (!d.exists()) {
            //noinspection ResultOfMethodCallIgnored
            d.mkdirs();
        }
        return d;
    }

    public void reload() {
        this.file = new File(getDataFolder(), getName() + ".yml");
        if (!this.file.exists())
            try {
                //noinspection ResultOfMethodCallIgnored
                this.file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        this.fc = YamlConfiguration.loadConfiguration(this.file);
        File defConfigStream = new File(getDataFolder(), getName() + ".yml");
        YamlConfiguration defConfig = YamlConfiguration.loadConfiguration(defConfigStream);
        this.fc.setDefaults(defConfig);
        configs.removeIf(c -> c.getName().equals(n));
    }

    public void saveConfig() {
        try {
            this.getConfig().save(this.getFile());
        } catch (final IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Config)) return false;
        Config config = (Config) o;
        return n.equals(config.n) &&
                d.equals(config.d) &&
                Objects.equals(fc, config.fc) &&
                Objects.equals(getFile(), config.getFile());
    }

    @Override
    public int hashCode() {
        return Objects.hash(n, d);
    }
}

