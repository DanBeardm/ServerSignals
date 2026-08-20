package dev.olliesbrother.config;

import java.util.ArrayList;
import java.util.List;

public final class MaintenanceConfig {

    public List<String> _comments =
            new ArrayList<>(List.of(
                    "Maintenance mode blocks new non-bypass players from joining.",
                    "Operators can bypass maintenance when allowOperators is true.",
                    "Add usernames or UUIDs to allowedPlayers for additional bypass access.",
                    "Scheduled maintenance warns players before enabling maintenance automatically."
            ));

    public boolean enabled =
            false;

    public String reason =
            "The server is currently undergoing maintenance.";

    public boolean allowOperators =
            true;

    public List<String> allowedPlayers =
            new ArrayList<>();

    public boolean kickOnlinePlayersOnEnable =
            false;

    public MaintenanceScheduleConfig schedule =
            createDefaultSchedule();

    public List<AnnouncementSectionConfig> disconnectSections =
            createDefaultDisconnectSections();


    private static MaintenanceScheduleConfig createDefaultSchedule() {

        MaintenanceScheduleConfig schedule =
                new MaintenanceScheduleConfig();

        schedule.enabled =
                true;

        schedule.warningDelivery =
                "chat";

        schedule.warningTimes =
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


        AnnouncementSectionConfig prefix =
                new AnnouncementSectionConfig();

        prefix.text =
                "Maintenance begins in ";

        prefix.style =
                new AnnouncementStyleConfig();

        prefix.style.color =
                "yellow";

        prefix.style.bold =
                true;

        prefix.style.italic =
                false;

        prefix.style.underlined =
                false;

        prefix.style.clickUrl =
                "";

        prefix.style.hoverText =
                "Maintenance begins at {maintenance_start_time}";


        AnnouncementSectionConfig remaining =
                new AnnouncementSectionConfig();

        remaining.text =
                "{maintenance_remaining}";

        remaining.style =
                new AnnouncementStyleConfig();

        remaining.style.color =
                "red";

        remaining.style.bold =
                true;

        remaining.style.italic =
                false;

        remaining.style.underlined =
                false;

        remaining.style.clickUrl =
                "";

        remaining.style.hoverText =
                "{maintenance_remaining_seconds} seconds remaining";


        AnnouncementSectionConfig reason =
                new AnnouncementSectionConfig();

        reason.text =
                " - {reason}";

        reason.style =
                new AnnouncementStyleConfig();

        reason.style.color =
                "gray";

        reason.style.bold =
                false;

        reason.style.italic =
                false;

        reason.style.underlined =
                false;

        reason.style.clickUrl =
                "";

        reason.style.hoverText =
                "";


        schedule.sections =
                new ArrayList<>(
                        List.of(
                                prefix,
                                remaining,
                                reason
                        )
                );


        schedule.titleTiming =
                new TitleTimingConfig();

        schedule.titleTiming.fadeInTicks =
                10;

        schedule.titleTiming.stayTicks =
                50;

        schedule.titleTiming.fadeOutTicks =
                10;


        schedule.bossBar =
                new MaintenanceBossBarConfig();

        schedule.bossBar.enabled =
                true;

        schedule.bossBar.color =
                "yellow";

        schedule.bossBar.style =
                "progress";

        return schedule;
    }


    private static List<AnnouncementSectionConfig>
    createDefaultDisconnectSections() {

        List<AnnouncementSectionConfig> sections =
                new ArrayList<>();


        AnnouncementSectionConfig title =
                new AnnouncementSectionConfig();

        title.text =
                "Server Maintenance\n";

        title.style =
                new AnnouncementStyleConfig();

        title.style.color =
                "red";

        title.style.bold =
                true;

        title.style.italic =
                false;

        title.style.underlined =
                false;

        title.style.clickUrl =
                "";

        title.style.hoverText =
                "";


        AnnouncementSectionConfig reason =
                new AnnouncementSectionConfig();

        reason.text =
                "{reason}\n\n";

        reason.style =
                new AnnouncementStyleConfig();

        reason.style.color =
                "yellow";

        reason.style.bold =
                false;

        reason.style.italic =
                false;

        reason.style.underlined =
                false;

        reason.style.clickUrl =
                "";

        reason.style.hoverText =
                "";


        AnnouncementSectionConfig footer =
                new AnnouncementSectionConfig();

        footer.text =
                "Please try again later.";

        footer.style =
                new AnnouncementStyleConfig();

        footer.style.color =
                "gray";

        footer.style.bold =
                false;

        footer.style.italic =
                false;

        footer.style.underlined =
                false;

        footer.style.clickUrl =
                "";

        footer.style.hoverText =
                "";


        sections.add(
                title
        );

        sections.add(
                reason
        );

        sections.add(
                footer
        );

        return sections;
    }
}