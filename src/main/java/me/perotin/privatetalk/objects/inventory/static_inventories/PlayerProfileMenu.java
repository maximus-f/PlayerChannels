package me.perotin.privatetalk.objects.inventory.static_inventories;

import com.github.stefvanschie.inventoryframework.gui.GuiItem;
import com.github.stefvanschie.inventoryframework.gui.type.util.Gui;
import com.github.stefvanschie.inventoryframework.pane.StaticPane;
import me.perotin.privatetalk.PrivateTalk;
import me.perotin.privatetalk.objects.Chatroom;
import me.perotin.privatetalk.objects.InventoryHelper;
import me.perotin.privatetalk.objects.PrivatePlayer;
import me.perotin.privatetalk.objects.inventory.actions.ChatroomItemStackAction;
import me.perotin.privatetalk.objects.inventory.actions.PlayerProfileAction;
import me.perotin.privatetalk.objects.inventory.paging_objects.PagingMenu;
import me.perotin.privatetalk.storage.Pair;
import me.perotin.privatetalk.storage.files.FileType;
import me.perotin.privatetalk.storage.files.PrivateFile;
import me.perotin.privatetalk.utils.PrivateUtils;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.stream.Collectors;

public class PlayerProfileMenu extends PagingMenu {

    private InventoryHelper helper;
    private PrivatePlayer player;
    private Player viewer;
    private StaticPane playerControlPane; // Pane that has controls for players like their status & if they are allowing invites or not
    public PlayerProfileMenu(Player viewer, PrivatePlayer player, Gui backMenu) {
        super(player.getName() + "'s Profile", 6, viewer, backMenu);

        this.viewer = viewer;
        helper = PrivateTalk.getInstance().getHelper();
        helper.setSideDecorationSlots(getMenu());
        helper.setNavigationBar(getMenu(), viewer);
        helper.setPagingNavBar(getMenu());
        playerControlPane = new StaticPane(3, 1, 3, 1);
        pane.setY(2); // Change from 1 to 2 so we can have the row for the control pane
        pane.setHeight(3);
        getPaginatedPane().populateWithGuiItems(generatePages());
        setPlayerControlPane();

    }




    @Override
    protected List<GuiItem> generatePages() {
        List<ItemStack> toDisplay = player.getChatrooms().stream().map(Chatroom::getItem).collect(Collectors.toList());
        toDisplay = toDisplay.stream().sorted(this::compare).collect(Collectors.toList());
        return toDisplay.stream().map(item -> new GuiItem(item, ChatroomItemStackAction.clickOnChatroom())).collect(Collectors.toList());


    }

    private void setPlayerControlPane(){
        PrivateFile messages = new PrivateFile(FileType.MESSAGES);
        Pair<ItemStack, Integer> status = InventoryHelper.getItem("player-profile-menu.player-controls.player-status", null);
        Pair<ItemStack, Integer> toggleInvites = InventoryHelper.getItem("player-profile-menu.player-controls.toggle-invite-status", null);
        Pair<ItemStack, Integer> invites = InventoryHelper.getItem("player-profile-menu.player-controls.invite-player", null);

        GuiItem statusItem = new GuiItem(PrivateUtils.appendToDisplayName(status.getFirst(), player.getStatus()), PlayerProfileAction.clickStatus(player));

        ItemStack togglesInvites = player.isAcceptingInvites() ? PrivateUtils.appendToDisplayName(toggleInvites.getFirst(), messages.getString("true"))
                : PrivateUtils.appendToDisplayName(toggleInvites.getFirst(), messages.getString("false"));
        GuiItem toggleInvitesItem = new GuiItem(togglesInvites, PlayerProfileAction.toggleInviteStatus(viewer, player));
        ItemStack invitePlayer = invites.getFirst();

        GuiItem invPlayerItem = new GuiItem(invitePlayer, PlayerProfileAction.invitePlayerToChatroom(viewer, player));
        playerControlPane.addItem(statusItem, status.getSecond(), 1);

        // Need to check if the person viewing is themselves
        if (getViewer().getName().equals(player.getName())) {
            // viewing themselves so add toggle invites option and set
            // actions to be able to change and toggle
            playerControlPane.addItem(toggleInvitesItem, toggleInvites.getSecond(), 1);

        } else {
            // different person. add invite player option but be sure to check
            // that the viewer is in a chatroom that they are able to invite the player
            // to
            playerControlPane.addItem(invPlayerItem, toggleInvites.getSecond(), 1);

        }
        getMenu().addPane(playerControlPane);



    }

    private int compare(ItemStack o, ItemStack o1){
        Material saved = Material.valueOf(PrivateTalk.getInstance().getConfig().getString("saved-material"));
        if(o.getType() == o1.getType()) return 0;
        else if(o.getType() == saved) return 1;
        else if(o1.getType() == saved) return -1;
        return 0;
    }
}
