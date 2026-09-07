package io.github.prabhusiddarth.sidd_bot.security;

public class PromptInjectionException extends IllegalArgumentException {
    public PromptInjectionException(String message) {
        super(message);
    }
}
