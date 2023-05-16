package me.perotin.privatetalk.objects.inventory.static_inventories;

import com.github.stefvanschie.inventoryframework.gui.type.ChestGui;
import me.perotin.privatetalk.PrivateTalk;
import me.perotin.privatetalk.objects.inventory.Menu;
import me.perotin.privatetalk.storage.files.FileType;
import me.perotin.privatetalk.storage.files.PrivateFile;
import org.bukkit.entity.Player;

/**
 * Base class for any menu that is a static (non-paging) menu
 */
public class StaticMenu extends Menu {

    private Player viewer;
    private String title;
    private PrivateFile messages;

    public StaticMenu(Player viewer, String title) {
        super(new ChestGui(6, title, PrivateTalk.getInstance()));
        this.viewer = viewer;
        this.title = title;
        this.messages = new PrivateFile(FileType.MENUS);

    }


    public Player getViewer() {
        return viewer;
    }

    public String getTitle() {
        return title;
    }

    public PrivateFile getMessages() {
        return messages;
    }
}
