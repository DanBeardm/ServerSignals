package dev.olliesbrother.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;
import dev.olliesbrother.ServerSignals;
import dev.olliesbrother.util.DurationParser;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.function.Supplier;

public final class ConfigManager {

    private static final Gson GSON =
            new GsonBuilder()
                    .setPrettyPrinting()
                    .disableHtmlEscaping()
                    .create();

    /*
     * config/ServerSignals/
     */
    private static final Path CONFIG_DIR =
            FabricLoader.getInstance()
                    .getConfigDir()
                    .resolve("ServerSignals");

    /*
     * Individual configuration files.
     */
    private static final Path ANNOUNCEMENTS_PATH =
            CONFIG_DIR.resolve(
                    "announcements.json"
            );

    private static final Path SCHEDULED_COMMANDS_PATH =
            CONFIG_DIR.resolve(
                    "scheduled_commands.json"
            );

    private static final Path PLAYER_MESSAGES_PATH =
            CONFIG_DIR.resolve(
                    "player_messages.json"
            );

    private static final Path RESTART_PATH =
            CONFIG_DIR.resolve(
                    "restart.json"
            );

    private static final Path MAINTENANCE_PATH =
            CONFIG_DIR.resolve(
                    "maintenance.json"
            );

    /*
     * The currently active configuration.
     *
     * The individual JSON files are loaded and combined into this
     * object so the rest of Server Signals can continue using:
     *
     * ConfigManager.getConfig()
     */
    private static ServerSignalsConfig config =
            new ServerSignalsConfig();

    private static String lastError =
            "";

    public static String getLastError() {
        return lastError;
    }

    private static final Set<String> VALID_DELIVERY_MODES =
            Set.of(
                    "chat",
                    "action_bar",
                    "title",
                    "subtitle",
                    "boss_bar"
            );

    private static final Set<String> VALID_TEXT_COLORS =
            Set.of(
                    "black",
                    "dark_blue",
                    "dark_green",
                    "dark_aqua",
                    "dark_red",
                    "dark_purple",
                    "gold",
                    "gray",
                    "dark_gray",
                    "blue",
                    "green",
                    "aqua",
                    "red",
                    "light_purple",
                    "yellow",
                    "white"
            );

    private ConfigManager() {
        // Utility class
    }

    /**
     * Returns the currently active configuration.
     */
    public static ServerSignalsConfig getConfig() {
        return config;
    }

    /**
     * Loads all Server Signals configuration files.
     *
     * The active config is only replaced after every file has
     * successfully loaded and passed validation.
     *
     * If something fails, the previous valid config remains active.
     */
    public static boolean load() {

        ServerSignalsConfig loadedConfig =
                readAndValidateConfig(
                        true
                );

        if (loadedConfig == null) {
            return false;
        }

        /*
         * Apply only after everything has passed.
         */
        config =
                loadedConfig;

        return true;
    }

    public static boolean validateOnly() {

        /*
         * false means:
         *
         * - don't create missing files
         * - don't write anything
         * - don't replace the active config
         */
        return readAndValidateConfig(
                false
        ) != null;
    }

