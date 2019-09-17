package me.perotin.privatetalk.objects.inventory;

import com.github.stefvanschie.inventoryframework.Gui;
import com.github.stefvanschie.inventoryframework.pane.StaticPane;
import me.perotin.privatetalk.PrivateTalk;

/* Created by Perotin on 8/22/19 */

/**
 * Wrapper class for @Gui object
 */

// Consider deleting this class, a bit useless IMO.
public class PrivateInventory {

    private Gui gui;


    public PrivateInventory(String name, int rows){
        this.gui = new Gui(PrivateTalk.getInstance(), rows, name);
    }

    public Gui getGui() {
        return gui;
    }





}
