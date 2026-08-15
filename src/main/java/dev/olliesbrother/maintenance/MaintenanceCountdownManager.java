package dev.olliesbrother.maintenance;

import dev.olliesbrother.ServerSignals;
import dev.olliesbrother.config.ConfigManager;
import dev.olliesbrother.config.MaintenanceConfig;
import dev.olliesbrother.config.MaintenanceScheduleConfig;
import dev.olliesbrother.delivery.BossBarValueParser;
import dev.olliesbrother.delivery.MaintenanceWarningDeliveryService;
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

public final class MaintenanceCountdownManager {

    private static final long NANOS_PER_SECOND =
            1_000_000_000L;

    private static boolean registered;
    private static boolean active;

    private static long totalNanos;
    private static long deadlineNanos;
    private static long startEpochMillis;

    private static long lastDisplayedSeconds =
            Long.MIN_VALUE;

    private static String scheduledReason = "";
    private static String startedBy = "";

    private static final Set<Long> PENDING_WARNINGS =
            new HashSet<>();

    private static ServerBossBar bossBar;

    private MaintenanceCountdownManager() {
    }

    public static void register() {
        if (registered) {
            return;
        }

        registered = true;

        ServerTickEvents.END_SERVER_TICK.register(
                MaintenanceCountdownManager::tick
        );

        ServerLifecycleEvents.SERVER_STOPPING.register(
                server -> clearState()
        );
    }

    public static boolean start(
            MinecraftServer server,
            long durationSeconds,
            String reason,
            String sourceName
    ) {
        if (active ||
                MaintenanceManager.isEnabled()) {

            return false;
        }

        MaintenanceConfig maintenance =
                ConfigManager.getConfig()
                        .maintenance;

        MaintenanceScheduleConfig schedule =
                maintenance.schedule;

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

            startEpochMillis =
                    Math.addExact(
                            System.currentTimeMillis(),
                            Math.multiplyExact(
                                    durationSeconds,
                                    1_000L
                            )
                    );

        } catch (ArithmeticException exception) {
            throw new IllegalArgumentException(
                    "Maintenance duration is too large.",
                    exception
            );
        }

        scheduledReason =
                reason == null ||
                        reason.isBlank()
                        ? maintenance.reason
                        : reason.trim();

        startedBy =
                sourceName;

        lastDisplayedSeconds =
                durationSeconds;

        PENDING_WARNINGS.clear();

        for (String warning :
                schedule.warningTimes) {

            long warningSeconds =
                    DurationParser.toSeconds(warning);

            if (warningSeconds < durationSeconds) {
                PENDING_WARNINGS.add(
                        warningSeconds
                );
            }
        }

        active = true;

        createBossBar(
                server,
                schedule,
                durationSeconds
        );

        MaintenanceWarningDeliveryService.deliver(
                server,
                schedule,
                scheduledReason,
                durationSeconds,
                startEpochMillis
        );

        ServerSignals.LOGGER.info(
                "Maintenance scheduled by {} for {} second(s). " +
                        "Reason: {}",
                sourceName,
                durationSeconds,
                scheduledReason
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
                "Scheduled maintenance cancelled by {}.",
                sourceName
        );

        clearState();

        return true;
    }

    public static MaintenanceScheduleStatus getStatus() {
        if (!active) {
            return new MaintenanceScheduleStatus(
                    false,
                    0L,
                    "",
                    ""
            );
        }

        return new MaintenanceScheduleStatus(
                true,
                calculateRemainingSeconds(),
                scheduledReason,
                startedBy
        );
    }

    private static void tick(
            MinecraftServer server
    ) {
        if (!active) {
            return;
        }

        /*
         * If maintenance was enabled manually or by
         * a config reload, the schedule is no longer needed.
         */
        if (MaintenanceManager.isEnabled()) {
            clearState();
            return;
        }

        long remainingNanos =
                deadlineNanos -
                        System.nanoTime();

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
            MaintenanceWarningDeliveryService.deliver(
                    server,
                    ConfigManager.getConfig()
                            .maintenance
                            .schedule,
                    scheduledReason,
                    remainingSeconds,
                    startEpochMillis
            );
        }

        lastDisplayedSeconds =
                remainingSeconds;
    }

    private static void finish(
            MinecraftServer server
    ) {
        String reason =
                scheduledReason;

        ServerSignals.LOGGER.info(
                "Maintenance countdown completed."
        );

        clearState();

        MaintenanceManager.EnableResult result =
                MaintenanceManager.enable(
                        server,
                        reason
                );

        if (!result.success()) {
            ServerSignals.LOGGER.error(
                    "Scheduled maintenance countdown finished, " +
                            "but maintenance mode could not be enabled."
            );

            server.getPlayerManager().broadcast(
                    Text.literal(
                            "Scheduled maintenance could not be enabled. " +
                                    "Please contact an administrator."
                    ),
                    false
            );

            return;
        }

        ServerSignals.LOGGER.info(
                "Scheduled maintenance enabled successfully. " +
                        "{} player(s) kicked.",
                result.kickedPlayers()
        );
    }

    private static void createBossBar(
            MinecraftServer server,
            MaintenanceScheduleConfig config,
            long remainingSeconds
    ) {
        if (!config.bossBar.enabled) {
            bossBar = null;
            return;
        }

        Text message =
                MaintenanceWarningDeliveryService.createMessage(
                        server,
                        config,
                        scheduledReason,
                        remainingSeconds,
                        startEpochMillis
                );

        bossBar =
                new ServerBossBar(
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

        synchronizePlayers(server);
    }

    private static void updateBossBar(
            MinecraftServer server,
            long remainingNanos,
            long remainingSeconds
    ) {
        if (bossBar == null) {
            return;
        }

        synchronizePlayers(server);

        float progress =
                (float) remainingNanos /
                        (float) totalNanos;

        progress =
                Math.max(
                        0.0F,
                        Math.min(
                                1.0F,
                                progress
                        )
                );

        bossBar.setPercent(progress);

        if (remainingSeconds ==
                lastDisplayedSeconds) {

            return;
        }

        Text updatedText =
                MaintenanceWarningDeliveryService.createMessage(
                        server,
                        ConfigManager.getConfig()
                                .maintenance
                                .schedule,
                        scheduledReason,
                        remainingSeconds,
                        startEpochMillis
                );

        bossBar.setName(updatedText);
    }

    private static void synchronizePlayers(
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
                deadlineNanos -
                        System.nanoTime();

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
        startEpochMillis = 0L;

        lastDisplayedSeconds =
                Long.MIN_VALUE;

        scheduledReason = "";
        startedBy = "";

        PENDING_WARNINGS.clear();
    }

    public record MaintenanceScheduleStatus(
            boolean active,
            long remainingSeconds,
            String reason,
            String startedBy
    ) {
    }
}