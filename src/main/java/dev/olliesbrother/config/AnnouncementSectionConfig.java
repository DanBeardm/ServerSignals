package dev.olliesbrother.config;

public final class AnnouncementSectionConfig {
    public String text;
    public AnnouncementStyleConfig style;

    public AnnouncementSectionConfig() {
        // Required for Gson.
    }

    public AnnouncementSectionConfig(
            String text,
            AnnouncementStyleConfig style
    ) {
        this.text = text;
        this.style = style;
    }
}