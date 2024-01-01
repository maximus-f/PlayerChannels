package me.perotin.playerchannels.objects;

import me.perotin.playerchannels.PlayerChannels;
import me.perotin.playerchannels.storage.files.FileType;
import me.perotin.playerchannels.storage.files.ChannelFile;
import me.perotin.playerchannels.utils.ChannelUtils;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

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
    // Think this method is broken because it is on the same line as $connected$ placeholder
    public static ChatRole getRoleFrom(ItemStack item) {
        // Define a mapping from the custom role names to the enum values
        ChannelFile messages = new ChannelFile(FileType.MESSAGES);
        FileConfiguration config = PlayerChannels.getInstance().getConfig();

        Map<String, ChatRole> roleMap = new HashMap<>();
        roleMap.put(messages.getString("member"), ChatRole.MEMBER);
        roleMap.put(messages.getString("moderator"), ChatRole.MODERATOR);
        roleMap.put(messages.getString("owner"), ChatRole.OWNER);
        roleMap.put(ChannelUtils.colorize(config.getString("server-channel-mod")), ChatRole.MODERATOR);
        roleMap.put(ChannelUtils.colorize(config.getString("server-channel-admin")), ChatRole.OWNER);

        // Get the lore
        String lore = Objects.requireNonNull(item.getItemMeta()).getLore().get(0);

        // Check if the lore contains each role name
        for (Map.Entry<String, ChatRole> entry : roleMap.entrySet()) {
            if (lore.contains(entry.getKey())) {
                // If it does, return the corresponding role
                return entry.getValue();
            }
        }

        // If no role name was found in the lore, throw an exception
        throw new IllegalArgumentException("Role not found in item lore");
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
