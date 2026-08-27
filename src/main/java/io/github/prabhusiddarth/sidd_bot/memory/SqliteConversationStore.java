package io.github.prabhusiddarth.sidd_bot.memory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import io.github.prabhusiddarth.sidd_bot.conversation.Conversation;
import io.github.prabhusiddarth.sidd_bot.conversation.Message;
import io.github.prabhusiddarth.sidd_bot.conversation.Role;

import java.io.IOException;
import java.sql.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * A persistent {@link ConversationStore} backed by a local SQLite database.
 *
 * <p>Conversations are serialised as JSON (using Jackson, which is already a transitive
 * dependency via {@code sidd-ai}) and stored in a single table:
 * <pre>
 *   conversations(id TEXT PRIMARY KEY, data TEXT NOT NULL)
 * </pre>
 *
 * <h3>Usage</h3>
 * <pre>{@code
 * // Default database file: sidd-bot.db in the working directory
 * ConversationStore store = new SqliteConversationStore();
 *
 * // Custom database file
 * ConversationStore store = new SqliteConversationStore("/var/data/my-bot.db");
 *
 * // In-memory SQLite (useful for tests)
 * ConversationStore store = new SqliteConversationStore(":memory:");
 *
 * // Plug into SiddBot via the builder (recommended)
 * SiddBot bot = SiddBot.builder()
 *         .storage(new SqliteConversationStore())
 *         .build();
 * }</pre>
 *
 * <h3>Architectural note</h3>
 * This class is the <em>only</em> part of {@code sidd-bot} that references SQLite.
 * Removing the {@code org.xerial:sqlite-jdbc} Maven dependency and deleting this
 * file is all that is required to drop SQLite from the project entirely.
 */
public class SqliteConversationStore implements ConversationStore {

    /** Default database file path used when no path is supplied. */
    public static final String DEFAULT_DB_PATH = "sidd-bot.db";

    private static final String CREATE_TABLE_SQL =
            "CREATE TABLE IF NOT EXISTS conversations (" +
            "  id   TEXT PRIMARY KEY," +
            "  data TEXT NOT NULL" +
            ")";

    private static final ObjectMapper MAPPER = new ObjectMapper()
            .findAndRegisterModules()
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    private static final String MEMORY_DB = ":memory:";

    private final String jdbcUrl;
    /**
     * Persistent connection, non-null only for in-memory databases.
     * SQLite's :memory: databases are per-connection; opening a new connection
     * would create a fresh (empty) database, so we reuse one long-lived connection.
     */
    private final Connection sharedConnection;

    /**
     * Creates a store using the default database file ({@value DEFAULT_DB_PATH})
     * in the working directory.
     */
    public SqliteConversationStore() {
        this(DEFAULT_DB_PATH);
    }

    /**
     * Creates a store using the supplied database file path.
     *
     * @param dbPath path to the SQLite database file, or {@code ":memory:"} for an
     *               in-process, non-persistent database (useful for testing)
     */
    public SqliteConversationStore(String dbPath) {
        this.jdbcUrl = "jdbc:sqlite:" + dbPath;
        if (MEMORY_DB.equals(dbPath)) {
            // :memory: is per-connection — open once and hold it for the lifetime of this store.
            try {
                this.sharedConnection = DriverManager.getConnection(jdbcUrl);
            } catch (SQLException e) {
                throw new SqliteStoreException("Failed to open in-memory SQLite connection", e);
            }
        } else {
            this.sharedConnection = null;
        }
        initSchema();
    }

    // -------------------------------------------------------------------------
    // ConversationStore implementation
    // -------------------------------------------------------------------------

    @Override
    public void save(Conversation conversation) {
        if (conversation == null || conversation.getId() == null) {
            return;
        }
        String json = toJson(conversation);
        String sql = "INSERT INTO conversations(id, data) VALUES(?,?) " +
                     "ON CONFLICT(id) DO UPDATE SET data = excluded.data";
        try (Connection conn = connect(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, conversation.getId());
            ps.setString(2, json);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new SqliteStoreException("Failed to save conversation: " + conversation.getId(), e);
        }
    }

