package me.perotin.playerchannels.objects.inventory.paging_objects;

import com.github.stefvanschie.inventoryframework.gui.GuiItem;
import com.github.stefvanschie.inventoryframework.gui.type.util.Gui;
import me.perotin.playerchannels.PlayerChannels;
import me.perotin.playerchannels.objects.ChatRole;
import me.perotin.playerchannels.objects.Chatroom;
import me.perotin.playerchannels.objects.InventoryHelper;
import me.perotin.playerchannels.objects.PlayerChannelUser;
import me.perotin.playerchannels.storage.Pair;
import me.perotin.playerchannels.storage.files.FileType;
import me.perotin.playerchannels.storage.files.ChannelFile;
import me.perotin.playerchannels.utils.ChannelUtils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * Class for representing menu where player views their invites from other chatrooms
 */
public class ChatroomInvitationListPager extends PagingMenu {


    private PlayerChannelUser player; // player viewing their own invites
    private InventoryHelper helper;
    public ChatroomInvitationListPager(PlayerChannelUser player, Player viewer, Gui backMenu) {
        super(new ChannelFile(FileType.MESSAGES).getString("invitations-menu-title"), 6, viewer, backMenu);
        this.player = player;
        this.helper = PlayerChannels.getInstance().getHelper();
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

            ChannelUtils.appendToDisplayName(invite, invited.getName());
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
    private Consumer<InventoryClickEvent> clickOnInvite(Chatroom chatroom, PlayerChannelUser clicker){
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
                  ChannelUtils.sendMenuMessage("You are banned!", Bukkit.getPlayer(clicker.getUuid()), null);
                  return;
              }
              clicker.addChatroom(chatroom);
              chatroom.addMember(new Pair<>(clicker.getUuid(), ChatRole.MEMBER));
              new ChatroomPager(chatroom, Bukkit.getPlayer(clicker.getUuid())).show();

          }
        };
    }
}
