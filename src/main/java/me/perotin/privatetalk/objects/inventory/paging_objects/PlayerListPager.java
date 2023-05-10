package me.perotin.privatetalk.objects.inventory.paging_objects;

import com.github.stefvanschie.inventoryframework.gui.GuiItem;
import com.github.stefvanschie.inventoryframework.gui.type.util.Gui;
import me.perotin.privatetalk.PrivateTalk;
import me.perotin.privatetalk.objects.InventoryHelper;
import me.perotin.privatetalk.objects.PrivatePlayer;
import me.perotin.privatetalk.objects.inventory.PagingMenu;
import me.perotin.privatetalk.storage.files.FileType;
import me.perotin.privatetalk.storage.files.PrivateFile;
import me.perotin.privatetalk.utils.ItemStackUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.*;

public class PlayerListPager extends PagingMenu {


    public PlayerListPager(Player viewer) {
        super("PlayerListPager", 6, viewer, new MainMenuPaging(viewer, PrivateTalk.getInstance()).getMenu());
        setPaginatedPane();
        getPaginatedPane().populateWithGuiItems(generatePages());
        setPlayerListPage();


    }

    /**
     * @return List of player heads of all players
     */
    @Override
    protected List<GuiItem> generatePages() {

        List<GuiItem> heads = new ArrayList<>();
        Map<ItemStack, PrivatePlayer> headsMap = getHeads();
        for (ItemStack i : headsMap.keySet()){
            // Associate consumer action with each privateplayer
            PrivatePlayer correspondingPlayer = headsMap.get(i);
            heads.add(new GuiItem(i, inventoryClickEvent -> {
                inventoryClickEvent.setCancelled(true);
                correspondingPlayer.showProfileTo((Player) inventoryClickEvent.getWhoClicked());

            }));
        }
        return heads;
    }

    private void setPlayerListPage() {
        InventoryHelper helper = PrivateTalk.getInstance().getHelper();
        helper.setNavigationBar(getMenu(), getViewer());
        helper.setSideDecorationSlots(getMenu());

    }
    /**
     * @return List of heads of every player
     */
    private Map<ItemStack, PrivatePlayer> getHeads(){
        Map<ItemStack, PrivatePlayer> heads = new HashMap<>();
        PrivateTalk plugin = PrivateTalk.getInstance();
        PrivateFile messages = new PrivateFile(FileType.MESSAGES);
        for(PrivatePlayer privatePlayer: plugin.getPlayers()){
           OfflinePlayer owner = Bukkit.getOfflinePlayer(privatePlayer.getUuid());
            if(Bukkit.getPlayer(privatePlayer.getUuid()) != null){
                // online
                Player player = Bukkit.getPlayer(privatePlayer.getUuid());
                ItemStackUtils head = new ItemStackUtils(Material.PLAYER_HEAD);
                head.setName(messages.getString("chatroom-head-display").replace("$player-name$", player.getName()));
                head.setOwner(owner);
                heads.put(head.build(), privatePlayer);
            } else {
                OfflinePlayer player = Bukkit.getOfflinePlayer(privatePlayer.getUuid());
                ItemStackUtils head = new ItemStackUtils(Material.PLAYER_HEAD);
                head.setName(messages.getString("chatroom-head-display").replace("$player-name$", player.getName()));
                head.setOwner(owner);
                heads.put(head.build(), privatePlayer);

            }
        }
        return heads;
    }
}
