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


/**
 * The {@code InviteSubCommand} class is responsible for handling the "invite" subcommand
 * in the PlayerChannels plugin. This command allows players to invite other players
 * to specific chatrooms they have permission to manage.
 *
 * <p>Format: <code>/channels invite &lt;player&gt; &lt;optional: channel-name&gt;</code></p>
 *
 */
public class InviteSubCommand extends SubCommand {

    /**
     * Creates a new instance of the {@code InviteSubCommand}.
     *
     * @param label The command label for this subcommand.
     */
    public InviteSubCommand(String label) {
        super(label);
    }

    /**
     * Executes the "invite" subcommand when called by a player.
     * This method handles the logic of inviting another player to a chatroom,
     * either the one the sender is currently in or to a specified chatroom.
     *
     * @param player The player executing the command.
     * @param user   The {@link PlayerChannelUser} associated with the player.
     * @param args   Command arguments passed by the player.
     */
    @Override
    public void onCommand(Player player, PlayerChannelUser user, String[] args) {
        ChannelFile messages = new ChannelFile(FileType.MESSAGES);

        if (args.length < 2) {
            // Not enough arguments
            player.sendMessage(messages.getString("help-command-usage"));
            return;
        }

        if (args.length == 2) {
            // Inviting a player when the sender is in only one chatroom
            if (user.getChatrooms().size() != 1) {
                player.sendMessage(messages.getString("not-in-one-chatroom"));
                player.sendMessage(messages.getString("help-command-usage"));
                return;
            }

            Chatroom channel = user.getChatrooms().get(0);

            if (channel.isPublic() || channel.getRole(player.getUniqueId()).getValue() < ChatRole.MODERATOR.getValue()) {
                player.sendMessage(messages.getString("no-permission-to-invite"));
                return;
            }

            Player toSend = Bukkit.getPlayer(args[1]);

            if (toSend == null) {
                player.sendMessage(messages.getString("player-not-found"));
                return;
            }

            if (channel.isInChatroom(toSend.getUniqueId())) {
                player.sendMessage(messages.getString("player-already-in-channel"));
                return;
            }

            PlayerChannelUser target = PlayerChannelUser.getPlayer(toSend.getUniqueId());
            if (target.hasPendingInviteFrom(channel)) {
                player.sendMessage(messages.getString("pending-invite-exists"));
                return;
            }

            // Send invite logic
            player.sendMessage(messages.getString("invite-sent-successfully").replace("$player$", toSend.getName()));
            toSend.sendMessage(messages.getString("invite-received").replace("$player$", player.getName())
                    .replace("$chatroom$", channel.getName()));
            String msg = messages.getString("invite-received-2")
                    .replace("$chatroom$", channel.getName());
            ChannelUtils.sendClickableCommand(toSend, msg, "/channels join " + channel.getName());
            target.addInvite(channel);
            return;
        }

        if (args.length == 3) {
            // Inviting a player while specifying the chatroom
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

            if (chatroom.isPublic() || !chatroom.isInChatroom(player.getUniqueId())
                    || chatroom.getRole(player.getUniqueId()).getValue() < ChatRole.MODERATOR.getValue()) {
                player.sendMessage(messages.getString("no-permission-to-invite"));
                return;
            }

            if (chatroom.isInChatroom(invitee.getUniqueId())) {
                player.sendMessage(messages.getString("already-in-channel"));
                return;
            }

            PlayerChannelUser target = PlayerChannelUser.getPlayer(invitee.getUniqueId());
            if (target.hasPendingInviteFrom(chatroom)) {
                player.sendMessage(messages.getString("pending-invite-exists"));
                return;
            }

            // Send invite logic
            player.sendMessage(messages.getString("invite-sent-successfully").replace("$player$", invitee.getName()));
            invitee.sendMessage(messages.getString("invite-received").replace("$player$", player.getName())
                    .replace("$chatroom$", chatroom.getName()));
            String msg = messages.getString("invite-received-2")
                    .replace("$chatroom$", chatroom.getName());
            ChannelUtils.sendClickableCommand(invitee, msg, "/channels join " + chatroom.getName());
            target.addInvite(chatroom);
        }
    }


    /**
     * Sends a plugin message to check if a specific player is online
     * on the network and to validate the context of the invitation.
     *
     * @param sender           The player who initiated the request.
     * @param targetPlayerName The name of the player being checked.
     * @param channelName      The name of the chatroom relevant to the check.
     */
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
