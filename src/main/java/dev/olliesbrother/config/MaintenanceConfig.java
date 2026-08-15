package dev.olliesbrother.config;

import java.util.ArrayList;
import java.util.List;

public final class MaintenanceConfig {

    public boolean enabled = false;

    public String reason =
            "The server is currently undergoing maintenance.";

    public boolean allowOperators = true;

    public List<String> allowedPlayers =
            new ArrayList<>();

    public boolean kickOnlinePlayersOnEnable = false;

    public MaintenanceScheduleConfig schedule =
            new MaintenanceScheduleConfig();

    public List<AnnouncementSectionConfig> disconnectSections =
            new ArrayList<>(List.of(
                    new AnnouncementSectionConfig(
                            "Server Maintenance\n",
                            new AnnouncementStyleConfig(
                                    "red",
                                    true,
                                    false,
                                    false,
                                    "",
                                    ""
                            )
                    ),
                    new AnnouncementSectionConfig(
                            "{reason}\n\n",
                            new AnnouncementStyleConfig(
                                    "yellow",
                                    false,
                                    false,
                                    false,
                                    "",
                                    ""
                            )
                    ),
                    new AnnouncementSectionConfig(
                            "Please try again later.",
                            new AnnouncementStyleConfig(
                                    "gray",
                                    false,
                                    false,
                                    false,
                                    "",
                                    ""
                            )
                    )
            ));

    public MaintenanceConfig() {
        // Required for Gson.
    }
}