    private static ServerSignalsConfig readAndValidateConfig(
            boolean createMissing
    ) {

        lastError =
                "";

        try {

            if (createMissing) {

                Files.createDirectories(
                        CONFIG_DIR
                );

            } else if (Files.notExists(
                    CONFIG_DIR
            )) {

                lastError =
                        "ServerSignals config directory does not exist.";

                logLoadFailure();

                return null;
            }


            /*
             * =====================================================
             * LOAD FILES
             * =====================================================
             */


            AnnouncementsFileConfig announcementsFile;

            try {

                announcementsFile =
                        loadConfigFile(
                                ANNOUNCEMENTS_PATH,
                                AnnouncementsFileConfig.class,
                                AnnouncementsFileConfig::new,
                                createMissing
                        );

            } catch (IOException | JsonParseException exception) {

                lastError =
                        formatFileError(
                                ANNOUNCEMENTS_PATH,
                                exception
                        );

                logLoadFailure();

                return null;
            }


            ScheduledCommandsFileConfig scheduledCommandsFile;

            try {

                scheduledCommandsFile =
                        loadConfigFile(
                                SCHEDULED_COMMANDS_PATH,
                                ScheduledCommandsFileConfig.class,
                                ScheduledCommandsFileConfig::new,
                                createMissing
                        );

            } catch (IOException | JsonParseException exception) {

                lastError =
                        formatFileError(
                                SCHEDULED_COMMANDS_PATH,
                                exception
                        );

                logLoadFailure();

                return null;
            }


            PlayerMessagesConfig playerMessages;

            try {

                playerMessages =
                        loadConfigFile(
                                PLAYER_MESSAGES_PATH,
                                PlayerMessagesConfig.class,
                                PlayerMessagesConfig::new,
                                createMissing
                        );

            } catch (IOException | JsonParseException exception) {

                lastError =
                        formatFileError(
                                PLAYER_MESSAGES_PATH,
                                exception
                        );

                logLoadFailure();

                return null;
            }


            RestartConfig restart;

            try {

                restart =
                        loadConfigFile(
                                RESTART_PATH,
                                RestartConfig.class,
                                RestartConfig::new,
                                createMissing
                        );

            } catch (IOException | JsonParseException exception) {

                lastError =
                        formatFileError(
                                RESTART_PATH,
                                exception
                        );

                logLoadFailure();

                return null;
            }


            MaintenanceConfig maintenance;

            try {

                maintenance =
                        loadConfigFile(
                                MAINTENANCE_PATH,
                                MaintenanceConfig.class,
                                MaintenanceConfig::new,
                                createMissing
                        );

            } catch (IOException | JsonParseException exception) {

                lastError =
                        formatFileError(
                                MAINTENANCE_PATH,
                                exception
                        );

                logLoadFailure();

                return null;
            }


            /*
             * =====================================================
             * ASSEMBLE CONFIG
             * =====================================================
             */

            ServerSignalsConfig loadedConfig =
                    new ServerSignalsConfig();

            loadedConfig.announcements =
                    announcementsFile.announcements;

            loadedConfig.scheduledCommands =
                    scheduledCommandsFile.scheduledCommands;

            loadedConfig.playerMessages =
                    playerMessages;

            loadedConfig.restart =
                    restart;

            loadedConfig.maintenance =
                    maintenance;


            /*
             * =====================================================
             * VALIDATE FILES
             * =====================================================
             */


            try {

                validateAnnouncements(
                        loadedConfig.announcements
                );

            } catch (IllegalArgumentException exception) {

                lastError =
                        "announcements.json\n" +
                                exception.getMessage();

                logLoadFailure();

                return null;
            }


            try {

                validateScheduledCommands(
                        loadedConfig.scheduledCommands
                );

            } catch (IllegalArgumentException exception) {

                lastError =
                        "scheduled_commands.json\n" +
                                exception.getMessage();

                logLoadFailure();

                return null;
            }


            try {

                validatePlayerMessages(
                        loadedConfig.playerMessages
                );

            } catch (IllegalArgumentException exception) {

                lastError =
                        "player_messages.json\n" +
                                exception.getMessage();

                logLoadFailure();

                return null;
            }


            try {

                validateRestart(
                        loadedConfig.restart
                );

            } catch (IllegalArgumentException exception) {

                lastError =
                        "restart.json\n" +
                                exception.getMessage();

                logLoadFailure();

                return null;
            }


            try {

                validateMaintenance(
                        loadedConfig.maintenance
                );

            } catch (IllegalArgumentException exception) {

                lastError =
                        "maintenance.json\n" +
                                exception.getMessage();

                logLoadFailure();

                return null;
            }


            /*
             * Everything is valid.
             *
             * IMPORTANT:
             * Nothing has been applied here.
             */
            return loadedConfig;

        } catch (IOException exception) {

            lastError =
                    "Could not access the Server Signals config directory.\n" +
                            cleanErrorMessage(
                                    exception
                            );

            logLoadFailure();

            return null;
        }
    }

