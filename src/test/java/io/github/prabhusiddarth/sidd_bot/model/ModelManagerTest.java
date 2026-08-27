package io.github.prabhusiddarth.sidd_bot.model;

import io.github.prabhusiddarth.sidd_ai.AiClient;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ModelManagerTest {
    @Test
    void testRegisterAndSwitchModel() {
        ModelManager manager = new ModelManager();
        Model gpt4 = new Model("gpt-4o", Provider.OPENAI);

        manager.registerModel("gpt4", gpt4);
        assertTrue(manager.setActiveModel("gpt4"));
        assertEquals("gpt-4o", manager.getActiveModel().getName());
        assertEquals(Provider.OPENAI, manager.getActiveModel().getProvider());
    }

    @Test
    void testInvalidModelSwitch() {
        ModelManager manager = new ModelManager();
        assertFalse(manager.setActiveModel("non-existent"));
    }

    @Test
    void testSingleArgModelConstructor() {
        Model model = new Model("claude-3-5-sonnet");
        assertEquals("claude-3-5-sonnet", model.getName());
        assertNull(model.getProvider());
    }

    @Test
    void testResolveModel() {
        ModelManager manager = new ModelManager();
        Model customModel = new Model("grok-3", Provider.GROK);
        manager.registerModel("my-grok", customModel);

        assertEquals(customModel, manager.resolveModel("my-grok"));
        Model autoResolved = manager.resolveModel("claude-3-5-sonnet");
        assertEquals("claude-3-5-sonnet", autoResolved.getName());
    }

    @Test
    void testCustomAiClientIntegration() {
        AiClient customClient = AiClient.builder()
                .defaultModel("gemini-3.6-flash")
                .build();
        ModelManager manager = new ModelManager(customClient);
        assertNotNull(manager.getAiClient());
        assertEquals("gemini-3.6-flash", manager.getActiveModel().getName());
    }
}
