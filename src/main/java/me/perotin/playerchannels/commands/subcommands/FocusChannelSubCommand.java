package me.perotin.playerchannels.commands.subcommands;

import me.perotin.playerchannels.PlayerChannels;
import me.perotin.playerchannels.objects.Chatroom;
import me.perotin.playerchannels.objects.PlayerChannelUser;
import me.perotin.playerchannels.storage.files.ChannelFile;
import me.perotin.playerchannels.storage.files.FileType;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.stream.Collectors;

public class FocusChannelSubCommand extends SubCommand{

    public FocusChannelSubCommand(String label) {
        super(label);
    }

    @Override
    public void onCommand(Player player, PlayerChannelUser user, String[] args) {

        ChannelFile msg = new ChannelFile(FileType.MESSAGES);
            Chatroom chatroom = PlayerChannels.getInstance().getChatroom(args[0]);
            // set focused chat here
            if (user.getFocusedChatroom() == null || (chatroom != null &&
                    !user.getFocusedChatroom().equals(chatroom))) {
                // Currently not focused on this chatroom, so set them to here
                player.sendMessage(msg.getString("focus-channel-set")
                                .replace("$chatroom$", chatroom.getName()));
                user.setFocusedChatroom(chatroom);

                return;
            } else {

                // Currently focused on this chatroom, so set it to null
                user.setFocusedChatroom(null);
                player.sendMessage(msg.getString("focus-channel-unset"));
                return;
            }


    }


}
