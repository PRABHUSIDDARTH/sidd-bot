package io.github.prabhusiddarth.sidd_bot;

import io.github.prabhusiddarth.sidd_bot.memory.InMemoryConversationStore;
import io.github.prabhusiddarth.sidd_bot.memory.SqliteConversationStore;
import io.github.prabhusiddarth.sidd_bot.model.Model;
import io.github.prabhusiddarth.sidd_bot.model.ModelManager;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests verifying that SiddBot passes full conversation context history
 * to ModelManager for multi-turn conversations across all storage backends.
 */
class SiddBotContextTest {

    static class CapturingModelManager extends ModelManager {
        final List<String> capturedPrompts = new ArrayList<>();

        @Override
        public io.github.prabhusiddarth.sidd_ai.ChatResponse chatResponse(
                Model model, String prompt, io.github.prabhusiddarth.sidd_bot.generation.GenerationConfig config) {
            capturedPrompts.add(prompt);
            String responseText = prompt.contains("fact")
                    ? "Paris is known for the Eiffel Tower."
                    : "The capital of France is Paris.";
            return new io.github.prabhusiddarth.sidd_ai.ChatResponse(
                    responseText,
                    model != null ? model.getName() : "stub",
                    0
            );
        }
    }

    @Test
    void testMultiTurnContextPassedToModelInMemory() {
        CapturingModelManager modelManager = new CapturingModelManager();
        SiddBot bot = SiddBot.builder()
                .storage(new InMemoryConversationStore())
                .model(modelManager)
                .build();

        // Turn 1
        String resp1 = bot.chat("session-context", "What is the capital of France?");
        assertEquals("The capital of France is Paris.", resp1);
        assertEquals(1, modelManager.capturedPrompts.size());
        assertEquals("What is the capital of France?", modelManager.capturedPrompts.get(0));

        // Turn 2
        String resp2 = bot.chat("session-context", "Tell me one fact about it?");
        assertEquals("Paris is known for the Eiffel Tower.", resp2);
        assertEquals(2, modelManager.capturedPrompts.size());

        String turn2Prompt = modelManager.capturedPrompts.get(1);
        assertTrue(turn2Prompt.contains("What is the capital of France?"), "Turn 2 prompt must contain Turn 1 question");
        assertTrue(turn2Prompt.contains("The capital of France is Paris."), "Turn 2 prompt must contain Turn 1 response");
        assertTrue(turn2Prompt.contains("Tell me one fact about it?"), "Turn 2 prompt must contain Turn 2 question");
    }

    @Test
    void testMultiTurnContextPassedToModelSqlite(@TempDir Path tempDir) {
        File dbFile = tempDir.resolve("context-sqlite.db").toFile();

        CapturingModelManager modelManager = new CapturingModelManager();
        SiddBot bot = SiddBot.builder()
                .storage(new SqliteConversationStore(dbFile.getAbsolutePath()))
                .model(modelManager)
                .build();

        // Turn 1
        bot.chat("sqlite-context-session", "What is the capital of France?");

        // Turn 2
        bot.chat("sqlite-context-session", "Tell me one fact about it?");

        assertEquals(2, modelManager.capturedPrompts.size());
        String turn2Prompt = modelManager.capturedPrompts.get(1);

        assertTrue(turn2Prompt.contains("What is the capital of France?"));
        assertTrue(turn2Prompt.contains("The capital of France is Paris."));
        assertTrue(turn2Prompt.contains("Tell me one fact about it?"));

        // Verify conversation history length in store
        var conv = bot.getConversationManager().getConversation("sqlite-context-session");
        assertTrue(conv.isPresent());
        assertEquals(4, conv.get().getMessages().size(), "Conversation should have 4 messages (2 turns x 2 roles)");
    }
}
