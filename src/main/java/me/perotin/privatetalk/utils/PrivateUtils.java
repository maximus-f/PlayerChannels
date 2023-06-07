package me.perotin.privatetalk.utils;

import com.github.stefvanschie.inventoryframework.gui.type.util.Gui;
import me.perotin.privatetalk.PrivateTalk;
import me.perotin.privatetalk.objects.Chatroom;
import me.perotin.privatetalk.objects.inventory.Menu;
import me.perotin.privatetalk.objects.inventory.paging_objects.MainMenuPaging;
import org.bukkit.*;
import org.bukkit.advancement.Advancement;
import org.bukkit.advancement.AdvancementProgress;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
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
import java.util.UUID;

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

//        // Create the BossBar
//        BossBar bossBar = Bukkit.createBossBar(message, BarColor.PURPLE, BarStyle.SOLID);

        sendAdvancementNotification(player, message, "", Material.WRITABLE_BOOK);
//        bossBar.addPlayer(player);

        if (nextMenu != null){
            nextMenu.show(player);
        }

//        Bukkit.getScheduler().runTaskLater(PrivateTalk.getInstance(), () -> bossBar.removePlayer(player), 5*20L); // 60L is approximately 3 seconds

    }


    private static void sendAdvancementNotification(Player player, String title, String description, Material iconMaterial) {
        // Generate a random UUID for this advancement.
        String uuid = UUID.randomUUID().toString();

        // Create a NamespacedKey for the advancement.
        NamespacedKey key = new NamespacedKey(PrivateTalk.getInstance(), uuid);

        // Create the JSON for the advancement.
        String json = "{"
                + "\"display\": {"
                + "\"title\": {\"text\": \"" + title + "\", \"color\": \"yellow\"},"
                + "\"description\": {\"text\": \"" + description + "\", \"color\": \"white\"},"
                + "\"icon\": {\"item\": \"minecraft:" + iconMaterial.getKey().getKey() + "\"},"
                + "\"frame\": \"goal\","
                + "\"announce_to_chat\": false,"
                + "\"show_toast\": true,"
                + "\"hidden\": true,"
                + "\"background\": \"minecraft:textures/block/stone.png\""
                + "},"
                + "\"criteria\": {"
                + "\"trigger\": {\"trigger\": \"minecraft:impossible\"}"
                + "}"
                + "}";

        // Load the advancement.
        Advancement advancement = Bukkit.getUnsafe().loadAdvancement(key, json);
        if (advancement != null) {
            // Grant the advancement to the player.
            AdvancementProgress progress = player.getAdvancementProgress(advancement);
            progress.awardCriteria("trigger");

            // Schedule the removal of the advancement.
            Bukkit.getScheduler().runTaskLater(PrivateTalk.getInstance(), () -> {
                progress.revokeCriteria("trigger");
                Bukkit.getUnsafe().removeAdvancement(key);
                Bukkit.getServer().reloadData();
            }, 20L);  // Remove the advancement after 20 ticks (1 second).
        }
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

    public static ItemStack replacePlaceHolderInDisplayName(ItemStack toEdit, String placeholder, String input) {
        ItemMeta meta = toEdit.getItemMeta();
        meta.setDisplayName(meta.getDisplayName().replace(placeholder, input));
        toEdit.setItemMeta(meta);
        return toEdit;
    }
}
