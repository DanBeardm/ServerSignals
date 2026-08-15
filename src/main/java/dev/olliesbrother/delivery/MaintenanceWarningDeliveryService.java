package dev.olliesbrother.delivery;

import dev.olliesbrother.config.MaintenanceScheduleConfig;
import dev.olliesbrother.config.TitleTimingConfig;
import dev.olliesbrother.text.AnnouncementTextFactory;
import dev.olliesbrother.text.MaintenanceSchedulePlaceholderResolver;
import net.minecraft.network.packet.s2c.play.SubtitleS2CPacket;
import net.minecraft.network.packet.s2c.play.TitleFadeS2CPacket;
import net.minecraft.network.packet.s2c.play.TitleS2CPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

public final class MaintenanceWarningDeliveryService {

    private MaintenanceWarningDeliveryService() {
    }

    public static Text createMessage(
            MinecraftServer server,
            MaintenanceScheduleConfig config,
            String reason,
            long remainingSeconds,
            long startEpochMillis
    ) {
        return AnnouncementTextFactory.create(
                config.sections,
                input ->
                        MaintenanceSchedulePlaceholderResolver.resolve(
                                input,
                                server,
                                reason,
                                remainingSeconds,
                                startEpochMillis
                        )
        );
    }

    public static void deliver(
            MinecraftServer server,
            MaintenanceScheduleConfig config,
            String reason,
            long remainingSeconds,
            long startEpochMillis
    ) {
        Text message =
                createMessage(
                        server,
                        config,
                        reason,
                        remainingSeconds,
                        startEpochMillis
                );

        DeliveryMode mode =
                DeliveryMode.fromConfig(
                        config.warningDelivery
                );

        switch (mode) {
            case CHAT ->
                    server.getPlayerManager()
                            .broadcast(
                                    message,
                                    false
                            );

            case ACTION_BAR -> {
                for (ServerPlayerEntity player :
                        server.getPlayerManager()
                                .getPlayerList()) {

                    player.sendMessage(
                            message,
                            true
                    );
                }
            }

            case TITLE ->
                    sendTitle(
                            server,
                            message,
                            config.titleTiming
                    );

            case SUBTITLE ->
                    sendSubtitle(
                            server,
                            message,
                            config.titleTiming
                    );

            case BOSS_BAR ->
                    throw new IllegalStateException(
                            "Maintenance warnings cannot use boss_bar."
                    );
        }
    }

    private static void sendTitle(
            MinecraftServer server,
            Text message,
            TitleTimingConfig timing
    ) {
        for (ServerPlayerEntity player :
                server.getPlayerManager().getPlayerList()) {

            sendTiming(player, timing);

            player.networkHandler.sendPacket(
                    new TitleS2CPacket(message)
            );
        }
    }

    private static void sendSubtitle(
            MinecraftServer server,
            Text message,
            TitleTimingConfig timing
    ) {
        for (ServerPlayerEntity player :
                server.getPlayerManager().getPlayerList()) {

            sendTiming(player, timing);

            player.networkHandler.sendPacket(
                    new SubtitleS2CPacket(message)
            );

            player.networkHandler.sendPacket(
                    new TitleS2CPacket(Text.empty())
            );
        }
    }

    private static void sendTiming(
            ServerPlayerEntity player,
            TitleTimingConfig timing
    ) {
        TitleTimingConfig actualTiming =
                timing == null
                        ? new TitleTimingConfig()
                        : timing;

        player.networkHandler.sendPacket(
                new TitleFadeS2CPacket(
                        actualTiming.fadeInTicks,
                        actualTiming.stayTicks,
                        actualTiming.fadeOutTicks
                )
        );
    }
}