package me.perotin.playerchannels.utils;

import me.perotin.playerchannels.PlayerChannels;
import me.perotin.playerchannels.objects.inventory.paging_objects.MainMenuPaging;
import me.perotin.playerchannels.storage.files.ChannelFile;
import me.perotin.playerchannels.storage.files.FileType;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Set;
import java.util.function.Consumer;

public class TutorialHelper {

    private PlayerChannels instance;

    public static Set<Player> inTutorial;
    public TutorialHelper (PlayerChannels instance) {
        this.instance = instance;
    }

    /**
     * Inventory Consumer event for when a player clicks on the help icon
     * @return
     */
    public Consumer<InventoryClickEvent> clickOnHelp() {
        return event -> {
            ChannelFile messages = new ChannelFile(FileType.MESSAGES);
            Player clicker = (Player) event.getWhoClicked();

            inTutorial.add(clicker);
            int j = 1;
            while (j < 3) {
                int finalJ = j;
                new BukkitRunnable(){
                    @Override
                    public void run() {
                        clicker.sendMessage(messages.getString("help-msg-"+ finalJ));
                    }
                }.runTaskLater(PlayerChannels.getInstance(), 3*20);

                j++;
            }
            // Open the menu for them
            MainMenuPaging mainMenuPaging = new MainMenuPaging(clicker, instance);
            mainMenuPaging.getMenu().show(clicker);

        };
    }
}
