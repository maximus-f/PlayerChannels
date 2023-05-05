package me.perotin.privatetalk.objects.inventory;


import com.github.stefvanschie.inventoryframework.gui.GuiItem;
import com.github.stefvanschie.inventoryframework.gui.type.ChestGui;
import com.github.stefvanschie.inventoryframework.pane.PaginatedPane;
import com.github.stefvanschie.inventoryframework.pane.Pane;
import me.perotin.privatetalk.PrivateTalk;
import org.bukkit.entity.Player;

import java.util.List;

/* Created by Perotin on 8/16/19 */


/** Base class for all paging menus in PrivateTalk, wrapper for PaginatedPane object primarily **/
public abstract class PagingMenu {

    protected PaginatedPane pane;
    private ChestGui menu;
    private Player viewer;
    /** Used in the title of every menu, for identifying what type of paging menu it is **/
    private final String identifier;

    public PagingMenu(String identifier, int rows, Player viewer){
        this.identifier = identifier;
        this.menu = new ChestGui(rows, identifier);
      //  menu.addPane(pane);
        this.viewer = viewer;
        PrivateTalk.getInstance().getHelper().setPagingNavBar(menu, true, true);
    }
    /**
     * Gives the next inventory in the sequence, null if it is the last inventory
     * @return the next inventory or null.
     */
    public void next(){
        pane.setPage(pane.getPage()+1);

    }
    public void previous(){
       pane.setPage(pane.getPage()-1);
    }

    public String getIdentifier() {
        return identifier;
    }


    public void show(){
        getMenu().show(viewer);
    }


    /**
     * Implement to the required design for each paging menu.
     * @return a list of inventorsy formatted specifically for each (#PagingMenu)
     */
    protected abstract List<GuiItem> generatePages();

    protected PaginatedPane getPaginatedPane(){
        return this.pane;
    }

    public ChestGui getMenu(){
        return this.menu;
    }

    public void addPaneToGui(Pane pane) {
        menu.addPane(pane);
    }

    /**
     * @return player viewing the pane
     */
    public Player getViewer(){
        return viewer;
    }

    protected abstract void setPaginatedPane();

}
