package me.perotin.playerchannels.objects.inventory.paging_objects;

import com.github.stefvanschie.inventoryframework.gui.GuiItem;
import com.github.stefvanschie.inventoryframework.gui.type.util.Gui;
import com.github.stefvanschie.inventoryframework.pane.PaginatedPane;
import com.github.stefvanschie.inventoryframework.pane.StaticPane;
import me.perotin.playerchannels.PlayerChannels;
import me.perotin.playerchannels.objects.Chatroom;
import me.perotin.playerchannels.objects.InventoryHelper;
import me.perotin.playerchannels.objects.inventory.actions.ChatroomItemStackAction;
import me.perotin.playerchannels.objects.inventory.static_inventories.AdminDeletionConfirmMenu;
import me.perotin.playerchannels.storage.Pair;
import me.perotin.playerchannels.utils.ItemStackUtils;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class AdminDeleteChatroomPager extends PagingMenu {

    private InventoryHelper helper;

    public AdminDeleteChatroomPager(Player viewer, Gui backMenu) {
        super("Delete a Channel", 6, viewer, backMenu);
        helper = PlayerChannels.getInstance().getHelper();
        helper.setNavigationBar(getMenu(), viewer);
        helper.setSideDecorationSlots(getMenu());
        helper.setPagingNavBar(getMenu());
        setPaginatedPane();
        getPaginatedPane().populateWithGuiItems(generatePages());


    }

    @Override
    protected void setPaginatedPane() {
        this.pane = new PaginatedPane(1, 1, 7, 3);
        getMenu().addPane(pane);

    }

    @Override
    protected List<GuiItem> generatePages() {
        List<ItemStack> toDisplay = PlayerChannels.getInstance().getChatrooms().stream()
                .map(Chatroom::getItemForDeletion)
                .collect(Collectors.toList());
        toDisplay = toDisplay.stream().map(item -> {

            return item;
        }).collect(Collectors.toList());

        // Assuming MainMenuPaging::compare method is correctly implemented for sorting
        toDisplay.sort(MainMenuPaging::compare);

        return toDisplay.stream()
                .map(item -> new GuiItem(item, goToDeletionConfirmationPage()))
                .collect(Collectors.toList());
    }

    private Consumer<InventoryClickEvent> goToDeletionConfirmationPage() {
        return inventoryClickEvent -> {
            inventoryClickEvent.setCancelled(true);
            ItemStack current = inventoryClickEvent.getCurrentItem();
            if (current != null && current.hasItemMeta() && current.getItemMeta().hasDisplayName()) {
                String displayName = current.getItemMeta().getDisplayName();
                String[] parts = displayName.split(" ", 2); // Split the string into two parts

                String channelName = "";
                if (parts.length > 1) {
                    channelName = parts[1]; // Get everything after the first space
                }

                new AdminDeletionConfirmMenu(getViewer(), "Delete " + channelName + "?").show(getViewer());
            }
        };
    }


}
