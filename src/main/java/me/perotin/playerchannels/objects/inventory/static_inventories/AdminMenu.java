package me.perotin.playerchannels.objects.inventory.static_inventories;

import com.github.stefvanschie.inventoryframework.gui.GuiItem;
import com.github.stefvanschie.inventoryframework.pane.StaticPane;
import me.perotin.playerchannels.PlayerChannels;
import me.perotin.playerchannels.objects.InventoryHelper;
import me.perotin.playerchannels.objects.inventory.paging_objects.AdminDeleteChatroomPager;
import me.perotin.playerchannels.storage.Pair;
import me.perotin.playerchannels.storage.files.ChannelFile;
import me.perotin.playerchannels.storage.files.FileType;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.function.Consumer;

public class AdminMenu extends StaticMenu {


    private InventoryHelper helper;

    private StaticPane adminTools;


    public AdminMenu(Player viewer, String title) {
        super(viewer, title);
        helper = PlayerChannels.getInstance().getHelper();
        helper.setNavigationBar(getMenu(), getViewer());
        helper.setSideDecorationSlots(getMenu());
        helper.setPagingNavBar(getMenu());
        this.adminTools = new StaticPane(2, 2, 5, 2);
        setAdminTools();
        getMenu().addPane(adminTools);
    }

    private void setAdminTools() {
        ChannelFile messages = new ChannelFile(FileType.MESSAGES);
        Pair<ItemStack, Integer> stopPlugin = InventoryHelper.getItem("admin-menu.stop-plugin", null);
        Pair<ItemStack, Integer> deleteChat = InventoryHelper.getItem("admin-menu.delete-chatroom", null);
        Pair<ItemStack, Integer> spyChatroom = InventoryHelper.getItem("admin-menu.spy-chatroom", null);
        Pair<ItemStack, Integer> reloadPlugin = InventoryHelper.getItem("admin-menu.reload-plugin", null);
        adminTools.addItem(new GuiItem(stopPlugin.getFirst(), stopPlugin()), stopPlugin.getSecond(), 0);
        adminTools.addItem(new GuiItem(deleteChat.getFirst(), deleteChatroom()), deleteChat.getSecond(), 0);
        adminTools.addItem(new GuiItem(spyChatroom.getFirst()), spyChatroom.getSecond(), 0);
        adminTools.addItem(new GuiItem(reloadPlugin.getFirst()), reloadPlugin.getSecond(), 1);

    }

    private Consumer<InventoryClickEvent> stopPlugin(){
        return inventoryClickEvent -> {
            inventoryClickEvent.setCancelled(true);
            Player clicker = (Player) inventoryClickEvent.getWhoClicked();
            clicker.closeInventory();
            clicker.sendMessage(ChatColor.RED + "Stopping PlayerChannels plugin.");
            Bukkit.getPluginManager().disablePlugin(PlayerChannels.getInstance());
        };
    }

    private Consumer<InventoryClickEvent> deleteChatroom(){
        return inventoryClickEvent -> {
            inventoryClickEvent.setCancelled(true);
            Player clicker = (Player) inventoryClickEvent.getWhoClicked();
            clicker.closeInventory();

            // Show another menu with all chatrooms for them to chose to delete one
            new AdminDeleteChatroomPager(clicker, this.getMenu()).show();

        };
    }

//    private Consumer<InventoryClickEvent> spyChatroom(){
//        return inventoryClickEvent -> {
//            inventoryClickEvent.setCancelled(true);
//            Player clicker = (Player) inventoryClickEvent.getWhoClicked();
//            clicker.closeInventory();
//
//            // Asking the admin for the name of the chatroom they wish to spy on.
//            clicker.sendMessage(ChatColor.YELLOW + "Please type the name of the chatroom you want to spy on.");
//
//            // Waiting for player input for chatroom name.
//            // (This is a hypothetical method, actual implementation might vary)
//            PlayerInput.waitForInput(clicker, chatroomName -> {
//                if (ChatroomManager.spyChatroom(clicker, chatroomName)) {
//                    clicker.sendMessage(ChatColor.GREEN + "Now spying on chatroom '" + chatroomName + "'.");
//                } else {
//                    clicker.sendMessage(ChatColor.RED + "Error: Unable to spy on the chatroom. Ensure it exists.");
//                }
//            });
//        };
//    }

//    private Consumer<InventoryClickEvent> reloadPlugin(){
//        return inventoryClickEvent -> {
//            inventoryClickEvent.setCancelled(true);
//            Player clicker = (Player) inventoryClickEvent.getWhoClicked();
//            clicker.closeInventory();
//
//            // Reloading the plugin configuration or any other necessary parts.
//            if (PlayerChannels.getInstance().reload()) {
//                clicker.sendMessage(ChatColor.GREEN + "PlayerChannels plugin has been reloaded successfully.");
//            } else {
//                clicker.sendMessage(ChatColor.RED + "Error: Unable to reload the plugin.");
//            }
//        };
//    }



}
