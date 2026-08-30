package ca.vendrx.ui;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

final class TransmissionFormatters {

    private static final DateTimeFormatter DATE_TIME_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private TransmissionFormatters() {
    }

    static String dateTime(LocalDateTime dateTime) {
        return dateTime.format(DATE_TIME_FORMATTER);
    }

    static String duration(Duration duration) {
        return String.format("%.3f s", duration.toMillis() / 1000.0);
    }

    static String rms(double rms) {
        return String.format("%.4f", rms);
    }
}
