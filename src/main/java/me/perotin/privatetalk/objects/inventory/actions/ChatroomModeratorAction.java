package me.perotin.privatetalk.objects.inventory.actions;

import me.perotin.privatetalk.objects.Chatroom;
import me.perotin.privatetalk.objects.PrivatePlayer;
import me.perotin.privatetalk.objects.inventory.paging_objects.ChatroomPager;
import me.perotin.privatetalk.objects.inventory.static_inventories.ChatroomModeratorMenu;
import me.perotin.privatetalk.utils.PrivateUtils;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.InventoryView;

import java.util.function.Consumer;

public class ChatroomModeratorAction {

    /**
     * @return inventory event for when a moderator kicks a member
     */
    public static Consumer<InventoryClickEvent> kickMember(Chatroom chatroom, PrivatePlayer player){
        return inventoryClickEvent -> {
            inventoryClickEvent.setCancelled(true);
            Player mod = (Player) inventoryClickEvent.getWhoClicked();
            chatroom.removeMember(player.getUuid());
            player.leaveChatroom(chatroom);
            PrivateUtils.sendMenuMessage("You have kicked " + player.getName() +"!", mod,
                    new ChatroomPager(chatroom, mod));

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
            Player mod = (Player) inventoryClickEvent.getWhoClicked();
            if (!chat.isMuted(player.getUuid())) {
                chat.mute(player.getUuid());
                    PrivateUtils.sendMenuMessage("You have muted " + player.getName() +"!", mod,
                         new ChatroomModeratorMenu(mod, player, chat));
            } else {
                // unmute
                chat.unmute(player.getUuid());
                PrivateUtils.sendMenuMessage("You have unmuted " + player.getName() +"!", mod,
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
    public static Consumer<InventoryClickEvent> banMember(Chatroom chat, PrivatePlayer player){
        return inventoryClickEvent -> {
            inventoryClickEvent.setCancelled(true);
            Player mod = (Player) inventoryClickEvent.getWhoClicked();

            chat.ban(player.getUuid());
            player.leaveChatroom(chat);
            PrivateUtils.sendMenuMessage("You have kicked " + player.getName() +"!", mod,
                    new ChatroomPager(chat, mod));

        };
    }
}
