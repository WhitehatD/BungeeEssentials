package me.whitehatd.BungeeEssentials;

import me.whitehatd.BungeeEssentials.commands.CommandRegister;
import me.whitehatd.BungeeEssentials.listeners.ListenerRegister;
import me.whitehatd.BungeeEssentials.utils.ChatUtil;
import me.whitehatd.BungeeEssentials.utils.Config;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.connection.Server;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.api.plugin.Plugin;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class Core extends Plugin {

    private ChatUtil chatUtil;
    private JedisPool jedisPool;
    private Config config;
    private ExecutorService executor;
    public static Core INSTANCE;
    public static List<ProxiedPlayer> staffMembers;

    public void onEnable(){

        staffMembers = new ArrayList<>();

        INSTANCE = this;

        config = new Config("config.yml", this);
        config.saveDefault();

        chatUtil = new ChatUtil(this);


        jedisPool = new JedisPool(new JedisPoolConfig(),
                getConfig().get().getString("jedis.host"),
                getConfig().get().getInt("jedis.port"),
                0, getConfig().get().getString("jedis.password"));


        executor = Executors.newFixedThreadPool(8);

        executor.submit(() -> {
            try(Jedis jedis = jedisPool.getResource()) {
                jedis.subscribe(new JedisConnection(), "staff-chat", "chat");
            }


        });

        try(Jedis jedis = getJedisPool().getResource()) {
            jedis.set("config", this.getDataFolder().getAbsolutePath() + File.separator + "config.yml");
        }

        CommandRegister register = new CommandRegister(this);
        for(Command command : register.commands)
            getProxy().getPluginManager().registerCommand(this, command);

        ListenerRegister registerListener = new ListenerRegister(this);
        for(Listener listener : registerListener.listeners)
            getProxy().getPluginManager().registerListener(this, listener);

        chatUtil.message(getProxy().getConsole(), "&aBungeeEssentials enabled! Made by WhitehatD#6615");

    }




    public JedisPool getJedisPool() {
        return jedisPool;
    }

    public ChatUtil getChatUtil() {
        return chatUtil;
    }

    public Config getConfig() {
        return config;
    }

    public ExecutorService getExecutor() {
        return executor;
    }

}
