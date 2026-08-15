package dev.olliesbrother.delivery;

import dev.olliesbrother.config.RestartConfig;
import dev.olliesbrother.config.TitleTimingConfig;
import dev.olliesbrother.text.AnnouncementTextFactory;
import dev.olliesbrother.text.RestartPlaceholderResolver;
import net.minecraft.network.packet.s2c.play.SubtitleS2CPacket;
import net.minecraft.network.packet.s2c.play.TitleFadeS2CPacket;
import net.minecraft.network.packet.s2c.play.TitleS2CPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

public final class RestartWarningDeliveryService {

    private RestartWarningDeliveryService() {
        // Utility class
    }

    public static Text createMessage(
            MinecraftServer server,
            RestartConfig config,
            long remainingSeconds,
            long endEpochMillis
    ) {
        return AnnouncementTextFactory.create(
                config.sections,
                input ->
                        RestartPlaceholderResolver.resolve(
                                input,
                                server,
                                remainingSeconds,
                                endEpochMillis
                        )
        );
    }

    public static void deliver(
            MinecraftServer server,
            RestartConfig config,
            long remainingSeconds,
            long endEpochMillis
    ) {
        Text message =
                createMessage(
                        server,
                        config,
                        remainingSeconds,
                        endEpochMillis
                );

        DeliveryMode deliveryMode =
                DeliveryMode.fromConfig(
                        config.warningDelivery
                );

        switch (deliveryMode) {
            case CHAT ->
                    deliverChat(server, message);

            case ACTION_BAR ->
                    deliverActionBar(server, message);

            case TITLE ->
                    deliverTitle(
                            server,
                            message,
                            config.titleTiming
                    );

            case SUBTITLE ->
                    deliverSubtitle(
                            server,
                            message,
                            config.titleTiming
                    );

            case BOSS_BAR ->
                    throw new IllegalStateException(
                            "Restart warnings cannot use boss_bar."
                    );
        }
    }

    private static void deliverChat(
            MinecraftServer server,
            Text message
    ) {
        server.getPlayerManager().broadcast(
                message,
                false
        );
    }

    private static void deliverActionBar(
            MinecraftServer server,
            Text message
    ) {
        for (ServerPlayerEntity player :
                server.getPlayerManager().getPlayerList()) {

            player.sendMessage(message, true);
        }
    }

    private static void deliverTitle(
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

    private static void deliverSubtitle(
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