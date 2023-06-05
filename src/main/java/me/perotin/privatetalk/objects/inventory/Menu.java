package me.perotin.privatetalk.objects.inventory;

import com.github.stefvanschie.inventoryframework.gui.type.ChestGui;
import com.github.stefvanschie.inventoryframework.gui.type.util.Gui;
import com.github.stefvanschie.inventoryframework.pane.StaticPane;
import me.perotin.privatetalk.PrivateTalk;
import org.bukkit.Bukkit;
import org.bukkit.entity.HumanEntity;

/**
 * @Class menu object to represent paging objects and static menus. Used to generalize #show method to present any
 * type of PrivateTalk menu to a player
 */
public class Menu {

    private ChestGui menu;

    private StaticPane navBar;
    public Menu(ChestGui menu) {
        this.menu = menu;

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
            return new ChestGui(3, "Boo", PrivateTalk.getInstance());
        } else {
            return menu;
        }
    }

    public void setMenu(ChestGui menu) {
        this.menu = menu;
    }
}
