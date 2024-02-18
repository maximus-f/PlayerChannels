package me.perotin.playerchannels.commands.subcommands;

import me.perotin.playerchannels.PlayerChannels;
import me.perotin.playerchannels.objects.PlayerChannelUser;
import me.perotin.playerchannels.storage.files.ChannelFile;
import me.perotin.playerchannels.storage.files.FileType;
import org.bukkit.entity.Player;

public class CreateChannelSubCommand extends SubCommand {


    public CreateChannelSubCommand(String label) {
        super(label);
    }

    @Override
    public void onCommand(Player player, PlayerChannelUser user, String[] args) {
        ChannelFile messages = new ChannelFile(FileType.MESSAGES);
        if (!player.hasPermission("playerchannels.create")) {
            messages.sendConfigMsg(player, "no-permission");
            return;
        }
        if (args.length == 1) {
            // Typed just /pc create
            // Check if has permissions, "Player's channel" <- check if that is availible
           String channelName = player.getName() + "'s Channel";
           if (PlayerChannels.getInstance().getChatroom(channelName) != null) {
               // Keep adding numbers until not found
               int addition = 0;
               while (PlayerChannels.getInstance().getChatroom(channelName) != null) {
                   addition++;
                   channelName += "" + addition;
               }
           } else {

           }
        } else if (args.length == 2) {
            // Typed /pc create <name>
        } else if (args.length >= 3) {
            // Typed /pc create <name> <description>
        }

        return;
    }
}
