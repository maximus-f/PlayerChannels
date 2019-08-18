package me.perotin.privatetalk.objects.inventory;

import org.bukkit.Bukkit;
import org.bukkit.inventory.Inventory;

/* Created by Perotin on 8/15/19 */
public class PrivateInventory  {

    private Inventory inventory;
    private final String name;

    public PrivateInventory(String name, int slots) {
        this.inventory = Bukkit.createInventory(null, slots, name);
        this.name = name;
    }

    public Inventory getInventory() {
        return inventory;
    }

    public String getName() {
        return name;
    }
}
