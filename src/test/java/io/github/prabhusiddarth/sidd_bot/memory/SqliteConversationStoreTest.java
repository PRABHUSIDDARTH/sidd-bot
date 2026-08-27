package io.github.prabhusiddarth.sidd_bot.memory;

import io.github.prabhusiddarth.sidd_bot.conversation.Conversation;
import io.github.prabhusiddarth.sidd_bot.conversation.Role;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for {@link SqliteConversationStore}.
 * All tests use SQLite's in-memory mode ({@code :memory:}) so no files are written to disk.
 */
class SqliteConversationStoreTest {

    // SQLite :memory: — fast, no file I/O, automatically discarded after the connection closes.
    // Each test gets a fresh store because @BeforeEach creates a new instance.
    private SqliteConversationStore store;

    @BeforeEach
    void setUp() {
        store = new SqliteConversationStore(":memory:");
    }

    @AfterEach
    void tearDown() {
        store.clearAll();
    }

    @Test
    void testSaveAndGet() {
        Conversation conv = new Conversation("sqlite-test-1");
        conv.addMessage(Role.USER, "Hello SQLite!");

        store.save(conv);

        Optional<Conversation> result = store.get("sqlite-test-1");
        assertTrue(result.isPresent());
        assertEquals("sqlite-test-1", result.get().getId());
        assertEquals(1, result.get().getMessages().size());
        assertEquals("Hello SQLite!", result.get().getMessages().get(0).getContent());
        assertEquals(Role.USER, result.get().getMessages().get(0).getRole());
    }

    @Test
    void testExists() {
        Conversation conv = new Conversation("sqlite-test-2");
        assertFalse(store.exists("sqlite-test-2"));

        store.save(conv);
        assertTrue(store.exists("sqlite-test-2"));
    }

    @Test
    void testDelete() {
        Conversation conv = new Conversation("sqlite-test-3");
        store.save(conv);
        assertTrue(store.exists("sqlite-test-3"));

        store.delete("sqlite-test-3");
        assertFalse(store.exists("sqlite-test-3"));
        assertFalse(store.get("sqlite-test-3").isPresent());
    }

    @Test
    void testClearAll() {
        store.save(new Conversation("clear-a"));
        store.save(new Conversation("clear-b"));

        store.clearAll();

        assertFalse(store.exists("clear-a"));
        assertFalse(store.exists("clear-b"));
    }

    @Test
    void testUpdateExistingConversation() {
        Conversation conv = new Conversation("sqlite-update");
        conv.addMessage(Role.USER, "First message");
        store.save(conv);

        // Add another message and save again — should overwrite
        conv.addMessage(Role.ASSISTANT, "First reply");
        store.save(conv);

        Optional<Conversation> result = store.get("sqlite-update");
        assertTrue(result.isPresent());
        assertEquals(2, result.get().getMessages().size());
    }

    @Test
    void testGetNonExistentReturnsEmpty() {
        Optional<Conversation> result = store.get("does-not-exist");
        assertFalse(result.isPresent());
    }

    @Test
    void testMessageTimestampPreserved() {
        Conversation conv = new Conversation("sqlite-ts");
        conv.addMessage(Role.USER, "Timestamp test");
        store.save(conv);

        Conversation loaded = store.get("sqlite-ts").orElseThrow();
        assertNotNull(loaded.getMessages().get(0).getTimestamp());
    }

    @Test
    void testMultipleMessages() {
        Conversation conv = new Conversation("sqlite-multi");
        conv.addMessage(Role.USER, "msg1");
        conv.addMessage(Role.ASSISTANT, "reply1");
        conv.addMessage(Role.USER, "msg2");
        store.save(conv);

        Conversation loaded = store.get("sqlite-multi").orElseThrow();
        assertEquals(3, loaded.getMessages().size());
        assertEquals(Role.USER, loaded.getMessages().get(0).getRole());
        assertEquals("msg1", loaded.getMessages().get(0).getContent());
        assertEquals(Role.ASSISTANT, loaded.getMessages().get(1).getRole());
        assertEquals(Role.USER, loaded.getMessages().get(2).getRole());
    }

    @Test
    void testSaveNullConversationDoesNotThrow() {
        // Should silently ignore null — no exception
        assertDoesNotThrow(() -> store.save(null));
    }
}
