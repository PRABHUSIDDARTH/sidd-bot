package io.github.prabhusiddarth.sidd_bot.examples;

import io.github.prabhusiddarth.sidd_bot.SiddBot;
import io.github.prabhusiddarth.sidd_bot.memory.InMemoryConversationStore;
import io.github.prabhusiddarth.sidd_bot.memory.StorageType;
import io.github.prabhusiddarth.sidd_bot.model.Model;
import io.github.prabhusiddarth.sidd_bot.model.Provider;

public class RuntimeModelSwitch {
    @SuppressWarnings("unused")
    public static void main(String[] args) {

        // ── Recommended: builder with explicit storage ────────────────────────
        SiddBot bot = SiddBot.builder()
                .storage(new InMemoryConversationStore())   // swap for any ConversationStore impl
                .defaultModel("gemini-3.6-flash")
                .build();

        // Shortcut: built-in StorageType enum
        SiddBot sqliteBot = SiddBot.builder()
                .storageType(StorageType.SQLITE, "sidd-bot.db")  // persistent local SQLite
                .defaultModel("gemini-3.6-flash")
                .build();

        // Shortcut: in-memory via StorageType
        SiddBot inMemBot = SiddBot.builder()
                .storageType(StorageType.IN_MEMORY)
                .defaultModel("gemini-3.6-flash")
                .build();

        // ── Runtime model switching still works as before ─────────────────────
        System.out.println("Initial Active Model: " + bot.getModelManager().getActiveModel());

        Model claude = new Model("claude-3-5-sonnet", Provider.ANTHROPIC);
        bot.getModelManager().registerModel("claude", claude);
        bot.getModelManager().setActiveModel("claude");

        System.out.println("Switched to: " + bot.getModelManager().getActiveModel());

        // chat() now calls sidd-ai under the hood
        String response = bot.chat("Explain quantum computing in one sentence.");
        System.out.println("Response: " + response);
    }
}

