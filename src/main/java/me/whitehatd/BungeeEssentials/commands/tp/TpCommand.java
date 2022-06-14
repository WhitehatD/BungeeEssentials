package me.whitehatd.BungeeEssentials.commands.tp;

import me.whitehatd.BungeeEssentials.Core;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.api.plugin.TabExecutor;
import org.apache.commons.lang3.math.NumberUtils;
import redis.clients.jedis.Jedis;

import java.util.ArrayList;
import java.util.Collections;
import java.util.stream.Collectors;


public class TpCommand extends Command implements TabExecutor {

    private final Core core;

    public TpCommand(Core core) {
        super("tb");

        this.core = core;
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if(!(sender instanceof ProxiedPlayer)) return;
        ProxiedPlayer player = (ProxiedPlayer) sender;

        switch (args.length) {

            //force tp yourself to another player
            case 1: {
                if(!player.hasPermission("bessentials.tp")){
                    core.getChatUtil().cmessage(player, "messages.no-perm");
                    return;
                }

                ProxiedPlayer target = ProxyServer.getInstance().getPlayer(args[0]);
                if (target == null || !target.isConnected()) {
                    core.getChatUtil().cmessage(player, "messages.tp.invalid-player");
                    return;
                }

                //if they are both on the same server just tp the first to the second
                if (target.getServer().getInfo().getName().equalsIgnoreCase(player.getServer().getInfo().getName())) {
                    core.getExecutor().submit(() -> {
                        try(Jedis jedis = core.getJedisPool().getResource()) {
                            jedis.publish(player.getServer().getInfo().getName(),
                                    "tp-player-same-server " + player.getName() + " " + target.getName());
                        }
                    });
                } else {

                    if(!target.getServer().getInfo().canAccess(player)) {
                        core.getChatUtil().cmessage(player, "messages.tp.cannot-go");
                        return;
                    }
                        core.getExecutor().submit(() -> {
                         player.connect(target.getServer().getInfo());
                        });

                    core.getExecutor().submit(() -> {
                        try(Jedis jedis = core.getJedisPool().getResource()) {
                            jedis.hset("to-tp-player", player.getName(), target.getName());
                        }
                    });
                }
                break;
            }
            // force tp a player to another player
            case 2: {
                if(!player.hasPermission("bessentials.tp-others")){
                    core.getChatUtil().cmessage(player, "messages.no-perm");
                    return;
                }

                ProxiedPlayer from = ProxyServer.getInstance().getPlayer(args[0]);
                if (from == null || !from.isConnected()) {
                    core.getChatUtil().cmessage(player, "messages.tp.invalid-player");
                    return;
                }

                ProxiedPlayer to = ProxyServer.getInstance().getPlayer(args[1]);
                if (to == null || !to.isConnected()) {
                    core.getChatUtil().cmessage(player, "messages.tp.invalid-player");
                    return;
                }

                //if they are both on the same server just tp the first to the second
                if (to.getServer().getInfo().getName().equalsIgnoreCase(from.getServer().getInfo().getName())) {
                    core.getExecutor().submit(() -> {
                        try(Jedis jedis = core.getJedisPool().getResource()) {
                            jedis.publish(to.getServer().getInfo().getName(),
                                    "tp-player-same-server " + from.getName() + " " + to.getName());
                        }
                    });
                } else {
                    core.getExecutor().submit(() -> {
                        from.connect(to.getServer().getInfo());
                    });

                    core.getExecutor().submit(() -> {
                        try (Jedis jedis = core.getJedisPool().getResource()) {
                            jedis.hset("to-tp-player", from.getName(), to.getName());
                        }
                    });
                }

                core.getChatUtil().cmessage(player, "messages.tp.success-other", s ->
                        s.replaceAll("%player%", from.getName()).replaceAll("%target%", to.getName()));
                break;
            }
            // tp to coords in same server
            case 3: {
                if (!player.hasPermission("bessentials.tp")) {
                    core.getChatUtil().cmessage(player, "messages.no-perm");
                    return;
                }

                if (!(NumberUtils.isParsable(args[0].replaceAll("~", "0")) &&
                        NumberUtils.isParsable(args[1].replaceAll("~", "0")) &&
                        NumberUtils.isParsable(args[2].replaceAll("~", "0")))) {
                    core.getChatUtil().cmessage(player, "messages.tp.invalid-coords");
                    return;
                }

                core.getExecutor().submit(() -> {
                    try (Jedis jedis = core.getJedisPool().getResource()) {
                        jedis.publish(player.getServer().getInfo().getName(),
                                "tp-current " + player.getName() + " " + args[0] + " " + args[1] + " " + args[2]);
                    }

                });

                break;
            }
            // tp to coords in different server
            case 4: {
                if(!player.hasPermission("bessentials.tp")){
                    core.getChatUtil().cmessage(player, "messages.no-perm");
                    return;
                }

                if (!(NumberUtils.isParsable(args[0].replaceAll("~", "0")) &&
                        NumberUtils.isParsable(args[1].replaceAll("~", "0")) &&
                        NumberUtils.isParsable(args[2].replaceAll("~", "0")))) {
                    core.getChatUtil().cmessage(player, "messages.tp.invalid-coords");
                    return;
                }


                ServerInfo serverInfo = ProxyServer.getInstance().getServerInfo(args[3]);
                if (serverInfo == null) {
                    core.getChatUtil().cmessage(player, "messages.tp.invalid-server");
                    return;
                }

                if (!serverInfo.canAccess(player)) {
                    core.getChatUtil().cmessage(player, "messages.tp.cannot-go");
                    return;
                }

                core.getExecutor().submit(() -> {
                    player.connect(serverInfo);
                });

                core.getExecutor().submit(() -> {
                    try(Jedis jedis = core.getJedisPool().getResource()) {
                        jedis.publish(player.getServer().getInfo().getName(), "request-location " + player.getName());
                    }
                    try(Jedis jedis = core.getJedisPool().getResource()) {
                        jedis.hset("to-tp", player.getName(), args[0] + " " + args[1] + " " + args[2]);
                    }
                });

                break;
            }

            default: {
                core.getChatUtil().cmessageList(player, "messages.tp.usage");
            }
        }

    }

    @Override
    public Iterable<String> onTabComplete(CommandSender sender, String[] args) {
        if(args.length == 1)
            return ProxyServer.getInstance().getPlayers()
                    .stream()
                    .map(ProxiedPlayer::getName)
                    .filter(name -> name.toLowerCase().startsWith(args[0].toLowerCase()))
                    .collect(Collectors.toList());

        if(args.length == 2)
            return ProxyServer.getInstance().getPlayers()
                    .stream()
                    .map(ProxiedPlayer::getName)
                    .filter(name -> name.toLowerCase().startsWith(args[1].toLowerCase()))
                    .collect(Collectors.toList());

        return Collections.emptyList();
    }
}
