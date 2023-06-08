package me.perotin.privatetalk.events.chat_events;

import me.perotin.privatetalk.PrivateTalk;
import me.perotin.privatetalk.objects.PrivatePlayer;
import me.perotin.privatetalk.objects.inventory.paging_objects.MainMenuPaging;
import me.perotin.privatetalk.objects.inventory.static_inventories.PlayerProfileMenu;
import me.perotin.privatetalk.utils.PrivateUtils;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashSet;

public class StatusInputEvent implements Listener {
    public static HashSet<Player> enteringStatus = new HashSet<>();

    @EventHandler
    public void onStatusInput(AsyncPlayerChatEvent event) {
        Player chatter = event.getPlayer();
        if (enteringStatus.contains(chatter)) {
            // Entering status
            event.setCancelled(true);
            PrivatePlayer player = PrivatePlayer.getPlayer(chatter.getUniqueId());
            player.setStatus(PrivateUtils.addColor(event.getMessage()));
            enteringStatus.remove(chatter);
            new BukkitRunnable(){
                @Override
                public void run() {
                    new PlayerProfileMenu(chatter, player, new MainMenuPaging(chatter, PrivateTalk.getInstance()).getMenu()).show();

                }
            }.runTask(PrivateTalk.getInstance());
        }

    }
}
