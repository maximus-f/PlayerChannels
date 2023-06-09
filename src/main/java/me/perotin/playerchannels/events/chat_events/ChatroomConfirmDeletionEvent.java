package me.perotin.playerchannels.events.chat_events;

import me.perotin.playerchannels.PlayerChannels;
import me.perotin.playerchannels.objects.Chatroom;
import me.perotin.playerchannels.objects.inventory.paging_objects.ChatroomPager;
import me.perotin.playerchannels.objects.inventory.paging_objects.MainMenuPaging;
import me.perotin.playerchannels.storage.files.FileType;
import me.perotin.playerchannels.storage.files.ChannelFile;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.Map;

public class ChatroomConfirmDeletionEvent implements Listener {

    private PlayerChannels plugin;
    private ChannelFile messages;
    public static Map<Player, Chatroom> confirmDeletion = new HashMap<>();
    public ChatroomConfirmDeletionEvent(PlayerChannels plugin) {
        this.plugin = plugin;
        this.messages = new ChannelFile(FileType.MESSAGES);
    }

    @EventHandler
    public void onConfirmDeletion(AsyncPlayerChatEvent event) {
        Player chatter = event.getPlayer();
        if (confirmDeletion.containsKey(chatter)) {
            event.setCancelled(true);
            Chatroom chatroom = confirmDeletion.get(chatter);
            String nameToMatch = ChatColor.stripColor(chatroom.getName());
            String message = event.getMessage();
            confirmDeletion.remove(chatter);
            if (nameToMatch.equalsIgnoreCase(message)) {
                // Delete chatroom
                chatroom.delete();

                openInvSynchronously(null, chatter);
            } else {
                openInvSynchronously(chatroom, chatter);

            }


        }
    }

    private void openInvSynchronously(Chatroom chatroom, Player chatter) {
        new BukkitRunnable(){
            @Override
            public void run() {
                if (chatroom != null) {
                    new ChatroomPager(chatroom, chatter).show();
                } else {
                    new MainMenuPaging(chatter, PlayerChannels.getInstance()).show();
                }
            }
        }.runTask(PlayerChannels.getInstance());
    }
}
