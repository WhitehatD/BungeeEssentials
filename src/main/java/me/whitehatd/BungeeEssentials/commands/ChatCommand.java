package me.whitehatd.BungeeEssentials.commands;

import me.whitehatd.BungeeEssentials.Core;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;
import redis.clients.jedis.Jedis;

public class ChatCommand extends Command {

    private final Core core;

    public ChatCommand(Core core){
        super("chat");

        this.core = core;
    }


    @Override
    public void execute(CommandSender sender, String[] args) {
        if(!(sender instanceof ProxiedPlayer)) return;
        ProxiedPlayer player = (ProxiedPlayer) sender;

        if(!player.hasPermission("bessentials.staff-chat")){
            core.getChatUtil().cmessage(player, "messages.no-perm");
            return;
        }

        if(args.length == 0){
            try(Jedis jedis = core.getJedisPool().getResource()) {
                if (jedis.hget("chat", player.getName()).equals("global")) {
                    jedis.hset("chat", player.getName(), "staff");
                    core.getChatUtil().cmessage(player, "messages.chat.staff");

                    Core.staffMembers.add(player);
                } else {
                    jedis.hset("chat", player.getName(), "global");
                    core.getChatUtil().cmessage(player, "messages.chat.global");

                    Core.staffMembers.remove(player);
                }
            }
        }
    }
}
