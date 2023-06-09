package me.perotin.playerchannels.objects.inventory.paging_objects;

import com.github.stefvanschie.inventoryframework.gui.GuiItem;
import me.perotin.playerchannels.PlayerChannels;
import me.perotin.playerchannels.objects.InventoryHelper;
import me.perotin.playerchannels.objects.PlayerChannelUser;
import me.perotin.playerchannels.objects.inventory.static_inventories.PlayerProfileMenu;
import me.perotin.playerchannels.storage.files.FileType;
import me.perotin.playerchannels.storage.files.ChannelFile;
import me.perotin.playerchannels.utils.ItemStackUtils;
import me.perotin.playerchannels.utils.ChannelUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.*;

public class PlayerListPager extends PagingMenu {


    public PlayerListPager(Player viewer) {
        super(ChannelUtils.getMessageString("player-list-menu-title"), 6, viewer, new MainMenuPaging(viewer, PlayerChannels.getInstance()).getMenu());
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
        Map<ItemStack, PlayerChannelUser> headsMap = getHeads();
        for (ItemStack i : headsMap.keySet()){
            // Associate consumer action with each privateplayer
            PlayerChannelUser correspondingPlayer = headsMap.get(i);
            heads.add(new GuiItem(i, inventoryClickEvent -> {
                inventoryClickEvent.setCancelled(true);
                Player clicker = (Player) inventoryClickEvent.getWhoClicked();
                PlayerChannelUser playerChannelUser = PlayerChannelUser.getPlayer(clicker.getUniqueId());
                Bukkit.getLogger().info("Clicker " + clicker.getName() + " Clicked;" + correspondingPlayer.getName());
                new PlayerProfileMenu(clicker, correspondingPlayer, ChannelUtils.getMainMenu(clicker)).showProfile(clicker);

            }));
        }
        return heads;
    }

    private void setPlayerListPage() {
        InventoryHelper helper = PlayerChannels.getInstance().getHelper();
        helper.setNavigationBar(getMenu(), getViewer());
        helper.setSideDecorationSlots(getMenu());

    }
    /**
     * @return List of heads of every player
     */
    private Map<ItemStack, PlayerChannelUser> getHeads(){
        Map<ItemStack, PlayerChannelUser> heads = new HashMap<>();
        PlayerChannels plugin = PlayerChannels.getInstance();
        ChannelFile messages = new ChannelFile(FileType.MESSAGES);
        for(PlayerChannelUser playerChannelUser : plugin.getPlayers()){
           OfflinePlayer owner = Bukkit.getOfflinePlayer(playerChannelUser.getUuid());
            if(Bukkit.getPlayer(playerChannelUser.getUuid()) != null){
                // online
                Player player = Bukkit.getPlayer(playerChannelUser.getUuid());
                ItemStackUtils head = new ItemStackUtils(Material.PLAYER_HEAD);
                head.setName(messages.getString("chatroom-head-display").replace("$player-name$", player.getName()));
                head.setOwner(owner);
                heads.put(head.build(), playerChannelUser);
            } else {
                OfflinePlayer player = Bukkit.getOfflinePlayer(playerChannelUser.getUuid());
                ItemStackUtils head = new ItemStackUtils(Material.PLAYER_HEAD);
                head.setName(messages.getString("chatroom-head-display").replace("$player-name$", player.getName()));
                head.setOwner(owner);
                heads.put(head.build(), playerChannelUser);

            }
        }
        return heads;
    }
}
