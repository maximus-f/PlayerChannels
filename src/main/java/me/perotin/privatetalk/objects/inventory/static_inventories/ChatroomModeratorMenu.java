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
import org.bukkit.inventory.meta.ItemMeta;

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
        Pair<ItemStack, Integer> unmutePair = InventoryHelper.getItem("chatroom-mod.unmute-item", null);

        Pair<ItemStack, Integer> kickPair = InventoryHelper.getItem("chatroom-mod.kick-item", null);
        Pair<ItemStack, Integer> banPair = InventoryHelper.getItem("chatroom-mod.ban-item", null);

       ItemStack mute = setNamesForItems(mutePair.getFirst());
        ItemStack unmute = setNamesForItems(unmutePair.getFirst());

        ItemStack kick = setNamesForItems(kickPair.getFirst());
        ItemStack ban = setNamesForItems(banPair.getFirst());

        if (!chatroom.isMuted(toPunish.getUuid())) {
            modItems.addItem(new GuiItem(mute, ChatroomModeratorAction.muteMember(chatroom, toPunish)), mutePair.getSecond(), 0);
        } else {
            modItems.addItem(new GuiItem(unmute, ChatroomModeratorAction.muteMember(chatroom, toPunish)), unmutePair.getSecond(), 0);

        }
        modItems.addItem(new GuiItem(kick, ChatroomModeratorAction.kickMember(chatroom, toPunish)), kickPair.getSecond(), 0);
        modItems.addItem(new GuiItem(ban, ChatroomModeratorAction.banMember(chatroom, toPunish)), banPair.getSecond(), 0);

    }
    private ItemStack setNamesForItems(ItemStack addName) {
        ItemMeta meta = addName.getItemMeta();
        meta.setDisplayName(meta.getDisplayName() + "" + toPunish.getName());
        addName.setItemMeta(meta);
        return addName;
    }


}
