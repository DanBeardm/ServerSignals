package dev.olliesbrother.config;

import java.util.ArrayList;
import java.util.List;

public final class AnnouncementConfig {
    public String id;

    public List<AnnouncementSectionConfig> sections =
            new ArrayList<>();

    public String interval;
    public boolean enabled;
    public boolean onlyWhenPlayersOnline;

    public String delivery = "chat";

    public TitleTimingConfig titleTiming =
            new TitleTimingConfig();

    public BossBarConfig bossBar =
            new BossBarConfig();

    public AnnouncementConfig() {
        // Required for Gson.
    }

    public AnnouncementConfig(
            String id,
            List<AnnouncementSectionConfig> sections,
            String interval,
            boolean enabled,
            boolean onlyWhenPlayersOnline
    ) {
        this.id = id;
        this.sections = new ArrayList<>(sections);
        this.interval = interval;
        this.enabled = enabled;
        this.onlyWhenPlayersOnline = onlyWhenPlayersOnline;
    }

}