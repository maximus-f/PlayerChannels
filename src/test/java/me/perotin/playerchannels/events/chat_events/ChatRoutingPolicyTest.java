package me.perotin.playerchannels.events.chat_events;

import org.junit.Test;

import static me.perotin.playerchannels.events.chat_events.ChatRoutingPolicy.Route.*;
import static org.junit.Assert.assertEquals;

public class ChatRoutingPolicyTest {

    @Test
    public void promptClaimsMessagesBeforeFocusedAndFastChat() {
        assertEquals(PROMPT, ChatRoutingPolicy.route(true, true, 1, "!", "!hello"));
        assertEquals(PROMPT, ChatRoutingPolicy.route(true, false, 2, "!", "1:hello"));
    }

    @Test
    public void focusClaimsEveryMessageWhenNoPromptIsActive() {
        assertEquals(FOCUSED, ChatRoutingPolicy.route(false, true, 1, "!", "!hello"));
        assertEquals(FOCUSED, ChatRoutingPolicy.route(false, true, 2, "!", "1:hello"));
    }

    @Test
    public void quickChatRequiresExactlyOneChannel() {
        assertEquals(QUICK, ChatRoutingPolicy.route(false, false, 1, "!", "!hello"));
        assertEquals(NORMAL, ChatRoutingPolicy.route(false, false, 2, "!", "!hello"));
        assertEquals(NORMAL, ChatRoutingPolicy.route(false, false, 1, "", "hello"));
    }

    @Test
    public void indexedMessagesAreClaimedEvenForInvalidChannelNumbers() {
        assertEquals(INDEXED, ChatRoutingPolicy.route(false, false, 0, "!", "1:hello"));
        assertEquals(INDEXED, ChatRoutingPolicy.route(false, false, 2, "!", "99:hello"));
        assertEquals(NORMAL, ChatRoutingPolicy.route(false, false, 2, "!", "100:hello"));
    }
}
