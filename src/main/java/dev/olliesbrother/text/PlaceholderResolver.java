package dev.olliesbrother.text;

import dev.olliesbrother.config.AnnouncementConfig;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.PlayerManager;

public final class PlaceholderResolver {

    private PlaceholderResolver() {
        // Utility class
    }

    public static String resolve(
            String input,
            MinecraftServer server,
            AnnouncementConfig announcement
    ) {
        if (input == null || input.isEmpty()) {
            return "";
        }

        PlayerManager playerManager =
                server.getPlayerManager();

        return input
                .replace(
                        "{online}",
                        Integer.toString(
                                playerManager.getCurrentPlayerCount()
                        )
                )
                .replace(
                        "{max_players}",
                        Integer.toString(
                                playerManager.getMaxPlayerCount()
                        )
                )
                .replace(
                        "{server_name}",
                        server.getServerMotd()
                )
                .replace(
                        "{uptime}",
                        formatTicks(server.getTicks())
                )
                .replace(
                        "{announcement_id}",
                        announcement.id
                );
    }

    private static String formatTicks(int ticks) {
        long totalSeconds = ticks / 20L;

        long days = totalSeconds / 86_400L;
        long hours = (totalSeconds % 86_400L) / 3_600L;
        long minutes = (totalSeconds % 3_600L) / 60L;
        long seconds = totalSeconds % 60L;

        if (days > 0) {
            return days + "d " +
                    hours + "h " +
                    minutes + "m";
        }

        if (hours > 0) {
            return hours + "h " +
                    minutes + "m";
        }

        if (minutes > 0) {
            return minutes + "m " +
                    seconds + "s";
        }

        return seconds + "s";
    }

    public static String resolvePlayerMessage(
            String input,
            MinecraftServer server,
            String messageId,
            String playerName,
            String playerUuid,
            boolean firstJoin
    ) {
        if (input == null || input.isEmpty()) {
            return "";
        }

        PlayerManager playerManager =
                server.getPlayerManager();

        return input
                .replace(
                        "{online}",
                        Integer.toString(
                                playerManager.getCurrentPlayerCount()
                        )
                )
                .replace(
                        "{max_players}",
                        Integer.toString(
                                playerManager.getMaxPlayerCount()
                        )
                )
                .replace(
                        "{server_name}",
                        server.getServerMotd()
                )
                .replace(
                        "{uptime}",
                        formatTicks(server.getTicks())
                )
                .replace(
                        "{message_id}",
                        messageId
                )
                .replace(
                        "{announcement_id}",
                        messageId
                )
                .replace(
                        "{player}",
                        playerName
                )
                .replace(
                        "{player_uuid}",
                        playerUuid
                )
                .replace(
                        "{first_join}",
                        Boolean.toString(firstJoin)
                );
    }
}