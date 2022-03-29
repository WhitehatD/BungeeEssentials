package me.whitehatd.BungeeEssentials.commands.warp;

import me.whitehatd.BungeeEssentials.Core;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;
import redis.clients.jedis.Jedis;

public class DelwarpCommand extends Command {

    private final Core core;

    public DelwarpCommand(Core core){
        super("delwarp");
        this.core = core;
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if(!(sender instanceof ProxiedPlayer)) return;
        ProxiedPlayer player = (ProxiedPlayer) sender;

        if(!player.hasPermission("bessentials.delwarp")){
            core.getChatUtil().cmessage(player, "messages.no-perm");
            return;
        }

        if(args.length != 1){
            core.getChatUtil().cmessage(player, "messages.delwarp.usage");
            return;
        }

        try(Jedis jedis = core.getJedisPool().getResource()) {
            if (jedis.hget("warps", args[0].toLowerCase()) == null) {
                core.getChatUtil().cmessage(player, "messages.delwarp.no-exist");
                return;
            }
        }
        try(Jedis jedis = core.getJedisPool().getResource()) {
            jedis.hdel("warps", args[0].toLowerCase());
        }
        core.getChatUtil().cmessage(player, "messages.delwarp.success", s -> s.replaceAll("%name%", args[0].toLowerCase()));
    }
}
