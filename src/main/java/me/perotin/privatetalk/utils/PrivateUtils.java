package me.perotin.privatetalk.utils;

import com.github.stefvanschie.inventoryframework.gui.type.util.Gui;
import me.perotin.privatetalk.PrivateTalk;
import me.perotin.privatetalk.objects.Chatroom;
import me.perotin.privatetalk.objects.inventory.Menu;
import me.perotin.privatetalk.objects.inventory.paging_objects.MainMenuPaging;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandMap;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

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
     *
     */
    public static Chatroom getChatroomWith(String name){
        PrivateTalk plugin = PrivateTalk.getInstance();
        for(Chatroom chat : plugin.getChatrooms()){
            if(ChatColor.stripColor( chat.getName()).equals(ChatColor.stripColor(name))){
                return chat;
            }
        }
        // not found in loaded chatrooms
        // check files
        Chatroom chat = Chatroom.loadChatroom(name);
        return chat; // may be null
    }

    /**
     * Strips an itemstack of either all of its lore or a specified index
     * @param toStrip
     * @param all
     * @param index
     * @return
     */
    public static ItemStack stripLore(ItemStack toStrip, boolean all, int index) {
        ItemMeta meta = toStrip.getItemMeta();
        if (all) {
            meta.setLore(new ArrayList<>());
        } else {
            List<String> lores = meta.getLore();
            lores.remove(index);
            meta.setLore(lores);
        }
        toStrip.setItemMeta(meta);
        return toStrip;
    }

    /**
     * @param message of error
     */
    public static void sendMenuMessage(String message, Player player, Menu nextMenu){
        InventoryView menu = player.getOpenInventory();
        player.closeInventory();
        player.sendTitle(message, "", 0, 2*20, 0);
        new BukkitRunnable() {
            @Override
            public void run() {
                if (nextMenu != null) {
                    nextMenu.show(player);
                } else {
                    player.openInventory(menu);
                }
            }
        }.runTaskLater(PrivateTalk.getInstance(), 2*20);
    }


    public static ItemStack appendToDisplayName(ItemStack item, String append){
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(meta.getDisplayName() + " " + append);
        item.setItemMeta(meta);
        return item;

    }

    public static Gui getMainMenu(Player viewer){
        return new MainMenuPaging(viewer, PrivateTalk.getInstance()).getMenu();
    }
}
