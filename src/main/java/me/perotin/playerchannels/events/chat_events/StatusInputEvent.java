package me.perotin.playerchannels.events.chat_events;

import me.perotin.playerchannels.PlayerChannels;
import me.perotin.playerchannels.objects.PlayerChannelUser;
import me.perotin.playerchannels.objects.inventory.paging_objects.MainMenuPaging;
import me.perotin.playerchannels.objects.inventory.static_inventories.PlayerProfileMenu;
import me.perotin.playerchannels.storage.files.FileType;
import me.perotin.playerchannels.storage.files.ChannelFile;
import me.perotin.playerchannels.utils.ChannelUtils;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashSet;

public class StatusInputEvent implements Listener {
    public static HashSet<Player> enteringStatus = new HashSet<>();
    private ChannelFile messages;

    @EventHandler
    public void onStatusInput(AsyncPlayerChatEvent event) {
        Player chatter = event.getPlayer();
        if (enteringStatus.contains(chatter)) {
            // Entering status
            event.setCancelled(true);
            messages = new ChannelFile(FileType.MESSAGES);
            PlayerChannelUser player = PlayerChannelUser.getPlayer(chatter.getUniqueId());
            String msg = event.getMessage();
            if (!ChatColor.stripColor(messages.getString("cancel")).equalsIgnoreCase(ChatColor.stripColor(msg))){
                player.setStatus(ChannelUtils.addColor(event.getMessage()));

            }
            enteringStatus.remove(chatter);
            new BukkitRunnable(){
                @Override
                public void run() {
                    new PlayerProfileMenu(chatter, player, new MainMenuPaging(chatter, PlayerChannels.getInstance()).getMenu()).show();

                }
            }.runTask(PlayerChannels.getInstance());
        }

    }
}
