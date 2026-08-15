package dev.olliesbrother.util;

public final class DurationFormatter {

    private DurationFormatter() {
        // Utility class
    }

    public static String formatSeconds(long totalSeconds) {
        long safeSeconds = Math.max(0L, totalSeconds);

        long days = safeSeconds / 86_400L;
        long hours =
                (safeSeconds % 86_400L) / 3_600L;
        long minutes =
                (safeSeconds % 3_600L) / 60L;
        long seconds =
                safeSeconds % 60L;

        if (days > 0) {
            return days + "d " +
                    hours + "h " +
                    minutes + "m " +
                    seconds + "s";
        }

        if (hours > 0) {
            return hours + "h " +
                    minutes + "m " +
                    seconds + "s";
        }

        if (minutes > 0) {
            return minutes + "m " +
                    seconds + "s";
        }

        return seconds + "s";
    }
}