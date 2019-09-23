package me.perotin.privatetalk.utils;

import me.perotin.privatetalk.PrivateTalk;
import me.perotin.privatetalk.objects.Chatroom;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandMap;

import java.lang.reflect.Field;

/* Created by Perotin on 8/21/19 */


/**
 * Random util class for static methods that are useful
 */
public class PrivateUtils {

    private PrivateUtils(){}


    /**
     * @param command to register
     */
    public static void registerCommand(Command command) {
        try {
            final Field commandMapField = Bukkit.getServer().getClass().getDeclaredField("commandMap");
            commandMapField.setAccessible(true);

            final CommandMap commandMap = (CommandMap) commandMapField.get(Bukkit.getServer());
            commandMap.register(command.getLabel(), command);

        } catch(final Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * @param name of chatroom
     * @return chatroom with specific name
     */
    public static Chatroom getChatroomWith(String name){
        PrivateTalk plugin = PrivateTalk.getInstance();
        for(Chatroom chat : plugin.getChatrooms()){
            if(ChatColor.stripColor( chat.getName()).equals(ChatColor.stripColor(name))){
                return chat;
            }
        }
        return null;
    }
}
