package me.perotin.playerchannels;

import com.fren_gor.ultimateAdvancementAPI.AdvancementMain;
import com.fren_gor.ultimateAdvancementAPI.UltimateAdvancementAPI;
import com.google.common.base.Charsets;
import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteStreams;
import me.perotin.playerchannels.commands.AdminCommand;
import me.perotin.playerchannels.commands.CancelTutorialCommand;
import me.perotin.playerchannels.commands.PlayerChannelsCommand;
import me.perotin.playerchannels.commands.tabs_completer.PlayerChannelsTabCompleter;
import me.perotin.playerchannels.events.chat_events.*;
import me.perotin.playerchannels.events.join.PlayerChannelUserJoinEvent;
import me.perotin.playerchannels.objects.*;
import me.perotin.playerchannels.proxy.BungeeMessageHandler;
import me.perotin.playerchannels.storage.changelog.ChannelManager;
import me.perotin.playerchannels.storage.files.FileType;
import me.perotin.playerchannels.storage.files.ChannelFile;
import me.perotin.playerchannels.storage.mysql.SQLHandler;
import me.perotin.playerchannels.utils.ChannelUtils;
import me.perotin.playerchannels.utils.Metrics;
import me.perotin.playerchannels.utils.TutorialHelper;
import me.perotin.playerchannels.utils.UpdateChecker;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.PluginCommand;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.messaging.PluginMessageListener;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;

/* Created by Perotin on 8/13/19 */

/**
 * @Author Perotin
 * @dateBegan 8/13/19
 *
 * This rewrite is meant to use better practices, more readable & maintainable code, and overall renew my joy
 * for writing PrivateTalk. This is my 3rd rewrite of this plugin, a project originally conceived early 2017, nearly 3 years ago.
 */

/*
3.8 TO-DO
- Need a way to delete MySQL channel
  Lots of testing


    Last time, was running out of memory just due to having lots of tabs, 2 minecrafts up, 2 servers etc.
    Left off trying to use reflection to change status of isEnabled to be able to send a plugin message there.
    Should work theoretically, test then proceed with other tests.


TODO
- Test, theoretically is done now for real
-

 */
public class PlayerChannels extends JavaPlugin implements PluginMessageListener {


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

    private boolean bungeecord, usePermission, createPermission, checkLimit, mySQL;

    private int defaultChannelLimit;

    private SQLHandler sqlHandler;

    private ChannelManager channelManager;




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


//        main.load();

        main.enableInMemory();
        api = UltimateAdvancementAPI.getInstance(this);

       this.bungeecord = getConfig().getBoolean("bungeecord");
       this.usePermission = getConfig().contains("use-permission") && getConfig().getBoolean("use-permission");
       this.createPermission = getConfig().contains("create-permission") && getConfig().getBoolean("create-permission");
       this.checkLimit = getConfig().contains("check-limit") && getConfig().getBoolean("check-limit");
       this.defaultChannelLimit = getConfig().contains("default-channel-limit") ? getConfig().getInt("default-channel-limit") : 3;

       // Setup MySQL is true
        setupMySQL();

        // Enable bungeecord support
        setupBungeecordSupport();



