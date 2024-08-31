package me.perotin.playerchannels.objects;

/* Created by Perotin on 8/14/19 */

import me.perotin.playerchannels.PlayerChannels;
import me.perotin.playerchannels.storage.Pair;
import me.perotin.playerchannels.storage.files.FileType;
import me.perotin.playerchannels.storage.files.ChannelFile;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.hover.content.Text;
import org.bukkit.*;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.*;
import java.util.stream.Collectors;

import static me.perotin.playerchannels.storage.files.FileType.CHATROOM;
import static me.perotin.playerchannels.storage.files.FileType.MESSAGES;

/**
 * Captures a chatroom object.
 */
public class Chatroom {

    /** @apiNote contains all members with their respective chat roles **/
    private Map<UUID, ChatRole> members;
    // owner of chatroom
    private UUID owner;
    private String name;
    private String description;
    // true if chatroom is public, false if private
    private boolean isPublic;
    // true if saved, false if not
    /**
     * @hidden - does channel appear in list and channels command, need playerchannels.hidden to view
     */
    private boolean isSaved, nicknamesEnabled, isServerOwned, isGlobal, hidden;
    private List<UUID> bannedMembers;

    private List<UUID> mutedMembers;

    private Map<UUID, String> nickNames;
    private ChannelFile messages;

    /** List of UUIDs for admins spying**/
    private Set<UUID> spyers;
    private ItemStack display;




    /**
     * Players that are currently chatting in the chatroom
     */

    /**
     * Initial chatroom constructor
     */
    public Chatroom(UUID owner, String name, String description, boolean isPublic, boolean isSaved, boolean isServerOwned, boolean isGlobal, boolean hidden) {
        this.members = new HashMap<>();
        this.members.put(owner, ChatRole.OWNER);
        this.owner = owner;
        this.name = name;
        this.description = description;
        this.isPublic = isPublic;
        this.nicknamesEnabled = true;
        this.isGlobal = isGlobal;
        this.hidden = false;
        this.messages = new ChannelFile(MESSAGES);
        this.isSaved = isSaved;
        this.display = generateItem();
        this.mutedMembers = new ArrayList<>();
        this.bannedMembers = new ArrayList<>();
        this.nickNames = new HashMap<>();
        this.spyers = new HashSet<>();
        this.isServerOwned = isServerOwned;
    }

    /**
     * Used for loading a chatroom from file with members
     * @return chatroom
     */
    public Chatroom(UUID owner, String name, String description, boolean isPublic, boolean isSaved, boolean isServerOwned, boolean isGlobal, Map<UUID, ChatRole> members, List<UUID> bannedMembers, List<UUID> mutedMembers, Map<UUID, String> nicknames, boolean nicknamesenabled ) {
        this(owner, name, description, isPublic, isSaved, isServerOwned, isGlobal, false);
        this.messages = new ChannelFile(MESSAGES);
        this.members = members;
        this.bannedMembers = bannedMembers;
        this.mutedMembers = mutedMembers;
        this.nickNames = nicknames;
        this.nicknamesEnabled = nicknamesenabled;
        this.spyers = new HashSet<>();


    }

    /**
     * @return Set of all Moderators
     */
    public List<UUID> getModerators(){
        List<UUID> moderators = new ArrayList<>();
        for(UUID uuid : members.keySet()){
            if(members.get(uuid) == ChatRole.MODERATOR){
                moderators.add(uuid);
            }
        }
        return moderators;
    }

    public Map<UUID, ChatRole> getMemberMap(){
        return members;
    }
    public Set<UUID> getMembers() {
        return members.keySet();
    }

    public boolean isMuted(UUID uuid) {
        return mutedMembers.contains(uuid);
    }
    /**
     * @return List of all online players
     */
    public List<Player> getOnlinePlayers(){
       return getMembers().stream().map(Bukkit::getPlayer).filter(Objects::nonNull).collect(Collectors.toList());
    }


    /**
     * @return if global
     */
    public boolean isGlobal() {
        return isGlobal;
    }

