package dev.olliesbrother.commands;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import dev.olliesbrother.permissions.PermissionHelper;
import dev.olliesbrother.permissions.PermissionNodes;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public final class HelpCommand {

    private HelpCommand() {
        // Utility class
    }

    public static LiteralArgumentBuilder<ServerCommandSource> build() {
        return CommandManager.literal("help")

                // /serversignals help
                .executes(context ->
                        showOverview(
                                context.getSource()
                        )
                )

                // /serversignals help announcements
                .then(
                        CommandManager.literal("announcements")
                                .executes(context ->
                                        showAnnouncements(
                                                context.getSource()
                                        )
                                )
                )

                // /serversignals help commands
                .then(
                        CommandManager.literal("commands")
                                .executes(context ->
                                        showScheduledCommands(
                                                context.getSource()
                                        )
                                )
                )

                // /serversignals help player-messages
                .then(
                        CommandManager.literal("player-messages")
                                .executes(context ->
                                        showPlayerMessages(
                                                context.getSource()
                                        )
                                )
                )

                // /serversignals help restart
                .then(
                        CommandManager.literal("restart")
                                .executes(context ->
                                        showRestart(
                                                context.getSource()
                                        )
                                )
                )

                // /serversignals help maintenance
                .then(
                        CommandManager.literal("maintenance")
                                .executes(context ->
                                        showMaintenance(
                                                context.getSource()
                                        )
                                )
                )

                // /serversignals help config
                .then(
                        CommandManager.literal("config")
                                .executes(context ->
                                        showConfig(
                                                context.getSource()
                                        )
                                )
                );
    }

    private static int showOverview(
            ServerCommandSource source
    ) {
        sendHeader(
                source,
                "Server Signals Help"
        );

        int visibleCommands = 0;

        visibleCommands += sendIfAllowed(
                source,
                PermissionNodes.ANNOUNCEMENT_LIST,
                2,
                "/serversignals list",
                "List configured announcements."
        );

        visibleCommands += sendIfAllowed(
                source,
                PermissionNodes.ANNOUNCEMENT_TEST,
                2,
                "/serversignals test <id>",
                "Test an announcement immediately."
        );

        visibleCommands += sendIfAllowed(
                source,
                PermissionNodes.RELOAD,
                4,
                "/serversignals reload",
                "Reload the configuration."
        );

        visibleCommands += sendIfAllowed(
                source,
                PermissionNodes.SCHEDULED_COMMAND_LIST,
                2,
                "/serversignals command list",
                "List scheduled command tasks."
        );

        visibleCommands += sendIfAllowed(
                source,
                PermissionNodes.PLAYER_MESSAGE_TEST,
                2,
                "/serversignals player-message",
                "View and test player messages."
        );

        visibleCommands += sendIfAllowed(
                source,
                PermissionNodes.RESTART_STATUS,
                2,
                "/serversignals restart status",
                "View the restart countdown."
        );

        visibleCommands += sendIfAllowed(
                source,
                PermissionNodes.MAINTENANCE_STATUS,
                2,
                "/serversignals maintenance status",
                "View maintenance status."
        );

        if (visibleCommands == 0) {
            source.sendFeedback(
                    () -> Text.literal(
                            "You do not currently have access " +
                                    "to any Server Signals commands."
                    ).formatted(Formatting.RED),
                    false
            );
        }

        source.sendFeedback(
                () -> Text.literal(
                        "\nDetailed help:"
                ).formatted(Formatting.GRAY),
                false
        );

        source.sendFeedback(
                () -> Text.literal(
                        "/serversignals help announcements"
                ).formatted(Formatting.YELLOW),
                false
        );

        source.sendFeedback(
                () -> Text.literal(
                        "/serversignals help commands"
                ).formatted(Formatting.YELLOW),
                false
        );

        source.sendFeedback(
                () -> Text.literal(
                        "/serversignals help player-messages"
                ).formatted(Formatting.YELLOW),
                false
        );

        source.sendFeedback(
                () -> Text.literal(
                        "/serversignals help restart"
                ).formatted(Formatting.YELLOW),
                false
        );

        source.sendFeedback(
                () -> Text.literal(
                        "/serversignals help maintenance"
                ).formatted(Formatting.YELLOW),
                false
        );

        return 1;
    }

    private static int showAnnouncements(
            ServerCommandSource source
    ) {
        sendHeader(
                source,
                "Announcements"
        );

        int visible = 0;

        visible += sendIfAllowed(
                source,
                PermissionNodes.ANNOUNCEMENT_LIST,
                2,
                "/serversignals list",
                "List configured announcements."
        );

        visible += sendIfAllowed(
                source,
                PermissionNodes.ANNOUNCEMENT_TEST,
                2,
                "/serversignals test",
                "Test the first enabled announcement."
        );

        visible += sendIfAllowed(
                source,
                PermissionNodes.ANNOUNCEMENT_TEST,
                2,
                "/serversignals test <id>",
                "Test a specific announcement."
        );

        sendNoAccessIfEmpty(
                source,
                visible
        );

        return 1;
    }

    private static int showScheduledCommands(
            ServerCommandSource source
    ) {
        sendHeader(
                source,
                "Scheduled Commands"
        );

        int visible = 0;

        visible += sendIfAllowed(
                source,
                PermissionNodes.SCHEDULED_COMMAND_LIST,
                2,
                "/serversignals command",
                "Show scheduled-command status."
        );

        visible += sendIfAllowed(
                source,
                PermissionNodes.SCHEDULED_COMMAND_LIST,
                2,
                "/serversignals command list",
                "List scheduled command tasks."
        );

        visible += sendIfAllowed(
                source,
                PermissionNodes.SCHEDULED_COMMAND_TEST,
                4,
                "/serversignals command test <id>",
                "Execute a scheduled task immediately."
        );

        sendNoAccessIfEmpty(
                source,
                visible
        );

        return 1;
    }

    private static int showPlayerMessages(
            ServerCommandSource source
    ) {
        sendHeader(
                source,
                "Player Messages"
        );

        int visible = 0;

        visible += sendIfAllowed(
                source,
                PermissionNodes.PLAYER_MESSAGE_TEST,
                2,
                "/serversignals player-message",
                "Show player-message status."
        );

        visible += sendIfAllowed(
                source,
                PermissionNodes.PLAYER_MESSAGE_TEST,
                2,
                "/serversignals player-message test join <player>",
                "Test the join message."
        );

        visible += sendIfAllowed(
                source,
                PermissionNodes.PLAYER_MESSAGE_TEST,
                2,
                "/serversignals player-message test first_join <player>",
                "Test the first-join message."
        );

        visible += sendIfAllowed(
                source,
                PermissionNodes.PLAYER_MESSAGE_TEST,
                2,
                "/serversignals player-message test leave <player>",
                "Test the leave message."
        );

        sendNoAccessIfEmpty(
                source,
                visible
        );

        return 1;
    }

    private static int showRestart(
            ServerCommandSource source
    ) {
        sendHeader(
                source,
                "Restart"
        );

        int visible = 0;

        visible += sendIfAllowed(
                source,
                PermissionNodes.RESTART_STATUS,
                2,
                "/serversignals restart status",
                "View the current restart countdown."
        );

        visible += sendIfAllowed(
                source,
                PermissionNodes.RESTART_START,
                4,
                "/serversignals restart start <duration>",
                "Start a restart countdown."
        );

        visible += sendIfAllowed(
                source,
                PermissionNodes.RESTART_CANCEL,
                4,
                "/serversignals restart cancel",
                "Cancel the active restart countdown."
        );

        visible += sendIfAllowed(
                source,
                PermissionNodes.RESTART_NOW,
                4,
                "/serversignals restart now",
                "Execute the configured restart command now."
        );

        sendNoAccessIfEmpty(
                source,
                visible
        );

        return 1;
    }

    private static int showMaintenance(
            ServerCommandSource source
    ) {
        sendHeader(
                source,
                "Maintenance"
        );

        int visible = 0;

        visible += sendIfAllowed(
                source,
                PermissionNodes.MAINTENANCE_STATUS,
                2,
                "/serversignals maintenance status",
                "View maintenance status."
        );

        visible += sendIfAllowed(
                source,
                PermissionNodes.MAINTENANCE_ENABLE,
                4,
                "/serversignals maintenance enable [reason]",
                "Enable maintenance mode."
        );

        visible += sendIfAllowed(
                source,
                PermissionNodes.MAINTENANCE_DISABLE,
                4,
                "/serversignals maintenance disable",
                "Disable maintenance mode."
        );

        visible += sendIfAllowed(
                source,
                PermissionNodes.MAINTENANCE_KICK,
                4,
                "/serversignals maintenance kick",
                "Kick non-bypass players."
        );

        visible += sendIfAllowed(
                source,
                PermissionNodes.MAINTENANCE_SCHEDULE,
                4,
                "/serversignals maintenance schedule <duration> [reason]",
                "Schedule maintenance mode."
        );

        visible += sendIfAllowed(
                source,
                PermissionNodes.MAINTENANCE_STATUS,
                2,
                "/serversignals maintenance schedule status",
                "View the maintenance countdown."
        );

        visible += sendIfAllowed(
                source,
                PermissionNodes.MAINTENANCE_CANCEL,
                4,
                "/serversignals maintenance schedule cancel",
                "Cancel scheduled maintenance."
        );

        sendNoAccessIfEmpty(
                source,
                visible
        );

        return 1;
    }

    private static int showConfig(
            ServerCommandSource source
    ) {
        sendHeader(
                source,
                "Configuration"
        );

        int visible = 0;

        visible += sendIfAllowed(
                source,
                PermissionNodes.RELOAD,
                4,
                "/serversignals reload",
                "Reload server_signals.json."
        );

        sendNoAccessIfEmpty(
                source,
                visible
        );

        return 1;
    }

    private static int sendIfAllowed(
            ServerCommandSource source,
            String permission,
            int fallbackLevel,
            String command,
            String description
    ) {
        if (!PermissionHelper.check(
                source,
                permission,
                fallbackLevel
        )) {
            return 0;
        }

        MutableText message =
                Text.literal(command)
                        .formatted(
                                Formatting.AQUA
                        )
                        .append(
                                Text.literal(
                                        " - " +
                                                description
                                ).formatted(
                                        Formatting.GRAY
                                )
                        );

        source.sendFeedback(
                () -> message,
                false
        );

        return 1;
    }

    private static void sendHeader(
            ServerCommandSource source,
            String title
    ) {
        source.sendFeedback(
                () -> Text.literal(
                        "----- " +
                                title +
                                " -----"
                ).formatted(
                        Formatting.GOLD,
                        Formatting.BOLD
                ),
                false
        );
    }

    private static void sendNoAccessIfEmpty(
            ServerCommandSource source,
            int visibleCommands
    ) {
        if (visibleCommands > 0) {
            return;
        }

        source.sendFeedback(
                () -> Text.literal(
                        "You do not have access " +
                                "to any commands in this category."
                ).formatted(
                        Formatting.RED
                ),
                false
        );
    }
}