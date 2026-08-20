package dev.olliesbrother.delivery;

import dev.olliesbrother.ServerSignals;
import dev.olliesbrother.config.PlayerMessageConfig;
import dev.olliesbrother.config.TitleTimingConfig;
import dev.olliesbrother.text.AnnouncementTextFactory;
import dev.olliesbrother.text.PlaceholderResolver;
import net.minecraft.network.packet.s2c.play.SubtitleS2CPacket;
import net.minecraft.network.packet.s2c.play.TitleFadeS2CPacket;
import net.minecraft.network.packet.s2c.play.TitleS2CPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.List;

public final class PlayerMessageDeliveryService {

    private PlayerMessageDeliveryService() {
        // Utility class
    }

    public static boolean deliver(
            MinecraftServer server,
            String messageId,
            PlayerMessageConfig config,
            ServerPlayerEntity subject,
            boolean firstJoin,
            boolean subjectIsConnected
    ) {
        if (config == null) {
            return false;
        }

        List<ServerPlayerEntity> recipients =
                getRecipients(
                        server,
                        config,
                        subject,
                        subjectIsConnected
                );

        if (recipients.isEmpty()) {
            return false;
        }

        String playerName =
                subject.getGameProfile().getName();

        String playerUuid =
                subject.getGameProfile()
                        .getId()
                        .toString();

        Text message =
                AnnouncementTextFactory.create(
                        config.sections,
                        input ->
                                PlaceholderResolver
                                        .resolvePlayerMessage(
                                                input,
                                                server,
                                                messageId,
                                                playerName,
                                                playerUuid,
                                                firstJoin
                                        )
                );

        DeliveryMode mode =
                DeliveryMode.fromConfig(
                        config.delivery
                );

        switch (mode) {
            case CHAT ->
                    sendChat(recipients, message);

            case ACTION_BAR ->
                    sendActionBar(recipients, message);

            case TITLE ->
                    sendTitle(
                            recipients,
                            message,
                            config.titleTiming
                    );

            case SUBTITLE ->
                    sendSubtitle(
                            recipients,
                            message,
                            config.titleTiming
                    );

            case BOSS_BAR -> {
                ServerSignals.LOGGER.warn(
                        "Player message '{}' requested boss_bar, " +
                                "but targeted player-message boss bars " +
                                "are not implemented yet.",
                        messageId
                );

                return false;
            }
        }

        ServerSignals.LOGGER.info(
                "Delivered player message '{}' to {} player(s).",
                messageId,
                recipients.size()
        );

        return true;
    }

    private static List<ServerPlayerEntity> getRecipients(
            MinecraftServer server,
            PlayerMessageConfig config,
            ServerPlayerEntity subject,
            boolean subjectIsConnected
    ) {
        PlayerMessageAudience audience =
                PlayerMessageAudience.fromConfig(
                        config.audience
                );

        if (audience ==
                PlayerMessageAudience.PLAYER_ONLY) {

            if (!subjectIsConnected) {
                return List.of();
            }

            return List.of(subject);
        }

        List<ServerPlayerEntity> recipients =
                new ArrayList<>(
                        server.getPlayerManager()
                                .getPlayerList()
                );

        if (!subjectIsConnected) {
            recipients.removeIf(player ->
                    player.getUuid().equals(
                            subject.getUuid()
                    )
            );
        }

        return recipients;
    }

    private static void sendChat(
            List<ServerPlayerEntity> recipients,
            Text message
    ) {
        for (ServerPlayerEntity player : recipients) {
            player.sendMessage(message, false);
        }
    }

    private static void sendActionBar(
            List<ServerPlayerEntity> recipients,
            Text message
    ) {
        for (ServerPlayerEntity player : recipients) {
            player.sendMessage(message, true);
        }
    }

    private static void sendTitle(
            List<ServerPlayerEntity> recipients,
            Text message,
            TitleTimingConfig timing
    ) {
        for (ServerPlayerEntity player : recipients) {
            sendTiming(player, timing);

            player.networkHandler.sendPacket(
                    new TitleS2CPacket(message)
            );
        }
    }

    private static void sendSubtitle(
            List<ServerPlayerEntity> recipients,
            Text message,
            TitleTimingConfig timing
    ) {
        for (ServerPlayerEntity player : recipients) {
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