package me.perotin.privatetalk;

import me.perotin.privatetalk.commands.PrivateTalkCommand;
import me.perotin.privatetalk.events.chat_events.*;
import me.perotin.privatetalk.events.join.PrivatePlayerJoinEvent;
import me.perotin.privatetalk.objects.Chatroom;
import me.perotin.privatetalk.objects.InventoryHelper;
import me.perotin.privatetalk.objects.PreChatroom;
import me.perotin.privatetalk.objects.PrivatePlayer;
import me.perotin.privatetalk.storage.files.FileType;
import me.perotin.privatetalk.storage.files.PrivateFile;
import me.perotin.privatetalk.utils.PrivateUtils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

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
public class PrivateTalk extends JavaPlugin {


    /**
     * Chatrooms loaded on the server
     */
    private List<Chatroom> chatrooms;

    private List<PrivatePlayer> players;
    private static PrivateTalk instance;

    public static String QUICK_CHAT_PREFIX;
    private InventoryHelper helper;


    // Enabling method
    @Override
    public void onEnable(){
        instance = this;
        saveDefaultConfig();
        PrivateFile.loadFiles();
        this.chatrooms = new ArrayList<>();
        this.players = new ArrayList<>();
        this.helper = new InventoryHelper();
        QUICK_CHAT_PREFIX = getConfig().getString("quickchat-prefix");
        init();
        loadChatrooms();

        for (Player player : Bukkit.getOnlinePlayers()) {
            players.add(PrivatePlayer.getPlayer(player.getUniqueId()));
            Bukkit.broadcastMessage("Added " + player.getName() + "! to pmemory");
        }
    }

    // Clean up collections
    @Override
    public void onDisable(){
        // Save each to save chatroom to file, will worry about global chatrooms at another time
        chatrooms.stream().filter(Chatroom::isSaved).forEach(Chatroom::saveToFile);

        // Save each player to file
        for (PrivatePlayer privatePlayer : players) {
            Bukkit.getLogger().info("Saving " + privatePlayer.getName());
            privatePlayer.savePlayer();
        }
        this.players.clear();
        this.chatrooms.clear();
        this.helper = null;
        instance = null;
    }

    private void init(){
        Bukkit.getPluginManager().registerEvents(new CreateChatroomInputEvent(this), this);
        Bukkit.getPluginManager().registerEvents(new ChatroomChatEvent(this), this);
        Bukkit.getPluginManager().registerEvents(new StatusInputEvent(), this);

        Bukkit.getPluginManager().registerEvents(new PrivatePlayerJoinEvent(this), this);
        Bukkit.getPluginManager().registerEvents(new ChatroomSetNicknameEvent(this), this);
        Bukkit.getPluginManager().registerEvents(new ChatroomSetDescriptionEvent(this), this);



        PrivateUtils.registerCommand(new PrivateTalkCommand(getConfig().getString("command-name"), getConfig().getStringList("aliases"), this));

    }

    /**
     * @return instance of main class
     */
    public static PrivateTalk getInstance(){
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
    public List<PrivatePlayer> getPlayers() {
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
    public void addPlayer(PrivatePlayer player){
        if (!players.contains(player)) {
            players.add(player);
        }
    }

    /**
     * Load all chatrooms stored in chatrooms.yml
     */
    private void loadChatrooms() {
        PrivateFile chatroomsFile = new PrivateFile(FileType.CHATROOM);
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

}
