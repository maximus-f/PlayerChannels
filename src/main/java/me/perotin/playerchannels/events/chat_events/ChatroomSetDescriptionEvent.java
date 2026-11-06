package me.perotin.playerchannels.events.chat_events;

import me.perotin.playerchannels.PlayerChannels;
import me.perotin.playerchannels.objects.Chatroom;
import me.perotin.playerchannels.objects.inventory.paging_objects.ChatroomPager;
import me.perotin.playerchannels.storage.files.FileType;
import me.perotin.playerchannels.storage.files.ChannelFile;
import me.perotin.playerchannels.utils.ChannelUtils;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;

public class ChatroomSetDescriptionEvent {

    private PlayerChannels plugin;
    private ChannelFile messages;
    public static Map<Player, Chatroom> setDescription = new HashMap<>();
    public ChatroomSetDescriptionEvent(PlayerChannels plugin) {
        this.plugin = plugin;
        this.messages = new ChannelFile(FileType.MESSAGES);
    }

    public void processInput(Player chatter, String newDescription) {
        if (setDescription.containsKey(chatter)) {
            String cancel = messages.getString("cancel");
            Chatroom chatroom = setDescription.get(chatter);
            setDescription.remove(chatter);
            if (ChatColor.stripColor(cancel).equalsIgnoreCase(newDescription)) {
                new ChatroomPager(chatroom, chatter).show();
            } else {
                chatroom.setDescription(ChannelUtils.addColor(newDescription));
                new ChatroomPager(chatroom, chatter).show();

            }


        }
    }

}
