package me.perotin.playerchannels.events.join;

import me.perotin.playerchannels.PlayerChannels;
import me.perotin.playerchannels.events.chat_events.CreateChatroomInputEvent;
import me.perotin.playerchannels.objects.PlayerChannelUser;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

/**
 * Listener class to check if player is registered with PlayerChannels.
 * Registers if not.
 */

public class PlayerChannelUserJoinEvent implements Listener {

    private PlayerChannels plugin;

    public PlayerChannelUserJoinEvent(PlayerChannels plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event){
        Player join = event.getPlayer();
        PlayerChannelUser playerChannelUser = PlayerChannelUser.getPlayer(join.getUniqueId());
        plugin.addPlayer(playerChannelUser);
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        plugin.clearPendingChatInput(player);
        CreateChatroomInputEvent creation = CreateChatroomInputEvent.getInstance();
        creation.getInCreation().remove(player.getUniqueId());
        plugin.getListeningPlayers().remove(player.getUniqueId());
        plugin.forgetChatRouting(player);
    }

}
