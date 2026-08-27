package io.github.prabhusiddarth.sidd_bot.security;

import java.util.List;
import java.util.regex.Pattern;

public class PromptGuard {
    private static final List<Pattern> SUSPICIOUS_PATTERNS = List.of(
            Pattern.compile("ignore previous instructions", Pattern.CASE_INSENSITIVE),
            Pattern.compile("system prompt override", Pattern.CASE_INSENSITIVE),
            Pattern.compile("you are now dan", Pattern.CASE_INSENSITIVE),
            Pattern.compile("jailbreak", Pattern.CASE_INSENSITIVE)
    );

    public boolean isSafe(String prompt) {
        if (prompt == null || prompt.isBlank()) {
            return true;
        }
        for (Pattern pattern : SUSPICIOUS_PATTERNS) {
            if (pattern.matcher(prompt).find()) {
                return false;
            }
        }
        return true;
    }

    public String sanitize(String prompt) {
        if (prompt == null) {
            return "";
        }
        return prompt.trim();
    }
}