    private static void validateMaintenance(
            MaintenanceConfig config
    ) {
        if (config == null) {
            throw new IllegalArgumentException(
                    "Maintenance configuration is missing."
            );
        }

        if (config.reason == null ||
                config.reason.isBlank()) {

            throw new IllegalArgumentException(
                    "reason cannot be empty."
            );
        }

        if (config.allowedPlayers == null) {
            throw new IllegalArgumentException(
                    "allowedPlayers is missing."
            );
        }

        for (String allowedPlayer :
                config.allowedPlayers) {

            if (allowedPlayer == null ||
                    allowedPlayer.isBlank()) {

                throw new IllegalArgumentException(
                        "allowedPlayers contains an empty value."
                );
            }
        }

        if (config.disconnectSections == null ||
                config.disconnectSections.isEmpty()) {

            throw new IllegalArgumentException(
                    "disconnectSections must contain at least one section."
            );
        }

        validateSections(
                config.disconnectSections,
                "Maintenance disconnect message"
        );

        validateMaintenanceSchedule(
                config.schedule
        );
    }

    private static void validateMaintenanceSchedule(
            MaintenanceScheduleConfig schedule
    ) {
        if (schedule == null) {
            throw new IllegalArgumentException(
                    "schedule configuration is missing."
            );
        }

        if (schedule.warningDelivery == null ||
                schedule.warningDelivery.isBlank()) {

            throw new IllegalArgumentException(
                    "schedule.warningDelivery cannot be empty."
            );
        }

        String delivery =
                schedule.warningDelivery
                        .trim()
                        .toLowerCase(
                                Locale.ROOT
                        );

        if (!Set.of(
                "chat",
                "action_bar",
                "title",
                "subtitle"
        ).contains(delivery)) {

            throw new IllegalArgumentException(
                    "Invalid schedule.warningDelivery '" +
                            schedule.warningDelivery +
                            "'."
            );
        }

        if (schedule.warningTimes == null) {
            throw new IllegalArgumentException(
                    "schedule.warningTimes is missing."
            );
        }

        Set<Long> warningSeconds =
                new HashSet<>();

        for (String warningTime :
                schedule.warningTimes) {

            if (warningTime == null ||
                    warningTime.isBlank()) {

                throw new IllegalArgumentException(
                        "schedule.warningTimes contains an empty value."
                );
            }

            long seconds;

            try {
                seconds =
                        DurationParser.toSeconds(
                                warningTime
                        );
            } catch (IllegalArgumentException exception) {

                throw new IllegalArgumentException(
                        "Invalid maintenance warning time '" +
                                warningTime +
                                "'."
                );
            }

            if (!warningSeconds.add(
                    seconds
            )) {

                throw new IllegalArgumentException(
                        "Duplicate maintenance warning time '" +
                                warningTime +
                                "'."
                );
            }
        }

        if (schedule.sections == null ||
                schedule.sections.isEmpty()) {

            throw new IllegalArgumentException(
                    "schedule.sections must contain at least one section."
            );
        }

        validateSections(
                schedule.sections,
                "Maintenance warning"
        );

        validateTitleTiming(
                schedule.titleTiming,
                "Maintenance schedule"
        );

        validateMaintenanceBossBar(
                schedule.bossBar
        );
    }

