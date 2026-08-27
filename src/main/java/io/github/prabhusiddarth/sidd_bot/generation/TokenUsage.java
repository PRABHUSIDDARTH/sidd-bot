package io.github.prabhusiddarth.sidd_bot.generation;

import java.util.Objects;

/**
 * Token usage metrics for a single request/response cycle.
 */
public class TokenUsage {
    private final int promptTokens;
    private final int completionTokens;
    private final int totalTokens;

    public TokenUsage(int totalTokens) {
        this(0, 0, totalTokens);
    }

    public TokenUsage(int promptTokens, int completionTokens, int totalTokens) {
        this.promptTokens = promptTokens;
        this.completionTokens = completionTokens;
        this.totalTokens = totalTokens;
    }

    public int getPromptTokens() {
        return promptTokens;
    }

    public int getCompletionTokens() {
        return completionTokens;
    }

    public int getTotalTokens() {
        return totalTokens;
    }

    public static TokenUsage of(int totalTokens) {
        return new TokenUsage(totalTokens);
    }

    public static TokenUsage of(int promptTokens, int completionTokens, int totalTokens) {
        return new TokenUsage(promptTokens, completionTokens, totalTokens);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TokenUsage that = (TokenUsage) o;
        return promptTokens == that.promptTokens &&
               completionTokens == that.completionTokens &&
               totalTokens == that.totalTokens;
    }

    @Override
    public int hashCode() {
        return Objects.hash(promptTokens, completionTokens, totalTokens);
    }

    @Override
    public String toString() {
        return "TokenUsage{" +
                "promptTokens=" + promptTokens +
                ", completionTokens=" + completionTokens +
                ", totalTokens=" + totalTokens +
                '}';
    }
}
