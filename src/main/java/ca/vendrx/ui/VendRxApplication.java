package ca.vendrx.ui;

import ca.vendrx.audio.AudioDeviceService;
import ca.vendrx.config.AudioConfig;
import ca.vendrx.database.TransmissionRepository;
import ca.vendrx.model.Transmission;
import ca.vendrx.service.AudioPlaybackService;
import ca.vendrx.service.TransmissionService;
import ca.vendrx.service.VendRxService;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.SplitPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import javax.sound.sampled.Mixer;
import java.util.List;

public final class VendRxApplication extends Application {

    private static final int RECENT_TRANSMISSION_LIMIT = 100;

    private TransmissionService transmissionService;
    private VendRxService vendRxService;
    private AudioDeviceService audioDeviceService;
    private AudioPlaybackService audioPlaybackService;

    private ComboBox<Mixer.Info> inputComboBox;
    private Button startButton;
    private Button stopButton;
    private Label statusLabel;
    private TransmissionTable transmissionTable;
    private TransmissionDetailsPane detailsPane;

    @Override
    public void start(Stage stage) {
        initializeServices();

        BorderPane root = new BorderPane();
        root.setPadding(new Insets(15));
        root.setTop(createTopPanel());
        root.setCenter(createTransmissionPanel());
        root.setBottom(createStatusBar());

        configureServiceCallbacks();

        stage.setTitle("VendRx");
        stage.setScene(new Scene(root, 800, 500));
        stage.show();

        loadAudioDevices();
        loadTransmissions();
    }

    private void initializeServices() {
        TransmissionRepository repository = new TransmissionRepository();
        repository.initialize();

        transmissionService = new TransmissionService(repository);
        vendRxService = new VendRxService(AudioConfig.defaultConfig(), repository);
        audioDeviceService = new AudioDeviceService();
        audioPlaybackService = new AudioPlaybackService();
    }

    private void configureServiceCallbacks() {
        vendRxService.setAudioMonitorListener(transmission ->
                Platform.runLater(() -> transmissionTable.addFirst(transmission)));

        audioPlaybackService.setOnPlaybackStopped(() -> Platform.runLater(() -> {
            detailsPane.setPlaying(false);
            restoreStatus();
        }));
    }

    private Pane createTopPanel() {
        inputComboBox = new ComboBox<>();
        inputComboBox.setPrefWidth(400);
        inputComboBox.setCellFactory(listView -> createMixerCell());
        inputComboBox.setButtonCell(createMixerCell());

        startButton = new Button("Start monitoring");
        stopButton = new Button("Stop");
        stopButton.setDisable(true);

        startButton.setOnAction(event -> startMonitoring());
        stopButton.setOnAction(event -> stopMonitoring());

        HBox controls = new HBox(
                10,
                new Label("Audio input:"),
                inputComboBox,
                startButton,
                stopButton);
        controls.setPadding(new Insets(0, 0, 15, 0));
        return controls;
    }

    private ListCell<Mixer.Info> createMixerCell() {
        return new ListCell<>() {
            @Override
            protected void updateItem(Mixer.Info mixerInfo, boolean empty) {
                super.updateItem(mixerInfo, empty);
                setText(empty || mixerInfo == null ? null : mixerInfo.getName());
            }
        };
    }

    private SplitPane createTransmissionPanel() {
        transmissionTable = new TransmissionTable();
        detailsPane = new TransmissionDetailsPane();

        transmissionTable.selectedTransmissionProperty().addListener(
                (observable, previous, selected) -> detailsPane.showTransmission(selected));
        detailsPane.setOnPlay(this::playSelectedTransmission);
        detailsPane.setOnStop(audioPlaybackService::stop);
        detailsPane.setOnDelete(this::deleteSelectedTransmission);

        VBox tablePanel = new VBox(10, new Label("Transmissions"), transmissionTable);
        VBox.setVgrow(transmissionTable, Priority.ALWAYS);

        SplitPane splitPane = new SplitPane(tablePanel, detailsPane);
        splitPane.setDividerPositions(0.68);
        return splitPane;
    }

    private Pane createStatusBar() {
        statusLabel = new Label("Idle");
        HBox statusBar = new HBox(statusLabel);
        statusBar.setPadding(new Insets(10, 0, 0, 0));
        return statusBar;
    }

    private void loadAudioDevices() {
        List<Mixer.Info> devices = audioDeviceService.getInputDevices();
        inputComboBox.setItems(FXCollections.observableArrayList(devices));

        if (!devices.isEmpty()) {
            inputComboBox.getSelectionModel().selectFirst();
        }
    }

    private void loadTransmissions() {
        transmissionTable.setTransmissions(
                transmissionService.findRecent(RECENT_TRANSMISSION_LIMIT));
    }

    private void playSelectedTransmission() {
        Transmission transmission = transmissionTable.getSelectedTransmission();
        if (transmission == null) {
            return;
        }

        try {
            audioPlaybackService.play(transmission.getFilePath());
            detailsPane.setPlaying(true);
            statusLabel.setText("Playing: " + transmission.getFilePath().getFileName());
        } catch (Exception e) {
            detailsPane.setPlaying(false);
            statusLabel.setText("Unable to play audio: " + e.getMessage());
        }
    }

    private void startMonitoring() {
        Mixer.Info selectedDevice = inputComboBox.getValue();
        if (selectedDevice == null) {
            statusLabel.setText("Select an audio input.");
            return;
        }

        vendRxService.startMonitoring(selectedDevice);
        statusLabel.setText("Monitoring: " + selectedDevice.getName());
        setMonitoringControls(true);
    }

    private void stopMonitoring() {
        vendRxService.stopMonitoring();
        statusLabel.setText("Idle");
        setMonitoringControls(false);
        loadTransmissions();
    }

    private void setMonitoringControls(boolean monitoring) {
        startButton.setDisable(monitoring);
        stopButton.setDisable(!monitoring);
        inputComboBox.setDisable(monitoring);
    }

    private void deleteSelectedTransmission() {
        Transmission transmission = transmissionTable.getSelectedTransmission();
        if (transmission == null || !confirmDeletion(transmission)) {
            return;
        }

        try {
            audioPlaybackService.stop();
            transmissionService.delete(transmission);
            transmissionTable.removeTransmission(transmission);
            detailsPane.clear();
            statusLabel.setText("Transmission deleted.");
        } catch (Exception e) {
            showError("Delete failed", "Unable to delete transmission.", e.getMessage());
        }
    }

    private boolean confirmDeletion(Transmission transmission) {
        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Delete transmission");
        confirmation.setHeaderText("Delete this transmission?");
        confirmation.setContentText(
                transmission.getFilePath().getFileName()
                        + "\n\nThe WAV file and database entry will be deleted.");
        return confirmation.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK;
    }

    private void showError(String title, String header, String message) {
        Alert error = new Alert(Alert.AlertType.ERROR);
        error.setTitle(title);
        error.setHeaderText(header);
        error.setContentText(message);
        error.showAndWait();
    }

    private void restoreStatus() {
        Mixer.Info selectedDevice = inputComboBox.getValue();
        if (vendRxService.isMonitoring() && selectedDevice != null) {
            statusLabel.setText("Monitoring: " + selectedDevice.getName());
        } else {
            statusLabel.setText("Idle");
        }
    }

    @Override
    public void stop() {
        if (audioPlaybackService != null) {
            audioPlaybackService.stop();
        }
        if (vendRxService != null && vendRxService.isMonitoring()) {
            vendRxService.stopMonitoring();
        }
    }
}
