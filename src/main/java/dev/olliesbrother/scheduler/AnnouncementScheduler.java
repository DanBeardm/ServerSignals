package dev.olliesbrother.scheduler;

import dev.olliesbrother.ServerSignals;
import dev.olliesbrother.config.AnnouncementConfig;
import dev.olliesbrother.config.ConfigManager;
import dev.olliesbrother.util.DurationParser;
import dev.olliesbrother.delivery.AnnouncementDeliveryService;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.server.MinecraftServer;

import java.util.HashMap;
import java.util.Map;

public final class AnnouncementScheduler {
    private static final Map<String, Long> TICKS_REMAINING =
            new HashMap<>();

    private static boolean registered;

    private AnnouncementScheduler() {
        // Utility class
    }

    public static void register() {
        if (registered) {
            return;
        }

        registered = true;

        resetAll();

        ServerTickEvents.END_SERVER_TICK.register(
                AnnouncementScheduler::tick
        );
    }

    public static void resetAll() {
        TICKS_REMAINING.clear();

        for (AnnouncementConfig announcement :
                ConfigManager.getConfig().announcements) {

            if (!announcement.enabled) {
                continue;
            }

            TICKS_REMAINING.put(
                    announcement.id,
                    DurationParser.toTicks(announcement.interval)
            );
        }

        ServerSignals.LOGGER.info(
                "Reset {} enabled announcement timers.",
                TICKS_REMAINING.size()
        );
    }

    public static boolean broadcastNow(
            MinecraftServer server,
            String announcementId
    ) {
        for (AnnouncementConfig announcement :
                ConfigManager.getConfig().announcements) {

            if (announcement.id.equals(announcementId)) {
                broadcast(server, announcement);
                return true;
            }
        }

        return false;
    }

    private static void tick(MinecraftServer server) {
        for (AnnouncementConfig announcement :
                ConfigManager.getConfig().announcements) {

            if (!announcement.enabled) {
                continue;
            }

            long ticksRemaining =
                    TICKS_REMAINING.getOrDefault(
                            announcement.id,
                            DurationParser.toTicks(announcement.interval)
                    );

            ticksRemaining--;

            if (ticksRemaining > 0) {
                TICKS_REMAINING.put(
                        announcement.id,
                        ticksRemaining
                );

                continue;
            }

            TICKS_REMAINING.put(
                    announcement.id,
                    DurationParser.toTicks(announcement.interval)
            );

            if (announcement.onlyWhenPlayersOnline &&
                    server.getPlayerManager()
                            .getCurrentPlayerCount() == 0) {
                continue;
            }

            broadcast(server, announcement);
        }
    }

    private static void broadcast(
            MinecraftServer server,
            AnnouncementConfig announcement
    ) {
        AnnouncementDeliveryService.deliver(
                server,
                announcement
        );
    }
}