package me.perotin.playerchannels.objects.inventory.actions;

import me.perotin.playerchannels.objects.Chatroom;
import me.perotin.playerchannels.objects.PlayerChannelUser;
import me.perotin.playerchannels.objects.inventory.paging_objects.ChatroomPager;
import me.perotin.playerchannels.objects.inventory.static_inventories.ChatroomModeratorMenu;
import me.perotin.playerchannels.utils.ChannelUtils;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;

import java.util.function.Consumer;

//TODO: menu messages should be configurable
public class ChatroomModeratorAction {


    /**
     * Inventory event for when owner promotes a member to moderator
     * @param chatroom
     * @param player
     * @return
     */
    public static Consumer<InventoryClickEvent> promoteMember(Chatroom chatroom, PlayerChannelUser player) {
        return event -> {
            event.setCancelled(true);
            chatroom.promoteMemberToModerator(player.getUuid());
            Player mod = (Player) event.getWhoClicked();

            // tell person they've been promoted and show new mod menu
            ChannelUtils.sendMenuMessage("You have promoted " + player.getName() +" to mod!", mod,
                    new ChatroomPager(chatroom, mod));

        };
    }

    /**
     * Inventory event for when owner demotes a moderator to member
     * @param chatroom
     * @param player
     * @return
     */
    public static Consumer<InventoryClickEvent> demoteModerator(Chatroom chatroom, PlayerChannelUser player) {
        return event -> {
            event.setCancelled(true);
            chatroom.demoteModeratorToMember(player.getUuid());
            Player mod = (Player) event.getWhoClicked();

            // tell person they've been promoted and show new mod menu
            ChannelUtils.sendMenuMessage("You have demoted " + player.getName() +" from mod!", mod,
                    new ChatroomPager(chatroom, mod));

        };
    }

    /**
     * Inventory event for when owner promotes a moderator to owner
     * May do another thing here to confirm promotion to owner since it is a decisive action
     * @param chatroom
     * @param player
     * @return
     */
    public static Consumer<InventoryClickEvent> promoteModerator(Chatroom chatroom, PlayerChannelUser player) {
        return event -> {
            event.setCancelled(true);
            Player owner = (Player) event.getWhoClicked();

            chatroom.promoteModeratorToOwner(player.getUuid(), owner.getUniqueId());

            // tell person they've been promoted and show new mod menu
            ChannelUtils.sendMenuMessage("You have promoted " + player.getName() +" to owner!", owner,
                    new ChatroomPager(chatroom, owner));
        };
    }
    /**
     * @return inventory event for when a moderator kicks a member
     */
    public static Consumer<InventoryClickEvent> kickMember(Chatroom chatroom, PlayerChannelUser player){
        return inventoryClickEvent -> {
            inventoryClickEvent.setCancelled(true);
            Player mod = (Player) inventoryClickEvent.getWhoClicked();
            chatroom.removeMember(player.getUuid());
            player.leaveChatroom(chatroom);
            ChannelUtils.sendMenuMessage("You have kicked " + player.getName() +"!", mod,
                    new ChatroomPager(chatroom, mod));

        };
    }

    /**
     * @param chat to mute or unmute in
     * @param player
     * @return inventory event for when a moderator mutes a member in a chatroom
     */

    public static Consumer<InventoryClickEvent> muteMember(Chatroom chat, PlayerChannelUser player){
        return inventoryClickEvent -> {
            inventoryClickEvent.setCancelled(true);
            Player mod = (Player) inventoryClickEvent.getWhoClicked();
            if (!chat.isMuted(player.getUuid())) {
                chat.mute(player.getUuid());
                    ChannelUtils.sendMenuMessage("You have muted " + player.getName() +"!", mod,
                         new ChatroomModeratorMenu(mod, player, chat));
            } else {
                // unmute
                chat.unmute(player.getUuid());
                ChannelUtils.sendMenuMessage("You have unmuted " + player.getName() +"!", mod,
                 new ChatroomModeratorMenu(mod, player, chat));
            }

        };
    }

    /**
     *
     * @param chat
     * @param player
     * @return inventory event for when a moderator bans a member
     */
    public static Consumer<InventoryClickEvent> banMember(Chatroom chat, PlayerChannelUser player){
        return inventoryClickEvent -> {
            inventoryClickEvent.setCancelled(true);
            Player mod = (Player) inventoryClickEvent.getWhoClicked();

            chat.ban(player.getUuid());
            player.leaveChatroom(chat);
            ChannelUtils.sendMenuMessage("You have kicked " + player.getName() +"!", mod,
                    new ChatroomPager(chat, mod));

        };
    }
}
