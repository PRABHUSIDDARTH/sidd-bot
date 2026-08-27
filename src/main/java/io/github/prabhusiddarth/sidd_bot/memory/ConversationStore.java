package io.github.prabhusiddarth.sidd_bot.memory;

import io.github.prabhusiddarth.sidd_bot.conversation.Conversation;

import java.util.Optional;

public interface ConversationStore {
    void save(Conversation conversation);
    Optional<Conversation> get(String conversationId);
    void delete(String conversationId);
    boolean exists(String conversationId);
    void clearAll();
}
