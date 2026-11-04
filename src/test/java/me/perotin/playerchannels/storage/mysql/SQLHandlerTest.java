package me.perotin.playerchannels.storage.mysql;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import me.perotin.playerchannels.PlayerChannels;
import me.perotin.playerchannels.events.chat_events.*;
import me.perotin.playerchannels.objects.ChatRole;
import me.perotin.playerchannels.objects.Chatroom;
import me.perotin.playerchannels.objects.GlobalChatroom;
import me.perotin.playerchannels.objects.PlayerChannelUser;
import me.perotin.playerchannels.proxy.BungeeMessageHandler;
import me.perotin.playerchannels.storage.Pair;
import me.perotin.playerchannels.storage.changelog.ChannelManager;
import me.perotin.playerchannels.storage.files.ChannelFile;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitScheduler;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.MockedConstruction;
import org.mockito.MockedStatic;

import java.sql.*;
import java.util.*;
import java.util.function.Consumer;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/** Uses real storage, channel and routing code with JDBC rows and Bukkit services supplied by mocks. */
public class SQLHandlerTest {
    private final UUID owner = UUID.randomUUID();
    private final UUID member = UUID.randomUUID();
    private final UUID moderator = UUID.randomUUID();
    private final Map<UUID, Integer> storedMembers = new LinkedHashMap<>();
    private final List<Chatroom> registered = new ArrayList<>();
    private UUID storedOwner = owner;
    private boolean serverOwned;
    private Connection connection;
    private PlayerChannels plugin;
    private SQLHandler storage;
    private ChannelManager changes;
    private MockedStatic<PlayerChannels> singleton;
    private MockedStatic<Bukkit> bukkit;
    private MockedStatic<DriverManager> drivers;
    private MockedConstruction<ChannelFile> files;
    private MockedConstruction<ItemStack> items;

    @Before
    public void setUp() throws Exception {
        plugin = mock(PlayerChannels.class);
        singleton = mockStatic(PlayerChannels.class);
        singleton.when(PlayerChannels::getInstance).thenReturn(plugin);
        when(plugin.getName()).thenReturn("PlayerChannels");
        when(plugin.isEnabled()).thenReturn(true);
        when(plugin.isMySQL()).thenReturn(true);
        when(plugin.getChatrooms()).thenReturn(registered);
        when(plugin.getChatroom("Staff")).thenAnswer(call -> registered.isEmpty() ? null : registered.get(0));
        when(plugin.getListeningPlayers()).thenReturn(new HashSet<>());
        YamlConfiguration config = new YamlConfiguration();
        config.set("saved-material", "STONE");
        config.set("server-channel-material", "STONE");
        when(plugin.getConfig()).thenReturn(config);

        bukkit = mockStatic(Bukkit.class);
        bukkit.when(Bukkit::getConsoleSender).thenReturn(mock(ConsoleCommandSender.class));
        OfflinePlayer offline = mock(OfflinePlayer.class);
        when(offline.getName()).thenReturn("Player");
        bukkit.when(() -> Bukkit.getOfflinePlayer(any(UUID.class))).thenReturn(offline);
        files = mockConstruction(ChannelFile.class, (file, context) -> {
            when(file.getConfiguration()).thenReturn(new YamlConfiguration());
            when(file.getString(anyString())).thenAnswer(call -> call.getArgument(0));
        });
        items = mockConstruction(ItemStack.class, (item, context) ->
                when(item.getItemMeta()).thenReturn(mock(ItemMeta.class, RETURNS_DEEP_STUBS)));

        connection = mock(Connection.class);
        Statement query = mock(Statement.class);
        when(connection.createStatement()).thenReturn(query);
        when(query.executeQuery("SELECT * FROM chatrooms")).thenAnswer(call -> channelRow());
        when(connection.prepareStatement(anyString())).thenAnswer(call -> statement(call.getArgument(0)));
        drivers = mockStatic(DriverManager.class);
        drivers.when(() -> DriverManager.getConnection(anyString(), anyString(), anyString())).thenReturn(connection);
        storage = new SQLHandler("localhost", "channels", "user", "password", 3306);
        changes = new ChannelManager(storage, plugin);
        when(plugin.getChannelManager()).thenReturn(changes);

        storedMembers.put(owner, ChatRole.OWNER.getValue());
        storedMembers.put(member, ChatRole.MEMBER.getValue());
        storedMembers.put(moderator, ChatRole.MODERATOR.getValue());
    }

