package me.perotin.privatetalk.objects.inventory.paging_objects;

import com.github.stefvanschie.inventoryframework.GuiItem;
import com.github.stefvanschie.inventoryframework.pane.PaginatedPane;
import com.github.stefvanschie.inventoryframework.pane.StaticPane;
import me.perotin.privatetalk.PrivateTalk;
import me.perotin.privatetalk.objects.ChatRole;
import me.perotin.privatetalk.objects.Chatroom;
import me.perotin.privatetalk.objects.PrivatePlayer;
import me.perotin.privatetalk.objects.inventory.PagingMenu;
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

import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/* Created by Perotin on 8/17/19 */

/**
 * Paging object for all chatroom objects
 */
public class ChatroomPager extends PagingMenu {


    private Chatroom chatroom;
    private PrivateFile messages;
    public ChatroomPager(String identifier, Chatroom chatroom, Player viewer){
        super(identifier, 6, viewer);
        this.chatroom = chatroom;
         this.messages = new PrivateFile(FileType.MESSAGES);
         this.pane = new PaginatedPane(2, 1, 7, 3);
         getPaginatedPane().populateWithGuiItems(generatePages());

    }

    public Chatroom getChatroom() {
        return chatroom;
    }

    /**
     * @return a list of items to be added to the pane
     */
    protected List<GuiItem> generatePages() {
        List<GuiItem> items = new ArrayList<>();
        for(ItemStack i : getHeads().keySet()) {
            items.add(new GuiItem(i, goToProfile(getHeads().get(i))));
        }
        return items;
    }

    /**
     *
     * @param player to go to profile
     * @return consumer action to go to param player's profile page
     */
    private Consumer<InventoryClickEvent> goToProfile(PrivatePlayer player){
        return (InventoryClickEvent event) -> player.showProfileTo(getViewer());
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
                heads.put(head.build(), plugin.getPrivatePlayer(uuid));
            } else {
                OfflinePlayer player = Bukkit.getOfflinePlayer(uuid);
                ItemStackUtils head = new ItemStackUtils(Material.PLAYER_HEAD);
                head.setName(messages.getString("chatroom-head-display").replace("$name$", player.getName()));
                head.setLore(messages.getString("chatroom-head-lore1")
                        .replace("$connected$", messages.getString("offline")).replace("$role$", chatroom.getRole(uuid)));
                heads.put(head.build(), plugin.getPrivatePlayer(uuid));

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
}
