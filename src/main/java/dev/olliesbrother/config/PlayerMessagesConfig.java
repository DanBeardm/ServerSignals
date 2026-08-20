package dev.olliesbrother.config;

import java.util.ArrayList;
import java.util.List;

public final class PlayerMessagesConfig {

    public List<String> _comments =
            new ArrayList<>(List.of(
                    "Custom messages for players joining, joining for the first time, or leaving the server.",
                    "Set suppressVanillaJoinMessage or suppressVanillaLeaveMessage to true when replacing Minecraft's default messages.",
                    "Audience options: broadcast or player_only.",
                    "Delivery options: chat, action_bar, title or subtitle.",
                    "Example messages are disabled by default. Set enabled to true when ready."
            ));

    /*
     * Vanilla message behaviour.
     */
    public boolean suppressVanillaJoinMessage =
            false;

    public boolean suppressVanillaLeaveMessage =
            false;

    /*
     * If true, the normal join message can also be sent when a
     * player joins the server for the first time.
     */
    public boolean sendJoinOnFirstJoin =
            true;

    /*
     * Normal join message.
     */
    public PlayerMessageConfig join =
            createJoinMessage();

    /*
     * First-ever join message.
     */
    public PlayerMessageConfig firstJoin =
            createFirstJoinMessage();

    /*
     * Leave message.
     */
    public PlayerMessageConfig leave =
            createLeaveMessage();


    private static PlayerMessageConfig createJoinMessage() {

        PlayerMessageConfig message =
                new PlayerMessageConfig();

        message.enabled =
                false;

        message.delivery =
                "chat";

        message.audience =
                "broadcast";

        AnnouncementSectionConfig prefix =
                new AnnouncementSectionConfig();

        prefix.text =
                "[+] ";

        prefix.style =
                new AnnouncementStyleConfig();

        prefix.style.color =
                "green";

        prefix.style.bold =
                true;

        prefix.style.italic =
                false;

        prefix.style.underlined =
                false;

        prefix.style.clickUrl =
                "";

        prefix.style.hoverText =
                "";


        AnnouncementSectionConfig player =
                new AnnouncementSectionConfig();

        player.text =
                "{player} joined the server!";

        player.style =
                new AnnouncementStyleConfig();

        player.style.color =
                "yellow";

        player.style.bold =
                false;

        player.style.italic =
                false;

        player.style.underlined =
                false;

        player.style.clickUrl =
                "";

        player.style.hoverText =
                "{online}/{max_players} players online";


        message.sections =
                new ArrayList<>(
                        List.of(
                                prefix,
                                player
                        )
                );

        message.titleTiming =
                createDefaultTitleTiming();

        return message;
    }


    private static PlayerMessageConfig createFirstJoinMessage() {

        PlayerMessageConfig message =
                new PlayerMessageConfig();

        message.enabled =
                false;

        message.delivery =
                "title";

        message.audience =
                "player_only";


        AnnouncementSectionConfig welcome =
                new AnnouncementSectionConfig();

        welcome.text =
                "Welcome, {player}!";

        welcome.style =
                new AnnouncementStyleConfig();

        welcome.style.color =
                "gold";

        welcome.style.bold =
                true;

        welcome.style.italic =
                false;

        welcome.style.underlined =
                false;

        welcome.style.clickUrl =
                "";

        welcome.style.hoverText =
                "";


        message.sections =
                new ArrayList<>(
                        List.of(
                                welcome
                        )
                );

        message.titleTiming =
                new TitleTimingConfig();

        message.titleTiming.fadeInTicks =
                10;

        message.titleTiming.stayTicks =
                80;

        message.titleTiming.fadeOutTicks =
                20;

        return message;
    }


    private static PlayerMessageConfig createLeaveMessage() {

        PlayerMessageConfig message =
                new PlayerMessageConfig();

        message.enabled =
                false;

        message.delivery =
                "chat";

        message.audience =
                "broadcast";


        AnnouncementSectionConfig prefix =
                new AnnouncementSectionConfig();

        prefix.text =
                "[-] ";

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
                "";


        AnnouncementSectionConfig player =
                new AnnouncementSectionConfig();

        player.text =
                "{player} left the server.";

        player.style =
                new AnnouncementStyleConfig();

        player.style.color =
                "gray";

        player.style.bold =
                false;

        player.style.italic =
                false;

        player.style.underlined =
                false;

        player.style.clickUrl =
                "";

        player.style.hoverText =
                "";


        message.sections =
                new ArrayList<>(
                        List.of(
                                prefix,
                                player
                        )
                );

        message.titleTiming =
                createDefaultTitleTiming();

        return message;
    }


    private static TitleTimingConfig createDefaultTitleTiming() {

        TitleTimingConfig timing =
                new TitleTimingConfig();

        timing.fadeInTicks =
                10;

        timing.stayTicks =
                70;

        timing.fadeOutTicks =
                20;

        return timing;
    }
}