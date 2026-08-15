package dev.olliesbrother.text;

import dev.olliesbrother.util.DurationFormatter;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.PlayerManager;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

public final class RestartPlaceholderResolver {
    private static final DateTimeFormatter TIME_FORMAT =
            DateTimeFormatter.ofPattern("HH:mm:ss");

    private RestartPlaceholderResolver() {
        // Utility class
    }

    public static String resolve(
            String input,
            MinecraftServer server,
            long remainingSeconds,
            long endEpochMillis
    ) {
        if (input == null || input.isEmpty()) {
            return "";
        }

        PlayerManager playerManager =
                server.getPlayerManager();

        String endTime =
                Instant.ofEpochMilli(endEpochMillis)
                        .atZone(ZoneId.systemDefault())
                        .format(TIME_FORMAT);

        return input
                .replace(
                        "{restart_remaining}",
                        DurationFormatter.formatSeconds(
                                remainingSeconds
                        )
                )
                .replace(
                        "{restart_remaining_seconds}",
                        Long.toString(remainingSeconds)
                )
                .replace(
                        "{restart_end_time}",
                        endTime
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