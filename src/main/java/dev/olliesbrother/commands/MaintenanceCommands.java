package dev.olliesbrother.commands;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import dev.olliesbrother.ServerSignals;
import dev.olliesbrother.config.ConfigManager;
import dev.olliesbrother.config.MaintenanceConfig;
import dev.olliesbrother.maintenance.MaintenanceCountdownManager;
import dev.olliesbrother.maintenance.MaintenanceCountdownManager.MaintenanceScheduleStatus;
import dev.olliesbrother.maintenance.MaintenanceManager;
import dev.olliesbrother.maintenance.MaintenanceManager.EnableResult;
import dev.olliesbrother.permissions.PermissionHelper;
import dev.olliesbrother.permissions.PermissionNodes;
import dev.olliesbrother.restart.RestartCountdownManager;
import dev.olliesbrother.util.DurationFormatter;
import dev.olliesbrother.util.DurationParser;
import net.minecraft.command.CommandSource;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;

public final class MaintenanceCommands {

    private MaintenanceCommands() {
        // Utility class
    }

    public static LiteralArgumentBuilder<ServerCommandSource> build() {
        return CommandManager.literal("maintenance")

                // /serversignals maintenance
                .executes(context ->
                        status(context.getSource())
                )

                // /serversignals maintenance enable
                // /serversignals maintenance enable <reason>
                .then(
                        CommandManager.literal("enable")
                                .requires(
                                        PermissionHelper.require(
                                                PermissionNodes.MAINTENANCE_ENABLE,
                                                4
                                        )
                                )
                                .executes(context ->
                                        enable(
                                                context.getSource(),
                                                null
                                        )
                                )
                                .then(
                                        CommandManager.argument(
                                                        "reason",
                                                        StringArgumentType.greedyString()
                                                )
                                                .executes(context ->
                                                        enable(
                                                                context.getSource(),
                                                                StringArgumentType.getString(
                                                                        context,
                                                                        "reason"
                                                                )
                                                        )
                                                )
                                )
                )

                // /serversignals maintenance disable
                .then(
                        CommandManager.literal("disable")
                                .requires(
                                        PermissionHelper.require(
                                                PermissionNodes.MAINTENANCE_DISABLE,
                                                4
                                        )
                                )
                                .executes(context ->
                                        disable(context.getSource())
                                )
                )

                // /serversignals maintenance status
                .then(
                        CommandManager.literal("status")
                                .requires(
                                        PermissionHelper.require(
                                                PermissionNodes.MAINTENANCE_STATUS,
                                                2
                                        )
                                )
                                .executes(context ->
                                        status(context.getSource())
                                )
                )

                // /serversignals maintenance kick
                .then(
                        CommandManager.literal("kick")
                                .requires(
                                        PermissionHelper.require(
                                                PermissionNodes.MAINTENANCE_KICK,
                                                4
                                        )
                                )
                                .executes(context ->
                                        kick(context.getSource())
                                )
                )

                // /serversignals maintenance schedule ...
                .then(
                        CommandManager.literal("schedule")
                                .requires(
                                        PermissionHelper.require(
                                                PermissionNodes.MAINTENANCE_SCHEDULE,
                                                4
                                        )
                                )

                                // /serversignals maintenance schedule
                                .executes(context ->
                                        scheduleStatus(
                                                context.getSource()
                                        )
                                )

                                // /serversignals maintenance schedule status
                                .then(
                                        CommandManager.literal("status")
                                                .requires(
                                                        PermissionHelper.require(
                                                                PermissionNodes.MAINTENANCE_STATUS,
                                                                2
                                                        )
                                                )
                                                .executes(context ->
                                                        scheduleStatus(
                                                                context.getSource()
                                                        )
                                                )
                                )

                                // /serversignals maintenance schedule cancel
                                .then(
                                        CommandManager.literal("cancel")
                                                .requires(
                                                        PermissionHelper.require(
                                                                PermissionNodes.MAINTENANCE_CANCEL,
                                                                4
                                                        )
                                                )
                                                .executes(context ->
                                                        cancelSchedule(
                                                                context.getSource()
                                                        )
                                                )
                                )

                                // /serversignals maintenance schedule <duration>
                                // /serversignals maintenance schedule <duration> <reason>
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
                                                        schedule(
                                                                context.getSource(),
                                                                StringArgumentType.getString(
                                                                        context,
                                                                        "duration"
                                                                ),
                                                                null
                                                        )
                                                )
                                                .then(
                                                        CommandManager.argument(
                                                                        "reason",
                                                                        StringArgumentType.greedyString()
                                                                )
                                                                .executes(context ->
                                                                        schedule(
                                                                                context.getSource(),
                                                                                StringArgumentType.getString(
                                                                                        context,
                                                                                        "duration"
                                                                                ),
                                                                                StringArgumentType.getString(
                                                                                        context,
                                                                                        "reason"
                                                                                )
                                                                        )
                                                                )
                                                )
                                )
                );
    }

    private static int enable(
            ServerCommandSource source,
            String reason
    ) {
        if (MaintenanceManager.isEnabled()) {
            source.sendError(
                    Text.literal(
                            "Maintenance mode is already enabled."
                    )
            );

            return 0;
        }

        /*
         * If maintenance was scheduled, enabling it manually
         * should cancel the pending countdown.
         */
        MaintenanceScheduleStatus scheduleStatus =
                MaintenanceCountdownManager.getStatus();

        if (scheduleStatus.active()) {
            MaintenanceCountdownManager.cancel(
                    source.getName()
            );
        }

        EnableResult result =
                MaintenanceManager.enable(
                        source.getServer(),
                        reason
                );

        if (!result.success()) {
            source.sendError(
                    Text.literal(
                            "Could not save maintenance state. " +
                                    "Check the server console."
                    )
            );

            return 0;
        }

        MaintenanceConfig config =
                ConfigManager.getConfig()
                        .maintenance;

        source.sendFeedback(
                () -> Text.literal(
                        "Maintenance mode enabled. Reason: " +
                                config.reason
                ),
                true
        );

        if (result.kickedPlayers() > 0) {
            source.sendFeedback(
                    () -> Text.literal(
                            "Kicked " +
                                    result.kickedPlayers() +
                                    " player(s)."
                    ),
                    false
            );
        }

        ServerSignals.LOGGER.info(
                "Maintenance mode enabled by {}.",
                source.getName()
        );

        return 1;
    }

    private static int disable(
            ServerCommandSource source
    ) {
        if (!MaintenanceManager.isEnabled()) {
            source.sendError(
                    Text.literal(
                            "Maintenance mode is not enabled."
                    )
            );

            return 0;
        }

        if (!MaintenanceManager.disable()) {
            source.sendError(
                    Text.literal(
                            "Could not save maintenance state."
                    )
            );

            return 0;
        }

        source.sendFeedback(
                () -> Text.literal(
                        "Maintenance mode disabled."
                ),
                true
        );

        ServerSignals.LOGGER.info(
                "Maintenance mode disabled by {}.",
                source.getName()
        );

        return 1;
    }

    private static int status(
            ServerCommandSource source
    ) {
        MaintenanceConfig config =
                ConfigManager.getConfig()
                        .maintenance;

        if (!config.enabled) {
            source.sendFeedback(
                    () -> Text.literal(
                            "Maintenance mode is disabled."
                    ),
                    false
            );

            return 1;
        }

        source.sendFeedback(
                () -> Text.literal(
                        "Maintenance mode is enabled. Reason: " +
                                config.reason
                ),
                false
        );

        source.sendFeedback(
                () -> Text.literal(
                        "Operator bypass: " +
                                config.allowOperators +
                                ". Allowed players: " +
                                config.allowedPlayers.size()
                ),
                false
        );

        return 1;
    }

    private static int kick(
            ServerCommandSource source
    ) {
        if (!MaintenanceManager.isEnabled()) {
            source.sendError(
                    Text.literal(
                            "Maintenance mode is not enabled."
                    )
            );

            return 0;
        }

        int kicked =
                MaintenanceManager.kickNonBypassPlayers(
                        source.getServer()
                );

        source.sendFeedback(
                () -> Text.literal(
                        "Kicked " +
                                kicked +
                                " non-bypass player(s)."
                ),
                true
        );

        return 1;
    }

    private static int schedule(
            ServerCommandSource source,
            String durationInput,
            String reason
    ) {
        MaintenanceConfig config =
                ConfigManager.getConfig()
                        .maintenance;

        if (!config.schedule.enabled) {
            source.sendError(
                    Text.literal(
                            "Scheduled maintenance is disabled in the config."
                    )
            );

            return 0;
        }

        if (MaintenanceManager.isEnabled()) {
            source.sendError(
                    Text.literal(
                            "Maintenance mode is already enabled."
                    )
            );

            return 0;
        }

        if (RestartCountdownManager
                .getStatus()
                .active()) {

            source.sendError(
                    Text.literal(
                            "A restart countdown is currently active. " +
                                    "Cancel it before scheduling maintenance."
                    )
            );

            return 0;
        }

        if (MaintenanceCountdownManager
                .getStatus()
                .active()) {

            source.sendError(
                    Text.literal(
                            "A maintenance countdown is already active."
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
                    MaintenanceCountdownManager.start(
                            source.getServer(),
                            durationSeconds,
                            reason,
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
                            "Could not start the maintenance countdown."
                    )
            );

            return 0;
        }

        String actualReason =
                reason == null ||
                        reason.isBlank()
                        ? config.reason
                        : reason.trim();

        source.sendFeedback(
                () -> Text.literal(
                        "Maintenance scheduled in " +
                                DurationFormatter.formatSeconds(
                                        durationSeconds
                                ) +
                                ". Reason: " +
                                actualReason
                ),
                true
        );

        ServerSignals.LOGGER.info(
                "Maintenance scheduled by {} for {} second(s).",
                source.getName(),
                durationSeconds
        );

        return 1;
    }

    private static int scheduleStatus(
            ServerCommandSource source
    ) {
        MaintenanceScheduleStatus status =
                MaintenanceCountdownManager.getStatus();

        if (!status.active()) {
            source.sendFeedback(
                    () -> Text.literal(
                            "No maintenance countdown is active."
                    ),
                    false
            );

            return 1;
        }

        source.sendFeedback(
                () -> Text.literal(
                        "Maintenance begins in " +
                                DurationFormatter.formatSeconds(
                                        status.remainingSeconds()
                                ) +
                                ". Reason: " +
                                status.reason() +
                                ". Scheduled by " +
                                status.startedBy() +
                                "."
                ),
                false
        );

        return 1;
    }

    private static int cancelSchedule(
            ServerCommandSource source
    ) {
        boolean cancelled =
                MaintenanceCountdownManager.cancel(
                        source.getName()
                );

        if (!cancelled) {
            source.sendError(
                    Text.literal(
                            "No maintenance countdown is active."
                    )
            );

            return 0;
        }

        source.sendFeedback(
                () -> Text.literal(
                        "Scheduled maintenance cancelled."
                ),
                true
        );

        return 1;
    }
}