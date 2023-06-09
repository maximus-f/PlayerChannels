package me.perotin.playerchannels.commands;

import me.perotin.playerchannels.PlayerChannels;
import me.perotin.playerchannels.objects.PreChatroom;
import me.perotin.playerchannels.objects.inventory.paging_objects.MainMenuPaging;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/* Created by Perotin on 8/20/19 */

/**
 * Base command for PlayerChannels, extends (need to look into)Command for ability to set custom command names, aliases etc.
 */
public class PlayerChannelsCommand extends Command  {



    private PlayerChannels plugin;


   public PlayerChannelsCommand(String name, List<String> aliases, PlayerChannels plugin){
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
        mainMenuPaging.getMenu().show(player);


        return true;
   }
}
