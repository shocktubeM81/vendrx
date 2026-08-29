package ca.vendrx.database;
import ca.vendrx.config.AppPaths;
import ca.vendrx.model.Transmission;

import java.io.IOException;
import java.nio.file.Files;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.nio.file.Path;
import java.sql.ResultSet;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class TransmissionRepository {

    private final String databaseUrl;

    public TransmissionRepository() {

        databaseUrl =
                "jdbc:sqlite:"
                + AppPaths.getDatabasePath();
    }

    public void initialize() {

        try {

            Files.createDirectories(
                    AppPaths.getDataDirectory()
            );

        } catch (IOException e) {

            throw new RuntimeException(
                    "Unable to create VendRx data directory.",
                    e
            );
        }

        String sql = """
                CREATE TABLE IF NOT EXISTS transmission (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    start_time TEXT NOT NULL,
                    end_time TEXT NOT NULL,
                    duration_ms INTEGER NOT NULL,
                    file_path TEXT NOT NULL,
                    average_rms REAL,
                    max_rms REAL
                );
                """;

        try (
                Connection connection =
                        DriverManager.getConnection(databaseUrl);

                Statement statement =
                        connection.createStatement()
        ) {

            statement.execute(sql);

            System.out.println(
                    "Database: "
                    + AppPaths.getDatabasePath()
            );

        } catch (SQLException e) {

            throw new RuntimeException(
                    "Unable to initialize database.",
                    e
            );
        }
    }

    public Transmission save(
                Transmission transmission
        ) {

        String sql = """
                INSERT INTO transmission (
                        start_time,
                        end_time,
                        duration_ms,
                        file_path,
                        average_rms,
                        max_rms
                )
                VALUES (?, ?, ?, ?, ?, ?);
                """;

        try (
                Connection connection =
                        DriverManager.getConnection(
                                databaseUrl
                        );

                PreparedStatement statement =
                        connection.prepareStatement(
                                sql,
                                Statement.RETURN_GENERATED_KEYS
                        )
        ) {

                statement.setString(
                        1,
                        transmission
                                .getStartTime()
                                .toString()
                );

                statement.setString(
                        2,
                        transmission
                                .getEndTime()
                                .toString()
                );

                statement.setLong(
                        3,
                        transmission
                                .getDuration()
                                .toMillis()
                );

                statement.setString(
                        4,
                        transmission
                                .getFilePath()
                                .toString()
                );

                statement.setDouble(
                        5,
                        transmission.getAverageRms()
                );

                statement.setDouble(
                        6,
                        transmission.getMaxRms()
                );

                statement.executeUpdate();

                try (
                        ResultSet generatedKeys =
                                statement.getGeneratedKeys()
                ) {

                if (!generatedKeys.next()) {

                        throw new SQLException(
                                "No ID generated for transmission."
                        );
                }

                long id =
                        generatedKeys.getLong(1);

                System.out.println(
                        "Transmission saved with ID "
                        + id
                );

                return new Transmission(
                        id,
                        transmission.getStartTime(),
                        transmission.getEndTime(),
                        transmission.getFilePath(),
                        transmission.getAverageRms(),
                        transmission.getMaxRms()
                );
                }

        } catch (SQLException e) {

                throw new RuntimeException(
                        "Unable to save transmission.",
                        e
                );
        }
        }

    public List<Transmission> findRecent(int limit) {

        if (limit <= 0) {
            throw new IllegalArgumentException(
                    "limit must be greater than 0"
            );
        }

        String sql = """
                SELECT
                    id,
                    start_time,
                    end_time,
                    file_path,
                    average_rms,
                    max_rms
                FROM transmission
                ORDER BY id DESC
                LIMIT ?;
                """;

        List<Transmission> transmissions =
                new ArrayList<>();

        try (
                Connection connection =
                        DriverManager.getConnection(databaseUrl);

                PreparedStatement statement =
                        connection.prepareStatement(sql)
        ) {

            statement.setInt(1, limit);

            try (ResultSet resultSet =
                        statement.executeQuery()) {

                while (resultSet.next()) {

                    Transmission transmission =
                        new Transmission(
                                resultSet.getLong(
                                        "id"
                                ),
                                LocalDateTime.parse(
                                        resultSet.getString(
                                                "start_time"
                                        )
                                ),
                                LocalDateTime.parse(
                                        resultSet.getString(
                                                "end_time"
                                        )
                                ),
                                Path.of(
                                        resultSet.getString(
                                                "file_path"
                                        )
                                ),
                                resultSet.getDouble(
                                        "average_rms"
                                ),
                                resultSet.getDouble(
                                        "max_rms"
                                )
                        );

                    transmissions.add(
                            transmission
                    );
                }
            }

            return transmissions;

        } catch (SQLException e) {

            throw new RuntimeException(
                    "Unable to read transmissions.",
                    e
            );
        }
    }
    
        public void delete(
                Transmission transmission
        ) {

        if (transmission.getId() == null) {

                throw new IllegalArgumentException(
                        "Cannot delete a transmission without an ID."
                );
        }

        String sql = """
                DELETE FROM transmission
                WHERE id = ?;
                """;

        try (
                Connection connection =
                        DriverManager.getConnection(
                                databaseUrl
                        );

                PreparedStatement statement =
                        connection.prepareStatement(
                                sql
                        )
        ) {

                statement.setLong(
                        1,
                        transmission.getId()
                );

                int deletedRows =
                        statement.executeUpdate();

                if (deletedRows == 0) {

                throw new IllegalStateException(
                        "Transmission not found in database: "
                        + transmission.getId()
                );
                }

        } catch (SQLException e) {

                throw new RuntimeException(
                        "Unable to delete transmission from database.",
                        e
                );
        }
        }
}