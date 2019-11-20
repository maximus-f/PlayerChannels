package me.perotin.privatetalk;

import me.perotin.privatetalk.commands.PrivateTalkCommand;
import me.perotin.privatetalk.events.chat_events.CreateChatroomInputEvent;
import me.perotin.privatetalk.objects.Chatroom;
import me.perotin.privatetalk.objects.InventoryHelper;
import me.perotin.privatetalk.objects.PreChatroom;
import me.perotin.privatetalk.objects.PrivatePlayer;
import me.perotin.privatetalk.storage.files.PrivateFile;
import org.bukkit.Bukkit;
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
1. PrivateTalkCommand is not working


Was working on the Inventory Actions and setting each item to an appropiate action, in theory, this MAY be done but very unlikely it will work first try.
Goal is to get the creation menu up and the basic switches and toggles to be working... dive deep into that. Also, maybe spend a commit just commenting/refactoring
since codebase is getting a bit messy
 */


/*
Finish PrivatePlayer.getPlayer(UUID)
 */
public class PrivateTalk extends JavaPlugin {


    /**
     * Chatrooms loaded on the server
     */
    private List<Chatroom> chatrooms;

    private List<PrivatePlayer> players;
    private static PrivateTalk instance;
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
        init();
        getCommand("privatetalk").setExecutor(new PrivateTalkCommand(this));

    }

    // Clean up collections
    @Override
    public void onDisable(){
        this.players.clear();
        this.chatrooms.clear();
        this.helper = null;
        instance = null;
    }

    private void init(){
        Bukkit.getPluginManager().registerEvents(new CreateChatroomInputEvent(this), this);
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
    public void createChatroom(PreChatroom chatroom){
        chatrooms.add(chatroom.toChatroom());
    }


}
