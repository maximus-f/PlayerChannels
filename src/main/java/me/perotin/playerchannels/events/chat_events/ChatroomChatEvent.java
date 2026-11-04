package me.perotin.playerchannels.events.chat_events;

import me.perotin.playerchannels.PlayerChannels;
import me.perotin.playerchannels.objects.Chatroom;
import me.perotin.playerchannels.objects.ChatRole;
import me.perotin.playerchannels.objects.PlayerChannelUser;
import me.perotin.playerchannels.objects.PreChatroom;
import me.perotin.playerchannels.storage.Pair;
import me.perotin.playerchannels.storage.files.ChannelFile;
import me.perotin.playerchannels.storage.files.FileType;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import java.util.Collections;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
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
    // Only accessed on the main thread. Each object identifies one particular prompt session.
    private final Map<UUID, PromptSession> activePrompts = new HashMap<>();
    private volatile Set<Player> filteredRecipients = Collections.emptySet();

    private static final class RouteState {
        private final PromptSession prompt;
        private final Chatroom focused;
        private final List<Chatroom> channels;

        private RouteState(PromptSession prompt, Chatroom focused, List<Chatroom> channels) {
            this.prompt = prompt;
            this.focused = focused;
            // The async callback may select references, but never reads the channels' mutable fields.
            this.channels = Collections.unmodifiableList(new ArrayList<>(channels));
        }
    }

    private enum PromptType {
        DELETION, DESCRIPTION, NICKNAME, STATUS, CREATION_NAME, CREATION_DESCRIPTION
    }

    private static final class PromptSession {
        private final PromptType type;
        private final Chatroom channel;
        private final UUID nicknameTarget;
        private final PreChatroom draft;

        private PromptSession(PromptType type, Chatroom channel, UUID nicknameTarget, PreChatroom draft) {
            this.type = type;
            this.channel = channel;
            this.nicknameTarget = nicknameTarget;
            this.draft = draft;
        }

        private boolean hasSameTarget(PromptSession other) {
            return other != null && type == other.type && channel == other.channel
                    && Objects.equals(nicknameTarget, other.nicknameTarget) && draft == other.draft;
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
        PromptSession prompt = findPrompt(player);
        if (prompt == null) {
            activePrompts.remove(id);
        } else {
            PromptSession previous = activePrompts.get(id);
            if (prompt.hasSameTarget(previous)) prompt = previous;
            activePrompts.put(id, prompt);
        }
        // Reconstruct live filtering from the cached preference, including after a reconnect.
        if (user.getListeningChatrooms().isEmpty()) plugin.getListeningPlayers().remove(id);
        else plugin.getListeningPlayers().add(id);
        routes.put(id, new RouteState(prompt, user.getFocusedChatroom(), user.getChatrooms()));
        refreshFilteredRecipients();
    }

    public void refresh(Player player) {
        refresh(PlayerChannelUser.getPlayer(player.getUniqueId()));
    }

    public void forget(Player player) {
        routes.remove(player.getUniqueId());
        activePrompts.remove(player.getUniqueId());
        refreshFilteredRecipients();
    }

    /** Replace a pending text prompt while keeping any in-progress channel draft. */
    public void clearPendingInput(Player player) {
        activePrompts.remove(player.getUniqueId());
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
        activePrompts.clear();
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
        String prefix = PlayerChannels.QUICK_CHAT_PREFIX;
        ChatRoutingPolicy.Route route = ChatRoutingPolicy.route(
                state != null && state.prompt != null,
                state != null && state.focused != null,
                state == null ? 0 : state.channels.size(),
                prefix,
                message);

        if (route == ChatRoutingPolicy.Route.NORMAL) {
            event.getRecipients().removeAll(filteredRecipients);
            return;
        }

        event.setCancelled(true);
        Player player = event.getPlayer();
        Runnable action = () -> process(player, message, route, state, prefix);
        if (event.isAsynchronous()) Bukkit.getScheduler().runTask(plugin, action);
        else action.run();
    }

    private void process(Player player, String message, ChatRoutingPolicy.Route route,
                         RouteState state, String prefix) {
        if (!plugin.isEnabled() || !player.isOnline()) return;
        try {
            if (route == ChatRoutingPolicy.Route.PROMPT) {
                processPrompt(player, message, state.prompt);
                return;
            }
            PlayerChannelUser user = PlayerChannelUser.getPlayer(player.getUniqueId());
            Chatroom channel;
            String content = message;
            if (route == ChatRoutingPolicy.Route.FOCUSED) {
                channel = state.focused;
            } else if (route == ChatRoutingPolicy.Route.QUICK) {
                channel = state.channels.get(0);
                content = message.substring(prefix.length());
            } else {
                String[] parts = message.split(":", 2);
                int index = Integer.parseInt(parts[0]) - 1;
                int size = state == null ? 0 : state.channels.size();
                if (index < 0 || index >= size) {
                    String key = size == 1 ? "fast-chat-invalid" : "fast-chat-invalid-range";
                    String error = messages.getString(key);
                    player.sendMessage(size == 1 ? error : error.replace("$end$", Integer.toString(size)));
                    return;
                }
                channel = state.channels.get(index);
                content = parts[1].trim();
            }
            if (!isAvailable(channel) || !user.isMemberOf(channel) || !channel.isInChatroom(player.getUniqueId())) {
                sendExpiredMessage(player);
                return;
            }
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

    private PromptSession findPrompt(Player player) {
        UUID id = player.getUniqueId();
        if (ChatroomConfirmDeletionEvent.confirmDeletion.containsKey(player)) {
            return new PromptSession(PromptType.DELETION,
                    ChatroomConfirmDeletionEvent.confirmDeletion.get(player), null, null);
        }
        if (ChatroomSetDescriptionEvent.setDescription.containsKey(player)) {
            return new PromptSession(PromptType.DESCRIPTION,
                    ChatroomSetDescriptionEvent.setDescription.get(player), null, null);
        }
        if (ChatroomSetNicknameEvent.setNickname.containsKey(player)) {
            Pair<Chatroom, UUID> target = ChatroomSetNicknameEvent.setNickname.get(player);
            return new PromptSession(PromptType.NICKNAME, target.getFirst(), target.getSecond(), null);
        }
        if (StatusInputEvent.enteringStatus.contains(player)) {
            return new PromptSession(PromptType.STATUS, null, null, null);
        }
        if (creationInput.isEnteringText(id)) {
            PromptType type = creationInput.getSetName().contains(id)
                    ? PromptType.CREATION_NAME : PromptType.CREATION_DESCRIPTION;
            return new PromptSession(type, null, null, creationInput.getInCreation().get(id));
        }
        return null;
    }

    private void processPrompt(Player player, String message, PromptSession prompt) {
        UUID id = player.getUniqueId();
        if (activePrompts.get(id) != prompt || !prompt.hasSameTarget(findPrompt(player))) {
            sendExpiredMessage(player);
            return;
        }
        if (prompt.channel != null) {
            boolean permitted = isAvailable(prompt.channel)
                    && PlayerChannelUser.getPlayer(id).isMemberOf(prompt.channel)
                    && prompt.channel.isInChatroom(id);
            switch (prompt.type) {
                case DELETION:
                    permitted = permitted && prompt.channel.getRole(id) == ChatRole.OWNER && !prompt.channel.isServerOwned();
                    break;
                case DESCRIPTION:
                    permitted = permitted && prompt.channel.hasModeratorPermissions(id);
                    break;
                case NICKNAME:
                    permitted = permitted && (id.equals(prompt.nicknameTarget) || prompt.channel.hasModeratorPermissions(id))
                            && prompt.channel.isInChatroom(prompt.nicknameTarget);
                    break;
                default:
                    break;
            }
            if (!permitted) {
                clearPendingInput(player);
                sendExpiredMessage(player);
                return;
            }
        }
        switch (prompt.type) {
            case DELETION:
                deletionInput.processInput(player, message);
                break;
            case DESCRIPTION:
                descriptionInput.processInput(player, message);
                break;
            case NICKNAME:
                nicknameInput.processInput(player, message);
                break;
            case STATUS:
                statusInput.processInput(player, message);
                break;
            case CREATION_NAME:
            case CREATION_DESCRIPTION:
                creationInput.processInput(player, message);
                break;
        }
    }

    private boolean isAvailable(Chatroom channel) {
        // Compare object identity so a deleted channel cannot be replaced by another with the same name.
        return channel != null && plugin.getChatrooms().stream().anyMatch(current -> current == channel);
    }

    private void sendExpiredMessage(Player player) {
        String message = messages.getConfiguration().getString("chat-input-expired",
                "&cYour chat destination is no longer available. Please send your message again.");
        player.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
    }
}
