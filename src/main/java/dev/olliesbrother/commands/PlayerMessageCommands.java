package dev.olliesbrother.commands;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import dev.olliesbrother.config.ConfigManager;
import dev.olliesbrother.config.PlayerMessageConfig;
import dev.olliesbrother.config.PlayerMessagesConfig;
import dev.olliesbrother.delivery.PlayerMessageDeliveryService;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

public final class PlayerMessageCommands {

    private PlayerMessageCommands() {
        // Utility class
    }

    public static LiteralArgumentBuilder<ServerCommandSource>
    build() {
        return CommandManager.literal("player-message")

                .executes(context ->
                        showStatus(
                                context.getSource()
                        )
                )

                .then(
                        CommandManager.literal("test")
                                .then(
                                        createTestBranch(
                                                "join"
                                        )
                                )
                                .then(
                                        createTestBranch(
                                                "first_join"
                                        )
                                )
                                .then(
                                        createTestBranch(
                                                "leave"
                                        )
                                )
                );
    }

    private static LiteralArgumentBuilder<ServerCommandSource>
    createTestBranch(String messageId) {
        return CommandManager.literal(messageId)
                .then(
                        CommandManager.argument(
                                        "player",
                                        EntityArgumentType.player()
                                )
                                .executes(context -> {
                                    ServerPlayerEntity player =
                                            EntityArgumentType.getPlayer(
                                                    context,
                                                    "player"
                                            );

                                    return test(
                                            context.getSource(),
                                            messageId,
                                            player
                                    );
                                })
                );
    }

    private static int showStatus(
            ServerCommandSource source
    ) {
        PlayerMessagesConfig messages =
                ConfigManager.getConfig()
                        .playerMessages;

        source.sendFeedback(
                () -> Text.literal(
                        "Player messages: join=" +
                                status(messages.join) +
                                ", first_join=" +
                                status(messages.firstJoin) +
                                ", leave=" +
                                status(messages.leave)
                ),
                false
        );

        return 1;
    }

    private static int test(
            ServerCommandSource source,
            String messageId,
            ServerPlayerEntity player
    ) {
        PlayerMessagesConfig messages =
                ConfigManager.getConfig()
                        .playerMessages;

        PlayerMessageConfig message =
                switch (messageId) {
                    case "join" -> messages.join;
                    case "first_join" ->
                            messages.firstJoin;
                    case "leave" -> messages.leave;

                    default -> throw new IllegalArgumentException(
                            "Unknown player message: " +
                                    messageId
                    );
                };

        boolean delivered =
                PlayerMessageDeliveryService.deliver(
                        source.getServer(),
                        messageId,
                        message,
                        player,
                        messageId.equals("first_join"),
                        true
                );

        if (!delivered) {
            source.sendError(
                    Text.literal(
                            "The player message is disabled " +
                                    "or had no recipients."
                    )
            );

            return 0;
        }

        source.sendFeedback(
                () -> Text.literal(
                        "Tested player message '" +
                                messageId +
                                "' using player " +
                                player.getGameProfile()
                                        .getName() +
                                "."
                ),
                false
        );

        return 1;
    }

    private static String status(
            PlayerMessageConfig message
    ) {
        return message != null && message.enabled
                ? "enabled"
                : "disabled";
    }
}