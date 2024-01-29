package me.perotin.playerchannels.objects;

import com.google.common.collect.Iterables;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import me.perotin.playerchannels.PlayerChannels;
import me.perotin.playerchannels.storage.Pair;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Objects;
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
    }


    @Override
    public void addMember(Pair<UUID, ChatRole> value, String name) {
        if (name.equalsIgnoreCase("")) {
            Player player = Bukkit.getPlayer(value.getFirst());
            name = "failed"; // Default name if the player is not found

            if (player != null) { // Check if the player object is not null
                name = player.getName(); // Get the player's name if they are online
            }
        }


        super.addMember(value, name);
        sendBungeeWrite("AddMember", getName(), value.getFirst().toString(), value.getSecond().getValue(), name);

    }

    @Override
    public void removeMember(UUID key) {
        super.removeMember(key);
        sendBungeeWrite("Remove", getName(), key.toString());
    }



    @Override
    public String chat(String sender, String message, UUID id){
        String msg = super.chat(sender, message, id);
        chatToAllServers(msg);
        return msg;
    }

    /**
     * Send message to all servers
     * @param message
     */
    private void chatToAllServers(String message) {
        sendBungeeWrite(getName(), message);

    }

    /**
     * Function to send message to all servers to contain the chatroom
     */
    public void writeToAllServers() {
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

    private void sendBungeeWrite(String channel, Object ...o) {
        ByteArrayDataOutput out = ByteStreams.newDataOutput();
        out.writeUTF("Forward");
        out.writeUTF("ALL");
        out.writeUTF(channel);

        ByteArrayOutputStream msgbytes = new ByteArrayOutputStream();
        DataOutputStream msgout = new DataOutputStream(msgbytes);
        try {

            for (Object obj : o) {
                if (obj instanceof String) {
                    msgout.writeUTF((String) obj);
                } else if (obj instanceof Integer) {
                    msgout.writeInt((Integer) obj);
                } else if (obj instanceof Boolean) {
                    msgout.writeBoolean((boolean) obj);
                } else if (obj instanceof UUID) {
                    UUID uuid = (UUID) obj;
                    msgout.writeUTF(uuid.toString());
                }else {
                    msgout.write((Integer) obj);
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
