package me.perotin.privatetalk.objects;

/* Created by Perotin on 8/14/19 */

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Captures a chatroom object.
 */
public class Chatroom {

    private List<UUID> members;
    /**@apiNote used to identify chatroom me.perotin.privatetalk.objects. Distinct value.*/
    private final UUID uuid;
    // owner of chatroom
    private UUID owner;
    private String name;
    private List<String> description;
    // true if chatroom is public, false if private
    private boolean isPublic;
    private List<UUID> bannedMembers;
    private HashMap<UUID, String> nickNames;

    public Chatroom(UUID owner, String name, List<String> description, boolean isPublic) {
        this.members = new ArrayList<>();
        this.members.add(owner);
        this.owner = owner;
        this.name = name;
        this.description = description;
        this.isPublic = isPublic;
        this.uuid = UUID.randomUUID();
    }


    public List<UUID> getMembers() {
        return members;
    }

    /**
     * @return List of all online players
     */
    public List<Player> getOnlinePlayers(){
       return getMembers().stream().map(Bukkit::getPlayer).filter(Objects::nonNull).collect(Collectors.toList());
    }

    public void setMembers(List<UUID> members) {
        this.members = members;
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

    public List<String> getDescription() {
        return description;
    }

    public void setDescription(List<String> description) {
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
}
