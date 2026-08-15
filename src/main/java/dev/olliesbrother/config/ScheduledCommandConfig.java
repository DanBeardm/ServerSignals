package dev.olliesbrother.config;

import java.util.ArrayList;
import java.util.List;

public final class ScheduledCommandConfig {
    public String id;

    public List<String> commands =
            new ArrayList<>();

    public String interval = "1h";

    public boolean enabled;

    public boolean onlyWhenPlayersOnline;

    public ScheduledCommandConfig() {
        // Required for Gson.
    }

    public ScheduledCommandConfig(
            String id,
            List<String> commands,
            String interval,
            boolean enabled,
            boolean onlyWhenPlayersOnline
    ) {
        this.id = id;
        this.commands = new ArrayList<>(commands);
        this.interval = interval;
        this.enabled = enabled;
        this.onlyWhenPlayersOnline =
                onlyWhenPlayersOnline;
    }
}