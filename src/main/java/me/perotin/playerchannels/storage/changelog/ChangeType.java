package me.perotin.playerchannels.storage.changelog;

/**
 * @Enum to represent types of actions that require updates or deletions
 *  to database that require tracking and can potentially cancel
 *  corresponding actions (e.g. leave but then rejoin, hence no data change)
 */
public enum ChangeType {

    ADD_CHANNEL,
    REMOVE_CHANNEL,
    ADD_MEMBER,
    REMOVE_MEMBER,
    RANK_CHANGE,
    FIELD_CHANGE;


}
