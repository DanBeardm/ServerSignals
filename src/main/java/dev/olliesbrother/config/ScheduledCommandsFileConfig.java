package dev.olliesbrother.config;

import java.util.ArrayList;
import java.util.List;

public final class ScheduledCommandsFileConfig {

    public List<String> _comments =
            new ArrayList<>(List.of(
                    "Commands automatically executed by the server at a configured interval.",
                    "Intervals support s, m, h and d, for example: 30s, 15m, 1h or 2d.",
                    "Scheduled commands run with server-level permissions.",
                    "Example tasks are disabled by default. Set enabled to true when ready."
            ));

    public List<ScheduledCommandConfig> scheduledCommands =
            createDefaults();

    private static List<ScheduledCommandConfig> createDefaults() {

        List<ScheduledCommandConfig> defaults =
                new ArrayList<>();

        ScheduledCommandConfig automaticSave =
                new ScheduledCommandConfig();

        automaticSave.id =
                "automatic_save";

        automaticSave.commands =
                new ArrayList<>(
                        List.of(
                                "save-all flush"
                        )
                );

        automaticSave.interval =
                "30m";

        automaticSave.enabled =
                false;

        automaticSave.onlyWhenPlayersOnline =
                false;

        defaults.add(
                automaticSave
        );

        return defaults;
    }
}