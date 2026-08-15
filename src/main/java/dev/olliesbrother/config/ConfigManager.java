package dev.olliesbrother.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;
import dev.olliesbrother.ServerSignals;
import dev.olliesbrother.delivery.BossBarValueParser;
import dev.olliesbrother.delivery.DeliveryMode;
import dev.olliesbrother.delivery.PlayerMessageAudience;
import dev.olliesbrother.util.DurationParser;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.util.Formatting;
import java.util.ArrayList;
import java.util.List;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Locale;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;

public final class ConfigManager {
    private static final Gson GSON = new GsonBuilder()
            .setPrettyPrinting()
            .create();

    private static final Path CONFIG_PATH =
            FabricLoader.getInstance()
                    .getConfigDir()
                    .resolve("ServerSignals/server_signals.json");

    private static ServerSignalsConfig config =
            new ServerSignalsConfig();

    private ConfigManager() {
        // Utility class
    }

    public static ServerSignalsConfig getConfig() {
        return config;
    }

    public static boolean load() {
        try {
            Files.createDirectories(CONFIG_PATH.getParent());

            if (Files.notExists(CONFIG_PATH)) {
                config = new ServerSignalsConfig();
                save();

                ServerSignals.LOGGER.info(
                        "Created default config at {}",
                        CONFIG_PATH
                );

                return true;
            }

            ServerSignalsConfig loadedConfig;

            try (Reader reader = Files.newBufferedReader(
                    CONFIG_PATH,
                    StandardCharsets.UTF_8
            )) {
                loadedConfig = GSON.fromJson(
                        reader,
                        ServerSignalsConfig.class
                );
            }

            validate(loadedConfig);

            // Replace the active config only after validation succeeds.
            config = loadedConfig;

            ServerSignals.LOGGER.info(
                    "Loaded {} announcements from {}",
                    config.announcements.size(),
                    CONFIG_PATH
            );

            return true;

        } catch (IOException | JsonParseException exception) {
            ServerSignals.LOGGER.error(
                    "Could not load Server Signals config. " +
                            "Keeping the previous configuration.",
                    exception
            );

            return false;
        }
    }

    private static void save() throws IOException {
        try (Writer writer = Files.newBufferedWriter(
                CONFIG_PATH,
                StandardCharsets.UTF_8
        )) {
            GSON.toJson(config, writer);
        }
    }

    private static void validate(
            ServerSignalsConfig loadedConfig
    ) {
        if (loadedConfig == null) {
            throw new JsonParseException(
                    "The config file is empty."
            );
        }

        if (loadedConfig.announcements == null ||
                loadedConfig.announcements.isEmpty()) {
            throw new JsonParseException(
                    "The announcements list cannot be empty."
            );
        }

        Set<String> usedIds = new HashSet<>();

        for (int index = 0;
             index < loadedConfig.announcements.size();
             index++) {

            AnnouncementConfig announcement =
                    loadedConfig.announcements.get(index);

            if (announcement == null) {
                throw new JsonParseException(
                        "Announcement at index " + index + " is null."
                );
            }

            if (announcement.id == null ||
                    announcement.id.isBlank()) {
                throw new JsonParseException(
                        "Announcement at index " + index +
                                " has no ID."
                );
            }

            if (!announcement.id.matches("[a-z0-9_-]+")) {
                throw new JsonParseException(
                        "Invalid announcement ID: " +
                                announcement.id +
                                ". Use lowercase letters, numbers, " +
                                "underscores and hyphens only."
                );
            }

            if (!usedIds.add(announcement.id)) {
                throw new JsonParseException(
                        "Duplicate announcement ID: " +
                                announcement.id
                );
            }

            if (announcement.sections == null ||
                    announcement.sections.isEmpty()) {
                throw new JsonParseException(
                        "Announcement '" +
                                announcement.id +
                                "' must contain at least one section."
                );
            }

            for (int sectionIndex = 0;
                 sectionIndex < announcement.sections.size();
                 sectionIndex++) {

                AnnouncementSectionConfig section =
                        announcement.sections.get(sectionIndex);

                if (section == null) {
                    throw new JsonParseException(
                            "Announcement '" +
                                    announcement.id +
                                    "' has a null section at position " +
                                    (sectionIndex + 1) +
                                    "."
                    );
                }

                if (section.text == null ||
                        section.text.isEmpty()) {
                    throw new JsonParseException(
                            "Announcement '" +
                                    announcement.id +
                                    "' section " +
                                    (sectionIndex + 1) +
                                    " has no text."
                    );
                }

                validateSectionStyle(
                        announcement.id,
                        sectionIndex,
                        section
                );
                validateDelivery(announcement);
                validateBossBar(announcement);

                if (loadedConfig.scheduledCommands == null) {
                    loadedConfig.scheduledCommands =
                            new ArrayList<>();
                }

                validateScheduledCommands(
                        loadedConfig.scheduledCommands
                );

                if (loadedConfig.playerMessages == null) {
                    loadedConfig.playerMessages =
                            new PlayerMessagesConfig();
                }

                validatePlayerMessages(
                        loadedConfig.playerMessages
                );

                if (loadedConfig.restart == null) {
                    loadedConfig.restart =
                            new RestartConfig();
                }

                validateRestart(loadedConfig.restart);

                if (loadedConfig.maintenance == null) {
                    loadedConfig.maintenance =
                            new MaintenanceConfig();
                }

                validateMaintenance(
                        loadedConfig.maintenance
                );


            }
        }
    }

