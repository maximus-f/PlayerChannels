package me.perotin.privatetalk.objects.inventory.paging_objects;

import com.github.stefvanschie.inventoryframework.gui.GuiItem;
import com.github.stefvanschie.inventoryframework.gui.type.ChestGui;
import com.github.stefvanschie.inventoryframework.gui.type.util.Gui;
import com.github.stefvanschie.inventoryframework.pane.PaginatedPane;
import com.github.stefvanschie.inventoryframework.pane.Pane;
import com.github.stefvanschie.inventoryframework.pane.StaticPane;
import me.perotin.privatetalk.PrivateTalk;
import me.perotin.privatetalk.objects.ChatRole;
import me.perotin.privatetalk.objects.Chatroom;
import me.perotin.privatetalk.objects.InventoryHelper;
import me.perotin.privatetalk.objects.PrivatePlayer;
import me.perotin.privatetalk.objects.inventory.static_inventories.ChatroomModeratorMenu;
import me.perotin.privatetalk.objects.inventory.static_inventories.PlayerProfileMenu;
import me.perotin.privatetalk.storage.Pair;
import me.perotin.privatetalk.storage.files.FileType;
import me.perotin.privatetalk.storage.files.PrivateFile;
import me.perotin.privatetalk.utils.ChatRoleComparator;
import me.perotin.privatetalk.utils.ItemStackUtils;
import me.perotin.privatetalk.utils.PrivateUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/* Created by Perotin on 8/17/19 */

/**
 * Paging object for all chatroom objects
 *
 * Different views of the chatroom that may occur
 *
 * 1: Owner is viewing the chatroom
 * 2: Moderator is viewing the chatroom
 * 3: Regular member is viewing the chatroom
 * 4: Non-member is viewing the chatroom
 *
 * The difference for each of these views is the first row that is shown that is consistent on all pages.
 */
public class ChatroomPager extends PagingMenu {


    private Chatroom chatroom; /** Chatroom to show the paging for **/

    private PrivateFile messages;
    private StaticPane chatroomBar; /** Items that appear directly beneath nav bar**/
    private final StaticPane bottomRow; /** Items that appear in the bottom row, for viewing ban menu etc. **/

    public ChatroomPager(Chatroom chatroom, Player viewer){
        super(viewer.getName()+"-chatroom", 6, viewer, new MainMenuPaging(viewer, PrivateTalk.getInstance()).getMenu());
        this.chatroom = chatroom;
         this.messages = new PrivateFile(FileType.MESSAGES);
         this.chatroomBar = new StaticPane(2, 1, 5, 1);
         chatroomBar.setPriority(Pane.Priority.HIGH);
        this.bottomRow = new StaticPane(3, 5, 3, 1);
        this.bottomRow.setPriority(Pane.Priority.HIGHEST);
        PrivateTalk.getInstance().getHelper().setSideDecorationSlots(getMenu());
        PrivateTalk.getInstance().getHelper().setNavigationBar(getMenu(), getViewer());
        setPaginatedPane();

        getPaginatedPane().populateWithGuiItems(generatePages());
        setChatroomBar();
        setBottomRow();
        addPaneToGui(bottomRow);
      //  getMenu().getPanes().forEach(p -> p.getItems().forEach(i ->
              //  PrivateTalk.getInstance().getLogger().info(i.getItem().toString())));


    }

