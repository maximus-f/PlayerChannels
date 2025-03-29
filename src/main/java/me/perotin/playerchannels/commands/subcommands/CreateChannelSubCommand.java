package me.perotin.playerchannels.commands.subcommands;

import me.perotin.playerchannels.PlayerChannels;
import me.perotin.playerchannels.events.chat_events.CreateChatroomInputEvent;
import me.perotin.playerchannels.objects.PlayerChannelUser;
import me.perotin.playerchannels.objects.PreChatroom;
import me.perotin.playerchannels.objects.inventory.actions.CreateChatroomAction;
import me.perotin.playerchannels.storage.files.ChannelFile;
import me.perotin.playerchannels.storage.files.FileType;
import me.perotin.playerchannels.utils.PermissionsHandler;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class CreateChannelSubCommand extends SubCommand {


    public CreateChannelSubCommand(String label) {
        super(label);
    }

    @Override
    public void onCommand(Player player, PlayerChannelUser user, String[] args) {
        ChannelFile messages = new ChannelFile(FileType.MESSAGES);
        PlayerChannels plugin = PlayerChannels.getInstance();
        if (PlayerChannels.getInstance().isCreatePermission()) {
             if (!player.hasPermission("playerchannels.create")) {
                messages.sendConfigMsg(player, "no-permission");
                return;
            }
            if (PlayerChannels.getInstance().checkForLimit() && !(player.hasPermission("playerchannels.create.*") || player.hasPermission("playerchannels.create.bypass"))) {
                int limit = new PermissionsHandler().getMaxChannels(player);
                int currentCount = user.getOwnedChannelsSize();

                 if (currentCount >= limit) {
                    // At the limit
                    player.sendMessage(messages.getString("channel-creation-limit")
                            .replace("$count$", ""+limit));
                    return;
                }

             }
        }
        if (args.length == 1) {
            // Typed just /pc create
            // Check if has permissions, "Player's channel" <- check if that is available
            String baseChannelName = player.getName() + "'s-Channel";
            String channelName = baseChannelName;
            if (plugin.getChatroom(channelName) != null) {
                // Keep adding numbers until not found
                int addition = 1; // Start from 1
                while (plugin.getChatroom(channelName) != null) {
                    // Append the number to the base channel name, not the already modified channel name
                    channelName = baseChannelName + addition;
                    addition++;
                }
            }

            PreChatroom chatroom = new PreChatroom(player.getUniqueId());
           chatroom.setName(channelName);
           chatroom.setDescription("");
           CreateChatroomAction.createChatroom(chatroom, user, player);
        } else if (args.length == 2) {
            // Typed /pc create <name>
            String channelName = args[1];
            if (CreateChatroomInputEvent.isNameTaken(channelName)) {
                player.sendMessage(messages.getString("taken-name"));
            }  else {
                // Create it
                PreChatroom chatroom = new PreChatroom(player.getUniqueId());
                chatroom.setName(channelName);
                chatroom.setDescription("");
                CreateChatroomAction.createChatroom(chatroom, user, player);
            }


        } else if (args.length >= 3) {
            // Typed /pc create <name> <description>
            String channelName = args[1];
            StringBuilder description = new StringBuilder();
            for (int j = 2; j < args.length; j++) {
                description.append(args[j]).append(" ");
            }
            if (CreateChatroomInputEvent.isNameTaken(channelName)) {
                player.sendMessage(messages.getString("taken-name"));
            }  else {
                // Create it
                PreChatroom chatroom = new PreChatroom(player.getUniqueId());
                chatroom.setName(channelName);
                chatroom.setDescription(description.toString().trim());
                CreateChatroomAction.createChatroom(chatroom, user, player);
            }
        }

    }
}
