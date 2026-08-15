package dev.olliesbrother.config;

public final class TitleTimingConfig {
    public int fadeInTicks = 10;
    public int stayTicks = 70;
    public int fadeOutTicks = 20;

    public TitleTimingConfig() {
        // Required for Gson.
    }

    public TitleTimingConfig(
            int fadeInTicks,
            int stayTicks,
            int fadeOutTicks
    ) {
        this.fadeInTicks = fadeInTicks;
        this.stayTicks = stayTicks;
        this.fadeOutTicks = fadeOutTicks;
    }
}