package me.perotin.privatetalk.objects;

/* Created by Perotin on 9/22/19 */

import java.util.UUID;

/**
 * Object to hold data values for a chatroom in the process of being created
 */
public class PreChatroom {

    private String name;
    private String description;
    private boolean isPublic;
    private boolean isSaved;
    private UUID owner;

    public PreChatroom(UUID owner){
        this.owner = owner;
        this.name = "";
        this.description="";
        this.isPublic = false;
        this.isSaved = false;
    }

    public PreChatroom(){
        this(null);
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

    public Chatroom toChatroom(){
        return new Chatroom(owner, name, description, isPublic, isSaved);
    }
}
