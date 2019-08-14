package me.perotin.privatetalk;

import me.perotin.privatetalk.objects.Chatroom;
import me.perotin.privatetalk.objects.PrivatePlayer;
import me.perotin.privatetalk.storage.files.PrivateFile;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;

/* Created by Perotin on 8/13/19 */

/**
 * @Author Perotin
 * @dateBegan 8/13/19
 *
 * This rewrite is meant to use better practices, more readable & maintainable code, and overall renew my joy
 * for writing PrivateTalk. This is my 3rd rewrite of this plugin, a project originally conceived early 2017, nearly 3 years ago.
 */
public class PrivateTalk extends JavaPlugin {

    /**
     * Chatrooms loaded on the server
     */
    private List<Chatroom> chatrooms;
    private List<PrivatePlayer> players;
    private static PrivateTalk instance;

    @Override
    public void onEnable(){
        this.chatrooms = new ArrayList<>();
        this.players = new ArrayList<>();
        instance = this;
        PrivateFile.loadFiles();

    }

    public static PrivateTalk getInstance(){
        return instance;
    }
}
