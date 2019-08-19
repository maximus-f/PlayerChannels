package me.perotin.privatetalk.utils;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/* Created by Perotin on 8/17/19 */

/**
 * Chain-based Item util to make creating, naming, and setting meta a lot quicker.
 */
public class ItemStackUtils {

    private ItemStack item;
    private ItemMeta meta;
    private String name;
    private List<String> lore;

    public ItemStackUtils(Material type) {
        this.item = new ItemStack(type);
        this.meta = item.getItemMeta();
        this.name = item.getItemMeta().getDisplayName();
        this.lore = item.getItemMeta().getLore();

    }

    public ItemStackUtils setName(String name) {
        this.name = name;
        return this;
    }

    public ItemStackUtils setLore(String... lore) {
        List<String> lores = new ArrayList<>();
        Collections.addAll(lores, lore);
        this.lore = lores;
        return this;
    }
    public ItemStack build(){
        meta.setDisplayName(name);
        meta.setLore(lore);
        item.setItemMeta(meta);
        return item;

    }





}