    @After
    public void tearDown() {
        if (items != null) items.close();
        if (files != null) files.close();
        if (drivers != null) drivers.close();
        if (bukkit != null) bukkit.close();
        if (singleton != null) singleton.close();
    }

    @Test
    public void restoresSavedMembersAndRolesWithoutLoggingMutations() throws Exception {
        Chatroom channel = reload();
        assertEquals(ChatRole.OWNER, channel.getRole(owner));
        assertEquals(ChatRole.MEMBER, channel.getRole(member));
        assertEquals(ChatRole.MODERATOR, channel.getRole(moderator));
        assertEquals(3, channel.getMembers().size());
        assertTrue(changes.isEmpty());
    }

    @Test
    public void explicitOwnerSurvivesMissingMemberRowAndStaleOwnerRanks() throws Exception {
        storedMembers.remove(owner);
        storedMembers.put(member, ChatRole.OWNER.getValue());
        Chatroom channel = reload();
        assertEquals(ChatRole.OWNER, channel.getRole(owner));
        assertEquals(ChatRole.MODERATOR, channel.getRole(member));
    }

    @Test
    public void legacyZeroRankLoadsAsMemberAndOneKeepsLeastPrivilege() throws Exception {
        storedMembers.put(member, 0);
        storedMembers.put(moderator, 1);
        Chatroom channel = reload();
        assertEquals(ChatRole.MEMBER, channel.getRole(member));
        assertEquals(ChatRole.MEMBER, channel.getRole(moderator));
    }

    @Test
    public void unknownRankPreventsPublication() {
        storedMembers.put(member, 99);
        SQLException failure = assertThrows(SQLException.class, () -> reload());
        assertTrue(failure.getMessage().contains("Unknown member rank 99"));
        assertTrue(registered.isEmpty());
    }

    @Test
    public void missingMembershipTablePreventsPublication() throws Exception {
        SQLException missingTable = new SQLException("Missing membership table", "42S02");
        when(connection.prepareStatement(startsWith("SELECT memberUUID")))
                .thenThrow(missingTable);
        assertThrows(SQLException.class, () -> reload());
        assertTrue(registered.isEmpty());
    }

    @Test
    public void freshDatabaseWithNoChannelTableLoadsEmpty() throws Exception {
        SQLException missingTable = new SQLException("Missing channel table", "42S02");
        when(connection.createStatement()).thenThrow(missingTable);
        assertTrue(storage.getAllChatrooms().isEmpty());
    }

    @Test
    public void failedChannelReadDoesNotReturnPartialResults() throws Exception {
        ResultSet rows = channelRow();
        when(rows.next()).thenReturn(true).thenThrow(new SQLException("Connection lost"));
        Statement query = connection.createStatement();
        when(query.executeQuery("SELECT * FROM chatrooms")).thenReturn(rows);
        assertThrows(SQLException.class, () -> reload());
        assertTrue(registered.isEmpty());
    }

    @Test
    public void joinPromotionDemotionAndRemovalSurviveReload() throws Exception {
        UUID joined = UUID.randomUUID();
        GlobalChatroom channel = reload();
        channel.addMember(new Pair<>(joined, ChatRole.MEMBER), "Joined");
        persist();
        channel = reload();
        assertEquals(ChatRole.MEMBER, channel.getRole(joined));

        channel.promoteMemberToModerator(joined);
        persist();
        channel = reload();
        assertEquals(ChatRole.MODERATOR, channel.getRole(joined));

        channel.demoteModeratorToMember(joined);
        persist();
        channel = reload();
        assertEquals(ChatRole.MEMBER, channel.getRole(joined));

        channel.removeMember(joined);
        persist();
        assertFalse(reload().isInChatroom(joined));
    }

    @Test
    public void ownershipTransferPersistsNewOwnerAndFormerOwnerRole() throws Exception {
        reload().promoteModeratorToOwner(moderator, owner);
        persist();
        Chatroom channel = reload();
        assertEquals(moderator, channel.getOwner());
        assertEquals(ChatRole.OWNER, channel.getRole(moderator));
        assertEquals(ChatRole.MODERATOR, channel.getRole(owner));
    }

    @Test
    public void serverOwnedChannelsPreserveDirectlyAddedAdminRoles() throws Exception {
        serverOwned = true;
        UUID admin = UUID.randomUUID();
        reload().addMember(new Pair<>(admin, ChatRole.OWNER), "Admin");
        persist();
        Chatroom channel = reload();
        assertEquals(ChatRole.OWNER, channel.getRole(owner));
        assertEquals(ChatRole.OWNER, channel.getRole(admin));
    }