    /**
     * Called only once to generate the itemstack to represent the chatroom in player-profiles and the main menu
     * @return Itemstack representation of the chatroom
     */
    private ItemStack generateItem(){
        ChannelFile messages = new ChannelFile(FileType.MENUS);
        ChannelFile msgs = new ChannelFile(MESSAGES);

        ItemStack item;
        if (isServerOwned()){
            String serverString = PlayerChannels.getInstance().getConfig().getString("server-channel-material");
            Material server;
            try {
                server = Material.valueOf(serverString);
            } catch (IllegalArgumentException ex){
                // exception handling
                Bukkit.getLogger().severe("The material id for the server-channel-material key in /PlayerChannels/config.yml is invalid! Make sure to use a correct value.");
                return null;
            }
            item = new ItemStack(server);

        } else if(isSaved()){
           String savedString = PlayerChannels.getInstance().getConfig().getString("saved-material");

           Material saved;
           try {
                saved = Material.valueOf(savedString);
           } catch (IllegalArgumentException ex){
               // exception handling
               Bukkit.getLogger().severe("The material id for the saved-material key in /PlayerChannels/config.yml is invalid! Make sure to use a correct value.");
               return null;
           }
             item = new ItemStack(saved);

        } else {
            // player skin head
            item = new ItemStack(Material.PLAYER_HEAD);
            SkullMeta skull = (SkullMeta) item.getItemMeta();
            skull.setOwningPlayer(Bukkit.getOfflinePlayer(getOwner()));
            item.setItemMeta(skull);
        }
        ItemMeta itemMeta = item.getItemMeta();
        if (!isServerOwned()) {
            String displayName = isGlobal() ? messages.getString("global-chatroom.name") : messages.getString("chatroom-items.name");
            String description = isGlobal() ? messages.getString("global-chatroom.description") : messages.getString("chatroom-items.description");
            String status = isGlobal() ? messages.getString("global-chatroom.status") : messages.getString("chatroom-items.status");
            String owner = isGlobal() ? messages.getString("global-chatroom.owner") : messages.getString("chatroom-items.owner");
            String members = isGlobal() ? messages.getString("global-chatroom.members") : messages.getString("chatroom-items.members");

            displayName = displayName.replace("$name$", getName());
            displayName = isHidden() ?  displayName.replace("$hidden$", msgs.getString("list-message-4")) : displayName.replace("$hidden$", "");
            itemMeta.setDisplayName(displayName);

            List<String> lores = new ArrayList<>();
            if (!(getDescription().trim().isEmpty() || getDescription() == null)) {
                lores.add(description.replace("$description$", getDescription()));
            } else {

            }
           lores.add(status.replace("$status$", getStringStatus()));
            lores.add(owner.replace("$owner$", Bukkit.getOfflinePlayer(getOwner()).getName()));
            lores.add(members.replace("$member_count$", getMembers().size() + ""));
            itemMeta.setLore(lores);
        } else {
            String displayName = messages.getString("server-chatroom-items.name").replace("$name$", getName());
            displayName = isHidden() ?  displayName.replace("$hidden$", msgs.getString("list-message-4")) : displayName.replace("$hidden$", "");

            itemMeta.setDisplayName(displayName);

            List<String> lores = new ArrayList<>();
            if (!(getDescription().trim().isEmpty() || getDescription() == null)) {
                lores.add(messages.getString("server-chatroom-items.description").replace("$description$", getDescription()));
            } else {

            }
                    lores.add(messages.getString("server-chatroom-items.status").replace("$status$", getStringStatus()));
                   lores.add(messages.getString("server-chatroom-items.members").replace("$member_count$", getMembers().size() + ""));
                   itemMeta.setLore(lores);
        }
        NamespacedKey chatroomName = new NamespacedKey(PlayerChannels.getInstance(), "chatroomName");  // 'plugin' is your JavaPlugin instance

        // Set the custom data
        itemMeta.getPersistentDataContainer().set(chatroomName, PersistentDataType.STRING, getName());

        item.setItemMeta(itemMeta);



        return item;
    }


    /**
     * @return boolean if shown in channel/list command or not
     */
    public boolean isHidden() {
        return hidden;
    }

    public void setHidden(boolean hidden) {
        this.hidden = hidden;
    }

