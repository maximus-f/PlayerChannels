package me.perotin.playerchannels.objects;

import com.google.common.collect.Iterables;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import me.perotin.playerchannels.PlayerChannels;
import org.bukkit.Bukkit;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.UUID;

/**
 * @author maxfuligni
 * @dateBegan 1/6/24
 *
 * Class to represent a channel that is cross-network
 */
public class GlobalChatroom extends Chatroom {
    public GlobalChatroom(UUID owner, String name, String description, boolean isPublic, boolean isSaved, boolean isServerOwned) {
        super(owner, name, description, isPublic, isSaved, isServerOwned);
        writeToAllServers();
    }

    /**
     * Function to send message to all servers to contain the chatroom
     */
    private void writeToAllServers() {
        ByteArrayDataOutput out = ByteStreams.newDataOutput();
        out.writeUTF("Forward");
        out.writeUTF("ALL");
        out.writeUTF("Create");

        ByteArrayOutputStream msgbytes = new ByteArrayOutputStream();
        DataOutputStream msgout = new DataOutputStream(msgbytes);
        try {
            msgout.writeUTF(getName());
            msgout.writeUTF(getDescription());
            if (getOwner() == null) {
                msgout.writeUTF("0");
            } else {
                msgout.writeUTF(getOwner().toString());
            }
            msgout.writeBoolean(isPublic());
            msgout.writeBoolean(isSaved());
            msgout.writeBoolean(isServerOwned());
            out.writeShort(msgbytes.toByteArray().length);
            out.write(msgbytes.toByteArray());
            Iterables.getFirst(Bukkit.getOnlinePlayers(), null).sendPluginMessage(PlayerChannels.getInstance(), "BungeeCord", out.toByteArray());
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }




}
