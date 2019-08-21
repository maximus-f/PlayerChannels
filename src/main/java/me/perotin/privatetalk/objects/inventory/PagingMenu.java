package me.perotin.privatetalk.objects.inventory;

import com.github.stefvanschie.inventoryframework.Gui;
import com.github.stefvanschie.inventoryframework.pane.PaginatedPane;
import com.github.stefvanschie.inventoryframework.pane.StaticPane;

import java.util.List;

/* Created by Perotin on 8/16/19 */
public abstract class PagingMenu {

    private PaginatedPane pane;
    /** Used in the title of every menu, for identifying what type of paging menu it is **/
    private String identifier;

    public PagingMenu(String identifier){
        this.identifier = identifier;
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
     * @return an inventory formatted specifically for each (#PagingMenu)
     */
    public abstract StaticPane getBlankInventory();

    public PaginatedPane getPane(){
        return this.pane;
    }

}