    /**
     * @return String form of the status
     */
    private String getStringStatus(){
        ChannelFile messages = new ChannelFile(MESSAGES);
        if(isPublic){
            return messages.getString("public");
        } else {
            return messages.getString("private");
        }
    }

    public boolean isNicknamesEnabled() {
        return nicknamesEnabled;
    }

    public void setNicknamesEnabled(boolean nicknamesEnabled) {
        this.nicknamesEnabled = nicknamesEnabled;
    }

    /**
     * @return true if chatroom gets saved, false if not
     */

    public boolean isSaved() {
        return isSaved;
    }


    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * @return Item representation of the chatroom
     */
    public ItemStack getItem(){
        return generateItem();
    }

    public ItemStack getItemForDeletion(){
        ItemStack item = generateItem();
        ItemMeta meta = item.getItemMeta();
        List<String> lores = new ArrayList<>();
        lores.add(ChatColor.GRAY + "Click to " + ChatColor.RED + ChatColor.BOLD + "DELETE");

        meta.setLore(lores);
        item.setItemMeta(meta);
        return item;
    }

    public ItemStack getItemForSpy(UUID admin){
        ItemStack item = generateItem();
        ItemMeta meta = item.getItemMeta();
        List<String> lores = new ArrayList<>();
        if (getSpyers().contains(admin)) {
            lores.add(ChatColor.GRAY + "Click to " + ChatColor.RED + ChatColor.BOLD + "UNSPY");
        } else {
            lores.add(ChatColor.GRAY + "Click to " + ChatColor.GREEN + ChatColor.BOLD + "SPY");

        }

        meta.setLore(lores);
        item.setItemMeta(meta);
        return item;
    }

    /**
     * When a new player joins the chatroom
     * @param value
     */
    public void addMember(Pair<UUID, ChatRole> value, String addedPlayerName) {
        this.members.put(value.getFirst(), value.getSecond());

        if (addedPlayerName.equalsIgnoreCase("")) {
            addedPlayerName = Bukkit.getPlayer(value.getFirst()).getName();
        }
        String finalAddedPlayerName = addedPlayerName;
        getOnlinePlayers().forEach(p->p.sendMessage(new ChannelFile(MESSAGES).getString("new-player-join")
                .replace("$name$", finalAddedPlayerName)
                .replace("$chatroom$", getName())));
    }

    /**
     * Chatroom method for removing a member or when a member leaves
     *
     * @param key to remove
     */
    public void removeMember(UUID key) {
        this.members.remove(key);
        Player remove = Bukkit.getPlayer(key);
        String playerName;
        if (remove != null) {
            playerName = remove.getName();
        } else if (Bukkit.getOfflinePlayer(key).getName() != null) {
            playerName = Bukkit.getOfflinePlayer(key).getName();
        } else {
            playerName = "";
        }

        getOnlinePlayers().forEach(p->p.sendMessage(new ChannelFile(MESSAGES).getString("player-leave")
                .replace("$name$", playerName)
                .replace("$chatroom$", getName())));
        if (getMembers().isEmpty() && !isSaved() && !isServerOwned()){
            PlayerChannels.getInstance().getChatrooms().remove(this);
        }
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


    public boolean isBanned(UUID uuid){
        return bannedMembers.contains(uuid);
    }

    /**
     * @return if channel can be joined by anyone
     */
    public boolean isPublic() {
        return isPublic;
    }

    public void setPublic(boolean aPublic) {
        isPublic = aPublic;
    }

    public List<UUID> getBannedMembers() {
        return bannedMembers;
    }

    /**
     * @return if server owned, i.e. no player owner
     */
    public boolean isServerOwned() {
        return isServerOwned;
    }

    public Map<UUID, String> getNickNames() {
        return nickNames;
    }

    /**
     * Main method for sending a chatroom message to all participents of the chatroom
     * that are currently listening to messages
     *
     * @param message
     */
    public String chat(String sender, String message, UUID id){
        List<Player> members = getOnlinePlayers();
        // Perform operations to format message accordingly
        String nickname = sender;
        if (getNickNames().containsKey(id) && isNicknamesEnabled()) {
            nickname = ChatColor.translateAlternateColorCodes('&', getNickNames().get(id));
        }
        // TODO need to make this a bit more complex, for example
        // if a nickname does not exist, it should default to their regular name.
        String chatroomFormat = ChatColor.translateAlternateColorCodes('&', PlayerChannels.getInstance().getConfig().getString("chatroom-message-format")
                .replace("$chatroom$", getName())
                .replace("$message$", message)
                        .replace("$role$", getStringRole(id))
                .replace("$name$", sender))
                        .replace("$nickname$", nickname);

        // Check for ping message
        Set<String> pings = new HashSet<>();
        if (chatroomFormat.contains("@")) {
            String[] words = chatroomFormat.split(" ");
            for (String word : words) {
                if (word.startsWith("@")) {
                    // Strip colors and extract the pinged username
                    String pingedPlayer = ChatColor.stripColor(word.substring(1));
                    pings.add(pingedPlayer.toLowerCase()); // Use lower case for case-insensitive comparison
                }
            }
        }

        for (UUID spy : getSpyers()) {
            if (Bukkit.getPlayer(spy) != null) {
                Bukkit.getPlayer(spy).sendMessage(ChatColor.GRAY + "[Spy] " + chatroomFormat);

            }
        }

        FileConfiguration config = PlayerChannels.getInstance().getConfig();
        TextComponent jsonMessage = new TextComponent(TextComponent.fromLegacyText(chatroomFormat));

        // Potential make this cleaner later
        jsonMessage.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text(ChatColor.translateAlternateColorCodes('&',
                config.getString("hover-text")
                        .replace("$world$", Bukkit.getPlayer(id).getWorld().getName())))));

