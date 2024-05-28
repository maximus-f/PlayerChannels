package me.perotin.playerchannels.commands.subcommands;

import me.perotin.playerchannels.PlayerChannels;
import me.perotin.playerchannels.objects.Chatroom;
import me.perotin.playerchannels.objects.PlayerChannelUser;
import me.perotin.playerchannels.storage.files.ChannelFile;
import me.perotin.playerchannels.storage.files.FileType;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.stream.Collectors;

public class ListSubCommand extends SubCommand{


    public ListSubCommand(String label) {
        super(label);
    }

    @Override
    public void onCommand(Player player, PlayerChannelUser user, String[] args) {
        // For loop through channels and send name and status
        ChannelFile messages = new ChannelFile(FileType.MESSAGES);

        List<Chatroom> channels = PlayerChannels.getInstance().getChatrooms();

        if (!player.hasPermission("playerchannels.admin")) {
            channels = channels.stream().filter(c -> !c.isHidden()).collect(Collectors.toList());

            for (Chatroom hidden : PlayerChannels.getInstance().getHiddenChatrooms()) {
                if (hidden.isInChatroom(user.getUuid()) && !channels.contains(hidden)) {
                    channels.add(hidden);
                }
            }
        }

        messages.sendConfigMsg(player, "list-message-1");
        for (Chatroom channel : channels) {
            String status = channel.isPublic() ? messages.getString("public") : messages.getString("private");
            String hidden = channel.isHidden() ? messages.getString("list-message-4") : "";
            String finalMessage = messages.getString("list-message-2")
                    .replace("$hidden$", hidden)
                    .replace("$chatroom$", channel.getName())
                    .replace("$status$", status)
                    .replace("$count$", channel.getMembers().size()+"");

            sendJoinableMessage(player, "/channels join " + channel.getName(), finalMessage, channel.getName());
        }

    }

    private void sendJoinableMessage(Player player, String command, String message, String name) {
        if (message != null) {
            TextComponent messageComponent = new TextComponent(ChatColor.translateAlternateColorCodes('&', message));
            messageComponent.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, command));
            messageComponent.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(ChatColor.GREEN + "Click to join " + ChatColor.YELLOW +  name).create()));
            player.spigot().sendMessage(messageComponent);
        } else {
            player.sendMessage(ChatColor.RED + "There was an error loading the message for: " + message);
        }
    }
}
