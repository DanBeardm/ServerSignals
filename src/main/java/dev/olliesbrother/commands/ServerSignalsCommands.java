package dev.olliesbrother.commands;

import com.mojang.brigadier.arguments.StringArgumentType;
import dev.olliesbrother.ServerSignals;
import dev.olliesbrother.config.AnnouncementConfig;
import dev.olliesbrother.config.ConfigManager;
import dev.olliesbrother.config.ServerSignalsConfig;
import dev.olliesbrother.maintenance.MaintenanceCountdownManager;
import dev.olliesbrother.permissions.PermissionHelper;
import dev.olliesbrother.permissions.PermissionNodes;
import dev.olliesbrother.restart.RestartCountdownManager;
import dev.olliesbrother.scheduler.AnnouncementScheduler;
import dev.olliesbrother.scheduler.CommandScheduler;
import dev.olliesbrother.util.DurationFormatter;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.command.CommandSource;
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

                                        // /serversignals list
                                        .requires(
                                                PermissionHelper.require(
                                                        PermissionNodes.ANNOUNCEMENT_TEST,
                                                        2
                                                )
                                        )

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

                                        // /serversignals test
                                        // /serversignals test <id>
                                        .then(
                                                CommandManager.literal("test")
                                                        .requires(
                                                                PermissionHelper.require(
                                                                        PermissionNodes.ANNOUNCEMENT_TEST,
                                                                        2
                                                                )
                                                        )

                                                        // /serversignals test
                                                        .executes(context ->
                                                                testFirstEnabled(
                                                                        context.getSource()
                                                                )
                                                        )

                                                        // /serversignals test <id>
                                                        .then(
                                                                CommandManager.argument(
                                                                                "id",
                                                                                StringArgumentType.word()
                                                                        )
                                                                        .suggests(
                                                                                (context, builder) ->
                                                                                        CommandSource.suggestMatching(
                                                                                                ConfigManager
                                                                                                        .getConfig()
                                                                                                        .announcements
                                                                                                        .stream()
                                                                                                        .map(
                                                                                                                announcement ->
                                                                                                                        announcement.id
                                                                                                        ),
                                                                                                builder
                                                                                        )
                                                                        )
                                                                        .executes(context ->
                                                                                testById(
                                                                                        context.getSource(),
                                                                                        StringArgumentType.getString(
                                                                                                context,
                                                                                                "id"
                                                                                        )
                                                                                )
                                                                        )
                                                        )
                                        )

                                        .then(ScheduledCommandCommands.build())
                                        .then(PlayerMessageCommands.build())
                                        .then(RestartCommands.build())
                                        .then(MaintenanceCommands.build())
                        )
        );
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

    private static int listAnnouncements(
            ServerCommandSource source
    ) {
        source.sendFeedback(
                () -> Text.literal(
                        "Configured announcements:"
                ),
                false
        );

        for (AnnouncementConfig announcement :
                ConfigManager.getConfig().announcements) {

            String status = announcement.enabled
                    ? "enabled"
                    : "disabled";

            source.sendFeedback(
                    () -> Text.literal(
                            "- " + announcement.id +
                                    ": " + status +
                                    ", every " +
                                    announcement.interval +
                                    " seconds"
                    ),
                    false
            );
        }

        return 1;
    }

    private static int reload(
            ServerCommandSource source
    ) {
        boolean loaded = ConfigManager.load();

        if (!loaded) {
            source.sendError(
                    Text.literal(
                            "Could not reload Server Signals. " +
                                    "Check the server console."
                    )
            );

            return 0;
        }

        AnnouncementScheduler.resetAll();
        CommandScheduler.resetAll();

        source.sendFeedback(
                () -> Text.literal(
                        "Server Signals reloaded. " +
                                ConfigManager.getConfig()
                                        .announcements.size() +
                                " announcements loaded."
                ),
                false
        );

        ServerSignals.LOGGER.info(
                "Configuration reloaded by {}.",
                source.getName()
        );

        return 1;
    }

    private static int testFirstEnabled(
            ServerCommandSource source
    ) {
        for (AnnouncementConfig announcement :
                ConfigManager.getConfig().announcements) {

            if (!announcement.enabled) {
                continue;
            }

            return testById(
                    source,
                    announcement.id
            );
        }

        source.sendError(
                Text.literal(
                        "There are no enabled announcements to test."
                )
        );

        return 0;
    }

    private static int testById(
            ServerCommandSource source,
            String announcementId
    ) {
        boolean found =
                AnnouncementScheduler.broadcastNow(
                        source.getServer(),
                        announcementId
                );

        if (!found) {
            source.sendError(
                    Text.literal(
                            "Unknown announcement ID: " +
                                    announcementId
                    )
            );

            return 0;
        }

        source.sendFeedback(
                () -> Text.literal(
                        "Tested announcement: " +
                                announcementId
                ),
                false
        );

        ServerSignals.LOGGER.info(
                "Announcement '{}' tested by {}.",
                announcementId,
                source.getName()
        );

        return 1;
    }
}