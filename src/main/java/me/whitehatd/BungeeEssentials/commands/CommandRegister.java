package me.whitehatd.BungeeEssentials.commands;

import me.whitehatd.BungeeEssentials.Core;
import me.whitehatd.BungeeEssentials.commands.speed.FlyspeedCommand;
import me.whitehatd.BungeeEssentials.commands.speed.SpeedCommand;
import me.whitehatd.BungeeEssentials.commands.speed.WalkspeedCommand;
import me.whitehatd.BungeeEssentials.commands.tp.TbaCommand;
import me.whitehatd.BungeeEssentials.commands.tp.TbhCommand;
import me.whitehatd.BungeeEssentials.commands.tp.TbrCommand;
import me.whitehatd.BungeeEssentials.commands.tp.TpCommand;
import me.whitehatd.BungeeEssentials.commands.warp.DelwarpCommand;
import me.whitehatd.BungeeEssentials.commands.warp.SetwarpCommand;
import me.whitehatd.BungeeEssentials.commands.warp.WarpCommand;
import net.md_5.bungee.api.plugin.Command;

import java.util.ArrayList;

public class CommandRegister {

    public ArrayList<Command> commands;

    public CommandRegister(Core core){
        commands = new ArrayList<>();

        commands.add(new TpCommand(core));
        commands.add(new TbrCommand(core));
        commands.add(new TbaCommand(core));
        commands.add(new TbhCommand(core));
        commands.add(new SetwarpCommand(core));
        commands.add(new WarpCommand(core));
        commands.add(new DelwarpCommand(core));
        commands.add(new ChatCommand(core));
        commands.add(new FlyspeedCommand(core));
        commands.add(new WalkspeedCommand(core));
        commands.add(new SpeedCommand(core));
    }
}
