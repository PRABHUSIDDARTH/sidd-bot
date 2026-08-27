package io.github.prabhusiddarth.sidd_bot.memory;

import io.github.prabhusiddarth.sidd_bot.conversation.Conversation;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class InMemoryConversationStore implements ConversationStore {
    private final Map<String, Conversation> store = new ConcurrentHashMap<>();

    @Override
    public void save(Conversation conversation) {
        if (conversation != null && conversation.getId() != null) {
            store.put(conversation.getId(), conversation);
        }
    }

    @Override
    public Optional<Conversation> get(String conversationId) {
        return Optional.ofNullable(store.get(conversationId));
    }

    @Override
    public void delete(String conversationId) {
        store.remove(conversationId);
    }

    @Override
    public boolean exists(String conversationId) {
        return store.containsKey(conversationId);
    }

    @Override
    public void clearAll() {
        store.clear();
    }
}
