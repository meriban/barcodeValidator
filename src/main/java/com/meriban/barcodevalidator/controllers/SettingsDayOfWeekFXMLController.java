package com.meriban.barcodevalidator.controllers;

import com.meriban.barcodevalidator.managers.PropertiesManager;
import com.meriban.barcodevalidator.navigators.SettingsSubSceneManager;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.RadioButton;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;

import java.time.DayOfWeek;
import java.util.Objects;
/**
 * FXML Controller for the start of the week settings sub-scene.
 *
 * @author meriban
 */
public class SettingsDayOfWeekFXMLController {
    @FXML
    RadioButton mondayRB, tuesdayRB, wednesdayRB, thursdayRB, fridayRB, saturdayRB, sundayRB;
    @FXML
    ToggleGroup daysToggleGroup;
    @FXML
    Button saveButton;
    /**
     * Carries out post-loading setup. This method is called after the controller has been initialised and all control
     * variables injected.
     * <p>Assigns days of the week as user data to {@code RadioButtons}, selecting the ones that corresponds to the
     * current start of week setting.</p>
     */
    @FXML
    public void initialize(){
        String startOfWeek = PropertiesManager.getInstance().getProperty("start_of_week");
        mondayRB.setUserData(DayOfWeek.MONDAY);
        tuesdayRB.setUserData(DayOfWeek.TUESDAY);
        wednesdayRB.setUserData(DayOfWeek.WEDNESDAY);
        thursdayRB.setUserData(DayOfWeek.THURSDAY);
        fridayRB.setUserData(DayOfWeek.FRIDAY);
        saturdayRB.setUserData(DayOfWeek.SATURDAY);
        sundayRB.setUserData(DayOfWeek.SUNDAY);
        for(Toggle button : daysToggleGroup.getToggles()){
            if(Objects.equals(button.getUserData().toString(), startOfWeek)){
                button.setSelected(true);
            }
        }
        daysToggleGroup.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
            //Activate save button if different toggle is selected
            @Override
            public void changed(ObservableValue<? extends Toggle> observableValue, Toggle oldToggle, Toggle newToggle) {
                if(daysToggleGroup.getSelectedToggle() !=null && newToggle!=oldToggle){
                    saveButton.setDisable(false);
                }
            }
        });
    }
    @FXML
    private void handleSaveButtonOnAction(Event event){
        PropertiesManager.getInstance().updateApplicationProperty("start_of_week", daysToggleGroup.getSelectedToggle().getUserData().toString());
        saveButton.setDisable(true);
        Node parent = SettingsSubSceneManager.getSource();
        parent.requestFocus();
    }
}
