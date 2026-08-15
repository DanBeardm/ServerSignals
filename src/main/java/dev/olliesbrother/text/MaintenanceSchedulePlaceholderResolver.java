package dev.olliesbrother.text;

import dev.olliesbrother.util.DurationFormatter;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.PlayerManager;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

public final class MaintenanceSchedulePlaceholderResolver {

    private static final DateTimeFormatter TIME_FORMAT =
            DateTimeFormatter.ofPattern("HH:mm:ss");

    private MaintenanceSchedulePlaceholderResolver() {
        // Utility class
    }

    public static String resolve(
            String input,
            MinecraftServer server,
            String reason,
            long remainingSeconds,
            long startEpochMillis
    ) {
        if (input == null || input.isEmpty()) {
            return "";
        }

        PlayerManager playerManager =
                server.getPlayerManager();

        String startTime =
                Instant.ofEpochMilli(startEpochMillis)
                        .atZone(ZoneId.systemDefault())
                        .format(TIME_FORMAT);

        return input
                .replace(
                        "{reason}",
                        reason
                )
                .replace(
                        "{maintenance_remaining}",
                        DurationFormatter.formatSeconds(
                                remainingSeconds
                        )
                )
                .replace(
                        "{maintenance_remaining_seconds}",
                        Long.toString(remainingSeconds)
                )
                .replace(
                        "{maintenance_start_time}",
                        startTime
                )
                .replace(
                        "{online}",
                        Integer.toString(
                                playerManager
                                        .getCurrentPlayerCount()
                        )
                )
                .replace(
                        "{max_players}",
                        Integer.toString(
                                playerManager
                                        .getMaxPlayerCount()
                        )
                )
                .replace(
                        "{server_name}",
                        server.getServerMotd()
                )
                .replace(
                        "{uptime}",
                        DurationFormatter.formatSeconds(
                                server.getTicks() / 20L
                        )
                );
    }
}