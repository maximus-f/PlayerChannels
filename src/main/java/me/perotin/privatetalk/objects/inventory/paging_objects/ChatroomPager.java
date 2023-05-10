package me.perotin.privatetalk.objects.inventory.paging_objects;

import com.github.stefvanschie.inventoryframework.gui.GuiItem;
import com.github.stefvanschie.inventoryframework.gui.type.util.Gui;
import com.github.stefvanschie.inventoryframework.pane.PaginatedPane;
import com.github.stefvanschie.inventoryframework.pane.Pane;
import com.github.stefvanschie.inventoryframework.pane.StaticPane;
import me.perotin.privatetalk.PrivateTalk;
import me.perotin.privatetalk.objects.ChatRole;
import me.perotin.privatetalk.objects.Chatroom;
import me.perotin.privatetalk.objects.InventoryHelper;
import me.perotin.privatetalk.objects.PrivatePlayer;
import me.perotin.privatetalk.objects.inventory.PagingMenu;
import me.perotin.privatetalk.objects.inventory.static_inventories.ChatroomModeratorMenu;
import me.perotin.privatetalk.storage.Pair;
import me.perotin.privatetalk.storage.files.FileType;
import me.perotin.privatetalk.storage.files.PrivateFile;
import me.perotin.privatetalk.utils.ChatRoleComparator;
import me.perotin.privatetalk.utils.ItemStackUtils;
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
    public ChatroomPager(Chatroom chatroom, Player viewer){
        super(viewer.getName()+"-chatroom", 6, viewer, new MainMenuPaging(viewer, PrivateTalk.getInstance()).getMenu());
        this.chatroom = chatroom;
         this.messages = new PrivateFile(FileType.MESSAGES);
         this.chatroomBar = new StaticPane(2, 1, 5, 1);
         chatroomBar.setPriority(Pane.Priority.HIGH);
        PrivateTalk.getInstance().getHelper().setSideDecorationSlots(getMenu());
        PrivateTalk.getInstance().getHelper().setNavigationBar(getMenu(), getViewer());
        setPaginatedPane();

        getPaginatedPane().populateWithGuiItems(generatePages());
        setChatroomBar();
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


        ItemMeta inChatToggle =  inChat.getFirst().getItemMeta();

        //TODO use messages.yml values
        if (player.getFocusedChatroom() != null && player.getFocusedChatroom().equals(chatroom)) {
            inChatToggle.setDisplayName(inChatToggle.getDisplayName() + " true");
        } else {
            inChatToggle.setDisplayName(inChatToggle.getDisplayName() + " false");

        }
        inChat.getFirst().setItemMeta(inChatToggle);

        GuiItem desc = new GuiItem(description.getFirst(), i -> i.setCancelled(true));
        GuiItem joinItem = new GuiItem(join.getFirst(), joinOrLeaveEvent(true));
        GuiItem leaveItem = new GuiItem(leave.getFirst(), joinOrLeaveEvent(false));
        GuiItem inChatItem = new GuiItem(inChat.getFirst(), toggleFocusedChat());

        chatroomBar.addItem(desc, description.getSecond(), 0);

        PrivateTalk.getInstance().getLogger().info(description.getSecond() + " x --");
        player.getChatrooms().forEach(c ->
                PrivateTalk.getInstance().getLogger().info(c.getName() + " in this"));
        if (player.isMemberOf(chatroom)) {
            ChatRole role = chatroom.getMemberMap().get(player.getUuid());
            chatroomBar.addItem(leaveItem, leave.getSecond(), 0);
            chatroomBar.addItem(inChatItem, inChat.getSecond(), 0);

        } else {
            // Not a member
            chatroomBar.addItem(joinItem, join.getSecond(), 0);


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
     * @return a list of items to be added to the pane
     */
    protected List<GuiItem> generatePages() {
        List<GuiItem> items = new ArrayList<>();
        for(ItemStack i : getHeads().keySet()) {
            items.add(new GuiItem(i, viewModMenuFor(getHeads().get(i))));
        }
        return items;
    }



    /**
     * TODO
     * This needs to be changed to not show their profile but show options for moderator actions if permission suffices
     * i.e. moderator or above
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
                head.setName(messages.getString("chatroom-head-display").replace("$name$", player.getName()));
                head.setLore(messages.getString("chatroom-head-lore1")
                .replace("$connected$", messages.getString("online")).replace("$role$", chatroom.getRole(uuid)));
                heads.put(head.build(), PrivatePlayer.getPlayer(uuid));
            } else {
                OfflinePlayer player = Bukkit.getOfflinePlayer(uuid);
                ItemStackUtils head = new ItemStackUtils(Material.PLAYER_HEAD);
                head.setName(messages.getString("chatroom-head-display").replace("$name$", player.getName()));
                head.setLore(messages.getString("chatroom-head-lore1")
                        .replace("$connected$", messages.getString("offline")).replace("$role$", chatroom.getRole(uuid)));
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
                PrivateTalk.getInstance().getLogger().info("Join!");
                player.addChatroom(chatroom);
                chatroom.addMember(new Pair<>(player.getUuid(), ChatRole.MEMBER));
            } else {
                // Leaving the chatroom
                PrivateTalk.getInstance().getLogger().info("Leave!");

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
}

