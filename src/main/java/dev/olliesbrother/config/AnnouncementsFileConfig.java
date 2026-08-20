package dev.olliesbrother.config;

import java.util.ArrayList;
import java.util.List;

public final class AnnouncementsFileConfig {

    public List<String> _comments =
            new ArrayList<>(List.of(
                    "Timed messages automatically shown to online players.",
                    "Intervals support s, m, h and d, for example: 30s, 15m, 1h or 2d.",
                    "Delivery options: chat, action_bar, title, subtitle or boss_bar.",
                    "Example announcements are disabled by default. Set enabled to true when ready."
            ));

    public List<AnnouncementConfig> announcements =
            createDefaults();

    private static List<AnnouncementConfig> createDefaults() {

        List<AnnouncementConfig> defaults =
                new ArrayList<>();

        /*
         * Example 1:
         * Basic chat announcement.
         */
        AnnouncementConfig voteReminder =
                new AnnouncementConfig();

        voteReminder.id =
                "vote_reminder";

        voteReminder.interval =
                "1h";

        voteReminder.enabled =
                false;

        voteReminder.onlyWhenPlayersOnline =
                true;

        voteReminder.delivery =
                "chat";

        AnnouncementSectionConfig votePrefix =
                new AnnouncementSectionConfig();

        votePrefix.text =
                "[Server] ";

        votePrefix.style =
                new AnnouncementStyleConfig();

        votePrefix.style.color =
                "gold";

        votePrefix.style.bold =
                true;

        votePrefix.style.italic =
                false;

        votePrefix.style.underlined =
                false;

        votePrefix.style.clickUrl =
                "";

        votePrefix.style.hoverText =
                "";


        AnnouncementSectionConfig voteMessage =
                new AnnouncementSectionConfig();

        voteMessage.text =
                "Remember to vote for the server!";

        voteMessage.style =
                new AnnouncementStyleConfig();

        voteMessage.style.color =
                "yellow";

        voteMessage.style.bold =
                false;

        voteMessage.style.italic =
                false;

        voteMessage.style.underlined =
                false;

        voteMessage.style.clickUrl =
                "";

        voteMessage.style.hoverText =
                "Thanks for supporting the server!";


        voteReminder.sections =
                new ArrayList<>(
                        List.of(
                                votePrefix,
                                voteMessage
                        )
                );

        defaults.add(
                voteReminder
        );


        /*
         * Example 2:
         * Demonstrates placeholders and action-bar delivery.
         */
        AnnouncementConfig serverStatus =
                new AnnouncementConfig();

        serverStatus.id =
                "server_status";

        serverStatus.interval =
                "30m";

        serverStatus.enabled =
                false;

        serverStatus.onlyWhenPlayersOnline =
                true;

        serverStatus.delivery =
                "action_bar";


        AnnouncementSectionConfig statusSection =
                new AnnouncementSectionConfig();

        statusSection.text =
                "{online}/{max_players} players online • Uptime: {uptime}";

        statusSection.style =
                new AnnouncementStyleConfig();

        statusSection.style.color =
                "aqua";

        statusSection.style.bold =
                false;

        statusSection.style.italic =
                false;

        statusSection.style.underlined =
                false;

        statusSection.style.clickUrl =
                "";

        statusSection.style.hoverText =
                "";


        serverStatus.sections =
                new ArrayList<>(
                        List.of(
                                statusSection
                        )
                );

        defaults.add(
                serverStatus
        );

        return defaults;
    }
}