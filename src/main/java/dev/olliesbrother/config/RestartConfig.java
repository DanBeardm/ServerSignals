package dev.olliesbrother.config;

import java.util.ArrayList;
import java.util.List;

public final class RestartConfig {
    public boolean enabled = true;

    public String finalCommand = "stop";

    public String warningDelivery = "chat";

    public List<String> warningTimes =
            new ArrayList<>(List.of(
                    "10m",
                    "5m",
                    "1m",
                    "30s",
                    "10s",
                    "5s",
                    "4s",
                    "3s",
                    "2s",
                    "1s"
            ));

    public List<AnnouncementSectionConfig> sections =
            new ArrayList<>(List.of(
                    new AnnouncementSectionConfig(
                            "Server restarting in ",
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
                            "{restart_remaining}",
                            new AnnouncementStyleConfig(
                                    "yellow",
                                    true,
                                    false,
                                    false,
                                    "",
                                    ""
                            )
                    )
            ));

    public TitleTimingConfig titleTiming =
            new TitleTimingConfig();

    public RestartBossBarConfig bossBar =
            new RestartBossBarConfig();
}