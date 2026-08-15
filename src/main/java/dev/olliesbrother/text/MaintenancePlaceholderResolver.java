package dev.olliesbrother.text;

import com.mojang.authlib.GameProfile;
import dev.olliesbrother.config.MaintenanceConfig;

public final class MaintenancePlaceholderResolver {

    private MaintenancePlaceholderResolver() {
        // Utility class
    }

    public static String resolve(
            String input,
            MaintenanceConfig config,
            GameProfile profile
    ) {
        if (input == null || input.isEmpty()) {
            return "";
        }

        String playerName =
                profile != null &&
                        profile.getName() != null
                        ? profile.getName()
                        : "Player";

        String playerUuid =
                profile != null &&
                        profile.getId() != null
                        ? profile.getId().toString()
                        : "unknown";

        return input
                .replace(
                        "{reason}",
                        config.reason
                )
                .replace(
                        "{player}",
                        playerName
                )
                .replace(
                        "{player_uuid}",
                        playerUuid
                );
    }
}