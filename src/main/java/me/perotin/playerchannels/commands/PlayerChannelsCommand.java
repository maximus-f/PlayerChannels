package me.perotin.playerchannels.commands;

import me.perotin.playerchannels.PlayerChannels;
import me.perotin.playerchannels.objects.Chatroom;
import me.perotin.playerchannels.objects.PlayerChannelUser;
import me.perotin.playerchannels.objects.PreChatroom;
import me.perotin.playerchannels.objects.inventory.paging_objects.MainMenuPaging;
import me.perotin.playerchannels.storage.files.ChannelFile;
import me.perotin.playerchannels.storage.files.FileType;
import me.perotin.playerchannels.utils.ChannelUtils;
import org.bukkit.ChatColor;
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
    private ChannelFile messages;


   public PlayerChannelsCommand(String name, List<String> aliases, PlayerChannels plugin){
       super(name, "Main command for all chatrooms", "/" + name, aliases);
       this.plugin = plugin;
       this.messages = new ChannelFile(FileType.MESSAGES);
    }


// TODO send help message dialog

    @Override
    public boolean execute(@NotNull CommandSender commandSender, @NotNull String s, @NotNull String[] args) {
        Player player = (Player) commandSender;
        PlayerChannelUser playerChannelUser = PlayerChannelUser.getPlayer(player.getUniqueId());
        if (args.length > 0) {
            // Check if arg length is greater than 0 and if second key-word is "focus" or something of the sort
            String secondArg = args[0];
            if (secondArg.equalsIgnoreCase(plugin.getConfig().getString("listen"))){
                /*
                Possible commands of this form
                /playerchannels listen add <name>
                /playerchannels listen remove <name>
                /playerchannels listen off
                anything else send help
                 */
                String thirdArg = args[1];

                if (args.length == 2) {
                  // Check for condition where they do listen off
                  if (thirdArg.equalsIgnoreCase("off")) {
                      // Remove the player from any blockers and let them view normal global chat again
                      return true;
                  }
              } else if (args.length == 3) {
                    String chatroomName = args[2];
                    // Need to verify that the chatroom is loaded and active and correct
                    Chatroom chatroom = ChannelUtils.getChatroomWith(chatroomName);
                    if (chatroom == null) {
                        // If null send message
                        player.sendMessage(ChatColor.RED + "That chatroom does not exist or is not active!");
                        return true;
                    }
                    if (!playerChannelUser.isMemberOf(chatroom)) {
                        player.sendMessage(messages.getString("listen-not-in-channel"));
                        return true;
                    }
                  if (thirdArg.equalsIgnoreCase("add")) {
                      // Add following chatroom
                      // Things to check:
                      /*
                      Check if user is in that chatroom first and if they are not already listening to that chatroom
                       */

                      if (!playerChannelUser.isListeningTo(chatroom)) {
                          player.sendMessage(messages.getString("listen-added-channel")
                                  .replace("$chatroom$", chatroomName));

                          playerChannelUser.addChannelToListen(chatroom);
                          return true;
                      } else if (playerChannelUser.isListeningTo(chatroom)) {
                          // remove them
                          player.sendMessage(messages.getString("listen-removed-channel")
                                  .replace("$chatroom$", chatroomName));
                          playerChannelUser.removeChannelToListen(chatroom);
                          return true;
                      }


                  } else if (thirdArg.equalsIgnoreCase("remove")) {
                      // Remove subsequent chatroom
                      if (playerChannelUser.isListeningTo(chatroom)) {
                          player.sendMessage(messages.getString("listen-removed-channel")
                                  .replace("$chatroom$", chatroomName));
                          playerChannelUser.removeChannelToListen(chatroom);
                          return true;
                      } else {
                          player.sendMessage(messages.getString("listen-not-currently-listening"));
                          return true;
                      }


                  }
              }


            }
        }
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
