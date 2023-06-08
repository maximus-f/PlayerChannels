package me.perotin.privatetalk.objects.inventory.paging_objects;

import com.github.stefvanschie.inventoryframework.gui.GuiItem;
import com.github.stefvanschie.inventoryframework.pane.Pane;
import com.github.stefvanschie.inventoryframework.pane.StaticPane;
import me.perotin.privatetalk.PrivateTalk;
import me.perotin.privatetalk.objects.Chatroom;
import me.perotin.privatetalk.objects.InventoryHelper;
import me.perotin.privatetalk.objects.inventory.actions.ChatroomItemStackAction;
import me.perotin.privatetalk.storage.Pair;
import me.perotin.privatetalk.storage.files.FileType;
import me.perotin.privatetalk.storage.files.PrivateFile;
import me.perotin.privatetalk.utils.PrivateUtils;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.stream.Collectors;

/* Created by Perotin on 11/29/19 */

/**
 * Class for all Main Menu instances that are shown to players
 */
public class MainMenuPaging extends PagingMenu {


    private PrivateFile messages;
    private PrivateTalk plugin;

    private final StaticPane bottomRow; // Bottom row to contain button to view all players, join a random chatroom, leave all current chatrooms etc.
    public MainMenuPaging(Player viewer, PrivateTalk plugin){
        super(PrivateUtils.getMessageString("main-menu-title"), 6, viewer, null);
        this.messages = new PrivateFile(FileType.MESSAGES);
        this.plugin = plugin;
        this.bottomRow = new StaticPane(3, 5, 3, 1);
        this.bottomRow.setPriority(Pane.Priority.HIGHEST);
        setMainPage();
        getPaginatedPane().populateWithGuiItems(generatePages());
        setBottomRow();
        getMenu().addPane(bottomRow);



    }


    /**
     * Generates list of all chatroom in sorted order
     * @return list of chatroom items
     */

    @Override
    protected List<GuiItem> generatePages() {
        List<ItemStack> toDisplay = plugin.getChatrooms().stream().map(Chatroom::getItem).collect(Collectors.toList());
        toDisplay = toDisplay.stream().sorted(this::compare).collect(Collectors.toList());
        List<GuiItem> guiItems = toDisplay.stream().map(item -> new GuiItem(item, ChatroomItemStackAction.clickOnChatroom())).collect(Collectors.toList());

//        List<GuiItem> test = new ArrayList<>();
//        for (int i = 0; i < 150; i++) {
//            test.add(new GuiItem(new ItemStack(Material.APPLE)));
//        }
        return guiItems;
    }


    private void setMainPage() {
        InventoryHelper helper = plugin.getHelper();
        helper.setNavigationBar(getMenu(), getViewer());
        helper.setSideDecorationSlots(getMenu());

    }

    private void setBottomRow(){
        Pair<ItemStack, Integer> viewAllPlayer = InventoryHelper.getItem("main-menu.bottom-row.view-players", null);
        GuiItem viewAllPlayersItem = new GuiItem(viewAllPlayer.getFirst(), i -> {
            i.setCancelled(true);
            new PlayerListPager((Player) i.getWhoClicked()).show();
        });

        bottomRow.addItem(viewAllPlayersItem, viewAllPlayer.getSecond(), 0);
    }






    // TODO: Look into using PersistentDataContainers instead of Lore.
    /**
     * Sorts the itemstacks by whether or not they have the saved material type, sorting them from saved -> everything else
     * @param o
     * @param o1
     * @return
     */
    private int compare(ItemStack o, ItemStack o1){
        Material saved = Material.valueOf(plugin.getConfig().getString("saved-material"));
        if(o.getType() == o1.getType()) return 0;
        else if(o.getType() == saved) return 1;
        else if(o1.getType() == saved) return -1;
        return 0;
    }
}
