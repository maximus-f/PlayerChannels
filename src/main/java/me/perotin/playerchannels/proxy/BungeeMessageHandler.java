package me.perotin.playerchannels.proxy;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteStreams;
import me.perotin.playerchannels.PlayerChannels;
import me.perotin.playerchannels.objects.Chatroom;
import me.perotin.playerchannels.objects.GlobalChatroom;
import org.bukkit.entity.Player;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.util.UUID;

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


        if (subchannel.equalsIgnoreCase("Create")) {
            handleCreateMessage(in);
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
