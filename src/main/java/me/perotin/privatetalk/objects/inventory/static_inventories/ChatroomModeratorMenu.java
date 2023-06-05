package me.perotin.privatetalk.objects.inventory.static_inventories;

import com.github.stefvanschie.inventoryframework.gui.GuiItem;
import com.github.stefvanschie.inventoryframework.gui.type.ChestGui;
import com.github.stefvanschie.inventoryframework.pane.StaticPane;
import me.perotin.privatetalk.PrivateTalk;
import me.perotin.privatetalk.objects.ChatRole;
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

    private Player punisher;
    private StaticPane modItems;
    private InventoryHelper helper;


    public ChatroomModeratorMenu(Player moderator, PrivatePlayer toPunish, Chatroom chatroom) {
        super(moderator, "Moderator Actions ");
        this.toPunish = toPunish;
        this.chatroom = chatroom;
        this.modItems = new StaticPane(2, 2, 5, 2);
        this.helper = PrivateTalk.getInstance().getHelper();
        this.punisher = moderator;

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

        Pair<ItemStack, Integer> promoteMember = InventoryHelper.getItem("chatroom-mod.promote-member", null);
        Pair<ItemStack, Integer> demoteModerator = InventoryHelper.getItem("chatroom-mod.demote-moderator", null);
        Pair<ItemStack, Integer> promoteModerator = InventoryHelper.getItem("chatroom-mod.promote-moderator", null);


        // Check if person is owner
        if (getPunisher().getUniqueId().equals(chatroom.getOwner())) {
            // owner in menu so display options for promotion/demotion etc.
            ChatRole role = chatroom.getRole(toPunish.getUuid());
            switch (role){
                case MEMBER:
                    // Member so show option to promote
                    modItems.addItem(new GuiItem(promoteMember.getFirst(), ChatroomModeratorAction.promoteMember(chatroom, toPunish)), promoteMember.getSecond(), 1);
                    break;
                case MODERATOR:
                    // Moderator so show option to promote to owner or demote to member
                    modItems.addItem(new GuiItem(promoteModerator.getFirst()), promoteModerator.getSecond(), 1);
                    modItems.addItem(new GuiItem(demoteModerator.getFirst(),
                            ChatroomModeratorAction.demoteModerator(chatroom, toPunish)), demoteModerator.getSecond(), 1);
                    break;
                case OWNER:
                    break;
            }
        }
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

    public Player getPunisher() {
        return punisher;
    }
}
