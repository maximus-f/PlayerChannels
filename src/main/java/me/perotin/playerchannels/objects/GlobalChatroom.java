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
    private boolean isRemoteCall = true;

    public GlobalChatroom(UUID owner, String name, String description, boolean isPublic, boolean isSaved, boolean isServerOwned, boolean log) {
        super(owner, name, description, isPublic, isSaved, isServerOwned, true);
        if (isSaved && log) {
            PlayerChannels.getInstance().getSqlHandler().storeChatroom(this);
        }
    }

    public GlobalChatroom(UUID owner, String name, String description, boolean isPublic, boolean isSaved, boolean isServerOwned, boolean nicknamesEnabled, boolean hidden, boolean log) {
        super(owner, name, description, isPublic, isSaved, isServerOwned, true);
//        super.setNicknamesEnabled(nicknamesEnabled);
        super.setHidden(hidden);
        if (isSaved && log) {
            PlayerChannels.getInstance().getSqlHandler().storeChatroom(this);
        }

    }


    /**
     * More thorough test because could be saved but not have mysql enabled
     * @return
     */
    public boolean isSavedInDatabase() {
        return super.isSaved() && PlayerChannels.getInstance().isMySQL();
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

        if (isSavedInDatabase()) manager.addMemberToChannel(getName(), value.getFirst().toString());
        super.addMember(value, name);
        sendBungeeWrite("AddMember", getName(), value.getFirst().toString(), value.getSecond().getValue(), name);

    }

    @Override
    public void setNicknamesEnabled(boolean enable) {
        super.setNicknamesEnabled(enable);


        if (isRemoteCall()) sendBungeeWrite("ToggleNicknames", getName(), enable);
        else enableRemote();

        if (isSavedInDatabase()) manager.changeFieldStatus(getName());

    }

    @Override
    public void setHidden(boolean hidden) {
        super.setHidden(hidden);

        if (isSavedInDatabase()) manager.changeFieldStatus(getName());

       if (isRemoteCall()) sendBungeeWrite("SetHidden", getName(), hidden);
       else enableRemote(); // enable remote if disabled but skip global message
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
        if (isSavedInDatabase()) {
            manager.rankChange(getName(), member.toString(), 1);
        }
        super.promoteMemberToModerator(member);
        sendBungeeWrite("PromoteToMod", getName(), member.toString());
    }
    @Override
    public void demoteModeratorToMember(UUID member) {
        if (isSavedInDatabase()) {
            manager.rankChange(getName(), member.toString(), 0);
        }
        super.demoteModeratorToMember(member);
        sendBungeeWrite("DemoteToMember", getName(), member.toString());

    }

    @Override
    public void setDescription(String description) {
        super.setDescription(description);
        if (isSavedInDatabase()) manager.changeFieldStatus(getName());
        sendBungeeWrite("SetDescription", getName(), description);
    }

    @Override
    public void broadcastMessage(String message){
        super.broadcastMessage(message);
        sendBungeeWrite("BroadcastMessageToAll_", getName(), message);
    }
    @Override
    public void promoteModeratorToOwner(UUID member, UUID oldOwner) {
        if (isSavedInDatabase()) {
            manager.rankChange(getName(), member.toString(), 2);
        }
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

        if (isSavedInDatabase()) {
            PlayerChannels.getInstance().getSqlHandler().deleteChannel(getName());
        }
        super.delete();

        sendBungeeWrite("Delete", getName());

    }

    /**
     * @return if next global method should send a bungee write or not to avoid
     * infinite chain reaction
     */
    private boolean isRemoteCall() {
        return isRemoteCall;
    }

    /**
     * Send message to all servers
     * @param message
     */
    private void chatToAllServers(String message) {
        sendBungeeWrite(getName(), message);

    }


    public void turnOffRemote() {
        this.isRemoteCall = false;
    }

    private void enableRemote(){
        this.isRemoteCall = true;

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
            Player player = Iterables.getFirst(Bukkit.getOnlinePlayers(), null);
            // check if not null to avoid NPE
            if (player != null) player.sendPluginMessage(PlayerChannels.getInstance(), "BungeeCord", out.toByteArray());
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
