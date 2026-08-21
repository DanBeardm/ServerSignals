package dev.olliesbrother.commands;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import dev.olliesbrother.ServerSignals;
import dev.olliesbrother.config.ConfigManager;
import dev.olliesbrother.config.ScheduledCommandConfig;
import dev.olliesbrother.permissions.PermissionHelper;
import dev.olliesbrother.permissions.PermissionNodes;
import dev.olliesbrother.scheduler.CommandScheduler;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;

public final class ScheduledCommandCommands {

    private ScheduledCommandCommands() {
        // Utility class
    }

    public static LiteralArgumentBuilder<ServerCommandSource>
    build() {
        return
                CommandManager.literal("command")
                .requires(
                        PermissionHelper.require(
                                PermissionNodes.SCHEDULED_COMMAND_LIST,
                                2
                        )
                )

                // /serversignals command list
                .then(
                        CommandManager.literal("list")
                                .executes(context ->
                                        listTasks(
                                                context.getSource()
                                        )
                                )
                )

                // /serversignals command test <id>
                .requires(
                        PermissionHelper.require(
                                PermissionNodes.SCHEDULED_COMMAND_TEST,
                                4
                        )
                );

    }

    private static int showSummary(
            ServerCommandSource source
    ) {
        long enabledCount =
                ConfigManager.getConfig()
                        .scheduledCommands
                        .stream()
                        .filter(task -> task.enabled)
                        .count();

        int totalCount =
                ConfigManager.getConfig()
                        .scheduledCommands
                        .size();

        source.sendFeedback(
                () -> Text.literal(
                        enabledCount +
                                " of " +
                                totalCount +
                                " scheduled command tasks are enabled."
                ),
                false
        );

        return 1;
    }

    private static int listTasks(
            ServerCommandSource source
    ) {
        if (ConfigManager.getConfig()
                .scheduledCommands.isEmpty()) {

            source.sendFeedback(
                    () -> Text.literal(
                            "No scheduled command tasks are configured."
                    ),
                    false
            );

            return 1;
        }

        source.sendFeedback(
                () -> Text.literal(
                        "Scheduled command tasks:"
                ),
                false
        );

        for (ScheduledCommandConfig task :
                ConfigManager.getConfig().scheduledCommands) {

            String status =
                    task.enabled
                            ? "enabled"
                            : "disabled";

            source.sendFeedback(
                    () -> Text.literal(
                            "- " +
                                    task.id +
                                    ": " +
                                    status +
                                    ", every " +
                                    task.interval +
                                    ", " +
                                    task.commands.size() +
                                    " command(s)"
                    ),
                    false
            );
        }

        return 1;
    }

    private static int testTask(
            ServerCommandSource source,
            String taskId
    ) {
        boolean found =
                CommandScheduler.executeNow(
                        source.getServer(),
                        taskId
                );

        if (!found) {
            source.sendError(
                    Text.literal(
                            "Unknown scheduled command task: " +
                                    taskId
                    )
            );

            return 0;
        }

        source.sendFeedback(
                () -> Text.literal(
                        "Triggered scheduled command task: " +
                                taskId
                ),
                false
        );

        ServerSignals.LOGGER.info(
                "Scheduled command task '{}' manually " +
                        "triggered by {}.",
                taskId,
                source.getName()
        );

        return 1;
    }
}