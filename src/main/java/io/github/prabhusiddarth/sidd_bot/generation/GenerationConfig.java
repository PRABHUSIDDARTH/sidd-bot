package io.github.prabhusiddarth.sidd_bot.generation;

import java.util.Objects;

/**
 * Configuration parameters for AI model text generation (e.g. temperature, maxTokens, topP).
 */
public class GenerationConfig {
    private final Double temperature;
    private final Integer maxTokens;
    private final Double topP;

    public GenerationConfig() {
        this(null, null, null);
    }

    public GenerationConfig(Double temperature) {
        this(temperature, null, null);
    }

    public GenerationConfig(Double temperature, Integer maxTokens) {
        this(temperature, maxTokens, null);
    }

    public GenerationConfig(Double temperature, Integer maxTokens, Double topP) {
        this.temperature = temperature;
        this.maxTokens = maxTokens;
        this.topP = topP;
    }

    public Double getTemperature() {
        return temperature;
    }

    public Integer getMaxTokens() {
        return maxTokens;
    }

    public Double getTopP() {
        return topP;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private Double temperature;
        private Integer maxTokens;
        private Double topP;

        public Builder temperature(Double temperature) {
            this.temperature = temperature;
            return this;
        }

        public Builder maxTokens(Integer maxTokens) {
            this.maxTokens = maxTokens;
            return this;
        }

        public Builder topP(Double topP) {
            this.topP = topP;
            return this;
        }

        public GenerationConfig build() {
            return new GenerationConfig(temperature, maxTokens, topP);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        GenerationConfig config = (GenerationConfig) o;
        return Objects.equals(temperature, config.temperature) &&
               Objects.equals(maxTokens, config.maxTokens) &&
               Objects.equals(topP, config.topP);
    }

    @Override
    public int hashCode() {
        return Objects.hash(temperature, maxTokens, topP);
    }

    @Override
    public String toString() {
        return "GenerationConfig{" +
                "temperature=" + temperature +
                ", maxTokens=" + maxTokens +
                ", topP=" + topP +
                '}';
    }
}
