package me.perotin.playerchannels.objects.inventory.paging_objects;

import com.github.stefvanschie.inventoryframework.gui.GuiItem;
import com.github.stefvanschie.inventoryframework.gui.type.util.Gui;
import com.github.stefvanschie.inventoryframework.pane.PaginatedPane;
import com.github.stefvanschie.inventoryframework.pane.StaticPane;
import me.perotin.playerchannels.PlayerChannels;
import me.perotin.playerchannels.objects.Chatroom;
import me.perotin.playerchannels.objects.InventoryHelper;
import me.perotin.playerchannels.objects.inventory.actions.ChatroomItemStackAction;
import me.perotin.playerchannels.storage.Pair;
import me.perotin.playerchannels.utils.ItemStackUtils;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;
import java.util.stream.Collectors;

public class AdminDeleteChatroomPager extends PagingMenu {

    private InventoryHelper helper;
    private StaticPane infoPane;

    public AdminDeleteChatroomPager(Player viewer, Gui backMenu) {
        super("Delete a Channel", 6, viewer, backMenu);
        helper = PlayerChannels.getInstance().getHelper();
        helper.setNavigationBar(getMenu(), viewer);
        helper.setSideDecorationSlots(getMenu());
        helper.setPagingNavBar(getMenu());
        setPaginatedPane();
        this.infoPane = new StaticPane(1, 1, 6, 1);
        getMenu().addPane(infoPane);
        getPaginatedPane().populateWithGuiItems(generatePages());

        Pair<ItemStack, Integer> infoItem = InventoryHelper.getItem("admin-menu.delete-chatroom-info", null);
        infoPane.addItem(new GuiItem(infoItem.getFirst()), infoItem.getSecond(), 0);
    }

    @Override
    protected void setPaginatedPane() {
        this.pane = new PaginatedPane(1, 2, 7, 3);
        getMenu().addPane(pane);

    }

    @Override
    protected List<GuiItem> generatePages() {
        List<ItemStack> toDisplay = PlayerChannels.getInstance().getChatrooms().stream()
                .map(Chatroom::getItem)
                .collect(Collectors.toList());
        toDisplay = toDisplay.stream().map(item -> {
            ItemMeta meta = item.getItemMeta();
            List<String> lore = meta.getLore();
            lore.add(ChatColor.GRAY + "Click to " + ChatColor.RED + ChatColor.BOLD + "DELETE");
            meta.setLore(lore);
            item.setItemMeta(meta);
            return item;
        }).collect(Collectors.toList());

        // Assuming MainMenuPaging::compare method is correctly implemented for sorting
        toDisplay.sort(MainMenuPaging::compare);

        List<GuiItem> guiItems = toDisplay.stream()
                .map(item -> new GuiItem(item, ChatroomItemStackAction.clickOnChatroom()))
                .collect(Collectors.toList());

        return guiItems;
    }

}
