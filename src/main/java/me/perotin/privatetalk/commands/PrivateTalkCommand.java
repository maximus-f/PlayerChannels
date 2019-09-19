package me.perotin.privatetalk.commands;

import com.github.stefvanschie.inventoryframework.Gui;
import com.github.stefvanschie.inventoryframework.pane.StaticPane;
import me.perotin.privatetalk.PrivateTalk;
import me.perotin.privatetalk.objects.InventoryHelper;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/* Created by Perotin on 8/20/19 */

/**
 * Base command for PrivateTalk, extends Command for ability to set custom command names, aliases etc.
 */
public class PrivateTalkCommand extends Command implements CommandExecutor  {



    private PrivateTalk plugin;


    public PrivateTalkCommand(@NotNull String name, @NotNull String description, @NotNull String usageMessage, @NotNull List<String> aliases, PrivateTalk plugin) {
        super(name, description, usageMessage, aliases);
        this.plugin = plugin;
    }

    //ignored
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        Gui gui = new Gui(plugin, 6, "Test");
        StaticPane test = new StaticPane(1, 1, 4, 3);
        test.fillWith(InventoryHelper.DECO_ITEM());
        gui.addPane(test);
        Player player = (Player) sender;
        gui.show(player);
        player.sendMessage("test");
        return true;
    }


    @Override
    public boolean execute(@NotNull CommandSender sender, @NotNull String commandLabel, @NotNull String[] args) {
        Gui gui = new Gui(plugin, 6, "Test");
        plugin.getHelper().setCreationMenu(gui);
        Player clicker = (Player) sender;
        gui.show(clicker);
        return true;
    }
}