    /**
     * Sets the chatroom items beneath the navigation bar depending on relationship to chatroom
     * TODO add rest of items (toggling saved, global) Make based on role as well
     */
    private void setChatroomBar() {
        PrivatePlayer player = PrivatePlayer.getPlayer(getViewer().getUniqueId());
        Pair<ItemStack, Integer> description = InventoryHelper.getItem("chatroom-bar.description", null);
        Pair<ItemStack, Integer> join = InventoryHelper.getItem("chatroom-bar.join-chatroom", null);
        Pair<ItemStack, Integer> leave = InventoryHelper.getItem("chatroom-bar.leave-chatroom", null);
        Pair<ItemStack, Integer> inChat = InventoryHelper.getItem("chatroom-bar.in-chat", null);
        Pair<ItemStack, Integer> nicknames = InventoryHelper.getItem("chatroom-bar.nicknames", null);
        Pair<ItemStack, Integer> status = InventoryHelper.getItem("chatroom-bar.status", null);




        ItemMeta inChatToggle =  inChat.getFirst().getItemMeta();

        //TODO use messages.yml values
        if (player.getFocusedChatroom() != null && player.getFocusedChatroom().equals(chatroom)) {
            inChatToggle.setDisplayName(inChatToggle.getDisplayName() + " true");
        } else {
            inChatToggle.setDisplayName(inChatToggle.getDisplayName() + " false");

        }
        inChat.getFirst().setItemMeta(inChatToggle);

        ItemStack descItem = PrivateUtils.appendToDisplayName(description.getFirst(), chatroom.getDescription());
        GuiItem desc = new GuiItem(descItem, i -> i.setCancelled(true));
        GuiItem joinItem = new GuiItem(join.getFirst(), joinOrLeaveEvent(true));
        GuiItem leaveItem = new GuiItem(leave.getFirst(), joinOrLeaveEvent(false));
        GuiItem inChatItem = new GuiItem(inChat.getFirst(), toggleFocusedChat());
        ItemStack nicknamesStack = nicknames.getFirst();
        ItemStack statusStack = status.getFirst();

        if (!chatroom.hasModeratorPermissions(getViewer().getUniqueId())) {
            nicknamesStack = PrivateUtils.stripLore(nicknames.getFirst(), true, -1);
            statusStack = PrivateUtils.stripLore(statusStack, true, -1);

        }
        String nickStatus = chatroom.isNicknamesEnabled() ? messages.getString("true") : messages.getString("false");
        GuiItem nicknameItem = new GuiItem(PrivateUtils.appendToDisplayName(nicknamesStack, nickStatus), toggleNicknameStatus());

        String statusAppend = chatroom.isPublic() ? messages.getString("public") : messages.getString("private");
        GuiItem statusItem = new GuiItem(PrivateUtils.appendToDisplayName(statusStack, statusAppend), togglePublicStatus());

        chatroomBar.addItem(nicknameItem, nicknames.getSecond(), 0);
        chatroomBar.addItem(statusItem, status.getSecond(), 0);

        chatroomBar.addItem(desc, description.getSecond(), 0);

        PrivateTalk.getInstance().getLogger().info(description.getSecond() + " x --");

        if (player.isMemberOf(chatroom)) {
            ChatRole role = chatroom.getMemberMap().get(player.getUuid());
            chatroomBar.addItem(leaveItem, leave.getSecond(), 0);
            chatroomBar.addItem(inChatItem, inChat.getSecond(), 0);

        } else {
            // Not a member
            // Check if chatroom is public or not and if player is not banned
            if (chatroom.isPublic() && !chatroom.isBanned(getViewer().getUniqueId())) {
                chatroomBar.addItem(joinItem, join.getSecond(), 0);
            }


        }
        addPaneToGui(chatroomBar);

    }

    /**
     * @return chatroom for the paging menu
     */
    public Chatroom getChatroom() {
        return chatroom;
    }

    /**
     * List of items that appear in the chatroom with either event for punish or viewing profile based on power of role
     * and person they are trying to punish
     * @return a list of items to be added to the pane
     */
    protected List<GuiItem> generatePages() {
        List<GuiItem> items = new ArrayList<>();
        for(ItemStack i : getHeads().keySet()) {
            if (chatroom.getRole(getViewer().getUniqueId()) == ChatRole.OWNER && ChatRole.getRoleFrom(i) != ChatRole.OWNER) {
                items.add(new GuiItem(i, viewModMenuFor(getHeads().get(i))));
            } else if (chatroom.getRole(getViewer().getUniqueId()) == ChatRole.MODERATOR
            && ChatRole.getRoleFrom(i) == ChatRole.MEMBER) {
                items.add(new GuiItem(i, viewModMenuFor(getHeads().get(i))));
            } else if (chatroom.getRole(getViewer().getUniqueId()) == ChatRole.MODERATOR
                    && ChatRole.getRoleFrom(i) == ChatRole.MODERATOR || ChatRole.getRoleFrom(i) == ChatRole.OWNER) {
                items.add(new GuiItem(i, viewProfile(getHeads().get(i))));
            } else {
                // must be a member
                items.add(new GuiItem(i, viewProfile(getHeads().get(i))));

            }
        }
        return items;
    }



