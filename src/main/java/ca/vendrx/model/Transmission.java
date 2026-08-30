package ca.vendrx.model;

import java.nio.file.Path;
import java.time.Duration;
import java.time.LocalDateTime;

public class Transmission {

    private final Long id;

    private final LocalDateTime startTime;
    private final LocalDateTime endTime;

    private final Path filePath;

    private final double averageRms;
    private final double maxRms;

    /*
     * Constructor for a new transmission
     * that has not yet been saved to the database.
     */
    public Transmission(
            LocalDateTime startTime,
            LocalDateTime endTime,
            Path filePath,
            double averageRms,
            double maxRms) {

        this(
                null,
                startTime,
                endTime,
                filePath,
                averageRms,
                maxRms);
    }

    /*
     * Constructor for a transmission
     * loaded from the database.
     */
    public Transmission(
            long id,
            LocalDateTime startTime,
            LocalDateTime endTime,
            Path filePath,
            double averageRms,
            double maxRms) {

        this(
                Long.valueOf(id),
                startTime,
                endTime,
                filePath,
                averageRms,
                maxRms);
    }

    private Transmission(
            Long id,
            LocalDateTime startTime,
            LocalDateTime endTime,
            Path filePath,
            double averageRms,
            double maxRms) {

        this.id = id;
        this.startTime = startTime;
        this.endTime = endTime;
        this.filePath = filePath;
        this.averageRms = averageRms;
        this.maxRms = maxRms;
    }

    public Long getId() {
        return id;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }

    public Duration getDuration() {
        return Duration.between(
                startTime,
                endTime);
    }

    public Path getFilePath() {
        return filePath;
    }

    public double getAverageRms() {
        return averageRms;
    }

    public double getMaxRms() {
        return maxRms;
    }

    @Override
    public String toString() {

        return "Transmission{" +
                "id=" + id +
                ", startTime=" + startTime +
                ", endTime=" + endTime +
                ", duration=" + getDuration().toMillis() + " ms" +
                ", filePath=" + filePath +
                ", averageRms=" + averageRms +
                ", maxRms=" + maxRms +
                '}';
    }
}