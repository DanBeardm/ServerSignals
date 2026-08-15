package dev.olliesbrother.text;

import dev.olliesbrother.config.AnnouncementConfig;
import dev.olliesbrother.config.AnnouncementSectionConfig;
import dev.olliesbrother.config.AnnouncementStyleConfig;
import net.minecraft.server.MinecraftServer;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.List;
import java.util.Locale;
import java.util.function.UnaryOperator;

public final class AnnouncementTextFactory {

    private AnnouncementTextFactory() {
        // Utility class
    }

    public static Text create(
            MinecraftServer server,
            AnnouncementConfig announcement
    ) {
        return create(
                announcement.sections,
                input -> PlaceholderResolver.resolve(
                        input,
                        server,
                        announcement
                )
        );
    }

    public static Text create(
            List<AnnouncementSectionConfig> sections,
            UnaryOperator<String> placeholderResolver
    ) {
        MutableText completeMessage =
                Text.literal("");

        for (AnnouncementSectionConfig section : sections) {
            String resolvedText =
                    placeholderResolver.apply(section.text);

            MutableText sectionText =
                    Text.literal(resolvedText);

            Style style = createStyle(
                    section.style,
                    placeholderResolver
            );

            sectionText.setStyle(style);
            completeMessage.append(sectionText);
        }

        return completeMessage;
    }

    private static Style createStyle(
            AnnouncementStyleConfig configuredStyle,
            UnaryOperator<String> placeholderResolver
    ) {
        if (configuredStyle == null) {
            return Style.EMPTY;
        }

        Style style = Style.EMPTY;

        style = applyColor(
                style,
                configuredStyle.color
        );

        style = style
                .withBold(configuredStyle.bold)
                .withItalic(configuredStyle.italic)
                .withUnderline(configuredStyle.underlined);

        if (configuredStyle.clickUrl != null &&
                !configuredStyle.clickUrl.isBlank()) {

            style = style.withClickEvent(
                    new ClickEvent(
                            ClickEvent.Action.OPEN_URL,
                            configuredStyle.clickUrl
                    )
            );
        }

        if (configuredStyle.hoverText != null &&
                !configuredStyle.hoverText.isBlank()) {

            String hoverText =
                    placeholderResolver.apply(
                            configuredStyle.hoverText
                    );

            style = style.withHoverEvent(
                    new HoverEvent(
                            HoverEvent.Action.SHOW_TEXT,
                            Text.literal(hoverText)
                    )
            );
        }

        return style;
    }

    private static Style applyColor(
            Style style,
            String configuredColor
    ) {
        if (configuredColor == null ||
                configuredColor.isBlank()) {

            return style.withColor(Formatting.WHITE);
        }

        String normalized =
                configuredColor.trim()
                        .toLowerCase(Locale.ROOT);

        if (normalized.matches("#[0-9a-f]{6}")) {
            int rgb = Integer.parseInt(
                    normalized.substring(1),
                    16
            );

            return style.withColor(rgb);
        }

        Formatting formatting =
                Formatting.byName(normalized);

        if (formatting == null ||
                !formatting.isColor()) {

            return style.withColor(Formatting.WHITE);
        }

        return style.withColor(formatting);
    }
}