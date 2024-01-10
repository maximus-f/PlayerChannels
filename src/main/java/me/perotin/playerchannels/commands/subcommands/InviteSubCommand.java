package me.perotin.playerchannels.commands.subcommands;

import me.perotin.playerchannels.PlayerChannels;
import me.perotin.playerchannels.objects.ChatRole;
import me.perotin.playerchannels.objects.Chatroom;
import me.perotin.playerchannels.objects.PlayerChannelUser;
import me.perotin.playerchannels.storage.files.ChannelFile;
import me.perotin.playerchannels.storage.files.FileType;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class InviteSubCommand extends SubCommand{

    public InviteSubCommand(String label) {
        super(label);
    }


    /*
    Steps to invite:
    1. Check if the player sending the invite is in a chatroom
    2. Check if the player sending the invite has permission to send an invite
    3. Check if the player receiving exists
    4. Check if the player receiving is currently in the channel
    5. Check if the player receiving already has an invite to that channel
    6. Check if the player sending the invite is in more than 1 channel

    Format: /channels invite <player> <optional: channel-name>

     */

    @Override
    public void onCommand(Player player, PlayerChannelUser user, String[] args) {
        ChannelFile messages = new ChannelFile(FileType.MESSAGES);

        if (args.length == 1) {
            // Player used the command without specifying enough arguments
            player.sendMessage(messages.getString("help-command-usage"));
        } else if (args.length == 2) {
            // Inviting a player when the sender is in only one chatroom
            if (user.getChatrooms().size() == 1) {
                Chatroom channel = user.getChatrooms().get(0);
                if (!channel.isPublic() && channel.getRole(player.getUniqueId()).getValue() >= ChatRole.MODERATOR.getValue()) {
                    Player toSend = Bukkit.getPlayer(args[1]);
                    if (toSend != null) {
                        if (!channel.isInChatroom(toSend.getUniqueId())) {
                            PlayerChannelUser target = PlayerChannelUser.getPlayer(toSend.getUniqueId());
                            if (!target.hasPendingInviteFrom(channel)) {
                                // Send invite logic here
                                player.sendMessage(messages.getString("invite-sent-successfully").replace("$player$", toSend.getName()));
                                toSend.sendMessage(messages.getString("invite-received").replace("$player$", player.getName())
                                        .replace("$chatroom$", channel.getName()));
                            } else {
                                player.sendMessage(messages.getString("pending-invite-exists"));
                            }
                        } else {
                            player.sendMessage(messages.getString("player-already-in-channel"));
                        }
                    } else {
                        player.sendMessage(messages.getString("player-not-found"));
                    }
                } else {
                    player.sendMessage(messages.getString("no-permission-to-invite"));
                }
            } else {
                player.sendMessage(messages.getString("not-in-one-chatroom"));
                player.sendMessage(messages.getString("help-command-usage"));

            }
        } else if (args.length == 3) {
            // Inviting a player when specifying the chatroom
            Chatroom chatroom = PlayerChannels.getInstance().getChatroom(args[2]);
            Player invitee = Bukkit.getPlayer(args[1]);

            if (invitee == null) {
                player.sendMessage(messages.getString("player-not-found"));
                return;
            }
            if (chatroom == null) {
                player.sendMessage(messages.getString("channel-not-found"));
                return;
            }
            if (!chatroom.isPublic() && chatroom.isInChatroom(player.getUniqueId()) && chatroom.getRole(player.getUniqueId()).getValue() >= ChatRole.MODERATOR.getValue()) {
                if (!chatroom.isInChatroom(invitee.getUniqueId())) {
                    PlayerChannelUser target = PlayerChannelUser.getPlayer(invitee.getUniqueId());
                    if (!target.hasPendingInviteFrom(chatroom)) {
                        // Send invite logic here
                        player.sendMessage(messages.getString("invite-sent-successfully").replace("$player$", invitee.getName()));
                        invitee.sendMessage(messages.getString("invite-received").replace("$player$", player.getName())
                                .replace("$chatroom$", chatroom.getName()));
                    } else {
                        player.sendMessage(messages.getString("pending-invite-exists"));
                    }
                } else {
                    player.sendMessage(messages.getString("already-in-channel"));
                }
            } else {
                player.sendMessage(messages.getString("no-permission-to-invite"));
            }
        }
    }

//    @Override
//    public void onCommand(Player player, PlayerChannelUser user, String[] args) {
//        // First work on case with member of one channel
//        ChannelFile messages = new ChannelFile(FileType.MESSAGES);
//        if (args.length == 1) {
//            // Just did /channels invite, send help message for this command
//        } else if (args.length == 2) {
//            // Check if they are a member of a chatroom with permission to invite, then check if player exists
//            if (user.getChatrooms().size() == 1) {
//                // Check if arg[1] is a valid player
//                Chatroom channel = user.getChatrooms().get(0);
//
//                if (!channel.isPublic() && channel.getRole(player.getUniqueId()).getValue() >= ChatRole.MODERATOR.getValue()) {
//                    if (Bukkit.getPlayer(args[1]) != null) {
//                        // Send invite
//                        Player toSend = Bukkit.getPlayer(args[1]);
//                        if (channel.isInChatroom(toSend.getUniqueId())) {
//                            // Send message that they are already in the channel
//                            return;
//                        }
//                        PlayerChannelUser target = PlayerChannelUser.getPlayer(toSend.getUniqueId());
//                        if (target.hasPendingInviteFrom(channel)) {
//                            // Send message that they already have a pending invite
//                            return;
//                        }
//                    } else {
//                        // Player not found
//                    }
//                } else {
//                    // No permission to send
//                }
//
//            } else {
//                // Tell them they are not in only one chatroom
//            }
//
//        } else if (args.length == 3) {
//            // If in more than one chatroom, must specify channel name at the end
//            Chatroom chatroom = PlayerChannels.getInstance().getChatroom(args[2]);
//            Player invitee = Bukkit.getPlayer(args[1]);
//
//            if (invitee == null) {
//                // Send message saying player not found
//                return;
//            }
//            if (chatroom == null) {
//                // Send message saying channel not found
//                return;
//            }
//            if (!chatroom.isInChatroom(invitee.getUniqueId())) {
//                // Send message saying they are not in that channel
//                return;
//            }
//            if (!chatroom.isPublic() && chatroom.getRole(player.getUniqueId()).getValue() >= ChatRole.MODERATOR.getValue()) {
//                if (chatroom.isInChatroom(invitee.getUniqueId())) {
//                    // Send message saying that player is already in that channel
//                    return;
//                } else {
//                    // Send invite
//                    PlayerChannelUser target = PlayerChannelUser.getPlayer(invitee.getUniqueId());
//                    if (target.hasPendingInviteFrom(chatroom)) {
//                        // Send message that they already have a pending invite
//                        return;
//                    }
//                }
//            } else {
//                // Send message saying that they do not have permission to do this
//            }
//        }
//    }
}
