package me.perotin.playerchannels.events.chat_events;

import me.perotin.playerchannels.PlayerChannels;
import me.perotin.playerchannels.objects.Chatroom;
import me.perotin.playerchannels.objects.inventory.paging_objects.ChatroomNicknameManagerPager;
import me.perotin.playerchannels.storage.Pair;
import me.perotin.playerchannels.storage.files.FileType;
import me.perotin.playerchannels.storage.files.ChannelFile;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

public class ChatroomSetNicknameEvent implements Listener {

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
    @EventHandler
    public void onSetNick(AsyncPlayerChatEvent event) {
        Player chatter = event.getPlayer();
        if (setNickname.containsKey(chatter)){
            event.setCancelled(true);
            String nickname = event.getMessage();
            Chatroom chatroom = setNickname.get(chatter).getFirst();
            UUID toSetFor = setNickname.get(chatter).getSecond();

            setNickname.remove(chatter);
            if (messages.getString("cancel").equalsIgnoreCase(nickname)){
                // cancel setting the nickname
                openInvSynchronously(chatroom, chatter);
                return;

            }
            chatroom.setNickname(toSetFor, nickname);
            // Show them the nickname menu again
            openInvSynchronously(chatroom, chatter);
        }
    }

    private void openInvSynchronously(Chatroom chatroom, Player chatter) {
        new BukkitRunnable(){
            @Override
            public void run() {
                new ChatroomNicknameManagerPager(chatroom, chatter).show();
            }
        }.runTask(PlayerChannels.getInstance());
    }
}
