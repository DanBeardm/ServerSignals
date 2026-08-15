package dev.olliesbrother.events;

import com.mojang.authlib.GameProfile;
import dev.olliesbrother.ServerSignals;
import dev.olliesbrother.config.ConfigManager;
import dev.olliesbrother.config.MaintenanceConfig;
import dev.olliesbrother.maintenance.MaintenanceManager;
import dev.olliesbrother.text.MaintenanceMessageFactory;
import net.fabricmc.fabric.api.networking.v1.ServerConfigurationConnectionEvents;
import net.minecraft.text.Text;

public final class MaintenanceConnectionHandler {

    private static boolean registered;

    private MaintenanceConnectionHandler() {
        // Utility class
    }

    public static void register() {
        if (registered) {
            return;
        }

        registered = true;

        ServerConfigurationConnectionEvents
                .BEFORE_CONFIGURE
                .register(
                        (handler, server) -> {

                            if (!MaintenanceManager
                                    .isEnabled()) {

                                return;
                            }

                            GameProfile profile =
                                    handler.getDebugProfile();

                            if (MaintenanceManager.canBypass(
                                    server,
                                    profile
                            )) {
                                return;
                            }

                            MaintenanceConfig config =
                                    ConfigManager
                                            .getConfig()
                                            .maintenance;

                            Text disconnectMessage =
                                    MaintenanceMessageFactory
                                            .create(
                                                    config,
                                                    profile
                                            );

                            handler.disconnect(
                                    disconnectMessage
                            );

                            ServerSignals.LOGGER.info(
                                    "Blocked {} from joining " +
                                            "during maintenance.",
                                    profile.getName()
                            );
                        }
                );
    }
}