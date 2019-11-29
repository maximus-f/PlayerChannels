package me.perotin.privatetalk.objects.inventory.paging_objects;

import com.github.stefvanschie.inventoryframework.GuiItem;
import com.github.stefvanschie.inventoryframework.pane.PaginatedPane;
import me.perotin.privatetalk.PrivateTalk;
import me.perotin.privatetalk.objects.Chatroom;
import me.perotin.privatetalk.objects.inventory.PagingMenu;
import me.perotin.privatetalk.storage.files.FileType;
import me.perotin.privatetalk.storage.files.PrivateFile;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

/* Created by Perotin on 11/29/19 */

/**
 * Class for all Main Menu instances that are shown to players
 */
public class MainMenuPaging extends PagingMenu {


    private PrivateFile messages;
    private PrivateTalk plugin;
    public MainMenuPaging(Player viewer, PrivateTalk plugin){
        super(viewer.getName()+"-main", 6, viewer);
        this.messages = new PrivateFile(FileType.MESSAGES);
        this.pane = new PaginatedPane(2, 1, 7, 4);
        this.plugin = plugin;

    }



    @Override
    protected List<GuiItem> generatePages() {
        List<GuiItem> items = new ArrayList<>();
        List<Chatroom> toDisplay = plugin.getChatrooms();


        return items;
    }
}
