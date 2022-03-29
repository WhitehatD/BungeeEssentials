package me.whitehatd.BungeeEssentials.commands.tp;

import me.whitehatd.BungeeEssentials.Core;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;
import redis.clients.jedis.Jedis;

public class TbaCommand extends Command {

    private final Core core;

    public TbaCommand(Core core){
        super("tba");

        this.core = core;
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (!(sender instanceof ProxiedPlayer)) return;
        ProxiedPlayer player = (ProxiedPlayer) sender;

        if (!player.hasPermission("bessentials.tp.request")) {
            core.getChatUtil().cmessage(player, "messages.no-perm");
            return;
        }

        try (Jedis jedis = core.getJedisPool().getResource()) {
            if (jedis.hget("tp-requests", player.getName()) == null) {
                core.getChatUtil().cmessage(player, "messages.tba.no-req");
                return;
            }
        }
        try (Jedis jedis = core.getJedisPool().getResource()) {
            if (jedis.hget("tp-requests", player.getName()).contains("@")) {   //means that it's a tp here request

                ProxiedPlayer from = ProxyServer.getInstance().getPlayer(jedis.hget("tp-requests", player.getName())
                        .replaceAll("@", ""));
                if (from == null || !from.isConnected()) {
                    core.getChatUtil().cmessage(player, "messages.tba.error");
                    return;
                }

                core.getChatUtil().cmessage(player, "messages.tba.success");
                jedis.hdel("tp-requests", player.getName());

                if (player.getServer().getInfo().getName().equalsIgnoreCase(from.getServer().getInfo().getName())) {
                    core.getExecutor().submit(() -> {
                        jedis.publish(player.getServer().getInfo().getName(),
                                "tp-player-same-server " + player.getName() + " " + from.getName());
                    });
                } else {
                    core.getExecutor().submit(() -> {
                        player.connect(from.getServer().getInfo());
                    });

                    core.getExecutor().submit(() -> {
                        jedis.hset("to-tp-player", player.getName(), from.getName());
                    });
                }

            } else {

                ProxiedPlayer from = ProxyServer.getInstance().getPlayer(jedis.hget("tp-requests", player.getName()));

                if (from == null || !from.isConnected()) {
                    core.getChatUtil().cmessage(player, "messages.tba.error");
                    return;
                }

                core.getChatUtil().cmessage(player, "messages.tba.success");
                jedis.hdel("tp-requests", player.getName());

                if (player.getServer().getInfo().getName().equalsIgnoreCase(from.getServer().getInfo().getName())) {
                    core.getExecutor().submit(() -> {
                        jedis.publish(player.getServer().getInfo().getName(),
                                "tp-player-same-server " + from.getName() + " " + player.getName());
                    });
                } else {
                    core.getExecutor().submit(() -> {
                        from.connect(player.getServer().getInfo());
                    });

                    core.getExecutor().submit(() -> {
                        jedis.hset("to-tp-player", from.getName(), player.getName());
                    });
                }
            }
        }
    }
}
