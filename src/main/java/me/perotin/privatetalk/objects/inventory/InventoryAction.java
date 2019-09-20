package me.perotin.privatetalk.objects.inventory;

/* Created by Perotin on 9/19/19 */

import org.bukkit.event.inventory.InventoryClickEvent;

import java.util.function.Consumer;

/**
 * Class for InventoryClickEvent consumer events
 */
public class InventoryAction {


    public static Consumer<InventoryClickEvent> clickAnvilConsumer(){
        return clickEvent -> {

        };
    }
}
