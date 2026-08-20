package dev.olliesbrother.config;

import java.util.ArrayList;
import java.util.List;

public final class RestartConfig {

    public List<String> _comments =
            new ArrayList<>(List.of(
                    "Controls manual server restart countdowns.",
                    "The final command runs when the countdown reaches zero.",
                    "The default stop command works well when your hosting panel automatically restarts stopped servers.",
                    "Warning delivery options: chat, action_bar, title or subtitle."
            ));

    public boolean enabled =
            true;

    public String finalCommand =
            "stop";

    public String warningDelivery =
            "chat";

    public List<String> warningTimes =
            new ArrayList<>(
                    List.of(
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
                    )
            );

    public List<AnnouncementSectionConfig> sections =
            createDefaultSections();

    public TitleTimingConfig titleTiming =
            createDefaultTitleTiming();

    public RestartBossBarConfig bossBar =
            createDefaultBossBar();


    private static List<AnnouncementSectionConfig> createDefaultSections() {

        List<AnnouncementSectionConfig> sections =
                new ArrayList<>();


        AnnouncementSectionConfig prefix =
                new AnnouncementSectionConfig();

        prefix.text =
                "Server restarting in ";

        prefix.style =
                new AnnouncementStyleConfig();

        prefix.style.color =
                "red";

        prefix.style.bold =
                true;

        prefix.style.italic =
                false;

        prefix.style.underlined =
                false;

        prefix.style.clickUrl =
                "";

        prefix.style.hoverText =
                "Restart scheduled for {restart_end_time}";


        AnnouncementSectionConfig remaining =
                new AnnouncementSectionConfig();

        remaining.text =
                "{restart_remaining}";

        remaining.style =
                new AnnouncementStyleConfig();

        remaining.style.color =
                "yellow";

        remaining.style.bold =
                true;

        remaining.style.italic =
                false;

        remaining.style.underlined =
                false;

        remaining.style.clickUrl =
                "";

        remaining.style.hoverText =
                "{restart_remaining_seconds} seconds remaining";


        sections.add(
                prefix
        );

        sections.add(
                remaining
        );

        return sections;
    }


    private static TitleTimingConfig createDefaultTitleTiming() {

        TitleTimingConfig timing =
                new TitleTimingConfig();

        timing.fadeInTicks =
                10;

        timing.stayTicks =
                50;

        timing.fadeOutTicks =
                10;

        return timing;
    }


    private static RestartBossBarConfig createDefaultBossBar() {

        RestartBossBarConfig bossBar =
                new RestartBossBarConfig();

        bossBar.enabled =
                true;

        bossBar.color =
                "red";

        bossBar.style =
                "progress";

        return bossBar;
    }
}