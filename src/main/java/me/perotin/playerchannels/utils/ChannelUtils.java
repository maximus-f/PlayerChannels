package me.perotin.playerchannels.utils;

import com.fren_gor.ultimateAdvancementAPI.UltimateAdvancementAPI;
import com.fren_gor.ultimateAdvancementAPI.advancement.display.AdvancementDisplay;
import com.fren_gor.ultimateAdvancementAPI.advancement.display.AdvancementFrameType;
import com.github.stefvanschie.inventoryframework.gui.type.util.Gui;
import me.perotin.playerchannels.PlayerChannels;
import me.perotin.playerchannels.objects.Chatroom;
import me.perotin.playerchannels.objects.inventory.Menu;
import me.perotin.playerchannels.objects.inventory.paging_objects.MainMenuPaging;
import me.perotin.playerchannels.storage.files.FileType;
import me.perotin.playerchannels.storage.files.ChannelFile;
import org.bukkit.*;
import org.bukkit.advancement.Advancement;
import org.bukkit.advancement.AdvancementProgress;
import org.bukkit.command.Command;
import org.bukkit.command.CommandMap;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.lang.reflect.Field;
import java.util.*;

/* Created by Perotin on 8/21/19 */


/**
 * Random util class for static methods that are useful
 */
public class ChannelUtils {

    private ChannelUtils(){}


    private static final Map<UUID, Map<String, Long>> lastMessageSent = new HashMap<>();

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
        PlayerChannels plugin = PlayerChannels.getInstance();
        for(Chatroom chat : plugin.getChatrooms()){
            if(ChatColor.stripColor( chat.getName()).equalsIgnoreCase(ChatColor.stripColor(name))){
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

    public static String getMessageString(String path) {
        return new ChannelFile(FileType.MESSAGES).getString(path);
    }
    /**
     * @param message of error
     */
    public static void sendMenuMessage(String message, Player player, Menu nextMenu){

//        // Create the BossBar
//        BossBar bossBar = Bukkit.createBossBar(message, BarColor.PURPLE, BarStyle.SOLID);

        if (canSendMessage(player, message)) {
            try {
                sendAdvancementNotification(player, message, "", Material.WRITABLE_BOOK);
            } catch (Exception ex) {
                player.sendMessage(message);
            }
//        bossBar.addPlayer(player);


        }
        if (nextMenu != null) {
            nextMenu.show(player);
        }

//        Bukkit.getScheduler().runTaskLater(PrivateTalk.getInstance(), () -> bossBar.removePlayer(player), 5*20L); // 60L is approximately 3 seconds

    }

    private static boolean canSendMessage(Player player, String messageType) {
        long now = System.currentTimeMillis();
        Map<String, Long> playerMessages = lastMessageSent.computeIfAbsent(player.getUniqueId(), k -> new HashMap<>());
        Long lastSent = playerMessages.get(messageType);

        if (lastSent == null || (now - lastSent) > 60 * 1000) {
            playerMessages.put(messageType, now);
            return true;
        }
        return false;
    }


    private static void sendAdvancementNotification(Player player, String title, String description, Material iconMaterial) {

        try {
            PlayerChannels.getInstance().getToastApi().displayCustomToast(player, new ItemStack(iconMaterial), title, AdvancementFrameType.GOAL);

        } catch (Exception ex) {
            player.sendMessage(title);
        }
//        // Generate a random UUID for this advancement.
//        String uuid = UUID.randomUUID().toString();
//
//        // Create a NamespacedKey for the advancement.
//        NamespacedKey key = new NamespacedKey(PlayerChannels.getInstance(), uuid);
//
//        // Create the JSON for the advancement.
//        String json = "{"
//                + "\"display\": {"
//                + "\"title\": {\"text\": \"" + title + "\", \"color\": \"yellow\"},"
//                + "\"description\": {\"text\": \"" + description + "\", \"color\": \"white\"},"
//                + "\"icon\": {\"item\": \"minecraft:" + iconMaterial.getKey().getKey() + "\"},"
//                + "\"frame\": \"goal\","
//                + "\"announce_to_chat\": false,"
//                + "\"show_toast\": true,"
//                + "\"hidden\": true,"
//                + "\"background\": \"minecraft:textures/block/stone.png\""
//                + "},"
//                + "\"criteria\": {"
//                + "\"trigger\": {\"trigger\": \"minecraft:impossible\"}"
//                + "}"
//                + "}";
//
//        // Load the advancement.
//        Advancement advancement = Bukkit.getUnsafe().loadAdvancement(key, json);
//        if (advancement != null) {
//            // Grant the advancement to the player.
//            AdvancementProgress progress = player.getAdvancementProgress(advancement);
//            progress.awardCriteria("trigger");
//
//            // Schedule the removal of the advancement.
//            Bukkit.getScheduler().runTaskLater(PlayerChannels.getInstance(), () -> {
//                progress.revokeCriteria("trigger");
//                Bukkit.getUnsafe().removeAdvancement(key);
//               Bukkit.getServer().reloadData();
//            }, 20L);  // Remove the advancement after 20 ticks (1 second).
//        }
    }


    /**
     * Adds color to any given message provided that they are allowed
     * @param msg
     * @return
     */
    public static String addColor(String msg) {
        boolean allowed = PlayerChannels.getInstance().getConfig().getBoolean("chat-colors");
        if (allowed) {
            return ChatColor.translateAlternateColorCodes('&', msg);
        } else return msg;
    }


    public static ItemStack appendToDisplayName(ItemStack item, String append){
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(meta.getDisplayName() + " " + append);
        item.setItemMeta(meta);
        return item;

    }



    public static Gui getMainMenu(Player viewer){
        return new MainMenuPaging(viewer, PlayerChannels.getInstance()).getMenu();
    }

    /**
     * Replaces the display name of an item with actual input
     * @param toEdit
     * @param placeholder
     * @param input
     * @return
     */
    public static ItemStack replacePlaceHolderInDisplayName(ItemStack toEdit, String placeholder, String input) {
        ItemMeta meta = toEdit.getItemMeta();
        meta.setDisplayName(meta.getDisplayName().replace(placeholder, input));
        toEdit.setItemMeta(meta);
        return toEdit;
    }

    /**
     * Replaces the display name of an item with actual input
     * @param toEdit
     * @param placeholder
     * @param input
     * @return
     */
    public static ItemStack replacePlaceHolderInLore(ItemStack toEdit, String placeholder, String input, int line) {
        ItemMeta meta = toEdit.getItemMeta();
        if (meta != null) {
            List<String> lore = meta.getLore();
            if (lore != null && line >= 0 && line < lore.size()) {
                String updatedLine = lore.get(line).replace(placeholder, input);
                lore.set(line, updatedLine); // Update the line in the lore
                meta.setLore(lore);
                toEdit.setItemMeta(meta);
            }
        }
        return toEdit;
    }

    /**
     * Simple method to colorize strings with '&'
     * @param s
     * @return
     */
    public static String colorize(String s) {
        return ChatColor.translateAlternateColorCodes('&', s);
    }

}
