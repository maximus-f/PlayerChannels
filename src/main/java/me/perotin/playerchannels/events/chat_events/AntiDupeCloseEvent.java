package me.perotin.playerchannels.events.chat_events;

import me.perotin.playerchannels.storage.files.ChannelFile;
import me.perotin.playerchannels.storage.files.FileType;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

/*
    Class to update inventory for if players try to glitch items by spamming / closing inventory
 */
public class AntiDupeCloseEvent implements Listener {

//    @EventHandler
//    public void onClose(InventoryCloseEvent event) {
//        Player player = (Player) event.getPlayer();
//        Inventory inv = player.getInventory();
//
//        ChannelFile file = new ChannelFile(FileType.MENUS);
//        ConfigurationSection allSections = file.getConfigSection(""); // Root section
//
//        // Recursive method to iterate through all sections and subsections
//        processSection(inv, allSections);
//    }
//
//    private void processSection(Inventory inv, ConfigurationSection section) {
//        for (String key : section.getKeys(false)) {
//            Object value = section.get(key);
//            System.out.println("processing " + key);
//            if (value instanceof ConfigurationSection) {
//                // If this key represents another section, process that section recursively
//                processSection(inv, (ConfigurationSection) value);
//            } else if (key.equals("display") && section.contains("material")) {
//                // If this key represents a display name and there is a corresponding material
//                String displayName = ChatColor.translateAlternateColorCodes('&', section.getString("display"));
//                Material material = Material.matchMaterial(section.getString("material"));
//
//
//                // Check the player's inventory for this item and remove it
//                for (ItemStack itemStack : inv.getContents()) {
//                    if (itemStack != null && itemStack.getType() == material) {
//                        System.out.println(itemStack.getType());
//
//                        ItemMeta meta = itemStack.getItemMeta();
//                        if (meta != null && meta.getDisplayName().equals(displayName)) {
//                            inv.remove(itemStack);
//                        }
//                    }
//                }
//            }
//        }
//    }

}
