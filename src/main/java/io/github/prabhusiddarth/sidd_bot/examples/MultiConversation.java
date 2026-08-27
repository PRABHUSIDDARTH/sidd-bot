package io.github.prabhusiddarth.sidd_bot.examples;

import io.github.prabhusiddarth.sidd_bot.SiddBot;

public class MultiConversation {
    public static void main(String[] args) {
        SiddBot bot = new SiddBot();

        String session1 = "user-session-101";
        String session2 = "user-session-102";

        System.out.println("Session 1: " + bot.chat(session1, "My name is Alice."));
        System.out.println("Session 2: " + bot.chat(session2, "My name is Bob."));

        System.out.println("Session 1 Conv History Count: " +
                bot.getConversationManager().getConversation(session1).get().getMessages().size());
        System.out.println("Session 2 Conv History Count: " +
                bot.getConversationManager().getConversation(session2).get().getMessages().size());
    }
}
