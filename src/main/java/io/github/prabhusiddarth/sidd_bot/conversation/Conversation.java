package io.github.prabhusiddarth.sidd_bot.conversation;

import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;

public class Conversation {
    private final String id;
    private final List<Message> messages;

    public Conversation() {
        this(UUID.randomUUID().toString());
    }

    public Conversation(String id) {
        this.id = id;
        this.messages = new CopyOnWriteArrayList<>();
    }

    public String getId() {
        return id;
    }

    public void addMessage(Role role, String content) {
        messages.add(new Message(role, content));
    }

    public void addMessage(Message message) {
        messages.add(message);
    }

    public List<Message> getMessages() {
        return Collections.unmodifiableList(messages);
    }

    public void clear() {
        messages.clear();
    }
}
