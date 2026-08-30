package ca.vendrx.ui;

import ca.vendrx.model.Transmission;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

import java.util.Collection;
import java.util.List;

public final class TransmissionTable extends TableView<Transmission> {

    public TransmissionTable() {
        setColumnResizePolicy(CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        getColumns().addAll(List.of(
                createTimeColumn(),
                createDurationColumn(),
                createAverageRmsColumn(),
                createMaxRmsColumn(),
                createFileColumn()));
    }

    public Transmission getSelectedTransmission() {
        return getSelectionModel().getSelectedItem();
    }

    public ReadOnlyObjectProperty<Transmission> selectedTransmissionProperty() {
        return getSelectionModel().selectedItemProperty();
    }

    public void setTransmissions(Collection<Transmission> transmissions) {
        getItems().setAll(transmissions);
    }

    public void addFirst(Transmission transmission) {
        getItems().add(0, transmission);
    }

    public void removeTransmission(Transmission transmission) {
        getItems().remove(transmission);
    }

    private TableColumn<Transmission, String> createTimeColumn() {
        TableColumn<Transmission, String> column = new TableColumn<>("Time");
        column.setCellValueFactory(cell -> new SimpleStringProperty(
                TransmissionFormatters.dateTime(cell.getValue().getStartTime())));
        return column;
    }

    private TableColumn<Transmission, Number> createDurationColumn() {
        TableColumn<Transmission, Number> column = new TableColumn<>("Duration (s)");
        column.setCellValueFactory(cell -> new SimpleDoubleProperty(
                cell.getValue().getDuration().toMillis() / 1000.0));
        return column;
    }

    private TableColumn<Transmission, Number> createAverageRmsColumn() {
        TableColumn<Transmission, Number> column = new TableColumn<>("Avg RMS");
        column.setCellValueFactory(cell -> new SimpleDoubleProperty(
                cell.getValue().getAverageRms()));
        return column;
    }

    private TableColumn<Transmission, Number> createMaxRmsColumn() {
        TableColumn<Transmission, Number> column = new TableColumn<>("Max RMS");
        column.setCellValueFactory(cell -> new SimpleDoubleProperty(
                cell.getValue().getMaxRms()));
        return column;
    }

    private TableColumn<Transmission, String> createFileColumn() {
        TableColumn<Transmission, String> column = new TableColumn<>("File");
        column.setCellValueFactory(cell -> new SimpleStringProperty(
                cell.getValue().getFilePath().getFileName().toString()));
        return column;
    }
}
