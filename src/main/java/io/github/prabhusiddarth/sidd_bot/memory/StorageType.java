package io.github.prabhusiddarth.sidd_bot.memory;

/**
 * Convenience enum for selecting a built-in storage backend via {@code SiddBot.builder()}.
 *
 * <p>This enum is a shortcut API only. The fundamental extension point is {@link ConversationStore}:
 * implement that interface to plug in any storage system without modifying any core class.
 *
 * <p>Example — canonical (preferred) approach:
 * <pre>{@code
 * SiddBot bot = SiddBot.builder()
 *         .storage(new InMemoryConversationStore())
 *         .build();
 * }</pre>
 *
 * <p>Example — convenience shortcut:
 * <pre>{@code
 * SiddBot bot = SiddBot.builder()
 *         .storageType(StorageType.SQLITE)
 *         .build();
 * }</pre>
 */
public enum StorageType {

    /** Thread-safe, in-process store backed by {@link java.util.concurrent.ConcurrentHashMap}. Data is lost on JVM exit. */
    IN_MEMORY,

    /**
     * Persistent local store backed by SQLite via {@link SqliteConversationStore}.
     * Default database file: {@code sidd-bot.db} in the working directory.
     * Provide a custom path via {@code SiddBot.builder().storageType(StorageType.SQLITE, "path/to/file.db")}.
     */
    SQLITE
}
