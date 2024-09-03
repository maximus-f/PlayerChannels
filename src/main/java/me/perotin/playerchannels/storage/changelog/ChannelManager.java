package me.perotin.playerchannels.storage.changelog;

import java.util.List;

/**
 *  Helper class to facilitate change log for persistent channels on store.
 */
public class ChannelManager {


    private final ChangeLog changeLog = new ChangeLog();

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
            switch (change.getChangeType()) {
                case ADD_CHANNEL:

                    break;
                case REMOVE_CHANNEL:

                    break;
                case ADD_MEMBER:

                    break;
                case REMOVE_MEMBER:

                    break;
            }
        }
    }
}
