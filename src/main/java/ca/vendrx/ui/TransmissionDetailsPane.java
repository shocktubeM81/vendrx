package ca.vendrx.ui;

import ca.vendrx.model.Transmission;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

public final class TransmissionDetailsPane extends VBox {

    private static final String EMPTY_VALUE = "-";

    private final Label startLabel = new Label(EMPTY_VALUE);
    private final Label endLabel = new Label(EMPTY_VALUE);
    private final Label durationLabel = new Label(EMPTY_VALUE);
    private final Label averageRmsLabel = new Label(EMPTY_VALUE);
    private final Label maxRmsLabel = new Label(EMPTY_VALUE);
    private final Label fileLabel = new Label(EMPTY_VALUE);

    private final Button playButton = new Button("▶ Play");
    private final Button stopButton = new Button("■ Stop");
    private final Button deleteButton = new Button("Delete");

    private Transmission transmission;

    public TransmissionDetailsPane() {
        setSpacing(15);
        setPadding(new Insets(10));
        fileLabel.setWrapText(true);

        GridPane detailsGrid = createDetailsGrid();
        HBox controls = new HBox(10, playButton, stopButton, deleteButton);

        getChildren().addAll(new Label("Transmission details"), detailsGrid, controls);
        clear();
    }

    public void showTransmission(Transmission transmission) {
        if (transmission == null) {
            clear();
            return;
        }

        this.transmission = transmission;
        startLabel.setText(TransmissionFormatters.dateTime(transmission.getStartTime()));
        endLabel.setText(TransmissionFormatters.dateTime(transmission.getEndTime()));
        durationLabel.setText(TransmissionFormatters.duration(transmission.getDuration()));
        averageRmsLabel.setText(TransmissionFormatters.rms(transmission.getAverageRms()));
        maxRmsLabel.setText(TransmissionFormatters.rms(transmission.getMaxRms()));
        fileLabel.setText(transmission.getFilePath().toString());
        setPlaying(false);
    }

    public void clear() {
        transmission = null;
        startLabel.setText(EMPTY_VALUE);
        endLabel.setText(EMPTY_VALUE);
        durationLabel.setText(EMPTY_VALUE);
        averageRmsLabel.setText(EMPTY_VALUE);
        maxRmsLabel.setText(EMPTY_VALUE);
        fileLabel.setText(EMPTY_VALUE);
        setPlaying(false);
    }

    public void setPlaying(boolean playing) {
        boolean noSelection = transmission == null;
        playButton.setDisable(playing || noSelection);
        stopButton.setDisable(!playing);
        deleteButton.setDisable(playing || noSelection);
    }

    public void setOnPlay(Runnable action) {
        playButton.setOnAction(event -> action.run());
    }

    public void setOnStop(Runnable action) {
        stopButton.setOnAction(event -> action.run());
    }

    public void setOnDelete(Runnable action) {
        deleteButton.setOnAction(event -> action.run());
    }

    private GridPane createDetailsGrid() {
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);

        addRow(grid, 0, "Started:", startLabel);
        addRow(grid, 1, "Ended:", endLabel);
        addRow(grid, 2, "Duration:", durationLabel);
        addRow(grid, 3, "Average RMS:", averageRmsLabel);
        addRow(grid, 4, "Maximum RMS:", maxRmsLabel);
        addRow(grid, 5, "File:", fileLabel);
        return grid;
    }

    private void addRow(GridPane grid, int row, String title, Label value) {
        grid.add(new Label(title), 0, row);
        grid.add(value, 1, row);
    }
}
