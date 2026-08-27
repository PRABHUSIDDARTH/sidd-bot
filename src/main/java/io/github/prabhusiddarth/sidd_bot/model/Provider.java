package io.github.prabhusiddarth.sidd_bot.model;

public enum Provider {
    OPENAI("openai"),
    GEMINI("gemini"),
    ANTHROPIC("anthropic"),
    GROK("grok"),
    NIM("nim"),
    KIMI("kimi"),
    OLLAMA("ollama"),
    CUSTOM("custom");

    private final String code;

    Provider(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }
}
