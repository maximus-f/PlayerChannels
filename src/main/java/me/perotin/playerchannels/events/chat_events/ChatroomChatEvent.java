package me.perotin.playerchannels.events.chat_events;

import me.perotin.playerchannels.PlayerChannels;
import me.perotin.playerchannels.objects.Chatroom;
import me.perotin.playerchannels.objects.PlayerChannelUser;
import me.perotin.playerchannels.storage.files.FileType;
import me.perotin.playerchannels.storage.files.ChannelFile;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import java.util.stream.Collectors;

/**
 * @author Perotin
 * Class for when a player is chatting within their chatroom.
 * Chatting in a chatroom can be done in a various amount of ways, so long
 * as the player is in their chat mode, it will occur
 */
public class ChatroomChatEvent implements Listener {

    private PlayerChannels plugin;
    private ChannelFile messages;

    public ChatroomChatEvent(PlayerChannels plugin) {
        this.plugin = plugin;
        this.messages = new ChannelFile(FileType.MESSAGES);
    }


    /**
     * Checks for chat event occuring in chatroom and cancels event and sends message to members
     * of chatroom. This approach is the focused chat which players opt into on the chatroom menu
     *
     * Prefix with configurable symbol is the other approach
     * @param event
     */
    @EventHandler
    public void onChatroomChat(AsyncPlayerChatEvent event) {
        Player chatter = event.getPlayer();
        // Should be able to retrieve private player without issue
        PlayerChannelUser playerChannelUser = PlayerChannelUser.getPlayer(chatter.getUniqueId());
        String message = event.getMessage();
        // Player is opted into one singular chatroom and every message goes as such

        // Remove players listening to any chatroom
        plugin.getListeningPlayers().stream().map(Bukkit::getPlayer).collect(Collectors.toList()).forEach(event.getRecipients()::remove);

       if (playerChannelUser.getFocusedChatroom() != null) {
           event.setCancelled(true);
           Chatroom focusedChatroom = playerChannelUser.getFocusedChatroom();
           if (focusedChatroom.isMuted(chatter.getUniqueId())) {
               // Player is muted within the chatroom
               chatter.sendMessage(messages.getString("muted-message")
                       .replace("$chatroom$", focusedChatroom.getName()));
               return;
           }
           focusedChatroom.chat(chatter.getName(), event.getMessage(), chatter.getUniqueId());

       }

       // Player used a special prefix and is only in one chatroom
       if (message.startsWith(PlayerChannels.QUICK_CHAT_PREFIX) && playerChannelUser.getChatrooms().size() == 1){
           event.setCancelled(true);

           Chatroom quickChatroom = playerChannelUser.getChatrooms().get(0);
           if (quickChatroom.isMuted(chatter.getUniqueId())) {
               // Player is muted within the chatroom
               chatter.sendMessage(messages.getString("muted-message")
                       .replace("$chatroom$", quickChatroom.getName()));
               return;
           }
           quickChatroom.chat(chatter.getName(), event.getMessage().substring(1), chatter.getUniqueId());
           return;
       }

       // Player is using their index keys to "fast chat" in multiple chatrooms
        // TODO
        if (message.matches("^[1-9][0-9]?:.*$")) {
            event.setCancelled(true);

            String[] parts = message.split(":", 2);
            int chatroomIndex;
            try {
                chatroomIndex = Integer.parseInt(parts[0]) - 1;
            } catch (NumberFormatException e) {
                chatter.sendMessage("Invalid chatroom index");
                return;
            }

            int chatroomsSize = playerChannelUser.getChatrooms().size();
            if (chatroomIndex < 0 || chatroomIndex >= chatroomsSize) {
                String errorMessage;
                if (chatroomsSize == 1) {
                    errorMessage = messages.getString("fast-chat-invalid");
                } else {
                    errorMessage = messages.getString("fast-chat-invalid-range")
                            .replace("$end$", Integer.toString(chatroomsSize));
                }
                chatter.sendMessage(errorMessage);
                return;
            }

            Chatroom fastChatroom = playerChannelUser.getChatrooms().get(chatroomIndex);
            if (fastChatroom.isMuted(chatter.getUniqueId())) {
                // Player is muted within the chatroom
                chatter.sendMessage(messages.getString("muted-message")
                        .replace("$chatroom$", fastChatroom.getName()));
                return;
            }
            fastChatroom.chat(chatter.getName(), parts[1].trim(), chatter.getUniqueId());
            return;
        }



    }
}
