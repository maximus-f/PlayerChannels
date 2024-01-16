package me.perotin.playerchannels.commands.tabs_completer;

import me.perotin.playerchannels.PlayerChannels;
import me.perotin.playerchannels.objects.Chatroom;
import me.perotin.playerchannels.objects.PlayerChannelUser;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import sun.net.www.protocol.file.FileURLConnection;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class PlayerChannelsTabCompleter implements TabCompleter {
    @Nullable
    @Override
    public List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        List<String> completions = new ArrayList<>();
        PlayerChannelUser user = PlayerChannelUser.getPlayer(Bukkit.getPlayer(commandSender.getName()).getUniqueId());
        FileConfiguration config = PlayerChannels.getInstance().getConfig();
        String listen = config.getString("listen");
        String create = config.getString("create");
        String join = config.getString("join");
        String invite = config.getString("invite");
        String help = config.getString("help");
        String leave = config.getString("leave");
        String list = config.getString("list");
        if (!user.getChatrooms().isEmpty()) {
            List<String> chatrooms = user.getChatrooms().stream().map(Chatroom::getName).collect(Collectors.toList());
            completions.addAll(chatrooms);
        }


        if (args.length == 1) {
            completions.add(help);
            completions.add(create);
            completions.add(join);
            completions.add(invite);
            completions.add(listen);
            completions.add(leave);
            completions.add(list);

        } else if (args.length == 2) {
            // If the second argument is being typed and it's the 'listen' subcommand
            if (args[0].equalsIgnoreCase(listen)) {
                completions.add("add");
                completions.add("remove");
                completions.add("off");
            }
            if (args[0].equalsIgnoreCase(join)) {
                completions.addAll(getChannelNames());

            }
            if (args[0].equalsIgnoreCase(leave)) {
                if (!user.getChatrooms().isEmpty()) {
                    completions.addAll(user.getChatrooms().stream().map(Chatroom::getName).collect(Collectors.toList()));

                }
            }
            if (args[0].equalsIgnoreCase(invite)){
                List<String> names = Bukkit.getOnlinePlayers().stream().map(Player::getName).collect(Collectors.toList());
                names.remove(user.getName());
                completions.addAll(names);

            }

        } else if (args.length == 3) {
            if (args[0].equalsIgnoreCase(listen)) {


                if (args[1].equalsIgnoreCase("add") || args[1].equalsIgnoreCase("remove")) {
                    completions.addAll(getChannelNames());
                }
            }
            if (args[0].equalsIgnoreCase(invite)) {
                if (user != null) {
                    // Get list of strings of all names that the player is at least a moderator in
                    completions.addAll(user.getChatroomsWithModeratorPermissions().stream().map(Chatroom::getName).collect(Collectors.toList()));
                }
            }
        }
        return completions;
    }



    private List<String> getChannelNames (){
        return PlayerChannels.getInstance().getChatrooms().stream().map(Chatroom::getName).collect(Collectors.toList());
    }
}
