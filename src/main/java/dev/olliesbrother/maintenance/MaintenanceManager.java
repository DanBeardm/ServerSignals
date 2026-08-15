package dev.olliesbrother.maintenance;

import com.mojang.authlib.GameProfile;
import dev.olliesbrother.ServerSignals;
import dev.olliesbrother.config.ConfigManager;
import dev.olliesbrother.config.MaintenanceConfig;
import dev.olliesbrother.text.MaintenanceMessageFactory;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

import java.util.List;

public final class MaintenanceManager {

    private MaintenanceManager() {
        // Utility class
    }

    public static boolean isEnabled() {
        return ConfigManager
                .getConfig()
                .maintenance
                .enabled;
    }

    public static boolean canBypass(
            MinecraftServer server,
            GameProfile profile
    ) {
        MaintenanceConfig config =
                ConfigManager
                        .getConfig()
                        .maintenance;

        if (profile == null) {
            return false;
        }

        /*
         * Operators may bypass maintenance mode.
         */
        if (config.allowOperators &&
                server.getPlayerManager()
                        .isOperator(profile)) {

            return true;
        }

        String name =
                profile.getName();

        String uuid =
                profile.getId() == null
                        ? ""
                        : profile.getId().toString();

        for (String allowed :
                config.allowedPlayers) {

            if (allowed.equalsIgnoreCase(name) ||
                    allowed.equalsIgnoreCase(uuid)) {

                return true;
            }
        }

        return false;
    }

    public static EnableResult enable(
            MinecraftServer server,
            String reasonOverride
    ) {
        MaintenanceConfig config =
                ConfigManager
                        .getConfig()
                        .maintenance;

        boolean previousEnabled =
                config.enabled;

        String previousReason =
                config.reason;

        config.enabled = true;

        if (reasonOverride != null &&
                !reasonOverride.isBlank()) {

            config.reason =
                    reasonOverride.trim();
        }

        /*
         * Persist maintenance state.
         */
        if (!ConfigManager.saveCurrent()) {
            config.enabled =
                    previousEnabled;

            config.reason =
                    previousReason;

            return new EnableResult(
                    false,
                    0
            );
        }

        int kickedPlayers = 0;

        if (config.kickOnlinePlayersOnEnable) {
            kickedPlayers =
                    kickNonBypassPlayers(server);
        }

        ServerSignals.LOGGER.info(
                "Maintenance mode enabled. Reason: {}",
                config.reason
        );

        return new EnableResult(
                true,
                kickedPlayers
        );
    }

    public static boolean disable() {
        MaintenanceConfig config =
                ConfigManager
                        .getConfig()
                        .maintenance;

        boolean previousEnabled =
                config.enabled;

        config.enabled = false;

        if (!ConfigManager.saveCurrent()) {
            config.enabled =
                    previousEnabled;

            return false;
        }

        ServerSignals.LOGGER.info(
                "Maintenance mode disabled."
        );

        return true;
    }

    public static int kickNonBypassPlayers(
            MinecraftServer server
    ) {
        MaintenanceConfig config =
                ConfigManager
                        .getConfig()
                        .maintenance;

        List<ServerPlayerEntity> players =
                List.copyOf(
                        server.getPlayerManager()
                                .getPlayerList()
                );

        int kicked = 0;

        for (ServerPlayerEntity player :
                players) {

            if (canBypass(
                    server,
                    player.getGameProfile()
            )) {
                continue;
            }

            Text disconnectMessage =
                    MaintenanceMessageFactory.create(
                            config,
                            player.getGameProfile()
                    );

            player.networkHandler.disconnect(
                    disconnectMessage
            );

            kicked++;
        }

        ServerSignals.LOGGER.info(
                "Kicked {} player(s) for maintenance.",
                kicked
        );

        return kicked;
    }

    public record EnableResult(
            boolean success,
            int kickedPlayers
    ) {
    }
}