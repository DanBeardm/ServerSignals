package dev.olliesbrother.commands;

import dev.olliesbrother.ServerSignals;
import dev.olliesbrother.config.ConfigManager;
import dev.olliesbrother.config.ServerSignalsConfig;
import dev.olliesbrother.maintenance.MaintenanceCountdownManager;
import dev.olliesbrother.permissions.PermissionHelper;
import dev.olliesbrother.permissions.PermissionNodes;
import dev.olliesbrother.restart.RestartCountdownManager;
import dev.olliesbrother.util.DurationFormatter;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public final class ServerSignalsCommands {

    private ServerSignalsCommands() {
        // Utility class
    }

    public static void register() {
        CommandRegistrationCallback.EVENT.register(
                (dispatcher, registryAccess, environment) ->
                        dispatcher.register(
                                CommandManager.literal("serversignals")
                                        .requires(
                                                PermissionHelper.require(
                                                        PermissionNodes.ROOT,
                                                        2
                                                )
                                        )

                                        // /serversignals
                                        .executes(context ->
                                                showStatus(
                                                        context.getSource()
                                                )
                                        )

                                        //serversignals help
                                        .then(HelpCommand.build())

                                        // /serversignals reload
                                        .then(
                                                CommandManager.literal("reload")
                                                        .requires(
                                                                PermissionHelper.require(
                                                                        PermissionNodes.RELOAD,
                                                                        4
                                                                )
                                                        )
                                                        .executes(context ->
                                                                reload(
                                                                        context.getSource()
                                                                )
                                                        )
                                        )

                                        .then(
                                                AnnouncementCommands.build()
                                        )
                                        .then(ScheduledCommandCommands.build())
                                        .then(PlayerMessageCommands.build())
                                        .then(RestartCommands.build())
                                        .then(MaintenanceCommands.build())
                                        .then(
                                                CommandManager.literal("validate")
                                                        .requires(
                                                                PermissionHelper.require(
                                                                        PermissionNodes.VALIDATE,
                                                                        2
                                                                )
                                                        )
                                                        .executes(context ->
                                                                validateConfig(
                                                                        context.getSource()
                                                                )
                                                        )
                                        )
                        )
        );
    }


    private static int validateConfig(
            ServerCommandSource source
    ) {

        boolean success =
                ConfigManager.validateOnly();

        if (!success) {

            source.sendError(
                    Text.literal(
                            "Server Signals configuration is invalid."
                    )
            );

            String error =
                    ConfigManager.getLastError();

            if (error != null &&
                    !error.isBlank()) {

                source.sendError(
                        Text.literal(
                                error
                        )
                );
            }

            source.sendFeedback(
                    () -> Text.literal(
                            "Active configuration was not changed."
                    ).formatted(
                            Formatting.GRAY
                    ),
                    false
            );

            return 0;
        }


        source.sendFeedback(
                () -> Text.literal(
                        "Server Signals configuration is valid."
                ).formatted(
                        Formatting.GREEN,
                        Formatting.BOLD
                ),
                false
        );

        sendConfigSuccess(
                source,
                "announcements.json"
        );

        sendConfigSuccess(
                source,
                "scheduled_commands.json"
        );

        sendConfigSuccess(
                source,
                "player_messages.json"
        );

        sendConfigSuccess(
                source,
                "restart.json"
        );

        sendConfigSuccess(
                source,
                "maintenance.json"
        );

        source.sendFeedback(
                () -> Text.literal(
                        "No changes were applied."
                ).formatted(
                        Formatting.GRAY
                ),
                false
        );

        return 1;
    }

    private static int showStatus(
            ServerCommandSource source
    ) {
        ServerSignalsConfig config =
                ConfigManager.getConfig();

        long enabledAnnouncements =
                config.announcements
                        .stream()
                        .filter(
                                announcement ->
                                        announcement.enabled
                        )
                        .count();

        long enabledScheduledCommands =
                config.scheduledCommands
                        .stream()
                        .filter(
                                task ->
                                        task.enabled
                        )
                        .count();

        RestartCountdownManager.RestartStatus restartStatus =
                RestartCountdownManager.getStatus();

        MaintenanceCountdownManager.MaintenanceScheduleStatus
                maintenanceSchedule =
                MaintenanceCountdownManager.getStatus();

        String version =
                FabricLoader.getInstance()
                        .getModContainer(
                                ServerSignals.MOD_ID
                        )
                        .map(
                                container ->
                                        container
                                                .getMetadata()
                                                .getVersion()
                                                .getFriendlyString()
                        )
                        .orElse("unknown");

        source.sendFeedback(
                () -> Text.literal(
                        "----- Server Signals v" +
                                version +
                                " -----"
                ).formatted(
                        Formatting.GOLD,
                        Formatting.BOLD
                ),
                false
        );

        source.sendFeedback(
                () -> Text.literal(
                                "Announcements: "
                        ).formatted(Formatting.GRAY)
                        .append(
                                Text.literal(
                                        enabledAnnouncements +
                                                "/" +
                                                config.announcements.size() +
                                                " enabled"
                                ).formatted(
                                        Formatting.AQUA
                                )
                        ),
                false
        );

        source.sendFeedback(
                () -> Text.literal(
                                "Scheduled Commands: "
                        ).formatted(Formatting.GRAY)
                        .append(
                                Text.literal(
                                        enabledScheduledCommands +
                                                "/" +
                                                config.scheduledCommands.size() +
                                                " enabled"
                                ).formatted(
                                        Formatting.AQUA
                                )
                        ),
                false
        );

        source.sendFeedback(
                () -> Text.literal(
                                "Maintenance: "
                        ).formatted(Formatting.GRAY)
                        .append(
                                Text.literal(
                                        config.maintenance.enabled
                                                ? "Enabled"
                                                : "Disabled"
                                ).formatted(
                                        config.maintenance.enabled
                                                ? Formatting.RED
                                                : Formatting.GREEN
                                )
                        ),
                false
        );

        source.sendFeedback(
                () -> Text.literal(
                                "Restart Countdown: "
                        ).formatted(Formatting.GRAY)
                        .append(
                                Text.literal(
                                        restartStatus.active()
                                                ? DurationFormatter
                                                .formatSeconds(
                                                        restartStatus
                                                                .remainingSeconds()
                                                )
                                                : "None"
                                ).formatted(
                                        restartStatus.active()
                                                ? Formatting.YELLOW
                                                : Formatting.GREEN
                                )
                        ),
                false
        );

        source.sendFeedback(
                () -> Text.literal(
                                "Maintenance Countdown: "
                        ).formatted(Formatting.GRAY)
                        .append(
                                Text.literal(
                                        maintenanceSchedule.active()
                                                ? DurationFormatter
                                                .formatSeconds(
                                                        maintenanceSchedule
                                                                .remainingSeconds()
                                                )
                                                : "None"
                                ).formatted(
                                        maintenanceSchedule.active()
                                                ? Formatting.YELLOW
                                                : Formatting.GREEN
                                )
                        ),
                false
        );

        source.sendFeedback(
                () -> Text.literal(
                        "\nUse /serversignals help for commands."
                ).formatted(
                        Formatting.YELLOW
                ),
                false
        );

        return 1;
    }

    private static int reload(
            ServerCommandSource source
    ) {

        boolean success =
                ConfigManager.load();

        if (!success) {

            source.sendError(
                    Text.literal(
                            "Could not reload Server Signals."
                    )
            );

            String error =
                    ConfigManager.getLastError();

            if (error != null &&
                    !error.isBlank()) {

                source.sendError(
                        Text.literal(
                                error
                        )
                );
            }

            source.sendFeedback(
                    () -> Text.literal(
                            "Previous configuration remains active."
                    ).formatted(
                            Formatting.GRAY
                    ),
                    false
            );

            return 0;
        }


        source.sendFeedback(
                () -> Text.literal(
                        "Server Signals configuration reloaded."
                ).formatted(
                        Formatting.GREEN,
                        Formatting.BOLD
                ),
                true
        );


        sendConfigSuccess(
                source,
                "announcements.json"
        );

        sendConfigSuccess(
                source,
                "scheduled_commands.json"
        );

        sendConfigSuccess(
                source,
                "player_messages.json"
        );

        sendConfigSuccess(
                source,
                "restart.json"
        );

        sendConfigSuccess(
                source,
                "maintenance.json"
        );


        ServerSignals.LOGGER.info(
                "Server Signals configuration reloaded by {}.",
                source.getName()
        );

        return 1;
    }

    private static void sendConfigSuccess(
            ServerCommandSource source,
            String fileName
    ) {

        source.sendFeedback(
                () -> Text.literal(
                        "✓ "
                ).formatted(
                        Formatting.GREEN
                ).append(
                        Text.literal(
                                fileName
                        ).formatted(
                                Formatting.GRAY
                        )
                ),
                false
        );
    }

}