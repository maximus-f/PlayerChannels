package me.perotin.playerchannels.commands.subcommands;

import me.perotin.playerchannels.PlayerChannels;
import me.perotin.playerchannels.objects.Chatroom;
import me.perotin.playerchannels.objects.PlayerChannelUser;
import me.perotin.playerchannels.storage.files.ChannelFile;
import me.perotin.playerchannels.storage.files.FileType;
import me.perotin.playerchannels.utils.ChannelUtils;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;


public class ListenSubCommand extends SubCommand{


    public ListenSubCommand(String label) {
        super(label);
    }

    /**
     *
     * @param player
     * @param user
     * @param args
     */
    @Override
    public void onCommand(Player player, PlayerChannelUser user, String[] args) {
                /*
                Possible commands of this form
                /playerchannels listen add <name>
                /playerchannels listen remove <name>
                /playerchannels listen off
                anything else send help
                 */
        String thirdArg = "";
        ChannelFile messages = new ChannelFile(FileType.MESSAGES);
        PlayerChannels plugin = PlayerChannels.getInstance();

        if (args.length > 1) {
            thirdArg = args[1];
        }
        if (args.length == 2) {
            // Check for condition where they do listen off
            if (thirdArg.equalsIgnoreCase("off")) {
                // Remove the player from any blockers and let them view normal global chat again
                user.getListeningChatrooms().clear();
                plugin.getListeningPlayers().remove(player.getUniqueId());
                player.sendMessage(messages.getString("listen-off"));
                return;
            }
        } else if (args.length == 3) {
            String chatroomName = args[2];
            // Need to verify that the chatroom is loaded and active and correct
            Chatroom chatroom = ChannelUtils.getChatroomWith(chatroomName);
            if (chatroom == null) {
                // If null send message
                player.sendMessage(ChatColor.RED + "That chatroom does not exist or is not active!");
                return;
            }
            if (!user.isMemberOf(chatroom)) {
                player.sendMessage(messages.getString("listen-not-in-channel"));
                return;
            }
            if (thirdArg.equalsIgnoreCase("add")) {
                // Add following chatroom
                // Things to check:
                      /*
                      Check if user is in that chatroom first and if they are not already listening to that chatroom
                       */

                if (!user.isListeningTo(chatroom)) {
                    player.sendMessage(messages.getString("listen-added-channel")
                            .replace("$chatroom$", chatroomName));

                    user.addChannelToListen(chatroom);
                    return;
                } else if (user.isListeningTo(chatroom)) {
                    // remove them
                    player.sendMessage(messages.getString("listen-removed-channel")
                            .replace("$chatroom$", chatroomName));
                    user.removeChannelToListen(chatroom);
                    return;
                }


            } else if (thirdArg.equalsIgnoreCase("remove")) {
                // Remove subsequent chatroom
                if (user.isListeningTo(chatroom)) {
                    player.sendMessage(messages.getString("listen-removed-channel")
                            .replace("$chatroom$", chatroomName));
                    user.removeChannelToListen(chatroom);
                    return;
                } else {
                    player.sendMessage(messages.getString("listen-not-currently-listening"));
                    return;
                }


            }

        }

        player.sendMessage(messages.getString("listen-help")
                .replace("$listen$", plugin.getConfig().getString("listen")));
    }
}

