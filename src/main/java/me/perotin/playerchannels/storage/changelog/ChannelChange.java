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

    /**
     * Retrieves the type of change associated with this ChannelChange instance.
     * @return the ChangeType representing the type of change (e.g., ADD_MEMBER, REMOVE_MEMBER, etc.).
     */
    public ChangeType getChangeType() {
        return changeType;
    }

    /**
     * Retrieves the name of the channel associated with this change.
     * @return the name of the channel as a string
     */
    public String getChannelName() {
        return channelName;
    }

    /**
     * Retrieves the UUID of the member associated with this channel change.
     * @return the UUID of the member as a String, or null if the member change is not relevant.
     */
    public String getMemberUUID() {
        return memberUUID;
    }

    /**
     * Retrieves the rank associated with this ChannelChange.
     * @return the rank value, or -1 if the rank is not specified.
     */
    public int getRank() {
        return rank;
    }

    /**
     * Retrieves the timestamp indicating when the change was created.
     * @return the timestamp of the change as a long value, representing the time in milliseconds
     *         since the epoch (January 1, 1970, 00:00:00 GMT).
     */
    public long getTimestamp() {
        return timestamp;
    }

    /**
     * Provides a comparator for sorting instances of {@code ChannelChange} by their timestamp in ascending order.
     * @return A comparator that compares {@code ChannelChange} objects based on their timestamp.
     */
    public static Comparator<ChannelChange> sortByTimestamp() {
        return Comparator.comparingLong(ChannelChange::getTimestamp);
    }

    /**
     * Merges two lists of {@code ChannelChange} objects from two servers into a single list.
     * The merged list is sorted by the timestamp of the {@code ChannelChange} objects in ascending order.
     *
     * @param server1Changes the list of {@code ChannelChange} objects from the first server
     * @param server2Changes the list of {@code ChannelChange} objects from the second server
     * @return a merged and sorted list of {@code ChannelChange} objects from both servers
     */
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
