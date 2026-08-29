package ca.vendrx.config;

import java.nio.file.Path;

public final class AppPaths {

    private static final Path DATA_DIRECTORY =
            Path.of(
                    System.getProperty("user.home"),
                    "VendRxData"
            );

    private AppPaths() {
        // Prevent instantiation
    }

    public static Path getDataDirectory() {
        return DATA_DIRECTORY;
    }

    public static Path getRecordingsDirectory() {
        return DATA_DIRECTORY.resolve("recordings");
    }

    public static Path getDatabasePath() {
        return DATA_DIRECTORY.resolve("vendrx.db");
    }
}