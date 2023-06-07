package me.perotin.privatetalk.events.chat_events;

import me.perotin.privatetalk.PrivateTalk;
import me.perotin.privatetalk.objects.Chatroom;
import me.perotin.privatetalk.objects.inventory.paging_objects.ChatroomNicknameManagerPager;
import me.perotin.privatetalk.objects.inventory.paging_objects.ChatroomPager;
import me.perotin.privatetalk.objects.inventory.paging_objects.MainMenuPaging;
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

public class ChatroomConfirmDeletionEvent implements Listener {

    private PrivateTalk plugin;
    private PrivateFile messages;
    public static Map<Player, Chatroom> confirmDeletion = new HashMap<>();
    public ChatroomConfirmDeletionEvent(PrivateTalk plugin) {
        this.plugin = plugin;
        this.messages = new PrivateFile(FileType.MESSAGES);
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
                    new MainMenuPaging(chatter, PrivateTalk.getInstance()).show();
                }
            }
        }.runTask(PrivateTalk.getInstance());
    }
}
