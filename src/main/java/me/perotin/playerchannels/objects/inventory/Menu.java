package me.perotin.playerchannels.objects.inventory;

import com.github.stefvanschie.inventoryframework.gui.type.ChestGui;
import com.github.stefvanschie.inventoryframework.pane.StaticPane;
import me.perotin.playerchannels.PlayerChannels;
import me.perotin.playerchannels.storage.files.FileType;
import me.perotin.playerchannels.storage.files.ChannelFile;
import org.bukkit.entity.HumanEntity;

/**
 * @Class menu object to represent paging objects and static menus. Used to generalize #show method to present any
 * type of PlayerChannels menu to a player
 */
public class Menu {

    private ChestGui menu;

    private StaticPane navBar;
    private ChannelFile messages;
    public Menu(ChestGui menu) {
        this.menu = menu;
        this.messages = new ChannelFile(FileType.MESSAGES);

    }

    public String getStringFromMessages(String path) {
        return messages.getString(path);
    }

    public void show(HumanEntity entity){
        menu.show(entity);
    }

    public void setNavBar(StaticPane navBar) {
        this.navBar = navBar;
    }

    public StaticPane getNavBar() {
        return navBar;
    }

    public ChestGui getMenu() {
        if (menu == null){
            return new ChestGui(3, "Boo", PlayerChannels.getInstance());
        } else {
            return menu;
        }
    }

    public void setMenu(ChestGui menu) {
        this.menu = menu;
    }
}
