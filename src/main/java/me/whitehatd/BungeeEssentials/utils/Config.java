package me.whitehatd.BungeeEssentials.utils;

import me.whitehatd.BungeeEssentials.Core;
import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.config.ConfigurationProvider;
import net.md_5.bungee.config.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

public class Config {

    private Configuration config = null;
    private File configfile = null;
    private String name = "";
    private final Core core;

    public Config(String name, Core core){
        this.name = name;
        this.core = core;
    }

    public void reload() {
        if (config == null)
            configfile = new File(core.getDataFolder(), name);
        try {
            config = ConfigurationProvider.getProvider(YamlConfiguration.class).load(configfile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Configuration get() {
        if (config == null)
            reload();
        return config;
    }

    public void save() {
        if (config == null || configfile == null)
            return;
        try {
            ConfigurationProvider.getProvider(YamlConfiguration.class).save(config, configfile);
        } catch (IOException ex) {
        }
    }

    public void saveDefault() {
        if(!core.getDataFolder().exists())
            core.getDataFolder().mkdirs();
        if (config == null)
            configfile = new File(core.getDataFolder(), name);
        if (!configfile.exists()) {
            try {
                Files.copy(core.getResourceAsStream(name), configfile.toPath());

                config = ConfigurationProvider.getProvider(YamlConfiguration.class).load(configfile);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
