package me.perotin.playerchannels.events.chat_events;

import com.github.stefvanschie.inventoryframework.gui.type.ChestGui;
import com.github.stefvanschie.inventoryframework.gui.type.util.Gui;
import me.perotin.playerchannels.PlayerChannels;
import me.perotin.playerchannels.events.join.PlayerChannelUserJoinEvent;
import me.perotin.playerchannels.objects.ChatRole;
import me.perotin.playerchannels.objects.Chatroom;
import me.perotin.playerchannels.objects.InventoryHelper;
import me.perotin.playerchannels.objects.PlayerChannelUser;
import me.perotin.playerchannels.objects.PreChatroom;
import me.perotin.playerchannels.objects.inventory.actions.CreateChatroomAction;
import me.perotin.playerchannels.storage.Pair;
import me.perotin.playerchannels.storage.files.ChannelFile;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.scheduler.BukkitScheduler;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.MockedConstruction;
import org.mockito.MockedStatic;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Queue;
import java.util.Set;
import java.util.UUID;
import java.util.function.Consumer;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/** Runs the real event callback, pauses its scheduler, changes state, then resumes the main-thread task. */
public class ChatroomChatEventTest {
    private final UUID id = UUID.randomUUID();
    private final Queue<Runnable> tasks = new ArrayDeque<>();
    private final List<Chatroom> memberships = new ArrayList<>();
    private final List<Chatroom> registered = new ArrayList<>();
    private final List<Chatroom> listening = new ArrayList<>();
    private final Set<UUID> listeningPlayers = new HashSet<>();
    private MockedStatic<Bukkit> bukkit;
    private MockedStatic<PlayerChannelUser> users;
    private MockedConstruction<ChannelFile> files;
    private PlayerChannels plugin;
    private Player player;
    private PlayerChannelUser user;
    private Chatroom staff;
    private Chatroom trade;
    private ChatroomChatEvent router;
    private CreateChatroomInputEvent creation;
    private ChatroomSetDescriptionEvent descriptions;
    private StatusInputEvent statuses;
    private ChatroomSetNicknameEvent nicknames;
    private ChatroomConfirmDeletionEvent deletions;

    @Before
    public void setUp() {
        plugin = mock(PlayerChannels.class);
        player = mock(Player.class);
        user = mock(PlayerChannelUser.class);
        staff = mock(Chatroom.class);
        trade = mock(Chatroom.class);
        when(staff.getName()).thenReturn("Staff");
        when(trade.getName()).thenReturn("Trade");
        when(staff.isInChatroom(id)).thenReturn(true);
        when(trade.isInChatroom(id)).thenReturn(true);
        registered.addAll(Arrays.asList(staff, trade));
        memberships.addAll(registered);
        when(plugin.isEnabled()).thenReturn(true);
        when(plugin.getChatrooms()).thenReturn(registered);
        when(plugin.getListeningPlayers()).thenReturn(listeningPlayers);
        when(player.getUniqueId()).thenReturn(id);
        when(player.getName()).thenReturn("Sender");
        when(player.isOnline()).thenReturn(true);
        when(user.getUuid()).thenReturn(id);
        when(user.getChatrooms()).thenReturn(memberships);
        when(user.getListeningChatrooms()).thenReturn(listening);
        when(user.isMemberOf(any())).thenAnswer(call -> memberships.contains(call.getArgument(0)));
        when(user.isListeningTo(any())).thenAnswer(call -> listening.contains(call.getArgument(0)));

        bukkit = mockStatic(Bukkit.class);
        bukkit.when(() -> Bukkit.getPlayer(id)).thenReturn(player);
        BukkitScheduler scheduler = mock(BukkitScheduler.class);
        bukkit.when(Bukkit::getScheduler).thenReturn(scheduler);
        when(scheduler.runTask(eq(plugin), any(Runnable.class))).thenAnswer(call -> {
            tasks.add(call.getArgument(1));
            return null;
        });
        users = mockStatic(PlayerChannelUser.class);
        users.when(() -> PlayerChannelUser.getPlayer(id)).thenReturn(user);
        files = mockConstruction(ChannelFile.class, (file, context) -> {
            when(file.getConfiguration()).thenReturn(new YamlConfiguration());
            when(file.getString(anyString())).thenAnswer(call -> call.getArgument(0));
        });
        creation = new CreateChatroomInputEvent(plugin);
        descriptions = mock(ChatroomSetDescriptionEvent.class);
        statuses = mock(StatusInputEvent.class);
        nicknames = mock(ChatroomSetNicknameEvent.class);
        deletions = mock(ChatroomConfirmDeletionEvent.class);
        router = new ChatroomChatEvent(plugin, creation, statuses, nicknames, descriptions, deletions);
        doAnswer(call -> { router.refresh(user); return null; }).when(plugin).addPlayer(user);
        doAnswer(call -> { router.clearPendingInput(player); return null; }).when(plugin).clearPendingChatInput(player);
        doAnswer(call -> { router.forget(player); return null; }).when(plugin).forgetChatRouting(player);
        PlayerChannels.QUICK_CHAT_PREFIX = "@";
        router.refresh(user);
    }

