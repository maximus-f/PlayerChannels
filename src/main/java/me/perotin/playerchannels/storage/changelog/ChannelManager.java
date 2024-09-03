package me.perotin.playerchannels.storage.changelog;

import me.perotin.playerchannels.storage.mysql.SQLHandler;
import org.bukkit.Bukkit;

import java.util.List;

/**
 *  Helper class to facilitate change log for persistent channels on store.
 */
public class ChannelManager {


    private final ChangeLog changeLog = new ChangeLog();
    private final SQLHandler sqlHandler;
    public ChannelManager(SQLHandler handler) {
        this.sqlHandler = handler;
    }

    public void addChannel(String channelName) {

        changeLog.logChange(ChangeType.ADD_CHANNEL, channelName, null);
    }

    public void removeChannel(String channelName) {

        changeLog.logChange(ChangeType.REMOVE_CHANNEL, channelName, null);
    }

    public void addMemberToChannel(String channelName, String memberUUID) {

        changeLog.logChange(ChangeType.ADD_MEMBER, channelName, memberUUID);
    }

    public void removeMemberFromChannel(String channelName, String memberUUID) {

        changeLog.logChange(ChangeType.REMOVE_MEMBER, channelName, memberUUID);
    }

    public void onDisable() {
        persistChangesToDatabase(changeLog.getChanges());

        changeLog.clear();
    }

    /**
     * @param changes
     * TODO make this cancel out corresponding actions, e.g. (leave/join same person)
     */
    private void persistChangesToDatabase(List<ChannelChange> changes) {
        for (ChannelChange change : changes) {
            Bukkit.getConsoleSender().sendMessage("[PlayerChannels] " + change.getChannelName() + " -> " + change.getMemberUUID() + " for action " + change.getChangeType().toString());
            switch (change.getChangeType()) {
                case ADD_CHANNEL:

                    break;
                case REMOVE_CHANNEL:

                    break;
                case ADD_MEMBER:
                    sqlHandler.updateMemberInDatabase(change.getChannelName(), change.getMemberUUID(), 1, SQLHandler.OperationType.ADD);
                    break;
                case REMOVE_MEMBER:
                    sqlHandler.updateMemberInDatabase(change.getChannelName(), change.getMemberUUID(), 1, SQLHandler.OperationType.REMOVE);
                    break;
            }
        }
    }
}
