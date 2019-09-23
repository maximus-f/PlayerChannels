package me.perotin.privatetalk.objects.inventory;

/* Created by Perotin on 9/19/19 */

import me.perotin.privatetalk.events.chat_events.CreateChatroomInputEvent;
import me.perotin.privatetalk.objects.PreChatroom;
import org.bukkit.event.inventory.InventoryClickEvent;

import java.util.function.Consumer;

/**
 * Class for InventoryClickEvent consumer events
 */
public class InventoryAction {


    // TODO Close inventories / send messages etc.

    public static Consumer<InventoryClickEvent> createChatroomConsumer(){
        return clickEvent -> {
            CreateChatroomInputEvent.getInstance().getInCreation().put(clickEvent.getWhoClicked().getUniqueId(), new PreChatroom(clickEvent.getWhoClicked().getUniqueId()));
        };
    }
    public static Consumer<InventoryClickEvent> setNameConsumer(){
        return clickEvent -> {
            CreateChatroomInputEvent.getInstance().getSetName().add(clickEvent.getWhoClicked().getUniqueId());
        };
    }

    public static Consumer<InventoryClickEvent> setDescriptionConsumer(){
        return clickEvent -> {
            CreateChatroomInputEvent.getInstance().getSetDescription().add(clickEvent.getWhoClicked().getUniqueId());
        };
    }
}
