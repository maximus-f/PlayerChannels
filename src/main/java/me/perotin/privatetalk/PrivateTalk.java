package me.perotin.privatetalk;

import me.perotin.privatetalk.commands.PrivateTalkCommand;
import me.perotin.privatetalk.commands.TestCommand;
import me.perotin.privatetalk.events.chat_events.CreateChatroomInputEvent;
import me.perotin.privatetalk.objects.Chatroom;
import me.perotin.privatetalk.objects.InventoryHelper;
import me.perotin.privatetalk.objects.PrivatePlayer;
import me.perotin.privatetalk.storage.files.PrivateFile;
import me.perotin.privatetalk.utils.PrivateUtils;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

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
1. PrivateTalkCommand is not working
2. Working on CreateChatroomInputEvent
 */
public class PrivateTalk extends JavaPlugin {


    /**
     * Chatrooms loaded on the server
     */
    private List<Chatroom> chatrooms;
    private List<PrivatePlayer> players;
    private static PrivateTalk instance;
    private InventoryHelper helper;

    @Override
    public void onEnable(){
        this.chatrooms = new ArrayList<>();
        this.players = new ArrayList<>();
        instance = this;
        this.helper = new InventoryHelper();
        saveDefaultConfig();
        PrivateFile.loadFiles();
        getCommand("privatetalk").setExecutor(new PrivateTalkCommand(this));
        getCommand("test").setExecutor(new TestCommand());
        init();



    }

    private void init(){
        Bukkit.getPluginManager().registerEvents(new CreateChatroomInputEvent(this), this);
    }

    public static PrivateTalk getInstance(){
        return instance;
    }

    public PrivatePlayer getPrivatePlayer(UUID uuid){
        return null;
    }

    public InventoryHelper getHelper() {
        return helper;
    }

    public List<Chatroom> getChatrooms() {
        return chatrooms;
    }

    public List<PrivatePlayer> getPlayers() {
        return players;
    }


}