    /**
     *Shows the punish menu for when a moderator or owner tries to punish an eligible player
     * @param player to go to profile
     * @return consumer action to go to param player's profile page
     */
    private Consumer<InventoryClickEvent> viewModMenuFor(PrivatePlayer player){
        return (InventoryClickEvent event) -> {
            event.setCancelled(true);
            new ChatroomModeratorMenu(getViewer(), player, chatroom).getMenu().show(getViewer());
        };
    }

    /**
     * For when a player clicks on another player but they do not have permission to punish so it shows them their profile
     * @param player
     * @return
     */
    private Consumer<InventoryClickEvent> viewProfile(PrivatePlayer player){
        return (InventoryClickEvent event) -> {
            event.setCancelled(true);
            new PlayerProfileMenu(getViewer(), player, getMenu()).getMenu().show(getViewer());
        };
    }

    /**
     * Overrides the parent PagingMenu paginated pane to start at y = 2 instead of y = 1
     * Default PagingMenu begins at (1, 1). This may have to change however in general since the first row is used
     * for other things as well.
     */
    @Override
    protected void setPaginatedPane(){
        pane = new PaginatedPane(1, 2, 7, 3);
        getMenu().addPane(pane);
    }

    /**
     * @return List of heads of every player in the chatroom
     */
    private Map<ItemStack, PrivatePlayer> getHeads(){
        Map<ItemStack, PrivatePlayer> heads = new HashMap<>();
        PrivateTalk plugin = PrivateTalk.getInstance();
        for(UUID uuid: chatroom.getMembers()){
            if(Bukkit.getPlayer(uuid) != null){
                // online
                Player player = Bukkit.getPlayer(uuid);
                ItemStackUtils head = new ItemStackUtils(Material.PLAYER_HEAD);
                head.setName(messages.getString("chatroom-head-display").replace("$player-name$", player.getName()));
                head.setLore(messages.getString("chatroom-head-lore1")
                .replace("$connected$", messages.getString("online")).replace("$role$", chatroom.getStringRole(uuid)));
                head.setOwner(player);
                heads.put(head.build(), PrivatePlayer.getPlayer(uuid));
            } else {
                OfflinePlayer player = Bukkit.getOfflinePlayer(uuid);
                ItemStackUtils head = new ItemStackUtils(Material.PLAYER_HEAD);
                head.setName(messages.getString("chatroom-head-display").replace("$player-name$", player.getName()));
                head.setLore(messages.getString("chatroom-head-lore1")
                        .replace("$connected$", messages.getString("offline")).replace("$role$", chatroom.getStringRole(uuid)));
                head.setOwner(player);
                heads.put(head.build(), PrivatePlayer.getPlayer(uuid));

            }
        }
        return sortListByRank(heads);
    }

    /**
     *
     * @apiNote This function may not work, in theory should work though.
     * @param items to sort
     * @return sorted list of items by chat role
     *
     */
    private Map<ItemStack, PrivatePlayer> sortListByRank(Map<ItemStack, PrivatePlayer> items){
        Map<ItemStack, PrivatePlayer> sorted = new HashMap<>();
        List<ItemStack> sortedList = items.keySet().stream().sorted(new ChatRoleComparator()).collect(Collectors.toList());
        for(ItemStack item : sortedList){
            PrivatePlayer value = items.get(item);
            sorted.put(item, value);
        }
        return sorted;
    }

