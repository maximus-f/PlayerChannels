package me.perotin.playerchannels.objects.inventory.paging_objects;


import com.github.stefvanschie.inventoryframework.gui.GuiItem;
import com.github.stefvanschie.inventoryframework.gui.type.util.Gui;
import com.github.stefvanschie.inventoryframework.pane.PaginatedPane;
import me.perotin.playerchannels.PlayerChannels;
import me.perotin.playerchannels.objects.Chatroom;
import me.perotin.playerchannels.objects.InventoryHelper;
import me.perotin.playerchannels.utils.ChannelUtils;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class AdminSpyChatroomPager extends PagingMenu {

    private InventoryHelper helper;

    public AdminSpyChatroomPager(Player viewer, Gui backMenu) {
        super("Spy on a Channel", 6, viewer, backMenu);
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
        getMenu().addPane(pane);    }

    @Override
    protected List<GuiItem> generatePages() {
        UUID admin = getViewer().getUniqueId();
        List<ItemStack> toDisplay = PlayerChannels.getInstance().getChatrooms().stream()
                .map(c -> c.getItemForSpy(admin))
                .collect(Collectors.toList());
        toDisplay = toDisplay.stream().map(item -> {

            return item;
        }).collect(Collectors.toList());

        // Assuming MainMenuPaging::compare method is correctly implemented for sorting
        toDisplay.sort(MainMenuPaging::compare);

        return toDisplay.stream()
                .map(item -> new GuiItem(item, goToSpyingConfirmationPage()))
                .collect(Collectors.toList());    }

    private Consumer<InventoryClickEvent> goToSpyingConfirmationPage() {
        return inventoryClickEvent -> {
            inventoryClickEvent.setCancelled(true);
            ItemStack current = inventoryClickEvent.getCurrentItem();
            if (current != null && current.hasItemMeta() && current.getItemMeta().hasDisplayName()) {
                String displayName = current.getItemMeta().getDisplayName();
                String[] parts = displayName.split(" ", 2);

                String channelName = "";
                if (parts.length > 1) {
                    channelName = parts[1]; // Get the name of the channel
                }

                // Find the chatroom by name
                Chatroom chatroomToSpy = ChannelUtils.getChatroomWith(channelName);
                if (chatroomToSpy != null) {
                    UUID adminUuid = inventoryClickEvent.getWhoClicked().getUniqueId();
                    if (chatroomToSpy.getSpyers().contains(adminUuid)) {
                        chatroomToSpy.getSpyers().remove(adminUuid);
                        inventoryClickEvent.getWhoClicked().sendMessage(ChatColor.RED + "You have stopped spying on " + channelName + ".");
                    } else {
                        chatroomToSpy.addSpy(adminUuid);
                        inventoryClickEvent.getWhoClicked().sendMessage(ChatColor.GREEN + "You are now spying on " + channelName + ".");
                    }
                }
            }
        };
    }


    // ... other methods ...
}
