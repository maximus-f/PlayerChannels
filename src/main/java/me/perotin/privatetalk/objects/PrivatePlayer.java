package me.perotin.privatetalk.objects;

/* Created by Perotin on 8/14/19 */

import me.perotin.privatetalk.storage.files.FileType;
import me.perotin.privatetalk.storage.files.PrivateFile;
import org.bukkit.entity.Player;

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


    public PrivatePlayer(UUID uuid, String name) {
        this.uuid = uuid;
        this.name = name;
    }

    public PrivatePlayer(UUID uuid, String name, List<Chatroom> chatrooms) {
        this(uuid, name);
        this.chatrooms = chatrooms;
    }

    /**
     * @param player to show player profile to
     */
    public void showProfileTo(Player player){

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
     * Saves data to player file
     */
    public void savePlayer(){
        PrivateFile playerFile = new PrivateFile(FileType.PLAYERS);
        playerFile.set(uuid.toString()+".name", name);
        playerFile.set(uuid.toString()+".name", name);
        playerFile.set(uuid.toString()+".chatrooms", getChatrooms().stream().map(Chatroom::getName).collect(Collectors.toList()));
        playerFile.save();

    }

    /**
     *
     * @param uuid
     * @return PrivatePlayer object
     */
    public static PrivatePlayer getPlayer(UUID uuid){
        PrivateFile playerFile = new PrivateFile(FileType.PLAYERS);
        return null;
    }
}
