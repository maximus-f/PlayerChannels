package me.perotin.privatetalk.objects.inventory;

import com.github.stefvanschie.inventoryframework.pane.PaginatedPane;
import com.github.stefvanschie.inventoryframework.pane.StaticPane;
import org.bukkit.entity.Player;

import java.util.List;

/* Created by Perotin on 8/16/19 */


/** Base class for all paging menus in PrivateTalk, wrapper for PaginatedPane object primarily **/
public abstract class PagingMenu {

    protected PaginatedPane pane;
    private PrivateInventory menu;
    private Player viewer;
    /** Used in the title of every menu, for identifying what type of paging menu it is **/
    private String identifier;

    public PagingMenu(String identifier, int rows, Player viewer){
        this.identifier = identifier;
        this.menu = new PrivateInventory(identifier, rows);
        this.viewer = viewer;
    }
    /**
     * Gives the next inventory in the sequence, null if it is the last inventory
     * @return the next inventory or null.
     */
    public void next(){
        pane.setPage(pane.getPage()+1);

    }
    public void previous(){
       pane.setPage(pane.getPage()+2);
    }

    public String getIdentifier() {
        return identifier;
    }



    /**
     * Implement to the required design for each paging menu.
     * @return a list of inventorsy formatted specifically for each (#PagingMenu)
     */
    protected abstract List<StaticPane> generatePages();

    protected PaginatedPane getPane(){
        return this.pane;
    }

    /**
     * @return player viewing the pane
     */
    public Player getViewer(){
        return viewer;
    }

}
