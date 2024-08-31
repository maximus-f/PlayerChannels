package me.perotin.playerchannels.objects;

/* Created by Perotin on 8/14/19 */

import com.github.stefvanschie.inventoryframework.gui.type.ChestGui;
import me.perotin.playerchannels.PlayerChannels;
import me.perotin.playerchannels.storage.files.FileType;
import me.perotin.playerchannels.storage.files.ChannelFile;
import me.perotin.playerchannels.utils.ChannelUtils;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

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

    /**
     *  New PlayerChannelUser constructor for first-time creation
     * @param uuid
     * @param name
     */
    public PlayerChannelUser(UUID uuid, String name) {
        this.uuid = uuid;
        this.name = name;
        this.chatrooms = new ArrayList<>();
        this.status = "";
        this.invites = new ArrayList<>();
        this.listeningChatrooms = new ArrayList<>();
        this.isAcceptingInvites = true;
    }

    public PlayerChannelUser(UUID uuid, String name, String status, List<Chatroom> chatrooms) {
        this(uuid, name);
        this.chatrooms = chatrooms;
        this.status = status;
        // store invites correctly as another list
        this.invites = new ArrayList<>();
        this.isAcceptingInvites = true;


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


   // Helper method to inform player of which channels they are currently listening to
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
     * @return List of chatrooms where the player has at least moderator permissions
     */
    public List<Chatroom> getChatroomsWithModeratorPermissions() {
        return chatrooms.stream().filter(chatroom -> chatroom.getRole(getUuid()).getValue() >= ChatRole.MODERATOR.getValue()).collect(Collectors.toList());

    }

    /**
     * @return true or false if player has a pending invite from specified chatroom
     */

    public boolean hasPendingInviteFrom(Chatroom chat) {
        return getInvites().contains(chat);
    }

    /**
     * @param chatroom to add to their list
     */
    public void addChatroom(Chatroom chatroom) {
        if(!chatrooms.contains(chatroom)) chatrooms.add(chatroom);
        Player player = Bukkit.getPlayer(getUuid());
        ChannelFile msgs = new ChannelFile(FileType.MESSAGES);
        if (player != null) {
            player.sendMessage(msgs.getString("player-join-message")
                    .replace("$chatroom$", chatroom.getName()));

            String message = msgs.getString("join-focus-chat-tip")
                    .replace("$chatroom$", ChatColor.stripColor(chatroom.getName()));
            TextComponent messageComponent = new TextComponent(ChatColor.translateAlternateColorCodes('&', message));
            messageComponent.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/playerchannels " + chatroom.getName()));
            messageComponent.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(ChatColor.GREEN + "Click to chat in " + chatroom.getName()).create()));
            player.spigot().sendMessage(messageComponent);
        }
    }

    /**
     * @param chatroom to leave
     */
    public void leaveChatroom(Chatroom chatroom) {
        chatrooms.remove(chatroom);
        if (focusedChatroom != null && focusedChatroom.equals(chatroom)){
            setFocusedChatroom(null);
        }
        if (Bukkit.getPlayer(getUuid()) != null) {
            Bukkit.getPlayer(getUuid()).sendMessage(new ChannelFile(FileType.MESSAGES).getString("player-leave-message")
                    .replace("$chatroom$", chatroom.getName()));
        }
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

    /**
     * @return only persistent channels
     */
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

    public void sendMessage(String message) {
        if (Bukkit.getPlayer(getUuid()) != null){
            Bukkit.getPlayer(getUuid()).sendMessage(message);
        }
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
     * @param focusedChatroom sets the playeres current focused chatroom
     */
    public void setFocusedChatroom(Chatroom focusedChatroom) {


        this.focusedChatroom = focusedChatroom;
        if (focusedChatroom != null) {
            Bukkit.getPlayer(getUuid()).spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(ChatColor.GREEN + "Focused Channel: " + ChatColor.YELLOW + focusedChatroom.getName()));

        } else {
            Bukkit.getPlayer(getUuid()).spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(ChatColor.GREEN + "Focused Channel: " + ChatColor.RED + "None"));

        }

    }

    public String getStatus() {
        return status;
    }


    /**
     * @return size of channels that player is owner of
     */
    public int getOwnedChannelsSize() {
        return getChatrooms().stream().filter(c -> c.getOwner() != null && c.getOwner().equals(getUuid())).collect(Collectors.toList()).size();
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

            PlayerChannelUser user =  new PlayerChannelUser(uuid, name, status, playerChatrooms);
            PlayerChannels.getInstance().addPlayer(user);
            return user;
        } else {
            // New player so create new objectc
            if (Bukkit.getPlayer(uuid) != null) {
                PlayerChannelUser user = new PlayerChannelUser(uuid, Bukkit.getPlayer(uuid).getName());
                PlayerChannels.getInstance().addPlayer(user);
                return user;
            } else if (Bukkit.getOfflinePlayer(uuid).getName() != null){
                PlayerChannelUser user = new PlayerChannelUser(uuid, Bukkit.getOfflinePlayer(uuid).getName());
                PlayerChannels.getInstance().addPlayer(user);
                return user;

            } else {
                // Have never played on this server before, Bungeecord moment
                PlayerChannelUser user = new PlayerChannelUser(uuid, "-1");
                PlayerChannels.getInstance().addPlayer(user);
                return user;


            }
        }
    }


}
