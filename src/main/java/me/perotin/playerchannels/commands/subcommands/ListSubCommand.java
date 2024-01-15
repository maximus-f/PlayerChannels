package me.perotin.playerchannels.commands.subcommands;

import me.perotin.playerchannels.objects.PlayerChannelUser;
import org.bukkit.entity.Player;

public class ListSubCommand extends SubCommand{


    public ListSubCommand(String label) {
        super(label);
    }

    @Override
    public void onCommand(Player player, PlayerChannelUser user, String[] args) {
        // For loop through channels and send name and status
    }
}
