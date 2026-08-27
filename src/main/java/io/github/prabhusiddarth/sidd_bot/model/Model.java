package io.github.prabhusiddarth.sidd_bot.model;

import io.github.prabhusiddarth.sidd_bot.generation.GenerationConfig;

public class Model {
    private final String name;
    private final Provider provider;
    private final String apiKey;
    private final GenerationConfig generationConfig;

    public Model(String name) {
        this(name, null, null, null);
    }

    public Model(String name, Provider provider) {
        this(name, provider, null, null);
    }

    public Model(String name, Provider provider, String apiKey) {
        this(name, provider, apiKey, null);
    }

    public Model(String name, GenerationConfig generationConfig) {
        this(name, null, null, generationConfig);
    }

    public Model(String name, Provider provider, GenerationConfig generationConfig) {
        this(name, provider, null, generationConfig);
    }

    public Model(String name, Provider provider, String apiKey, GenerationConfig generationConfig) {
        this.name = name;
        this.provider = provider;
        this.apiKey = apiKey;
        this.generationConfig = generationConfig;
    }

    public String getName() {
        return name;
    }

    public Provider getProvider() {
        return provider;
    }

    public String getApiKey() {
        return apiKey;
    }

    public GenerationConfig getGenerationConfig() {
        return generationConfig;
    }

    @Override
    public String toString() {
        return "Model{" +
                "name='" + name + '\'' +
                ", provider=" + provider +
                ", generationConfig=" + generationConfig +
                '}';
    }
}
