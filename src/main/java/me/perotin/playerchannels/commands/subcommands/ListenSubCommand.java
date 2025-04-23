package me.perotin.playerchannels.commands.subcommands;

import me.perotin.playerchannels.PlayerChannels;
import me.perotin.playerchannels.objects.Chatroom;
import me.perotin.playerchannels.objects.PlayerChannelUser;
import me.perotin.playerchannels.storage.files.ChannelFile;
import me.perotin.playerchannels.storage.files.FileType;
import me.perotin.playerchannels.utils.ChannelUtils;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public class ListenSubCommand extends SubCommand {

    public ListenSubCommand(String label) {
        super(label);
    }

    /**
     * Handles the listen subcommand which allows players to:
     * - Listen to specific chatrooms they are a member of
     * - Stop listening to specific chatrooms
     * - Turn off listening to all chatrooms
     *
     * Command formats:
     * /playerchannels listen add <name>
     * /playerchannels listen remove <name>
     * /playerchannels listen off
     *
     * @param player The player executing the command
     * @param user The PlayerChannelUser object for the player
     * @param args Command arguments
     */
    @Override
    public void onCommand(Player player, PlayerChannelUser user, String[] args) {
        ChannelFile messages = new ChannelFile(FileType.MESSAGES);
        PlayerChannels plugin = PlayerChannels.getInstance();

        // Display help message if no arguments or invalid format
        if (args.length <= 1) {
            player.sendMessage(messages.getString("listen-help")
                    .replace("$listen$", plugin.getConfig().getString("listen")));
            return;
        }

        String action = args[1].toLowerCase();

        // Handle "listen off" command
        if (action.equals("off") && args.length == 2) {
            user.getListeningChatrooms().clear();
            plugin.getListeningPlayers().remove(player.getUniqueId());
            player.sendMessage(messages.getString("listen-off"));
            return;
        }

        // Handle "listen add/remove <chatroom>" commands
        if ((action.equals("add") || action.equals("remove")) && args.length == 3) {
            String chatroomName = args[2];
            Chatroom chatroom = ChannelUtils.getChatroomWith(chatroomName);

            // Validate chatroom exists
            if (chatroom == null) {
                player.sendMessage(ChatColor.RED + "That chatroom does not exist or is not active!");
                return;
            }

            // Validate player is a member of the chatroom
            if (!user.isMemberOf(chatroom)) {
                player.sendMessage(messages.getString("listen-not-in-channel"));
                return;
            }

            // Handle add command
            if (action.equals("add") && !user.isListeningTo(chatroom)) {
                user.addChannelToListen(chatroom);
                player.sendMessage(messages.getString("listen-added-channel")
                        .replace("$chatroom$", chatroomName));
                return;
            }

            // Handle remove command
            if (action.equals("remove") && user.isListeningTo(chatroom)) {
                user.removeChannelToListen(chatroom);
                player.sendMessage(messages.getString("listen-removed-channel")
                        .replace("$chatroom$", chatroomName));
                return;
            }

            // Handle case where player tries to add a channel they're already listening to
            if (action.equals("add") && user.isListeningTo(chatroom)) {
                user.removeChannelToListen(chatroom);
                player.sendMessage(messages.getString("listen-removed-channel")
                        .replace("$chatroom$", chatroomName));
                return;
            }

            // Handle case where player tries to remove a channel they're not listening to
            if (action.equals("remove") && !user.isListeningTo(chatroom)) {
                player.sendMessage(messages.getString("listen-not-currently-listening"));
                return;
            }
        }

        // Display help message for invalid command format
        player.sendMessage(messages.getString("listen-help")
                .replace("$listen$", plugin.getConfig().getString("listen")));
    }
}
