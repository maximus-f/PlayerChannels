package me.perotin.playerchannels.events.chat_events;

import me.perotin.playerchannels.PlayerChannels;
import me.perotin.playerchannels.objects.Chatroom;
import me.perotin.playerchannels.objects.inventory.paging_objects.ChatroomPager;
import me.perotin.playerchannels.storage.files.FileType;
import me.perotin.playerchannels.storage.files.ChannelFile;
import me.perotin.playerchannels.utils.ChannelUtils;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.Map;

public class ChatroomSetDescriptionEvent implements Listener {

    private PlayerChannels plugin;
    private ChannelFile messages;
    public static Map<Player, Chatroom> setDescription = new HashMap<>();
    public ChatroomSetDescriptionEvent(PlayerChannels plugin) {
        this.plugin = plugin;
        this.messages = new ChannelFile(FileType.MESSAGES);
    }

    @EventHandler
    public void onSetDescription(AsyncPlayerChatEvent event) {
        Player chatter = event.getPlayer();
        if (setDescription.containsKey(chatter)) {
            event.setCancelled(true);
            String cancel = messages.getString("cancel");
            Chatroom chatroom = setDescription.get(chatter);
            String newDescription = event.getMessage();
            setDescription.remove(chatter);
            if (ChatColor.stripColor(cancel).equalsIgnoreCase(newDescription)) {
                openInvSynchronously(chatroom, chatter);
            } else {
                chatroom.setDescription(ChannelUtils.addColor(newDescription));
                openInvSynchronously(chatroom, chatter);

            }


        }
    }

    private void openInvSynchronously(Chatroom chatroom, Player chatter) {
        new BukkitRunnable(){
            @Override
            public void run() {
                new ChatroomPager(chatroom, chatter).show();
            }
        }.runTask(PlayerChannels.getInstance());
    }
}
