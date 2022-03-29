package me.whitehatd.BungeeEssentials.commands.warp;

import me.whitehatd.BungeeEssentials.Core;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;
import redis.clients.jedis.Jedis;

public class SetwarpCommand extends Command {

    private final Core core;

    public SetwarpCommand(Core core){
        super("setwarp");
        this.core = core;
    }


    @Override
    public void execute(CommandSender sender, String[] args) {
        if(!(sender instanceof ProxiedPlayer)) return;
        ProxiedPlayer player = (ProxiedPlayer) sender;

        if(!player.hasPermission("bessentials.setwarp")){
            core.getChatUtil().cmessage(player, "messages.no-perm");
            return;
        }

        if(args.length != 1){
            core.getChatUtil().cmessage(player, "messages.setwarp.usage");
            return;
        }

        try(Jedis jedis = core.getJedisPool().getResource()) {
            if (jedis.hget("warps", args[0].toLowerCase()) != null) {
                core.getChatUtil().cmessage(player, "messages.setwarp.already-exist");
                return;
            }
        }

        core.getExecutor().submit(() -> {
            try(Jedis jedis = core.getJedisPool().getResource()) {
                jedis.publish(player.getServer().getInfo().getName(),
                        "setwarp "
                                + args[0].toLowerCase() + " "
                                + player.getName());
            }
        });
    }
}
