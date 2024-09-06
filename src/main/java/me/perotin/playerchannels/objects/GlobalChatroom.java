package me.perotin.playerchannels.objects;

import com.google.common.collect.Iterables;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import me.perotin.playerchannels.PlayerChannels;
import me.perotin.playerchannels.storage.Pair;
import me.perotin.playerchannels.storage.changelog.ChannelManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Objects;
import java.util.UUID;

/**
 * @author maxfuligni
 * @dateBegan 1/6/24
 *
 * Class to represent a channel that is cross-network
 */
public class GlobalChatroom extends Chatroom {
    private final ChannelManager manager = PlayerChannels.getInstance().getChannelManager();

    public GlobalChatroom(UUID owner, String name, String description, boolean isPublic, boolean isSaved, boolean isServerOwned, boolean log) {
        super(owner, name, description, isPublic, isSaved, isServerOwned, true, false);
        if (isSaved && log) {
            manager.addChannel(name, owner.toString());
        }
    }



    @Override
    public void mute(UUID uuid) {
        super.mute(uuid);
        sendBungeeWrite("Mute", getName(), uuid.toString());
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

        manager.addMemberToChannel(getName(), value.getFirst().toString());
        super.addMember(value, name);
        sendBungeeWrite("AddMember", getName(), value.getFirst().toString(), value.getSecond().getValue(), name);

    }

    @Override
    public void setNicknamesEnabled(boolean enable) {
        super.setNicknamesEnabled(enable);
        sendBungeeWrite("ToggleNicknames", getName(), enable);
    }

    @Override
    public void setNickname(UUID toSet, String nick) {
        super.setNickname(toSet, nick);
        sendBungeeWrite("SetNickname", getName(), toSet.toString(), nick);
    }

    @Override
    public void removeMember(UUID key) {
        super.removeMember(key);
        manager.removeMemberFromChannel(getName(), key.toString());
        sendBungeeWrite("Remove", getName(), key.toString());
    }

    @Override
    public void unmute(UUID member) {
        super.unmute(member);
        sendBungeeWrite("Unmute", getName(), member.toString());
    }

    @Override
    public void unbanMember(UUID member) {
        super.unbanMember(member);
        sendBungeeWrite("Unban", getName(), member.toString());

    }

    @Override
    public void promoteMemberToModerator(UUID member) {
        super.promoteMemberToModerator(member);
        sendBungeeWrite("PromoteToMod", getName(), member.toString());
    }
    @Override
    public void demoteModeratorToMember(UUID member) {
        super.demoteModeratorToMember(member);
        sendBungeeWrite("DemoteToMember", getName(), member.toString());

    }

    @Override
    public void broadcastMessage(String message){
        super.broadcastMessage(message);
        sendBungeeWrite("BroadcastMessageToAll_", getName(), message);
    }
    @Override
    public void promoteModeratorToOwner(UUID member, UUID oldOwner) {
        super.promoteModeratorToOwner(member, oldOwner);
        sendBungeeWrite("PromoteModToOwner", getName(), member.toString(), oldOwner.toString());
    }

    @Override
    public String chat(String sender, String message, UUID id){
        String msg = super.chat(sender, message, id);
        chatToAllServers(msg);
        return msg;
    }

    @Override
    public void ban(UUID uuid) {
        super.ban(uuid);
        sendBungeeWrite("Ban", getName(), uuid.toString());
    }


    /**
     * Deletes a global channel and sends message to delete channel on other servers.
     *  Deletes in MySQL.
     */
    @Override
    public void delete() {

        PlayerChannels instance = PlayerChannels.getInstance();
        if (instance.isMySQL()) {
            instance.getSqlHandler().deleteChannel(this);
        }
        super.delete();

        sendBungeeWrite("Delete", getName());

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
            msgout.writeUTF(Objects.requireNonNull(Bukkit.getPlayer(getOwner())).getName());
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


    public static void sendGlobalSearch() {
        ByteArrayDataOutput out = ByteStreams.newDataOutput();
        out.writeUTF("Forward");
        out.writeUTF("ALL");
        out.writeUTF("GlobalSearchOnRestart");

        ByteArrayOutputStream msgbytes = new ByteArrayOutputStream();
        DataOutputStream msgout = new DataOutputStream(msgbytes);
        try {
            msgout.writeUTF("Test");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        out.writeShort(msgbytes.toByteArray().length);
        out.write(msgbytes.toByteArray());
        Player toSend = Iterables.getFirst(Bukkit.getOnlinePlayers(), null);
        if (toSend != null) {
            toSend.sendPluginMessage(PlayerChannels.getInstance(), "BungeeCord", out.toByteArray());
        }
    }

    public static void sendChannelManagerClear() {
        ByteArrayDataOutput out = ByteStreams.newDataOutput();
        out.writeUTF("Forward");
        out.writeUTF("ALL");
        out.writeUTF("ClearChannelManager");

        ByteArrayOutputStream msgbytes = new ByteArrayOutputStream();
        DataOutputStream msgout = new DataOutputStream(msgbytes);
        try {
            msgout.writeUTF("Test");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }


        out.writeShort(msgbytes.toByteArray().length);
        out.write(msgbytes.toByteArray());
        Player toSend = Iterables.getFirst(Bukkit.getOnlinePlayers(), null);
        if (toSend != null) {
            toSend.sendPluginMessage(PlayerChannels.getInstance(), "BungeeCord", out.toByteArray());
        }

    }



}
