package ca.vendrx.ui;

import ca.vendrx.audio.AudioDeviceService;
import ca.vendrx.config.AudioConfig;
import ca.vendrx.database.TransmissionRepository;
import ca.vendrx.model.Transmission;
import ca.vendrx.service.VendRxService;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;

import java.time.format.DateTimeFormatter;

import javax.sound.sampled.Mixer;
import java.util.List;

public class VendRxApplication extends Application {

        private TransmissionRepository repository;
        private VendRxService vendRxService;
        private AudioDeviceService audioDeviceService;

        private ComboBox<Mixer.Info> inputComboBox;

        private Button startButton;
        private Button stopButton;

        private Label statusLabel;
        private Label detailStartLabel;
        private Label detailEndLabel;
        private Label detailDurationLabel;
        private Label detailAverageRmsLabel;
        private Label detailMaxRmsLabel;
        private Label detailFileLabel;

        private TableView<Transmission> transmissionTable;

        @Override
        public void start(Stage stage) {
        
                        initializeServices();

                BorderPane root =
                        new BorderPane();

                root.setPadding(
                        new Insets(15)
                );

                root.setTop(
                        createTopPanel()
                );

                root.setCenter(
                        createTransmissionPanel()
                );

                root.setBottom(
                        createStatusBar()
                );

                Scene scene =
                        new Scene(
                                root,
                                800,
                                500
                        );

                stage.setTitle(
                        "VendRx"
                );

                stage.setScene(scene);

                stage.show();

                loadAudioDevices();
                loadTransmissions();
        }

        private void initializeServices() {

                repository =
                        new TransmissionRepository();

                repository.initialize();

                AudioConfig audioConfig =
                        AudioConfig.defaultConfig();

                vendRxService =
                        new VendRxService(
                                audioConfig,
                                repository
                        );

                vendRxService.setAudioMonitorListener(
                        transmission ->
                                Platform.runLater(
                                        () ->
                                                transmissionTable
                                                        .getItems()
                                                        .add(0, transmission)
                                )
                );

                audioDeviceService =
                        new AudioDeviceService();
        }

        private Pane createTopPanel() {

                Label inputLabel =
                        new Label(
                                "Audio input:"
                        );

                inputComboBox =
                        new ComboBox<>();

                inputComboBox.setPrefWidth(
                        400
                );

                inputComboBox.setCellFactory(
                        listView ->
                                createMixerCell()
                );

                inputComboBox.setButtonCell(
                        createMixerCell()
                );

                startButton =
                        new Button(
                                "Start monitoring"
                        );

                stopButton =
                        new Button(
                                "Stop"
                        );

                stopButton.setDisable(true);

                startButton.setOnAction(
                        event ->
                                startMonitoring()
                );

                stopButton.setOnAction(
                        event ->
                                stopMonitoring()
                );

                HBox controls =
                        new HBox(
                                10,
                                inputLabel,
                                inputComboBox,
                                startButton,
                                stopButton
                        );

                controls.setPadding(
                        new Insets(
                                0,
                                0,
                                15,
                                0
                        )
                );

                return controls;
        }

        private ListCell<Mixer.Info> createMixerCell() {

                return new ListCell<>() {

                @Override
                protected void updateItem(
                        Mixer.Info mixerInfo,
                        boolean empty
                ) {

                        super.updateItem(
                                mixerInfo,
                                empty
                        );

                        if (
                                empty
                                || mixerInfo == null
                        ) {

                        setText(null);

                        } else {

                        setText(
                                mixerInfo.getName()
                        );
                        }
                }
                };
        }

