package dev.olliesbrother.config;

public final class AnnouncementStyleConfig {
    public String color = "white";
    public boolean bold;
    public boolean italic;
    public boolean underlined;
    public String clickUrl = "";
    public String hoverText = "";

    public AnnouncementStyleConfig() {
        // Required for Gson.
    }

    public AnnouncementStyleConfig(
            String color,
            boolean bold,
            boolean italic,
            boolean underlined,
            String clickUrl,
            String hoverText
    ) {
        this.color = color;
        this.bold = bold;
        this.italic = italic;
        this.underlined = underlined;
        this.clickUrl = clickUrl;
        this.hoverText = hoverText;
    }
}