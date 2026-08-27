package io.github.prabhusiddarth.sidd_bot.conversation;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ConversationTest {
    @Test
    void testAddAndRetrieveMessages() {
        Conversation conversation = new Conversation("test-conv");
        assertEquals("test-conv", conversation.getId());

        conversation.addMessage(Role.USER, "Hi");
        conversation.addMessage(Role.ASSISTANT, "Hello!");

        assertEquals(2, conversation.getMessages().size());
        assertEquals(Role.USER, conversation.getMessages().get(0).getRole());
        assertEquals("Hi", conversation.getMessages().get(0).getContent());
    }

    @Test
    void testClearMessages() {
        Conversation conversation = new Conversation();
        conversation.addMessage(Role.USER, "Test");
        assertEquals(1, conversation.getMessages().size());

        conversation.clear();
        assertEquals(0, conversation.getMessages().size());
    }
}
