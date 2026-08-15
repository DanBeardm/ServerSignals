package dev.olliesbrother.delivery;

import net.minecraft.entity.boss.BossBar;

import java.util.Locale;

public final class BossBarValueParser {

    private BossBarValueParser() {
        // Utility class
    }

    public static BossBar.Color parseColor(String value) {
        String normalized = normalize(value);

        return switch (normalized) {
            case "pink" -> BossBar.Color.PINK;
            case "blue" -> BossBar.Color.BLUE;
            case "red" -> BossBar.Color.RED;
            case "green" -> BossBar.Color.GREEN;
            case "yellow" -> BossBar.Color.YELLOW;
            case "purple" -> BossBar.Color.PURPLE;
            case "white" -> BossBar.Color.WHITE;

            default -> throw new IllegalArgumentException(
                    "Expected pink, blue, red, green, " +
                            "yellow, purple or white."
            );
        };
    }

    public static BossBar.Style parseStyle(String value) {
        String normalized = normalize(value);

        return switch (normalized) {
            case "progress" -> BossBar.Style.PROGRESS;
            case "notched_6" -> BossBar.Style.NOTCHED_6;
            case "notched_10" -> BossBar.Style.NOTCHED_10;
            case "notched_12" -> BossBar.Style.NOTCHED_12;
            case "notched_20" -> BossBar.Style.NOTCHED_20;

            default -> throw new IllegalArgumentException(
                    "Expected progress, notched_6, " +
                            "notched_10, notched_12 or notched_20."
            );
        };
    }

    private static String normalize(String value) {
        if (value == null || value.isBlank()) {
            return "";
        }

        return value.trim().toLowerCase(Locale.ROOT);
    }
}