        private Pane createTransmissionPanel() {

                Label title =
                        new Label("Transmissions");

                transmissionTable =
                        new TableView<>();

                transmissionTable.setColumnResizePolicy(
                        TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN
                );

                DateTimeFormatter dateFormatter =
                        DateTimeFormatter.ofPattern(
                                "yyyy-MM-dd HH:mm:ss"
                        );

                TableColumn<Transmission, String> timeColumn =
                        new TableColumn<>("Time");

                timeColumn.setCellValueFactory(
                        cellData ->
                                new SimpleStringProperty(
                                        cellData
                                                .getValue()
                                                .getStartTime()
                                                .format(dateFormatter)
                                )
                );

                TableColumn<Transmission, Number> durationColumn =
                        new TableColumn<>("Duration (s)");

                durationColumn.setCellValueFactory(
                        cellData ->
                                new SimpleDoubleProperty(
                                        cellData
                                                .getValue()
                                                .getDuration()
                                                .toMillis()
                                                / 1000.0
                                )
                );

                TableColumn<Transmission, Number> averageRmsColumn =
                        new TableColumn<>("Avg RMS");

                averageRmsColumn.setCellValueFactory(
                        cellData ->
                                new SimpleDoubleProperty(
                                        cellData
                                                .getValue()
                                                .getAverageRms()
                                )
                );

                TableColumn<Transmission, Number> maxRmsColumn =
                        new TableColumn<>("Max RMS");

                maxRmsColumn.setCellValueFactory(
                        cellData ->
                                new SimpleDoubleProperty(
                                        cellData
                                                .getValue()
                                                .getMaxRms()
                                )
                );

                TableColumn<Transmission, String> fileColumn =
                        new TableColumn<>("File");

                fileColumn.setCellValueFactory(
                        cellData ->
                                new SimpleStringProperty(
                                        cellData
                                                .getValue()
                                                .getFilePath()
                                                .getFileName()
                                                .toString()
                                )
                );

                transmissionTable
                        .getColumns()
                        .addAll(
                                timeColumn,
                                durationColumn,
                                averageRmsColumn,
                                maxRmsColumn,
                                fileColumn
                        );

                transmissionTable
                        .getSelectionModel()
                        .selectedItemProperty()
                        .addListener(
                                (observable, oldValue, newValue) -> {

                                        if (newValue != null) {
                                        showTransmissionDetails(
                                                newValue
                                        );
                                        }
                                }
                        );

                VBox tablePanel =
                        new VBox(
                                10,
                                title,
                                transmissionTable
                        );

                VBox.setVgrow(
                        transmissionTable,
                        Priority.ALWAYS
                );

                Pane detailPanel =
                        createTransmissionDetailPanel();

                SplitPane splitPane =
                        new SplitPane(
                                tablePanel,
                                detailPanel
                        );

                splitPane.setDividerPositions(
                        0.68
                );

                return splitPane;
        }

    private Pane createStatusBar() {

        statusLabel =
                new Label(
                        "Idle"
                );

        HBox statusBar =
                new HBox(
                        statusLabel
                );

        statusBar.setPadding(
                new Insets(
                        10,
                        0,
                        0,
                        0
                )
        );

        return statusBar;
    }

    private void loadAudioDevices() {

        List<Mixer.Info> devices =
                audioDeviceService
                        .getInputDevices();

        inputComboBox.setItems(
                FXCollections.observableArrayList(
                        devices
                )
        );

        if (!devices.isEmpty()) {

            inputComboBox
                    .getSelectionModel()
                    .selectFirst();
        }
    }

    private void loadTransmissions() {

        List<Transmission> transmissions =
                repository.findRecent(100);

        transmissionTable
                .getItems()
                .setAll(transmissions);
    }

    private void startMonitoring() {

        Mixer.Info selectedDevice =
                inputComboBox.getValue();

        if (selectedDevice == null) {

            statusLabel.setText(
                    "Select an audio input."
            );

            return;
        }

        vendRxService.startMonitoring(
                selectedDevice
        );

        statusLabel.setText(
                "Monitoring: "
                + selectedDevice.getName()
        );

        startButton.setDisable(true);
        stopButton.setDisable(false);

        inputComboBox.setDisable(true);
    }

    private void stopMonitoring() {

        vendRxService.stopMonitoring();

        statusLabel.setText(
                "Idle"
        );

        startButton.setDisable(false);
        stopButton.setDisable(true);

        inputComboBox.setDisable(false);

        loadTransmissions();
    }

    @Override
    public void stop() {

        if (
                vendRxService != null
                && vendRxService.isMonitoring()
        ) {

            vendRxService.stopMonitoring();
        }
    }
}