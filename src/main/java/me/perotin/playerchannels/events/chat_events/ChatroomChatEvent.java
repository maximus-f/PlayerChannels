package me.perotin.playerchannels.events.chat_events;

import me.perotin.playerchannels.PlayerChannels;
import me.perotin.playerchannels.objects.Chatroom;
import me.perotin.playerchannels.objects.PlayerChannelUser;
import me.perotin.playerchannels.storage.files.ChannelFile;
import me.perotin.playerchannels.storage.files.FileType;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/** Owns chat routing. Event-thread work is limited to immutable routing data and the event itself. */
public class ChatroomChatEvent implements Listener {

    private final PlayerChannels plugin;
    private final CreateChatroomInputEvent creationInput;
    private final StatusInputEvent statusInput;
    private final ChatroomSetNicknameEvent nicknameInput;
    private final ChatroomSetDescriptionEvent descriptionInput;
    private final ChatroomConfirmDeletionEvent deletionInput;
    private final ChannelFile messages;
    private final ConcurrentMap<UUID, RouteState> routes = new ConcurrentHashMap<>();
    private volatile Set<Player> filteredRecipients = Collections.emptySet();

    private static final class RouteState {
        private final boolean prompt;
        private final boolean focused;
        private final int channelCount;

        private RouteState(boolean prompt, boolean focused, int channelCount) {
            this.prompt = prompt;
            this.focused = focused;
            this.channelCount = channelCount;
        }
    }

    public ChatroomChatEvent(PlayerChannels plugin, CreateChatroomInputEvent creationInput,
                             StatusInputEvent statusInput, ChatroomSetNicknameEvent nicknameInput,
                             ChatroomSetDescriptionEvent descriptionInput,
                             ChatroomConfirmDeletionEvent deletionInput) {
        this.plugin = plugin;
        this.creationInput = creationInput;
        this.statusInput = statusInput;
        this.nicknameInput = nicknameInput;
        this.descriptionInput = descriptionInput;
        this.deletionInput = deletionInput;
        this.messages = new ChannelFile(FileType.MESSAGES);
    }

    /** Call on the main thread after changing a player's route or pending input. */
    public void refresh(PlayerChannelUser user) {
        UUID id = user.getUuid();
        Player player = Bukkit.getPlayer(id);
        if (player == null) {
            routes.remove(id);
            refreshFilteredRecipients();
            return;
        }
        boolean prompt = StatusInputEvent.enteringStatus.contains(player)
                || ChatroomSetNicknameEvent.setNickname.containsKey(player)
                || ChatroomSetDescriptionEvent.setDescription.containsKey(player)
                || ChatroomConfirmDeletionEvent.confirmDeletion.containsKey(player)
                || creationInput.isEnteringText(id);
        routes.put(id, new RouteState(prompt, user.getFocusedChatroom() != null, user.getChatrooms().size()));
        refreshFilteredRecipients();
    }

    public void refresh(Player player) {
        refresh(PlayerChannelUser.getPlayer(player.getUniqueId()));
    }

    public void forget(Player player) {
        routes.remove(player.getUniqueId());
        refreshFilteredRecipients();
    }

    /** Replace a pending text prompt while keeping any in-progress channel draft. */
    public void clearPendingInput(Player player) {
        StatusInputEvent.enteringStatus.remove(player);
        ChatroomSetNicknameEvent.setNickname.remove(player);
        ChatroomSetDescriptionEvent.setDescription.remove(player);
        ChatroomConfirmDeletionEvent.confirmDeletion.remove(player);
        UUID id = player.getUniqueId();
        creationInput.getSetName().remove(id);
        creationInput.getSetDescription().remove(id);
    }

    public void clear() {
        routes.clear();
        filteredRecipients = Collections.emptySet();
        StatusInputEvent.enteringStatus.clear();
        ChatroomSetNicknameEvent.setNickname.clear();
        ChatroomSetDescriptionEvent.setDescription.clear();
        ChatroomConfirmDeletionEvent.confirmDeletion.clear();
        creationInput.getSetName().clear();
        creationInput.getSetDescription().clear();
        creationInput.getInCreation().clear();
    }

