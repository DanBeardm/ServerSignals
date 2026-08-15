package dev.olliesbrother.delivery;

import java.util.Locale;

public enum DeliveryMode {
    CHAT("chat"),
    ACTION_BAR("action_bar"),
    TITLE("title"),
    SUBTITLE("subtitle"),
    BOSS_BAR("boss_bar");

    private final String configName;

    DeliveryMode(String configName) {
        this.configName = configName;
    }

    public String getConfigName() {
        return configName;
    }

    public static DeliveryMode fromConfig(String value) {
        if (value == null || value.isBlank()) {
            return CHAT;
        }

        String normalized =
                value.trim().toLowerCase(Locale.ROOT);

        for (DeliveryMode mode : values()) {
            if (mode.configName.equals(normalized)) {
                return mode;
            }
        }

        throw new IllegalArgumentException(
                "Unknown delivery mode: " + value
        );
    }
}