package io.github.prabhusiddarth.sidd_bot.generation;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class GenerationConfigTest {

    @Test
    public void testDefaultConstructor() {
        GenerationConfig config = new GenerationConfig();
        assertNull(config.getTemperature());
        assertNull(config.getMaxTokens());
        assertNull(config.getTopP());
    }

    @Test
    public void testTemperatureConstructor() {
        GenerationConfig config = new GenerationConfig(0.7);
        assertEquals(0.7, config.getTemperature());
        assertNull(config.getMaxTokens());
        assertNull(config.getTopP());
    }

    @Test
    public void testBuilder() {
        GenerationConfig config = GenerationConfig.builder()
                .temperature(0.9)
                .maxTokens(500)
                .topP(0.95)
                .build();

        assertEquals(0.9, config.getTemperature());
        assertEquals(500, config.getMaxTokens());
        assertEquals(0.95, config.getTopP());
    }

    @Test
    public void testEqualsAndHashCode() {
        GenerationConfig config1 = GenerationConfig.builder().temperature(0.7).maxTokens(100).build();
        GenerationConfig config2 = GenerationConfig.builder().temperature(0.7).maxTokens(100).build();

        assertEquals(config1, config2);
        assertEquals(config1.hashCode(), config2.hashCode());
    }
}
