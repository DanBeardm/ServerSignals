package dev.olliesbrother.data;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import dev.olliesbrother.ServerSignals;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public final class SeenPlayerStore {
    private static final Gson GSON =
            new GsonBuilder()
                    .setPrettyPrinting()
                    .create();

    private static final Path DATA_PATH =
            FabricLoader.getInstance()
                    .getConfigDir()
                    .resolve("ServerSignals")
                    .resolve("seen_players.json");

    private static Set<String> seenPlayers =
            new HashSet<>();

    private SeenPlayerStore() {
        // Utility class
    }

    public static void load() {
        try {
            Files.createDirectories(
                    DATA_PATH.getParent()
            );

            if (Files.notExists(DATA_PATH)) {
                save();

                ServerSignals.LOGGER.info(
                        "Created seen-player data at {}.",
                        DATA_PATH
                );

                return;
            }

            try (Reader reader = Files.newBufferedReader(
                    DATA_PATH,
                    StandardCharsets.UTF_8
            )) {
                SeenPlayerData data =
                        GSON.fromJson(
                                reader,
                                SeenPlayerData.class
                        );

                if (data == null ||
                        data.players == null) {

                    seenPlayers = new HashSet<>();
                } else {
                    seenPlayers =
                            new HashSet<>(data.players);
                }
            }

            ServerSignals.LOGGER.info(
                    "Loaded {} seen player(s).",
                    seenPlayers.size()
            );

        } catch (IOException exception) {
            ServerSignals.LOGGER.error(
                    "Could not load seen-player data.",
                    exception
            );

            seenPlayers = new HashSet<>();
        }
    }

    /**
     * @return true when this is the first time this mod
     * has recorded the player.
     */
    public static boolean markSeen(UUID playerUuid) {
        boolean firstJoin =
                seenPlayers.add(
                        playerUuid.toString()
                );

        if (!firstJoin) {
            return false;
        }

        try {
            save();
        } catch (IOException exception) {
            ServerSignals.LOGGER.error(
                    "Could not save seen-player data.",
                    exception
            );
        }

        return true;
    }

    private static void save() throws IOException {
        SeenPlayerData data =
                new SeenPlayerData();

        data.players =
                new HashSet<>(seenPlayers);

        try (Writer writer = Files.newBufferedWriter(
                DATA_PATH,
                StandardCharsets.UTF_8
        )) {
            GSON.toJson(data, writer);
        }
    }

    private static final class SeenPlayerData {
        private Set<String> players =
                new HashSet<>();
    }
}