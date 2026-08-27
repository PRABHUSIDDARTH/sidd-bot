package io.github.prabhusiddarth.sidd_bot.memory;

import io.github.prabhusiddarth.sidd_bot.conversation.Conversation;
import io.github.prabhusiddarth.sidd_bot.conversation.Message;
import io.github.prabhusiddarth.sidd_bot.conversation.Role;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.nio.file.Path;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration-style tests verifying that SqliteConversationStore correctly persists,
 * reloads, updates without duplicating messages, and preserves stable Message IDs across
 * distinct store instances.
 */
class SqlitePersistenceTest {

    @Test
    void testPersistenceAndNoDuplicatesAcrossStoreInstances(@TempDir Path tempDir) {
        File dbFile = tempDir.resolve("test-persistence.db").toFile();
        String dbPath = dbFile.getAbsolutePath();

        // 1. Create first store instance
        ConversationStore store1 = new SqliteConversationStore(dbPath);

        Conversation conv = new Conversation("test-session");
        Message msg1 = new Message(Role.USER, "What is the capital of France?");
        Message msg2 = new Message(Role.ASSISTANT, "Paris");

        conv.addMessage(msg1);
        conv.addMessage(msg2);
        store1.save(conv);

        String msg1Id = msg1.getId();
        String msg2Id = msg2.getId();

        assertNotNull(msg1Id, "Message 1 must have a non-null ID");
        assertNotNull(msg2Id, "Message 2 must have a non-null ID");

        // 2. Create a NEW store instance pointing to the same file database
        ConversationStore store2 = new SqliteConversationStore(dbPath);

        Optional<Conversation> loaded1 = store2.get("test-session");
        assertTrue(loaded1.isPresent(), "Conversation should exist in store2");
        assertEquals(2, loaded1.get().getMessages().size(), "Should contain exactly 2 messages");

        Message loadedMsg1 = loaded1.get().getMessages().get(0);
        Message loadedMsg2 = loaded1.get().getMessages().get(1);

        assertEquals(Role.USER, loadedMsg1.getRole());
        assertEquals("What is the capital of France?", loadedMsg1.getContent());
        assertEquals(msg1Id, loadedMsg1.getId(), "Message 1 ID must remain stable after reload");

        assertEquals(Role.ASSISTANT, loadedMsg2.getRole());
        assertEquals("Paris", loadedMsg2.getContent());
        assertEquals(msg2Id, loadedMsg2.getId(), "Message 2 ID must remain stable after reload");

        // 3. Add a third message to the loaded conversation and save
        Conversation activeConv = loaded1.get();
        Message msg3 = new Message(Role.USER, "Tell me one fact about it?");
        activeConv.addMessage(msg3);

        assertEquals(3, activeConv.getMessages().size(), "Should now contain 3 messages in-memory");
        store2.save(activeConv);

        // 4. Create a THIRD store instance pointing to the same database file
        ConversationStore store3 = new SqliteConversationStore(dbPath);

        Optional<Conversation> loaded2 = store3.get("test-session");
        assertTrue(loaded2.isPresent(), "Conversation should exist in store3");
        assertEquals(3, loaded2.get().getMessages().size(), "Store3 must contain exactly 3 messages (no duplicates!)");

        assertEquals("What is the capital of France?", loaded2.get().getMessages().get(0).getContent());
        assertEquals("Paris", loaded2.get().getMessages().get(1).getContent());
        assertEquals("Tell me one fact about it?", loaded2.get().getMessages().get(2).getContent());

        // Verify IDs across all 3 messages
        assertEquals(msg1Id, loaded2.get().getMessages().get(0).getId());
        assertEquals(msg2Id, loaded2.get().getMessages().get(1).getId());
        assertEquals(msg3.getId(), loaded2.get().getMessages().get(2).getId());

        // 5. Multiple repeated save calls on the same store should NEVER duplicate messages
        store3.save(loaded2.get());
        store3.save(loaded2.get());

        ConversationStore store4 = new SqliteConversationStore(dbPath);
        Optional<Conversation> loaded3 = store4.get("test-session");
        assertTrue(loaded3.isPresent());
        assertEquals(3, loaded3.get().getMessages().size(), "Repeated save calls must not duplicate messages");
    }
}
