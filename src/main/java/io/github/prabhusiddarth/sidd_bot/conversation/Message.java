package io.github.prabhusiddarth.sidd_bot.conversation;

import java.time.Instant;
import java.util.UUID;

/**
 * An immutable message in a conversation.
 *
 * <p>Each message carries a <strong>stable, unique {@code id}</strong> (UUID v4) that is
 * generated once at construction and preserved faithfully through JSON serialization/
 * deserialization. This guarantees that saving and reloading a conversation from any
 * {@link io.github.prabhusiddarth.sidd_bot.memory.ConversationStore} never changes
 * a message's identity.
 */
public class Message {
    /** Stable unique identifier for this message. Generated once at construction. */
    private final String id;
    private final Role role;
    private final String content;
    private final Instant timestamp;

    /** Primary constructor — generates a new random UUID for {@code id}. */
    public Message(Role role, String content) {
        this(UUID.randomUUID().toString(), role, content, Instant.now());
    }

    /**
     * Constructor for deserialization — allows restoring a message with its original
     * {@code id} and {@code timestamp} intact.
     *
     * @param id        stable message ID (must not be null)
     * @param role      the sender role
     * @param content   the message text
     * @param timestamp when the message was originally created
     */
    public Message(String id, Role role, String content, Instant timestamp) {
        this.id = id;
        this.role = role;
        this.content = content;
        this.timestamp = timestamp;
    }

    /**
     * Legacy convenience constructor that auto-generates a timestamp.
     * Kept for backwards compatibility; prefer {@link #Message(Role, String)}.
     */
    public Message(Role role, String content, Instant timestamp) {
        this(UUID.randomUUID().toString(), role, content, timestamp);
    }

    /** Returns this message's stable unique identifier. Never null, never changes. */
    public String getId() {
        return id;
    }

    public Role getRole() {
        return role;
    }

    public String getContent() {
        return content;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    @Override
    public String toString() {
        return "Message{" +
                "id='" + id + '\'' +
                ", role=" + role +
                ", content='" + content + '\'' +
                ", timestamp=" + timestamp +
                '}';
    }
}
