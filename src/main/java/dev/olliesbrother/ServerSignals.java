package dev.olliesbrother;


import dev.olliesbrother.commands.ServerSignalsCommands;
import dev.olliesbrother.config.ConfigManager;
import dev.olliesbrother.delivery.BossBarDeliveryService;
import dev.olliesbrother.events.MaintenanceConnectionHandler;
import dev.olliesbrother.maintenance.MaintenanceCountdownManager;
import dev.olliesbrother.restart.RestartCountdownManager;
import dev.olliesbrother.scheduler.AnnouncementScheduler;
import dev.olliesbrother.scheduler.CommandScheduler;
import dev.olliesbrother.data.SeenPlayerStore;
import dev.olliesbrother.events.VanillaPlayerMessageSuppressor;

import dev.olliesbrother.events.PlayerConnectionHandler;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.loader.api.FabricLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class ServerSignals implements ModInitializer {
    public static final String MOD_ID = "server_signals";

    public static final Logger LOGGER =
            LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        String version =
                getVersion();

        LOGGER.info(
                "Loading Server Signals {}...",
                version
        );

        boolean configLoaded =
                ConfigManager.load();

        if (configLoaded) {

            ConfigManager.logStartupSummary();

        } else {

            LOGGER.error(
                    "Server Signals configuration could not be loaded."
            );

            LOGGER.error(
                    "Fix the configuration error and run " +
                            "/serversignals reload."
            );
        }

        ConfigManager.load();
        SeenPlayerStore.load();

        BossBarDeliveryService.register();

        AnnouncementScheduler.register();
        CommandScheduler.register();
        RestartCountdownManager.register();
        MaintenanceCountdownManager.register();

        VanillaPlayerMessageSuppressor.register();
        MaintenanceConnectionHandler.register();
        PlayerConnectionHandler.register();


        ServerSignalsCommands.register();

        ServerLifecycleEvents.SERVER_STARTED.register(
                server -> {

                    if (configLoaded) {

                        LOGGER.info(
                                "Server Signals {} ready.",
                                version
                        );

                    } else {

                        LOGGER.warn(
                                "Server Signals {} started, but its " +
                                        "configuration did not load successfully.",
                                version
                        );
                    }
                }
        );
    }

    private static String getVersion() {

        return FabricLoader.getInstance()
                .getModContainer(
                        MOD_ID
                )
                .map(container ->
                        container
                                .getMetadata()
                                .getVersion()
                                .getFriendlyString()
                )
                .orElse("unknown");
    }
}