    private static void validateMaintenance(
            MaintenanceConfig maintenance
    ) {
        if (maintenance.reason == null ||
                maintenance.reason.isBlank()) {

            maintenance.reason =
                    "The server is currently undergoing maintenance.";
        }

        if (maintenance.allowedPlayers == null) {
            maintenance.allowedPlayers =
                    new ArrayList<>();
        }

        maintenance.allowedPlayers.removeIf(
                player ->
                        player == null ||
                                player.isBlank()
        );

        for (int index = 0;
             index < maintenance.allowedPlayers.size();
             index++) {

            maintenance.allowedPlayers.set(
                    index,
                    maintenance.allowedPlayers
                            .get(index)
                            .trim()
            );
        }

        if (maintenance.disconnectSections == null ||
                maintenance.disconnectSections.isEmpty()) {

            throw new JsonParseException(
                    "Maintenance mode must contain " +
                            "at least one disconnect section."
            );
        }

        for (int index = 0;
             index < maintenance.disconnectSections.size();
             index++) {

            AnnouncementSectionConfig section =
                    maintenance.disconnectSections.get(index);

            if (section == null ||
                    section.text == null ||
                    section.text.isEmpty()) {

                throw new JsonParseException(
                        "Maintenance disconnect section " +
                                (index + 1) +
                                " has no text."
                );
            }

            validateSectionStyle(
                    "maintenance",
                    index,
                    section
            );
        }

        if (maintenance.schedule == null) {
            maintenance.schedule =
                    new MaintenanceScheduleConfig();
        }

        validateMaintenanceSchedule(
                maintenance.schedule
        );
    }

    private static void validateMaintenanceSchedule(
            MaintenanceScheduleConfig schedule
    ) {
        DeliveryMode deliveryMode;

        try {
            deliveryMode =
                    DeliveryMode.fromConfig(
                            schedule.warningDelivery
                    );
        } catch (IllegalArgumentException exception) {
            throw new JsonParseException(
                    "Maintenance schedule warningDelivery must be " +
                            "chat, action_bar, title or subtitle."
            );
        }

        if (deliveryMode == DeliveryMode.BOSS_BAR) {
            throw new JsonParseException(
                    "Maintenance schedule warningDelivery cannot " +
                            "be boss_bar. Use the separate bossBar settings."
            );
        }

        schedule.warningDelivery =
                deliveryMode.getConfigName();

        validateMaintenanceWarningTimes(schedule);
        validateMaintenanceScheduleSections(schedule);

        if (schedule.titleTiming == null) {
            schedule.titleTiming =
                    new TitleTimingConfig();
        }

        validateTitleTiming(
                "maintenance schedule",
                schedule.titleTiming
        );

        if (schedule.bossBar == null) {
            schedule.bossBar =
                    new MaintenanceBossBarConfig();
        }

        try {
            BossBarValueParser.parseColor(
                    schedule.bossBar.color
            );
        } catch (IllegalArgumentException exception) {
            throw new JsonParseException(
                    "Maintenance schedule boss-bar colour is invalid: " +
                            exception.getMessage()
            );
        }

        try {
            BossBarValueParser.parseStyle(
                    schedule.bossBar.style
            );
        } catch (IllegalArgumentException exception) {
            throw new JsonParseException(
                    "Maintenance schedule boss-bar style is invalid: " +
                            exception.getMessage()
            );
        }
    }