    private static void validateMaintenanceBossBar(
            MaintenanceBossBarConfig bossBar
    ) {
        if (bossBar == null) {
            throw new IllegalArgumentException(
                    "schedule.bossBar configuration is missing."
            );
        }

        if (bossBar.color == null ||
                bossBar.color.isBlank()) {

            throw new IllegalArgumentException(
                    "schedule.bossBar.color cannot be empty."
            );
        }

        String color =
                bossBar.color
                        .trim()
                        .toLowerCase(
                                Locale.ROOT
                        );

        if (!Set.of(
                "pink",
                "blue",
                "red",
                "green",
                "yellow",
                "purple",
                "white"
        ).contains(color)) {

            throw new IllegalArgumentException(
                    "Invalid schedule.bossBar color '" +
                            bossBar.color +
                            "'."
            );
        }

        if (bossBar.style == null ||
                bossBar.style.isBlank()) {

            throw new IllegalArgumentException(
                    "schedule.bossBar.style cannot be empty."
            );
        }

        String style =
                bossBar.style
                        .trim()
                        .toLowerCase(
                                Locale.ROOT
                        );

        if (!Set.of(
                "progress",
                "notched_6",
                "notched_10",
                "notched_12",
                "notched_20"
        ).contains(style)) {

            throw new IllegalArgumentException(
                    "Invalid schedule.bossBar style '" +
                            bossBar.style +
                            "'."
            );
        }
    }

    private static void validateRestart(
            RestartConfig config
    ) {
        if (config == null) {
            throw new IllegalArgumentException(
                    "Restart configuration is missing."
            );
        }

        if (config.finalCommand == null ||
                config.finalCommand.isBlank()) {

            throw new IllegalArgumentException(
                    "finalCommand cannot be empty."
            );
        }

        if (config.warningDelivery == null ||
                config.warningDelivery.isBlank()) {

            throw new IllegalArgumentException(
                    "warningDelivery cannot be empty."
            );
        }

        String delivery =
                config.warningDelivery
                        .trim()
                        .toLowerCase(
                                Locale.ROOT
                        );

        if (!Set.of(
                "chat",
                "action_bar",
                "title",
                "subtitle"
        ).contains(delivery)) {

            throw new IllegalArgumentException(
                    "Invalid warningDelivery '" +
                            config.warningDelivery +
                            "'."
            );
        }

        if (config.warningTimes == null) {
            throw new IllegalArgumentException(
                    "warningTimes is missing."
            );
        }

        Set<Long> warningSeconds =
                new HashSet<>();

        for (String warningTime :
                config.warningTimes) {

            if (warningTime == null ||
                    warningTime.isBlank()) {

                throw new IllegalArgumentException(
                        "warningTimes contains an empty value."
                );
            }

            long seconds;

            try {
                seconds =
                        DurationParser.toSeconds(
                                warningTime
                        );
            } catch (IllegalArgumentException exception) {

                throw new IllegalArgumentException(
                        "Invalid warning time '" +
                                warningTime +
                                "'."
                );
            }

            if (!warningSeconds.add(
                    seconds
            )) {
                throw new IllegalArgumentException(
                        "Duplicate warning time '" +
                                warningTime +
                                "'."
                );
            }
        }

        if (config.sections == null ||
                config.sections.isEmpty()) {

            throw new IllegalArgumentException(
                    "Restart message must contain at least one section."
            );
        }

        validateSections(
                config.sections,
                "Restart message"
        );

        validateTitleTiming(
                config.titleTiming,
                "Restart"
        );

        validateRestartBossBar(
                config.bossBar
        );
    }

    private static void validateRestartBossBar(
            RestartBossBarConfig bossBar
    ) {
        if (bossBar == null) {
            throw new IllegalArgumentException(
                    "bossBar configuration is missing."
            );
        }

        if (bossBar.color == null ||
                bossBar.color.isBlank()) {

            throw new IllegalArgumentException(
                    "bossBar color cannot be empty."
            );
        }

        String color =
                bossBar.color
                        .trim()
                        .toLowerCase(
                                Locale.ROOT
                        );

        if (!Set.of(
                "pink",
                "blue",
                "red",
                "green",
                "yellow",
                "purple",
                "white"
        ).contains(color)) {

            throw new IllegalArgumentException(
                    "Invalid bossBar color '" +
                            bossBar.color +
                            "'."
            );
        }

        if (bossBar.style == null ||
                bossBar.style.isBlank()) {

            throw new IllegalArgumentException(
                    "bossBar style cannot be empty."
            );
        }

        String style =
                bossBar.style
                        .trim()
                        .toLowerCase(
                                Locale.ROOT
                        );

        if (!Set.of(
                "progress",
                "notched_6",
                "notched_10",
                "notched_12",
                "notched_20"
        ).contains(style)) {

            throw new IllegalArgumentException(
                    "Invalid bossBar style '" +
                            bossBar.style +
                            "'."
            );
        }
    }

