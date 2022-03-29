package me.whitehatd.BungeeEssentials;

import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import redis.clients.jedis.JedisPubSub;

public class JedisConnection extends JedisPubSub {

    private final Core core;

    public JedisConnection(){
        this.core = Core.INSTANCE;
    }

    @Override
    public void onMessage(String channel, String message) {
        if(channel.equals("chat")) {
            ProxiedPlayer player = ProxyServer.getInstance().getPlayer(message.split(" ")[0]);
            String text = message.substring(message.indexOf(" ") + 1);

            for (ProxiedPlayer all : ProxyServer.getInstance().getPlayers())
                core.getChatUtil().cmessageNoPrefix(all, "messages.chat.format",
                        s -> s.replaceAll("%player%", player.getName()).replaceAll("%message%", text));

        } else if(channel.equals("staff-chat")){

            ProxiedPlayer player = ProxyServer.getInstance().getPlayer(message.split(" ")[0]);
            String text = message.substring(message.indexOf(" ") + 1);

            for (ProxiedPlayer all : Core.staffMembers)
                core.getChatUtil().cmessageNoPrefix(all, "messages.chat.staff-format",
                        s -> s.replaceAll("%player%", player.getName()).replaceAll("%message%", text));

        }
    }

}
