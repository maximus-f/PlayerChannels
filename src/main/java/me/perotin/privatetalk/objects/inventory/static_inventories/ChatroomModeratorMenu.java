package me.perotin.privatetalk.objects.inventory.static_inventories;

import com.github.stefvanschie.inventoryframework.gui.GuiItem;
import com.github.stefvanschie.inventoryframework.gui.type.ChestGui;
import com.github.stefvanschie.inventoryframework.pane.StaticPane;
import me.perotin.privatetalk.PrivateTalk;
import me.perotin.privatetalk.objects.Chatroom;
import me.perotin.privatetalk.objects.InventoryHelper;
import me.perotin.privatetalk.objects.PrivatePlayer;
import me.perotin.privatetalk.objects.inventory.actions.ChatroomModeratorAction;
import me.perotin.privatetalk.storage.Pair;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

/**
 * Class to handle menu for when a moderator / owner is doing a punishment
 */
public class ChatroomModeratorMenu extends StaticMenu {

    private PrivatePlayer toPunish; // Player that is being punished in the menu
    private Chatroom chatroom; // Chatroom that the punishment is being dealt within

    private StaticPane modItems;
    private InventoryHelper helper;


    public ChatroomModeratorMenu(Player moderator, PrivatePlayer toPunish, Chatroom chatroom) {
        super(moderator, "Moderator Actions ");
        this.toPunish = toPunish;
        this.chatroom = chatroom;
        this.modItems = new StaticPane(2, 2, 5, 1);
        this.helper = PrivateTalk.getInstance().getHelper();

        setDecorations();
        setModItems();
        getMenu().addPane(modItems);

    }

    private void setDecorations() {
        helper.setSideDecorationSlots(getMenu());
        helper.setNavigationBar(getMenu(), getViewer());
        helper.setPagingNavBar(getMenu());
    }

    private void setModItems(){
        Pair<ItemStack, Integer> mutePair = InventoryHelper.getItem("chatroom-mod.mute-item", null);
        Pair<ItemStack, Integer> kickPair = InventoryHelper.getItem("chatroom-mod.kick-item", null);
        Pair<ItemStack, Integer> banPair = InventoryHelper.getItem("chatroom-mod.ban-item", null);

        modItems.addItem(new GuiItem(mutePair.getFirst(), ChatroomModeratorAction.kickMember()), mutePair.getSecond(), 0);
        modItems.addItem(new GuiItem(kickPair.getFirst(), ChatroomModeratorAction.kickMember()), kickPair.getSecond(), 0);
        modItems.addItem(new GuiItem(banPair.getFirst(), ChatroomModeratorAction.kickMember()), banPair.getSecond(), 0);

    }


}
