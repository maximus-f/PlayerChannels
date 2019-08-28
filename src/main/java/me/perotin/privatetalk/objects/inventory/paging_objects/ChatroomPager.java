package me.perotin.privatetalk.objects.inventory.paging_objects;

import com.github.stefvanschie.inventoryframework.GuiItem;
import com.github.stefvanschie.inventoryframework.pane.PaginatedPane;
import com.github.stefvanschie.inventoryframework.pane.StaticPane;
import me.perotin.privatetalk.objects.ChatRole;
import me.perotin.privatetalk.objects.Chatroom;
import me.perotin.privatetalk.objects.inventory.PagingMenu;
import me.perotin.privatetalk.storage.files.FileType;
import me.perotin.privatetalk.storage.files.PrivateFile;
import me.perotin.privatetalk.utils.ChatRoleComparator;
import me.perotin.privatetalk.utils.ItemStackUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
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
     * TODO Write the function that attaches the heads to the player profiles
     */
    protected List<GuiItem> generatePages() {
        List<GuiItem> items = new ArrayList<>();
        for(ItemStack i : getHeads()) {
            items.add(new GuiItem(i));
        }
        return items;
    }


    /**
     * @return List of heads of every player in the chatroom
     */
    private List<ItemStack> getHeads(){
        List<ItemStack> heads = new ArrayList<>();
        for(UUID uuid: chatroom.getMembers()){
            if(Bukkit.getPlayer(uuid) != null){
                // online
                Player player = Bukkit.getPlayer(uuid);
                ItemStackUtils head = new ItemStackUtils(Material.PLAYER_HEAD);
                head.setName(messages.getString("chatroom-head-display").replace("$name$", player.getName()));
                head.setLore(messages.getString("chatroom-head-lore1")
                .replace("$connected$", messages.getString("online")).replace("$role$", chatroom.getRole(uuid)));
                heads.add(head.build());
            } else {
                OfflinePlayer player = Bukkit.getOfflinePlayer(uuid);
                ItemStackUtils head = new ItemStackUtils(Material.PLAYER_HEAD);
                head.setName(messages.getString("chatroom-head-display").replace("$name$", player.getName()));
                head.setLore(messages.getString("chatroom-head-lore1")
                        .replace("$connected$", messages.getString("offline")).replace("$role$", chatroom.getRole(uuid)));
                heads.add(head.build());

            }
        }
        return sortListByRank(heads);
    }

    /**
     *
     * @param items to sort
     * @return sorted list of items by chat role
     */
    private List<ItemStack> sortListByRank(List<ItemStack> items){
        return items.stream().sorted((i1, i2) -> new ChatRoleComparator().compare(ChatRole.getRoleFrom(i1), ChatRole.getRoleFrom(i2))).collect(Collectors.toList());
    }
}
