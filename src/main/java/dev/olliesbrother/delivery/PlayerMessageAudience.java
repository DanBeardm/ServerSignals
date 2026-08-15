package dev.olliesbrother.delivery;

import java.util.Locale;

public enum PlayerMessageAudience {
    BROADCAST("broadcast"),
    PLAYER_ONLY("player_only");

    private final String configName;

    PlayerMessageAudience(String configName) {
        this.configName = configName;
    }

    public String getConfigName() {
        return configName;
    }

    public static PlayerMessageAudience fromConfig(
            String value
    ) {
        if (value == null || value.isBlank()) {
            return BROADCAST;
        }

        String normalized =
                value.trim().toLowerCase(Locale.ROOT);

        for (PlayerMessageAudience audience : values()) {
            if (audience.configName.equals(normalized)) {
                return audience;
            }
        }

        throw new IllegalArgumentException(
                "Unknown player-message audience: " + value
        );
    }
}