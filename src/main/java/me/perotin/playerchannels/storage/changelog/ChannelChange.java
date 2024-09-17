package me.perotin.playerchannels.storage.changelog;


import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 *  Class used for batch-processing for saved global channels that requires updating at
 *  end of a session (e.g. tracking joins and leaves)
 */

public class ChannelChange {
    private final ChangeType changeType;
    private final String channelName;
    private final String memberUUID; // Nullable, only relevant for member changes
    private int rank = -1;
    private final long timestamp;  // Time when the change was created


    public ChannelChange(ChangeType changeType, String channelName, String memberUUID) {
        this.changeType = changeType;
        this.channelName = channelName;
        this.memberUUID = memberUUID;
        this.timestamp = System.currentTimeMillis(); // Capture the current time

    }

    public ChannelChange(ChangeType changeType, String channelName, String memberUUID, int rank) {
        this.changeType = changeType;
        this.channelName = channelName;
        this.memberUUID = memberUUID;
        this.rank = rank;
        this.timestamp = System.currentTimeMillis(); // Capture the current time

    }

    public ChangeType getChangeType() {
        return changeType;
    }

    public String getChannelName() {
        return channelName;
    }

    public String getMemberUUID() {
        return memberUUID;
    }

    public int getRank() {
        return rank;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public static Comparator<ChannelChange> sortByTimestamp() {
        return Comparator.comparingLong(ChannelChange::getTimestamp);
    }

    public static List<ChannelChange> mergeChangeLogs(List<ChannelChange> server1Changes, List<ChannelChange> server2Changes) {
        // Create a new list containing changes from both servers
        List<ChannelChange> mergedChanges = new ArrayList<>();
        mergedChanges.addAll(server1Changes);
        mergedChanges.addAll(server2Changes);

        // Sort the merged list by timestamp
        Collections.sort(mergedChanges, ChannelChange.sortByTimestamp());

        return mergedChanges;
    }
}
