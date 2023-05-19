package me.perotin.privatetalk.objects;

/* Created by Perotin on 8/14/19 */

import com.github.stefvanschie.inventoryframework.gui.type.ChestGui;
import me.perotin.privatetalk.PrivateTalk;
import me.perotin.privatetalk.storage.files.FileType;
import me.perotin.privatetalk.storage.files.PrivateFile;
import me.perotin.privatetalk.utils.PrivateUtils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Capture of a player instance with fields pertaining to chatrooms, statuses, and the like.
 */
public class PrivatePlayer {

    private final UUID uuid;
    private String name;
    private List<Chatroom> chatrooms;

    private Chatroom focusedChatroom; //  null if none is focused

    private List<Chatroom> invites; // Represent chatrooms that have invited this player

    private boolean isAcceptingInvites; // Toggle for if the player is accepting invites or not

    private String status;

    public PrivatePlayer(UUID uuid, String name) {
        this.uuid = uuid;
        this.name = name;
        this.chatrooms = new ArrayList<>();
        this.status = "";
    }

    public PrivatePlayer(UUID uuid, String name, List<Chatroom> chatrooms) {
        this(uuid, name);
        this.chatrooms = chatrooms;
        this.status = "";
    }


    /**
     * @param player to show player profile to
     */
    public void showProfileTo(Player player){
        new ChestGui(6, getName() + "'s profile").show(player);

    }

    /**
     * @return player uuid
     */
    public UUID getUuid() {
        return uuid;
    }

    /**
     * @return player name
     */
    public String getName() {
        return name;
    }

    /**
     * @param name to set, in case of a name change
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * @return all chatrooms that player is a a part of
     */
    public List<Chatroom> getChatrooms() {
        return chatrooms;
    }


    /**
     * @param chatroom to add to their list
     */
    public void addChatroom(Chatroom chatroom) {
        if(!chatrooms.contains(chatroom)) chatrooms.add(chatroom);
    }

    /**
     * @param chatroom to leave
     */
    public void leaveChatroom(Chatroom chatroom) {
        chatrooms.remove(chatroom);
        if (focusedChatroom != null && focusedChatroom.equals(chatroom)){
            setFocusedChatroom(null);
        }
    }

    /**
     * Saves data to player file
     */
    public void savePlayer(){
        PrivateFile playerFile = new PrivateFile(FileType.PLAYERS);
        playerFile.set(uuid.toString()+".name", name);
        playerFile.set(uuid.toString()+".chatrooms", getChatrooms().stream().map(Chatroom::getName).collect(Collectors.toList()));
        playerFile.save();

    }

    /**
     * @param chatroom to test
     * @return whether is a member of
     */
    public boolean isMemberOf(Chatroom chatroom) {
        return chatrooms.contains(chatroom);
    }

    /**
     * @return chatroom that the player is currently chatting in
     */
    public Chatroom getFocusedChatroom() {
        return focusedChatroom;
    }

    /**
     * @return list of chatrooms that have invited this player
     */
    public List<Chatroom> getInvites() {
        return invites;
    }

    /**
     * @param invite chatroom to add
     */
    public void addInvite(Chatroom invite) {
        this.invites.add(invite);
    }

    /**
     *
     * @return if they're accepting invites or not
     */
    public boolean isAcceptingInvites() {
        return isAcceptingInvites;
    }

    /**
     *
     * @param acceptingInvites toggle whether they are accepting invites or not
     */
    public void setAcceptingInvites(boolean acceptingInvites) {
        isAcceptingInvites = acceptingInvites;
    }
    /**
     *
     * @param sets the playeres current focused chatroom
     */
    public void setFocusedChatroom(Chatroom focusedChatroom) {
        this.focusedChatroom = focusedChatroom;
    }

    public String getStatus() {
        return status;
    }


    public void setStatus(String status) {
        this.status = status;
    }

    /**
     *
     * @param uuid
     * @return PrivatePlayer object
     */
    public static PrivatePlayer getPlayer(UUID uuid){
        PrivateTalk instance = PrivateTalk.getInstance();
        for(PrivatePlayer player : instance.getPlayers()){
            if(uuid.equals(player.getUuid())){
                return player;
            }
        }
        PrivateFile playerFile = new PrivateFile(FileType.PLAYERS);
        if (playerFile.getConfiguration().isSet(uuid.toString()+".name")) {
            String name = playerFile.getString(uuid.toString() + ".name");
            // do some checking with chatrooms that aren't loaded
            List<Chatroom> chatrooms = playerFile.getConfiguration().getStringList(uuid.toString() + ".chatrooms")
                    .stream().map(PrivateUtils::getChatroomWith).collect(Collectors.toList());

            return new PrivatePlayer(uuid, name, chatrooms);
        } else {
            // New player so create new object
            return new PrivatePlayer(uuid, Bukkit.getPlayer(uuid).getName());
        }
    }

}
