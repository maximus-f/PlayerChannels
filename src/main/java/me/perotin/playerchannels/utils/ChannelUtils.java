package me.perotin.playerchannels.utils;

import com.fren_gor.ultimateAdvancementAPI.UltimateAdvancementAPI;
import com.fren_gor.ultimateAdvancementAPI.advancement.display.AdvancementDisplay;
import com.fren_gor.ultimateAdvancementAPI.advancement.display.AdvancementFrameType;
import com.github.stefvanschie.inventoryframework.gui.type.util.Gui;
import me.perotin.playerchannels.PlayerChannels;
import me.perotin.playerchannels.commands.tabs_completer.PlayerChannelsTabCompleter;
import me.perotin.playerchannels.events.chat_events.ChatroomConfirmDeletionEvent;
import me.perotin.playerchannels.objects.ChatRole;
import me.perotin.playerchannels.objects.Chatroom;
import me.perotin.playerchannels.objects.PlayerChannelUser;
import me.perotin.playerchannels.objects.inventory.Menu;
import me.perotin.playerchannels.objects.inventory.paging_objects.MainMenuPaging;
import me.perotin.playerchannels.storage.Pair;
import me.perotin.playerchannels.storage.files.FileType;
import me.perotin.playerchannels.storage.files.ChannelFile;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.*;
import org.bukkit.advancement.Advancement;
import org.bukkit.advancement.AdvancementProgress;
import org.bukkit.command.Command;
import org.bukkit.command.CommandMap;
import org.bukkit.command.PluginCommand;
import org.bukkit.command.TabCompleter;
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


        } else {
            // avoid spamming it, so send as a basic message
            player.sendMessage(message);
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

    public static void leaveChatroom (PlayerChannelUser user, Chatroom channel, boolean showMainMenu) {
        ChannelFile messages = new ChannelFile(FileType.MESSAGES);
        if (!channel.isServerOwned() && channel.getRole(user.getUuid()) == ChatRole.OWNER && (channel.getMembers().size() > 1 || channel.isSaved())) {
            Player player = Bukkit.getPlayer(user.getUuid());
            ChatroomConfirmDeletionEvent.confirmDeletion.put(player, channel);

            String warningDelete = messages.getString("owner-leave-chatroom")
                    .replace("$chatroom$", channel.getName());
            player.sendMessage(warningDelete);
            player.sendMessage(messages.getString("owner-leave-chatroom2"));

        } else {
            // let them leave simply
            Player player = Bukkit.getPlayer(user.getUuid());

            user.leaveChatroom(channel);
            channel.removeMember(user.getUuid());
            // so it doesn't show them the empty chatroom
            if (channel.getMembers().size() == 0 && showMainMenu){
                new MainMenuPaging(player, PlayerChannels.getInstance()).show();
            }
        }
    }
    public static void joinChatroom (PlayerChannelUser player, Chatroom chatroom){
        ChannelFile messages = new ChannelFile(FileType.MESSAGES);
        Player clicker = Bukkit.getPlayer(player.getUuid());

        if (chatroom.isBanned(player.getUuid())){
            messages.sendConfigMsg(clicker, "banned-from-chatroom");
//            ChannelUtils.sendMenuMessage("You are banned!", clicker, null);
            return;
        }
        // check if player already exists in this chatroom since that could happen with new addition
        // of join command

        if(player.isMemberOf(chatroom)){
            messages.sendConfigMsg(clicker, "already-in-channel");
            return;
        }
        player.addChatroom(chatroom);
        if (!chatroom.isServerOwned() || (chatroom.isServerOwned()) && (!(clicker.hasPermission("playerchannels.admin") || clicker.hasPermission("playerchannels.moderator")))) {
            chatroom.addMember(new Pair<>(player.getUuid(), ChatRole.MEMBER));
        } else {
            // Idea here is that we want to automatically promote those with OP/playerchannels.admin or playerchannels.moderator
            // to be automatic staff in this channel

            // Two options: is server owned and either a moderator or admin

            if (clicker.hasPermission("playerchannels.admin")) {
                chatroom.addMember(new Pair<>(player.getUuid(), ChatRole.OWNER));

            } else if (clicker.hasPermission("playerchannels.moderator")) {
                chatroom.addMember(new Pair<>(player.getUuid(), ChatRole.MODERATOR));
            }

        }
    }


    public static void sendClickableCommand(Player player, String message, String command) {
        ChannelFile msgs = new ChannelFile(FileType.MESSAGES);

        if (message != null) {
            TextComponent messageComponent = new TextComponent(ChatColor.translateAlternateColorCodes('&', message));
            messageComponent.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, command));
            messageComponent.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(msgs.getString("click-to-copy")).create()));
            player.spigot().sendMessage(messageComponent);
        } else {
            player.sendMessage(ChatColor.RED + "There was an error loading the message for: " + message);
        }
    }
    private static void sendAdvancementNotification(Player player, String title, String description, Material iconMaterial) {

        try {
            PlayerChannels.getInstance().getToastApi().displayCustomToast(player, new ItemStack(iconMaterial), title, AdvancementFrameType.GOAL);

        } catch (Exception ex) {
            player.sendMessage(title);
        }

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

    public static void sendMsgFromConfig(Player player, String path) {
        if (player != null) {
            player.sendMessage(colorize(PlayerChannels.getInstance().getConfig().getString(path)));
        }
    }

}
