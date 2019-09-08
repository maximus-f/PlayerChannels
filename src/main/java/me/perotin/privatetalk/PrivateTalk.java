package me.perotin.privatetalk;

import me.perotin.privatetalk.commands.PrivateTalkCommand;
import me.perotin.privatetalk.objects.Chatroom;
import me.perotin.privatetalk.objects.InventoryHelper;
import me.perotin.privatetalk.objects.PrivatePlayer;
import me.perotin.privatetalk.storage.files.PrivateFile;
import me.perotin.privatetalk.utils.PrivateUtils;
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
1. Write function for sorting HashMap by ChatRole
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
        this.helper = new InventoryHelper();
        instance = this;
        init();

    }

    private void init(){
        PrivateFile.loadFiles();
        saveDefaultConfig();
        FileConfiguration config = getConfig();
        PrivateUtils.registerCommand(new PrivateTalkCommand(config.getString("command-name"), "Base command for PrivateTalk", "/"+config.getString("command-name"), config.getStringList("aliases"), this));
    }

    public static PrivateTalk getInstance(){
        return instance;
    }

    public PrivatePlayer getPrivatePlayer(UUID uuid){
        return null;
    }
}