    private static void validatePlayerMessages(
            PlayerMessagesConfig config
    ) {
        if (config == null) {
            throw new IllegalArgumentException(
                    "Player messages configuration is missing."
            );
        }

        validatePlayerMessage(
                config.join,
                "join"
        );

        validatePlayerMessage(
                config.firstJoin,
                "firstJoin"
        );

        validatePlayerMessage(
                config.leave,
                "leave"
        );
    }

    private static void validatePlayerMessage(
            PlayerMessageConfig message,
            String name
    ) {
        if (message == null) {
            throw new IllegalArgumentException(
                    "Player message '" +
                            name +
                            "' is missing."
            );
        }

        if (message.delivery == null ||
                message.delivery.isBlank()) {

            throw new IllegalArgumentException(
                    "Player message '" +
                            name +
                            "' is missing a delivery mode."
            );
        }

        String delivery =
                message.delivery
                        .trim()
                        .toLowerCase(
                                Locale.ROOT
                        );

        if (!Set.of(
                "chat",
                "action_bar",
                "title",
                "subtitle"
        ).contains(delivery)) {

            throw new IllegalArgumentException(
                    "Player message '" +
                            name +
                            "' has invalid delivery mode '" +
                            message.delivery +
                            "'."
            );
        }

        if (message.audience == null ||
                message.audience.isBlank()) {

            throw new IllegalArgumentException(
                    "Player message '" +
                            name +
                            "' is missing an audience."
            );
        }

        String audience =
                message.audience
                        .trim()
                        .toLowerCase(
                                Locale.ROOT
                        );

        if (!Set.of(
                "broadcast",
                "player_only"
        ).contains(audience)) {

            throw new IllegalArgumentException(
                    "Player message '" +
                            name +
                            "' has invalid audience '" +
                            message.audience +
                            "'."
            );
        }

        if (message.sections == null ||
                message.sections.isEmpty()) {

            throw new IllegalArgumentException(
                    "Player message '" +
                            name +
                            "' must contain at least one section."
            );
        }

        validateSections(
                message.sections,
                "Player message '" +
                        name +
                        "'"
        );

        validateTitleTiming(
                message.titleTiming,
                "Player message '" +
                        name +
                        "'"
        );
    }

    private static void validateTitleTiming(
            TitleTimingConfig timing,
            String owner
    ) {
        if (timing == null) {
            throw new IllegalArgumentException(
                    owner +
                            " is missing titleTiming."
            );
        }

        if (timing.fadeInTicks < 0) {
            throw new IllegalArgumentException(
                    owner +
                            " fadeInTicks cannot be negative."
            );
        }

        if (timing.stayTicks < 0) {
            throw new IllegalArgumentException(
                    owner +
                            " stayTicks cannot be negative."
            );
        }

        if (timing.fadeOutTicks < 0) {
            throw new IllegalArgumentException(
                    owner +
                            " fadeOutTicks cannot be negative."
            );
        }
    }

    private static String formatFileError(
            Path path,
            Exception exception
    ) {
        return path.getFileName() +
                "\n" +
                cleanErrorMessage(
                        exception
                );
    }

    private static String cleanErrorMessage(
            Exception exception
    ) {
        String message =
                exception.getMessage();

        if (message == null ||
                message.isBlank()) {

            return exception
                    .getClass()
                    .getSimpleName();
        }

        return message;
    }

