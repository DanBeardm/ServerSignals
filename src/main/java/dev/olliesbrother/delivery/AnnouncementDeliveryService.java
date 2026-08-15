package dev.olliesbrother.delivery;

import dev.olliesbrother.ServerSignals;
import dev.olliesbrother.config.AnnouncementConfig;
import dev.olliesbrother.config.TitleTimingConfig;
import dev.olliesbrother.text.AnnouncementTextFactory;
import net.minecraft.network.packet.s2c.play.SubtitleS2CPacket;
import net.minecraft.network.packet.s2c.play.TitleFadeS2CPacket;
import net.minecraft.network.packet.s2c.play.TitleS2CPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

public final class AnnouncementDeliveryService {

    private AnnouncementDeliveryService() {
        // Utility class
    }

    public static void deliver(
            MinecraftServer server,
            AnnouncementConfig announcement
    ) {
        Text message =
                AnnouncementTextFactory.create(
                        server,
                        announcement
                );

        DeliveryMode deliveryMode =
                DeliveryMode.fromConfig(
                        announcement.delivery
                );

        switch (deliveryMode) {
            case CHAT ->
                    deliverToChat(
                            server,
                            message
                    );

            case ACTION_BAR ->
                    deliverToActionBar(
                            server,
                            message
                    );

            case TITLE ->
                    deliverAsTitle(
                            server,
                            message,
                            announcement.titleTiming
                    );

            case SUBTITLE ->
                    deliverAsSubtitle(
                            server,
                            message,
                            announcement.titleTiming
                    );

            case BOSS_BAR ->
                    BossBarDeliveryService.show(
                            server,
                            announcement,
                            message
                    );
        }

        ServerSignals.LOGGER.info(
                "Sent announcement '{}' using delivery mode '{}'.",
                announcement.id,
                deliveryMode.getConfigName()
        );
    }

    private static void deliverToChat(
            MinecraftServer server,
            Text message
    ) {
        server.getPlayerManager().broadcast(
                message,
                false
        );
    }

    private static void deliverToActionBar(
            MinecraftServer server,
            Text message
    ) {
        for (ServerPlayerEntity player :
                server.getPlayerManager().getPlayerList()) {

            player.sendMessage(
                    message,
                    true
            );
        }
    }

    private static void deliverAsTitle(
            MinecraftServer server,
            Text message,
            TitleTimingConfig timing
    ) {
        for (ServerPlayerEntity player :
                server.getPlayerManager().getPlayerList()) {

            sendTitleTiming(player, timing);

            player.networkHandler.sendPacket(
                    new TitleS2CPacket(message)
            );
        }
    }

    private static void deliverAsSubtitle(
            MinecraftServer server,
            Text message,
            TitleTimingConfig timing
    ) {
        for (ServerPlayerEntity player :
                server.getPlayerManager().getPlayerList()) {

            sendTitleTiming(player, timing);

            player.networkHandler.sendPacket(
                    new SubtitleS2CPacket(message)
            );

            // A title packet triggers the title display.
            // The title itself is intentionally empty.
            player.networkHandler.sendPacket(
                    new TitleS2CPacket(Text.empty())
            );
        }
    }

    private static void sendTitleTiming(
            ServerPlayerEntity player,
            TitleTimingConfig timing
    ) {
        player.networkHandler.sendPacket(
                new TitleFadeS2CPacket(
                        timing.fadeInTicks,
                        timing.stayTicks,
                        timing.fadeOutTicks
                )
        );
    }
}