    /**
     * @param join true if joining, false if leaving
     * @return
     */
    private Consumer<InventoryClickEvent> joinOrLeaveEvent(boolean join){

        return inventoryClickEvent -> {
            inventoryClickEvent.setCancelled(true);
            Player clicker = (Player) inventoryClickEvent.getWhoClicked();
            PrivatePlayer player = PrivatePlayer.getPlayer(clicker.getUniqueId());

            if (join) {
                // Currently not a member, so enter chatroom
                // Need to check if banned
                if (chatroom.isBanned(player.getUuid())){
                    // TODO figure out way to send messages that is smart and sensible. Through changing
                    // menu title again probably but in a smart way
                    //PrivateUtils.sendErrorMessage("You are banned!");
                    return;
                }
                player.addChatroom(chatroom);
                chatroom.addMember(new Pair<>(player.getUuid(), ChatRole.MEMBER));
            } else {
                // Leaving the chatroom

                player.leaveChatroom(chatroom);
                chatroom.removeMember(player.getUuid());

            }
            new ChatroomPager(chatroom, clicker).show();
        };

    }




    /**
     * @return consumer of inventory event of when player toggles focused chat mode
     */
    private Consumer<InventoryClickEvent> toggleFocusedChat() {
        return inventoryClickEvent -> {
          inventoryClickEvent.setCancelled(true);
            Player clicker = (Player) inventoryClickEvent.getWhoClicked();
            PrivatePlayer player = PrivatePlayer.getPlayer(clicker.getUniqueId());

            if (player.getFocusedChatroom() == null ||
            !player.getFocusedChatroom().equals(chatroom)) {
                // Currently not focused on this chatroom, so set them to here
                player.setFocusedChatroom(chatroom);
            } else {
                // Currently focused on this chatroom, so set it to null
                player.setFocusedChatroom(null);
            }
            new ChatroomPager(chatroom, clicker).show();


        };
    }

    /**
     * @return consumer of inventory event of when mod/owner toggles nickname status
     */
    private Consumer<InventoryClickEvent> toggleNicknameStatus() {
        return inventoryClickEvent -> {
            inventoryClickEvent.setCancelled(true);
            Player clicker = (Player) inventoryClickEvent.getWhoClicked();

            if (chatroom.hasModeratorPermissions(getViewer().getUniqueId())){
                chatroom.setNicknamesEnabled(!chatroom.isNicknamesEnabled());
                new ChatroomPager(chatroom, clicker).show();

            }
        };
    }

    private Consumer<InventoryClickEvent> togglePublicStatus() {
        return inventoryClickEvent -> {
            inventoryClickEvent.setCancelled(true);
            Player clicker = (Player) inventoryClickEvent.getWhoClicked();

            if (chatroom.hasModeratorPermissions(getViewer().getUniqueId())){
                chatroom.setPublic(!chatroom.isPublic());
                new ChatroomPager(chatroom, clicker).show();

            }
        };
    }

    /**
     *
     * @return consumer event showing the ban menu
     */
    private Consumer<InventoryClickEvent> viewBanMenu(){
        return event -> {
            event.setCancelled(true);
            new ChatroomBannedMembersListPager(chatroom, getViewer()).show();
        };
    }

    /**
     *
     * @return consumer event showing the nickname menu
     */
    private Consumer<InventoryClickEvent> viewNicknameMenu(){
        return event -> {
            event.setCancelled(true);
            new ChatroomNicknameManagerPager(chatroom, getViewer()).show();
        };
    }

    /**
     * Sets the bottom row for use of mod actions like viewing ban menu and members for setting nicknames
     */
    private void setBottomRow(){
        Pair<ItemStack, Integer> nicknames = InventoryHelper.getItem("chatroom-bottom-bar.nicknames", null);
        Pair<ItemStack, Integer> banMenu = InventoryHelper.getItem("chatroom-bottom-bar.ban-menu", null);

        GuiItem banItem = new GuiItem(banMenu.getFirst(), viewBanMenu());
        GuiItem nickNameItem = new GuiItem(nicknames.getFirst(), viewNicknameMenu());

        if (chatroom.isNicknamesEnabled()) {
            bottomRow.addItem(nickNameItem, nicknames.getSecond(), 0);
        }

        if (chatroom.hasModeratorPermissions(getViewer().getUniqueId())){

            bottomRow.addItem(banItem, banMenu.getSecond(), 0);
        }

    }

}

