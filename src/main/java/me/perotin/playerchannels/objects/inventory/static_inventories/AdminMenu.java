package me.perotin.playerchannels.objects.inventory.static_inventories;

import com.github.stefvanschie.inventoryframework.gui.GuiItem;
import com.github.stefvanschie.inventoryframework.pane.StaticPane;
import me.perotin.playerchannels.PlayerChannels;
import me.perotin.playerchannels.objects.InventoryHelper;
import me.perotin.playerchannels.storage.Pair;
import me.perotin.playerchannels.storage.files.ChannelFile;
import me.perotin.playerchannels.storage.files.FileType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

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
        adminTools.addItem(new GuiItem(stopPlugin.getFirst()), stopPlugin.getSecond(), 0);
        adminTools.addItem(new GuiItem(deleteChat.getFirst()), deleteChat.getSecond(), 0);
        adminTools.addItem(new GuiItem(spyChatroom.getFirst()), spyChatroom.getSecond(), 0);
        adminTools.addItem(new GuiItem(reloadPlugin.getFirst()), reloadPlugin.getSecond(), 1);

    }


}
