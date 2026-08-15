package dev.olliesbrother.delivery;

import dev.olliesbrother.config.AnnouncementConfig;
import dev.olliesbrother.config.BossBarConfig;
import dev.olliesbrother.util.DurationParser;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.entity.boss.BossBar;
import net.minecraft.entity.boss.ServerBossBar;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class BossBarDeliveryService {
    private static final Map<String, ActiveBossBar> ACTIVE_BOSS_BARS =
            new HashMap<>();

    private static boolean registered;

    private BossBarDeliveryService() {
        // Utility class
    }

    public static void register() {
        if (registered) {
            return;
        }

        registered = true;

        ServerTickEvents.END_SERVER_TICK.register(
                BossBarDeliveryService::tick
        );
    }

    public static void show(
            MinecraftServer server,
            AnnouncementConfig announcement,
            Text message
    ) {
        remove(announcement.id);

        BossBarConfig config = announcement.bossBar;

        BossBar.Color color =
                BossBarValueParser.parseColor(config.color);

        BossBar.Style style =
                BossBarValueParser.parseStyle(config.style);

        long durationTicks =
                DurationParser.toTicks(config.duration);

        ServerBossBar bossBar =
                new ServerBossBar(
                        message,
                        color,
                        style
                );

        bossBar.setPercent(1.0F);
        bossBar.setVisible(true);

        for (ServerPlayerEntity player :
                server.getPlayerManager().getPlayerList()) {

            bossBar.addPlayer(player);
        }

        ACTIVE_BOSS_BARS.put(
                announcement.id,
                new ActiveBossBar(
                        bossBar,
                        durationTicks,
                        durationTicks,
                        config.countdown
                )
        );
    }

    private static void tick(MinecraftServer server) {
        var iterator =
                ACTIVE_BOSS_BARS.entrySet().iterator();

        while (iterator.hasNext()) {
            Map.Entry<String, ActiveBossBar> entry =
                    iterator.next();

            ActiveBossBar active = entry.getValue();

            synchronizePlayers(
                    server,
                    active.bossBar
            );

            active.remainingTicks--;

            if (active.countdown) {
                float progress =
                        (float) active.remainingTicks /
                                (float) active.totalTicks;

                progress = Math.max(
                        0.0F,
                        Math.min(1.0F, progress)
                );

                active.bossBar.setPercent(progress);
            }

            if (active.remainingTicks > 0) {
                continue;
            }

            active.bossBar.clearPlayers();
            iterator.remove();
        }
    }

    private static void synchronizePlayers(
            MinecraftServer server,
            ServerBossBar bossBar
    ) {
        List<ServerPlayerEntity> onlinePlayers =
                server.getPlayerManager().getPlayerList();

        // Add players who joined while the bar was active.
        for (ServerPlayerEntity player : onlinePlayers) {
            if (!bossBar.getPlayers().contains(player)) {
                bossBar.addPlayer(player);
            }
        }

        // Remove players who disconnected.
        for (ServerPlayerEntity player :
                List.copyOf(bossBar.getPlayers())) {

            if (!onlinePlayers.contains(player)) {
                bossBar.removePlayer(player);
            }
        }
    }

    private static void remove(String announcementId) {
        ActiveBossBar existing =
                ACTIVE_BOSS_BARS.remove(
                        announcementId
                );

        if (existing != null) {
            existing.bossBar.clearPlayers();
        }
    }

    public static void clearAll() {
        for (ActiveBossBar active :
                ACTIVE_BOSS_BARS.values()) {

            active.bossBar.clearPlayers();
        }

        ACTIVE_BOSS_BARS.clear();
    }

    private static final class ActiveBossBar {
        private final ServerBossBar bossBar;
        private final long totalTicks;
        private final boolean countdown;

        private long remainingTicks;

        private ActiveBossBar(
                ServerBossBar bossBar,
                long remainingTicks,
                long totalTicks,
                boolean countdown
        ) {
            this.bossBar = bossBar;
            this.remainingTicks = remainingTicks;
            this.totalTicks = totalTicks;
            this.countdown = countdown;
        }
    }
}