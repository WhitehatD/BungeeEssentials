package me.whitehatd.BungeeEssentials.commands.tp;

import me.whitehatd.BungeeEssentials.Core;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.api.plugin.TabExecutor;
import redis.clients.jedis.Jedis;

import java.util.Collections;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class TbrCommand extends Command implements TabExecutor {

    private final Core core;

    public TbrCommand(Core core){
        super("tbr");

        this.core = core;
    }


    @Override
    public void execute(CommandSender sender, String[] args) {
        if(!(sender instanceof ProxiedPlayer)) return;
        ProxiedPlayer player = (ProxiedPlayer) sender;

        if(!player.hasPermission("bessentials.tp.request")){
            core.getChatUtil().cmessage(player, "messages.no-perm");
            return;
        }

        if(args.length != 1){
            core.getChatUtil().cmessage(player, "messages.tbr.usage");
            return;
        }

        ProxiedPlayer target = ProxyServer.getInstance().getPlayer(args[0]);
        if(target == null || !target.isConnected()){
            core.getChatUtil().cmessage(player, "messages.tp.invalid-player");
            return;
        }

        try(Jedis jedis = core.getJedisPool().getResource()) {
            if (jedis.hget("tp-requests", target.getName()) != null) {
                core.getChatUtil().cmessage(player, "messages.tbr.already-sent");
                return;
            }
        }
        try(Jedis jedis = core.getJedisPool().getResource()) {
            jedis.hset("tp-requests", target.getName(), player.getName());
        }

        ProxyServer.getInstance().getScheduler().schedule(core, () -> {
            try(Jedis jedis = core.getJedisPool().getResource()) {
                if (jedis.hget("tp-requests", target.getName()) != null)
                    jedis.hdel("tp-requests", target.getName());
            }
        }, 15, TimeUnit.SECONDS);

        core.getChatUtil().cmessage(player, "messages.tbr.sent", s -> s.replaceAll("%target%", target.getName()));
        core.getChatUtil().cmessage(target, "messages.tbr.received", s -> s.replaceAll("%target%", player.getName()));

    }

    @Override
    public Iterable<String> onTabComplete(CommandSender sender, String[] args) {
        if(args.length == 1)
            return ProxyServer.getInstance().getPlayers()
                    .stream()
                    .map(ProxiedPlayer::getName)
                    .filter(name -> name.toLowerCase().startsWith(args[0].toLowerCase()))
                    .collect(Collectors.toList());
        return Collections.emptyList();
    }
}
