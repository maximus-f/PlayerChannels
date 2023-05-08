package me.perotin.privatetalk.objects.inventory.paging_objects;

import com.github.stefvanschie.inventoryframework.gui.GuiItem;
import com.github.stefvanschie.inventoryframework.gui.type.util.Gui;
import me.perotin.privatetalk.PrivateTalk;
import me.perotin.privatetalk.objects.inventory.PagingMenu;
import org.bukkit.entity.Player;

import java.util.List;

public class PlayerListPager extends PagingMenu {


    public PlayerListPager(String identifier, Player viewer, Gui backMenu) {
        super("PlayerListPager", 6, viewer, new MainMenuPaging(viewer, PrivateTalk.getInstance()).getMenu());
    }

    @Override
    protected List<GuiItem> generatePages() {
        return null;
    }
}
