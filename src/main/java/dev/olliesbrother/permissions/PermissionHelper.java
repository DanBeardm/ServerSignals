package dev.olliesbrother.permissions;

import me.lucko.fabric.api.permissions.v0.Permissions;
import net.minecraft.server.command.ServerCommandSource;

import java.util.function.Predicate;

public final class PermissionHelper {

    private PermissionHelper() {
        // Utility class
    }

    public static boolean check(
            ServerCommandSource source,
            String permission,
            int fallbackLevel
    ) {
        return Permissions.check(
                source,
                permission,
                fallbackLevel
        );
    }

    public static Predicate<ServerCommandSource> require(
            String permission,
            int fallbackLevel
    ) {
        return source ->
                check(
                        source,
                        permission,
                        fallbackLevel
                );
    }
}