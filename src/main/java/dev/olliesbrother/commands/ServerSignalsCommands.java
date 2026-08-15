package dev.olliesbrother.commands;

import com.mojang.brigadier.arguments.StringArgumentType;
import dev.olliesbrother.ServerSignals;
import dev.olliesbrother.config.AnnouncementConfig;
import dev.olliesbrother.config.ConfigManager;
import dev.olliesbrother.config.ServerSignalsConfig;
import dev.olliesbrother.scheduler.AnnouncementScheduler;
import dev.olliesbrother.scheduler.CommandScheduler;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;

public final class ServerSignalsCommands {

    private ServerSignalsCommands() {
        // Utility class
    }

    public static void register() {
        CommandRegistrationCallback.EVENT.register(
                (dispatcher, registryAccess, environment) ->
                        dispatcher.register(
                                CommandManager.literal("serversignals")
                                        .requires(source ->
                                                source.hasPermissionLevel(2)
                                        )

                                        // /serversignals
                                        .executes(context ->
                                                showStatus(
                                                        context.getSource()
                                                )
                                        )

                                        // /serversignals list
                                        .then(
                                                CommandManager.literal("list")
                                                        .executes(context ->
                                                                listAnnouncements(
                                                                        context.getSource()
                                                                )
                                                        )
                                        )

                                        // /serversignals reload
                                        .then(
                                                CommandManager.literal("reload")
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
                                                        .executes(context ->
                                                                testFirstEnabled(
                                                                        context.getSource()
                                                                )
                                                        )
                                                        .then(
                                                                CommandManager.argument(
                                                                                "id",
                                                                                StringArgumentType.word()
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

        long enabledCount = config.announcements
                .stream()
                .filter(announcement -> announcement.enabled)
                .count();

        source.sendFeedback(
                () -> Text.literal(
                        "Server Signals is running. " +
                                enabledCount + " of " +
                                config.announcements.size() +
                                " announcements are enabled."
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