package me.perotin.privatetalk.events.chat_events;

import me.perotin.privatetalk.PrivateTalk;
import me.perotin.privatetalk.objects.Chatroom;
import me.perotin.privatetalk.objects.PrivatePlayer;
import me.perotin.privatetalk.storage.files.FileType;
import me.perotin.privatetalk.storage.files.PrivateFile;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

/**
 * @author Perotin
 * Class for when a player is chatting within their chatroom.
 * Chatting in a chatroom can be done in a various amount of ways, so long
 * as the player is in their chat mode, it will occur
 */
public class ChatroomChatEvent implements Listener {

    private PrivateTalk plugin;
    private PrivateFile messages;

    public ChatroomChatEvent(PrivateTalk plugin) {
        this.plugin = plugin;
        this.messages = new PrivateFile(FileType.MESSAGES);
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
        PrivatePlayer privatePlayer = PrivatePlayer.getPlayer(chatter.getUniqueId());
        String message = event.getMessage();
        // Player is opted into one singular chatroom and every message goes as such

       if (privatePlayer.getFocusedChatroom() != null) {
           event.setCancelled(true);
           Chatroom focusedChatroom = privatePlayer.getFocusedChatroom();
           if (focusedChatroom.isMuted(chatter.getUniqueId())) {
               // Player is muted within the chatroom
               chatter.sendMessage(messages.getString("muted-message")
                       .replace("$chatroom$", focusedChatroom.getName()));
               return;
           }
           focusedChatroom.chat(chatter.getName(), event.getMessage());

       }

       // Player used a special prefix and is only in one chatroom
       if (message.startsWith(PrivateTalk.QUICK_CHAT_PREFIX) && privatePlayer.getChatrooms().size() == 1){
           event.setCancelled(true);

           Chatroom quickChatroom = privatePlayer.getChatrooms().get(0);
           if (quickChatroom.isMuted(chatter.getUniqueId())) {
               // Player is muted within the chatroom
               chatter.sendMessage(messages.getString("muted-message")
                       .replace("$chatroom$", quickChatroom.getName()));
               return;
           }
           quickChatroom.chat(chatter.getName(), event.getMessage().substring(1));
           return;
       }

       // Player is using their index keys to "fast chat" in multiple chatrooms
        // TODO

    }
}
