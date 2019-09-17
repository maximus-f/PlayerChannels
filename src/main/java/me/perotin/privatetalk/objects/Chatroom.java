package me.perotin.privatetalk.objects;

/* Created by Perotin on 8/14/19 */

import me.perotin.privatetalk.storage.Pair;
import me.perotin.privatetalk.storage.files.FileType;
import me.perotin.privatetalk.storage.files.PrivateFile;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Captures a chatroom object.
 */
public class Chatroom {

    /** @apiNote contains all members with their respective chat roles **/
    private Map<UUID, ChatRole> members;
    /**@apiNote used to identify chatroom me.perotin.privatetalk.objects. Distinct value.*/
    private final UUID uuid;
    // owner of chatroom
    private UUID owner;
    private String name;
    private String description;
    // true if chatroom is public, false if private
    private boolean isPublic;
    // true if saved, false if not
    private boolean isSaved;
    private List<UUID> bannedMembers;
    private HashMap<UUID, String> nickNames;
    private PrivateFile messages;

    public Chatroom(UUID owner, String name, String description, boolean isPublic, boolean isSaved) {
        this.members = new HashMap<>();
        this.members.put(owner, ChatRole.OWNER);
        this.owner = owner;
        this.name = name;
        this.description = description;
        this.isPublic = isPublic;
        this.uuid = UUID.randomUUID();
        this.messages = new PrivateFile(FileType.MESSAGES);
        this.isSaved = isSaved;
    }


    public Map<UUID, ChatRole> getMemberMap(){
        return members;
    }
    public Set<UUID> getMembers() {
        return members.keySet();
    }

    /**
     * @return List of all online players
     */
    public List<Player> getOnlinePlayers(){
       return getMembers().stream().map(Bukkit::getPlayer).filter(Objects::nonNull).collect(Collectors.toList());
    }

    /**
     * @return true if chatroom gets saved, false if not
     */
    public boolean isSaved() {
        return isSaved;
    }

    public void addMember(Pair<UUID, ChatRole> value) {
        this.members.put(value.getFirst(), value.getSecond());
    }

    public UUID getUuid() {
        return uuid;
    }

    public UUID getOwner() {
        return owner;
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

    public void setPublic(boolean aPublic) {
        isPublic = aPublic;
    }

    public List<UUID> getBannedMembers() {
        return bannedMembers;
    }

    public void setBannedMembers(List<UUID> bannedMembers) {
        this.bannedMembers = bannedMembers;
    }

    public HashMap<UUID, String> getNickNames() {
        return nickNames;
    }

    public void setNickNames(HashMap<UUID, String> nickNames) {
        this.nickNames = nickNames;
    }


    public boolean isInChatroom(UUID uuid){
        return getMembers().contains(uuid);
    }

    /**
     *
     * @param member of chatroom
     * @return string version of their role, being either member, moderator, or owner
     */
    public String getRole(UUID member){
        if(isInChatroom(member)){
            ChatRole value = getMemberMap().get(member);
            switch(value){
                case OWNER: messages.getString("owner");
                case MODERATOR: messages.getString("moderator");
                case MEMBER: messages.getString("member");
            }
        }
        return "";
    }
}