    private static void logLoadFailure() {

        ServerSignals.LOGGER.error(
                "Server Signals configuration check failed: {}",
                lastError.replace(
                        "\n",
                        " - "
                )
        );

        ServerSignals.LOGGER.warn(
                "The active Server Signals configuration was not changed."
        );
    }

    /**
     * Loads a configuration file.
     *
     * If the file does not exist, the supplied default object is
     * written to disk first.
     */
    private static <T> T loadOrCreate(
            Path path,
            Class<T> type,
            Supplier<T> defaultSupplier
    ) throws IOException {

        if (Files.notExists(path)) {

            T defaultConfig =
                    defaultSupplier.get();

            saveFile(
                    path,
                    defaultConfig
            );

            ServerSignals.LOGGER.info(
                    "Created default config file: {}",
                    path.getFileName()
            );
        }

        return loadFile(
                path,
                type
        );
    }

    private static <T> T loadConfigFile(
            Path path,
            Class<T> type,
            Supplier<T> defaultSupplier,
            boolean createMissing
    ) throws IOException {

        if (createMissing) {
            return loadOrCreate(
                    path,
                    type,
                    defaultSupplier
            );
        }

        /*
         * Validation should not create or modify files.
         */
        if (Files.notExists(path)) {
            throw new IOException(
                    "File does not exist."
            );
        }

        return loadFile(
                path,
                type
        );
    }

    /**
     * Loads one JSON file into the requested config class.
     */
    private static <T> T loadFile(
            Path path,
            Class<T> type
    ) throws IOException {

        try (Reader reader =
                     Files.newBufferedReader(
                             path,
                             StandardCharsets.UTF_8
                     )) {

            T loaded =
                    GSON.fromJson(
                            reader,
                            type
                    );

            if (loaded == null) {

                throw new JsonParseException(
                        path.getFileName() +
                                " is empty."
                );
            }

            return loaded;
        }
    }

    /**
     * Writes one configuration object to disk.
     */
    private static void saveFile(
            Path path,
            Object value
    ) throws IOException {

        Files.createDirectories(
                CONFIG_DIR
        );

        try (Writer writer =
                     Files.newBufferedWriter(
                             path,
                             StandardCharsets.UTF_8
                     )) {

            GSON.toJson(
                    value,
                    writer
            );
        }
    }

    /**
     * Saves only maintenance.json.
     *
     * Maintenance mode changes at runtime, so we do not want
     * enabling/disabling maintenance to rewrite every config file.
     */
    public static boolean saveMaintenance() {

        try {

            saveFile(
                    MAINTENANCE_PATH,
                    config.maintenance
            );

            ServerSignals.LOGGER.info(
                    "Saved maintenance configuration."
            );

            return true;

        } catch (IOException exception) {

            ServerSignals.LOGGER.error(
                    "Could not save maintenance configuration.",
                    exception
            );

            return false;
        }
    }

    /**
     * Temporary backwards compatibility.
     *
     * If MaintenanceManager still calls saveCurrent(), it will
     * continue working until we replace those calls with
     * saveMaintenance().
     */
    @Deprecated
    public static boolean saveCurrent() {
        return saveMaintenance();
    }

    /*
     * =========================================================
     * VALIDATION
     * =========================================================
     */

    private static void validate(
            ServerSignalsConfig config
    ) {

        if (config.announcements == null) {
            throw new IllegalArgumentException(
                    "announcements.json does not contain an announcements list."
            );
        }

        if (config.scheduledCommands == null) {
            throw new IllegalArgumentException(
                    "scheduled_commands.json does not contain a scheduledCommands list."
            );
        }

        if (config.playerMessages == null) {
            throw new IllegalArgumentException(
                    "player_messages.json could not be loaded."
            );
        }

        if (config.restart == null) {
            throw new IllegalArgumentException(
                    "restart.json could not be loaded."
            );
        }

        if (config.maintenance == null) {
            throw new IllegalArgumentException(
                    "maintenance.json could not be loaded."
            );
        }

        validateAnnouncements(
                config.announcements
        );

        validateScheduledCommands(
                config.scheduledCommands
        );
    }

