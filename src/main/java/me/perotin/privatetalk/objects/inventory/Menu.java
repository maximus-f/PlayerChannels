package me.perotin.privatetalk.objects.inventory;

import com.github.stefvanschie.inventoryframework.gui.type.ChestGui;
import com.github.stefvanschie.inventoryframework.gui.type.util.Gui;
import me.perotin.privatetalk.PrivateTalk;
import org.bukkit.Bukkit;
import org.bukkit.entity.HumanEntity;

/**
 * @Class menu object to represent paging objects and static menus. Used to generalize #show method to present any
 * type of PrivateTalk menu to a player
 */
public class Menu {

    private ChestGui menu;
    public Menu(ChestGui menu) {
        this.menu = menu;
    }

    public void show(HumanEntity entity){
        menu.show(entity);
    }

    public ChestGui getMenu() {
        if (menu == null){
            Bukkit.getLogger().info("getMenu() is null somehow still!!");
            return new ChestGui(3, "Boo", PrivateTalk.getInstance());
        } else {
            Bukkit.getLogger().info("getMenu() is not null!");

            return menu;
        }
    }

    public void setMenu(ChestGui menu) {
        this.menu = menu;
    }
}
