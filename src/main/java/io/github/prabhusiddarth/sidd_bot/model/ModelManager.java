package io.github.prabhusiddarth.sidd_bot.model;

import io.github.prabhusiddarth.sidd_ai.AiClient;
import io.github.prabhusiddarth.sidd_ai.ChatRequest;
import io.github.prabhusiddarth.sidd_ai.ChatResponse;
import io.github.prabhusiddarth.sidd_ai.router.ModelRouter;
import io.github.prabhusiddarth.sidd_bot.generation.GenerationConfig;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class ModelManager {
    private final Map<String, Model> models = new ConcurrentHashMap<>();
    private Model activeModel;
    private final AiClient aiClient;

    public ModelManager() {
        this((AiClient) null);
    }

    public ModelManager(AiClient aiClient) {
        this.aiClient = aiClient;
        Model defaultModel = new Model("gemini-3.6-flash", Provider.GEMINI);
        registerModel("default", defaultModel);
        setActiveModel("default");
    }

    public ModelManager(String defaultModelName) {
        this((AiClient) null);
        Model defaultModel = new Model(defaultModelName);
        registerModel("default", defaultModel);
        setActiveModel("default");
    }

    public void registerModel(String alias, Model model) {
        models.put(alias, model);
        if (activeModel == null) {
            activeModel = model;
        }
    }

    public Optional<Model> getModel(String alias) {
        return Optional.ofNullable(models.get(alias));
    }

    public boolean setActiveModel(String alias) {
        Model model = models.get(alias);
        if (model != null) {
            this.activeModel = model;
            return true;
        }
        return false;
    }

    public Model getActiveModel() {
        return activeModel;
    }

    public Map<String, Model> getRegisteredModels() {
        return Map.copyOf(models);
    }

    public AiClient getAiClient() {
        return aiClient;
    }

    public Model resolveModel(String aliasOrName) {
        if (models.containsKey(aliasOrName)) {
            return models.get(aliasOrName);
        }
        return new Model(aliasOrName);
    }

    public String generateResponse(String prompt) {
        return generateResponse(activeModel, prompt, null);
    }

    public String generateResponse(String prompt, GenerationConfig generationConfig) {
        return generateResponse(activeModel, prompt, generationConfig);
    }

    public String generateResponse(String aliasOrModel, String prompt) {
        return generateResponse(resolveModel(aliasOrModel), prompt, null);
    }

    public String generateResponse(String aliasOrModel, String prompt, GenerationConfig generationConfig) {
        return generateResponse(resolveModel(aliasOrModel), prompt, generationConfig);
    }

    public String generateResponse(Model model, String prompt) {
        return generateResponse(model, prompt, null);
    }

    public String generateResponse(Model model, String prompt, GenerationConfig generationConfig) {
        ChatResponse response = chatResponse(model, prompt, generationConfig);
        return response != null ? response.getContent() : null;
    }

    public ChatResponse chatResponse(String prompt) {
        return chatResponse(activeModel, prompt, null);
    }

    public ChatResponse chatResponse(String prompt, GenerationConfig generationConfig) {
        return chatResponse(activeModel, prompt, generationConfig);
    }

    public ChatResponse chatResponse(String aliasOrModel, String prompt) {
        return chatResponse(resolveModel(aliasOrModel), prompt, null);
    }

    public ChatResponse chatResponse(String aliasOrModel, String prompt, GenerationConfig generationConfig) {
        return chatResponse(resolveModel(aliasOrModel), prompt, generationConfig);
    }

    public ChatResponse chatResponse(Model model, String prompt) {
        return chatResponse(model, prompt, null);
    }

    public ChatResponse chatResponse(Model model, String prompt, GenerationConfig generationConfig) {
        Model modelToUse = model != null ? model : activeModel;
        if (modelToUse == null) {
            modelToUse = new Model("gemini-3.6-flash", Provider.GEMINI);
        }

        String modelName = modelToUse.getName();
        Provider provider = modelToUse.getProvider();
        String customApiKey = modelToUse.getApiKey();

        // Effective GenerationConfig priority: explicit call config > model-level config
        GenerationConfig effectiveConfig = generationConfig != null ? generationConfig : modelToUse.getGenerationConfig();

        if (customApiKey != null && !customApiKey.isEmpty()) {
            AiClient.Builder builder = AiClient.builder();
            if (provider != null) {
                switch (provider) {
                    case OPENAI -> builder.openAiApiKey(customApiKey);
                    case GEMINI -> builder.geminiApiKey(customApiKey);
                    case ANTHROPIC -> builder.anthropicApiKey(customApiKey);
                    case GROK -> builder.grokApiKey(customApiKey);
                    case NIM -> builder.nimApiKey(customApiKey);
                    case KIMI -> builder.kimiApiKey(customApiKey);
                    default -> {}
                }
                return builder.build().chatResponse(modelName, prompt);
            } else {
                return builder.build().chatResponse(modelName, prompt);
            }
        }

        if (aiClient != null) {
            return aiClient.chatResponse(modelName, prompt);
        }

        if (effectiveConfig != null && (effectiveConfig.getTemperature() != null || effectiveConfig.getMaxTokens() != null)) {
            ChatRequest.Builder reqBuilder = ChatRequest.builder()
                    .prompt(prompt)
                    .model(modelName);
            if (effectiveConfig.getTemperature() != null) {
                reqBuilder.temperature(effectiveConfig.getTemperature());
            }
            if (effectiveConfig.getMaxTokens() != null) {
                reqBuilder.maxTokens(effectiveConfig.getMaxTokens());
            }
            return ModelRouter.route(modelName).callResponse(reqBuilder.build());
        }

        return new ChatResponse(AiClient.chatQuick(modelName, prompt), modelName, 0);
    }
}
