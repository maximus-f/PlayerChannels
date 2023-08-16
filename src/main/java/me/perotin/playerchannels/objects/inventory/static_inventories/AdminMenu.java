package me.perotin.playerchannels.objects.inventory.static_inventories;

import me.perotin.playerchannels.objects.InventoryHelper;
import org.bukkit.entity.Player;

public class AdminMenu extends StaticMenu {


    private InventoryHelper helper;


    public AdminMenu(Player viewer, String title) {
        super(viewer, title);
        
    }
}