    @After
    public void tearDown() {
        if (router != null) router.clear();
        if (files != null) files.close();
        if (users != null) users.close();
        if (bukkit != null) bukkit.close();
    }

    private AsyncPlayerChatEvent receive(String message) {
        AsyncPlayerChatEvent event = new AsyncPlayerChatEvent(true, player, message, new HashSet<>());
        router.onChatroomChat(event);
        return event;
    }

    private void runNextTask() {
        assertFalse("Expected a scheduled task", tasks.isEmpty());
        tasks.remove().run();
    }

    @Test
    public void focusedMessageKeepsItsOriginalAudienceAfterFocusChanges() {
        when(user.getFocusedChatroom()).thenReturn(staff);
        router.refresh(user);
        assertTrue(receive("private").isCancelled());
        verifyNoInteractions(staff, trade);
        when(user.getFocusedChatroom()).thenReturn(trade);
        router.refresh(user);
        runNextTask();
        verify(staff).chat("Sender", "private", id);
        verify(trade, never()).chat(anyString(), anyString(), any());
    }

    @Test
    public void indexedMessageKeepsItsOriginalChannelAfterMembershipOrderChanges() {
        receive("2: private");
        memberships.remove(staff);
        memberships.add(staff);
        router.refresh(user);
        runNextTask();
        verify(trade).chat("Sender", "private", id);
        verify(staff, never()).chat(anyString(), anyString(), any());
    }

    @Test
    public void quickChatKeepsItsChannelAndOriginalPrefix() {
        memberships.remove(trade);
        PlayerChannels.QUICK_CHAT_PREFIX = "@@";
        router.refresh(user);
        receive("@@private");
        memberships.add(trade);
        PlayerChannels.QUICK_CHAT_PREFIX = "!";
        router.refresh(user);
        runNextTask();
        verify(staff).chat("Sender", "private", id);
    }

    @Test
    public void removedMembershipRejectsTheCapturedChannel() {
        receive("1:private");
        memberships.remove(staff);
        router.refresh(user);
        runNextTask();
        assertExpired();
        verify(staff, never()).chat(anyString(), anyString(), any());
        verify(trade, never()).chat(anyString(), anyString(), any());
    }

    @Test
    public void deletedChannelCannotBeReplacedByAChannelAtTheSameIndex() {
        receive("1:private");
        registered.remove(staff);
        memberships.remove(staff);
        Chatroom replacement = mock(Chatroom.class);
        registered.add(replacement);
        memberships.add(0, replacement);
        router.refresh(user);
        runNextTask();
        assertExpired();
        verify(replacement, never()).chat(anyString(), anyString(), any());
        verify(staff, never()).chat(anyString(), anyString(), any());
    }

    @Test
    public void muteAppliedWhileQueuedPreventsDelivery() {
        receive("1:private");
        when(staff.isMuted(id)).thenReturn(true);
        runNextTask();
        verify(player).sendMessage("muted-message");
        verify(staff, never()).chat(anyString(), anyString(), any());
    }

    @Test
    public void channelMembershipRevocationBlocksDeliveryEvenIfTheUserCacheStillContainsIt() {
        receive("1:private");
        when(staff.isInChatroom(id)).thenReturn(false);
        runNextTask();
        assertExpired();
        verify(staff, never()).chat(anyString(), anyString(), any());
    }

