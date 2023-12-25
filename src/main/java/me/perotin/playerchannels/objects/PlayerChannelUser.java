package me.perotin.playerchannels.objects;

/* Created by Perotin on 8/14/19 */

import com.github.stefvanschie.inventoryframework.gui.type.ChestGui;
import me.perotin.playerchannels.PlayerChannels;
import me.perotin.playerchannels.storage.files.FileType;
import me.perotin.playerchannels.storage.files.ChannelFile;
import me.perotin.playerchannels.utils.ChannelUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Capture of a player instance with fields pertaining to chatrooms, statuses, and the like.
 */
public class PlayerChannelUser {

    private final UUID uuid;
    private String name;
    private List<Chatroom> chatrooms;

    private Chatroom focusedChatroom; //  null if none is focused, to decide which chatroom they are chatting in

    private List<Chatroom> listeningChatrooms;

    private List<Chatroom> invites; // Represent chatrooms that have invited this player

    private boolean isAcceptingInvites; // Toggle for if the player is accepting invites or not

    private String status;

    public PlayerChannelUser(UUID uuid, String name) {
        this.uuid = uuid;
        this.name = name;
        this.chatrooms = new ArrayList<>();
        this.status = "";
        this.invites = new ArrayList<>();
        this.listeningChatrooms = new ArrayList<>();
    }

    public PlayerChannelUser(UUID uuid, String name, String status, List<Chatroom> chatrooms) {
        this(uuid, name);
        this.chatrooms = chatrooms;
        this.status = status;
        // store invites correctly as another list
        this.invites = new ArrayList<>();
        this.invites = new ArrayList<>();


    }


    /**
     * @param player to show player profile to
     */
    public void showProfileTo(Player player){
        new ChestGui(6, getName() + "'s profile").show(player);

    }

    /**
     * @return List of channels that the user is currently listening too
     */
    public List<Chatroom> getListeningChatrooms() {
        return listeningChatrooms;
    }

    /**
     * Adds a chatroom to the set of channels that the player will ONLY receive incoming messages from
     * @param chatroom
     */
   public void addChannelToListen(Chatroom chatroom) {
        getListeningChatrooms().add(chatroom);
        PlayerChannels.getInstance().getListeningPlayers().add(getUuid());
        tellListeningChannels();

   }

    /**
     * Removes a chatroom to the set of channels that the player will ONLY receive incoming messages from
     * @param chatroom
     */
   public void removeChannelToListen(Chatroom chatroom) {
       getListeningChatrooms().remove(chatroom);
       if (getListeningChatrooms().isEmpty()) {
           PlayerChannels.getInstance().getListeningPlayers().remove(getUuid());
       }
       tellListeningChannels();
   }

   private void tellListeningChannels() {
       Player player = Bukkit.getPlayer(getUuid());
       ChannelFile messages = new ChannelFile(FileType.MESSAGES);
       if (getListeningChatrooms().isEmpty()){
           player.sendMessage(messages.getString("listening-to-channels-empty"));
           return;
       }
       player.sendMessage(new ChannelFile(FileType.MESSAGES).getString("listening-to-channel"));

       for (Chatroom chat : getListeningChatrooms()) {
           String name = chat.getName();
           if (!chat.getName().contains(ChatColor.COLOR_CHAR + "")){
               name = ChatColor.YELLOW + name;
           }
           player.sendMessage(ChatColor.GREEN + "- " + name);
       }
   }

    /**
     *
     * @param chatroom
     * @return if player is listening to mentioned chatroom
     */
   public boolean isListeningTo(Chatroom chatroom) {
        return getListeningChatrooms().contains(chatroom);
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
        Bukkit.getPlayer(getUuid()).sendMessage(new ChannelFile(FileType.MESSAGES).getString("player-join-message")
                .replace("$chatroom$", chatroom.getName()));
    }

    /**
     * @param chatroom to leave
     */
    public void leaveChatroom(Chatroom chatroom) {
        chatrooms.remove(chatroom);
        if (focusedChatroom != null && focusedChatroom.equals(chatroom)){
            setFocusedChatroom(null);
        }
        Bukkit.getPlayer(getUuid()).sendMessage(new ChannelFile(FileType.MESSAGES).getString("player-leave-message")
                .replace("$chatroom$", chatroom.getName()));
    }

    /**
     * Saves data to player file
     */
    public void savePlayer(){
        ChannelFile playerFile = new ChannelFile(FileType.PLAYERS);
        playerFile.set(uuid.toString()+".name", name);
        playerFile.set(uuid+".status", status);

        if(!getSavedChatrooms().isEmpty()) {
            playerFile.set(uuid + ".chatrooms", getSavedChatrooms().stream().map(Chatroom::getName).collect(Collectors.toList()));
        } else {
            playerFile.set(uuid + ".chatrooms", new ArrayList<>());

        }
        playerFile.save();

    }
    private List<Chatroom> getSavedChatrooms() {
        return getChatrooms().stream().filter(Chatroom::isSaved).collect(Collectors.toList());
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
    public static PlayerChannelUser getPlayer(UUID uuid){
        PlayerChannels instance = PlayerChannels.getInstance();
        for(PlayerChannelUser player : instance.getPlayers()){
            if(uuid.equals(player.getUuid())){
                return player;
            }
        }
        ChannelFile playerFile = new ChannelFile(FileType.PLAYERS);
        if (playerFile.getConfiguration().isSet(uuid.toString()+".name")) {
            String name = playerFile.getString(uuid + ".name");
            String status = playerFile.getString(uuid + ".status");
            // do some checking with chatrooms that aren't loaded
            List<String> chatrooms = playerFile.getConfiguration().getStringList(uuid + ".chatrooms");
            List<Chatroom> playerChatrooms = new ArrayList<>();
            for (String chatName : chatrooms) {
                Chatroom chatroom = ChannelUtils.getChatroomWith(chatName);
                if (chatroom != null) {
                    playerChatrooms.add(chatroom);
                }
            }

            return new PlayerChannelUser(uuid, name, status, playerChatrooms);
        } else {
            // New player so create new objectc
            return new PlayerChannelUser(uuid, Bukkit.getPlayer(uuid).getName());
        }
    }

}
