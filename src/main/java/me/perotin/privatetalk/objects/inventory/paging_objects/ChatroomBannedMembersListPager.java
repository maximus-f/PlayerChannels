package me.perotin.privatetalk.objects.inventory.paging_objects;

import com.github.stefvanschie.inventoryframework.gui.GuiItem;
import com.github.stefvanschie.inventoryframework.pane.PaginatedPane;
import com.github.stefvanschie.inventoryframework.pane.Pane;
import com.github.stefvanschie.inventoryframework.pane.StaticPane;
import me.perotin.privatetalk.PrivateTalk;
import me.perotin.privatetalk.objects.Chatroom;
import me.perotin.privatetalk.utils.ItemStackUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Class for viewing all banned players and unbanning them
 */
public class ChatroomBannedMembersListPager extends PagingMenu {

    private Chatroom chatroom;
    private PrivateTalk plugin;

    public ChatroomBannedMembersListPager(Chatroom chatroom, Player viewer){
        super(chatroom.getName() + ": Banned players", 6, viewer, new ChatroomPager(chatroom, viewer).getMenu());
        this.chatroom = chatroom;
        this.plugin = PrivateTalk.getInstance();
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
