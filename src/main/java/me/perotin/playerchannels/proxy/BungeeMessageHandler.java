package me.perotin.playerchannels.proxy;

import com.google.common.collect.Iterables;
import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import me.perotin.playerchannels.PlayerChannels;
import me.perotin.playerchannels.objects.ChatRole;
import me.perotin.playerchannels.objects.Chatroom;
import me.perotin.playerchannels.objects.GlobalChatroom;
import me.perotin.playerchannels.objects.PlayerChannelUser;
import me.perotin.playerchannels.storage.Pair;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.io.*;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

public class BungeeMessageHandler {

    private PlayerChannels plugin;
    public BungeeMessageHandler(PlayerChannels plugin) {
        this.plugin = plugin;
    }

    public void handlePluginMessage(String channel, Player player, byte[] message) {
        if (!channel.equals("BungeeCord")) {
            return;
        }

        ByteArrayDataInput in = ByteStreams.newDataInput(message);
        String subchannel = in.readUTF();
        List<String> channelNames = plugin.getChatrooms().stream()
                .filter(c -> c instanceof GlobalChatroom)
                .map(c -> ChatColor.stripColor(c.getName().toLowerCase()))
                .collect(Collectors.toList());




        if (subchannel.equalsIgnoreCase("Create")) {
            handleCreateMessage(in);
        }
        if (subchannel.equalsIgnoreCase("AddMember")) {
            handleAddMember(in);
        }
        if (subchannel.equalsIgnoreCase("Remove")) {
            handleRemoveMember(in);
        }
        if (subchannel.equalsIgnoreCase("Mute")) {
            handleMuteMember(in);
        }
        if (subchannel.equalsIgnoreCase("PromoteToMod")) {
            handleModPromotion(in);
        }
        if (subchannel.equalsIgnoreCase("DemoteToMember")) {
            handleDemoteToMember(in);
        }
        if (subchannel.equalsIgnoreCase("PromoteModToOwner")) {
            handlePromoteToOwner(in);
        }
        if (subchannel.equalsIgnoreCase("Delete")) {
            handleDeleteChannel(in);
        }

        if (subchannel.equalsIgnoreCase("BroadcastMessageToAll_")) {
            handleBroadcastMessage(in);
        }
        if (subchannel.equalsIgnoreCase("Ban")) {
            handleBanMember(in);
        }
        if (subchannel.equalsIgnoreCase("Unban")) {
            handleUnbanMember(in);
        }
        if (subchannel.equalsIgnoreCase("Unmute")) {
            handleUnmuteMember(in);
        }
        if (subchannel.equalsIgnoreCase("SetNickname")) {
            handleSetNickname(in);
        }
        if (subchannel.equalsIgnoreCase("GlobalSearchOnRestart")){
            handleServerRestart(in);

        }

        if (subchannel.equals("ToggleNicknames")) {
            handleNicknameToggle(in);
        }

        if (channelNames.contains(subchannel.toLowerCase())) {
            // Bungee Chat Message
            handleChatMessage(in, subchannel);

        }

        if (subchannel.equalsIgnoreCase("GlobalChannelsReceived")) {
            handleReceiveGlobalChannels(in);
        }

        if (subchannel.equals("CheckPlayerPresence")) {
            String targetPlayerName = in.readUTF();
            String channelName = in.readUTF(); // The channel for context, if needed
            String requestName = in.readUTF();

            Player targetPlayer = Bukkit.getPlayerExact(targetPlayerName);
            if (targetPlayer != null) {
                // Player found, send a response back
                PlayerChannelUser.getPlayer(targetPlayer.getUniqueId()).addInvite(PlayerChannels.getInstance().getChatroom(channelName));
                sendPlayerFoundResponse(targetPlayer, player.getName(), channelName, requestName);
            }
        }

        if (subchannel.equals("PlayerFoundResponse")) {
            handlePlayerFoundResponse(in);

        }







        }

