package me.perotin.playerchannels.commands;

import me.perotin.playerchannels.PlayerChannels;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class AdminCommand implements CommandExecutor {

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {



        Player player = (Player) sender;

        if (!player.hasPermission("playerchannels.admin")) {
            player.sendMessage(ChatColor.RED + "You do not have permission to execute this command.");
            return true;
        }

        if (args.length == 0) {
            player.sendMessage(ChatColor.RED + "Incorrect usage. Valid sub-commands are:");
            player.sendMessage(ChatColor.RED + "/pcadmin delete <chatroom-name> - Deletes the specified chatroom.");
            player.sendMessage(ChatColor.RED + "/pcadmin reload - Reloads the plugin.");
            player.sendMessage(ChatColor.RED + "/pcadmin stop - Stops the plugin.");
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "delete":
                if (args.length < 2) {
                    player.sendMessage(ChatColor.RED + "You must specify a chatroom to delete.");
                    player.sendMessage(ChatColor.RED + "Usage: /pcadmin delete <chatroom-name>");
                } else {
                    // TODO: Implement delete chatroom functionality.
                    String chatroomName = args[1];
                    if (PlayerChannels.getInstance().getChatroom(chatroomName) != null) {
                        player.sendMessage(ChatColor.GREEN + "Deleting chatroom: " + chatroomName);
                        PlayerChannels.getInstance().getChatroom(chatroomName).delete();
                    } else {
                        player.sendMessage("Does not exist");
                    }
                }
                break;

            case "reload":
                // TODO: Implement reload functionality.
                player.sendMessage(ChatColor.GREEN + "Reloading the plugin...");
                PlayerChannels.getInstance().reloadConfig();
                break;

            case "stop":
                // TODO: Implement stop functionality.
                player.sendMessage(ChatColor.GREEN + "Stopping the plugin...");
                Bukkit.getPluginManager().disablePlugin(PlayerChannels.getInstance());
                break;

            default:
                player.sendMessage(ChatColor.RED + "Invalid sub-command. Valid sub-commands are:");
                player.sendMessage(ChatColor.RED + "/pcadmin delete <chatroom-name> - Deletes the specified chatroom.");
                player.sendMessage(ChatColor.RED + "/pcadmin reload - Reloads the plugin.");
                player.sendMessage(ChatColor.RED + "/pcadmin stop - Stops the plugin.");
                break;
        }

        return true;
    }
}

