package me.perotin.playerchannels.commands;

import me.perotin.playerchannels.PlayerChannels;
import me.perotin.playerchannels.storage.files.ChannelFile;
import me.perotin.playerchannels.storage.files.FileType;
import me.perotin.playerchannels.utils.TutorialHelper;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class CancelTutorialCommand extends Command {



    private PlayerChannels plugin;
    private ChannelFile messages;

    public CancelTutorialCommand(@NotNull String name, @NotNull List<String> aliases, PlayerChannels plugin) {
        super(name, "Cancel the tutorial", "/" + name, aliases);
        this.plugin = plugin;
        this.messages = new ChannelFile(FileType.MESSAGES);
    }

    @Override
    public boolean execute(@NotNull CommandSender sender, @NotNull String commandLabel, @NotNull String[] args) {

        Player player = (Player) sender;
        if (TutorialHelper.inTutorial.contains(player.getUniqueId())) {
            TutorialHelper.inTutorial.remove(player.getUniqueId());
            player.sendMessage(messages.getString("cancel-tutorial-cancelled"));
        } else {
            player.sendMessage(messages.getString("cancel-tutorial-not-in"));

        }
        return true;
    }
}
