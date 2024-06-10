package me.perotin.playerchannels.objects.inventory.static_inventories;

import com.github.stefvanschie.inventoryframework.gui.GuiItem;
import com.github.stefvanschie.inventoryframework.gui.type.util.Gui;
import com.github.stefvanschie.inventoryframework.pane.PaginatedPane;
import com.github.stefvanschie.inventoryframework.pane.Pane;
import com.github.stefvanschie.inventoryframework.pane.StaticPane;
import me.perotin.playerchannels.PlayerChannels;
import me.perotin.playerchannels.objects.Chatroom;
import me.perotin.playerchannels.objects.InventoryHelper;
import me.perotin.playerchannels.objects.PlayerChannelUser;
import me.perotin.playerchannels.objects.inventory.actions.ChatroomItemStackAction;
import me.perotin.playerchannels.objects.inventory.actions.PlayerProfileAction;
import me.perotin.playerchannels.objects.inventory.paging_objects.PagingMenu;
import me.perotin.playerchannels.storage.Pair;
import me.perotin.playerchannels.storage.files.FileType;
import me.perotin.playerchannels.storage.files.ChannelFile;
import me.perotin.playerchannels.utils.ItemStackUtils;
import me.perotin.playerchannels.utils.ChannelUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Class to represent when a player is viewing their own player profile or another users player profile
 */
public class PlayerProfileMenu extends PagingMenu {

    private InventoryHelper helper;
    private PlayerChannelUser player;
    private Player viewer;

    private final StaticPane playerControlPane; // Pane that has controls for players like their status & if they are allowing invites or not
    public PlayerProfileMenu(Player viewer, PlayerChannelUser player, Gui backMenu) {
        super(ChannelUtils.getMessageString("player-profile-menu-title").replace("$name$", player.getName()), 6, viewer, backMenu);

        this.viewer = viewer;
        helper = PlayerChannels.getInstance().getHelper();
        helper.setSideDecorationSlots(getMenu());
        setNavBar(helper.setNavigationBar(getMenu(), viewer).getSecond());
        ItemStack playerHead = new ItemStackUtils(Material.PLAYER_HEAD)
                .setOwner(Bukkit.getOfflinePlayer(player.getUuid()))
                .setName(ChatColor.YELLOW + player.getName())
                .build();
        getNavBar().addItem(new GuiItem(playerHead, event -> event.setCancelled(true)), 4, 0);
        this.player = player;
        helper.setPagingNavBar(getMenu());
        playerControlPane = new StaticPane(3, 1, 3, 1);
        playerControlPane.setPriority(Pane.Priority.HIGHEST);
        pane.setY(2); // Change from 1 to 2 so we can have the row for the control pane
        pane.setHeight(3);
        getMenu().getPanes().removeIf(pane -> pane instanceof PaginatedPane);
        getMenu().addPane(pane);
        getPaginatedPane().populateWithGuiItems(generatePages());
        setPlayerControlPane();
        addPane(playerControlPane);



    }




    @Override
    protected List<GuiItem> generatePages() {

        if (!player.getChatrooms().isEmpty()) {

            // Allow only themselves or admin to view all channels including hidden
            if (getViewer().getUniqueId().equals(player.getUuid()) || getViewer().hasPermission("playerchannels.admin")) {

                List<ItemStack> toDisplay = player.getChatrooms().stream().map(Chatroom::getItem).collect(Collectors.toList());
                toDisplay = toDisplay.stream().sorted(this::compare).collect(Collectors.toList());
                return toDisplay.stream().map(item -> new GuiItem(item, ChatroomItemStackAction.clickOnChatroom())).collect(Collectors.toList());

            } else {
                // Only show non-hidden
                List<ItemStack> toDisplay = player.getChatrooms().stream().filter(c -> !c.isHidden()).map(Chatroom::getItem).collect(Collectors.toList());
                toDisplay = toDisplay.stream().sorted(this::compare).collect(Collectors.toList());
                return toDisplay.stream().map(item -> new GuiItem(item, ChatroomItemStackAction.clickOnChatroom())).collect(Collectors.toList());

            }
        }
        return new ArrayList<>();

    }

    private void setPlayerControlPane(){
        ChannelFile messages = new ChannelFile(FileType.MESSAGES);
        Pair<ItemStack, Integer> status = InventoryHelper.getItem("player-profile-menu.player-controls.player-status", null);
        Pair<ItemStack, Integer> toggleInvites = InventoryHelper.getItem("player-profile-menu.player-controls.toggle-invite-status", null);
        Pair<ItemStack, Integer> invites = InventoryHelper.getItem("player-profile-menu.player-controls.invite-player", null);

        GuiItem statusItem = new GuiItem(ChannelUtils.appendToDisplayName(status.getFirst(), player.getStatus()), PlayerProfileAction.clickStatus(player));

        ItemStack togglesInvites = player.isAcceptingInvites() ? ChannelUtils.appendToDisplayName(toggleInvites.getFirst(), messages.getString("true"))
                : ChannelUtils.appendToDisplayName(toggleInvites.getFirst(), messages.getString("false"));
        GuiItem toggleInvitesItem = new GuiItem(togglesInvites, PlayerProfileAction.toggleInviteStatus(viewer, player));
        ItemStack invitePlayer = invites.getFirst();

        GuiItem invPlayerItem = new GuiItem(invitePlayer, PlayerProfileAction.invitePlayerToChatroom(viewer, player));
        playerControlPane.addItem(statusItem, status.getSecond(), 0);

        // Need to check if the person viewing is themselves

        if (getViewer().getName().equals(player.getName())) {
            // viewing themselves so add toggle invites option and set
            // actions to be able to change and toggle
            playerControlPane.addItem(toggleInvitesItem, toggleInvites.getSecond(), 0);

        } else {
            // different person. don't show if receipient has invites turned off
            if (player.isAcceptingInvites()) {
                playerControlPane.addItem(invPlayerItem, toggleInvites.getSecond(), 0);
            }

        }



    }

    private int compare(ItemStack o, ItemStack o1){
        Material saved = Material.valueOf(PlayerChannels.getInstance().getConfig().getString("saved-material"));
        if(o.getType() == o1.getType()) return 0;
        else if(o.getType() == saved) return 1;
        else if(o1.getType() == saved) return -1;
        return 0;
    }

    public void showProfile(HumanEntity entity){

        getMenu().show(entity);

    }
}
