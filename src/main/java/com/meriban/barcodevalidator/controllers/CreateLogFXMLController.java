package com.meriban.barcodevalidator.controllers;

import com.meriban.barcodevalidator.DateParser;
import com.meriban.barcodevalidator.LogParser;
import com.meriban.barcodevalidator.managers.PropertiesManager;
import com.meriban.barcodevalidator.navigators.WindowManager;
import javafx.beans.binding.Bindings;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Callback;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;

import static com.meriban.barcodevalidator.DateParser.FROM;
import static com.meriban.barcodevalidator.DateParser.TO;
import static com.meriban.barcodevalidator.LogParser.LogMode.*;

/**
 * FXML Controller for the application's log creation window scene.
 *
 * @author meriban
 */
public class CreateLogFXMLController {
    /**
     * This is this {@code Scene}'s id. It is used to register or deregister it with the {@link WindowManager} and to
     * load the .fxml file for it. For this to work the name of the .fxml file this class controls must be in the format
     * {@code idScene.fxml}. A window title property must be created in the language properties and must be in the
     * format {@code ID_NAME}, with the value of ID being in all caps.
     */
    public static final String ID = "createLog";
    @FXML
    RadioButton thisWeekRB, lastWeekRB, todayRB, customRB, newSinceRB, allRB;
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
    /**
     * Carries out post-loading setup. This method is called after the controller has been initialised and all control
     * variables injected.
     */
    @FXML
    public void initialize() {
        setUpRadioButtonsGroup();
        populateStartOfWeekInfoLabel();
        populateRunsListView();
        setUpDatePickers();
    }

