package me.perotin.privatetalk.objects.inventory.actions;

import me.perotin.privatetalk.PrivateTalk;
import me.perotin.privatetalk.objects.Chatroom;
import me.perotin.privatetalk.objects.inventory.paging_objects.ChatroomPager;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;

import java.util.List;
import java.util.function.Consumer;

/* Created by Perotin on 11/29/19 */
public class ChatroomItemStackAction {

    /**
     * Action for clicking on a chatroom and it bringing you to its menu
     */
    public static Consumer<InventoryClickEvent> clickOnChatroom() {
        //Check to see if this is the proper way of handling ChatroomPager and if we need to store the instance anywhere
        return clickEvent -> {
            Player clicker = (Player) clickEvent.getWhoClicked();
            clickEvent.setCancelled(true);
            // Create the namespaced key (same as before)
            NamespacedKey key = new NamespacedKey(PrivateTalk.getInstance(), "chatroomName");

            // Get the custom data
            String chatroomName = clickEvent.getCurrentItem().getItemMeta().getPersistentDataContainer().get(key, PersistentDataType.STRING);

            Chatroom clicked = PrivateTalk.getInstance().getChatroom(chatroomName);
//            Chatroom clicked = getChatroomWith(clickEvent.getCurrentItem());
            if (clicked != null) {
                Bukkit.getLogger().info("Clicked is not null! " + clicked.getName()+"!" );

            }
            new ChatroomPager(clicked, clicker).show();
        };

    }


}
