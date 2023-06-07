package me.perotin.privatetalk.events.chat_events;

import me.perotin.privatetalk.PrivateTalk;
import me.perotin.privatetalk.objects.Chatroom;
import me.perotin.privatetalk.objects.inventory.paging_objects.ChatroomNicknameManagerPager;
import me.perotin.privatetalk.objects.inventory.paging_objects.ChatroomPager;
import me.perotin.privatetalk.storage.files.FileType;
import me.perotin.privatetalk.storage.files.PrivateFile;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.Map;

public class ChatroomSetDescriptionEvent implements Listener {

    private PrivateTalk plugin;
    private PrivateFile messages;
    public static Map<Player, Chatroom> setDescription = new HashMap<>();
    public ChatroomSetDescriptionEvent(PrivateTalk plugin) {
        this.plugin = plugin;
        this.messages = new PrivateFile(FileType.MESSAGES);
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
                chatroom.setDescription(newDescription);
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
        }.runTask(PrivateTalk.getInstance());
    }
}
