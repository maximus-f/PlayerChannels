package me.perotin.privatetalk.objects.inventory.paging_objects;

import com.github.stefvanschie.inventoryframework.gui.GuiItem;
import com.github.stefvanschie.inventoryframework.gui.type.util.Gui;
import me.perotin.privatetalk.PrivateTalk;
import me.perotin.privatetalk.objects.ChatRole;
import me.perotin.privatetalk.objects.Chatroom;
import me.perotin.privatetalk.objects.InventoryHelper;
import me.perotin.privatetalk.objects.PrivatePlayer;
import me.perotin.privatetalk.storage.Pair;
import me.perotin.privatetalk.utils.PrivateUtils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * Class for representing menu where player views their invites from other chatrooms
 */
public class ChatroomInvitationListPager extends PagingMenu {


    private PrivatePlayer player; // player viewing their own invites
    private InventoryHelper helper;
    public ChatroomInvitationListPager(PrivatePlayer player, Player viewer, Gui backMenu) {
        super("Your invitations", 6, viewer, backMenu);
        this.player = player;
        this.helper = PrivateTalk.getInstance().getHelper();
        this.helper.setNavigationBar(getMenu(), viewer);
        this.helper.setPagingNavBar(getMenu());
        this.helper.setSideDecorationSlots(getMenu());
        getPaginatedPane().populateWithGuiItems(generatePages());


    }

    @Override
    protected List<GuiItem> generatePages() {
        List<GuiItem> items = new ArrayList<>();
        for (Chatroom invited : player.getInvites()) {
            // Build chatroom invite item for each
            ItemStack invite = InventoryHelper.getItem("invitations-menu.invite-item", null).getFirst();

            PrivateUtils.appendToDisplayName(invite, invited.getName());
            items.add(new GuiItem(invite, clickOnInvite(invited, player)));
        }
        return items;
    }

    /**
     * Inventory event for when a player clicks on an invite in their invitation menu from
     * navigation bar
     * @param chatroom that invite is originated from
     * @param clicker
     * @return event
     */
    private Consumer<InventoryClickEvent> clickOnInvite(Chatroom chatroom, PrivatePlayer clicker){
        return event -> {
            event.setCancelled(true);
          if (event.getClick() == ClickType.LEFT){
              // reject
              clicker.getInvites().remove(chatroom);
              new ChatroomInvitationListPager(clicker, getViewer(), getBackMenu()).show();
          }  else if (event.getClick() == ClickType.RIGHT) {
              // accept
              clicker.getInvites().remove(chatroom);
              // need to look for code when joins to make sure it is right
              // TODO make sure not banned
              if (chatroom.isBanned(clicker.getUuid())){
                  PrivateUtils.sendMenuMessage("You are banned!", Bukkit.getPlayer(clicker.getUuid()), null);
                  return;
              }
              clicker.addChatroom(chatroom);
              chatroom.addMember(new Pair<>(clicker.getUuid(), ChatRole.MEMBER));
              new ChatroomPager(chatroom, Bukkit.getPlayer(clicker.getUuid())).show();

          }
        };
    }
}