    private static void validateMaintenanceWarningTimes(
            MaintenanceScheduleConfig schedule
    ) {
        if (schedule.warningTimes == null) {
            schedule.warningTimes =
                    new ArrayList<>();

            return;
        }

        Set<Long> usedTimes =
                new HashSet<>();

        List<String> normalized =
                new ArrayList<>();

        for (String warning :
                schedule.warningTimes) {

            long seconds;

            try {
                seconds =
                        DurationParser.toSeconds(warning);
            } catch (IllegalArgumentException exception) {
                throw new JsonParseException(
                        "Invalid maintenance warning time: " +
                                exception.getMessage()
                );
            }

            if (!usedTimes.add(seconds)) {
                continue;
            }

            normalized.add(
                    warning.trim()
                            .toLowerCase(Locale.ROOT)
            );
        }

        schedule.warningTimes =
                normalized;
    }

    private static void validateMaintenanceScheduleSections(
            MaintenanceScheduleConfig schedule
    ) {
        if (schedule.sections == null ||
                schedule.sections.isEmpty()) {

            throw new JsonParseException(
                    "Maintenance schedule must contain " +
                            "at least one message section."
            );
        }

        for (int index = 0;
             index < schedule.sections.size();
             index++) {

            AnnouncementSectionConfig section =
                    schedule.sections.get(index);

            if (section == null ||
                    section.text == null ||
                    section.text.isEmpty()) {

                throw new JsonParseException(
                        "Maintenance schedule section " +
                                (index + 1) +
                                " has no text."
                );
            }

            validateSectionStyle(
                    "maintenance schedule",
                    index,
                    section
            );
        }
    }

    private static void validateRestart(
            RestartConfig restart
    ) {
        if (restart.finalCommand == null ||
                restart.finalCommand.isBlank()) {

            throw new JsonParseException(
                    "Restart finalCommand cannot be empty."
            );
        }

        if (restart.finalCommand.contains("\n") ||
                restart.finalCommand.contains("\r")) {

            throw new JsonParseException(
                    "Restart finalCommand must be on one line."
            );
        }

        restart.finalCommand =
                restart.finalCommand.trim();

        validateRestartDelivery(restart);
        validateRestartWarnings(restart);
        validateRestartSections(restart);
        validateRestartBossBar(restart);

        if (restart.titleTiming == null) {
            restart.titleTiming =
                    new TitleTimingConfig();
        }

        validateTitleTiming(
                "restart",
                restart.titleTiming
        );
    }

    private static void validateRestartDelivery(
            RestartConfig restart
    ) {
        DeliveryMode mode;

        try {
            mode = DeliveryMode.fromConfig(
                    restart.warningDelivery
            );
        } catch (IllegalArgumentException exception) {
            throw new JsonParseException(
                    "Restart warningDelivery must be chat, " +
                            "action_bar, title or subtitle."
            );
        }

        if (mode == DeliveryMode.BOSS_BAR) {
            throw new JsonParseException(
                    "Restart warningDelivery cannot be boss_bar. " +
                            "Use the separate restart bossBar setting."
            );
        }

        restart.warningDelivery =
                mode.getConfigName();
    }

