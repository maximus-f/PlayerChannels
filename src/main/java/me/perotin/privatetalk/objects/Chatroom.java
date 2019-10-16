package me.perotin.privatetalk.objects;

/* Created by Perotin on 8/14/19 */

import me.perotin.privatetalk.storage.Pair;
import me.perotin.privatetalk.storage.files.FileType;
import me.perotin.privatetalk.storage.files.PrivateFile;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Captures a chatroom object.
 */
public class Chatroom{

    /** @apiNote contains all members with their respective chat roles **/
    private Map<UUID, ChatRole> members;
    /**@apiNote used to identify chatroom me.perotin.privatetalk.objects. Distinct value.*/
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

    /**
     * Initial chatroom constructor
     */
    public Chatroom(UUID owner, String name, String description, boolean isPublic, boolean isSaved) {
        this.members = new HashMap<>();
        this.members.put(owner, ChatRole.OWNER);
        this.owner = owner;
        this.name = name;
        this.description = description;
        this.isPublic = isPublic;
        this.messages = new PrivateFile(FileType.MESSAGES);
        this.isSaved = isSaved;
    }

    /**
     * Used for loading a chatroom from file with members
     * @return chatroom
     */
    public Chatroom(UUID owner, String name, String description, boolean isPublic, boolean isSaved, Map<UUID, ChatRole> members) {
        this(owner, name, description, isPublic, isSaved);
        this.messages = new PrivateFile(FileType.MESSAGES);
        this.members = members;
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

    /**
     *Sets the nickname map
     */
    public void setNickNames(HashMap<UUID, String> nickNames) {
        this.nickNames = nickNames;
    }


    /**
     * Checks if a memmber is in this chatroom object
     */
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

    /**
     * @apiNote Used to save a chatroom to chatrooms.yml
     */
    public void saveToFile(){
        PrivateFile chatrooms = new PrivateFile(FileType.CHATROOM);
        chatrooms.set(name+".status", isPublic);
        chatrooms.set(name+".saved", isSaved);
        chatrooms.set(name+".owner", getOwner().toString());
        chatrooms.set(name+".description", description);
        chatrooms.getConfiguration().createSection(name+".members", getMemberMap());
        chatrooms.save();
    }


    /**
     * @param name of chatroom to load
     * @return chatroom object
     *
     */
    public static Chatroom loadChatroom(String name){
        PrivateFile chatrooms = new PrivateFile(FileType.CHATROOM);
        String description = chatrooms.getString(name+".description");
        UUID owner = UUID.fromString(chatrooms.getString(name+".owner"));
        boolean saved = chatrooms.getBool(name+".saved");
        boolean isPublic = chatrooms.getBool(name+".status");
        ConfigurationSection sec = chatrooms.getConfiguration().getConfigurationSection(name+".members");
        Map<UUID, ChatRole> loadedRoles = new HashMap<>();
        for(String key : sec.getKeys(false)){
            String role = sec.get(key).toString();
            ChatRole roleO = ChatRole.valueOf(role);
            UUID uuid = UUID.fromString(key);
            loadedRoles.put(uuid, roleO);
        }
        return null;

    }


}
