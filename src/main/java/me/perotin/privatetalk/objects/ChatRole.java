package me.perotin.privatetalk.objects;

import org.bukkit.ChatColor;
import org.bukkit.inventory.ItemStack;

import java.util.Objects;

/**
 * @Enum for types of roles in PT.
 */
public enum ChatRole {
    OWNER(3), MODERATOR(2), MEMBER(1);

    private int numVal;

    ChatRole(int numVal) {
        this.numVal = numVal;
    }

    public int getValue() {
        return numVal;
    }

    /**
     * Prone to throw errors if item passed does not meet critera
     * @param item to find chat role of
     * @return chat role of itemstack
     */
    public static ChatRole getRoleFrom(ItemStack item){
        return ChatRole.valueOf(ChatColor.stripColor(Objects.requireNonNull(item.getItemMeta()).getLore().get(0)));
    }
}