    @Test
    public void validStatusNicknameAndDeletionPromptsReachTheirProcessors() {
        StatusInputEvent.enteringStatus.add(player);
        router.refresh(user);
        receive("status");
        runNextTask();
        verify(statuses).processInput(player, "status");

        router.clearPendingInput(player);
        ChatroomSetNicknameEvent.setNickname.put(player, new Pair<>(staff, id));
        router.refresh(user);
        receive("nickname");
        runNextTask();
        verify(nicknames).processInput(player, "nickname");

        router.clearPendingInput(player);
        when(staff.getRole(id)).thenReturn(ChatRole.OWNER);
        ChatroomConfirmDeletionEvent.confirmDeletion.put(player, staff);
        router.refresh(user);
        receive("Staff");
        runNextTask();
        verify(deletions).processInput(player, "Staff");
    }

    @Test
    public void invalidIndexStaysInvalidEvenIfPlayerJoinsAnotherChannel() {
        memberships.remove(trade);
        router.refresh(user);
        assertTrue(receive("2:hello").isCancelled());
        memberships.add(trade);
        router.refresh(user);
        runNextTask();
        verify(player).sendMessage("fast-chat-invalid");
        verify(trade, never()).chat(anyString(), anyString(), any());
    }

    @Test
    public void replacedPromptCannotConsumeAnOldAnswerEvenForTheSameFieldAndChannel() {
        beginDescription(staff);
        receive("old answer");
        router.clearPendingInput(player);
        beginDescription(staff);
        runNextTask();
        assertExpired();
        verifyNoInteractions(descriptions);
        assertSame(staff, ChatroomSetDescriptionEvent.setDescription.get(player));
    }

    @Test
    public void descriptionAnswerCannotBecomeAStatusAnswer() {
        beginDescription(staff);
        receive("old answer");
        router.clearPendingInput(player);
        StatusInputEvent.enteringStatus.add(player);
        router.refresh(user);
        runNextTask();
        assertExpired();
        verifyNoInteractions(descriptions, statuses);
    }

    @Test
    public void consumedPromptRejectsASecondQueuedAnswer() {
        beginDescription(staff);
        doAnswer(call -> { ChatroomSetDescriptionEvent.setDescription.remove(player); return null; })
                .when(descriptions).processInput(player, "first");
        receive("first");
        receive("second");
        runNextTask();
        runNextTask();
        verify(descriptions).processInput(player, "first");
        verify(descriptions, never()).processInput(player, "second");
        assertExpired();
    }

    @Test
    public void ordinaryRefreshKeepsTheSamePromptSession() {
        beginDescription(staff);
        when(user.getFocusedChatroom()).thenReturn(trade);
        router.refresh(user);
        receive("new description");
        router.refresh(user);
        runNextTask();
        verify(descriptions).processInput(player, "new description");
        verify(staff, never()).chat(anyString(), anyString(), any());
        verify(trade, never()).chat(anyString(), anyString(), any());
    }

    @Test
    public void revokedModeratorPermissionPreventsDescriptionEdit() {
        beginDescription(staff);
        receive("new description");
        when(staff.hasModeratorPermissions(id)).thenReturn(false);
        runNextTask();
        assertExpired();
        assertFalse(ChatroomSetDescriptionEvent.setDescription.containsKey(player));
        verifyNoInteractions(descriptions);
    }

    @Test
    public void nicknamePromptKeepsItsTargetAndRechecksPermission() {
        UUID target = UUID.randomUUID();
        when(staff.hasModeratorPermissions(id)).thenReturn(true);
        when(staff.isInChatroom(target)).thenReturn(true);
        ChatroomSetNicknameEvent.setNickname.put(player, new Pair<>(staff, target));
        router.refresh(user);
        receive("nickname");
        when(staff.hasModeratorPermissions(id)).thenReturn(false);
        runNextTask();
        assertExpired();
        verifyNoInteractions(nicknames);
    }

    @Test
    public void revokedOwnershipPreventsDeletion() {
        when(staff.getRole(id)).thenReturn(ChatRole.OWNER);
        ChatroomConfirmDeletionEvent.confirmDeletion.put(player, staff);
        router.refresh(user);
        receive("Staff");
        when(staff.getRole(id)).thenReturn(ChatRole.MEMBER);
        runNextTask();
        assertExpired();
        verifyNoInteractions(deletions);
    }

