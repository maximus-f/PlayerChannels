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

import java.util.HashSet;

public class StatusInputEvent {
    public static HashSet<Player> enteringStatus = new HashSet<>();
    private ChannelFile messages;

    public void processInput(Player chatter, String msg) {
        if (enteringStatus.contains(chatter)) {
            // Entering status
            messages = new ChannelFile(FileType.MESSAGES);
            PlayerChannelUser player = PlayerChannelUser.getPlayer(chatter.getUniqueId());
            if (!ChatColor.stripColor(messages.getString("cancel")).equalsIgnoreCase(ChatColor.stripColor(msg))){
                player.setStatus(ChannelUtils.addColor(msg));

            }
            enteringStatus.remove(chatter);
            new PlayerProfileMenu(chatter, player, new MainMenuPaging(chatter, PlayerChannels.getInstance()).getMenu()).show();
        }

    }
}
