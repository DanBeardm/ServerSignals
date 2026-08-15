package dev.olliesbrother.util;

import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class DurationParser {
    private static final Pattern DURATION_PATTERN =
            Pattern.compile(
                    "^([1-9]\\d*)([smhd])$",
                    Pattern.CASE_INSENSITIVE
            );

    private DurationParser() {
        // Utility class
    }

    public static long toSeconds(String duration) {
        if (duration == null || duration.isBlank()) {
            throw new IllegalArgumentException(
                    "Duration cannot be empty."
            );
        }

        String normalized =
                duration.trim().toLowerCase(Locale.ROOT);

        Matcher matcher =
                DURATION_PATTERN.matcher(normalized);

        if (!matcher.matches()) {
            throw new IllegalArgumentException(
                    "Expected a duration such as 10s, 15m, 1h or 2d."
            );
        }

        long amount;

        try {
            amount = Long.parseLong(matcher.group(1));
        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException(
                    "Duration number is too large.",
                    exception
            );
        }

        char unit = matcher.group(2).charAt(0);

        try {
            return switch (unit) {
                case 's' -> amount;
                case 'm' -> Math.multiplyExact(amount, 60L);
                case 'h' -> Math.multiplyExact(amount, 3_600L);
                case 'd' -> Math.multiplyExact(amount, 86_400L);

                default -> throw new IllegalArgumentException(
                        "Unsupported duration unit: " + unit
                );
            };
        } catch (ArithmeticException exception) {
            throw new IllegalArgumentException(
                    "Duration is too large.",
                    exception
            );
        }
    }

    public static long toTicks(String duration) {
        try {
            return Math.multiplyExact(
                    toSeconds(duration),
                    20L
            );
        } catch (ArithmeticException exception) {
            throw new IllegalArgumentException(
                    "Duration is too large.",
                    exception
            );
        }
    }
}