    @Test
    public void replacementCreationDraftCannotConsumeQueuedInput() {
        PreChatroom original = new PreChatroom(id);
        creation.getInCreation().put(id, original);
        creation.getSetName().add(id);
        router.refresh(user);
        receive("OldName");
        PreChatroom replacement = new PreChatroom(id);
        creation.getInCreation().put(id, replacement);
        router.refresh(user);
        runNextTask();
        assertExpired();
        assertEquals("", replacement.getName());
        assertEquals("", original.getName());
    }

    @Test
    public void disconnectBeforeDeliveryDropsTheTaskAndCleansPromptState() {
        beginDescription(staff);
        receive("answer");
        PlayerChannelUserJoinEvent lifecycle = new PlayerChannelUserJoinEvent(plugin);
        lifecycle.onQuit(new PlayerQuitEvent(player, "quit"));
        when(player.isOnline()).thenReturn(false);
        runNextTask();
        verifyNoInteractions(descriptions);
        assertFalse(ChatroomSetDescriptionEvent.setDescription.containsKey(player));
    }

    @Test
    public void reconnectRestoresNormalChatFilteringFromCachedListeningPreference() {
        listening.add(staff);
        router.refresh(user);
        PlayerChannelUserJoinEvent lifecycle = new PlayerChannelUserJoinEvent(plugin);
        lifecycle.onQuit(new PlayerQuitEvent(player, "quit"));
        assertFalse(listeningPlayers.contains(id));
        assertEquals(Arrays.asList(staff), listening);
        lifecycle.onJoin(new PlayerJoinEvent(player, "join"));
        assertTrue(listeningPlayers.contains(id));

        Player sender = mock(Player.class);
        when(sender.getUniqueId()).thenReturn(UUID.randomUUID());
        Set<Player> recipients = new HashSet<>(Arrays.asList(player));
        AsyncPlayerChatEvent normal = new AsyncPlayerChatEvent(true, sender, "public", recipients);
        router.onChatroomChat(normal);
        assertFalse(normal.isCancelled());
        assertFalse(recipients.contains(player));

        listening.clear();
        router.refresh(user);
        assertFalse(listeningPlayers.contains(id));
        recipients.add(player);
        router.onChatroomChat(normal);
        assertTrue(recipients.contains(player));
    }

    @Test
    public void allFourCreationMenuTogglesOpenOnTheNextTick() {
        PreChatroom draft = new PreChatroom(id);
        creation.getInCreation().put(id, draft);
        InventoryHelper helper = mock(InventoryHelper.class);
        ChestGui inventory = mock(ChestGui.class);
        Gui menu = mock(Gui.class);
        when(plugin.getHelper()).thenReturn(helper);
        when(helper.getCreationMenu(draft)).thenReturn(inventory);
        when(helper.setNavigationBar(inventory, player)).thenReturn(new Pair<>(menu, null));

        when(player.hasPermission(anyString())).thenReturn(true);
        List<Consumer<InventoryClickEvent>> actions = Arrays.asList(
                CreateChatroomAction.toggleStatusConsumer(),
                CreateChatroomAction.toggleGlobalStatus(),
                CreateChatroomAction.toggleSavedConsumer(),
                CreateChatroomAction.toggleIsServerChannel());
        int opened = 0;
        for (Consumer<InventoryClickEvent> action : actions) {
            InventoryClickEvent click = mock(InventoryClickEvent.class);
            when(click.getWhoClicked()).thenReturn(player);
            action.accept(click);
            verify(click).setCancelled(true);
            verify(menu, times(opened)).show(player);
            runNextTask();
            verify(menu, times(++opened)).show(player);
        }
    }

    @Test
    public void obsoleteCreationMenuDoesNotReopenAfterDraftIsReplaced() {
        PreChatroom draft = new PreChatroom(id);
        creation.getInCreation().put(id, draft);
        creation.showUpdatedMenu(player, draft);
        creation.getInCreation().put(id, new PreChatroom(id));
        runNextTask();
        verify(plugin, never()).getHelper();
    }

    private void beginDescription(Chatroom channel) {
        when(channel.hasModeratorPermissions(id)).thenReturn(true);
        ChatroomSetDescriptionEvent.setDescription.put(player, channel);
        router.refresh(user);
    }

    private void assertExpired() {
        verify(player).sendMessage(contains("Please send your message again"));
    }
}
