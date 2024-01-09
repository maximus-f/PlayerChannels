package me.perotin.playerchannels.commands;

import me.perotin.playerchannels.PlayerChannels;
import me.perotin.playerchannels.commands.subcommands.FocusChannelSubCommand;
import me.perotin.playerchannels.events.chat_events.CreateChatroomInputEvent;
import me.perotin.playerchannels.objects.Chatroom;
import me.perotin.playerchannels.objects.InventoryHelper;
import me.perotin.playerchannels.objects.PlayerChannelUser;
import me.perotin.playerchannels.objects.PreChatroom;
import me.perotin.playerchannels.objects.inventory.paging_objects.MainMenuPaging;
import me.perotin.playerchannels.storage.files.ChannelFile;
import me.perotin.playerchannels.storage.files.FileType;
import me.perotin.playerchannels.utils.ChannelUtils;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/* Created by Perotin on 8/20/19 */

/**
 * Base command for PlayerChannels, extends (need to look into)Command for ability to set custom command names, aliases etc.
 */
public class PlayerChannelsCommand implements CommandExecutor {



    private PlayerChannels plugin;
    private ChannelFile messages;


   public PlayerChannelsCommand( PlayerChannels plugin){
       this.plugin = plugin;
       this.messages = new ChannelFile(FileType.MESSAGES);
    }


// TODO send help message dialog

    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {
        Player player = (Player) commandSender;
        PlayerChannelUser playerChannelUser = PlayerChannelUser.getPlayer(player.getUniqueId());
        if (args.length > 0) {
            // Check if arg length is greater than 0 and if second key-word is "focus" or other subcommands
            String secondArg = args[0];
            if (secondArg.equalsIgnoreCase(plugin.getConfig().getString("help"))) {
                // Help command
                  ChannelUtils.sendMsgFromConfig(player, "help-msg");
                sendClickableCommand(player, "/channels", "help-msg-1");
                sendClickableCommand(player, "/channels create <name> [Optional: description]", "help-msg-2");
                sendClickableCommand(player, "/channels join <name>", "help-msg-3");
                sendClickableCommand(player, "/channels listen <add/remove/off> <name>", "help-msg-4");
                // Add more messages as needed


                return true;
            }
            if (secondArg.equalsIgnoreCase(plugin.getConfig().getString("join"))) {
                if (args.length == 1) {
                    messages.sendConfigMsg(player, "join-subcommand-help");
                    return true;
                } else if (args.length > 1) {
                    // Try to find chatroom with args[2]
                    String chatroomName = args[1];
                    Chatroom found = plugin.getChatroom(chatroomName);
                    if (found == null) {
                        messages.sendConfigMsg(player, "join-subcommand-not-found");
                        return true;
                    } else {
                        // check if player can join the chatroom
                        // Not staff and is private
                        if (!(player.hasPermission("playerchannels.admin") || player.hasPermission("playerchannels.moderator"))){
                            if (!found.isPublic()) {
                                messages.sendConfigMsg(player, "join-subcommand-private");
                                return true;
                            }
                        }
                        // otherwise, let them join
                        ChannelUtils.joinChatroom(playerChannelUser, found);
                        return true;

                    }
                }
            }
            if (secondArg.equalsIgnoreCase(plugin.getConfig().getString("create"))){
                if (args.length == 1) {
                    // Open creation menu with nothing set
                    PreChatroom chatroom = new PreChatroom(player.getUniqueId());
                    InventoryHelper helper = PlayerChannels.getInstance().getHelper();
                    CreateChatroomInputEvent.getInstance().getInCreation().put(player.getUniqueId(), chatroom);
                    helper.setNavigationBar(helper.getCreationMenu(chatroom),  player).getFirst().show(player);
                    return true;
                } else if (args.length == 2) {
                    // Open creation menu with name set
                    String name = args[1]; // TODO Validate data (e.g. name not taken)
                    if (CreateChatroomInputEvent.isNameTaken(name)) {
                        player.sendMessage(messages.getString("taken-name"));
                        return true;
                    }
                    PreChatroom chatroom = new PreChatroom(player.getUniqueId());
                    chatroom.setName(name);
                    InventoryHelper helper = PlayerChannels.getInstance().getHelper();
                    CreateChatroomInputEvent.getInstance().getInCreation().put(player.getUniqueId(), chatroom);
                    helper.setNavigationBar(helper.getCreationMenu(chatroom),  player).getFirst().show(player);
                    return true;
                } else {
                    // Open creation menu with name & description set
                    String name = args[1]; // TODO Validate data (e.g. name not taken)
                    if (CreateChatroomInputEvent.isNameTaken(name)) {
                        player.sendMessage(messages.getString("taken-name"));
                        return true;
                    }
                    StringBuilder builder = new StringBuilder();
                    for (int j = 2; j < args.length; j++) {
                        builder.append(args[j] +" ");
                    }
                    String description = builder.toString(); // This is not correct because it should be the remaining args but whatever
                    PreChatroom chatroom = new PreChatroom(player.getUniqueId());
                    chatroom.setName(name);
                    chatroom.setDescription(description);
                    InventoryHelper helper = PlayerChannels.getInstance().getHelper();
                    CreateChatroomInputEvent.getInstance().getInCreation().put(player.getUniqueId(), chatroom);
                    helper.setNavigationBar(helper.getCreationMenu(chatroom),  player).getFirst().show(player);
                    return true;
                }
            }
            if (secondArg.equalsIgnoreCase(plugin.getConfig().getString("listen"))){
                /*
                Possible commands of this form
                /playerchannels listen add <name>
                /playerchannels listen remove <name>
                /playerchannels listen off
                anything else send help
                 */
                String thirdArg = "";
                if (args.length > 1) thirdArg = args[1];

                if (args.length == 2) {
                  // Check for condition where they do listen off
                  if (thirdArg.equalsIgnoreCase("off")) {
                      // Remove the player from any blockers and let them view normal global chat again
                      playerChannelUser.getListeningChatrooms().clear();
                      plugin.getListeningPlayers().remove(player.getUniqueId());
                      player.sendMessage(messages.getString("listen-off"));
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

                player.sendMessage(messages.getString("listen-help")
                        .replace("$listen$", plugin.getConfig().getString("listen")));
                return true;
            }
            // Check if arg is any of the name of that which they are a member of
            List<String> memberChatroomNames = playerChannelUser.getChatrooms().stream().map(Chatroom::getName).map(ChatColor::stripColor).map(String::toLowerCase).collect(Collectors.toList());

            if (memberChatroomNames.contains(args[0].toLowerCase())) {
                new FocusChannelSubCommand("").onCommand(player, playerChannelUser, args);
                return true;
            } else {
                /*
          focus-channel-not-found: "&cYou are not a member of that channel! You can only focus on channels you are a member of."
focus-channel-list: "&a-&e $chatroom$"
             */
                messages.sendConfigMsg(player, "focus-channel-not-found");
                if (playerChannelUser.getChatrooms().isEmpty()) {
                    messages.sendConfigMsg(player, "focus-channel-no-channels");
                } else {
                    playerChannelUser.getChatrooms().forEach(c -> {
                        player.sendMessage(messages.getString("focus-channel-list")
                                .replace("$chatroom$", c.getName()));
                    });
                }
            }

        }
        PreChatroom chat = new PreChatroom(player.getUniqueId());

        MainMenuPaging mainMenuPaging = new MainMenuPaging(player, plugin);
        mainMenuPaging.getMenu().show(player);



        return true;
   }


    private void sendClickableCommand(Player player, String command, String messageConfigPath) {
        String message = plugin.getConfig().getString(messageConfigPath);
        if (message != null) {
            TextComponent messageComponent = new TextComponent(ChatColor.translateAlternateColorCodes('&', message));
            messageComponent.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, command));
            messageComponent.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("Click to copy").create()));
            player.spigot().sendMessage(messageComponent);
        } else {
            player.sendMessage(ChatColor.RED + "There was an error loading the message for: " + messageConfigPath);
        }
    }


}
