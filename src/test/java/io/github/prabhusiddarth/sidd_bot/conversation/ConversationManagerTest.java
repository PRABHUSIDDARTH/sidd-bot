package io.github.prabhusiddarth.sidd_bot.conversation;

import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class ConversationManagerTest {
    @Test
    void testCreateAndGetConversation() {
        ConversationManager manager = new ConversationManager();
        Conversation conv = manager.createConversation("c1");

        assertNotNull(conv);
        assertEquals("c1", conv.getId());

        Optional<Conversation> fetched = manager.getConversation("c1");
        assertTrue(fetched.isPresent());
        assertEquals("c1", fetched.get().getId());
    }

    @Test
    void testGetOrCreateConversation() {
        ConversationManager manager = new ConversationManager();
        Conversation conv = manager.getOrCreateConversation("c2");

        assertNotNull(conv);
        assertEquals("c2", conv.getId());
    }
}
