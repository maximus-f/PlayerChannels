package me.perotin.privatetalk.objects.inventory.actions;

import org.bukkit.event.inventory.InventoryClickEvent;

import java.util.function.Consumer;

public class ChatroomModeratorAction {

    /**
     * @return inventory event for when a moderator kicks a member
     */
    public static Consumer<InventoryClickEvent> kickMember(){
        return inventoryClickEvent -> {
            inventoryClickEvent.setCancelled(true);
        };
    }
}
