package com.meriban.barcodevalidator.controllers;

import com.meriban.barcodevalidator.navigators.SettingsSubSceneManager;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.layout.StackPane;

public class SettingsFXMLController {
    public static final String ID = "settings";
    @FXML
    StackPane settingsStackPane;

    @FXML
    public void initialize() {
        SettingsSubSceneManager.setController(this);
        SettingsSubSceneManager.loadSubScene(SettingsSubSceneManager.VALIDATION_RULES_SCENE);
    }
    @FXML
    private void handleEditValidationRulesButtonOnAction(Event event) {
        if (!SettingsSubSceneManager.getCurrentSubScene().equals(SettingsSubSceneManager.VALIDATION_RULES_SCENE)) {
            SettingsSubSceneManager.loadSubScene(SettingsSubSceneManager.VALIDATION_RULES_SCENE);
            SettingsSubSceneManager.setSource((Node) event.getTarget());
        }
    }
    @FXML
    private void handleEditValidationDelimiterButtonOnAction(Event event){
        if (!SettingsSubSceneManager.getCurrentSubScene().equals(SettingsSubSceneManager.VALIDATION_DELIMITER_SCENE)) {
            SettingsSubSceneManager.loadSubScene(SettingsSubSceneManager.VALIDATION_DELIMITER_SCENE);
            SettingsSubSceneManager.setSource((Node) event.getTarget());
        }
    }

    @FXML
    private void handleEditStartOfWeekButtonOnAction(Event event){
        if (!SettingsSubSceneManager.getCurrentSubScene().equals(SettingsSubSceneManager.START_OF_WEEK_SCENE)) {
            SettingsSubSceneManager.loadSubScene(SettingsSubSceneManager.START_OF_WEEK_SCENE);
            SettingsSubSceneManager.setSource((Node) event.getTarget());
        }
    }

    public void setSubScene(Node node, String id){
        node.setUserData(id);
        settingsStackPane.getChildren().setAll(node);
    }


}
