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

import java.util.Objects;

public class SettingsDayOfWeekFXMLController {
    @FXML
    RadioButton mondayRB, tuesdayRB, wednesdayRB, thursdayRB, fridayRB, saturdayRB, sundayRB;
    @FXML
    ToggleGroup daysToggleGroup;
    @FXML
    Button saveButton;
    PropertiesManager propertiesManager;

    @FXML
    public void initialize(){
        propertiesManager= PropertiesManager.getInstance();
        String startofWeek = propertiesManager.getProperty("start_of_week");
        mondayRB.setUserData("MONDAY");
        tuesdayRB.setUserData("TUESDAY");
        wednesdayRB.setUserData("WEDNESDAY");
        thursdayRB.setUserData("THURSDAY");
        fridayRB.setUserData("FRIDAY");
        saturdayRB.setUserData("SATURDAY");
        sundayRB.setUserData("SUNDAY");
        for(Toggle button : daysToggleGroup.getToggles()){
            if(Objects.equals(button.getUserData().toString(), startofWeek)){
                button.setSelected(true);
            }
        }
        daysToggleGroup.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
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
        propertiesManager.updateApplicationProperty("start_of_week", daysToggleGroup.getSelectedToggle().getUserData().toString());
        saveButton.setDisable(true);
        Node parent = SettingsSubSceneManager.getSource();
        parent.requestFocus();
    }
}
