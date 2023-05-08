package me.perotin.privatetalk.objects;

import org.bukkit.ChatColor;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * @Enum for types of roles in PT.
 */
public enum ChatRole implements ConfigurationSerializable {
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

    @NotNull
    @Override
    public Map<String, Object> serialize() {
        HashMap<String, Object> result = new HashMap<>();
        result.put("numVal", numVal);
        return result;
    }

    public static ChatRole deserialize(Map<String, Object> args) {
        switch ((int) args.get("numVal")) {
            case 1:
                return ChatRole.MEMBER;
            case 2:
                return ChatRole.MODERATOR;
            case 3:
                return ChatRole.OWNER;
            default:
                return null;
        }
    }

    public static ChatRole getRole(int numVal) {
        switch (numVal) {
            case 1:
                return ChatRole.MEMBER;
            case 2:
                return ChatRole.MODERATOR;
            case 3:
                return ChatRole.OWNER;
            default:
                return null;
        }
    }

}