    private static void validateRestartWarnings(
            RestartConfig restart
    ) {
        if (restart.warningTimes == null) {
            restart.warningTimes =
                    new ArrayList<>();

            return;
        }

        Set<Long> usedSeconds =
                new HashSet<>();

        List<String> normalizedWarnings =
                new ArrayList<>();

        for (int index = 0;
             index < restart.warningTimes.size();
             index++) {

            String warning =
                    restart.warningTimes.get(index);

            long warningSeconds;

            try {
                warningSeconds =
                        DurationParser.toSeconds(warning);
            } catch (IllegalArgumentException exception) {
                throw new JsonParseException(
                        "Restart warning time " +
                                (index + 1) +
                                " is invalid: " +
                                exception.getMessage()
                );
            }

            if (!usedSeconds.add(warningSeconds)) {
                continue;
            }

            normalizedWarnings.add(
                    warning.trim()
                            .toLowerCase(Locale.ROOT)
            );
        }

        restart.warningTimes =
                normalizedWarnings;
    }

    private static void validateRestartSections(
            RestartConfig restart
    ) {
        if (restart.sections == null ||
                restart.sections.isEmpty()) {

            throw new JsonParseException(
                    "Restart messages must contain " +
                            "at least one section."
            );
        }

        for (int index = 0;
             index < restart.sections.size();
             index++) {

            AnnouncementSectionConfig section =
                    restart.sections.get(index);

            if (section == null ||
                    section.text == null ||
                    section.text.isEmpty()) {

                throw new JsonParseException(
                        "Restart message section " +
                                (index + 1) +
                                " has no text."
                );
            }

            validateSectionStyle(
                    "restart",
                    index,
                    section
            );
        }
    }

    private static void validateRestartBossBar(
            RestartConfig restart
    ) {
        if (restart.bossBar == null) {
            restart.bossBar =
                    new RestartBossBarConfig();
        }

        try {
            BossBarValueParser.parseColor(
                    restart.bossBar.color
            );
        } catch (IllegalArgumentException exception) {
            throw new JsonParseException(
                    "Restart boss-bar colour is invalid: " +
                            exception.getMessage()
            );
        }

        try {
            BossBarValueParser.parseStyle(
                    restart.bossBar.style
            );
        } catch (IllegalArgumentException exception) {
            throw new JsonParseException(
                    "Restart boss-bar style is invalid: " +
                            exception.getMessage()
            );
        }
    }

    private static void validatePlayerMessages(
            PlayerMessagesConfig playerMessages
    ) {
        validatePlayerMessage(
                "join",
                playerMessages.join
        );

        validatePlayerMessage(
                "first_join",
                playerMessages.firstJoin
        );

        validatePlayerMessage(
                "leave",
                playerMessages.leave
        );
    }

    private static void validatePlayerMessage(
            String messageId,
            PlayerMessageConfig message
    ) {
        if (message == null) {
            throw new JsonParseException(
                    "Player message '" +
                            messageId +
                            "' cannot be null."
            );
        }

        if (message.sections == null ||
                message.sections.isEmpty()) {

            throw new JsonParseException(
                    "Player message '" +
                            messageId +
                            "' must contain at least one section."
            );
        }

        for (int index = 0;
             index < message.sections.size();
             index++) {

            AnnouncementSectionConfig section =
                    message.sections.get(index);

            if (section == null ||
                    section.text == null ||
                    section.text.isEmpty()) {

                throw new JsonParseException(
                        "Player message '" +
                                messageId +
                                "' section " +
                                (index + 1) +
                                " has no text."
                );
            }

            validateSectionStyle(
                    "player message " + messageId,
                    index,
                    section
            );
        }

        DeliveryMode deliveryMode;

        try {
            deliveryMode =
                    DeliveryMode.fromConfig(
                            message.delivery
                    );
        } catch (IllegalArgumentException exception) {
            throw new JsonParseException(
                    "Player message '" +
                            messageId +
                            "' has an invalid delivery mode."
            );
        }

        if (deliveryMode == DeliveryMode.BOSS_BAR) {
            throw new JsonParseException(
                    "Player message '" +
                            messageId +
                            "' cannot use boss_bar yet. " +
                            "Use chat, action_bar, title or subtitle."
            );
        }

        message.delivery =
                deliveryMode.getConfigName();

        try {
            message.audience =
                    PlayerMessageAudience
                            .fromConfig(message.audience)
                            .getConfigName();
        } catch (IllegalArgumentException exception) {
            throw new JsonParseException(
                    "Player message '" +
                            messageId +
                            "' has an invalid audience. " +
                            "Expected broadcast or player_only."
            );
        }

        if (message.titleTiming == null) {
            message.titleTiming =
                    new TitleTimingConfig();
        }

        validateTitleTiming(
                "player message " + messageId,
                message.titleTiming
        );
    }

