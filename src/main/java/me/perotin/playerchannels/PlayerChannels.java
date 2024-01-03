package me.perotin.playerchannels;

import com.fren_gor.ultimateAdvancementAPI.AdvancementMain;
import com.fren_gor.ultimateAdvancementAPI.UltimateAdvancementAPI;
import com.google.common.base.Charsets;
import me.perotin.playerchannels.commands.AdminCommand;
import me.perotin.playerchannels.commands.CancelTutorialCommand;
import me.perotin.playerchannels.commands.PlayerChannelsCommand;
import me.perotin.playerchannels.commands.tabs_completer.PlayerChannelsTabCompleter;
import me.perotin.playerchannels.events.chat_events.*;
import me.perotin.playerchannels.events.join.PlayerChannelUserJoinEvent;
import me.perotin.playerchannels.objects.Chatroom;
import me.perotin.playerchannels.objects.InventoryHelper;
import me.perotin.playerchannels.objects.PreChatroom;
import me.perotin.playerchannels.objects.PlayerChannelUser;
import me.perotin.playerchannels.storage.files.FileType;
import me.perotin.playerchannels.storage.files.ChannelFile;
import me.perotin.playerchannels.utils.ChannelUtils;
import me.perotin.playerchannels.utils.Metrics;
import me.perotin.playerchannels.utils.TutorialHelper;
import me.perotin.playerchannels.utils.UpdateChecker;
import org.bukkit.Bukkit;
import org.bukkit.command.PluginCommand;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
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
3.6.1 update

New config.yml msgs:


# Subcommand for the help dialogue
help: "help"

# Help dialogue messages
help-msg: "&a-------------------------------"
help-msg-1: "&e/channels - &7Opens the channel menu"
help-msg-2: "&e/channels &6create &e<&aname&e> <optional: &adescription&e> - &7Creates a channel"
help-msg-3: "&e/channels &6join &e<&aname&e> - &7Joins a specified channel"
help-msg-4: "&e/channels &6listen &e<&aadd&e/&cremove&e/&coff&e> <&aname&e> - &7Only receive chat from a specified channel"





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

    private UltimateAdvancementAPI api;

    private AdvancementMain main;





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
        int pluginId = 19355;
        Metrics metrics = new Metrics(this, pluginId);

        new UpdateChecker(this).checkForUpdate();


        for (Player player : Bukkit.getOnlinePlayers()) {
            players.add(PlayerChannelUser.getPlayer(player.getUniqueId()));
        }

//        main.load();

        main.enableInMemory();
        api = UltimateAdvancementAPI.getInstance(this);










    }

    @Override
    public void onLoad() {
        main = new AdvancementMain(this);
        main.load();

    }






    // Clean up collections
    @Override
    public void onDisable(){
        // Save each to save chatroom to file, will worry about global chatrooms at another time

        //main.disable();
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



        PlayerChannelsCommand cmd = new PlayerChannelsCommand(this);

        getCommand("playerchannels").setExecutor(cmd);
        getCommand("playerchannels").setTabCompleter(new PlayerChannelsTabCompleter());



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

    /** @returns toast api for toast messages **/
    public UltimateAdvancementAPI getToastApi() {
        return api;
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


    public void reloadConfig(String fileName) {
        File configFile = new File(getDataFolder(), fileName);
        if (configFile.exists()) {
            FileConfiguration config = getConfig();
            try {
                config.load(configFile);
                getLogger().info(fileName + " has been reloaded!");
            } catch (IOException | InvalidConfigurationException e) {
                getLogger().severe("Could not reload " + fileName + ": " + e.getMessage());
            }
        } else {
            getLogger().severe(fileName + " does not exist and cannot be reloaded!");
        }
    }

}
