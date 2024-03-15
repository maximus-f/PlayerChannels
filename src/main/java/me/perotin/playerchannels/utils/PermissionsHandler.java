package me.perotin.playerchannels.utils;

import me.perotin.playerchannels.PlayerChannels;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.permissions.PermissionAttachmentInfo;

public class PermissionsHandler {

    public int getMaxChannels(Player player) {
        int maxChannels = -1; // Default to 0 if no permission is set
        for (PermissionAttachmentInfo perm : player.getEffectivePermissions()) {

            if (perm.getPermission().startsWith("playerchannels.create.")) {
                try {
                    // Extract the numeric part of the permission
                    maxChannels = Integer.parseInt(perm.getPermission().substring("playerchannels.create.".length()));
                    return maxChannels;
                    // Update maxChannels if this permission grants more than the previous ones
                } catch (NumberFormatException e) {
                    // Handle the case where the permission is not properly formatted
                }
            }
        }

        return PlayerChannels.getInstance().getDefaultChannelLimit();    }




}
