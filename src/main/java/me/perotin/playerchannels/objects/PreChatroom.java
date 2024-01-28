package me.perotin.playerchannels.objects;

/* Created by Perotin on 9/22/19 */

import java.util.UUID;

/**
 * Object to hold data values for a chatroom in the process of being created
 */
public class PreChatroom {

    private String name;
    private String description;
    private boolean isPublic = false;
    private boolean isSaved = false;
    private UUID owner;

    private boolean global = false;

    private boolean isServerOwned = false;

    public PreChatroom(UUID owner){
        this.owner = owner;
        this.name = "";
        this.description = "";
        this.isPublic = false;
        this.isSaved = false;
    }

    public PreChatroom(){
        this(null);
    }


    public boolean isGlobal() {
        return global;
    }

    public void setGlobal(boolean global) {
        this.global = global;
    }

    public UUID getOwner() {
        return owner;
    }

    public boolean isServerOwned() {
        return isServerOwned;
    }

    public void setServerOwned(boolean serverOwned) {
        isServerOwned = serverOwned;
    }

    public void setOwner(UUID owner) {
        this.owner = owner;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isPublic() {
        return isPublic;
    }

    public void setPublic(boolean isPublic) {
        this.isPublic = isPublic;
    }

    public boolean isSaved() {
        return isSaved;
    }

    public void setSaved(boolean saved) {
        isSaved = saved;
    }

    /**
     * @return a chatroom
     */
    public Chatroom toChatroom(){

        if (isGlobal()){
            GlobalChatroom c = new GlobalChatroom(owner, name, description, isPublic, isSaved, isServerOwned);
            c.writeToAllServers();
            return c;
        } else {
            return new Chatroom(owner, name, description, isPublic, isSaved, isServerOwned);
        }
    }
}
