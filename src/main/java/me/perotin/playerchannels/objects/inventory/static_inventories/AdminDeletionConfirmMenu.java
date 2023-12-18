package me.perotin.playerchannels.objects.inventory.static_inventories;

import com.github.stefvanschie.inventoryframework.gui.GuiItem;
import com.github.stefvanschie.inventoryframework.pane.StaticPane;
import me.perotin.playerchannels.PlayerChannels;
import me.perotin.playerchannels.objects.InventoryHelper;
import me.perotin.playerchannels.storage.Pair;
import me.perotin.playerchannels.utils.ChannelUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.function.Consumer;

public class AdminDeletionConfirmMenu extends StaticMenu {

    private InventoryHelper helper;
    private StaticPane yesOrNo;

    public AdminDeletionConfirmMenu(Player viewer, String title) {
        super(viewer, title);
        helper = PlayerChannels.getInstance().getHelper();
        helper.setNavigationBar(getMenu(), viewer);
        helper.setSideDecorationSlots(getMenu());
        helper.setPagingNavBar(getMenu());

        yesOrNo = new StaticPane(3, 2, 3, 1);
        Pair<ItemStack, Integer> confirm = InventoryHelper.getItem("admin-menu.delete-chatroom-confirm", null);
        Pair<ItemStack, Integer> cancel = InventoryHelper.getItem("admin-menu.delete-chatroom-cancel", null);


        ItemStack confirmItem = ChannelUtils.replacePlaceHolderInLore(confirm.getFirst(), "$name$", title.split(" ")[1], 0);
        yesOrNo.addItem(new GuiItem(confirmItem, confirmDeletion(title.split(" ")[1])), confirm.getSecond(), 0);
        yesOrNo.addItem(new GuiItem(cancel.getFirst(), cancelDeletion()), cancel.getSecond(), 0);
        getMenu().addPane(yesOrNo);

    }

    private Consumer<InventoryClickEvent> confirmDeletion(String channelName) {
        return inventoryClickEvent -> {
            inventoryClickEvent.setCancelled(true);
            String chatName = channelName.substring(0, channelName.length() - 1);
            if (PlayerChannels.getInstance().getChatroom(chatName) != null) {

                getViewer().sendMessage(ChatColor.GREEN + "Deleting chatroom: " + chatName);
                getViewer().closeInventory();
                PlayerChannels.getInstance().getChatroom(chatName).delete();
            }
        };
    }

    private Consumer<InventoryClickEvent> cancelDeletion() {
        return inventoryClickEvent -> {
          inventoryClickEvent.setCancelled(true);
          new AdminMenu(getViewer(), "Admin Menu").show(getViewer());
        };

    }





}
