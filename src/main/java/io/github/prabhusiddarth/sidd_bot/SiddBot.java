package io.github.prabhusiddarth.sidd_bot;

import io.github.prabhusiddarth.sidd_bot.conversation.Conversation;
import io.github.prabhusiddarth.sidd_bot.conversation.ConversationManager;
import io.github.prabhusiddarth.sidd_bot.conversation.Role;
import io.github.prabhusiddarth.sidd_bot.generation.GenerationConfig;

import io.github.prabhusiddarth.sidd_bot.memory.ConversationStore;
import io.github.prabhusiddarth.sidd_bot.memory.InMemoryConversationStore;
import io.github.prabhusiddarth.sidd_bot.memory.SqliteConversationStore;
import io.github.prabhusiddarth.sidd_bot.memory.StorageType;
import io.github.prabhusiddarth.sidd_bot.model.Model;
import io.github.prabhusiddarth.sidd_bot.model.ModelManager;
import io.github.prabhusiddarth.sidd_bot.security.PromptGuard;

import io.github.prabhusiddarth.sidd_ai.ChatResponse;

public class SiddBot {
    private final ConversationManager conversationManager;
    private final ModelManager modelManager;
    private final PromptGuard promptGuard;
    private final GenerationConfig defaultGenerationConfig;

    public SiddBot() {
        this(new ConversationManager(), new ModelManager(), new PromptGuard(), null);
    }

    public SiddBot(ModelManager modelManager) {
        this(new ConversationManager(), modelManager, new PromptGuard(), null);
    }

    public SiddBot(ConversationManager conversationManager, ModelManager modelManager, PromptGuard promptGuard) {
        this(conversationManager, modelManager, promptGuard, null);
    }

    public SiddBot(ConversationManager conversationManager, ModelManager modelManager, PromptGuard promptGuard, GenerationConfig defaultGenerationConfig) {
        this.conversationManager = conversationManager;
        this.modelManager = modelManager;
        this.promptGuard = promptGuard;
        this.defaultGenerationConfig = defaultGenerationConfig;
    }

    public String chat(String userPrompt) {
        return chat("default", userPrompt, null);
    }

    public String chat(String userPrompt, GenerationConfig generationConfig) {
        return chat("default", userPrompt, generationConfig);
    }

    public String chat(String conversationId, String userPrompt) {
        return chat(conversationId, userPrompt, null);
    }

    public String chat(String conversationId, String userPrompt, GenerationConfig generationConfig) {
        if (!promptGuard.isSafe(userPrompt)) {
            throw new IllegalArgumentException("Prompt flag relates to unsafe content or prompt injection attempt.");
        }

        Conversation conversation = conversationManager.getOrCreateConversation(conversationId);
        conversation.addMessage(Role.USER, userPrompt);

        String fullPrompt = buildPromptWithContext(conversation);
        Model activeModel = modelManager.getActiveModel();
        GenerationConfig configToUse = generationConfig != null ? generationConfig : defaultGenerationConfig;
        String responseContent = modelManager.generateResponse(activeModel, fullPrompt, configToUse);

        conversation.addMessage(Role.ASSISTANT, responseContent);
        conversationManager.getStore().save(conversation);
        return responseContent;
    }

    public String chatWithModel(String modelOrAlias, String userPrompt) {
        return chatWithModel("default", modelOrAlias, userPrompt, null);
    }

    public String chatWithModel(String modelOrAlias, String userPrompt, GenerationConfig generationConfig) {
        return chatWithModel("default", modelOrAlias, userPrompt, generationConfig);
    }

    public String chatWithModel(String conversationId, String modelOrAlias, String userPrompt) {
        return chatWithModel(conversationId, modelOrAlias, userPrompt, null);
    }

    public String chatWithModel(String conversationId, String modelOrAlias, String userPrompt, GenerationConfig generationConfig) {
        if (!promptGuard.isSafe(userPrompt)) {
            throw new IllegalArgumentException("Prompt flag relates to unsafe content or prompt injection attempt.");
        }

        Conversation conversation = conversationManager.getOrCreateConversation(conversationId);
        conversation.addMessage(Role.USER, userPrompt);

        String fullPrompt = buildPromptWithContext(conversation);
        Model model = modelManager.resolveModel(modelOrAlias);
        GenerationConfig configToUse = generationConfig != null ? generationConfig : defaultGenerationConfig;
        String responseContent = modelManager.generateResponse(model, fullPrompt, configToUse);

        conversation.addMessage(Role.ASSISTANT, responseContent);
        conversationManager.getStore().save(conversation);
        return responseContent;
    }

    public ChatResponse chatResponse(String userPrompt) {
        return chatResponse("default", modelManager.getActiveModel().getName(), userPrompt, null);
    }

    public ChatResponse chatResponse(String userPrompt, GenerationConfig generationConfig) {
        return chatResponse("default", modelManager.getActiveModel().getName(), userPrompt, generationConfig);
    }