    @Test
    public void remoteAdditionsKeepTheirTransmittedRolesAfterReload() throws Exception {
        serverOwned = true;
        GlobalChatroom channel = reload();
        try (MockedStatic<PlayerChannelUser> users = mockStatic(PlayerChannelUser.class)) {
            for (ChatRole role : ChatRole.values()) {
                UUID joined = UUID.randomUUID();
                PlayerChannelUser user = new PlayerChannelUser(joined, "Remote");
                users.when(() -> PlayerChannelUser.getPlayer(joined)).thenReturn(user);
                receiveRemoteChange("AddMember", payload -> {
                    payload.writeUTF(joined.toString());
                    payload.writeInt(role.getValue());
                    payload.writeUTF("Remote");
                });
                assertTrue(user.isMemberOf(channel));
                persist();
                channel = reload();
                assertEquals(role, channel.getRole(joined));
            }
        }
    }

    @Test
    public void remotePromotionAndDemotionPersistCanonicalRanks() throws Exception {
        reload();
        receiveRemoteChange("PromoteToMod", payload -> payload.writeUTF(member.toString()));
        persist();
        assertEquals(ChatRole.MODERATOR, reload().getRole(member));
        assertEquals(Integer.valueOf(ChatRole.MODERATOR.getValue()), storedMembers.get(member));

        receiveRemoteChange("DemoteToMember", payload -> payload.writeUTF(member.toString()));
        persist();
        assertEquals(ChatRole.MEMBER, reload().getRole(member));
        assertEquals(Integer.valueOf(ChatRole.MEMBER.getValue()), storedMembers.get(member));
    }

    @Test
    public void remoteOwnershipTransferPersistsBothRolesAndOwnerField() throws Exception {
        reload();
        receiveRemoteChange("PromoteModToOwner", payload -> {
            payload.writeUTF(moderator.toString());
            payload.writeUTF(owner.toString());
        });
        persist();
        Chatroom channel = reload();
        assertEquals(moderator, channel.getOwner());
        assertEquals(ChatRole.OWNER, channel.getRole(moderator));
        assertEquals(ChatRole.MODERATOR, channel.getRole(owner));
        assertEquals(Integer.valueOf(ChatRole.OWNER.getValue()), storedMembers.get(moderator));
        assertEquals(Integer.valueOf(ChatRole.MODERATOR.getValue()), storedMembers.get(owner));
    }

    @Test
    public void remoteRemovalSurvivesReload() throws Exception {
        Chatroom channel = reload();
        PlayerChannelUser user = new PlayerChannelUser(member, "Remote", "",
                new ArrayList<>(Collections.singletonList(channel)));
        try (MockedStatic<PlayerChannelUser> users = mockStatic(PlayerChannelUser.class)) {
            users.when(() -> PlayerChannelUser.getPlayer(member)).thenReturn(user);
            receiveRemoteChange("Remove", payload -> payload.writeUTF(member.toString()));
            assertFalse(user.isMemberOf(channel));
            persist();
            assertFalse(reload().isInChatroom(member));
        }
    }

    @Test
    public void restoredMemberCanSendButRevocationWhileQueuedBlocksDelivery() throws Exception {
        GlobalChatroom channel = spy(reload());
        registered.set(0, channel);
        doReturn("sent").when(channel).chat(anyString(), anyString(), any());
        Player player = mock(Player.class);
        when(player.getUniqueId()).thenReturn(member);
        when(player.getName()).thenReturn("Sender");
        when(player.isOnline()).thenReturn(true);
        bukkit.when(() -> Bukkit.getPlayer(member)).thenReturn(player);
        PlayerChannelUser user = new PlayerChannelUser(member, "Sender", "", Collections.singletonList(channel));
        Queue<Runnable> tasks = new ArrayDeque<>();
        BukkitScheduler scheduler = mock(BukkitScheduler.class);
        bukkit.when(Bukkit::getScheduler).thenReturn(scheduler);
        when(scheduler.runTask(eq(plugin), any(Runnable.class))).thenAnswer(call -> {
            tasks.add(call.getArgument(1));
            return null;
        });
        ChatroomChatEvent router = new ChatroomChatEvent(plugin, new CreateChatroomInputEvent(plugin),
                mock(StatusInputEvent.class), mock(ChatroomSetNicknameEvent.class),
                mock(ChatroomSetDescriptionEvent.class), mock(ChatroomConfirmDeletionEvent.class));
        try (MockedStatic<PlayerChannelUser> users = mockStatic(PlayerChannelUser.class)) {
            users.when(() -> PlayerChannelUser.getPlayer(member)).thenReturn(user);
            router.refresh(user);
            AsyncPlayerChatEvent first = new AsyncPlayerChatEvent(true, player, "1:hello", new HashSet<>());
            router.onChatroomChat(first);
            assertTrue(first.isCancelled());
            assertEquals(1, tasks.size());
            tasks.remove().run();
            verify(channel).chat("Sender", "hello", member);

            AsyncPlayerChatEvent second = new AsyncPlayerChatEvent(true, player, "1:revoked", new HashSet<>());
            router.onChatroomChat(second);
            channel.removeMember(member);
            assertTrue(user.isMemberOf(channel));
            tasks.remove().run();
            verify(channel, never()).chat("Sender", "revoked", member);
            verify(player).sendMessage(contains("Please send your message again"));
        } finally {
            router.clear();
        }
    }

