package ca.vendrx.ui;

import ca.vendrx.model.Transmission;

import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.time.format.DateTimeFormatter;

public class TransmissionDetailsPane extends VBox {

    private static final DateTimeFormatter DATE_FORMATTER =
            DateTimeFormatter.ofPattern(
                    "yyyy-MM-dd HH:mm:ss"
            );

    private final Label startLabel =
            new Label("-");

    private final Label endLabel =
            new Label("-");

    private final Label durationLabel =
            new Label("-");

    private final Label averageRmsLabel =
            new Label("-");

    private final Label maxRmsLabel =
            new Label("-");

    private final Label fileLabel =
            new Label("-");

    private final Button playButton =
            new Button("▶ Play");

    private final Button stopButton =
            new Button("■ Stop");

    private final Button deleteButton =
            new Button("Delete");

    private Transmission transmission;

    public TransmissionDetailsPane() {

        setSpacing(15);
        setPadding(
                new Insets(10)
        );

        fileLabel.setWrapText(true);

        GridPane grid =
                new GridPane();

        grid.setHgap(10);
        grid.setVgap(10);

        grid.add(
                new Label("Started:"),
                0,
                0
        );

        grid.add(
                startLabel,
                1,
                0
        );

        grid.add(
                new Label("Ended:"),
                0,
                1
        );

        grid.add(
                endLabel,
                1,
                1
        );

        grid.add(
                new Label("Duration:"),
                0,
                2
        );

        grid.add(
                durationLabel,
                1,
                2
        );

        grid.add(
                new Label("Average RMS:"),
                0,
                3
        );

        grid.add(
                averageRmsLabel,
                1,
                3
        );

        grid.add(
                new Label("Maximum RMS:"),
                0,
                4
        );

        grid.add(
                maxRmsLabel,
                1,
                4
        );

        grid.add(
                new Label("File:"),
                0,
                5
        );

        grid.add(
                fileLabel,
                1,
                5
        );

        HBox controls =
                new HBox(
                        10,
                        playButton,
                        stopButton,
                        deleteButton
                );

        getChildren().addAll(
                new Label("Transmission details"),
                grid,
                controls
        );

        clear();
    }

    public void showTransmission(
            Transmission transmission
    ) {

        if (transmission == null) {
            clear();
            return;
        }

        this.transmission =
                transmission;

        startLabel.setText(
                transmission
                        .getStartTime()
                        .format(DATE_FORMATTER)
        );

        endLabel.setText(
                transmission
                        .getEndTime()
                        .format(DATE_FORMATTER)
        );

        durationLabel.setText(
                String.format(
                        "%.3f s",
                        transmission
                                .getDuration()
                                .toMillis()
                                / 1000.0
                )
        );

        averageRmsLabel.setText(
                String.format(
                        "%.4f",
                        transmission
                                .getAverageRms()
                )
        );

        maxRmsLabel.setText(
                String.format(
                        "%.4f",
                        transmission
                                .getMaxRms()
                )
        );

        fileLabel.setText(
                transmission
                        .getFilePath()
                        .toString()
        );

        playButton.setDisable(false);
        deleteButton.setDisable(false);
        stopButton.setDisable(true);
    }

    public void clear() {

        transmission = null;

        startLabel.setText("-");
        endLabel.setText("-");
        durationLabel.setText("-");
        averageRmsLabel.setText("-");
        maxRmsLabel.setText("-");
        fileLabel.setText("-");

        playButton.setDisable(true);
        stopButton.setDisable(true);
        deleteButton.setDisable(true);
    }

    public void setPlaying(
            boolean playing
    ) {

        playButton.setDisable(
                playing
                || transmission == null
        );

        stopButton.setDisable(
                !playing
        );

        deleteButton.setDisable(
                playing
                || transmission == null
        );
    }

    public void setOnPlay(
            Runnable action
    ) {

        playButton.setOnAction(
                event ->
                        action.run()
        );
    }

    public void setOnStop(
            Runnable action
    ) {

        stopButton.setOnAction(
                event ->
                        action.run()
        );
    }

    public void setOnDelete(
            Runnable action
    ) {

        deleteButton.setOnAction(
                event ->
                        action.run()
        );
    }
}