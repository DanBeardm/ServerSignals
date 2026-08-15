package dev.olliesbrother.restart;

import dev.olliesbrother.ServerSignals;
import dev.olliesbrother.config.ConfigManager;
import dev.olliesbrother.config.RestartConfig;
import dev.olliesbrother.delivery.BossBarValueParser;
import dev.olliesbrother.delivery.RestartWarningDeliveryService;
import dev.olliesbrother.util.DurationParser;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.entity.boss.ServerBossBar;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public final class RestartCountdownManager {
    private static final long NANOS_PER_SECOND =
            1_000_000_000L;

    private static boolean registered;
    private static boolean active;

    private static long totalNanos;
    private static long deadlineNanos;
    private static long endEpochMillis;

    private static long lastDisplayedSeconds =
            Long.MIN_VALUE;

    private static String startedBy = "";
    private static String finalCommand = "";

    private static final Set<Long> PENDING_WARNINGS =
            new HashSet<>();

    private static ServerBossBar bossBar;

    private RestartCountdownManager() {
        // Utility class
    }

    public static void register() {
        if (registered) {
            return;
        }

        registered = true;

        ServerTickEvents.END_SERVER_TICK.register(
                RestartCountdownManager::tick
        );

        ServerLifecycleEvents.SERVER_STOPPING.register(
                server -> clearState()
        );
    }

    public static boolean start(
            MinecraftServer server,
            long durationSeconds,
            String sourceName
    ) {
        if (active) {
            return false;
        }

        RestartConfig config =
                ConfigManager.getConfig().restart;

        try {
            totalNanos =
                    Math.multiplyExact(
                            durationSeconds,
                            NANOS_PER_SECOND
                    );

            deadlineNanos =
                    Math.addExact(
                            System.nanoTime(),
                            totalNanos
                    );

            endEpochMillis =
                    Math.addExact(
                            System.currentTimeMillis(),
                            Math.multiplyExact(
                                    durationSeconds,
                                    1_000L
                            )
                    );
        } catch (ArithmeticException exception) {
            throw new IllegalArgumentException(
                    "Restart duration is too large.",
                    exception
            );
        }

        startedBy = sourceName;
        finalCommand = config.finalCommand;
        lastDisplayedSeconds =
                durationSeconds;

        PENDING_WARNINGS.clear();

        for (String warning :
                config.warningTimes) {

            long warningSeconds =
                    DurationParser.toSeconds(warning);

            // The initial start message already covers
            // a warning equal to the full duration.
            if (warningSeconds < durationSeconds) {
                PENDING_WARNINGS.add(
                        warningSeconds
                );
            }
        }

        active = true;

        createBossBar(
                server,
                config,
                durationSeconds
        );

        RestartWarningDeliveryService.deliver(
                server,
                config,
                durationSeconds,
                endEpochMillis
        );

        ServerSignals.LOGGER.info(
                "Restart countdown started by {} for {} second(s).",
                sourceName,
                durationSeconds
        );

        return true;
    }

    public static boolean cancel(
            String sourceName
    ) {
        if (!active) {
            return false;
        }

        ServerSignals.LOGGER.info(
                "Restart countdown cancelled by {}.",
                sourceName
        );

        clearState();
        return true;
    }

    public static boolean finishNow(
            MinecraftServer server,
            String sourceName
    ) {
        RestartConfig config =
                ConfigManager.getConfig().restart;

        String commandToRun =
                active
                        ? finalCommand
                        : config.finalCommand;

        ServerSignals.LOGGER.info(
                "Immediate restart command triggered by {}.",
                sourceName
        );

        clearState();

        executeFinalCommand(
                server,
                commandToRun
        );

        return true;
    }

    public static RestartStatus getStatus() {
        if (!active) {
            return new RestartStatus(
                    false,
                    0L,
                    ""
            );
        }

        return new RestartStatus(
                true,
                calculateRemainingSeconds(),
                startedBy
        );
    }

    private static void tick(
            MinecraftServer server
    ) {
        if (!active) {
            return;
        }

        long remainingNanos =
                deadlineNanos - System.nanoTime();

        if (remainingNanos <= 0L) {
            finish(server);
            return;
        }

        long remainingSeconds =
                divideRoundUp(
                        remainingNanos,
                        NANOS_PER_SECOND
                );

        updateBossBar(
                server,
                remainingNanos,
                remainingSeconds
        );

        boolean warningDue = false;

        var iterator =
                PENDING_WARNINGS.iterator();

        while (iterator.hasNext()) {
            long warningSeconds =
                    iterator.next();

            if (remainingSeconds <= warningSeconds) {
                warningDue = true;
                iterator.remove();
            }
        }

        if (warningDue) {
            RestartWarningDeliveryService.deliver(
                    server,
                    ConfigManager.getConfig().restart,
                    remainingSeconds,
                    endEpochMillis
            );
        }

        lastDisplayedSeconds =
                remainingSeconds;
    }

    private static void finish(
            MinecraftServer server
    ) {
        String commandToRun =
                finalCommand;

        ServerSignals.LOGGER.info(
                "Restart countdown completed. " +
                        "Executing final command."
        );

        clearState();

        executeFinalCommand(
                server,
                commandToRun
        );
    }

    private static void executeFinalCommand(
            MinecraftServer server,
            String command
    ) {
        try {
            server.getCommandManager()
                    .executeWithPrefix(
                            server.getCommandSource()
                                    .withLevel(4),
                            command
                    );
        } catch (RuntimeException exception) {
            ServerSignals.LOGGER.error(
                    "Could not execute restart final command.",
                    exception
            );
        }
    }

    private static void createBossBar(
            MinecraftServer server,
            RestartConfig config,
            long remainingSeconds
    ) {
        if (!config.bossBar.enabled) {
            bossBar = null;
            return;
        }

        Text message =
                RestartWarningDeliveryService.createMessage(
                        server,
                        config,
                        remainingSeconds,
                        endEpochMillis
                );

        bossBar = new ServerBossBar(
                message,
                BossBarValueParser.parseColor(
                        config.bossBar.color
                ),
                BossBarValueParser.parseStyle(
                        config.bossBar.style
                )
        );

        bossBar.setPercent(1.0F);
        bossBar.setVisible(true);

        synchronizeBossBarPlayers(server);
    }

    private static void updateBossBar(
            MinecraftServer server,
            long remainingNanos,
            long remainingSeconds
    ) {
        if (bossBar == null) {
            return;
        }

        synchronizeBossBarPlayers(server);

        float progress =
                (float) remainingNanos /
                        (float) totalNanos;

        progress = Math.max(
                0.0F,
                Math.min(1.0F, progress)
        );

        bossBar.setPercent(progress);

        if (remainingSeconds ==
                lastDisplayedSeconds) {

            return;
        }

        Text updatedName =
                RestartWarningDeliveryService.createMessage(
                        server,
                        ConfigManager.getConfig().restart,
                        remainingSeconds,
                        endEpochMillis
                );

        bossBar.setName(updatedName);
    }

    private static void synchronizeBossBarPlayers(
            MinecraftServer server
    ) {
        if (bossBar == null) {
            return;
        }

        List<ServerPlayerEntity> onlinePlayers =
                server.getPlayerManager()
                        .getPlayerList();

        for (ServerPlayerEntity player :
                onlinePlayers) {

            if (!bossBar.getPlayers()
                    .contains(player)) {

                bossBar.addPlayer(player);
            }
        }

        for (ServerPlayerEntity player :
                List.copyOf(
                        bossBar.getPlayers()
                )) {

            if (!onlinePlayers.contains(player)) {
                bossBar.removePlayer(player);
            }
        }
    }

    private static long calculateRemainingSeconds() {
        long remainingNanos =
                deadlineNanos - System.nanoTime();

        if (remainingNanos <= 0L) {
            return 0L;
        }

        return divideRoundUp(
                remainingNanos,
                NANOS_PER_SECOND
        );
    }

    private static long divideRoundUp(
            long value,
            long divisor
    ) {
        return 1L +
                ((value - 1L) / divisor);
    }

    private static void clearState() {
        if (bossBar != null) {
            bossBar.clearPlayers();
            bossBar = null;
        }

        active = false;
        totalNanos = 0L;
        deadlineNanos = 0L;
        endEpochMillis = 0L;
        lastDisplayedSeconds =
                Long.MIN_VALUE;

        startedBy = "";
        finalCommand = "";

        PENDING_WARNINGS.clear();
    }

    public record RestartStatus(
            boolean active,
            long remainingSeconds,
            String startedBy
    ) {
    }
}