    private static void validateScheduledCommands(
            List<ScheduledCommandConfig> tasks
    ) {
        Set<String> usedIds = new HashSet<>();

        for (int taskIndex = 0;
             taskIndex < tasks.size();
             taskIndex++) {

            ScheduledCommandConfig task =
                    tasks.get(taskIndex);

            if (task == null) {
                throw new JsonParseException(
                        "Scheduled command task at index " +
                                taskIndex +
                                " is null."
                );
            }

            if (task.id == null ||
                    task.id.isBlank()) {
                throw new JsonParseException(
                        "Scheduled command task at index " +
                                taskIndex +
                                " has no ID."
                );
            }

            if (!task.id.matches("[a-z0-9_-]+")) {
                throw new JsonParseException(
                        "Invalid scheduled command ID: " +
                                task.id +
                                ". Use lowercase letters, numbers, " +
                                "underscores and hyphens only."
                );
            }

            if (!usedIds.add(task.id)) {
                throw new JsonParseException(
                        "Duplicate scheduled command ID: " +
                                task.id
                );
            }

            if (task.commands == null ||
                    task.commands.isEmpty()) {
                throw new JsonParseException(
                        "Scheduled command task '" +
                                task.id +
                                "' must contain at least one command."
                );
            }

            for (int commandIndex = 0;
                 commandIndex < task.commands.size();
                 commandIndex++) {

                String command =
                        task.commands.get(commandIndex);

                if (command == null ||
                        command.isBlank()) {
                    throw new JsonParseException(
                            "Scheduled command task '" +
                                    task.id +
                                    "' contains an empty command " +
                                    "at position " +
                                    (commandIndex + 1) +
                                    "."
                    );
                }

                if (command.contains("\n") ||
                        command.contains("\r")) {
                    throw new JsonParseException(
                            "Scheduled command task '" +
                                    task.id +
                                    "' command " +
                                    (commandIndex + 1) +
                                    " must be on one line."
                    );
                }

                task.commands.set(
                        commandIndex,
                        command.trim()
                );
            }

            try {
                DurationParser.toTicks(task.interval);
            } catch (IllegalArgumentException exception) {
                throw new JsonParseException(
                        "Scheduled command task '" +
                                task.id +
                                "' has an invalid interval: " +
                                exception.getMessage()
                );
            }
        }
    }

    private static void validateBossBar(
            AnnouncementConfig announcement
    ) {
        if (announcement.bossBar == null) {
            announcement.bossBar =
                    new BossBarConfig();
        }

        try {
            BossBarValueParser.parseColor(
                    announcement.bossBar.color
            );
        } catch (IllegalArgumentException exception) {
            throw new JsonParseException(
                    "Announcement '" +
                            announcement.id +
                            "' has an invalid boss-bar colour: " +
                            announcement.bossBar.color +
                            ". " +
                            exception.getMessage()
            );
        }

        try {
            BossBarValueParser.parseStyle(
                    announcement.bossBar.style
            );
        } catch (IllegalArgumentException exception) {
            throw new JsonParseException(
                    "Announcement '" +
                            announcement.id +
                            "' has an invalid boss-bar style: " +
                            announcement.bossBar.style +
                            ". " +
                            exception.getMessage()
            );
        }

        try {
            DurationParser.toTicks(
                    announcement.bossBar.duration
            );
        } catch (IllegalArgumentException exception) {
            throw new JsonParseException(
                    "Announcement '" +
                            announcement.id +
                            "' has an invalid boss-bar duration: " +
                            exception.getMessage()
            );
        }
    }

