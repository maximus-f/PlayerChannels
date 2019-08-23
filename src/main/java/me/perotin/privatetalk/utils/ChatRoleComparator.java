package me.perotin.privatetalk.utils;

import me.perotin.privatetalk.objects.ChatRole;

import java.util.Comparator;

/* Created by Perotin on 8/22/19 */

/**
 * Compare the Enum ranks of chat roles
 */
public class ChatRoleComparator implements Comparator<ChatRole> {

    @Override
    public int compare(ChatRole o1, ChatRole o2) {
       if(o1.getValue() == o2.getValue()) return 0;
       if(o1.getValue() > o2.getValue()) return 1;
       else return -1;
    }
}
