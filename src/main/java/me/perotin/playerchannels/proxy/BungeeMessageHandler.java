package me.perotin.playerchannels.proxy;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteStreams;
import me.perotin.playerchannels.PlayerChannels;
import me.perotin.playerchannels.objects.Chatroom;
import me.perotin.playerchannels.objects.GlobalChatroom;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.util.List;
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

        if (channelNames.contains(channel.toLowerCase())) {
            // Bungee Chat Message
            handleChatMessage(in, channel);

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
                GlobalChatroom newChatroom = new GlobalChatroom(owner, name, description, isPrivate, isSaved,  serverChatroom);
                if (!plugin.getChatrooms().contains(newChatroom)) plugin.getChatrooms().add(newChatroom);
            } catch (IOException ex) {
                ex.printStackTrace();
            }

    }
}
