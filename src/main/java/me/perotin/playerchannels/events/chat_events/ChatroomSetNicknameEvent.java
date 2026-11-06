package me.perotin.playerchannels.events.chat_events;

import me.perotin.playerchannels.PlayerChannels;
import me.perotin.playerchannels.objects.Chatroom;
import me.perotin.playerchannels.objects.inventory.paging_objects.ChatroomNicknameManagerPager;
import me.perotin.playerchannels.storage.Pair;
import me.perotin.playerchannels.storage.files.FileType;
import me.perotin.playerchannels.storage.files.ChannelFile;
import org.bukkit.entity.Player;

import java.util.*;

public class ChatroomSetNicknameEvent {

    private PlayerChannels plugin;
    private ChannelFile messages;

    public ChatroomSetNicknameEvent(PlayerChannels plugin) {
        this.plugin = plugin;
        this.messages = new ChannelFile(FileType.MESSAGES);
    }

    /**
     * Map that contains the player who is setting the nickname
     * (mod or up or the player themselves) and the UUID
     * of who they are setting a nickname for and which chatroom it is occuring in
     */
    public static Map<Player, Pair<Chatroom, UUID>> setNickname = new HashMap<>();
    public void processInput(Player chatter, String nickname) {
        if (setNickname.containsKey(chatter)){
            Chatroom chatroom = setNickname.get(chatter).getFirst();
            UUID toSetFor = setNickname.get(chatter).getSecond();

            setNickname.remove(chatter);
            if (messages.getString("cancel").equalsIgnoreCase(nickname)){
                // cancel setting the nickname
                new ChatroomNicknameManagerPager(chatroom, chatter).show();
                return;

            }
            chatroom.setNickname(toSetFor, nickname);
            // Show them the nickname menu again
            new ChatroomNicknameManagerPager(chatroom, chatter).show();
        }
    }

}
