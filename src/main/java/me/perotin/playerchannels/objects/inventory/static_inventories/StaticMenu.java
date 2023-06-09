package me.perotin.playerchannels.objects.inventory.static_inventories;

import com.github.stefvanschie.inventoryframework.gui.type.ChestGui;
import me.perotin.playerchannels.PlayerChannels;
import me.perotin.playerchannels.objects.inventory.Menu;
import me.perotin.playerchannels.storage.files.FileType;
import me.perotin.playerchannels.storage.files.ChannelFile;
import org.bukkit.entity.Player;

/**
 * Base class for any menu that is a static (non-paging) menu
 */
public class StaticMenu extends Menu {

    private Player viewer;
    private String title;
    private ChannelFile messages;

    public StaticMenu(Player viewer, String title) {
        super(new ChestGui(6, title, PlayerChannels.getInstance()));
        this.viewer = viewer;
        this.title = title;
        this.messages = new ChannelFile(FileType.MENUS);

    }


    public Player getViewer() {
        return viewer;
    }

    public String getTitle() {
        return title;
    }

    public ChannelFile getMessages() {
        return messages;
    }
}
