package me.perotin.playerchannels.objects.inventory.paging_objects;

import com.github.stefvanschie.inventoryframework.gui.GuiItem;
import com.github.stefvanschie.inventoryframework.pane.PaginatedPane;
import com.github.stefvanschie.inventoryframework.pane.Pane;
import com.github.stefvanschie.inventoryframework.pane.StaticPane;
import me.perotin.playerchannels.PlayerChannels;
import me.perotin.playerchannels.events.chat_events.ChatroomConfirmDeletionEvent;
import me.perotin.playerchannels.events.chat_events.ChatroomSetDescriptionEvent;
import me.perotin.playerchannels.objects.ChatRole;
import me.perotin.playerchannels.objects.Chatroom;
import me.perotin.playerchannels.objects.InventoryHelper;
import me.perotin.playerchannels.objects.PlayerChannelUser;
import me.perotin.playerchannels.objects.inventory.static_inventories.ChatroomModeratorMenu;
import me.perotin.playerchannels.objects.inventory.static_inventories.PlayerProfileMenu;
import me.perotin.playerchannels.storage.Pair;
import me.perotin.playerchannels.storage.files.FileType;
import me.perotin.playerchannels.storage.files.ChannelFile;
import me.perotin.playerchannels.utils.ChatRoleComparator;
import me.perotin.playerchannels.utils.ItemStackUtils;
import me.perotin.playerchannels.utils.ChannelUtils;
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

    private ChannelFile messages;
    private StaticPane chatroomBar; /** Items that appear directly beneath nav bar**/
    private final StaticPane bottomRow; /** Items that appear in the bottom row, for viewing ban menu etc. **/

    public ChatroomPager(Chatroom chatroom, Player viewer){
        super(ChannelUtils.getMessageString(("chatroom-menu-title")).replace("$chatroom$", chatroom.getName()), 6, viewer, new MainMenuPaging(viewer, PlayerChannels.getInstance()).getMenu());
        this.chatroom = chatroom;
         this.messages = new ChannelFile(FileType.MESSAGES);
         this.chatroomBar = new StaticPane(2, 1, 5, 1);
         chatroomBar.setPriority(Pane.Priority.HIGH);
        this.bottomRow = new StaticPane(3, 5, 3, 1);
        this.bottomRow.setPriority(Pane.Priority.HIGHEST);
        PlayerChannels.getInstance().getHelper().setSideDecorationSlots(getMenu());
        PlayerChannels.getInstance().getHelper().setNavigationBar(getMenu(), getViewer());
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
        PlayerChannelUser player = PlayerChannelUser.getPlayer(getViewer().getUniqueId());
        Pair<ItemStack, Integer> description = InventoryHelper.getItem("chatroom-bar.description", null);
        Pair<ItemStack, Integer> join = InventoryHelper.getItem("chatroom-bar.join-chatroom", null);
        Pair<ItemStack, Integer> leave = InventoryHelper.getItem("chatroom-bar.leave-chatroom", null);
        Pair<ItemStack, Integer> inChat = InventoryHelper.getItem("chatroom-bar.in-chat", null);
        Pair<ItemStack, Integer> nicknames = InventoryHelper.getItem("chatroom-bar.nicknames", null);
        Pair<ItemStack, Integer> status = InventoryHelper.getItem("chatroom-bar.status", null);




        ItemMeta inChatToggle =  inChat.getFirst().getItemMeta();

        //TODO use messages.yml values
        if (player.getFocusedChatroom() != null && player.getFocusedChatroom().equals(chatroom)) {
            inChatToggle.setDisplayName(inChatToggle.getDisplayName() + " " + messages.getString("true"));
        } else {
            inChatToggle.setDisplayName(inChatToggle.getDisplayName() + " " + messages.getString("false"));

        }
        inChat.getFirst().setItemMeta(inChatToggle);

        ItemStack descItem = ChannelUtils.appendToDisplayName(description.getFirst(), chatroom.getDescription());
        GuiItem joinItem = new GuiItem(join.getFirst(), joinOrLeaveEvent(true));
        GuiItem leaveItem = new GuiItem(leave.getFirst(), joinOrLeaveEvent(false));
        GuiItem inChatItem = new GuiItem(inChat.getFirst(), toggleFocusedChat());
        ItemStack nicknamesStack = nicknames.getFirst();
        ItemStack statusStack = status.getFirst();

        if (!chatroom.hasModeratorPermissions(getViewer().getUniqueId())) {
            nicknamesStack = ChannelUtils.stripLore(nicknames.getFirst(), true, -1);
            statusStack = ChannelUtils.stripLore(statusStack, true, -1);
            descItem = ChannelUtils.stripLore(descItem, true, -1);

        }
        GuiItem desc = new GuiItem(descItem, changeChatroomDescription());

        String nickStatus = chatroom.isNicknamesEnabled() ? messages.getString("true") : messages.getString("false");
        GuiItem nicknameItem = new GuiItem(ChannelUtils.appendToDisplayName(nicknamesStack, nickStatus), toggleNicknameStatus());

        String statusAppend = chatroom.isPublic() ? messages.getString("public") : messages.getString("private");
        GuiItem statusItem = new GuiItem(ChannelUtils.appendToDisplayName(statusStack, statusAppend), togglePublicStatus());

        chatroomBar.addItem(nicknameItem, nicknames.getSecond(), 0);
        chatroomBar.addItem(statusItem, status.getSecond(), 0);

        chatroomBar.addItem(desc, description.getSecond(), 0);


        if (player.isMemberOf(chatroom)) {
            ChatRole role = chatroom.getMemberMap().get(player.getUuid());
            chatroomBar.addItem(leaveItem, leave.getSecond(), 0);
            chatroomBar.addItem(inChatItem, inChat.getSecond(), 0);

        } else {
            // Not a member
            // Check if chatroom is public or not and if player is not banned
            // Also check if the player is a mod or admin and allow them to join regardless of status
            Player staff = Bukkit.getPlayer(player.getUuid());
            if (chatroom.isPublic() && !chatroom.isBanned(getViewer().getUniqueId()) ||
                    (staff.hasPermission("playerchannels.moderator") || staff.hasPermission("playerchannels.admin"))) {
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
        Map<ItemStack, PlayerChannelUser> heads = getHeads();
        for(ItemStack i : heads.keySet()) {
            if (chatroom.getRole(getViewer().getUniqueId()) == ChatRole.OWNER && ChatRole.getRoleFrom(i) != ChatRole.OWNER) {
                items.add(new GuiItem(i, viewModMenuFor(heads.get(i))));
            } else if (chatroom.getRole(getViewer().getUniqueId()) == ChatRole.MODERATOR
            && ChatRole.getRoleFrom(i) == ChatRole.MEMBER) {
                items.add(new GuiItem(i, viewModMenuFor(heads.get(i))));
            } else if (chatroom.getRole(getViewer().getUniqueId()) == ChatRole.MODERATOR
                    && ChatRole.getRoleFrom(i) == ChatRole.MODERATOR || ChatRole.getRoleFrom(i) == ChatRole.OWNER) {
                items.add(new GuiItem(i, viewProfile(heads.get(i))));
            } else {
                // must be a member
                items.add(new GuiItem(i, viewProfile(heads.get(i))));

            }
        }
        return items;
    }



    /**
     *Shows the punish menu for when a moderator or owner tries to punish an eligible player
     * @param player to go to profile
     * @return consumer action to go to param player's profile page
     */
    private Consumer<InventoryClickEvent> viewModMenuFor(PlayerChannelUser player){
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
    private Consumer<InventoryClickEvent> viewProfile(PlayerChannelUser player){
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
    private Map<ItemStack, PlayerChannelUser> getHeads(){
        Map<ItemStack, PlayerChannelUser> heads = new HashMap<>();
        PlayerChannels plugin = PlayerChannels.getInstance();
        for(UUID uuid: chatroom.getMembers()){
            if(Bukkit.getPlayer(uuid) != null){
                // online
                Player player = Bukkit.getPlayer(uuid);
                ItemStackUtils head = new ItemStackUtils(Material.PLAYER_HEAD);
                head.setName(messages.getString("chatroom-head-display").replace("$player-name$", player.getName()));
                head.setLore(messages.getString("chatroom-head-lore1")
                .replace("$connected$", messages.getString("online")).replace("$role$", chatroom.getStringRole(uuid)));
                head.setOwner(player);
                heads.put(head.build(), PlayerChannelUser.getPlayer(uuid));
            } else {
                OfflinePlayer player = Bukkit.getOfflinePlayer(uuid);
                ItemStackUtils head = new ItemStackUtils(Material.PLAYER_HEAD);
                head.setName(messages.getString("chatroom-head-display").replace("$player-name$", player.getName()));
                head.setLore(messages.getString("chatroom-head-lore1")
                        .replace("$connected$", messages.getString("offline")).replace("$role$", chatroom.getStringRole(uuid)));
                head.setOwner(player);
                heads.put(head.build(), PlayerChannelUser.getPlayer(uuid));

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
    private Map<ItemStack, PlayerChannelUser> sortListByRank(Map<ItemStack, PlayerChannelUser> items){
        Map<ItemStack, PlayerChannelUser> sorted = new LinkedHashMap<>();
        List<ItemStack> sortedList = items.keySet().stream().sorted(new ChatRoleComparator()).collect(Collectors.toList());
        for(ItemStack item : sortedList){
            PlayerChannelUser value = items.get(item);
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
            PlayerChannelUser player = PlayerChannelUser.getPlayer(clicker.getUniqueId());

            if (join) {
                // Currently not a member, so enter chatroom
                // Need to check if banned
                if (chatroom.isBanned(player.getUuid())){
                    ChannelUtils.sendMenuMessage("You are banned!", clicker, null);
                    return;
                }
                player.addChatroom(chatroom);
                if (!chatroom.isServerOwned() || (chatroom.isServerOwned()) && (!(clicker.hasPermission("playerchannels.admin") || clicker.hasPermission("playerchannels.moderator")))) {
                    chatroom.addMember(new Pair<>(player.getUuid(), ChatRole.MEMBER), "");
                } else {
                    // Idea here is that we want to automatically promote those with OP/playerchannels.admin or playerchannels.moderator
                    // to be automatic staff in this channel

                    // Two options: is server owned and either a moderator or admin

                    if (clicker.hasPermission("playerchannels.admin")) {
                        chatroom.addMember(new Pair<>(player.getUuid(), ChatRole.OWNER), "");

                    } else if (clicker.hasPermission("playerchannels.moderator")) {
                        chatroom.addMember(new Pair<>(player.getUuid(), ChatRole.MODERATOR), "");
                    }

                }
            } else {
                // Leaving the chatroom
                // If owner is leaving and it is not a server channel then tell them that this will delete their chatroom and have them confirm it
                if (!getChatroom().isServerOwned() && getChatroom().getRole(player.getUuid()) == ChatRole.OWNER && (getChatroom().getMembers().size() > 1 || chatroom.isSaved())) {
                    clicker.closeInventory();
                    clicker.updateInventory();
                    ChatroomConfirmDeletionEvent.confirmDeletion.put(clicker, chatroom);

                    String warningDelete = messages.getString("owner-leave-chatroom")
                            .replace("$chatroom$", chatroom.getName());
                    clicker.sendMessage(warningDelete);
                    clicker.sendMessage(messages.getString("owner-leave-chatroom2"));
                    return;

                } else {
                    // let them leave simply
                    player.leaveChatroom(chatroom);
                    chatroom.removeMember(player.getUuid());
                    // so it doesn't show them the empty chatroom
                    if (getChatroom().getMembers().size() == 0){
                        new MainMenuPaging(clicker, PlayerChannels.getInstance()).show();
                        return;
                    }
                }

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
            PlayerChannelUser player = PlayerChannelUser.getPlayer(clicker.getUniqueId());

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

    private Consumer<InventoryClickEvent> changeChatroomDescription() {
        return inventoryClickEvent -> {
            inventoryClickEvent.setCancelled(true);
            Player clicker = (Player) inventoryClickEvent.getWhoClicked();

            if (chatroom.hasModeratorPermissions(getViewer().getUniqueId())){
                ChatroomSetDescriptionEvent.setDescription.put(clicker, chatroom);
                clicker.closeInventory();
                clicker.updateInventory();
                String setDesc = messages.getString("set-description").replace("$chatroom$", chatroom.getName())
                        .replace("$cancel$", messages.getString("cancel"));
                clicker.sendMessage(setDesc);


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
     * Toggles listening of this chatroom. Listening to a chatroom means it will block other channels besides for this one.
     * @return click event where player toggles whether they are listening to this chatroom
     */
    private Consumer<InventoryClickEvent> toggleListenItem(){
        return inventoryClickEvent -> {
            inventoryClickEvent.setCancelled(true);
            Player clicker = (Player) inventoryClickEvent.getWhoClicked();
            PlayerChannelUser player = PlayerChannelUser.getPlayer(clicker.getUniqueId());

            if (player.isListeningTo(getChatroom())) {
                player.removeChannelToListen(getChatroom());
            } else {
                player.addChannelToListen(getChatroom());
            }
            new ChatroomPager(chatroom, clicker).show();

        };
    }

    /**
     * Sets the bottom row for use of mod actions like viewing ban menu and members for setting nicknames
     */
    private void setBottomRow(){
        Pair<ItemStack, Integer> nicknames = InventoryHelper.getItem("chatroom-bottom-bar.nicknames", null);
        Pair<ItemStack, Integer> listen = InventoryHelper.getItem("chatroom-bottom-bar.listen", null);

        Pair<ItemStack, Integer> banMenu = InventoryHelper.getItem("chatroom-bottom-bar.ban-menu", null);


        String status = PlayerChannelUser.getPlayer(getViewer().getUniqueId()).isListeningTo(getChatroom()) ? messages.getString("on-status") : messages.getString("off-status");
        ItemStack listenIn = ChannelUtils.replacePlaceHolderInDisplayName(ChannelUtils.replacePlaceHolderInDisplayName(listen.getFirst(), "$channel$", getChatroom().getName()), "$status$", status);
        GuiItem banItem = new GuiItem(banMenu.getFirst(), viewBanMenu());
        GuiItem nickNameItem = new GuiItem(nicknames.getFirst(), viewNicknameMenu());
        GuiItem listenItem = new GuiItem(listenIn, toggleListenItem());

        if (chatroom.isNicknamesEnabled()) {
            bottomRow.addItem(nickNameItem, nicknames.getSecond(), 0);
        }


        if (chatroom.hasModeratorPermissions(getViewer().getUniqueId())){

            bottomRow.addItem(banItem, banMenu.getSecond(), 0);
        }
        if (chatroom.isInChatroom(getViewer().getUniqueId())) {
            bottomRow.addItem(listenItem, listen.getSecond(), 0);
        }

    }

}

