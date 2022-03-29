package me.whitehatd.BungeeEssentials.utils;

import me.whitehatd.BungeeEssentials.Core;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;

import java.util.function.Function;

public class ChatUtil {

    private final Core core;

    public ChatUtil(Core core){
        this.core = core;
    }

    public String toColor(String base){
        return ChatColor.translateAlternateColorCodes('&', base);
    }

    public void message(CommandSender player, String string){
        player.sendMessage(toColor(core.getConfig().get().getString("messages.prefix") + string));
    }

    public void cmessageNoPrefix(CommandSender player, String configSec, Function<String, String> format){
        String toSend = core.getConfig().get().getString(configSec);

        player.sendMessage(toColor(format.apply(toSend)));
    }

    public void cmessage(CommandSender player, String configSec, Function<String, String> format){
        String toSend = core.getConfig().get().getString(configSec);

        message(player, format.apply(toSend));
    }

    public void cmessage(CommandSender player, String configSec){
        message(player, core.getConfig().get().getString(configSec));
    }

    public void cmessageList(CommandSender player, String configSec){
        for(String s : core.getConfig().get().getStringList(configSec))
            message(player, s);
    }

    public String capitalize(String input){
        String copy = input;

        copy = String.valueOf(copy.charAt(0)).toUpperCase() + copy.substring(1).toLowerCase();

        copy = copy.replaceAll("_", " ");

        return copy;
    }

}
