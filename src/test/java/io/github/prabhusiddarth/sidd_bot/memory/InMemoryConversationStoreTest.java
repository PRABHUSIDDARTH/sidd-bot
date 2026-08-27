package io.github.prabhusiddarth.sidd_bot.memory;

import io.github.prabhusiddarth.sidd_bot.conversation.Conversation;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class InMemoryConversationStoreTest {
    @Test
    void testSaveGetDelete() {
        InMemoryConversationStore store = new InMemoryConversationStore();
        Conversation conversation = new Conversation("conv-1");

        store.save(conversation);
        assertTrue(store.exists("conv-1"));
        assertTrue(store.get("conv-1").isPresent());

        store.delete("conv-1");
        assertFalse(store.exists("conv-1"));
    }
}
