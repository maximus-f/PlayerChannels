package me.perotin.privatetalk.events.chat_events;

import me.perotin.privatetalk.objects.PreChatroom;
import me.perotin.privatetalk.utils.PrivateUtils;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.UUID;

/* Created by Perotin on 9/19/19 */
public class CreateChatroomInputEvent implements Listener {

    private static CreateChatroomInputEvent instance;
    private HashSet<UUID> setName = new HashSet<>();
    private HashSet<UUID> setDescription = new HashSet<>();
    private Map<UUID, PreChatroom> inCreation;


    public CreateChatroomInputEvent() {
        this.inCreation = new HashMap<>();
        instance = this;
    }

    @EventHandler
    public void onChat(AsyncPlayerChatEvent event) {
        Player chatter = event.getPlayer();

        if (inCreation.containsKey(chatter.getUniqueId())) {
            PreChatroom preChatroom = inCreation.get(chatter.getUniqueId());
            event.setCancelled(true);
            if (setName.contains(chatter.getUniqueId())) {
                String name = event.getMessage();
                // check if name is more than 1 word
                if(name.split(" ").length > 1){
                    //too many words TODO come up with way to show messages, maybe big text on screen y'know what I mean
                    chatter.sendTitle(ChatColor.RED+"Use only 1 word", "", 0, 20*3, 20);
                }
                if(isNameTaken(name)){

                }
            } else if (setDescription.contains(chatter.getUniqueId())) {
                String description = event.getMessage();
            }
        }
    }

    public static CreateChatroomInputEvent getInstance() {
        return instance;
    }

    public HashSet<UUID> getSetName() {
        return setName;
    }

    public HashSet<UUID> getSetDescription() {
        return setDescription;
    }

    public Map<UUID, PreChatroom> getInCreation() {
        return inCreation;
    }

    //TODO Show updated menu after inputting certain fields
    private void showUpdatedMenu(Player toShow, PreChatroom view){

    }


    /**
     *
     * @param name
     * @return true if name is used by another chatroom, false if not
     */
    private boolean isNameTaken(String name){
        return PrivateUtils.getChatroomWith(name) != null;
    }
}
