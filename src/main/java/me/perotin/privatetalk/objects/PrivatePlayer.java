package me.perotin.privatetalk.objects;

/* Created by Perotin on 8/14/19 */

import java.util.List;
import java.util.UUID;

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


    public UUID getUuid() {
        return uuid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<Chatroom> getChatrooms() {
        return chatrooms;
    }


    public void addChatroom(Chatroom chatroom) {
        if(!chatrooms.contains(chatroom)) chatrooms.add(chatroom);
    }
}
