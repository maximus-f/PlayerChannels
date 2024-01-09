package me.perotin.playerchannels.commands.subcommands;

import me.perotin.playerchannels.objects.PlayerChannelUser;
import org.bukkit.entity.Player;

public abstract class SubCommand {

    private String label;

    public SubCommand(String label) {
        this.label = label;
    }

    public abstract void onCommand(Player player, PlayerChannelUser user, String[] args);
}
