package me.perotin.privatetalk.objects.inventory.paging_objects;

import me.perotin.privatetalk.objects.inventory.PagingMenu;
import me.perotin.privatetalk.objects.inventory.PrivateInventory;
import org.bukkit.inventory.Inventory;

/* Created by Perotin on 8/17/19 */

/**
 * Paging object for all chatroom objects
 */
public class ChatroomPager extends PagingMenu {


    public ChatroomPager(String identifier){
        super(identifier);
    }



    public PrivateInventory getBlankInventory() {
        return null;
    }
}
