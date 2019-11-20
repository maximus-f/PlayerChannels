package me.perotin.privatetalk.storage.files;

import me.perotin.privatetalk.PrivateTalk;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.*;

/* Created by Perotin on 8/14/19 */
public class PrivateFile {

    private final FileType type;
    private File file;
    private FileConfiguration configuration;

    public PrivateFile(FileType type){
        this.type = type;
        switch(type){
            case PLAYERS:
                file = new File(PrivateTalk.getInstance().getDataFolder(), "players.yml");
                configuration = YamlConfiguration.loadConfiguration(file);
                break;
            case CHATROOM:
                file = new File(PrivateTalk.getInstance().getDataFolder(), "chatrooms.yml");
                configuration = YamlConfiguration.loadConfiguration(file);
                break;

            case MESSAGES:
                file = new File(PrivateTalk.getInstance().getDataFolder(), "messages.yml");
                configuration = YamlConfiguration.loadConfiguration(file);
                break;
            case MENUS:
                file = new File(PrivateTalk.getInstance().getDataFolder(), "menus.yml");
                configuration = YamlConfiguration.loadConfiguration(file);
                break;

        }
    }
    public void save() {
        try {
            configuration.save(file);
        } catch (IOException ex) {
            ex.printStackTrace();

        }
    }

    // some generic methods to speed up the process
    public boolean getBool(String path){
        return getConfiguration().getBoolean(path);
    }

    public FileConfiguration getConfiguration() {
        return configuration;
    }

    public Object get(String path) {
        return configuration.get(path);
    }

    public void set(String path, Object value) {
        configuration.set(path, value);
    }

    public String getString(String path) {
        if(configuration.getString(path) == null){
            Bukkit.getLogger().severe("Path " + path + " is null!");
            Bukkit.getLogger().severe(file.getAbsolutePath() + " is the file that this occurred!");
            return null;
        }
        return ChatColor.translateAlternateColorCodes('&', configuration.getString(path));
    }

    /**
     * loads all files with defaults
     */
    public void load() {

        File lang = null;
        InputStream defLangStream = null;

        switch (type) {
            case PLAYERS:
                lang = new File(PrivateTalk.getInstance().getDataFolder(), "players.yml");
                defLangStream = PrivateTalk.getInstance().getResource("players.yml");
                break;
            case CHATROOM:
                lang = new File(PrivateTalk.getInstance().getDataFolder(), "chatrooms.yml");
                defLangStream = PrivateTalk.getInstance().getResource("chatrooms.yml");
                break;
            case MESSAGES:
                lang = new File(PrivateTalk.getInstance().getDataFolder(), "messages.yml");
                defLangStream = PrivateTalk.getInstance().getResource("messages.yml");
                break;
            case MENUS:
                lang = new File(PrivateTalk.getInstance().getDataFolder(), "menus.yml");
                defLangStream = PrivateTalk.getInstance().getResource("menus.yml");
                break;

        }
        OutputStream out = null;
        if (!lang.exists()) {
            try {
                PrivateTalk.getInstance().getDataFolder().mkdir();
                lang.createNewFile();
                if (defLangStream != null) {
                    out = new FileOutputStream(lang);
                    int read;
                    byte[] bytes = new byte[1024];

                    while ((read = defLangStream.read(bytes)) != -1) {
                        out.write(bytes, 0, read);
                    }
                    return;
                }
            } catch (IOException e) {
                e.printStackTrace(); // So they notice
                Bukkit.getLogger().severe("[PrivateTalk] Couldn't create " + type.toString().toLowerCase() + " file.");
                Bukkit.getLogger().severe("[PrivateTalk] This is a fatal error. Now disabling");
                PrivateTalk.getInstance().getPluginLoader().disablePlugin(PrivateTalk.getInstance()); // Without
                // it
                // loaded,
                // we
                // can't
                // send
                // them
                // messages
            } finally {
                if (defLangStream != null) {
                    try {
                        defLangStream.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                if (out != null) {
                    try {
                        out.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                }
            }
        }
    }

    public File getFile(){
        return this.file;
    }
    public static void loadFiles(){
        if (!new File( PrivateTalk.getInstance().getDataFolder(), "chatrooms.yml").exists()) {
            PrivateTalk.getInstance().saveResource("chatrooms.yml", false);
        }
        if (!new File( PrivateTalk.getInstance().getDataFolder(), "messages.yml").exists()) {
            PrivateTalk.getInstance().saveResource("messages.yml", false);
        }  if (!new File( PrivateTalk.getInstance().getDataFolder(), "players.yml").exists()) {
            PrivateTalk.getInstance().saveResource("players.yml", false);
        } if (!new File( PrivateTalk.getInstance().getDataFolder(), "menus.yml").exists()) {
            PrivateTalk.getInstance().saveResource("menus.yml", false);
        }
    }






}