    /*
     * =========================================================
     * ANNOUNCEMENTS
     * =========================================================
     */

    private static void validateAnnouncements(
            List<AnnouncementConfig> announcements
    ) {

        Set<String> ids =
                new HashSet<>();

        for (AnnouncementConfig announcement :
                announcements) {

            if (announcement == null) {
                throw new IllegalArgumentException(
                        "announcements.json contains a null announcement."
                );
            }

            if (announcement.id == null ||
                    announcement.id.isBlank()) {

                throw new IllegalArgumentException(
                        "An announcement is missing an id."
                );
            }

            String id =
                    announcement.id.trim();

            if (!ids.add(id)) {

                throw new IllegalArgumentException(
                        "Duplicate announcement id: " +
                                id
                );
            }

            if (announcement.interval == null ||
                    announcement.interval.isBlank()) {

                throw new IllegalArgumentException(
                        "Announcement '" +
                                id +
                                "' is missing an interval."
                );
            }

            validateDuration(
                    announcement.interval,
                    "Announcement '" +
                            id +
                            "'"
            );

            if (announcement.delivery == null ||
                    announcement.delivery.isBlank()) {

                throw new IllegalArgumentException(
                        "Announcement '" +
                                id +
                                "' is missing a delivery mode."
                );
            }

            validateDeliveryMode(
                    announcement.delivery,
                    "Announcement '" +
                            id +
                            "'"
            );

            if (announcement.sections == null ||
                    announcement.sections.isEmpty()) {

                throw new IllegalArgumentException(
                        "Announcement '" +
                                id +
                                "' must contain at least one section."
                );
            }

            validateSections(
                    announcement.sections,
                    "Announcement '" +
                            id +
                            "'"
            );
        }
    }

    /*
     * =========================================================
     * SCHEDULED COMMANDS
     * =========================================================
     */

    private static void validateScheduledCommands(
            List<ScheduledCommandConfig> scheduledCommands
    ) {

        Set<String> ids =
                new HashSet<>();

        for (ScheduledCommandConfig scheduledCommand :
                scheduledCommands) {

            if (scheduledCommand == null) {

                throw new IllegalArgumentException(
                        "scheduled_commands.json contains a null task."
                );
            }

            if (scheduledCommand.id == null ||
                    scheduledCommand.id.isBlank()) {

                throw new IllegalArgumentException(
                        "A scheduled command task is missing an id."
                );
            }

            String id =
                    scheduledCommand.id.trim();

            if (!ids.add(id)) {

                throw new IllegalArgumentException(
                        "Duplicate scheduled command id: " +
                                id
                );
            }

            if (scheduledCommand.interval == null ||
                    scheduledCommand.interval.isBlank()) {

                throw new IllegalArgumentException(
                        "Scheduled command '" +
                                id +
                                "' is missing an interval."
                );
            }

            validateDuration(
                    scheduledCommand.interval,
                    "Scheduled command '" +
                            id +
                            "'"
            );

            if (scheduledCommand.commands == null ||
                    scheduledCommand.commands.isEmpty()) {

                throw new IllegalArgumentException(
                        "Scheduled command '" +
                                id +
                                "' must contain at least one command."
                );
            }

            for (String command :
                    scheduledCommand.commands) {

                if (command == null ||
                        command.isBlank()) {

                    throw new IllegalArgumentException(
                            "Scheduled command '" +
                                    id +
                                    "' contains an empty command."
                    );
                }
            }
        }
    }

    /*
     * =========================================================
     * MESSAGE SECTIONS
     * =========================================================
     */

