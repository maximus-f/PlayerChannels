package me.perotin.privatetalk.objects.inventory.static_inventories;

import me.perotin.privatetalk.objects.Chatroom;
import me.perotin.privatetalk.objects.PrivatePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class SelectChatroomToInviteMenu extends StaticMenu {
    public SelectChatroomToInviteMenu(Player inviter, PrivatePlayer inRoom, PrivatePlayer invited) {
        super(inviter, "Select chatroom to invite to");
        // TODO Doing this is a prototype manner to see if done, needs to be refactored
        inRoom.getChatrooms().forEach(c->getMenu().getInventory().addItem(c.getItem()));

    }
}
