package me.whitehatd.BungeeEssentials.commands.speed;

import me.whitehatd.BungeeEssentials.Core;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;
import org.apache.commons.lang3.math.NumberUtils;
import redis.clients.jedis.Jedis;

public class SpeedCommand extends Command {

    private final Core core;

    public SpeedCommand(Core core){
        super("speed");

        this.core = core;
    }


    @Override
    public void execute(CommandSender sender, String[] args) {
        if(!(sender instanceof ProxiedPlayer)) return;
        ProxiedPlayer player = (ProxiedPlayer) sender;

        if(!player.hasPermission("bessentials.speed")){
            core.getChatUtil().cmessage(player, "messages.no-perm");
            return;
        }

        if(args.length != 1){
            core.getChatUtil().cmessage(player, "messages.speed.usage");
            return;
        }

        if(!NumberUtils.isParsable(args[0])) {
            core.getChatUtil().cmessage(player, "messages.no-num");
            return;
        }

        Integer number = NumberUtils.toInt(args[0]);
        if(number < 1 || number > 10){
            core.getChatUtil().cmessage(player, "messages.no-num");
            return;
        }

        try(Jedis jedis = core.getJedisPool().getResource()) {
            jedis.hset("flyspeed", player.getName(), number.toString());
        }
        try(Jedis jedis = core.getJedisPool().getResource()) {
            jedis.publish(player.getServer().getInfo().getName(), "set-fly-speed " + player.getName() + " " + number);
        }
        try(Jedis jedis = core.getJedisPool().getResource()) {
            jedis.hset("walkspeed", player.getName(), number.toString());
        }
        try(Jedis jedis = core.getJedisPool().getResource()) {
            jedis.publish(player.getServer().getInfo().getName(), "set-walk-speed " + player.getName() + " " + number);
        }

        core.getChatUtil().cmessage(player, "messages.speed.success", s -> s.replaceAll("%number%", number + ""));
    }
}