    @Override
    public Optional<Conversation> get(String conversationId) {
        String sql = "SELECT data FROM conversations WHERE id = ?";
        try (Connection conn = connect(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, conversationId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(fromJson(rs.getString("data")));
                }
            }
        } catch (SQLException e) {
            throw new SqliteStoreException("Failed to retrieve conversation: " + conversationId, e);
        }
        return Optional.empty();
    }

    @Override
    public void delete(String conversationId) {
        String sql = "DELETE FROM conversations WHERE id = ?";
        try (Connection conn = connect(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, conversationId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new SqliteStoreException("Failed to delete conversation: " + conversationId, e);
        }
    }

    @Override
    public boolean exists(String conversationId) {
        String sql = "SELECT 1 FROM conversations WHERE id = ?";
        try (Connection conn = connect(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, conversationId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            throw new SqliteStoreException("Failed to check existence of conversation: " + conversationId, e);
        }
    }

    @Override
    public void clearAll() {
        String sql = "DELETE FROM conversations";
        try (Connection conn = connect(); Statement st = conn.createStatement()) {
            st.executeUpdate(sql);
        } catch (SQLException e) {
            throw new SqliteStoreException("Failed to clear all conversations", e);
        }
    }

    // -------------------------------------------------------------------------
    // Internal helpers
    // -------------------------------------------------------------------------

    private void initSchema() {
        try (Connection conn = connect(); Statement st = conn.createStatement()) {
            st.execute(CREATE_TABLE_SQL);
        } catch (SQLException e) {
            throw new SqliteStoreException("Failed to initialise SQLite schema at: " + jdbcUrl, e);
        }
    }

    /**
     * Returns a connection for use in a try-with-resources block.
     * <ul>
     *   <li>File-based: a new connection is opened and closed per operation (safe for multi-thread).</li>
     *   <li>{@code :memory:}: returns a wrapper around the shared connection that suppresses
     *       {@code close()} so the in-memory database and its schema persist across calls.</li>
     * </ul>
     */
    private Connection connect() throws SQLException {
        if (sharedConnection != null) {
            // Wrap the shared connection so that try-with-resources does NOT close it.
            return new UnclosableConnection(sharedConnection);
        }
        return DriverManager.getConnection(jdbcUrl);
    }

    private String toJson(Conversation conversation) {
        try {
            ConversationDto dto = ConversationDto.from(conversation);
            return MAPPER.writeValueAsString(dto);
        } catch (JsonProcessingException e) {
            throw new SqliteStoreException("Failed to serialise conversation: " + conversation.getId(), e);
        }
    }

    private Conversation fromJson(String json) {
        try {
            ConversationDto dto = MAPPER.readValue(json, ConversationDto.class);
            return dto.toConversation();
        } catch (IOException e) {
            throw new SqliteStoreException("Failed to deserialise conversation from JSON", e);
        }
    }

    // -------------------------------------------------------------------------
    // DTO — plain Java representation for JSON serialisation
    // -------------------------------------------------------------------------

    /** Internal DTO used purely for JSON round-tripping. Not part of the public API. */
    static class ConversationDto {
        public String id;
        public List<MessageDto> messages = new ArrayList<>();

        static ConversationDto from(Conversation c) {
            ConversationDto dto = new ConversationDto();
            dto.id = c.getId();
            for (Message m : c.getMessages()) {
                MessageDto mdto = new MessageDto();
                mdto.id = m.getId();
                mdto.role = m.getRole().name();
                mdto.content = m.getContent();
                mdto.timestamp = m.getTimestamp().toString();
                dto.messages.add(mdto);
            }
            return dto;
        }

        Conversation toConversation() {
            Conversation c = new Conversation(id);
            for (MessageDto mdto : messages) {
                Role role = Role.valueOf(mdto.role);
                Instant ts = Instant.parse(mdto.timestamp);
                if (mdto.id != null && !mdto.id.isBlank()) {
                    c.addMessage(new Message(mdto.id, role, mdto.content, ts));
                } else {
                    c.addMessage(new Message(role, mdto.content, ts));
                }
            }
            return c;
        }
    }

    static class MessageDto {
        public String id;
        public String role;
        public String content;
        public String timestamp;
    }

    // -------------------------------------------------------------------------
    // Exception
    // -------------------------------------------------------------------------

    /**
     * Unchecked exception wrapping any {@link SQLException} or serialisation error
     * from {@code SqliteConversationStore}.
     */
    public static class SqliteStoreException extends RuntimeException {
        public SqliteStoreException(String message, Throwable cause) {
            super(message, cause);
        }
    }

    // -------------------------------------------------------------------------
    // UnclosableConnection — keeps :memory: shared connection alive
    // -------------------------------------------------------------------------

    /**
     * A {@link Connection} delegator that makes {@link #close()} a no-op.
     * Used to wrap the persistent shared connection for in-memory SQLite databases
     * so that try-with-resources blocks don't destroy the database.
     */
    private static final class UnclosableConnection implements Connection {
        private final Connection delegate;

        UnclosableConnection(Connection delegate) { this.delegate = delegate; }

        /** Intentionally does nothing — the shared connection stays open. */
        @Override public void close() {}

        @Override public boolean isClosed() throws SQLException { return delegate.isClosed(); }
        @Override public java.sql.Statement createStatement() throws SQLException { return delegate.createStatement(); }
        @Override public java.sql.PreparedStatement prepareStatement(String sql) throws SQLException { return delegate.prepareStatement(sql); }
        @Override public java.sql.CallableStatement prepareCall(String sql) throws SQLException { return delegate.prepareCall(sql); }
        @Override public String nativeSQL(String sql) throws SQLException { return delegate.nativeSQL(sql); }
        @Override public void setAutoCommit(boolean a) throws SQLException { delegate.setAutoCommit(a); }
        @Override public boolean getAutoCommit() throws SQLException { return delegate.getAutoCommit(); }
        @Override public void commit() throws SQLException { delegate.commit(); }
        @Override public void rollback() throws SQLException { delegate.rollback(); }
        @Override public java.sql.DatabaseMetaData getMetaData() throws SQLException { return delegate.getMetaData(); }
        @Override public void setReadOnly(boolean r) throws SQLException { delegate.setReadOnly(r); }
        @Override public boolean isReadOnly() throws SQLException { return delegate.isReadOnly(); }
        @Override public void setCatalog(String c) throws SQLException { delegate.setCatalog(c); }
        @Override public String getCatalog() throws SQLException { return delegate.getCatalog(); }
        @Override public void setTransactionIsolation(int l) throws SQLException { delegate.setTransactionIsolation(l); }
        @Override public int getTransactionIsolation() throws SQLException { return delegate.getTransactionIsolation(); }
        @Override public java.sql.SQLWarning getWarnings() throws SQLException { return delegate.getWarnings(); }
        @Override public void clearWarnings() throws SQLException { delegate.clearWarnings(); }
        @Override public java.sql.Statement createStatement(int rt, int rc) throws SQLException { return delegate.createStatement(rt, rc); }
        @Override public java.sql.PreparedStatement prepareStatement(String sql, int rt, int rc) throws SQLException { return delegate.prepareStatement(sql, rt, rc); }
        @Override public java.sql.CallableStatement prepareCall(String sql, int rt, int rc) throws SQLException { return delegate.prepareCall(sql, rt, rc); }
        @Override public java.util.Map<String, Class<?>> getTypeMap() throws SQLException { return delegate.getTypeMap(); }
        @Override public void setTypeMap(java.util.Map<String, Class<?>> m) throws SQLException { delegate.setTypeMap(m); }
        @Override public void setHoldability(int h) throws SQLException { delegate.setHoldability(h); }
        @Override public int getHoldability() throws SQLException { return delegate.getHoldability(); }
        @Override public java.sql.Savepoint setSavepoint() throws SQLException { return delegate.setSavepoint(); }
        @Override public java.sql.Savepoint setSavepoint(String name) throws SQLException { return delegate.setSavepoint(name); }
        @Override public void rollback(java.sql.Savepoint sp) throws SQLException { delegate.rollback(sp); }
        @Override public void releaseSavepoint(java.sql.Savepoint sp) throws SQLException { delegate.releaseSavepoint(sp); }
        @Override public java.sql.Statement createStatement(int rt, int rc, int rh) throws SQLException { return delegate.createStatement(rt, rc, rh); }
        @Override public java.sql.PreparedStatement prepareStatement(String sql, int rt, int rc, int rh) throws SQLException { return delegate.prepareStatement(sql, rt, rc, rh); }
        @Override public java.sql.CallableStatement prepareCall(String sql, int rt, int rc, int rh) throws SQLException { return delegate.prepareCall(sql, rt, rc, rh); }
        @Override public java.sql.PreparedStatement prepareStatement(String sql, int[] ci) throws SQLException { return delegate.prepareStatement(sql, ci); }
        @Override public java.sql.PreparedStatement prepareStatement(String sql, String[] cn) throws SQLException { return delegate.prepareStatement(sql, cn); }
        @Override public java.sql.PreparedStatement prepareStatement(String sql, int ag) throws SQLException { return delegate.prepareStatement(sql, ag); }
        @Override public java.sql.Clob createClob() throws SQLException { return delegate.createClob(); }
        @Override public java.sql.Blob createBlob() throws SQLException { return delegate.createBlob(); }
        @Override public java.sql.NClob createNClob() throws SQLException { return delegate.createNClob(); }
        @Override public java.sql.SQLXML createSQLXML() throws SQLException { return delegate.createSQLXML(); }
        @Override public boolean isValid(int timeout) throws SQLException { return delegate.isValid(timeout); }
        @Override public void setClientInfo(String name, String value) throws java.sql.SQLClientInfoException { delegate.setClientInfo(name, value); }
        @Override public void setClientInfo(java.util.Properties p) throws java.sql.SQLClientInfoException { delegate.setClientInfo(p); }
        @Override public String getClientInfo(String name) throws SQLException { return delegate.getClientInfo(name); }
        @Override public java.util.Properties getClientInfo() throws SQLException { return delegate.getClientInfo(); }
        @Override public java.sql.Array createArrayOf(String tn, Object[] elements) throws SQLException { return delegate.createArrayOf(tn, elements); }
        @Override public java.sql.Struct createStruct(String tn, Object[] attrs) throws SQLException { return delegate.createStruct(tn, attrs); }
        @Override public void setSchema(String s) throws SQLException { delegate.setSchema(s); }
        @Override public String getSchema() throws SQLException { return delegate.getSchema(); }
        @Override public void abort(java.util.concurrent.Executor ex) throws SQLException { delegate.abort(ex); }
        @Override public void setNetworkTimeout(java.util.concurrent.Executor ex, int ms) throws SQLException { delegate.setNetworkTimeout(ex, ms); }
        @Override public int getNetworkTimeout() throws SQLException { return delegate.getNetworkTimeout(); }
        @Override public <T> T unwrap(Class<T> iface) throws SQLException { return delegate.unwrap(iface); }
        @Override public boolean isWrapperFor(Class<?> iface) throws SQLException { return delegate.isWrapperFor(iface); }
    }
}

