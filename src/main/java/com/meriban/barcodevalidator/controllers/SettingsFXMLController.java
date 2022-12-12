package com.meriban.barcodevalidator.controllers;

import com.meriban.barcodevalidator.navigators.SettingsSubSceneManager;
import com.meriban.barcodevalidator.navigators.WindowManager;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

/**
 * FXML Controller for the application's settings window scene.
 *
 * @author meriban
 */
public class SettingsFXMLController {
    public static final String ID = "settings";
    /**
     * The "parent" {@code Node} for this window's sub-scenes
     */
    @FXML
    StackPane settingsStackPane;
    /**
     * Carries out post-loading setup. This method is called after the controller has been initialised and all control
     * variables injected.
     * <p>Loads the default sub-scene.</p>
     */
    @FXML
    public void initialize() {
        SettingsSubSceneManager.setController(this);
        SettingsSubSceneManager.loadSubScene(SettingsSubSceneManager.VALIDATION_RULES_SCENE, null);
    }

    @FXML
    private void handleEditValidationRulesButtonOnAction(Event event) {
        if (!SettingsSubSceneManager.getCurrentSubScene().equals(SettingsSubSceneManager.VALIDATION_RULES_SCENE)) {
            SettingsSubSceneManager.loadSubScene(SettingsSubSceneManager.VALIDATION_RULES_SCENE, (Node) event.getTarget());
        }
    }
    @FXML
    private void handleEditValidationDelimiterButtonOnAction(Event event){
        if (!SettingsSubSceneManager.getCurrentSubScene().equals(SettingsSubSceneManager.VALIDATION_DELIMITER_SCENE)) {
            SettingsSubSceneManager.loadSubScene(SettingsSubSceneManager.VALIDATION_DELIMITER_SCENE, (Node) event.getTarget());
        }
    }

    @FXML
    private void handleEditStartOfWeekButtonOnAction(Event event){
        if (!SettingsSubSceneManager.getCurrentSubScene().equals(SettingsSubSceneManager.START_OF_WEEK_SCENE)) {
            SettingsSubSceneManager.loadSubScene(SettingsSubSceneManager.START_OF_WEEK_SCENE, (Node) event.getTarget());
        }
    }

    @FXML
    private void handleCancelButtonOnAction(Event event){
        Button button = (Button) event.getTarget();
        Stage stage = (Stage) button.getScene().getWindow();
        WindowManager.deregisterStage(ID);
        stage.close();
    }

    /**
     * Swaps out the child node(s) of the sub-scene container ({@link #settingsStackPane}) and assigns the new sub-scene
     * an ID using user data.
     * @param subScene the new sub-scene
     * @param id the new sub-scene's ID. This must be one of the sub-scene ID String constants of
     * {@link SettingsSubSceneManager}.
     */
    public void setSubScene(Node subScene, String id){
        subScene.setUserData(id);
        settingsStackPane.getChildren().setAll(subScene);
    }


}
