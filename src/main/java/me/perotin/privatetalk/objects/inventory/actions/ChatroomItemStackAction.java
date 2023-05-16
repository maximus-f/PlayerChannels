package me.perotin.privatetalk.objects.inventory.actions;

import me.perotin.privatetalk.PrivateTalk;
import me.perotin.privatetalk.objects.Chatroom;
import me.perotin.privatetalk.objects.inventory.paging_objects.ChatroomPager;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

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
            Chatroom clicked = getChatroomWith(clickEvent.getCurrentItem());
            if (clicked != null) {
                Bukkit.getLogger().info("Clicked is not null! " + clicked.getName()+"!" );

            }
            new ChatroomPager(clicked, clicker).show();
        };

    }

    private static Chatroom getChatroomWith(ItemStack item){
        List<Chatroom> chatrooms = PrivateTalk.getInstance().getChatrooms();
        for(Chatroom chat : chatrooms){
            if(chat.getItem().isSimilar(item)){
                Bukkit.getLogger().info("Found chatroom for " + item.getItemMeta().getDisplayName() + ": " + chat.getName());
                return chat;
            }
        }
        Bukkit.getLogger().info("Could not find chatroom for " + item.getItemMeta().getDisplayName()+"!");

        return null;
    }
}