    private void handleUnmuteMember(ByteArrayDataInput in) {
        short len = in.readShort();
        byte[] msgbytes = new byte[len];
        in.readFully(msgbytes);

        DataInputStream msgin = new DataInputStream(new ByteArrayInputStream(msgbytes));
        try {
            String channelName = msgin.readUTF();
            UUID member = UUID.fromString(msgin.readUTF());
            Chatroom channel = plugin.getChatroom(channelName);
            if (channel.isMuted(member) ) {
                channel.getMutedMembers().remove(member);
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    private void handleUnbanMember(ByteArrayDataInput in) {
        short len = in.readShort();
        byte[] msgbytes = new byte[len];
        in.readFully(msgbytes);

        DataInputStream msgin = new DataInputStream(new ByteArrayInputStream(msgbytes));
        try {
            String channelName = msgin.readUTF();
            UUID member = UUID.fromString(msgin.readUTF());
            Chatroom channel = plugin.getChatroom(channelName);
            if (channel.isBanned(member) ) {
                channel.getBannedMembers().remove(member);
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    private void handleSetNickname(ByteArrayDataInput in) {
        short len = in.readShort();
        byte[] msgbytes = new byte[len];
        in.readFully(msgbytes);

        DataInputStream msgin = new DataInputStream(new ByteArrayInputStream(msgbytes));
        try {
            String channelName = msgin.readUTF();
            UUID toSet = UUID.fromString(msgin.readUTF());
            String nick = msgin.readUTF();
            Chatroom channel = plugin.getChatroom(channelName);
            channel.getNickNames().put(toSet, nick);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    private void handleNicknameToggle(ByteArrayDataInput in) {
        short len = in.readShort();
        byte[] msgbytes = new byte[len];
        in.readFully(msgbytes);

        DataInputStream msgin = new DataInputStream(new ByteArrayInputStream(msgbytes));
        try {
            String channelName = msgin.readUTF();
            boolean value = msgin.readBoolean();
            Chatroom channel = plugin.getChatroom(channelName);
            channel.setNicknamesEnabled(value);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    private void handleReceiveGlobalChannels(ByteArrayDataInput in) {
        short len = in.readShort();
        byte[] msgbytes = new byte[len];
        in.readFully(msgbytes);

        DataInputStream msgin = new DataInputStream(new ByteArrayInputStream(msgbytes));
        try {
            while (msgin.available() > 0) { // Check if there's more data to read
                String channelName = msgin.readUTF();
                String description = msgin.readUTF();
                String ownerUUIDString = msgin.readUTF();
                UUID ownerUUID = ownerUUIDString.equals("0") ? null : UUID.fromString(ownerUUIDString);
                boolean isPublic = msgin.readBoolean();
                boolean isSaved = msgin.readBoolean();
                boolean isServerOwned = msgin.readBoolean();



                // Assuming there's a constructor or method to create/add a channel like this
                GlobalChatroom globalChatroom = new GlobalChatroom(ownerUUID, channelName, description, isPublic, isSaved, isServerOwned);

                if (ownerUUID != null) {
                    PlayerChannelUser.getPlayer(ownerUUID).addChatroom(globalChatroom);

                }
                // Reading moderators
                String uuidString;
                while (!(uuidString = msgin.readUTF()).equals("~")) { // Line 118 is here
                    UUID modUUID = UUID.fromString(uuidString);
                    globalChatroom.addMember(new Pair<>(modUUID, ChatRole.MODERATOR), "");
                    PlayerChannelUser.getPlayer(modUUID).addChatroom(globalChatroom);
                }

                // Reading members, assuming another delimiter if necessary, or end of stream if not
                String memberId;
                while (!(memberId = msgin.readUTF()).equalsIgnoreCase("~~")) {
                    UUID memberUUID = UUID.fromString(memberId);
                    globalChatroom.addMember(new Pair<>(memberUUID, ChatRole.MEMBER), ""); // Assuming a method like this exists
                    PlayerChannelUser.getPlayer(memberUUID).addChatroom(globalChatroom);

                }

                // Check if the channel already exists before adding
                if (!plugin.getChatrooms().contains(globalChatroom)) {
                    plugin.getChatrooms().add(globalChatroom);
                    Bukkit.getConsoleSender().sendMessage("[PlayerChannels] Added global channel: " + channelName);
                }
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }


    private void handleServerRestart(ByteArrayDataInput in) {
        short len = in.readShort();
        byte[] msgbytes = new byte[len];
        in.readFully(msgbytes);

        DataInputStream msgin = new DataInputStream(new ByteArrayInputStream(msgbytes));
        sendGlobalChannels();
    }

    private void handleDeleteChannel(ByteArrayDataInput in) {
        short len = in.readShort();
        byte[] msgbytes = new byte[len];
        in.readFully(msgbytes);

        DataInputStream msgin = new DataInputStream(new ByteArrayInputStream(msgbytes));
        try {
            String channelName = msgin.readUTF();
            Chatroom channel = plugin.getChatroom(channelName);
            Bukkit.broadcastMessage("Attempting to delete channel");
            if (channel != null) {
                channel.delete();
                Bukkit.broadcastMessage("Deleted channel");

            } else {
                Bukkit.broadcastMessage("Could not delete");

            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    private void handleBroadcastMessage(ByteArrayDataInput in) {
        short len = in.readShort();
        byte[] msgbytes = new byte[len];
        in.readFully(msgbytes);

        DataInputStream msgin = new DataInputStream(new ByteArrayInputStream(msgbytes));
        try {
            String channelName = msgin.readUTF();
            String broadcastMessage = msgin.readUTF();
            Chatroom channel = plugin.getChatroom(channelName);
            channel.getOnlinePlayers().forEach(p -> p.sendMessage(broadcastMessage));
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    private void handleMuteMember(ByteArrayDataInput in) {
        short len = in.readShort();
        byte[] msgbytes = new byte[len];
        in.readFully(msgbytes);

        DataInputStream msgin = new DataInputStream(new ByteArrayInputStream(msgbytes));
        try {
            String channelName = msgin.readUTF();
            UUID member = UUID.fromString(msgin.readUTF());
            Chatroom channel = plugin.getChatroom(channelName);
            if (channel.isInChatroom(member) && !channel.isMuted(member)) {
                channel.mute(member);
                Bukkit.broadcastMessage("Muted in " + channelName);
            } else {
                Bukkit.broadcastMessage("Tried to mute in " + channelName);

            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    private void handleBanMember(ByteArrayDataInput in) {
        short len = in.readShort();
        byte[] msgbytes = new byte[len];
        in.readFully(msgbytes);

        DataInputStream msgin = new DataInputStream(new ByteArrayInputStream(msgbytes));
        try {
            String channelName = msgin.readUTF();
            UUID member = UUID.fromString(msgin.readUTF());
            Chatroom channel = plugin.getChatroom(channelName);
            if (!channel.isBanned(member) ) {
                channel.ban(member);
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }


    private void handlePromoteToOwner(ByteArrayDataInput in) {
        short len = in.readShort();
        byte[] msgbytes = new byte[len];
        in.readFully(msgbytes);

        DataInputStream msgin = new DataInputStream(new ByteArrayInputStream(msgbytes));
        try {
            String channelName = msgin.readUTF();
            UUID toOwner = UUID.fromString(msgin.readUTF());
            UUID toMod = UUID.fromString(msgin.readUTF());
            Chatroom channel = plugin.getChatroom(channelName);
            if (channel.isInChatroom(toOwner) && channel.isInChatroom(toMod) && channel.getMemberMap().get(toOwner) == ChatRole.MODERATOR) {
                channel.promoteModeratorToOwner(toOwner, toMod);
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    private void handleDemoteToMember(ByteArrayDataInput in) {
        short len = in.readShort();
        byte[] msgbytes = new byte[len];
        in.readFully(msgbytes);

        DataInputStream msgin = new DataInputStream(new ByteArrayInputStream(msgbytes));
        try {
            String channelName = msgin.readUTF();
            UUID key = UUID.fromString(msgin.readUTF());
            Chatroom channel = plugin.getChatroom(channelName);
            Bukkit.broadcastMessage(channel.toString());
            if (channel.isInChatroom(key) && channel.getMemberMap().get(key) == ChatRole.MODERATOR) {
                channel.demoteModeratorToMember(key);
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    private void handleModPromotion(ByteArrayDataInput in) {
        short len = in.readShort();
        byte[] msgbytes = new byte[len];
        in.readFully(msgbytes);

        DataInputStream msgin = new DataInputStream(new ByteArrayInputStream(msgbytes));
        try {
            String channelName = msgin.readUTF();
            UUID key = UUID.fromString(msgin.readUTF());
            Chatroom channel = plugin.getChatroom(channelName); // How to ensure this is Chatroom obj and not GlobalChatroom?
            Bukkit.broadcastMessage(channel.toString());
            if (channel.isInChatroom(key) && channel.getMemberMap().get(key) == ChatRole.MEMBER) {
                channel.promoteMemberToModerator(key);

            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    /**
     * Funcion for when a player leaves a global channel
     * @param in
     */
    private void handleRemoveMember(ByteArrayDataInput in) {
        short len = in.readShort();
        byte[] msgbytes = new byte[len];
        in.readFully(msgbytes);

        DataInputStream msgin = new DataInputStream(new ByteArrayInputStream(msgbytes));
        try {
            String channelName = msgin.readUTF();
            UUID key = UUID.fromString(msgin.readUTF());
            Chatroom channel = plugin.getChatroom(channelName);
            if (channel.isInChatroom(key)) {
                channel.removeMember(key);
               PlayerChannelUser.getPlayer(key).leaveChatroom(channel);
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    /**
     * Function to handle when someone joins a global channel
     * @param in
     */
    private void handleAddMember(ByteArrayDataInput in) {
        short len = in.readShort();
        byte[] msgbytes = new byte[len];
        in.readFully(msgbytes);

        DataInputStream msgin = new DataInputStream(new ByteArrayInputStream(msgbytes));
        try {
            String channelName = msgin.readUTF();
            Chatroom channel = plugin.getChatroom(channelName);
            String stringId = msgin.readUTF();
            int role = msgin.readInt();
            String name = msgin.readUTF();
            UUID id = UUID.fromString(stringId);
            Pair<UUID, ChatRole> value = new Pair<>(id, ChatRole.getRole(role));

            if (!channel.isInChatroom(id)) {

                channel.addMember(value, name);
                // Need to find their PlayerChannelUser object and store it their too
                PlayerChannelUser user = PlayerChannelUser.getPlayer(id);

                if (user.getName().equalsIgnoreCase("-1")) {
                    // Name not found, set it to proper name
                    user.setName(name);
                }
                if (!user.isMemberOf(channel)) {
                    user.addChatroom(channel);
                }

            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }


    /**
     * Function to handle sending a chat message to all members of the channel on all servers
     * @param in
     */
    private void handleChatMessage (ByteArrayDataInput in, String channelName) {
        short len = in.readShort();
        byte[] msgbytes = new byte[len];
        in.readFully(msgbytes);

        DataInputStream msgin = new DataInputStream(new ByteArrayInputStream(msgbytes));
        try {
            String message = msgin.readUTF();
            Chatroom channel = plugin.getChatroom(channelName);
            channel.chat(message);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    /**
     * Function to handle a new global chatroom creation event
     * @param in
     */
    private void handleCreateMessage (ByteArrayDataInput in) {
            short len = in.readShort();
            byte[] msgbytes = new byte[len];
            in.readFully(msgbytes);

            DataInputStream msgin = new DataInputStream(new ByteArrayInputStream(msgbytes));
            try {
                String name = msgin.readUTF();
                String description = msgin.readUTF();
                String ownerUUID = msgin.readUTF();
                UUID owner = null;
                if(!ownerUUID.equals("0")){
                    owner = UUID.fromString(ownerUUID);
                }
                boolean isPrivate = msgin.readBoolean();
                boolean isSaved = msgin.readBoolean();
                boolean serverChatroom = msgin.readBoolean();
                String ownerName = msgin.readUTF();
                GlobalChatroom newChatroom = new GlobalChatroom(owner, name, description, isPrivate, isSaved,  serverChatroom);
                PlayerChannelUser user = PlayerChannelUser.getPlayer(owner);
                if (user.getName().equalsIgnoreCase("-1")) {
                    user.setName(ownerName);
                }

                if (!plugin.getChatrooms().stream().map(Chatroom::getName).collect(Collectors.toList()).contains(name)) {
                    plugin.getChatrooms().add(newChatroom);

                    user.addChatroom(newChatroom);

                } else {
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }

    }

    private void sendPlayerFoundResponse(Player targetPlayer, String senderName, String channelName, String requestName) {
        ByteArrayDataOutput out = ByteStreams.newDataOutput();
        out.writeUTF("ForwardToPlayer");
        out.writeUTF(senderName); // Sender's name is used to route the message
        out.writeUTF("PlayerFoundResponse"); // A custom sub-channel for this purpose

        // Prepare the actual data to send back
        ByteArrayOutputStream msgBytes = new ByteArrayOutputStream();
        DataOutputStream msgOut = new DataOutputStream(msgBytes);
        try {
            msgOut.writeUTF(targetPlayer.getUniqueId().toString()); // Send the found player's UUID
            msgOut.writeUTF(channelName); // Include the channel name for context
            msgOut.writeUTF(requestName);

            out.writeShort(msgBytes.toByteArray().length);
            out.write(msgBytes.toByteArray());

            // Send the plugin message. Assuming `PlayerChannels.getInstance()` gets your plugin instance correctly.
            targetPlayer.sendPluginMessage(PlayerChannels.getInstance(), "BungeeCord", out.toByteArray());
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    private void handlePlayerFoundResponse(ByteArrayDataInput in) {
        short len = in.readShort();
        byte[] msgbytes = new byte[len];
        in.readFully(msgbytes);
        DataInputStream msgIn = new DataInputStream(new ByteArrayInputStream(msgbytes));

        try {
            String playerUUIDStr = msgIn.readUTF();
            UUID playerUUID = UUID.fromString(playerUUIDStr);
            String channelName = msgIn.readUTF();
            String requestName = msgIn.readUTF();
            GlobalChatroom chatroom = (GlobalChatroom) PlayerChannels.getInstance().getChatroom(channelName);

            // Now that you have the UUID, you can proceed with the invite logic
            // This would typically involve inviting the player to the channel, possibly by creating a new invite object
            // and notifying the player if they are currently online on this server.
            PlayerChannelUser.getPlayer(playerUUID).addInvite(chatroom);
            Bukkit.broadcastMessage("Found player and added chatroom invite");
            sendFoundPlayerMessage(requestName);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }


    private void sendFoundPlayerMessage(String name) {

        ByteArrayDataOutput out = ByteStreams.newDataOutput();
        out.writeUTF("Message");
        out.writeUTF(name);
        out.writeUTF( "That has been invited from another server!");

        ByteArrayOutputStream msgbytes = new ByteArrayOutputStream();
        DataOutputStream msgout = new DataOutputStream(msgbytes);
        out.writeShort(msgbytes.toByteArray().length);
        out.write(msgbytes.toByteArray());
        Iterables.getFirst(Bukkit.getOnlinePlayers(), null).sendPluginMessage(PlayerChannels.getInstance(), "BungeeCord", out.toByteArray());


    }
    private void sendGlobalChannels() {
        ByteArrayDataOutput out = ByteStreams.newDataOutput();
        out.writeUTF("Forward");
        out.writeUTF("ALL");
        out.writeUTF("GlobalChannelsReceived");

        ByteArrayOutputStream msgbytes = new ByteArrayOutputStream();
        DataOutputStream msgout = new DataOutputStream(msgbytes);
        try {


            for (Chatroom c : plugin.getChatrooms()) {
                if (c instanceof GlobalChatroom) {
                    GlobalChatroom globalChatroom = (GlobalChatroom) c;
                    msgout.writeUTF(globalChatroom.getName());
                    msgout.writeUTF(globalChatroom.getDescription());
                    if (globalChatroom.getOwner() == null) {
                        msgout.writeUTF("0");
                    } else {
                        msgout.writeUTF(globalChatroom.getOwner().toString());
                    }
                    msgout.writeBoolean(globalChatroom.isPublic());
                    msgout.writeBoolean(globalChatroom.isSaved());
                    msgout.writeBoolean(globalChatroom.isServerOwned());
//                    msgout.writeUTF(Objects.requireNonNull(Bukkit.getPlayer(globalChatroom.getOwner())).getName());
                    for (UUID uuid : globalChatroom.getModerators()) {
                        msgout.writeUTF(uuid.toString());
                    }
                    msgout.writeUTF("~");

                    for (UUID uuid : globalChatroom.getMembersOnly()) {
                        msgout.writeUTF(uuid.toString());
                    }
                    msgout.writeUTF("~~");

                }
            }

            out.writeShort(msgbytes.toByteArray().length);
            out.write(msgbytes.toByteArray());
            Iterables.getFirst(Bukkit.getOnlinePlayers(), null).sendPluginMessage(PlayerChannels.getInstance(), "BungeeCord", out.toByteArray());
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
}
