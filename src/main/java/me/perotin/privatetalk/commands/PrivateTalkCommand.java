package me.perotin.privatetalk.commands;

import com.github.stefvanschie.inventoryframework.Gui;
import me.perotin.privatetalk.PrivateTalk;
import me.perotin.privatetalk.events.chat_events.CreateChatroomInputEvent;
import me.perotin.privatetalk.objects.PreChatroom;
import me.perotin.privatetalk.objects.inventory.paging_objects.MainMenuPaging;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Objects;

/* Created by Perotin on 8/20/19 */

/**
 * Base command for PrivateTalk, extends (need to look into)Command for ability to set custom command names, aliases etc.
 */
public class PrivateTalkCommand extends Command  {



    private PrivateTalk plugin;


   public PrivateTalkCommand(String name, List<String> aliases, PrivateTalk plugin){
       super(name, "Main command for all chatrooms", "/" + name, aliases);
       this.plugin = plugin;
    }




    @Override
    public boolean execute(@NotNull CommandSender commandSender, @NotNull String s, @NotNull String[] strings) {
        Player player = (Player) commandSender;
        PreChatroom chat = new PreChatroom(player.getUniqueId());
//        Gui menu =  plugin.getHelper().getCreationMenu(chat);
//
//        plugin.getHelper().setPagingNavBar(plugin.getHelper().setNavigationBar(menu, player), false, true).show(player);

      //  CreateChatroomInputEvent.getInstance().getInCreation().put(player.getUniqueId(), chat);

        MainMenuPaging mainMenuPaging = new MainMenuPaging(player, plugin);
        if (mainMenuPaging.getMenu() == null) Bukkit.broadcastMessage("MENU IS NULL---");
        mainMenuPaging.getMenu().show(player);
        return true;
   }
}
