package me.perotin.playerchannels.objects.inventory.static_inventories;

import com.github.stefvanschie.inventoryframework.gui.GuiItem;
import com.github.stefvanschie.inventoryframework.pane.StaticPane;
import me.perotin.playerchannels.PlayerChannels;
import me.perotin.playerchannels.objects.InventoryHelper;
import me.perotin.playerchannels.storage.Pair;
import me.perotin.playerchannels.utils.ChannelUtils;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

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


        ItemStack confirmItem= ChannelUtils.replacePlaceHolderInLore(confirm.getFirst(), "$name$", title.split(" ")[1], 0);
        yesOrNo.addItem(new GuiItem(confirmItem), confirm.getSecond(), 0);
        yesOrNo.addItem(new GuiItem(cancel.getFirst()), cancel.getSecond(), 0);
        getMenu().addPane(yesOrNo);

    }




}
