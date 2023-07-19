package me.perotin.playerchannels;

import me.perotin.playerchannels.commands.AdminCommand;
import me.perotin.playerchannels.commands.CancelTutorialCommand;
import me.perotin.playerchannels.commands.PlayerChannelsCommand;
import me.perotin.playerchannels.events.chat_events.*;
import me.perotin.playerchannels.events.join.PlayerChannelUserJoinEvent;
import me.perotin.playerchannels.objects.Chatroom;
import me.perotin.playerchannels.objects.InventoryHelper;
import me.perotin.playerchannels.objects.PreChatroom;
import me.perotin.playerchannels.objects.PlayerChannelUser;
import me.perotin.playerchannels.storage.files.FileType;
import me.perotin.playerchannels.storage.files.ChannelFile;
import me.perotin.playerchannels.utils.ChannelUtils;
import me.perotin.playerchannels.utils.TutorialHelper;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.*;

/* Created by Perotin on 8/13/19 */

/**
 * @Author Perotin
 * @dateBegan 8/13/19
 *
 * This rewrite is meant to use better practices, more readable & maintainable code, and overall renew my joy
 * for writing PrivateTalk. This is my 3rd rewrite of this plugin, a project originally conceived early 2017, nearly 3 years ago.
 */

/*
TODO List
5/6/23 Finished barebones main page, chatroom creation menu, and chatroom pager

Focus on show player
Inviting other players
Chatting in chatroom





 */


/*
 */
public class PlayerChannels extends JavaPlugin {


    /**
     * Chatrooms loaded on the server
     */
    private List<Chatroom> chatrooms;

    private List<PlayerChannelUser> players;
    private static PlayerChannels instance;

    public static String QUICK_CHAT_PREFIX;
    private InventoryHelper helper;

    private Set<UUID> disableGlobalChat;


    // Enabling method
    @Override
    public void onEnable(){
        instance = this;
        saveDefaultConfig();
        ChannelFile.loadFiles();
        this.chatrooms = new ArrayList<>();
        this.players = new ArrayList<>();
        this.helper = new InventoryHelper();
        this.disableGlobalChat = new HashSet<>();
        QUICK_CHAT_PREFIX = getConfig().getString("quickchat-prefix");
        init();
        loadChatrooms();

        for (Player player : Bukkit.getOnlinePlayers()) {
            players.add(PlayerChannelUser.getPlayer(player.getUniqueId()));
        }
    }

    // Clean up collections
    @Override
    public void onDisable(){
        // Save each to save chatroom to file, will worry about global chatrooms at another time
        chatrooms.stream().filter(Chatroom::isSaved).forEach(Chatroom::saveToFile);

        // Save each player to file
        for (PlayerChannelUser playerChannelUser : players) {
            Bukkit.getLogger().info("Saving " + playerChannelUser.getName());
            playerChannelUser.savePlayer();
        }
        this.players.clear();
        this.chatrooms.clear();
        this.helper = null;
        instance = null;
        TutorialHelper.inTutorial.clear();
    }

    private void init(){
        Bukkit.getPluginManager().registerEvents(new CreateChatroomInputEvent(this), this);
        Bukkit.getPluginManager().registerEvents(new ChatroomChatEvent(this), this);
        Bukkit.getPluginManager().registerEvents(new StatusInputEvent(), this);

        Bukkit.getPluginManager().registerEvents(new PlayerChannelUserJoinEvent(this), this);
        Bukkit.getPluginManager().registerEvents(new ChatroomSetNicknameEvent(this), this);
        Bukkit.getPluginManager().registerEvents(new ChatroomSetDescriptionEvent(this), this);
        Bukkit.getPluginManager().registerEvents(new ChatroomConfirmDeletionEvent(this), this);




        ChannelUtils.registerCommand(new PlayerChannelsCommand(getConfig().getString("command-name"), getConfig().getStringList("aliases"), this));
        ChannelUtils.registerCommand(new CancelTutorialCommand(getConfig().getString("cancel-tutorial"), getConfig().getStringList("cancel-tutorial-aliases"), this));
        getCommand("pcadmin").setExecutor(new AdminCommand());
    }

    /**
     * @return instance of main class
     */
    public static PlayerChannels getInstance(){
        return instance;
    }


    /**
     * @return helper for inventory actions
     */
    public InventoryHelper getHelper() {
        return helper;
    }

    /**
     * @return collection of chatrooms
     */
    public List<Chatroom> getChatrooms() {
        return chatrooms;
    }

    /**
     * @return player collection of registered players
     */
    public List<PlayerChannelUser> getPlayers() {
        return players;
    }

    /**
     * @param chatroom to fully initialize as a chatroom
     */
    public Chatroom createChatroom(PreChatroom chatroom){
        chatrooms.add(chatroom.toChatroom());
        return chatrooms.get(chatrooms.size() - 1);
    }

    /**
     * @param player to add to private talk's memory
     */
    public void addPlayer(PlayerChannelUser player){
        if (!players.contains(player)) {
            players.add(player);
        }
    }

    /**
     * Load all chatrooms stored in chatrooms.yml
     */
    private void loadChatrooms() {
        ChannelFile chatroomsFile = new ChannelFile(FileType.CHATROOM);
        for (String key : chatroomsFile.getConfiguration().getKeys(false)) {
            // These should be the names
            Chatroom toLoad = Chatroom.loadChatroom(key);
            getLogger().info("Loaded " + toLoad.getName() + "!");
            chatrooms.add(toLoad);
        }
    }

    public Chatroom getChatroom(String name){
        for (Chatroom chat : chatrooms) {
            if (chat.getName().equalsIgnoreCase(name))
                return chat;
        }
        return null;
    }

    public Set<UUID> getListeningPlayers() {
        return disableGlobalChat;
    }
}
