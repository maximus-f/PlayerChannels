package me.perotin.playerchannels.objects.inventory.paging_objects;

import com.github.stefvanschie.inventoryframework.gui.GuiItem;
import me.perotin.playerchannels.PlayerChannels;
import me.perotin.playerchannels.objects.Chatroom;
import me.perotin.playerchannels.utils.ItemStackUtils;
import me.perotin.playerchannels.utils.ChannelUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Class for viewing all banned players and unbanning them
 */
public class ChatroomBannedMembersListPager extends PagingMenu {

    private Chatroom chatroom;
    private PlayerChannels plugin;

    public ChatroomBannedMembersListPager(Chatroom chatroom, Player viewer){
        super(ChannelUtils.getMessageString("chatroom-ban-menu-title").replace("$chatroom$", chatroom.getName()).replace("$count$", chatroom.getBannedMembers().size()+""), 6, viewer, new ChatroomPager(chatroom, viewer).getMenu());
        this.chatroom = chatroom;
        this.plugin = PlayerChannels.getInstance();
        plugin.getHelper().setSideDecorationSlots(getMenu());
        plugin.getHelper().setNavigationBar(getMenu(), getViewer());
        getPaginatedPane().populateWithGuiItems(generatePages());
    }

    @Override
    protected List<GuiItem> generatePages() {
        List<GuiItem> items = new ArrayList<>();
        for(UUID uuid: chatroom.getBannedMembers()){
            // Get the player, either online or offline
            OfflinePlayer player = Bukkit.getOfflinePlayer(uuid);

            // Create a player head ItemStack for this player
            ItemStack playerHead = new ItemStackUtils(Material.PLAYER_HEAD)
                    .setOwner(player)
                    .setName(ChatColor.YELLOW + player.getName())
                    .setLore(ChatColor.GRAY + "Click to unban").build();



            GuiItem guiItem = new GuiItem(playerHead, event -> {
                event.setCancelled(true);
                chatroom.unbanMember(uuid);
                new ChatroomBannedMembersListPager(chatroom, getViewer()).show();
            });

            items.add(guiItem);
        }
        return items;
    }

//    // This is probably not needed I think
//    @Override
//    protected void setPaginatedPane(){
//        pane = new PaginatedPane(1, 2, 7, 3);
//        getMenu().addPane(pane);
//    }

}
