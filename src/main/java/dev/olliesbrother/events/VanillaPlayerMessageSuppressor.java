package dev.olliesbrother.events;

import dev.olliesbrother.config.ConfigManager;
import dev.olliesbrother.config.PlayerMessagesConfig;
import net.fabricmc.fabric.api.message.v1.ServerMessageEvents;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableTextContent;

public final class VanillaPlayerMessageSuppressor {
    private static final String JOIN_KEY =
            "multiplayer.player.joined";

    private static final String RENAMED_JOIN_KEY =
            "multiplayer.player.joined.renamed";

    private static final String LEAVE_KEY =
            "multiplayer.player.left";

    private static boolean registered;

    private VanillaPlayerMessageSuppressor() {
        // Utility class
    }

    public static void register() {
        if (registered) {
            return;
        }

        registered = true;

        ServerMessageEvents.ALLOW_GAME_MESSAGE.register(
                (server, message, overlay) ->
                        shouldAllowMessage(message, overlay)
        );
    }

    private static boolean shouldAllowMessage(
            Text message,
            boolean overlay
    ) {
        // Join and leave messages are not overlays,
        // so leave overlay messages untouched.
        if (overlay) {
            return true;
        }

        if (!(message.getContent()
                instanceof TranslatableTextContent content)) {

            return true;
        }

        PlayerMessagesConfig config =
                ConfigManager.getConfig().playerMessages;

        if (config == null) {
            return true;
        }

        return switch (content.getKey()) {
            case JOIN_KEY, RENAMED_JOIN_KEY ->
                    !config.suppressVanillaJoinMessage;

            case LEAVE_KEY ->
                    !config.suppressVanillaLeaveMessage;

            default -> true;
        };
    }
}