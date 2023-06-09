package me.perotin.playerchannels.events.join;

import me.perotin.playerchannels.PlayerChannels;
import me.perotin.playerchannels.objects.PlayerChannelUser;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class PlayerChannelUserJoinEvent implements Listener {

    private PlayerChannels plugin;

    public PlayerChannelUserJoinEvent(PlayerChannels plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event){
        Player join = event.getPlayer();
        PlayerChannelUser playerChannelUser = PlayerChannelUser.getPlayer(join.getUniqueId());
        if (!plugin.getPlayers().contains(playerChannelUser)) {
            plugin.addPlayer(playerChannelUser);
        }
    }


}
