package me.perotin.playerchannels.events.chat_events;

/** Decides whether a chat message belongs to PlayerChannels without touching Bukkit state. */
public final class ChatRoutingPolicy {

    public enum Route {
        NORMAL, PROMPT, FOCUSED, QUICK, INDEXED
    }

    private ChatRoutingPolicy() {
    }

    public static Route route(boolean prompt, boolean focused, int channelCount, String prefix, String message) {
        if (prompt) return Route.PROMPT;
        if (focused) return Route.FOCUSED;
        if (channelCount == 1 && prefix != null && !prefix.isEmpty() && message.startsWith(prefix)) {
            return Route.QUICK;
        }
        if (message.matches("^[1-9][0-9]?:.*$")) return Route.INDEXED;
        return Route.NORMAL;
    }
}
