package me.perotin.playerchannels.commands.tabs_completer;

import me.perotin.playerchannels.PlayerChannels;
import me.perotin.playerchannels.objects.Chatroom;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class PlayerChannelsTabCompleter implements TabCompleter {
    @Nullable
    @Override
    public List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        List<String> completions = new ArrayList<>();
        if (args.length == 1) {
            completions.add("create");
            completions.add("join");
            completions.add("listen");
            completions.add("help");
        } else if (args.length == 2) {
            // If the second argument is being typed and it's the 'listen' subcommand
            if (args[0].equalsIgnoreCase("listen")) {
                completions.add("add");
                completions.add("remove");
                completions.add("off");
            }
            if (args[0].equalsIgnoreCase("join")) {
                completions.addAll(getChannelNames());
            }

        } else if (args.length == 3 && args[0].equalsIgnoreCase("listen")) {


            if(args[1].equalsIgnoreCase("add") || args[1].equalsIgnoreCase("remove")) {
                    completions.addAll(getChannelNames());
            }
        }
        return completions;
    }



    private List<String> getChannelNames (){
        return PlayerChannels.getInstance().getChatrooms().stream().map(Chatroom::getName).collect(Collectors.toList());
    }
}
