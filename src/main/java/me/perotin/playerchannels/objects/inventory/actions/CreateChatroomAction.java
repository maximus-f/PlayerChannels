package me.perotin.playerchannels.objects.inventory.actions;

/* Created by Perotin on 9/19/19 */

import com.github.stefvanschie.inventoryframework.gui.type.ChestGui;
import com.github.stefvanschie.inventoryframework.gui.type.util.Gui;
import me.perotin.playerchannels.PlayerChannels;
import me.perotin.playerchannels.events.chat_events.CreateChatroomInputEvent;
import me.perotin.playerchannels.objects.Chatroom;
import me.perotin.playerchannels.objects.InventoryHelper;
import me.perotin.playerchannels.objects.PreChatroom;
import me.perotin.playerchannels.objects.PlayerChannelUser;
import me.perotin.playerchannels.objects.inventory.Menu;
import me.perotin.playerchannels.objects.inventory.paging_objects.MainMenuPaging;
import me.perotin.playerchannels.storage.files.FileType;
import me.perotin.playerchannels.storage.files.ChannelFile;
import me.perotin.playerchannels.utils.ChannelUtils;
import me.perotin.playerchannels.utils.TutorialHelper;
import org.bukkit.OfflinePlayer;
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
            Player clicker = (Player) clickEvent.getWhoClicked();

            PreChatroom chatroom = new PreChatroom(clickEvent.getWhoClicked().getUniqueId());
            InventoryHelper helper = PlayerChannels.getInstance().getHelper();
            CreateChatroomInputEvent.getInstance().getInCreation().put(clickEvent.getWhoClicked().getUniqueId(), chatroom);
            Gui creationMenu = helper.setNavigationBar(helper.getCreationMenu(chatroom), (OfflinePlayer) clickEvent.getWhoClicked()).getFirst();
            Menu creationMenuObj = new Menu((ChestGui) creationMenu);
            helper.setNavigationBar(helper.getCreationMenu(chatroom), (OfflinePlayer) clickEvent.getWhoClicked()).getFirst().show(clickEvent.getWhoClicked());
            if (TutorialHelper.inTutorial.contains(clicker.getUniqueId())) {
                // send them the message 3
                ChannelFile messages = new ChannelFile(FileType.MESSAGES);
                ChannelUtils.sendMenuMessage(messages.getString("help-msg-4"), clicker, creationMenuObj);
            }

        };
    }

    /**
     * Actions for setting the name of a chatroom
     */
    public static Consumer<InventoryClickEvent> setNameConsumer(){
        return clickEvent -> {
            clickEvent.setCancelled(true);

            ChannelFile messages = new ChannelFile(FileType.MESSAGES);
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
            ChannelFile messages = new ChannelFile(FileType.MESSAGES);
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
            chatroom.setPublic(!chatroom.isPublic());

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
            if (clicker.hasPermission("playerchannels.saved")) {
                CreateChatroomInputEvent input = CreateChatroomInputEvent.getInstance();
                PreChatroom chatroom = input.getInCreation().get(clicker.getUniqueId());
                // Toggle saved status
                chatroom.setSaved(!chatroom.isSaved());

                input.showUpdatedMenu(clicker, chatroom);
            } else {
                ChannelUtils.sendMenuMessage(new ChannelFile(FileType.MESSAGES).getString("no-permission"), clicker, null);
            }
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
            PlayerChannelUser playerChannelUser = PlayerChannelUser.getPlayer(clicker.getUniqueId());
            ChannelFile messages = new ChannelFile(FileType.MESSAGES);
            CreateChatroomInputEvent input = CreateChatroomInputEvent.getInstance();
            PreChatroom chatroom = input.getInCreation().get(clicker.getUniqueId());
            /* conditions to check for:
            1. name not set
            2. description not set
             */

            // may not be fully correct
            if(chatroom.getName().equals("")){
                ChannelUtils.sendMenuMessage(messages.getString("name-missing"), clicker, null);
//                clicker.sendTitle(messages.getString("name-missing"), "", 10, 20*3, 10);
                return;
            }

            // create the chatroom
            Chatroom addedChatroom = PlayerChannels.getInstance().createChatroom(chatroom);
            playerChannelUser.addChatroom(addedChatroom);
            input.getInCreation().remove(clicker.getUniqueId());
            new MainMenuPaging(clicker, PlayerChannels.getInstance()).show();

            if (TutorialHelper.inTutorial.contains(clicker.getUniqueId())) {
                // end tutorial set
                TutorialHelper.inTutorial.remove(clicker.getUniqueId());
                ChannelUtils.sendMenuMessage(messages.getString("help-msg-5"), clicker, null);
                clicker.sendMessage(messages.getString("help-msg-6"));
                clicker.sendMessage(messages.getString("help-msg-7"));
                clicker.sendMessage(messages.getString("help-msg-8"));
                clicker.sendMessage(messages.getString("help-msg-9"));




            }

        };
    }


}
