package me.perotin.privatetalk.objects.inventory.paging_objects;


import com.github.stefvanschie.inventoryframework.gui.GuiItem;
import com.github.stefvanschie.inventoryframework.gui.type.ChestGui;
import com.github.stefvanschie.inventoryframework.gui.type.util.Gui;
import com.github.stefvanschie.inventoryframework.pane.PaginatedPane;
import com.github.stefvanschie.inventoryframework.pane.Pane;
import com.github.stefvanschie.inventoryframework.pane.StaticPane;
import me.perotin.privatetalk.PrivateTalk;
import me.perotin.privatetalk.objects.InventoryHelper;
import me.perotin.privatetalk.objects.inventory.Menu;
import me.perotin.privatetalk.storage.files.FileType;
import me.perotin.privatetalk.storage.files.PrivateFile;
import me.perotin.privatetalk.utils.ItemStackUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/* Created by Perotin on 8/16/19 */


/** Base class for all paging menus in PrivateTalk, wrapper for PaginatedPane object primarily **/
public abstract class PagingMenu extends Menu {

    protected PaginatedPane pane;
    private Player viewer;
    /** Used in the title of every menu, for identifying what type of paging menu it is **/
    private String identifier;

    /**
     * Menu that player came from to reach this paging menu. Can be null if it does not exist
     */
    private Gui backMenu;

    private StaticPane pagingNavBar;

    public PagingMenu(String identifier, int rows, Player viewer, Gui backMenu){
        super(new ChestGui(rows, identifier, PrivateTalk.getInstance()));
        this.identifier = identifier;
        this.backMenu = backMenu;
        this.viewer = viewer;
        this.pagingNavBar = new StaticPane(0, 5, 9, 1);

        setPaginatedPane();
        setPagingNavigation(getMenu()); // Refactor this to main menu perhaps
    }
    /**
     * Sets the current page to + 1 if within bounds
     */
    public void next(){
        if (pane.getPages() > pane.getPage() + 1) {
            pane.setPage(pane.getPage() + 1);
            int pageNum = pane.getPage();
            getMenu().setTitle(identifier + " Page: " + pageNum);
            getMenu().update();
        }

    }

    /**
     * Sets the current page to the previous page if one exists
     */
    public void previous(Gui backMenu){
        if (pane.getPage() > 0) {
            pane.setPage(pane.getPage() - 1);
            getMenu().setTitle(identifier + " Page: " + pane.getPage());

            getMenu().update();
        } else {
            Bukkit.broadcastMessage("1");
            if (backMenu != null) {
                Bukkit.broadcastMessage("2");
                // Set the nav bar again
                PrivateTalk.getInstance().getHelper().setNavigationBar((ChestGui) backMenu, getViewer());

                backMenu.show(viewer);
            }
        }
    }

    public String getIdentifier() {
        return identifier;
    }


    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    public void show(){
        getMenu().show(viewer);
    }

    private void setPagingNavigation(ChestGui menu){
        PrivateFile file = new PrivateFile(FileType.MENUS);
        List<Integer> decoSlots = getAsInts(file.getConfiguration().getStringList("paging-nav-bar.deco-item.slots"));
        int nextSlot = file.getConfiguration().getInt("paging-nav-bar.next-item.slot");
        int backSlot = file.getConfiguration().getInt("paging-nav-bar.back-item.slot");

        getPagingNavBar().addItem(back_item(getBackMenu()), backSlot, 0);
        getPagingNavBar().addItem(next_item(), nextSlot, 0);

        GuiItem decoItem = InventoryHelper.DECO_ITEM();

        for (int x : decoSlots) {
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



    public void addPaneToGui(Pane pane) {
        getMenu().addPane(pane);
    }

    /**
     * @return player viewing the pane
     */
    public Player getViewer(){
        return viewer;
    }

    protected void setPaginatedPane(){
        this.pane = new PaginatedPane(1, 1, 7, 4);

        getMenu().addPane(pane);
    }

    /**
     * @param pane to add to Gui
     */
    public void addPane(Pane pane) {
        getMenu().addPane(pane);
    }

    public Gui getBackMenu() {
        return backMenu;
    }

    public void setBackMenu(Gui backMenu) {
        this.backMenu = backMenu;
    }

    /**
     * @return item used to navigate backwards in a menu
     */
    private GuiItem back_item(Gui backMenu) {
        PrivateFile items = new PrivateFile(FileType.MENUS);
        ItemStackUtils item = new ItemStackUtils(Material.getMaterial(items.getString("global-items.back-item.material")));
        item.setName(items.getString("global-items.back-item.display"));

        return new GuiItem(item.build(), inventoryClickEvent ->{
            inventoryClickEvent.setCancelled(true);
            previous(backMenu);
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
