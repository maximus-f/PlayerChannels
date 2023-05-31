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

public class SelectChatroomToInviteMenu extends StaticMenu {
    private StaticPane chatrooms;
    public SelectChatroomToInviteMenu(Player inviter, PrivatePlayer inRoom, PrivatePlayer invited) {
        super(inviter, "Select chatroom to invite to");
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
