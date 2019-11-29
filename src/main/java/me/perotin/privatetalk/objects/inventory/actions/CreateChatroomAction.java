package me.perotin.privatetalk.objects.inventory.actions;

/* Created by Perotin on 9/19/19 */

import me.perotin.privatetalk.PrivateTalk;
import me.perotin.privatetalk.events.chat_events.CreateChatroomInputEvent;
import me.perotin.privatetalk.objects.InventoryHelper;
import me.perotin.privatetalk.objects.PreChatroom;
import me.perotin.privatetalk.storage.files.FileType;
import me.perotin.privatetalk.storage.files.PrivateFile;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;

import java.util.function.Consumer;

/**
 * Class for InventoryClickEvent consumer events
 */
public class CreateChatroomAction {


    /**
     * Actions after hitting the 'create chatroom' button
     */
    public static Consumer<InventoryClickEvent> createChatroomConsumer(){
        return clickEvent -> {
            clickEvent.setCancelled(true);
            PreChatroom chatroom = new PreChatroom(clickEvent.getWhoClicked().getUniqueId());
            InventoryHelper helper = PrivateTalk.getInstance().getHelper();
            CreateChatroomInputEvent.getInstance().getInCreation().put(clickEvent.getWhoClicked().getUniqueId(), chatroom);
            helper.getCreationMenu(chatroom).show(clickEvent.getWhoClicked());
        };
    }

    /**
     * Actions for setting the name of a chatroom
     */
    public static Consumer<InventoryClickEvent> setNameConsumer(){
        return clickEvent -> {
            clickEvent.setCancelled(true);

            PrivateFile messages = new PrivateFile(FileType.MESSAGES);
            CreateChatroomInputEvent.getInstance().getSetName().add(clickEvent.getWhoClicked().getUniqueId());
            clickEvent.getWhoClicked().closeInventory();


            clickEvent.getWhoClicked().sendMessage(messages.getString("set-name-message")
            .replace("$cancel$", messages.getString("cancel")));
        };
    }

    /**
     * Actions for setting the description of a chatroom
     */
    public static Consumer<InventoryClickEvent> setDescriptionConsumer(){
        return clickEvent -> {
            clickEvent.setCancelled(true);

            CreateChatroomInputEvent.getInstance().getSetDescription().add(clickEvent.getWhoClicked().getUniqueId());
            PrivateFile messages = new PrivateFile(FileType.MESSAGES);
            clickEvent.getWhoClicked().closeInventory();
            clickEvent.getWhoClicked().sendMessage(messages.getString("set-description-message")
                    .replace("$cancel$", messages.getString("cancel")));

        };
    }

    /**
     * Actions for toggling the status of a chatroom, i.e. private or public
     */
    public static Consumer<InventoryClickEvent> toggleStatusConsumer(){
        return clickEvent -> {
            // Can cast to player because will only be called in those scenarios
            clickEvent.setCancelled(true);

            Player clicker = (Player) clickEvent.getWhoClicked();
            CreateChatroomInputEvent input = CreateChatroomInputEvent.getInstance();
            PreChatroom chatroom = input.getInCreation().get(clicker.getUniqueId());
            if(chatroom.isPublic()){
                chatroom.setPublic(false);
            } else  chatroom.setPublic(true);

            input.showUpdatedMenu(clicker, chatroom);

        };
    }

    /**
     * Actions for toggling whether a chatroom should be persistent i.e. saved
     */
    public static Consumer<InventoryClickEvent> toggleSavedConsumer(){
        return clickEvent -> {
            // Can cast to player because will only be called in those scenarios
            clickEvent.setCancelled(true);

            Player clicker = (Player) clickEvent.getWhoClicked();
            CreateChatroomInputEvent input = CreateChatroomInputEvent.getInstance();
            PreChatroom chatroom = input.getInCreation().get(clicker.getUniqueId());
            if(chatroom.isSaved()){
                chatroom.setSaved(false);
            } else  chatroom.setSaved(true);

            input.showUpdatedMenu(clicker, chatroom);
        };
    }

    /**
     * Clicking the 'create' button
     */
    public static Consumer<InventoryClickEvent> clickCreateButtonConsumer(){
        return clickEvent -> {
            // Can cast to player because will only be called in those scenarios
            clickEvent.setCancelled(true);

            Player clicker = (Player) clickEvent.getWhoClicked();
            PrivateFile messages = new PrivateFile(FileType.MESSAGES);
            CreateChatroomInputEvent input = CreateChatroomInputEvent.getInstance();
            PreChatroom chatroom = input.getInCreation().get(clicker.getUniqueId());
            /* conditions to check for:
            1. name not set
            2. description not set
             */

            // may not be fully correct
            if(chatroom.getName().equals("")){
                clicker.sendTitle(messages.getString("name-missing"), "", 10, 20*3, 10);
                return;
            } else if(chatroom.getDescription().equals("")){
                clicker.sendTitle(messages.getString("description-missing"), "", 10, 20*3, 10);
                return;
            }
            // create the chatroom
            PrivateTalk.getInstance().createChatroom(chatroom);
            input.getInCreation().remove(clicker.getUniqueId());

        };
    }


}