        // Load in player data after channel data
        for (Player player : Bukkit.getOnlinePlayers()) {
            players.add(PlayerChannelUser.getPlayer(player.getUniqueId()));
        }

    }



    @Override
    public void onLoad() {
        main = new AdvancementMain(this);
        main.load();

    }


    @Override
    public void onPluginMessageReceived(String channel, Player player, byte[] message) {
        BungeeMessageHandler handler = new BungeeMessageHandler(this);
        handler.handlePluginMessage(channel, player, message);
    }




    // Clean up collections
    @Override
    public void onDisable(){

        clearChannelCache(); // clear other servers change cache with reflection

        // Save each to save chatroom to file, will worry about global chatrooms at another time

        //main.disable();
        chatrooms.stream().filter(c -> c.isSaved() && !c.isGlobal()).forEach(Chatroom::saveToFile);



        // Save each player to file
        for (PlayerChannelUser playerChannelUser : players) {
            playerChannelUser.savePlayer();
        }


        this.players.clear();

        this.chatrooms.clear();
        this.helper = null;
        instance = null;
        TutorialHelper.inTutorial.clear();

        if (isBungeecord()) {
            this.getServer().getMessenger().unregisterOutgoingPluginChannel(this);
            this.getServer().getMessenger().unregisterIncomingPluginChannel(this);
        }

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




    // Use reflection to set status to enabled briefly in order to send plugin messages to synchronize caches
    private void clearChannelCache() {
        try {

            Field field = getField(JavaPlugin.class, "isEnabled");
            field.setAccessible(true);


            field.set(this, true);


            if (isBungeecord() && mySQL && channelManager != null && !channelManager.isEmpty()) {

                channelManager.onDisable();
            }

            field.set(this, false);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * Reflection method to get private field in heirarchy
     * @param clazz
     * @param name
     * @return
     */
    public static Field getField(Class<?> clazz, String name) {
        Field field = null;
        while (clazz != null && field == null) {
            try {
                field = clazz.getDeclaredField(name);
            } catch (Exception e) {
            }
            clazz = clazz.getSuperclass();
        }
        return field;
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
     * @return only hidden channels
     */
    public List<Chatroom> getHiddenChatrooms() {
        return getChatrooms().stream().filter(c -> c.isHidden()).collect(Collectors.toList());
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
     * Returns whether playerchannels.use will be used
     */
    public boolean isUsePermissionEnabled() {
        return usePermission;
    }

    /**
     * Returns whether playerchannels.use will be used
     */
    public boolean isCreatePermission() {
        return createPermission;
    }

    /**
     * @return whether MySQL db is enabled
     */
    public boolean isMySQL() {
        return mySQL;
    }

    /**
     * @return channel manager object to log changes
     */
    public ChannelManager getChannelManager() {
        return channelManager;
    }

    /**
     * @return MySQL utility class
     */
    public SQLHandler getSqlHandler() {
        return sqlHandler;
    }

    /**

     * @return if bungeecord is enabled
     */
    public boolean isBungeecord() {
        return bungeecord;
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


    /**
     * @param name
     * @return name of specific channel, null if not exist
     */
    public Chatroom getChatroom(String name){
        for (Chatroom chat : chatrooms) {
            if (ChatColor.stripColor(chat.getName()).equalsIgnoreCase(ChatColor.stripColor(name)))
                return chat;
        }
        return null;
    }

    /**
     * @return Set of players who are not listening to global chat.
     */
    public Set<UUID> getListeningPlayers() {
        return disableGlobalChat;
    }


    // Reload yaml file
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



    // Set up MySQL configuration if Bungeecord is true and test connection
    private void setupMySQL() {
        this.mySQL = getConfig().contains("mysql-enabled") && getConfig().getBoolean("mysql-enabled") && bungeecord;

        // Using MySQL unnecessarily
        if (getConfig().contains("mysql-enabled") && getConfig().getBoolean("mysql-enabled") && !bungeecord) {
            Bukkit.getConsoleSender().sendMessage("[PlayerChannels] MySQL is enabled but Bungeecord is not. Flat-storage is preferable in this scenario.");
            Bukkit.getConsoleSender().sendMessage("[PlayerChannels] MySQL will not be used for this session.");
            return;
        }

        if (mySQL) {
            sqlHandler = new SQLHandler(
                    getConfig().getString("host"),
                    getConfig().getString("database"),
                    getConfig().getString("username"),
                    getConfig().getString("password"),
                    getConfig().getInt("port")
            );

            this.channelManager = new ChannelManager(sqlHandler);


            try {
                if (!sqlHandler.getConnection().isValid(2)) {
                    mySQL = false;
                    Bukkit.getConsoleSender().sendMessage("[PlayerChannels] MySQL failed to register. Global saved channels will not be saved.");
                } else {
                    Bukkit.getConsoleSender().sendMessage("[PlayerChannels] MySQL successfully connected. Global saved channels will be saved.");
                }
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }
    }

    /**
     * Return whether to check for a max limit when players create a channel
     */
    public boolean checkForLimit() {
        return this.checkLimit;
    }

    /**
     * Returns the default max channels if none is specified
     */
    public int getDefaultChannelLimit() {
        return this.defaultChannelLimit;
    }

    // Add Bungeecord hook
    private void setupBungeecordSupport() {
        if (isBungeecord()) {
            Bukkit.getConsoleSender().sendMessage("[PlayerChannels] Loading Bungeecord hook.");
            this.getServer().getMessenger().registerOutgoingPluginChannel(this, "BungeeCord");
            this.getServer().getMessenger().registerIncomingPluginChannel(this, "BungeeCord", this);
            Bukkit.getConsoleSender().sendMessage("[PlayerChannels] Bungeecord channels registered.");

            Bukkit.getConsoleSender().sendMessage("[PlayerChannels] Loading in pre-existing Global Channels");


            GlobalChatroom.sendGlobalSearch();

            // Loading in global channels from mysql that were not added from the global search if possible
            if (isMySQL()) {
                try {
                    Set<String> addedNames = new HashSet<>();
                    addedNames.addAll(chatrooms.stream().map(Chatroom::getName).collect(Collectors.toList()));

                    List<Chatroom> allBCordChannels = sqlHandler.getAllChatrooms();

                    for (Chatroom chatroom : allBCordChannels) {
                        if (!addedNames.contains(chatroom.getName())) {
                            chatroom.setMembers(sqlHandler.getChatroomMembers(chatroom.getName()));
                            chatrooms.add(chatroom);
                            addedNames.add(chatroom.getName());
                            Bukkit.getConsoleSender().sendMessage("[PlayerChannels] Added in " + chatroom.getName() + " from " + sqlHandler.getDatabase()+" database!");

                        }
                    }
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }




}
