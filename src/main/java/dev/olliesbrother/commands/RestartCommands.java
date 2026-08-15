package dev.olliesbrother.commands;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import dev.olliesbrother.ServerSignals;
import dev.olliesbrother.config.ConfigManager;
import dev.olliesbrother.maintenance.MaintenanceCountdownManager;
import dev.olliesbrother.restart.RestartCountdownManager;
import dev.olliesbrother.restart.RestartCountdownManager.RestartStatus;
import dev.olliesbrother.util.DurationFormatter;
import dev.olliesbrother.util.DurationParser;
import net.minecraft.command.CommandSource;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;

public final class RestartCommands {

    private RestartCommands() {
        // Utility class
    }

    public static LiteralArgumentBuilder<ServerCommandSource>
    build() {
        return CommandManager.literal("restart")
                .requires(source ->
                        source.hasPermissionLevel(4)
                )

                // /serversignals restart
                .executes(context ->
                        showStatus(
                                context.getSource()
                        )
                )

                // /serversignals restart start <duration>
                .then(
                        CommandManager.literal("start")
                                .then(
                                        CommandManager.argument(
                                                        "duration",
                                                        StringArgumentType.word()
                                                )
                                                .suggests(
                                                        (context, builder) ->
                                                                CommandSource.suggestMatching(
                                                                        new String[]{
                                                                                "30s",
                                                                                "1m",
                                                                                "5m",
                                                                                "10m",
                                                                                "30m",
                                                                                "1h"
                                                                        },
                                                                        builder
                                                                )
                                                )
                                                .executes(context ->
                                                        start(
                                                                context.getSource(),
                                                                StringArgumentType.getString(
                                                                        context,
                                                                        "duration"
                                                                )
                                                        )
                                                )
                                )
                )

                // /serversignals restart status
                .then(
                        CommandManager.literal("status")
                                .executes(context ->
                                        showStatus(
                                                context.getSource()
                                        )
                                )
                )

                // /serversignals restart cancel
                .then(
                        CommandManager.literal("cancel")
                                .executes(context ->
                                        cancel(
                                                context.getSource()
                                        )
                                )
                )

                // /serversignals restart now
                .then(
                        CommandManager.literal("now")
                                .executes(context ->
                                        restartNow(
                                                context.getSource()
                                        )
                                )
                );
    }

    private static int start(
            ServerCommandSource source,
            String durationInput
    ) {
        if (MaintenanceCountdownManager
                .getStatus()
                .active()) {

            source.sendError(
                    Text.literal(
                            "A maintenance countdown is currently active. " +
                                    "Cancel it before scheduling a restart."
                    )
            );

            return 0;
        }
        if (!ConfigManager.getConfig()
                .restart.enabled) {

            source.sendError(
                    Text.literal(
                            "Restart countdowns are disabled " +
                                    "in the config."
                    )
            );

            return 0;
        }

        long durationSeconds;

        try {
            durationSeconds =
                    DurationParser.toSeconds(
                            durationInput
                    );
        } catch (IllegalArgumentException exception) {
            source.sendError(
                    Text.literal(
                            "Invalid duration: " +
                                    exception.getMessage()
                    )
            );

            return 0;
        }

        boolean started;

        try {
            started =
                    RestartCountdownManager.start(
                            source.getServer(),
                            durationSeconds,
                            source.getName()
                    );
        } catch (IllegalArgumentException exception) {
            source.sendError(
                    Text.literal(
                            exception.getMessage()
                    )
            );

            return 0;
        }

        if (!started) {
            source.sendError(
                    Text.literal(
                            "A restart countdown is already active."
                    )
            );

            return 0;
        }

        source.sendFeedback(
                () -> Text.literal(
                        "Restart countdown started for " +
                                DurationFormatter.formatSeconds(
                                        durationSeconds
                                ) +
                                "."
                ),
                true
        );

        ServerSignals.LOGGER.info(
                "Restart start command used by {}.",
                source.getName()
        );

        return 1;
    }

    private static int showStatus(
            ServerCommandSource source
    ) {
        RestartStatus status =
                RestartCountdownManager.getStatus();

        if (!status.active()) {
            source.sendFeedback(
                    () -> Text.literal(
                            "No restart countdown is active."
                    ),
                    false
            );

            return 1;
        }

        source.sendFeedback(
                () -> Text.literal(
                        "Restart in " +
                                DurationFormatter.formatSeconds(
                                        status.remainingSeconds()
                                ) +
                                ". Started by " +
                                status.startedBy() +
                                "."
                ),
                false
        );

        return 1;
    }

    private static int cancel(
            ServerCommandSource source
    ) {
        boolean cancelled =
                RestartCountdownManager.cancel(
                        source.getName()
                );

        if (!cancelled) {
            source.sendError(
                    Text.literal(
                            "No restart countdown is active."
                    )
            );

            return 0;
        }

        source.sendFeedback(
                () -> Text.literal(
                        "Restart countdown cancelled."
                ),
                true
        );

        return 1;
    }

    private static int restartNow(
            ServerCommandSource source
    ) {
        if (!ConfigManager.getConfig()
                .restart.enabled) {

            source.sendError(
                    Text.literal(
                            "Restart functionality is disabled " +
                                    "in the config."
                    )
            );

            return 0;
        }

        source.sendFeedback(
                () -> Text.literal(
                        "Executing the configured restart command."
                ),
                true
        );

        RestartCountdownManager.finishNow(
                source.getServer(),
                source.getName()
        );

        return 1;
    }
}