package me.perotin.playerchannels.objects.inventory.actions;

import me.perotin.playerchannels.PlayerChannels;
import me.perotin.playerchannels.objects.Chatroom;
import me.perotin.playerchannels.objects.inventory.paging_objects.ChatroomPager;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.persistence.PersistentDataType;

import java.util.function.Consumer;

/* Created by Perotin on 11/29/19 */
public class ChatroomItemStackAction {

    /**
     * Action for clicking on a chatroom and it bringing you to its menu
     * NOTE: Using PersistentDataContainer was introduced in 1.14,
     *  may want to do something to support earlier versions
     */
    public static Consumer<InventoryClickEvent> clickOnChatroom() {
        //Check to see if this is the proper way of handling ChatroomPager and if we need to store the instance anywhere
        return clickEvent -> {
            Player clicker = (Player) clickEvent.getWhoClicked();
            clickEvent.setCancelled(true);
            // Create the namespaced key (same as before)
            NamespacedKey key = new NamespacedKey(PlayerChannels.getInstance(), "chatroomName");

            // Get the custom data
            String chatroomName = clickEvent.getCurrentItem().getItemMeta().getPersistentDataContainer().get(key, PersistentDataType.STRING);

            Chatroom clicked = PlayerChannels.getInstance().getChatroom(chatroomName);
//            Chatroom clicked = getChatroomWith(clickEvent.getCurrentItem());
            if (clicked != null) {
                Bukkit.getLogger().info("Clicked is not null! " + clicked.getName()+"!" );

            }
            new ChatroomPager(clicked, clicker).show();
        };

    }


}
