package me.perotin.privatetalk.objects.inventory.static_inventories;

import com.github.stefvanschie.inventoryframework.gui.type.ChestGui;
import me.perotin.privatetalk.PrivateTalk;
import me.perotin.privatetalk.storage.files.FileType;
import me.perotin.privatetalk.storage.files.PrivateFile;
import org.bukkit.entity.Player;

/**
 * Base class for any menu that is a static (non-paging) menu
 */
public class StaticMenu {

    private ChestGui menu;
    private Player viewer;
    private String title;
    private PrivateFile messages;

    public StaticMenu(Player viewer, String title) {
        this.viewer = viewer;
        this.title = title;
        this.menu = new ChestGui(6, title, PrivateTalk.getInstance());
        this.messages = new PrivateFile(FileType.MENUS);

    }

    public ChestGui getMenu() {
        return menu;
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
