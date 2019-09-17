package me.perotin.privatetalk.utils;

import me.perotin.privatetalk.objects.ChatRole;
import org.bukkit.inventory.ItemStack;

import java.util.Comparator;

/* Created by Perotin on 8/22/19 */

/**
 * Compare the Enum ranks of chat roles
 */
public class ChatRoleComparator implements Comparator<ItemStack> {

    /**
     * Compares two item stacks by getting the chat roles from their lores
     * @param o4
     * @param o5
     * @return int
     */
    @Override
    public int compare(ItemStack o4, ItemStack o5) {
        ChatRole o1 = ChatRole.getRoleFrom(o4);
        ChatRole o2 = ChatRole.getRoleFrom(o5);
       if(o1.getValue() == o2.getValue()) return 0;
       if(o1.getValue() > o2.getValue()) return 1;
       else return -1;
    }
}
