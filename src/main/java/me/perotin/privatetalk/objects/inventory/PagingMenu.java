package me.perotin.privatetalk.objects.inventory;


import com.github.stefvanschie.inventoryframework.gui.GuiItem;
import com.github.stefvanschie.inventoryframework.gui.type.ChestGui;
import com.github.stefvanschie.inventoryframework.pane.PaginatedPane;
import com.github.stefvanschie.inventoryframework.pane.Pane;
import com.github.stefvanschie.inventoryframework.pane.StaticPane;
import me.perotin.privatetalk.PrivateTalk;
import me.perotin.privatetalk.objects.InventoryHelper;
import me.perotin.privatetalk.storage.files.FileType;
import me.perotin.privatetalk.storage.files.PrivateFile;
import me.perotin.privatetalk.utils.ItemStackUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.stream.Collectors;

/* Created by Perotin on 8/16/19 */


/** Base class for all paging menus in PrivateTalk, wrapper for PaginatedPane object primarily **/
public abstract class PagingMenu {

    protected PaginatedPane pane;
    private ChestGui menu;
    private Player viewer;
    /** Used in the title of every menu, for identifying what type of paging menu it is **/
    private final String identifier;

    private StaticPane pagingNavBar;

    public PagingMenu(String identifier, int rows, Player viewer){
        this.identifier = identifier;
        this.menu = new ChestGui(rows, identifier);
        this.viewer = viewer;
        this.pagingNavBar = new StaticPane(0, 5, 9, 1);

        setPaginatedPane();
        setPagingNavigation(menu); // Refactor this to main menu perhaps
    }
    /**
     * Sets the current page to + 1 if within bounds
     */
    public void next(){
        if (pane.getPages() > pane.getPage() + 1) {
            pane.setPage(pane.getPage() + 1);
            menu.setTitle(identifier + " Page: " + pane.getPage() + 1);
            menu.update();
        }

    }

    /**
     * Sets the current page to the previous page if one exists
     */
    public void previous(){
        if (pane.getPage() > 0) {
            pane.setPage(pane.getPage() - 1);
            menu.setTitle(identifier + " Page: " + pane.getPage() + 1);

            menu.update();
        }
    }

    public String getIdentifier() {
        return identifier;
    }


    public void show(){
        getMenu().show(viewer);
    }

    private void setPagingNavigation(ChestGui menu){
        PrivateFile file = new PrivateFile(FileType.MENUS);
        List<Integer> decoSlots = getAsInts(file.getConfiguration().getStringList("paging-nav-bar.deco-item.slots"));
        int nextSlot = file.getConfiguration().getInt("paging-nav-bar.next-item.slot");
        int backSlot = file.getConfiguration().getInt("paging-nav-bar.back-item.slot");

        getPagingNavBar().addItem(back_item(), backSlot, 0);
        getPagingNavBar().addItem(next_item(), nextSlot, 0);

        GuiItem decoItem = InventoryHelper.DECO_ITEM();

        Bukkit.broadcastMessage("SetPagingNav decoItem action: " + decoItem);
        for (int x : decoSlots) {
            Bukkit.broadcastMessage("adding deco item for " + x);

            getPagingNavBar().addItem(decoItem, x, 0);

        }
        menu.addPane(getPagingNavBar());
    }

    /**
     * @param stringList to convert
     * @return converted int list
     * @apiNote will break if stringList doesn't meet parsing conditions
     */
    private List<Integer> getAsInts(List<String> stringList) {
        return stringList.stream().map(Integer::parseInt).collect(Collectors.toList());
    }

    public StaticPane getPagingNavBar() {
        return pagingNavBar;
    }

    public void setPagingNavBar(StaticPane pagingNavBar) {
        this.pagingNavBar = pagingNavBar;
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

    protected void setPaginatedPane(){
        this.pane = new PaginatedPane(1, 1, 7, 4);

        menu.addPane(pane);
    }


    /**
     * @return item used to navigate backwards in a menu
     */
    private GuiItem back_item() {
        PrivateFile items = new PrivateFile(FileType.MENUS);
        ItemStackUtils item = new ItemStackUtils(Material.getMaterial(items.getString("global-items.back-item.material")));
        item.setName(items.getString("global-items.back-item.display"));

        return new GuiItem(item.build(), inventoryClickEvent ->{
            inventoryClickEvent.setCancelled(true);
            previous();
        });
    }

    /**
     * @return item used to navigate forwards in a menu
     */
    public GuiItem next_item() {
        PrivateFile items = new PrivateFile(FileType.MENUS);
        ItemStackUtils item = new ItemStackUtils(Material.getMaterial(items.getString("global-items.next-item.material")));
        item.setName(items.getString("global-items.next-item.display"));

        return new GuiItem(item.build(), inventoryClickEvent -> {
            inventoryClickEvent.setCancelled(true);
            next();
        });
    }

}
