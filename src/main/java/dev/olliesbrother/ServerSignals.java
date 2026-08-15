package dev.olliesbrother;

import dev.olliesbrother.commands.RestartCommands;
import dev.olliesbrother.commands.ServerSignalsCommands;
import dev.olliesbrother.config.ConfigManager;
import dev.olliesbrother.delivery.BossBarDeliveryService;
import dev.olliesbrother.restart.RestartCountdownManager;
import dev.olliesbrother.scheduler.AnnouncementScheduler;
import dev.olliesbrother.scheduler.CommandScheduler;
import dev.olliesbrother.data.SeenPlayerStore;
import dev.olliesbrother.events.VanillaPlayerMessageSuppressor;

import dev.olliesbrother.events.PlayerConnectionHandler;
import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class ServerSignals implements ModInitializer {
    public static final String MOD_ID = "server_signals";

    public static final Logger LOGGER =
            LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        LOGGER.info("Server Signals initializing.");

        ConfigManager.load();
        SeenPlayerStore.load();

        BossBarDeliveryService.register();
        AnnouncementScheduler.register();
        CommandScheduler.register();
        RestartCountdownManager.register();

        VanillaPlayerMessageSuppressor.register();
        PlayerConnectionHandler.register();

        ServerSignalsCommands.register();

        LOGGER.info("Server Signals initialized.");
    }
}