    /**
     * Sets up {@code RadioButtons} and their {@code ToggleGroup}.
     * <p>Associates {@code RadioButtons} with actions using setUserData and supplying the respective
     * {@link LogParser.LogMode} constant</p>
     * <p>Set {@code ChangeListener} on {@code ToggleGroup} to control enabling and disabling of {@code DatePickers}
     * and log runs {@code ListView}.</p>
     */
    private void setUpRadioButtonsGroup(){
        thisWeekRB.setUserData(THIS_WEEK);
        lastWeekRB.setUserData(LAST_WEEK);
        todayRB.setUserData(TODAY);
        customRB.setUserData(CUSTOM);
        newSinceRB.setUserData(SINCE_LAST);
        allRB.setUserData(ALL);
        //create and set ChangeListener on ToggleGroup to enable or disable DatePickers depending on which Toggle is
        //selected
        createLogRadioGroup.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
            @Override
            public void changed(ObservableValue<? extends Toggle> observableValue, Toggle oldToggle, Toggle newToggle) {
                if (createLogRadioGroup.getSelectedToggle() != null) {
                    switch ((LogParser.LogMode) createLogRadioGroup.getSelectedToggle().getUserData()) {
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
    }

    /**
     * Populate the start of week info label with current {@code start_of_week} property.
     * <p>The property is the {@code String} representation of {@link DayOfWeek}, thus when rendering it for display
     * it is transformed into only the first letter being a capital one.</p>
     */
    private void populateStartOfWeekInfoLabel(){
        String startOfWeek = PropertiesManager.getInstance().getApplicationProperties().getProperty("start_of_week");
        weekLabel.setText("*Start day of week is set as " + startOfWeek.charAt(0) + startOfWeek.substring(1).toLowerCase());
    }

    /**
     * Populates the log runs {@code ListView} with the 10 most recent log runs as specified by the {@code runs}
     * property.
     */
    private void populateRunsListView(){
        //This works to set the size of the ListView to be adaptable to the number of runs displayed; ListView rows are
        // by default 24px high
        final int ROW_HEIGHT = 24;
        //retrieve the log run history in reverse order as an ObservableList that can be fed to the ListView
        ObservableList<String> runsObservable = PropertiesManager.getInstance().getLogRunsHistory();
        runsListView.setItems(runsObservable);
        runsListView.prefHeightProperty().bind(Bindings.size(runsObservable).multiply(ROW_HEIGHT));
        runsListView.getSelectionModel().selectFirst();
    }

    /**
     * Sets up {@code DatePickers}. Both {@code DatePickers} are set to current date.
     * <p>A {@code ChangeListener} is set on the FromDatePicker to adjust the ToDatePicker's value
     * to the same date should its current date be before the date set on the FromDatePicker.</p>
     * <p>A DayCellFactory is created and set on the ToDatePicker ensuring dates before the FromDatePicker's date are
     * not selectable.</p>
     */
    private void setUpDatePickers(){
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

    /**
     * Controls behaviour when the {@link #createLogButton} is clicked.
     * <p>The {@link LogParser.LogMode} is retrieved from the selected {@code RadioButton}'s
     * user data, the state of the {@link #includeRemovalsCheckBox} evaluated and a save file created. The retrieved
     * data is then passed to the {@link LogParser} and the window deregistered from the {@link WindowManager} and
     * closed.</p>
     * @param event the button interaction event
     */
    @FXML
    private void handleCreateLogButtonOnAction(Event event) {
        LocalDateTime now = LocalDateTime.now();
        PropertiesManager.getInstance().updateApplicationProperty("last_run", now.toString());
        LogParser.LogMode mode = (LogParser.LogMode) createLogRadioGroup.getSelectedToggle().getUserData();
        //if none of the previous runs are selected do nothing
        if(mode==SINCE_LAST && runsListView.getSelectionModel().getSelectedItem()==null){
            return;
        }
        boolean removals = includeRemovalsCheckBox.isSelected();
        HashMap<Integer, LocalDate> dates = DateParser.getFileNameDates(mode, fromDatePicker.getValue(), toDatePicker.getValue());
        String initialFileName = createFileName(mode, dates);
        Button button = (Button) event.getTarget();
        File saveFile = getSaveFile(button,initialFileName);
        if (saveFile != null) {
            switch (mode){
                case CUSTOM:
                    LogParser.createLog(saveFile, mode, removals, fromDatePicker.getValue(), toDatePicker.getValue());
                    break;
                case SINCE_LAST:
                    LocalDateTime fromDateTime;
                    if(runsListView.getSelectionModel().getSelectedItem().equals("")){//if there is no previous run
                        fromDateTime=null;
                    }else{
                        //LocalDateTime.parse takes only yyyy-mm-ddThh:mm:ss format. To change that is a faff, replacing the
                        //space is easier.
                        fromDateTime = LocalDateTime.parse(runsListView.getSelectionModel().getSelectedItem().replace(" ", "T"));
                    }
                    PropertiesManager.getInstance().addToLogRunsHistory(now);
                    LogParser.createLog(saveFile, mode, removals, fromDateTime);
                    break;
                case ALL:
                    LogParser.createLog(saveFile,mode,removals,null);
                    break;
                default:
                    LogParser.createLog(saveFile, mode, removals, dates.get(FROM), dates.get(TO));
            }
            Stage stage = (Stage) button.getScene().getWindow();
            WindowManager.deregisterStage(ID);
            stage.close();
        }
    }
    /**
     * Creates a file name consisting of "barcodeValidatorLog" and the date range reported on, each separated by "_",
     * e.g. {@code barcodeValidatorLog_2022-11-28_2022-12-04.txt}.
     * If there is no to date only the from date will be used.
     * @param mode the {@link LogParser.LogMode}
     * @param dates the dates retrieved from the GUI or computed based on the respective {@code LogMode}.
     * @return the file name
     */
    private String createFileName(@NotNull LogParser.LogMode mode, HashMap<Integer, LocalDate> dates){
        String fileName;
        switch (mode){
            case CUSTOM:
                fileName = "barcodeValidatorLog_" + dates.get(FROM).toString() + "_"+dates.get(TO).toString();
                break;
            case SINCE_LAST:
                fileName = "barcodeValidatorLog_"+dates.get(FROM).toString();
                break;
            case ALL:
                fileName = "barcodeValidatorLog_all";
                break;
            default:
                fileName = "barcodeValidatorLog_" + dates.get(FROM).toString();
                if (dates.get(TO) != null) {
                    fileName = fileName + "_" + dates.get(TO).toString();
                }
        }
        return fileName;
    }

    /**
     * Opens a {@link FileChooser} with extension filters for {@code .txt} and {@code .csv} files, creating a save file
     * for the log report. The {@code FileChooser} defaults to the user's desktop folder.
     * @param node a window or node within a window the {@code FileChooser} can be shown from
     * @param fileName the file name populating the {@code FileChooser}
     * @return the save file
     */
    private File getSaveFile(@NotNull Node node, String fileName){
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Text Files", "*.txt"),
                new FileChooser.ExtensionFilter("CSV Files", "*.csv")
        );
        fileChooser.setInitialFileName(fileName);
        fileChooser.setInitialDirectory(new File (System.getProperty("user.home") +"/Desktop"));
        return  fileChooser.showSaveDialog(node.getScene().getWindow());
    }

    /**
     * Deregisters the window with the {@link WindowManager} and closes it when the "Cancel" button is clicked.
     * @param event the button interaction event
     */
    @FXML
    private void handleCancelButtonOnAction(@NotNull Event event){
        Button button = (Button) event.getTarget();
        Stage stage = (Stage) button.getScene().getWindow();
        WindowManager.deregisterStage(ID);
        stage.close();
    }
}
