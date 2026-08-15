package dev.olliesbrother.config;

import java.util.List;

public final class PlayerMessagesConfig {
    public boolean suppressVanillaJoinMessage = true;
    public boolean suppressVanillaLeaveMessage = true;
    public boolean sendJoinOnFirstJoin;

    public PlayerMessageConfig join =
            new PlayerMessageConfig(
                    false,
                    "chat",
                    "broadcast",
                    List.of(
                            new AnnouncementSectionConfig(
                                    "[+] ",
                                    new AnnouncementStyleConfig(
                                            "green",
                                            true,
                                            false,
                                            false,
                                            "",
                                            ""
                                    )
                            ),
                            new AnnouncementSectionConfig(
                                    "{player} joined the server!",
                                    new AnnouncementStyleConfig(
                                            "yellow",
                                            false,
                                            false,
                                            false,
                                            "",
                                            ""
                                    )
                            )
                    )
            );

    public PlayerMessageConfig firstJoin =
            new PlayerMessageConfig(
                    false,
                    "title",
                    "player_only",
                    List.of(
                            new AnnouncementSectionConfig(
                                    "Welcome, {player}!",
                                    new AnnouncementStyleConfig(
                                            "gold",
                                            true,
                                            false,
                                            false,
                                            "",
                                            ""
                                    )
                            )
                    )
            );

    public PlayerMessageConfig leave =
            new PlayerMessageConfig(
                    false,
                    "chat",
                    "broadcast",
                    List.of(
                            new AnnouncementSectionConfig(
                                    "[-] ",
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
                                    "{player} left the server.",
                                    new AnnouncementStyleConfig(
                                            "gray",
                                            false,
                                            false,
                                            false,
                                            "",
                                            ""
                                    )
                            )
                    )
            );
}