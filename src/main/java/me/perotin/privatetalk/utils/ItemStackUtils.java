package me.perotin.privatetalk.utils;

import me.perotin.privatetalk.PrivateTalk;
import me.perotin.privatetalk.storage.files.FileType;
import me.perotin.privatetalk.storage.files.PrivateFile;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

/* Created by Perotin on 8/17/19 */

/**
 * Chain-based Item util to make creating, naming, and setting meta a lot quicker.
 */
public class ItemStackUtils {


    private ItemStack item;
    private ItemMeta meta;
    private String name;
    private List<String> lore;
    private OfflinePlayer owningPlayer;


    public ItemStackUtils(Material type) {
        this.item = new ItemStack(type);
        this.meta = item.getItemMeta();
        this.name = item.getItemMeta().getDisplayName();
        this.lore = item.getItemMeta().getLore();

    }

    public ItemStackUtils(Material type, OfflinePlayer owner){
        this(type);
        this.owningPlayer = owner;
    }

    /**
     *
     * @param name to set
     * @return this with set name
     */
    public ItemStackUtils setName(String name) {
        this.name = name;
        return this;
    }

    /**
     * @param lore to set, can be single string
     */
    public ItemStackUtils setLore(String... lore) {
        List<String> lores = new ArrayList<>();
        Collections.addAll(lores, lore);
        this.lore = lores;
        return this;
    }

    /**
     * @param owner to set
     **/
    public ItemStackUtils setOwner(OfflinePlayer owner){
        this.owningPlayer = owner;
        return this;
    }

    /**
     * @param lore to set in form of a list
     */
    public ItemStackUtils setLore(List<String> lore) {
        this.lore = lore;
        return this;
    }

    // not sure if the SkullMeta part will work
    public ItemStack build(){
        if(owningPlayer != null){
            SkullMeta skullMeta = (SkullMeta) meta;
            skullMeta.setOwningPlayer(owningPlayer);
        }
        meta.setDisplayName(name);
        meta.setLore(lore);


        item.setItemMeta(meta);

        return item;

    }






}
