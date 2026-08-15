package dev.olliesbrother.scheduler;

import dev.olliesbrother.ServerSignals;
import dev.olliesbrother.config.ConfigManager;
import dev.olliesbrother.config.ScheduledCommandConfig;
import dev.olliesbrother.util.DurationParser;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.ServerCommandSource;

import java.util.HashMap;
import java.util.Map;

public final class CommandScheduler {
    private static final Map<String, Long> TICKS_REMAINING =
            new HashMap<>();

    private static boolean registered;

    private CommandScheduler() {
        // Utility class
    }

    public static void register() {
        if (registered) {
            return;
        }

        registered = true;

        resetAll();

        ServerTickEvents.END_SERVER_TICK.register(
                CommandScheduler::tick
        );
    }

    public static void resetAll() {
        TICKS_REMAINING.clear();

        for (ScheduledCommandConfig task :
                ConfigManager.getConfig().scheduledCommands) {

            if (!task.enabled) {
                continue;
            }

            TICKS_REMAINING.put(
                    task.id,
                    DurationParser.toTicks(task.interval)
            );
        }

        ServerSignals.LOGGER.info(
                "Reset {} enabled scheduled command timers.",
                TICKS_REMAINING.size()
        );
    }

    public static boolean executeNow(
            MinecraftServer server,
            String taskId
    ) {
        for (ScheduledCommandConfig task :
                ConfigManager.getConfig().scheduledCommands) {

            if (!task.id.equals(taskId)) {
                continue;
            }

            executeTask(server, task);
            return true;
        }

        return false;
    }

    private static void tick(
            MinecraftServer server
    ) {
        for (ScheduledCommandConfig task :
                ConfigManager.getConfig().scheduledCommands) {

            if (!task.enabled) {
                continue;
            }

            long ticksRemaining =
                    TICKS_REMAINING.getOrDefault(
                            task.id,
                            DurationParser.toTicks(
                                    task.interval
                            )
                    );

            ticksRemaining--;

            if (ticksRemaining > 0) {
                TICKS_REMAINING.put(
                        task.id,
                        ticksRemaining
                );

                continue;
            }

            // Reset before executing so a failed command
            // cannot break the schedule.
            TICKS_REMAINING.put(
                    task.id,
                    DurationParser.toTicks(
                            task.interval
                    )
            );

            if (task.onlyWhenPlayersOnline &&
                    server.getPlayerManager()
                            .getCurrentPlayerCount() == 0) {
                continue;
            }

            executeTask(server, task);
        }
    }

    private static void executeTask(
            MinecraftServer server,
            ScheduledCommandConfig task
    ) {
        ServerCommandSource commandSource =
                server.getCommandSource()
                        .withLevel(4);

        ServerSignals.LOGGER.info(
                "Executing scheduled command task '{}'.",
                task.id
        );

        for (int index = 0;
             index < task.commands.size();
             index++) {

            String command =
                    task.commands.get(index);

            try {
                server.getCommandManager()
                        .executeWithPrefix(
                                commandSource,
                                command
                        );

            } catch (RuntimeException exception) {
                ServerSignals.LOGGER.error(
                        "Scheduled command task '{}' " +
                                "failed while running command {}.",
                        task.id,
                        index + 1,
                        exception
                );
            }
        }

        ServerSignals.LOGGER.info(
                "Finished scheduled command task '{}' " +
                        "with {} configured command(s).",
                task.id,
                task.commands.size()
        );
    }
}