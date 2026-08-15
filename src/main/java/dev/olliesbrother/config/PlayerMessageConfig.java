package dev.olliesbrother.config;

import java.util.ArrayList;
import java.util.List;

public final class PlayerMessageConfig {
    public boolean enabled;

    public String delivery = "chat";

    public String audience = "broadcast";

    public List<AnnouncementSectionConfig> sections =
            new ArrayList<>();

    public TitleTimingConfig titleTiming =
            new TitleTimingConfig();

    public BossBarConfig bossBar =
            new BossBarConfig();

    public PlayerMessageConfig() {
        // Required for Gson.
    }

    public PlayerMessageConfig(
            boolean enabled,
            String delivery,
            String audience,
            List<AnnouncementSectionConfig> sections
    ) {
        this.enabled = enabled;
        this.delivery = delivery;
        this.audience = audience;
        this.sections = new ArrayList<>(sections);
    }
}