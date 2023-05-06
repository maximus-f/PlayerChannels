package me.perotin.privatetalk.events.join;

import me.perotin.privatetalk.PrivateTalk;
import me.perotin.privatetalk.objects.PrivatePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class PrivatePlayerJoinEvent implements Listener {

    private PrivateTalk plugin;

    public PrivatePlayerJoinEvent(PrivateTalk plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event){
        Player join = event.getPlayer();
        PrivatePlayer privatePlayer = PrivatePlayer.getPlayer(join.getUniqueId());
        plugin.addPlayer(privatePlayer);
    }


}