    private void refreshFilteredRecipients() {
        Set<Player> recipients = new HashSet<>();
        for (UUID id : plugin.getListeningPlayers()) {
            Player player = Bukkit.getPlayer(id);
            if (player != null) recipients.add(player);
        }
        filteredRecipients = Collections.unmodifiableSet(recipients);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onChatroomChat(AsyncPlayerChatEvent event) {
        UUID id = event.getPlayer().getUniqueId();
        String message = event.getMessage();
        RouteState state = routes.get(id);
        ChatRoutingPolicy.Route route = ChatRoutingPolicy.route(
                state != null && state.prompt,
                state != null && state.focused,
                state == null ? 0 : state.channelCount,
                PlayerChannels.QUICK_CHAT_PREFIX,
                message);

        if (route == ChatRoutingPolicy.Route.NORMAL) {
            event.getRecipients().removeAll(filteredRecipients);
            return;
        }

        event.setCancelled(true);
        Player player = event.getPlayer();
        Runnable action = () -> process(player, message, route);
        if (event.isAsynchronous()) Bukkit.getScheduler().runTask(plugin, action);
        else action.run();
    }

    private void process(Player player, String message, ChatRoutingPolicy.Route route) {
        if (!plugin.isEnabled() || !player.isOnline()) return;
        try {
            if (route == ChatRoutingPolicy.Route.PROMPT) {
                processPrompt(player, message);
                return;
            }
            PlayerChannelUser user = PlayerChannelUser.getPlayer(player.getUniqueId());
            Chatroom channel;
            String content = message;
            if (route == ChatRoutingPolicy.Route.FOCUSED) {
                channel = user.getFocusedChatroom();
            } else if (route == ChatRoutingPolicy.Route.QUICK) {
                channel = user.getChatrooms().size() == 1 ? user.getChatrooms().get(0) : null;
                content = message.substring(PlayerChannels.QUICK_CHAT_PREFIX.length());
            } else {
                String[] parts = message.split(":", 2);
                int index = Integer.parseInt(parts[0]) - 1;
                int size = user.getChatrooms().size();
                if (index < 0 || index >= size) {
                    String key = size == 1 ? "fast-chat-invalid" : "fast-chat-invalid-range";
                    String error = messages.getString(key);
                    player.sendMessage(size == 1 ? error : error.replace("$end$", Integer.toString(size)));
                    return;
                }
                channel = user.getChatrooms().get(index);
                content = parts[1].trim();
            }
            if (channel == null || !user.isMemberOf(channel)) return;
            if (channel.isMuted(player.getUniqueId())) {
                player.sendMessage(messages.getString("muted-message").replace("$chatroom$", channel.getName()));
                return;
            }
            if (!user.getListeningChatrooms().isEmpty() && !user.isListeningTo(channel)) {
                user.addChannelToListen(channel);
            }
            channel.chat(player.getName(), content, player.getUniqueId());
        } finally {
            if (player.isOnline()) refresh(player);
        }
    }

    private void processPrompt(Player player, String message) {
        // A single route owns the input, even if stale UI state left more than one prompt active.
        if (ChatroomConfirmDeletionEvent.confirmDeletion.containsKey(player)) {
            deletionInput.processInput(player, message);
        } else if (ChatroomSetDescriptionEvent.setDescription.containsKey(player)) {
            descriptionInput.processInput(player, message);
        } else if (ChatroomSetNicknameEvent.setNickname.containsKey(player)) {
            nicknameInput.processInput(player, message);
        } else if (StatusInputEvent.enteringStatus.contains(player)) {
            statusInput.processInput(player, message);
        } else if (creationInput.isEnteringText(player.getUniqueId())) {
            creationInput.processInput(player, message);
        }
    }
}
