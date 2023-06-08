package me.perotin.privatetalk.objects.inventory.static_inventories;

import com.github.stefvanschie.inventoryframework.gui.GuiItem;
import com.github.stefvanschie.inventoryframework.pane.StaticPane;
import me.perotin.privatetalk.objects.ChatRole;
import me.perotin.privatetalk.objects.Chatroom;
import me.perotin.privatetalk.objects.PrivatePlayer;
import me.perotin.privatetalk.utils.PrivateUtils;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * Class for when a player invites another player and is selecting which of their chatrooms to invite them too
 * since multiple possible candidates for a chatroom can exist
 */
public class SelectChatroomToInviteMenu extends StaticMenu {
    private StaticPane chatrooms;
    public SelectChatroomToInviteMenu(Player inviter, PrivatePlayer inRoom, PrivatePlayer invited) {
        super(inviter, PrivateUtils.getMessageString("select-chatroom-to-invite-menu-title"));
        chatrooms = new StaticPane(0, 0, 6, 7);
        // TODO Doing this is a prototype manner to see if done, needs to be refactored
        int x = 0, y = 0;
        for (Chatroom chat : inRoom.getChatrooms()){
            ChatRole role = chat.getRole(inviter.getUniqueId());
            if (role == ChatRole.MODERATOR || role == ChatRole.OWNER) {
                chatrooms.addItem(new GuiItem(chat.getItem(), inviteTo(invited, chat)), x, y);
                if (x == 9) {
                    x = 0;
                    y++;
                } else {
                    x++;
                }
            }

        }
        getMenu().addPane(chatrooms);

    }

    /**
     * Inventory event where player chooses which chatroom to invite a player to
     * Need to check if they already have an invite from them to avoid spamming with invites
     * as well as potentially checking if player is banend before trying to invite them
     * @param invited
     * @param chatroom
     * @return event
     */
    private Consumer<InventoryClickEvent> inviteTo(PrivatePlayer invited, Chatroom chatroom){
        return event -> {
            event.setCancelled(true);
            // Check if already have an invite
            invited.addInvite(chatroom);
            PrivateUtils.sendMenuMessage("You have invited " + invited.getName() + " to " + chatroom.getName(),
                    (Player) event.getWhoClicked(), null);

        };

    }
}