    private GlobalChatroom reload() throws SQLException {
        List<Chatroom> loaded = storage.getAllChatrooms();
        registered.clear();
        registered.addAll(loaded);
        return (GlobalChatroom) loaded.get(0);
    }

    private void persist() {
        changes.persistChangesToDatabase();
        changes.clear();
    }

    private void receiveRemoteChange(String action, Consumer<ByteArrayDataOutput> writeFields) {
        ByteArrayDataOutput payload = ByteStreams.newDataOutput();
        payload.writeUTF("Staff");
        writeFields.accept(payload);
        byte[] bytes = payload.toByteArray();
        ByteArrayDataOutput packet = ByteStreams.newDataOutput();
        packet.writeUTF(action);
        packet.writeShort(bytes.length);
        packet.write(bytes);
        new BungeeMessageHandler(plugin).handlePluginMessage("BungeeCord", mock(Player.class), packet.toByteArray());
    }

    private ResultSet channelRow() throws SQLException {
        ResultSet rows = mock(ResultSet.class);
        when(rows.next()).thenReturn(true, false);
        when(rows.getString("name")).thenReturn("Staff");
        when(rows.getString("description")).thenReturn("");
        when(rows.getString("owner")).thenReturn(storedOwner.toString());
        when(rows.getBoolean("isServerOwned")).thenReturn(serverOwned);
        return rows;
    }

    private PreparedStatement statement(String sql) throws SQLException {
        PreparedStatement statement = mock(PreparedStatement.class);
        Map<Integer, Object> parameters = new HashMap<>();
        doAnswer(call -> { parameters.put(call.getArgument(0), call.getArgument(1)); return null; })
                .when(statement).setString(anyInt(), anyString());
        doAnswer(call -> { parameters.put(call.getArgument(0), call.getArgument(1)); return null; })
                .when(statement).setInt(anyInt(), anyInt());
        when(statement.executeQuery()).thenAnswer(call -> {
            assertEquals("SELECT memberUUID, `rank` FROM members WHERE chatroomName = ?", sql);
            assertEquals("Staff", parameters.get(1));
            ResultSet rows = mock(ResultSet.class);
            Iterator<Map.Entry<UUID, Integer>> iterator = storedMembers.entrySet().iterator();
            when(rows.next()).thenAnswer(next -> iterator.hasNext());
            // JDBC reads the UUID before the rank, so retain that row's UUID for the rank lookup.
            final UUID[] current = new UUID[1];
            when(rows.getString("memberUUID")).thenAnswer(uuid -> {
                current[0] = iterator.next().getKey();
                return current[0].toString();
            });
            when(rows.getInt("rank")).thenAnswer(rank -> storedMembers.get(current[0]));
            return rows;
        });
        when(statement.executeUpdate()).thenAnswer(call -> {
            if (sql.startsWith("REPLACE INTO members")) {
                storedMembers.put(UUID.fromString((String) parameters.get(2)), (Integer) parameters.get(3));
            } else if (sql.startsWith("DELETE FROM members")) {
                storedMembers.remove(UUID.fromString((String) parameters.get(2)));
            } else if (sql.startsWith("UPDATE members")) {
                storedMembers.replace(UUID.fromString((String) parameters.get(2)), (Integer) parameters.get(1));
            } else if (sql.startsWith("UPDATE `chatrooms`")) {
                storedOwner = UUID.fromString((String) parameters.get(2));
            } else {
                assertTrue("Unexpected SQL " + sql, sql.startsWith("CREATE TABLE IF NOT EXISTS members"));
            }
            return 1;
        });
        return statement;
    }
}
