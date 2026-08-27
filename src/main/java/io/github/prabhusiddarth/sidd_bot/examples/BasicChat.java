package io.github.prabhusiddarth.sidd_bot.examples;

import io.github.prabhusiddarth.sidd_bot.SiddBot;

public class BasicChat {
    public static void main(String[] args) {
        SiddBot bot = new SiddBot();
        String response = bot.chat("What is the capital of France?");
        System.out.println("Bot Response: " + response);
    }
}
