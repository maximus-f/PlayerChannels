package me.perotin.playerchannels.storage.changelog;


/**
 *  Class used for batch-processing for saved global channels that requires updating at
 *  end of a session (e.g. tracking joins and leaves)
 */

public class ChannelChange {
    private final ChangeType changeType;
    private final String channelName;
    private final String memberUUID; // Nullable, only relevant for member changes
    private int rank = -1;

    public ChannelChange(ChangeType changeType, String channelName, String memberUUID) {
        this.changeType = changeType;
        this.channelName = channelName;
        this.memberUUID = memberUUID;
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
}
