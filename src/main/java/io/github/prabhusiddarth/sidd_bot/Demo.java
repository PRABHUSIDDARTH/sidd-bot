package io.github.prabhusiddarth.sidd_bot;

import io.github.prabhusiddarth.sidd_ai.ChatResponse;
import io.github.prabhusiddarth.sidd_bot.conversation.Conversation;
import io.github.prabhusiddarth.sidd_bot.generation.GenerationConfig;
import io.github.prabhusiddarth.sidd_bot.memory.InMemoryConversationStore;
import io.github.prabhusiddarth.sidd_bot.memory.StorageType;

import java.util.Optional;

/**
 * Interactive demo for sidd-bot.
 *
 * Run via Maven:
 * mvn exec:java -Dexec.mainClass=io.github.prabhusiddarth.sidd_bot.Demo
 *
 * Or with a specific model:
 * mvn exec:java -Dexec.mainClass=io.github.prabhusiddarth.sidd_bot.Demo
 * -Dexec.args="gemini-3.6-flash"
 */
public class Demo {

    public static void main(String[] args) throws Exception {
        String model = args.length > 0 ? args[0] : "nvidia/nemotron-3.5-lightning-30b-a3b";
        Double temperature = 0.7;
        Integer maxTokens = 500;

        GenerationConfig genConfig = GenerationConfig.builder()
                .temperature(temperature)
                .maxTokens(maxTokens)
                .build();

        // Clean up any old demo database file from previous runs
        java.io.File dbFile = new java.io.File("sidd-bot-demo.db");
        if (dbFile.exists()) {
            dbFile.delete();
        }

        printHeader();

        System.out.println("  Model       : " + model);
        System.out.println("  Temperature : " + temperature);
        System.out.println("  Max Tokens  : " + maxTokens);
        System.out.println("  Storage     : In-Memory + SQLite demo");
        System.out.println();

        // ── Demo 1: In-Memory Storage ────────────────────────────────────────
        section("DEMO 1 — In-Memory Storage (.storage(new InMemoryConversationStore()))");

        SiddBot memBot = SiddBot.builder()
                .storage(new InMemoryConversationStore())
                .defaultModel(model)
                .generationConfig(genConfig)
                .build();

        chat(memBot, "session-1", "Hello! I am Siddarth, a developer.");
        chat(memBot, "session-1", "What is my name?");

        printConversationHistory(memBot, "session-1", "In-Memory session-1 history");

        // ── Demo 2: SQLite Persistent Storage ────────────────────────────────
        section("DEMO 2 — SQLite Persistent Storage (.storageType(StorageType.SQLITE))");

        SiddBot sqliteBot = SiddBot.builder()
                .storageType(StorageType.SQLITE, "sidd-bot-demo.db")
                .defaultModel(model)
                .generationConfig(genConfig)
                .build();

        chat(sqliteBot, "persist-session", "What is the capital of France?");
        chat(sqliteBot, "persist-session", "Tell me one fact about it.");

        printConversationHistory(sqliteBot, "persist-session", "SQLite persist-session history");

        System.out.println();
        System.out.println("  SQLite database written to: sidd-bot-demo.db");

        // ── Demo 3: StorageType shortcut ─────────────────────────────────────
        section("DEMO 3 — StorageType.IN_MEMORY shortcut");

        SiddBot shortcutBot = SiddBot.builder()
                .storageType(StorageType.IN_MEMORY)
                .defaultModel(model)
                .generationConfig(genConfig)
                .build();

        chat(shortcutBot, "default", "Explain Java in one sentence.");

        footer();
    }

    // ─────────────────────────────────────────────────────────────────────────

    private static void chat(SiddBot bot, String sessionId, String prompt) {
        System.out.println("  > [" + sessionId + "] " + prompt);
        try {
            ChatResponse response = bot.chatResponse(sessionId, bot.getModelManager().getActiveModel().getName(), prompt);
            System.out.println("  < " + (response != null ? response.getContent() : ""));
            if (response != null && response.getTokensUsed() > 0) {
                System.out.println("  [Tokens Used: " + response.getTokensUsed() + "]");
            }
            Thread.sleep(2000);
        } catch (IllegalArgumentException e) {
            System.out.println("  ! BLOCKED: " + e.getMessage());
        } catch (Exception e) {
            System.out.println("  ! ERROR: " + e.getMessage());
        }
        System.out.println();
    }

    private static void printConversationHistory(SiddBot bot, String sessionId, String label) {
        Optional<Conversation> conv = bot.getConversationManager().getConversation(sessionId);
        System.out.println("  ┌─ " + label + " ──────────────────────");
        if (conv.isEmpty()) {
            System.out.println("  │  (no conversation found)");
        } else {
            conv.get().getMessages().forEach(m -> System.out.printf("  │  [%-9s] %s%n", m.getRole(), m.getContent()));
        }
        System.out.println("  └──────────────────────────────────────────────");
        System.out.println();
    }

    private static void section(String title) {
        System.out.println();
        System.out.println("  ══════════════════════════════════════════════════");
        System.out.println("  " + title);
        System.out.println("  ══════════════════════════════════════════════════");
        System.out.println();
    }

    private static void printHeader() {
        System.out.println();
        System.out.println("  ╔══════════════════════════════════════════════════╗");
        System.out.println("  ║           sidd-bot  —  Live Demo                 ║");
        System.out.println("  ║   Pluggable Storage Architecture Demo            ║");
        System.out.println("  ╚══════════════════════════════════════════════════╝");
        System.out.println();
    }

    private static void footer() {
        System.out.println();
        System.out.println("  ══════════════════════════════════════════════════");
        System.out.println("  Demo complete. All storage backends verified.");
        System.out.println("  ══════════════════════════════════════════════════");
        System.out.println();
    }
}
