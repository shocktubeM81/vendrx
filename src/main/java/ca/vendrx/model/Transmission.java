package ca.vendrx.model;

import java.nio.file.Path;
import java.time.Duration;
import java.time.LocalDateTime;

public class Transmission {

    private final LocalDateTime startTime;
    private final LocalDateTime endTime;

    private final Path filePath;

    private final double averageRms;
    private final double maxRms;

    public Transmission(
            LocalDateTime startTime,
            LocalDateTime endTime,
            Path filePath,
            double averageRms,
            double maxRms
    ) {
        this.startTime = startTime;
        this.endTime = endTime;
        this.filePath = filePath;
        this.averageRms = averageRms;
        this.maxRms = maxRms;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }

    public Duration getDuration() {
        return Duration.between(startTime, endTime);
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
                "startTime=" + startTime +
                ", endTime=" + endTime +
                ", duration=" + getDuration().toMillis() + " ms" +
                ", filePath=" + filePath +
                ", averageRms=" + averageRms +
                ", maxRms=" + maxRms +
                '}';
    }
}