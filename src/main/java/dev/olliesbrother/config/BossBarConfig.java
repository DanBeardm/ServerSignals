package dev.olliesbrother.config;

public final class BossBarConfig {
    public String color = "purple";
    public String style = "progress";
    public String duration = "10s";
    public boolean countdown = true;

    public BossBarConfig() {
        // Required for Gson.
    }

    public BossBarConfig(
            String color,
            String style,
            String duration,
            boolean countdown
    ) {
        this.color = color;
        this.style = style;
        this.duration = duration;
        this.countdown = countdown;
    }
}