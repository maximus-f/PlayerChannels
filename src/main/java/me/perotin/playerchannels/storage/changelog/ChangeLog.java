package me.perotin.playerchannels.storage.changelog;

import java.util.ArrayList;
import java.util.List;

/**
 *  Class to keep track of ChannelChanges to maintain atomicity
 */
public class ChangeLog {
    private final List<ChannelChange> changes = new ArrayList<>();

    public void logChange(ChangeType type, String channelName, String memberUUID) {
        changes.add(new ChannelChange(type, channelName, memberUUID));
    }

    public List<ChannelChange> getChanges() {
        return new ArrayList<>(changes);
    }

    public void clear() {
        changes.clear();
    }
}
