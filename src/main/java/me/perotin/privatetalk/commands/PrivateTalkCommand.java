package me.perotin.privatetalk.commands;

import com.github.stefvanschie.inventoryframework.Gui;
import me.perotin.privatetalk.PrivateTalk;
import me.perotin.privatetalk.events.chat_events.CreateChatroomInputEvent;
import me.perotin.privatetalk.objects.PreChatroom;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

/* Created by Perotin on 8/20/19 */

/**
 * Base command for PrivateTalk, extends (need to look into)Command for ability to set custom command names, aliases etc.
 */
public class PrivateTalkCommand  implements CommandExecutor  {



    private PrivateTalk plugin;


   public PrivateTalkCommand(PrivateTalk plugin){
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        Player player = (Player) sender;
        Gui menu = new Gui(plugin, 5, "Test");
        plugin.getHelper().setPagingNavBar(menu, true, true).show((Player)sender);

//        PreChatroom chat = new PreChatroom(player.getUniqueId());
//        Gui menu =  plugin.getHelper().getCreationMenu(chat);
//        plugin.getHelper().setPagingNavBar(plugin.getHelper().setNavigationBar(menu, player), false, true).show(player);
//
//        CreateChatroomInputEvent.getInstance().getInCreation().put(player.getUniqueId(), chat);
        return true;
    }



}
