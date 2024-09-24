package me.perotin.playerchannels.storage.changelog;

import me.perotin.playerchannels.PlayerChannels;
import me.perotin.playerchannels.objects.Chatroom;
import me.perotin.playerchannels.objects.GlobalChatroom;
import me.perotin.playerchannels.storage.mysql.SQLHandler;
import org.bukkit.Bukkit;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 *  Helper class to facilitate change log for persistent channels on store.
 */
public class ChannelManager {


    private final ChangeLog changeLog = new ChangeLog();
    private final SQLHandler sqlHandler;
    private final int THROTTLE_LIMIT;
    public ChannelManager(SQLHandler handler, PlayerChannels plugin) {
        this.sqlHandler = handler;
        this.THROTTLE_LIMIT = plugin.getConfig().getInt("throttle-in-sec");
    }


    public void addMemberToChannel(String channelName, String memberUUID) {

        Bukkit.getConsoleSender().sendMessage("[PlayerChannels] [ChangeLog] Add " + memberUUID + " to channel " + channelName);
        changeLog.logChange(ChangeType.ADD_MEMBER, channelName, memberUUID);
    }

    public void removeMemberFromChannel(String channelName, String memberUUID) {
        Bukkit.getConsoleSender().sendMessage("[PlayerChannels] [ChangeLog] Remove " + memberUUID + " to channel " + channelName);

        changeLog.logChange(ChangeType.REMOVE_MEMBER, channelName, memberUUID);
    }

    public void changeFieldStatus(String channelName) {
        Bukkit.getConsoleSender().sendMessage("[PlayerChannels] [ChangeLog] Change field status for " + channelName);
        changeLog.logChange(ChangeType.FIELD_CHANGE,  channelName, null);

    }

    public void rankChange(String channelName, String uuid, int newRank) {
        Bukkit.getConsoleSender().sendMessage("[PlayerChannels] [ChangeLog] Rank change for " + uuid + " to " + newRank);
        changeLog.logChange(ChangeType.RANK_CHANGE, channelName, uuid, newRank);
    }

    public void onDisable() {
        GlobalChatroom.sendChannelManagerClear();
        persistChangesToDatabase();
        changeLog.clear();
    }

    public void clear() {
        changeLog.clear();
    }

    public boolean isEmpty() {
        return changeLog.getChanges().isEmpty();
    }

    /**
     * @return throttle limit
     */
    public int THROTTLE_LIMIT() {
        return THROTTLE_LIMIT;
    }

    /**
     * @param changes
     * TODO make this use batching to group similiar updates together
     *
     */
    public void persistChangesToDatabase() {
        Set<String> batchedStatusChanges = new HashSet<>();
        for (ChannelChange change : changeLog.getChanges()) {
            Bukkit.getConsoleSender().sendMessage("[PlayerChannels] " + change.getChannelName() + " -> " + change.getMemberUUID() + " for action " + change.getChangeType().toString());

            ChangeType type = change.getChangeType();
            // Skip already processed status changes, i.e. one status change will update correctly for all potential types
            if (type == ChangeType.FIELD_CHANGE && batchedStatusChanges.contains(change.getChannelName())) continue;
            Chatroom channel = PlayerChannels.getInstance().getChatroom(change.getChannelName());

            if (channel == null) continue;
            if (!PlayerChannels.getInstance().isMySQL()) return;
            switch (change.getChangeType()) {
                case ADD_MEMBER:
                    sqlHandler.updateMemberInDatabase(change.getChannelName(), change.getMemberUUID(), 1, SQLHandler.OperationType.ADD);
                    break;
                case REMOVE_MEMBER:
                    sqlHandler.updateMemberInDatabase(change.getChannelName(), change.getMemberUUID(), 1, SQLHandler.OperationType.REMOVE);
                    break;
                case RANK_CHANGE:
                    sqlHandler.updateMemberInDatabase(change.getChannelName(), change.getMemberUUID(), change.getRank(), SQLHandler.OperationType.RANK_CHANGE);
                    break;
                case FIELD_CHANGE:
                    sqlHandler.updateChannelFields(channel);
                    batchedStatusChanges.add(change.getChannelName());
                    break;
            }
        }
    }
}
