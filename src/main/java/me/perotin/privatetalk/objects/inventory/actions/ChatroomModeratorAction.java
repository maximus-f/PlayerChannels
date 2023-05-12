package me.perotin.privatetalk.objects.inventory.actions;

import me.perotin.privatetalk.objects.Chatroom;
import me.perotin.privatetalk.objects.PrivatePlayer;
import org.bukkit.event.inventory.InventoryClickEvent;

import java.util.function.Consumer;

public class ChatroomModeratorAction {

    /**
     * @return inventory event for when a moderator kicks a member
     */
    public static Consumer<InventoryClickEvent> kickMember(Chatroom chatroom, PrivatePlayer player){
        return inventoryClickEvent -> {
            inventoryClickEvent.setCancelled(true);
            chatroom.removeMember(player.getUuid());
            player.leaveChatroom(chatroom);

        };
    }

    /**
     * @param chat to mute or unmute in
     * @param player
     * @return inventory event for when a moderator mutes a member in a chatroom
     */

    public static Consumer<InventoryClickEvent> muteMember(Chatroom chat, PrivatePlayer player){
        return inventoryClickEvent -> {
            inventoryClickEvent.setCancelled(true);
            if (!chat.isMuted(player.getUuid())) {
                chat.mute(player.getUuid());
            } else {
                // unmute
                chat.unmute(player.getUuid());
            }

        };
    }

    /**
     *
     * @param chat
     * @param player
     * @return inventory event for when a moderator bans a member
     */
    public static Consumer<InventoryClickEvent> banMember(Chatroom chat, PrivatePlayer player){
        return inventoryClickEvent -> {
            inventoryClickEvent.setCancelled(true);
            chat.ban(player.getUuid());
            player.leaveChatroom(chat);

        };
    }
}
