package me.perotin.playerchannels.commands.subcommands;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import me.perotin.playerchannels.PlayerChannels;
import me.perotin.playerchannels.objects.ChatRole;
import me.perotin.playerchannels.objects.Chatroom;
import me.perotin.playerchannels.objects.PlayerChannelUser;
import me.perotin.playerchannels.storage.files.ChannelFile;
import me.perotin.playerchannels.storage.files.FileType;
import me.perotin.playerchannels.utils.ChannelUtils;
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
                    if (toSend == null && PlayerChannels.getInstance().isBungeecord() && channel.isGlobal()) {
                        requestPlayerCheck(player, args[1], channel.getName());
                        Bukkit.broadcastMessage("Requesting player on all servers");


                    }

                    PlayerChannelUser playerChannelUser = PlayerChannelUser.getPlayer(toSend.getUniqueId());
                    if (toSend.getUniqueId() != null) {
                        if (!channel.isInChatroom(toSend.getUniqueId())) {
                            PlayerChannelUser target = PlayerChannelUser.getPlayer(toSend.getUniqueId());
                            if (!target.hasPendingInviteFrom(channel)) {
                                // Send invite logic here
                                player.sendMessage(messages.getString("invite-sent-successfully").replace("$player$", toSend.getName()));
                                toSend.sendMessage(messages.getString("invite-received").replace("$player$", player.getName())
                                        .replace("$chatroom$", channel.getName()));
                                String msg = messages.getString("invite-received-2")
                                        .replace("$chatroom$", channel.getName());
                                ChannelUtils.sendClickableCommand(toSend, msg, "/channels join " + channel.getName());


                                playerChannelUser.addInvite(channel);

                            } else {
                                player.sendMessage(messages.getString("pending-invite-exists"));
                            }
                        } else {
                            player.sendMessage(messages.getString("player-already-in-channel"));
                        }
                    }else {
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
                        String msg = messages.getString("invite-received-2")
                                .replace("$chatroom$", chatroom.getName());
                       ChannelUtils.sendClickableCommand(invitee, msg, "/channels join " + chatroom.getName());
                        target.addInvite(chatroom);
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

    private void requestPlayerCheck(Player sender, String targetPlayerName, String channelName) {
        ByteArrayDataOutput out = ByteStreams.newDataOutput();
        out.writeUTF("Forward");
        out.writeUTF("ALL");
        out.writeUTF("CheckPlayerPresence"); // Custom sub-channel for this purpose
        out.writeUTF(targetPlayerName);
        out.writeUTF(channelName); // Send the channel name for context in the response
        out.writeUTF(sender.getName());

        sender.sendPluginMessage(PlayerChannels.getInstance(), "BungeeCord", out.toByteArray());
    }


}
