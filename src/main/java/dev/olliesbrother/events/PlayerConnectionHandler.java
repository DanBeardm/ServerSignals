package dev.olliesbrother.events;

import dev.olliesbrother.ServerSignals;
import dev.olliesbrother.config.ConfigManager;
import dev.olliesbrother.config.PlayerMessagesConfig;
import dev.olliesbrother.data.SeenPlayerStore;
import dev.olliesbrother.delivery.PlayerMessageDeliveryService;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.server.network.ServerPlayerEntity;

public final class PlayerConnectionHandler {
    private static boolean registered;

    private PlayerConnectionHandler() {
        // Utility class
    }

    public static void register() {
        if (registered) {
            return;
        }

        registered = true;

        ServerPlayConnectionEvents.JOIN.register(
                (handler, sender, server) -> {
                    ServerPlayerEntity player =
                            handler.getPlayer();

                    boolean firstJoin =
                            SeenPlayerStore.markSeen(
                                    player.getUuid()
                            );

                    PlayerMessagesConfig messages =
                            ConfigManager.getConfig()
                                    .playerMessages;

                    if (firstJoin) {
                        PlayerMessageDeliveryService.deliver(
                                server,
                                "first_join",
                                messages.firstJoin,
                                player,
                                true,
                                true
                        );

                        if (messages.sendJoinOnFirstJoin) {
                            PlayerMessageDeliveryService.deliver(
                                    server,
                                    "join",
                                    messages.join,
                                    player,
                                    true,
                                    true
                            );
                        }

                        ServerSignals.LOGGER.info(
                                "Recorded first join for {}.",
                                player.getGameProfile()
                                        .getName()
                        );

                        return;
                    }

                    PlayerMessageDeliveryService.deliver(
                            server,
                            "join",
                            messages.join,
                            player,
                            false,
                            true
                    );
                }
        );

        ServerPlayConnectionEvents.DISCONNECT.register(
                (handler, server) -> {
                    ServerPlayerEntity player =
                            handler.getPlayer();

                    PlayerMessagesConfig messages =
                            ConfigManager.getConfig()
                                    .playerMessages;

                    PlayerMessageDeliveryService.deliver(
                            server,
                            "leave",
                            messages.leave,
                            player,
                            false,
                            false
                    );
                }
        );
    }
}