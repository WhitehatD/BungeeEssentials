package me.whitehatd.BungeeEssentials.listeners;

import me.whitehatd.BungeeEssentials.Core;
import me.whitehatd.BungeeEssentials.commands.*;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.api.plugin.Listener;

import java.util.ArrayList;

public class ListenerRegister {

    public ArrayList<Listener> listeners;

    public ListenerRegister(Core core){
        listeners = new ArrayList<>();

        listeners.add(new JoinListener(core));
    }
}
