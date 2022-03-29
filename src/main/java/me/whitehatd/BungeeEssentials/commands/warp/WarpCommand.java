package me.whitehatd.BungeeEssentials.commands.warp;

import me.whitehatd.BungeeEssentials.Core;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;
import redis.clients.jedis.Jedis;

public class WarpCommand extends Command {

    private final Core core;

    public WarpCommand(Core core){
        super("warp", null,  "w");
        this.core = core;
    }


    @Override
    public void execute(CommandSender sender, String[] args) {
        if(!(sender instanceof ProxiedPlayer)) return;
        ProxiedPlayer player = (ProxiedPlayer) sender;

        if(args.length != 1){
            core.getChatUtil().cmessage(player, "messages.warp.usage");
            return;
        }

        if(args[0].equalsIgnoreCase("list")){
            if(!player.hasPermission("bessentials.warp.list")){
                core.getChatUtil().cmessage(player, "messages.no-perm");
                return;
            }
            StringBuilder finalWarps = new StringBuilder();
            try(Jedis jedis = core.getJedisPool().getResource()) {
                if (jedis.hgetAll("warps").isEmpty())
                    finalWarps.append("No available warps");
                else {
                    for (String warp : jedis.hgetAll("warps").keySet()) {
                        finalWarps.append(warp).append(", ");
                    }
                    finalWarps.deleteCharAt(finalWarps.length() - 2);
                }
            }
            core.getChatUtil().cmessage(player, "messages.warp.list", s -> s.replaceAll("%warps%", finalWarps.toString()));
            return;
        }

        try(Jedis jedis = core.getJedisPool().getResource()) {
            if (jedis.hget("warps", args[0].toLowerCase()) == null) {
                core.getChatUtil().cmessage(player, "messages.warp.no-exist");
                return;
            }
        }

        if(!player.hasPermission("bessentials.warp." + args[0].toLowerCase())){
            core.getChatUtil().cmessage(player, "messages.no-perm");
            return;
        }
        try(Jedis jedis = core.getJedisPool().getResource()) {
            String warpServer = jedis.hget("warps", args[0].toLowerCase()).split(" ")[0];

            if (warpServer.equalsIgnoreCase(player.getServer().getInfo().getName())) { // player is on the same server as warp, normally tp them
                core.getExecutor().submit(() -> {
                    jedis.publish(warpServer, "warp-player " + args[0].toLowerCase() + " " + player.getName());
                });
            } else {
                core.getExecutor().submit(() -> {
                    player.connect(ProxyServer.getInstance().getServerInfo(warpServer));
                });

                core.getExecutor().submit(() -> {
                    jedis.hset("to-tp-warp", player.getName(), args[0].toLowerCase());
                });
            }
        }
    }
}
