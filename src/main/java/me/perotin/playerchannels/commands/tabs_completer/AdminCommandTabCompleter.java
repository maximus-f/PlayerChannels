package me.perotin.playerchannels.commands.tabs_completer;

import me.perotin.playerchannels.PlayerChannels;
import me.perotin.playerchannels.objects.Chatroom;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class AdminCommandTabCompleter implements TabCompleter {


    @Nullable
    @Override
    public List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {

        if (args.length == 1) {
            return new ArrayList<>(Arrays.asList("delete", "reload", "stop"));
        } else if (args.length == 2) {
            if (args[0].equalsIgnoreCase("delete")) {
                return PlayerChannels.getInstance().getChatrooms().stream().map(Chatroom::getName).collect(Collectors.toList());
            }
        }
        return Arrays.asList("delete", "reload", "stop");

    }
}
