package dev.olliesbrother.config;

import java.util.ArrayList;
import java.util.List;

public final class ServerSignalsConfig {
    public List<AnnouncementConfig> announcements =
            new ArrayList<>(List.of(
                    new AnnouncementConfig(
                            "vote_reminder",
                            List.of(
                                    new AnnouncementSectionConfig(
                                            "[Server] ",
                                            new AnnouncementStyleConfig(
                                                    "gold",
                                                    true,
                                                    false,
                                                    false,
                                                    "",
                                                    ""
                                            )
                                    ),
                                    new AnnouncementSectionConfig(
                                            "Support us by voting! ",
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
                                            "[VOTE NOW]",
                                            new AnnouncementStyleConfig(
                                                    "green",
                                                    true,
                                                    false,
                                                    true,
                                                    "",
                                                    "Click to open the voting page"
                                            )
                                    )
                            ),
                            "1h",
                            true,
                            true
                    ),
                    new AnnouncementConfig(
                            "discord_reminder",
                            List.of(
                                    new AnnouncementSectionConfig(
                                            "[Discord] ",
                                            new AnnouncementStyleConfig(
                                                    "dark_aqua",
                                                    true,
                                                    false,
                                                    false,
                                                    "",
                                                    ""
                                            )
                                    ),
                                    new AnnouncementSectionConfig(
                                            "Join our community ",
                                            new AnnouncementStyleConfig(
                                                    "aqua",
                                                    false,
                                                    false,
                                                    false,
                                                    "",
                                                    ""
                                            )
                                    ),
                                    new AnnouncementSectionConfig(
                                            "[JOIN NOW]",
                                            new AnnouncementStyleConfig(
                                                    "#5865f2",
                                                    true,
                                                    false,
                                                    true,
                                                    "",
                                                    "Click to join our Discord"
                                            )
                                    )
                            ),
                            "30m",
                            false,
                            true
                    )
            ));
    public List<ScheduledCommandConfig> scheduledCommands =
            new ArrayList<>();

    public PlayerMessagesConfig playerMessages =
            new PlayerMessagesConfig();

    public RestartConfig restart =
            new RestartConfig();
}