        boolean useJson = config.getBoolean("use-json");
        for (Player member : members) {
            // Send if not listening or is listening explicity to this one
            if (!PlayerChannels.getInstance().getListeningPlayers().contains(member.getUniqueId()) || PlayerChannelUser.getPlayer(member.getUniqueId()).getListeningChatrooms().contains(this)){

               // Bukkit.broadcastMessage("Ping: " + ping);
               if (!pings.isEmpty() && pings.contains(member.getName().toLowerCase())) {
                   String highlightedMessage = chatroomFormat;
                   for (String ping : pings) {
                       if (member.getName().equalsIgnoreCase(ping)) {
                           String pingText = "@" + ping;
                           highlightedMessage = highlightedMessage.replaceAll("(?i)" + pingText, ChatColor.GOLD + pingText + ChatColor.YELLOW);
                           member.playSound(member.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 1.0f);
                       }
                   }
                   member.sendMessage(highlightedMessage);
               } else {

                   if (useJson) { member.spigot().sendMessage(jsonMessage);
                   } else {
                       member.sendMessage(chatroomFormat);
                   }
               }
            }
        }
        return chatroomFormat;
    }

    /**
     * Simplified chat method for bungee messaging that already has completed message
     * @param message
     */
    public void chat (String message) {
        for (Player player : getOnlinePlayers()) {
            player.sendMessage(message);
        }
    }



    public void addSpy(UUID uuid) {
        getSpyers().add(uuid);
    }

    public void removeSpy(UUID uuid) {
        getSpyers().remove(uuid);
    }
    /**
     * Broadcast any message to the entire chatroom
     * @param message
     */
    public void broadcastMessage(String message) {
        List<Player> members = getOnlinePlayers();
        members.forEach(member -> member.sendMessage(message));

    }

    public List<UUID> getMutedMembers() {
        return mutedMembers;
    }

    /**
     * @param uuid of player to mute within the chatrrom
     */
    public void mute(UUID uuid) {
        mutedMembers.add(uuid);
    }

    /**
     * Returns only members, no moderators or owner
     */
    public List<UUID> getMembersOnly() {
        List<UUID> members = new ArrayList<>();
        for (UUID key : getMemberMap().keySet()) {
            if (!members.contains(key) && getMemberMap().get(key) == ChatRole.MEMBER) {
                members.add(key);
            }
        }
        return members;
    }

    /**
     * @param uuid of player to mute within the chatrrom
     */
    public void unmute(UUID uuid) {
        mutedMembers.remove(uuid);
    }
    /**
     * @param uuid to ban
     */
    public void ban(UUID uuid) {
        if (!bannedMembers.contains(uuid)) {
            bannedMembers.add(uuid);
        }
        removeMember(uuid);
    }


    /**
     * Sets a nickname for a specific player
     * @param uuid
     * @param nickname
     */
    public void setNickname(UUID uuid, String nickname) {
        getNickNames().put(uuid, nickname);
    }

    /**
     * @param member to promote to channel moderator
     */
    public void promoteMemberToModerator(UUID member) {
        updateRoleFor(member, ChatRole.MODERATOR);
    }

    /**
     * @param member to demote from moderator to member
     */
    public void demoteModeratorToMember(UUID member) {
        updateRoleFor(member, ChatRole.MEMBER);
    }

    /**
     * @param member
     * @param oldOwner
     *
     * Promote moderator to owner and demote owner to moderator
     */
    public void promoteModeratorToOwner(UUID member, UUID oldOwner) {
        updateRoleFor(member, ChatRole.OWNER);
        updateRoleFor(oldOwner, ChatRole.MODERATOR);
        setOwner(member);
    }

    private void updateRoleFor(UUID uuid, ChatRole role) {
        getMemberMap().remove(uuid);
        getMemberMap().put(uuid, role);
    }

    public boolean hasModeratorPermissions(UUID uuid) {
        return getRole(uuid) == ChatRole.MODERATOR || getRole(uuid) == ChatRole.OWNER;
    }

    /**
     * @return Admins spying on the chatroom
     */
    public Set<UUID> getSpyers() {
        return spyers;
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
    public String getStringRole(UUID member){
        FileConfiguration config = PlayerChannels.getInstance().getConfig();
        String role = "";
        if(isInChatroom(member)){
            ChatRole value = getMemberMap().get(member);
            switch(value){
                case OWNER:
                    if (isServerOwned()){
                        role = ChatColor.translateAlternateColorCodes('&', config.getString("server-channel-admin"));

                    } else {
                        role = messages.getString("owner");
                    }
                break;
                case MODERATOR:
                    if (isServerOwned()){
                        role = ChatColor.translateAlternateColorCodes('&', config.getString("server-channel-mod"));

                    } else {
                        role = messages.getString("moderator");
                    }                break;
                case MEMBER: role = messages.getString("member");
                break;
            }
        }
        return role;
    }

    /**
     * @apiNote Used to save a chatroom to chatrooms.yml
     */
    /**
     * owner
     * name
     * description
     * ispublic
     * isSaved
     * nicknamesEnabled
     * bannedMmebers
     * mutedMembers
     * nicknames
     *
     */
    public void saveToFile() {
        ChannelFile chatrooms = new ChannelFile(FileType.CHATROOM);
        HashMap<String, Integer> mappedToInt = new HashMap<>();
        for (UUID uuid : getMemberMap().keySet()) {
            mappedToInt.put(uuid.toString(), getMemberMap().get(uuid).getValue());
        }

        HashMap<String, String> stringNickNames = new HashMap<>();
        for (UUID uuid : nickNames.keySet()) {
            stringNickNames.put(uuid.toString(), nickNames.get(uuid));
        }

        List<String> stringBannedMembers = bannedMembers.stream().map(UUID::toString).collect(Collectors.toList());
        List<String> stringMutedMembers = mutedMembers.stream().map(UUID::toString).collect(Collectors.toList());

        chatrooms.set(name+".status", isPublic);
        chatrooms.set(name+".saved", isSaved);
        chatrooms.set(name+".isServerOwned", isServerOwned);
        chatrooms.set(name+".nicknamesEnabled", nicknamesEnabled);
        chatrooms.set(name+".owner", getOwner().toString());
        chatrooms.set(name+".description", description);
        chatrooms.set(name+".nicknames", stringNickNames);
        chatrooms.set(name+".banned", stringBannedMembers);
        chatrooms.set(name+".muted", stringMutedMembers);
        chatrooms.getConfiguration().createSection(name+".members", mappedToInt);
        chatrooms.save();
    }


    /**
     * Chatroom names are unique so this is a good way for checking this.
     * @param toCheck
     * @return boolean if chatroom name are equal
     */
    public boolean equals(Chatroom toCheck) {
        return this.name.equals(toCheck.getName());
    }

    /**
     * @param name of chatroom to load
     * @return chatroom object
     *
     */
    public static Chatroom loadChatroom(String name){
        ChannelFile chatrooms = new ChannelFile(FileType.CHATROOM);
        if(chatrooms.getConfiguration().contains(name)) {
            String description = chatrooms.getString(name + ".description");
            UUID owner = UUID.fromString(chatrooms.getString(name + ".owner"));
            boolean saved = chatrooms.getBool(name + ".saved");
            boolean isPublic = chatrooms.getBool(name + ".status");
            boolean nicknamesEnabled = chatrooms.getBool(name+".nicknamesEnabled");
            boolean isServerOwned = chatrooms.getBool(name+".isServerOwned");


            List<UUID> banned = chatrooms.getStringList(name+".banned").stream()
                    .map(UUID::fromString).collect(Collectors.toList());

            List<UUID> muted = chatrooms.getStringList(name+".muted").stream()
                    .map(UUID::fromString).collect(Collectors.toList());

            ConfigurationSection nicknamesSection = chatrooms.getConfiguration()
                    .getConfigurationSection(name + ".nicknames");
            Map<UUID, String> nicknames = new HashMap<>();
            if(nicknamesSection != null) {
                for(String key : nicknamesSection.getKeys(false)) {
                    UUID uuid = UUID.fromString(key);
                    String nickname = nicknamesSection.getString(key);
                    nicknames.put(uuid, nickname);
                }
            }

            ConfigurationSection sec = chatrooms.getConfiguration().getConfigurationSection(name + ".members");
            Map<UUID, ChatRole> loadedRoles = new HashMap<>();
            for (String key : sec.getKeys(false)) {
                int role = sec.getInt(key);
                ChatRole roleO = ChatRole.getRole(role);
                UUID uuid = UUID.fromString(key);
                loadedRoles.put(uuid, roleO);
            }
            Chatroom loadedChat = new Chatroom(owner, name, description, isPublic, saved, isServerOwned, false, loadedRoles, banned, muted, nicknames, nicknamesEnabled);
            Bukkit.getLogger().info("Loaded chatroom " + name + " with values: ");
            Bukkit.getLogger().info(loadedChat.toString());

            return loadedChat;
        } else {
            return null;
        }
    }


    public ChatRole getRole(UUID uuid) {
        return getMemberMap().get(uuid);
    }


    public void unbanMember(UUID uuid) {
        if (isBanned(uuid)){
            getBannedMembers().remove(uuid);
        }
    }

    /**
     * Deletes itself from memory. Will remove and remove its correspondence from all @PrivatePlayer objects
     * TODO make this compatible with saved chatrooms
     */
    public void delete() {
        getOnlinePlayers().forEach(p->p.sendMessage(messages.getString("chatroom-being-deleted").replace("$chatroom$", getName())));
        for (PlayerChannelUser player : getMembers().stream().map(PlayerChannelUser::getPlayer).collect(Collectors.toList())) {
            player.getChatrooms().remove(this);
            // Check if deleted chatroom is currently focused, will cause messages to be sent to the void if not nullified
            if (player.getFocusedChatroom() != null && player.getFocusedChatroom().equals(this)){
                player.setFocusedChatroom(null);
            }

        }
        PlayerChannels.getInstance().getChatrooms().remove(this);
        if (isSaved()) {
            // have to delete from chatrooms.yml
            // players.yml should be fine because it should get overwritten
            ChannelFile chatrooms = new ChannelFile(CHATROOM);
            if (chatrooms.getConfiguration().isSet(getName())) {
                chatrooms.getConfiguration().set(getName(), null);
                chatrooms.save();
            }
        }

    }

    @Override
    public String toString(){
        return  getClass().getName() + "[name=" + name+", description=" + description+", owner=" + owner +", isSaved=" + isSaved +", isPublic=" + isPublic + ", isNicknamesEnabled=" + isNicknamesEnabled()+", isGlobal=" + isGlobal+"]";
    }
}
