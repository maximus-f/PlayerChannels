package me.perotin.privatetalk.objects;

/* Created by Perotin on 8/27/19 */

import com.github.stefvanschie.inventoryframework.Gui;
import com.github.stefvanschie.inventoryframework.GuiItem;
import com.github.stefvanschie.inventoryframework.pane.StaticPane;
import me.perotin.privatetalk.objects.inventory.PrivateInventory;
import me.perotin.privatetalk.storage.Pair;
import me.perotin.privatetalk.storage.files.FileType;
import me.perotin.privatetalk.storage.files.PrivateFile;
import me.perotin.privatetalk.utils.ItemStackUtils;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * Class for bringing up static parts of inventories like the nav-bar etc.
 */
public class InventoryHelper {

    private PrivateFile file;
    private StaticPane navBar;
    private StaticPane pagingNavigationBar;
    private StaticPane creationMenu;
    /** Consumer for making decoration items cancel the click event and nothing else **/
    private Consumer<InventoryClickEvent> doNothing;

    public InventoryHelper(){
        this.file = new PrivateFile(FileType.MENUS);
        this.navBar = new StaticPane(0, 0, 9, 1);
        this.pagingNavigationBar = new StaticPane(0, 6, 9, 1);
        this.doNothing = (event) -> event.setCancelled(true);
        setNavBar();
        setPagingNavigationBar();
    }

    /**
     *
     * @param inventory to set
     * @return sets the nav bar for any given inventory
     */
    public  PrivateInventory setNavigationBar(PrivateInventory inventory, OfflinePlayer owner){
        Gui gui = inventory.getGui();
        navBar.addItem(new GuiItem(getItemFrom(Material.PLAYER_HEAD, "nav-bar.player-profile-head", owner).getFirst()), getItemFrom(Material.PLAYER_HEAD, "nav-bar.player-profile-head", owner).getSecond(), 0);
        gui.addPane(navBar);
        return inventory;
    }


    /**
     *
     * @param inventory to set paging-nav-bar
     * @return inventory with paging-navar bar
     */
    public PrivateInventory setPagingNavBar(PrivateInventory inventory){
        inventory.getGui().addPane(pagingNavigationBar);
        return inventory;
    }



    /**
     *
     * @param material path to the item in menus.yml, for example, "nav-bar.player-profile-head" will retrieve said path
     * @return
     */
    private Pair<ItemStack, Integer> getItemFrom(Material material, String path, OfflinePlayer owner){
        PrivateFile file = new PrivateFile(FileType.MENUS);
        ItemStackUtils builder = new ItemStackUtils(material, owner);
        builder.setName(file.getString(path+".display"));
        builder.setLore(file.getConfiguration().getStringList(path+".lore"));
        return new Pair<>(builder.build(), file.getConfiguration().getInt(path+".slot"));

    }

    /**
     * Sets the navigation bar that appears in most menus at the very top
     */
    private void setNavBar(){
        PrivateFile file = new PrivateFile(FileType.MENUS);
        Pair head = getItemFrom(Material.PLAYER_HEAD, "nav-bar.player-profile-head", null);
        Pair createChatroom = getItemFrom(Material.PLAYER_HEAD, "nav-bar.create-chatroom", null);
        Pair invites = getItemFrom(Material.PLAYER_HEAD, "nav-bar.manage-invites", null);
        navBar.addItem(new GuiItem((ItemStack) head.getFirst()), (int) head.getSecond(), 0);
        navBar.addItem(new GuiItem((ItemStack) createChatroom.getFirst()), (int) createChatroom.getSecond(), 0);
        navBar.addItem(new GuiItem((ItemStack) invites.getFirst()), (int) invites.getSecond(), 0);
        GuiItem deco = new GuiItem(DECO_ITEM(),  doNothing);
        List<Integer> slots = getAsInts(file.getConfiguration().getStringList("nav-bar.deco-item.slots"));
        for(int x : slots) {
            navBar.addItem(deco, x, 0);
        }

    }

    private void setPagingNavigationBar(){
        PrivateFile file = new PrivateFile(FileType.MENUS);
        List<Integer> decoSlots = getAsInts(file.getConfiguration().getStringList("paging-nav-bar.deco-item.slots"));
        int nextSlot = file.getConfiguration().getInt("paging-nav-bar.next-item.slot");
        int backSlot = file.getConfiguration().getInt("paging-nav-bar.back-item.slot");
        for(int x : decoSlots) {
            pagingNavigationBar.addItem(new GuiItem(DECO_ITEM(), doNothing), x, 6);
        }
        pagingNavigationBar.addItem(new GuiItem(BACK_ITEM()), backSlot, 6);
        pagingNavigationBar.addItem(new GuiItem(NEXT_ITEM()), nextSlot, 6);

    }

    /**
     * @apiNote will break if stringList doesn't mean parsing conditions
     * @param stringList to convert
     * @return converted int list
     */
    private List<Integer> getAsInts(List<String> stringList) {
        return stringList.stream().map(Integer::parseInt).collect(Collectors.toList());
    }

    public static ItemStack DECO_ITEM(){
        PrivateFile items = new PrivateFile(FileType.MENUS);
        ItemStackUtils item = new ItemStackUtils(Material.getMaterial(items.getString("global-items.deco-item.material")));
        item.setName(items.getString("global-items.deco-item.material"));
        return item.build();
    }

    public static ItemStack BACK_ITEM(){
        PrivateFile items = new PrivateFile(FileType.MENUS);
        ItemStackUtils item = new ItemStackUtils(Material.getMaterial(items.getString("global-items.back-item.material")));
        item.setName(items.getString("global-items.back-item.material"));
        return item.build();
    }
    public static ItemStack NEXT_ITEM(){
        PrivateFile items = new PrivateFile(FileType.MENUS);
        ItemStackUtils item = new ItemStackUtils(Material.getMaterial(items.getString("global-items.next-item.material")));
        item.setName(items.getString("global-items.next-item.material"));
        return item.build();
    }


}
