package me.perotin.privatetalk.commands;

import me.perotin.privatetalk.PrivateTalk;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/* Created by Perotin on 8/20/19 */

/**
 * Base command for PrivateTalk, extends Command for ability to set custom command names, aliases etc.
 */
public class PrivateTalkCommand extends Command implements CommandExecutor  {



    private PrivateTalk plugin;


    public PrivateTalkCommand(@NotNull String name, @NotNull String description, @NotNull String usageMessage, @NotNull List<String> aliases, PrivateTalk plugin) {
        super(name, description, usageMessage, aliases);
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {


        return true;
    }


    // ignored
    @Override
    public boolean execute(@NotNull CommandSender sender, @NotNull String commandLabel, @NotNull String[] args) {
        return false;
    }
}