    private static void validateDelivery(
            AnnouncementConfig announcement
    ) {
        DeliveryMode deliveryMode;

        try {
            deliveryMode =
                    DeliveryMode.fromConfig(
                            announcement.delivery
                    );
        } catch (IllegalArgumentException exception) {
            throw new JsonParseException(
                    "Announcement '" +
                            announcement.id +
                            "' has an invalid delivery mode: " +
                            announcement.delivery +
                            ". Expected chat, action_bar, title, " +
                            "subtitle or boss_bar."
            );
        }

        announcement.delivery =
                deliveryMode.getConfigName();

        if (announcement.titleTiming == null) {
            announcement.titleTiming =
                    new TitleTimingConfig();
        }

        validateTitleTiming(
                announcement.id,
                announcement.titleTiming
        );
    }

    private static void validateTitleTiming(
            String announcementId,
            TitleTimingConfig timing
    ) {
        if (timing.fadeInTicks < 0) {
            throw new JsonParseException(
                    "Announcement '" +
                            announcementId +
                            "' fadeInTicks cannot be negative."
            );
        }

        if (timing.stayTicks < 1) {
            throw new JsonParseException(
                    "Announcement '" +
                            announcementId +
                            "' stayTicks must be at least 1."
            );
        }

        if (timing.fadeOutTicks < 0) {
            throw new JsonParseException(
                    "Announcement '" +
                            announcementId +
                            "' fadeOutTicks cannot be negative."
            );
        }
    }
        private static void validateSectionStyle(
                String announcementId,
        int sectionIndex,
        AnnouncementSectionConfig section
) {
            if (section.style == null) {
                section.style = new AnnouncementStyleConfig();
            }

            String sectionLocation =
                    "Announcement '" +
                            announcementId +
                            "' section " +
                            (sectionIndex + 1);

            validateColor(
                    sectionLocation,
                    section.style
            );

            validateUrl(
                    sectionLocation,
                    section.style.clickUrl
            );
        }

        private static void validateColor(
                String sectionLocation,
                AnnouncementStyleConfig style
) {
            if (style.color == null ||
                    style.color.isBlank()) {
                style.color = "white";
                return;
            }

            String normalized =
                    style.color.trim()
                            .toLowerCase(Locale.ROOT);

            if (normalized.matches("#[0-9a-f]{6}")) {
                style.color = normalized;
                return;
            }

            Formatting formatting =
                    Formatting.byName(normalized);

            if (formatting == null ||
                    !formatting.isColor()) {
                throw new JsonParseException(
                        sectionLocation +
                                " has an invalid colour: " +
                                style.color
                );
            }

            style.color = normalized;
        }

        private static void validateUrl(
                String sectionLocation,
                String clickUrl
) {
            if (clickUrl == null ||
                    clickUrl.isBlank()) {
                return;
            }

            try {
                URI uri = new URI(clickUrl);
                String scheme = uri.getScheme();

                boolean validScheme =
                        "https".equalsIgnoreCase(scheme) ||
                                "http".equalsIgnoreCase(scheme);

                if (!validScheme ||
                        uri.getHost() == null) {
                    throw new JsonParseException(
                            sectionLocation +
                                    " has an invalid click URL. " +
                                    "Only complete HTTP or HTTPS URLs are allowed."
                    );
                }

            } catch (URISyntaxException exception) {
                throw new JsonParseException(
                        sectionLocation +
                                " has an invalid click URL.",
                        exception
                );
            }
        }

    public static boolean saveCurrent() {
        try {
            Files.createDirectories(
                    CONFIG_PATH.getParent()
            );

            save();

            ServerSignals.LOGGER.info(
                    "Saved Server Signals configuration to {}.",
                    CONFIG_PATH
            );

            return true;

        } catch (IOException exception) {
            ServerSignals.LOGGER.error(
                    "Could not save Server Signals configuration.",
                    exception
            );

            return false;
        }
    }
}