    public ChatResponse chatResponse(String conversationId, String modelOrAlias, String userPrompt) {
        return chatResponse(conversationId, modelOrAlias, userPrompt, null);
    }

    public ChatResponse chatResponse(String conversationId, String modelOrAlias, String userPrompt, GenerationConfig generationConfig) {
        if (!promptGuard.isSafe(userPrompt)) {
            throw new IllegalArgumentException("Prompt flag relates to unsafe content or prompt injection attempt.");
        }

        Conversation conversation = conversationManager.getOrCreateConversation(conversationId);
        conversation.addMessage(Role.USER, userPrompt);

        String fullPrompt = buildPromptWithContext(conversation);
        Model model = modelManager.resolveModel(modelOrAlias);
        GenerationConfig configToUse = generationConfig != null ? generationConfig : defaultGenerationConfig;
        ChatResponse response = modelManager.chatResponse(model, fullPrompt, configToUse);

        if (response != null && response.getContent() != null) {
            conversation.addMessage(Role.ASSISTANT, response.getContent());
        }
        conversationManager.getStore().save(conversation);
        return response;
    }

    private String buildPromptWithContext(Conversation conversation) {
        java.util.List<io.github.prabhusiddarth.sidd_bot.conversation.Message> messages = conversation.getMessages();
        if (messages.isEmpty()) {
            return "";
        }
        if (messages.size() == 1) {
            return messages.get(0).getContent();
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < messages.size(); i++) {
            io.github.prabhusiddarth.sidd_bot.conversation.Message msg = messages.get(i);
            String prefix = switch (msg.getRole()) {
                case SYSTEM -> "System: ";
                case USER -> "User: ";
                case ASSISTANT -> "Assistant: ";
            };
            sb.append(prefix).append(msg.getContent());
            if (i < messages.size() - 1) {
                sb.append("\n\n");
            }
        }
        return sb.toString();
    }

    public ConversationManager getConversationManager() {
        return conversationManager;
    }

    public ModelManager getModelManager() {
        return modelManager;
    }

    public PromptGuard getPromptGuard() {
        return promptGuard;
    }

    public GenerationConfig getDefaultGenerationConfig() {
        return defaultGenerationConfig;
    }

    /**
     * Returns a new {@link Builder} for constructing a {@code SiddBot} instance.
     */
    public static Builder builder() {
        return new Builder();
    }

    // =========================================================================
    // Builder
    // =========================================================================

    /**
     * Fluent builder for {@link SiddBot}.
     */
    public static final class Builder {

        private ConversationStore store;
        private ModelManager modelManager;
        private PromptGuard promptGuard;
        private GenerationConfig generationConfig;

        private Builder() {}

        // --- Storage (primary extension point) ---

        public Builder storage(ConversationStore store) {
            this.store = store;
            return this;
        }

        // --- StorageType shortcuts ---

        public Builder storageType(StorageType type) {
            return storageType(type, SqliteConversationStore.DEFAULT_DB_PATH);
        }

        public Builder storageType(StorageType type, String dbPath) {
            this.store = switch (type) {
                case IN_MEMORY -> new InMemoryConversationStore();
                case SQLITE    -> new SqliteConversationStore(dbPath);
            };
            return this;
        }

        // --- Model ---

        public Builder model(ModelManager modelManager) {
            this.modelManager = modelManager;
            return this;
        }

        public Builder defaultModel(String defaultModelName) {
            this.modelManager = new ModelManager(defaultModelName);
            return this;
        }

        // --- Generation Config & Temperature Shortcuts ---

        public Builder generationConfig(GenerationConfig generationConfig) {
            this.generationConfig = generationConfig;
            return this;
        }

        public Builder temperature(Double temperature) {
            if (this.generationConfig == null) {
                this.generationConfig = GenerationConfig.builder().temperature(temperature).build();
            } else {
                this.generationConfig = GenerationConfig.builder()
                        .temperature(temperature)
                        .maxTokens(this.generationConfig.getMaxTokens())
                        .topP(this.generationConfig.getTopP())
                        .build();
            }
            return this;
        }

        public Builder maxTokens(Integer maxTokens) {
            if (this.generationConfig == null) {
                this.generationConfig = GenerationConfig.builder().maxTokens(maxTokens).build();
            } else {
                this.generationConfig = GenerationConfig.builder()
                        .temperature(this.generationConfig.getTemperature())
                        .maxTokens(maxTokens)
                        .topP(this.generationConfig.getTopP())
                        .build();
            }
            return this;
        }

        // --- Security ---

        public Builder guard(PromptGuard guard) {
            this.promptGuard = guard;
            return this;
        }

        // --- Build ---

        public SiddBot build() {
            ConversationStore resolvedStore = (store != null) ? store : new InMemoryConversationStore();
            ConversationManager cm = new ConversationManager(resolvedStore);
            ModelManager mm = (modelManager != null) ? modelManager : new ModelManager();
            PromptGuard pg = (promptGuard != null) ? promptGuard : new PromptGuard();
            return new SiddBot(cm, mm, pg, generationConfig);
        }
    }
}
