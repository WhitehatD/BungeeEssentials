package me.whitehatd.BungeeEssentials.listeners;

import me.whitehatd.BungeeEssentials.Core;
import net.md_5.bungee.api.event.PlayerDisconnectEvent;
import net.md_5.bungee.api.event.PostLoginEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;
import redis.clients.jedis.Jedis;

public class JoinListener implements Listener {

    private final Core core;

    public JoinListener(Core core){
        this.core = core;
    }

    @EventHandler
    public void onConnect(PostLoginEvent e){
        //player logs into the proxy for the first time
        try(Jedis jedis = core.getJedisPool().getResource()) {

            if (jedis.hget("chat", e.getPlayer().getName()) == null) {

                jedis.hset("chat", e.getPlayer().getName(), "global");
                jedis.hset("walkspeed", e.getPlayer().getName(), "1");
                jedis.hset("flyspeed", e.getPlayer().getName(), "1");
            }

            if (jedis.hget("chat", e.getPlayer().getName()).equals("staff"))
                Core.staffMembers.add(e.getPlayer());
        }
    }

    @EventHandler
    public void onDisconnect(PlayerDisconnectEvent e){
        try(Jedis jedis = core.getJedisPool().getResource()) {

            if (jedis.hget("chat", e.getPlayer().getName()).equals("staff"))
                Core.staffMembers.remove(e.getPlayer());
        }
    }
}
