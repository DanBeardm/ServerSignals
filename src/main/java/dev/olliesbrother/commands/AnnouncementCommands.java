package dev.olliesbrother.commands;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import dev.olliesbrother.ServerSignals;
import dev.olliesbrother.config.AnnouncementConfig;
import dev.olliesbrother.config.ConfigManager;
import dev.olliesbrother.permissions.PermissionHelper;
import dev.olliesbrother.permissions.PermissionNodes;
import dev.olliesbrother.scheduler.AnnouncementScheduler;
import net.minecraft.command.CommandSource;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;

public final class AnnouncementCommands {

    private AnnouncementCommands() {
        // Utility class
    }

    public static LiteralArgumentBuilder<ServerCommandSource> build() {
        return CommandManager.literal("announcement")

                // /serversignals announcement
                .executes(context ->
                        listAnnouncements(
                                context.getSource()
                        )
                )

                // /serversignals announcement list
                .then(
                        CommandManager.literal("list")
                                .requires(
                                        PermissionHelper.require(
                                                PermissionNodes.ANNOUNCEMENT_LIST,
                                                2
                                        )
                                )
                                .executes(context ->
                                        listAnnouncements(
                                                context.getSource()
                                        )
                                )
                )

                // /serversignals announcement test <id>
                .then(
                        CommandManager.literal("test")
                                .requires(
                                        PermissionHelper.require(
                                                PermissionNodes.ANNOUNCEMENT_TEST,
                                                2
                                        )
                                )
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
                                                                                .map(a -> a.id),
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
                );
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