    private static void validateSections(
            List<AnnouncementSectionConfig> sections,
            String owner
    ) {

        for (int index = 0;
             index < sections.size();
             index++) {

            AnnouncementSectionConfig section =
                    sections.get(index);

            if (section == null) {

                throw new IllegalArgumentException(
                        owner +
                                " contains a null section at index " +
                                index +
                                "."
                );
            }

            if (section.text == null) {

                throw new IllegalArgumentException(
                        owner +
                                " section " +
                                index +
                                " has no text value."
                );
            }

            if (section.style == null) {

                throw new IllegalArgumentException(
                        owner +
                                " section " +
                                index +
                                " has no style object."
                );
            }

            validateSectionStyle(
                    section.style,
                    owner +
                            " section " +
                            index
            );
        }
    }

    private static void validateSectionStyle(
            AnnouncementStyleConfig style,
            String owner
    ) {

        if (style.color == null ||
                style.color.isBlank()) {

            return;
        }

        String color =
                style.color
                        .trim()
                        .toLowerCase(
                                Locale.ROOT
                        );

        /*
         * Named Minecraft colour.
         */
        if (VALID_TEXT_COLORS.contains(
                color
        )) {
            return;
        }

        /*
         * Hex colour, for example:
         *
         * #FFAA00
         */
        if (color.matches(
                "^#[0-9a-f]{6}$"
        )) {
            return;
        }

        throw new IllegalArgumentException(
                owner +
                        " has invalid colour '" +
                        style.color +
                        "'."
        );
    }

    /*
     * =========================================================
     * GENERAL VALIDATION
     * =========================================================
     */

    private static void validateDeliveryMode(
            String delivery,
            String owner
    ) {

        String normalized =
                delivery
                        .trim()
                        .toLowerCase(
                                Locale.ROOT
                        );

        if (!VALID_DELIVERY_MODES.contains(
                normalized
        )) {

            throw new IllegalArgumentException(
                    owner +
                            " has invalid delivery mode '" +
                            delivery +
                            "'."
            );
        }
    }

    private static void validateDuration(
            String duration,
            String owner
    ) {

        try {

            DurationParser.toSeconds(
                    duration
            );

        } catch (IllegalArgumentException exception) {

            throw new IllegalArgumentException(
                    owner +
                            " has invalid duration '" +
                            duration +
                            "'. " +
                            exception.getMessage()
            );
        }
    }

    public static void logStartupSummary() {

        long enabledAnnouncements =
                config.announcements
                        .stream()
                        .filter(announcement ->
                                announcement.enabled
                        )
                        .count();

        long enabledScheduledCommands =
                config.scheduledCommands
                        .stream()
                        .filter(task ->
                                task.enabled
                        )
                        .count();

        int enabledPlayerMessages =
                0;

        if (config.playerMessages.join != null &&
                config.playerMessages.join.enabled) {
            enabledPlayerMessages++;
        }

        if (config.playerMessages.firstJoin != null &&
                config.playerMessages.firstJoin.enabled) {
            enabledPlayerMessages++;
        }

        if (config.playerMessages.leave != null &&
                config.playerMessages.leave.enabled) {
            enabledPlayerMessages++;
        }


        ServerSignals.LOGGER.info(
                "Configuration:"
        );

        ServerSignals.LOGGER.info(
                "  announcements.json       ✓ {} configured / {} enabled",
                config.announcements.size(),
                enabledAnnouncements
        );

        ServerSignals.LOGGER.info(
                "  scheduled_commands.json  ✓ {} configured / {} enabled",
                config.scheduledCommands.size(),
                enabledScheduledCommands
        );

        ServerSignals.LOGGER.info(
                "  player_messages.json     ✓ {}/3 enabled",
                enabledPlayerMessages
        );

        ServerSignals.LOGGER.info(
                "  restart.json             ✓ {}",
                config.restart.enabled
                        ? "enabled"
                        : "disabled"
        );

        ServerSignals.LOGGER.info(
                "  maintenance.json         ✓ {}",
                config.maintenance.enabled
                        ? "enabled"
                        : "disabled"
        );
    }
}