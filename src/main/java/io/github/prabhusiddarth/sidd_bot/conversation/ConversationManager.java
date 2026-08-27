package io.github.prabhusiddarth.sidd_bot.conversation;

import io.github.prabhusiddarth.sidd_bot.memory.ConversationStore;
import io.github.prabhusiddarth.sidd_bot.memory.InMemoryConversationStore;

import java.util.Optional;

public class ConversationManager {
    private final ConversationStore store;

    public ConversationManager() {
        this(new InMemoryConversationStore());
    }

    public ConversationManager(ConversationStore store) {
        this.store = store;
    }

    public Conversation createConversation() {
        Conversation conversation = new Conversation();
        store.save(conversation);
        return conversation;
    }

    public Conversation createConversation(String id) {
        Conversation conversation = new Conversation(id);
        store.save(conversation);
        return conversation;
    }

    public Optional<Conversation> getConversation(String id) {
        return store.get(id);
    }

    public Conversation getOrCreateConversation(String id) {
        return store.get(id).orElseGet(() -> createConversation(id));
    }

    public void removeConversation(String id) {
        store.delete(id);
    }

    public ConversationStore getStore() {
        return store;
    }
}
