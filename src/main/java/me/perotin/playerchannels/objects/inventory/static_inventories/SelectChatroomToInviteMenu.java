package me.perotin.playerchannels.objects.inventory.static_inventories;

import com.github.stefvanschie.inventoryframework.gui.GuiItem;
import com.github.stefvanschie.inventoryframework.gui.type.util.Gui;
import me.perotin.playerchannels.PlayerChannels;
import me.perotin.playerchannels.objects.InventoryHelper;
import me.perotin.playerchannels.objects.inventory.paging_objects.PagingMenu;
import me.perotin.playerchannels.objects.ChatRole;
import me.perotin.playerchannels.objects.Chatroom;
import me.perotin.playerchannels.objects.PlayerChannelUser;
import me.perotin.playerchannels.storage.files.ChannelFile;
import me.perotin.playerchannels.storage.files.FileType;
import me.perotin.playerchannels.utils.ChannelUtils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * Class for when a player invites another player and is selecting which of their chatrooms to invite them too
 * since multiple possible candidates for a chatroom can exist
 */
public class SelectChatroomToInviteMenu extends PagingMenu {
    private final PlayerChannelUser invited;

    public SelectChatroomToInviteMenu(Player inviter, PlayerChannelUser invited, Gui backMenu) {
        super(ChannelUtils.getMessageString("select-chatroom-to-invite-menu-title"), 6, inviter, backMenu);
        this.invited = invited;
        InventoryHelper helper = PlayerChannels.getInstance().getHelper();
        helper.setNavigationBar(getMenu(), inviter);
        helper.setSideDecorationSlots(getMenu());
        getPaginatedPane().populateWithGuiItems(generatePages());
    }

    @Override
    protected List<GuiItem> generatePages() {
        List<GuiItem> items = new ArrayList<>();
        PlayerChannelUser inviter = PlayerChannelUser.getPlayer(getViewer().getUniqueId());
        for (Chatroom chat : inviter.getChatrooms()) {
            ChatRole role = chat.getRole(inviter.getUuid());
            if (role == ChatRole.MODERATOR || role == ChatRole.OWNER) {
                items.add(new GuiItem(chat.getItem(), inviteTo(invited, chat)));
            }
        }
        return items;
    }

    /**
     * Inventory event where player chooses which chatroom to invite a player to
     * Need to check if they already have an invite from them to avoid spamming with invites
     * as well as potentially checking if player is banend before trying to invite them
     * @param invited
     * @param chatroom
     * @return event
     */
    private Consumer<InventoryClickEvent> inviteTo(PlayerChannelUser invited, Chatroom chatroom){
        return event -> {
            event.setCancelled(true);
            // Check if already have an invite
            Player online = Bukkit.getPlayer(invited.getUuid());
            ChannelFile messages = new ChannelFile(FileType.MESSAGES);

            if (!invited.hasPendingInviteFrom(chatroom)) {
                invited.addInvite(chatroom);
                ChannelUtils.sendMenuMessage("You have invited " + invited.getName() + " to " + chatroom.getName(),
                        (Player) event.getWhoClicked(), null);
                if (online != null) {
                    online.sendMessage(messages.getString("received-invite")
                            .replace("$chatroom$", chatroom.getName()));
                }
            } else {
                ChannelUtils.sendMenuMessage( invited.getName() + " is already invited!",
                        (Player) event.getWhoClicked(), null);
            }



        };

    }
}
