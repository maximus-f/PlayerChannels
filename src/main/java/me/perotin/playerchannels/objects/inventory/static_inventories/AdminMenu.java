package me.perotin.playerchannels.objects.inventory.static_inventories;

import com.github.stefvanschie.inventoryframework.gui.GuiItem;
import com.github.stefvanschie.inventoryframework.pane.StaticPane;
import me.perotin.playerchannels.PlayerChannels;
import me.perotin.playerchannels.objects.Chatroom;
import me.perotin.playerchannels.objects.InventoryHelper;
import me.perotin.playerchannels.objects.inventory.paging_objects.AdminDeleteChatroomPager;
import me.perotin.playerchannels.objects.inventory.paging_objects.AdminSpyChatroomPager;
import me.perotin.playerchannels.storage.Pair;
import me.perotin.playerchannels.storage.files.ChannelFile;
import me.perotin.playerchannels.storage.files.FileType;
import me.perotin.playerchannels.utils.ChannelUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;

public class AdminMenu extends StaticMenu {


    private InventoryHelper helper;

    private StaticPane adminTools;


    public AdminMenu(Player viewer, String title) {
        super(viewer, title);
        helper = PlayerChannels.getInstance().getHelper();
        helper.setNavigationBar(getMenu(), getViewer());
        helper.setSideDecorationSlots(getMenu());
        helper.setPagingNavBar(getMenu());
        this.adminTools = new StaticPane(2, 2, 5, 2);
        setAdminTools();
        getMenu().addPane(adminTools);
    }

    private void setAdminTools() {
        ChannelFile messages = new ChannelFile(FileType.MESSAGES);
        Pair<ItemStack, Integer> stopPlugin = InventoryHelper.getItem("admin-menu.stop-plugin", null);
        Pair<ItemStack, Integer> deleteChat = InventoryHelper.getItem("admin-menu.delete-chatroom", null);
        Pair<ItemStack, Integer> spyChatroom = InventoryHelper.getItem("admin-menu.spy-chatroom", null);
        Pair<ItemStack, Integer> reloadPlugin = InventoryHelper.getItem("admin-menu.reload-plugin", null);
        adminTools.addItem(new GuiItem(stopPlugin.getFirst(), stopPlugin()), stopPlugin.getSecond(), 0);
        adminTools.addItem(new GuiItem(deleteChat.getFirst(), deleteChatroom()), deleteChat.getSecond(), 0);
        adminTools.addItem(new GuiItem(spyChatroom.getFirst(), spyFunction()), spyChatroom.getSecond(), 0);
        adminTools.addItem(new GuiItem(reloadPlugin.getFirst(), reloadYaml()), reloadPlugin.getSecond(), 1);

    }

    private Consumer<InventoryClickEvent> stopPlugin(){
        return inventoryClickEvent -> {
            inventoryClickEvent.setCancelled(true);
            Player clicker = (Player) inventoryClickEvent.getWhoClicked();
            clicker.closeInventory();
            clicker.sendMessage(ChatColor.RED + "Stopping PlayerChannels plugin.");
            Bukkit.getPluginManager().disablePlugin(PlayerChannels.getInstance());
        };
    }

    private Consumer<InventoryClickEvent> deleteChatroom(){
        return inventoryClickEvent -> {
            inventoryClickEvent.setCancelled(true);
            Player clicker = (Player) inventoryClickEvent.getWhoClicked();
            clicker.closeInventory();

            // Show another menu with all chatrooms for them to chose to delete one
            new AdminDeleteChatroomPager(clicker, this.getMenu()).show();

        };
    }

    private Consumer<InventoryClickEvent> spyFunction() {
        return event -> {
            event.setCancelled(true);
            if (event.getClick() == ClickType.LEFT) {
                Player player = (Player) event.getWhoClicked();
                UUID playerUuid = player.getUniqueId();
                List<Chatroom> chatrooms = PlayerChannels.getInstance().getChatrooms();
                boolean isSpyingOnAll = chatrooms.stream().allMatch(chatroom -> chatroom.getSpyers().contains(playerUuid));

                if (isSpyingOnAll) {
                    // The admin is spying on all chatrooms, remove them from all.
                    chatrooms.forEach(chatroom -> chatroom.removeSpy(playerUuid));
                    player.sendMessage(ChatColor.RED + "You are no longer spying on any chatrooms.");
                } else {
                    // The admin is not spying on all chatrooms, add them to all.
                    chatrooms.forEach(chatroom -> {
                        if (!chatroom.getSpyers().contains(playerUuid)) {
                            chatroom.addSpy(playerUuid);
                        }
                    });
                    player.sendMessage(ChatColor.GREEN + "You are now spying on all chatrooms.");
                }
            } else if (event.getClick() == ClickType.RIGHT) {
                new AdminSpyChatroomPager((Player) event.getWhoClicked(), getMenu()).show(event.getWhoClicked());

            }
        };
    }

    private Consumer<InventoryClickEvent> reloadYaml() {
        return event -> {
            event.setCancelled(true);
            // Assuming you have a reloadConfig method implemented
            PlayerChannels.getInstance().reloadConfig("chatrooms.yml");
            PlayerChannels.getInstance().reloadConfig("config.yml");
            PlayerChannels.getInstance().reloadConfig("menus.yml");
            PlayerChannels.getInstance().reloadConfig("messages.yml");
            PlayerChannels.getInstance().reloadConfig("players.yml");

            // Provide feedback to the player that the files have been reloaded.
            Player player = (Player) event.getWhoClicked();
            ChannelUtils.sendMenuMessage("Config files have been reloaded", player, null);
        };
    }






}
