package dev.olliesbrother.text;

import com.mojang.authlib.GameProfile;
import dev.olliesbrother.config.MaintenanceConfig;
import net.minecraft.text.Text;

public final class MaintenanceMessageFactory {

    private MaintenanceMessageFactory() {
        // Utility class
    }

    public static Text create(
            MaintenanceConfig config,
            GameProfile profile
    ) {
        return AnnouncementTextFactory.create(
                config.disconnectSections,
                input ->
                        MaintenancePlaceholderResolver.resolve(
                                input,
                                config,
                                profile
                        )
        );
    }
}