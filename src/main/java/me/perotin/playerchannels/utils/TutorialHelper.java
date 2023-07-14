package me.perotin.playerchannels.utils;

import me.perotin.playerchannels.PlayerChannels;
import me.perotin.playerchannels.objects.inventory.paging_objects.MainMenuPaging;
import me.perotin.playerchannels.storage.files.ChannelFile;
import me.perotin.playerchannels.storage.files.FileType;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.function.Consumer;

public class TutorialHelper {

    private PlayerChannels instance;

    public static Set<UUID> inTutorial = new HashSet<>();
    public TutorialHelper (PlayerChannels instance) {
        this.instance = instance;

    }

    /**
     * Inventory Consumer event for when a player clicks on the help icon
     *
     *  Should close the inventory, send some messages, then open the main menu again
     * This method is not accurate
     * @return
     */
    public Consumer<InventoryClickEvent> clickOnHelp() {
        return event -> {
            ChannelFile messages = new ChannelFile(FileType.MESSAGES);
            Player clicker = (Player) event.getWhoClicked();
            clicker.closeInventory();
            event.setCancelled(true);
            if (inTutorial.contains(clicker.getUniqueId())) {
                // already contains it
                ChannelUtils.sendMenuMessage(messages.getString("already-in-tutorial"), clicker, null);
                return;
            }
            inTutorial.add(clicker.getUniqueId());


            clicker.sendMessage(messages.getString("help-msg-1")
                    .replace("$cancel$", PlayerChannels.getInstance()
                            .getConfig().getString("cancel-tutorial")));
            for (int j = 2; j <= 3; j++) {
                int finalJ = j;
                new BukkitRunnable(){
                    @Override
                    public void run() {
                        if (TutorialHelper.inTutorial.contains(clicker.getUniqueId())) {
                            if (finalJ != 3) {
                                clicker.sendMessage(messages.getString("help-msg-" + finalJ));
                            }
                            if (finalJ == 3) {
                                MainMenuPaging mainMenuPaging = new MainMenuPaging(clicker, instance);
                                mainMenuPaging.getMenu().show(clicker);
                                ChannelUtils.sendMenuMessage(messages.getString("help-msg-3"), clicker, null);
                            }
                        }
                    }
                }.runTaskLater(PlayerChannels.getInstance(), finalJ * 45L);
            }
        };
    }
}
