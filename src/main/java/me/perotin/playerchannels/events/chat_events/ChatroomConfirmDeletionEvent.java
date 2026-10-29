package me.perotin.playerchannels.events.chat_events;

import me.perotin.playerchannels.PlayerChannels;
import me.perotin.playerchannels.objects.Chatroom;
import me.perotin.playerchannels.objects.GlobalChatroom;
import me.perotin.playerchannels.objects.inventory.paging_objects.ChatroomPager;
import me.perotin.playerchannels.objects.inventory.paging_objects.MainMenuPaging;
import me.perotin.playerchannels.storage.files.FileType;
import me.perotin.playerchannels.storage.files.ChannelFile;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;

public class ChatroomConfirmDeletionEvent {

    private PlayerChannels plugin;
    private ChannelFile messages;
    public static Map<Player, Chatroom> confirmDeletion = new HashMap<>();
    public ChatroomConfirmDeletionEvent(PlayerChannels plugin) {
        this.plugin = plugin;
        this.messages = new ChannelFile(FileType.MESSAGES);
    }

    public void processInput(Player chatter, String message) {
        if (confirmDeletion.containsKey(chatter)) {
            Chatroom chatroom = confirmDeletion.get(chatter);
            String nameToMatch = ChatColor.stripColor(chatroom.getName());
            confirmDeletion.remove(chatter);
            if (nameToMatch.equalsIgnoreCase(message)) {
                // Delete chatroom
                chatroom.delete();

                new MainMenuPaging(chatter, PlayerChannels.getInstance()).show();
            } else {
                new ChatroomPager(chatroom, chatter).show();

            }


        }
    }

}
