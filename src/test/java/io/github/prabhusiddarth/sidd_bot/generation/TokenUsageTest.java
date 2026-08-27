package io.github.prabhusiddarth.sidd_bot.generation;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class TokenUsageTest {

    @Test
    public void testTokenUsageCreation() {
        TokenUsage usage = TokenUsage.of(15, 25, 40);

        assertEquals(15, usage.getPromptTokens());
        assertEquals(25, usage.getCompletionTokens());
        assertEquals(40, usage.getTotalTokens());
    }

    @Test
    public void testTotalTokensOnly() {
        TokenUsage usage = TokenUsage.of(100);

        assertEquals(0, usage.getPromptTokens());
        assertEquals(0, usage.getCompletionTokens());
        assertEquals(100, usage.getTotalTokens());
    }

    @Test
    public void testEqualsAndHashCode() {
        TokenUsage usage1 = TokenUsage.of(10, 20, 30);
        TokenUsage usage2 = TokenUsage.of(10, 20, 30);

        assertEquals(usage1, usage2);
        assertEquals(usage1.hashCode(), usage2.hashCode());
    }
}
