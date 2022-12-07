package com.meriban.barcodevalidator.controllers;

import com.meriban.barcodevalidator.LogParser;
import com.meriban.barcodevalidator.managers.PropertiesManager;
import com.meriban.barcodevalidator.navigators.WindowManager;
import javafx.beans.binding.Bindings;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Callback;

import java.io.File;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Objects;
import java.util.Properties;

import static com.meriban.barcodevalidator.LogParser.FROM;
import static com.meriban.barcodevalidator.LogParser.TO;


public class CreateLogFXMLController {

    public static final String ID = "createLog";
    public static final String THIS_WEEK = "thisWeek";
    public static final String LAST_WEEK = "lastWeek";
    public static final String TODAY = "today";
    public static final String CUSTOM = "custom";
    public static final String BARCODE = "barcode";
    public static final String SINCE_LAST = "sinceLast";
    @FXML
    RadioButton thisWeekRB, lastWeekRB, todayRB, customRB, newSinceRB;
    @FXML
    ToggleGroup createLogRadioGroup;
    @FXML
    DatePicker fromDatePicker, toDatePicker;
    @FXML
    Button createLogButton;
    @FXML
    CheckBox includeRemovalsCheckBox;
    @FXML
    Label weekLabel;
    @FXML
    ListView<String> runsListView;

    LogParser logParser;
    private final int ROW_HEIGHT = 24;

    @FXML
    public void initialize() {
        logParser = LogParser.getInstance();
        thisWeekRB.setUserData(THIS_WEEK);
        lastWeekRB.setUserData(LAST_WEEK);
        todayRB.setUserData(TODAY);
        customRB.setUserData(CUSTOM);
        newSinceRB.setUserData(SINCE_LAST);

        Properties applicationProperties = PropertiesManager.getInstance().getApplicationProperties();
        String startOfWeek = applicationProperties.getProperty("start_of_week");
        weekLabel.setText("*Start day of week is set as " + startOfWeek.substring(0, 1) + startOfWeek.substring(1).toLowerCase());

        //retrieve the log run history in reverse order as an ObservableList that can be fed to the ListView
        ObservableList<String> runsObservable = PropertiesManager.getInstance().getLogRunsHistory();
        runsListView.setItems(runsObservable);
        runsListView.prefHeightProperty().bind(Bindings.size(runsObservable).multiply(ROW_HEIGHT));
        runsListView.getSelectionModel().selectFirst();

        //create and set ChangeListener on ToggleGroup to enable or disable DatePickers depending on which Toggle is
        //selected
        createLogRadioGroup.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
            @Override
            public void changed(ObservableValue<? extends Toggle> observableValue, Toggle oldToggle, Toggle newToggle) {
                if (createLogRadioGroup.getSelectedToggle() != null) {
                    switch (createLogRadioGroup.getSelectedToggle().getUserData().toString()) {
                        case CUSTOM:
                            fromDatePicker.setDisable(false);
                            toDatePicker.setDisable(false);
                            runsListView.setDisable(true);
                            break;
                        case SINCE_LAST:
                            fromDatePicker.setDisable(true);
                            toDatePicker.setDisable(true);
                            runsListView.setDisable(false);
                            break;
                        default:
                            fromDatePicker.setDisable(true);
                            toDatePicker.setDisable(true);
                            runsListView.setDisable(true);
                    }
                }
            }
        });

        //set fromDatePicker to current date.
        fromDatePicker.setValue(LocalDate.now());
        //Listener to adjust toDatePicker to fromDatePicker date if the current toDatePicker date is before the new
        //fromDatePicker date
        fromDatePicker.valueProperty().addListener(new ChangeListener<LocalDate>() {
            @Override
            public void changed(ObservableValue<? extends LocalDate> observableValue, LocalDate oldDate, LocalDate newDate) {
                if (toDatePicker.getValue().isBefore(newDate)) {
                    toDatePicker.setValue(fromDatePicker.getValue());
                }
            }
        });
        //create dayCellFactory to disable cells representing dates before the date of the fromDatePicker
        final Callback<DatePicker, DateCell> dayCellFactory = new Callback<>() {
            @Override
            public DateCell call(final DatePicker datePicker) {
                return new DateCell() {
                    @Override
                    public void updateItem(LocalDate item, boolean empty) {
                        super.updateItem(item, empty);
                        if (item.isBefore(fromDatePicker.getValue())) {
                            setDisable(true);
                            setStyle("-fx-background-color : #ffc0cb");
                        }
                    }
                };
            }
        };
        //apply dayCellFactory
        toDatePicker.setDayCellFactory(dayCellFactory);
        toDatePicker.setValue(fromDatePicker.getValue());

    }

    @FXML
    private void handleCreateLogButtonOnAction(Event event) {
        Toggle activeToggle = createLogRadioGroup.getSelectedToggle();
        String mode = String.valueOf(activeToggle.getUserData());
        boolean removals = includeRemovalsCheckBox.isSelected();
        LocalDate fromDate = null;
        LocalDate toDate = null;
        String initialFileName;
        switch (mode) {
            case CUSTOM:
                fromDate = fromDatePicker.getValue();
                toDate = toDatePicker.getValue();
                initialFileName = "barcodeValidatorLog_" + fromDate.toString() + "_" + toDate.toString();
                break;
            case SINCE_LAST:
                if(runsListView.getSelectionModel().getSelectedItem()!=null) {
                    fromDate = LocalDate.now();
                    initialFileName = "barcodeValidatorLog_" + fromDate;
                }else {
                    return;
            }
                break;
            default:
                Properties applicationProperties = PropertiesManager.getInstance().getApplicationProperties();
                String startOfWeek = applicationProperties.getProperty("start_of_week");
                HashMap<Integer, String> dates = logParser.computeDatesForDisplay(mode, DayOfWeek.valueOf(startOfWeek));
                initialFileName = "barcodeValidatorLog_" + dates.get(FROM);
                if (dates.get(TO) != null) {
                    initialFileName = initialFileName + "_" + dates.get(TO);
                }
        }
        Button button = (Button) event.getTarget();
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Text Files", "*.txt"),
                new FileChooser.ExtensionFilter("CSV Files", "*.csv")
        );
        fileChooser.setInitialFileName(initialFileName);
        fileChooser.setInitialDirectory(new File (System.getProperty("user.home") +"/Desktop"));
        File saveFile = fileChooser.showSaveDialog(button.getScene().getWindow());
        if (saveFile != null) {
            if (Objects.equals(mode, SINCE_LAST)) {
                //LocalDateTime.parse takes only yyyy-mm-ddThh:mm:ss format. To change that is a faff, replacing the
                //space is easier.
                LocalDateTime fromDateTime = LocalDateTime.parse(runsListView.getSelectionModel().getSelectedItem().replace(" ", "T"));
                PropertiesManager.getInstance().addToLogRunsHistory(LocalDateTime.now());
                logParser.createLog(saveFile, mode, removals, fromDateTime);
            } else {
                logParser.createLog(saveFile, mode, removals, fromDate, toDate);
            }
            Stage stage = (Stage) button.getScene().getWindow();
            WindowManager.deregisterStage(ID);
            stage.close();

        }
    }

    @FXML
    private void handleCancelButtonOnAction(Event event){
        Button button = (Button) event.getTarget();
        Stage stage = (Stage) button.getScene().getWindow();
        WindowManager.deregisterStage(ID);
        stage.close();
    }
}
