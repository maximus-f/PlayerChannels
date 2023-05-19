package me.perotin.privatetalk.objects.inventory.actions;

import me.perotin.privatetalk.events.chat_events.StatusInputEvent;
import me.perotin.privatetalk.objects.PrivatePlayer;
import me.perotin.privatetalk.objects.inventory.static_inventories.PlayerProfileMenu;
import me.perotin.privatetalk.objects.inventory.static_inventories.SelectChatroomToInviteMenu;
import me.perotin.privatetalk.utils.PrivateUtils;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;

import java.util.function.Consumer;

/**
 * Class to handle actions that occur when a player is viewing their own profile page
 * or another player is viewing them
 */
public class PlayerProfileAction {


    public static Consumer<InventoryClickEvent> clickStatus(PrivatePlayer player){
        return event -> {
            Player clicker = (Player) event.getWhoClicked();
            event.setCancelled(true);
            if (clicker.getName().equals(player.getName())) {
                // Viewing themselves
                // Need to put them in a set and check for a chat event, could maybe do an anvil here however
                // TODO look into maybe using anvil GUI's for user input, however, looks overly
                // complicated for the scope that I am looking for. Going native route first
                StatusInputEvent.enteringStatus.add(clicker);
                clicker.closeInventory();
                // send message

            }
        };
    }

    public static Consumer<InventoryClickEvent> toggleInviteStatus(Player viewer, PrivatePlayer player) {
       return event -> {
           event.setCancelled(true);
           player.setAcceptingInvites(!player.isAcceptingInvites());
           new PlayerProfileMenu(viewer, player, PrivateUtils.getMainMenu(viewer)).show();
         };
       }

    public static Consumer<InventoryClickEvent> invitePlayerToChatroom(Player inviter, PrivatePlayer invited) {
        return event -> {
            event.setCancelled(true);
            PrivatePlayer inRoom = PrivatePlayer.getPlayer(inviter.getUniqueId());
            // Need to check if in valid chatroom to invite, and if so,
            // check how many valid chatroom they are in that they can invite to
            // Only moderators and owners can invite to a chatroom. Just show menu of options
            // that they have of chatrooms to select from no matter the circumstances
            new SelectChatroomToInviteMenu(inviter, inRoom, invited).show(inviter);
        };
    }
}
