package me.perotin.playerchannels.commands.subcommands;

import me.perotin.playerchannels.PlayerChannels;
import me.perotin.playerchannels.objects.Chatroom;
import me.perotin.playerchannels.objects.PlayerChannelUser;
import me.perotin.playerchannels.storage.files.ChannelFile;
import me.perotin.playerchannels.storage.files.FileType;
import me.perotin.playerchannels.utils.ChannelUtils;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

/*
 Seems to be working relatively ok at first glance, sends wrong message when doing /pc leave and in more than 1 channel

 Also an issue if you do /pc leave tt tt do need to check args limit
 */
public class LeaveSubCommand extends SubCommand {


    public LeaveSubCommand(String label) {
        super(label);
    }

    /**
     * Can probably be optimized due to the static leaveChatroom handling owner case
     * @param player
     * @param user
     * @param args
     */
    @Override
    public void onCommand(Player player, PlayerChannelUser user, String[] args) {

        ChannelFile messages = new ChannelFile(FileType.MESSAGES);

        if (user.getChatrooms().isEmpty()) {
            // Send message that they are not in any channels
            messages.sendConfigMsg(player, "focus-channel-no-channels");
            return;
        }
        if (args.length == 1) {
            // Just type /channels leave
            if (user.getChatrooms().size() == 1) {
                Chatroom channel = user.getChatrooms().get(0);
                if (channel.getOwner().equals(user.getUuid()) && !channel.isServerOwned()) {
                    // Go through owner leaving process, bit tedious
                    ChannelUtils.leaveChatroom(user, channel, false);
                } else {
                    // Allow them to leave
                    ChannelUtils.leaveChatroom(user, channel, false);

                }
            } else {
                // In more than one chatroom, tell them to specify which channel to leave
                messages.sendConfigMsg(player, "focus-channel-no-channels");

            }
        } else if (args.length == 2) {
            Chatroom channel = PlayerChannels.getInstance().getChatroom(args[1]);
            if (channel == null) {
                // Send message saying that it is null
                player.sendMessage(ChatColor.translateAlternateColorCodes('&', PlayerChannels.getInstance().getConfig().getString("join-subcommand-not-found")));

                return;
            }
            if (!channel.isInChatroom(player.getUniqueId())) {
                // Send message that they are not in the channel
                messages.sendConfigMsg(player, "listen-not-in-channel");
                return;
            }
            if (channel.getOwner().equals(user.getUuid()) && !channel.isServerOwned()) {
                // Go through owner leaving process, bit tedious
                ChannelUtils.leaveChatroom(user, channel, false);

            } else {
                // Allow them to leave
                ChannelUtils.leaveChatroom(user, channel, false);

            }


        } else {
            // Send usage message, may be irrelevant however since impossible to access from main command class
        }
    }
}
