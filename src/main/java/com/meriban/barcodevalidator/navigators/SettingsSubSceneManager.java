package com.meriban.barcodevalidator.navigators;

import com.meriban.barcodevalidator.managers.PropertiesManager;
import com.meriban.barcodevalidator.controllers.SettingsFXMLController;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;

import java.io.IOException;
import java.util.Objects;

/**
 * The sub-scene manager for the application's settings GUI.
 * @author meriban
 */
public class SettingsSubSceneManager {
    //SUB-SCENE FXML FILE/ID STRING CONSTANTS
    //Used to load the respective sub-scene and also assigned as user data to the respective sub-scene for
    // identification purposes
    public final static String VALIDATION_RULES_SCENE = "/fxmlValidator/settingsValidationRulesScene.fxml";
    public final static String VALIDATION_DELIMITER_SCENE = "/fxmlValidator/settingValidationDelimiterScene.fxml";
    public final static String START_OF_WEEK_SCENE= "/fxmlValidator/settingsDayOfWeekScene.fxml";

    /**
     * Field holding an instance of the FXML controller class of the "parent" scene. In this case SettingsFXMLController.
     */
    private static SettingsFXMLController settingsFXMLController;
    /**
     * The currently shown sub-scene.
     */
    private static String currentSubScene;
    /**
     * The {@code Node} that triggered the current sub-scene's display
     */
    private static Node source;

    /**
     * Sets the instance of the "parent" FXML controller class.
     * @param controller an instance of the "parent" FXML controller class
     */
    public static void setController(SettingsFXMLController controller){
        settingsFXMLController = Objects.requireNonNull(controller, "parent fxml controller instance must not be null");
    }

    /**
     * Gets the instance of the "parent" FXML controller class.
     * @return the "parent" FXML controller class instance
     */
    public static SettingsFXMLController getController(){
        return settingsFXMLController;
    }

    /**
     * Loads the respective sub-scene and passes it to the "parent" FXML controller class.
     * @param sceneIdString the ID String of the sub-scene to load. This must be one of the sub-scene ID String
     *                      constants of this class.
     * @param source the {@code Node} triggering the sub-scene change
     */
    public static void loadSubScene(String sceneIdString, Node source){
        try{
            Node subScene = FXMLLoader.load(Objects.requireNonNull(SettingsSubSceneManager.class.getResource(sceneIdString)),
                    PropertiesManager.getInstance().getLanguageBundle());
            settingsFXMLController.setSubScene(subScene, sceneIdString);
            currentSubScene=sceneIdString;
            setSource(source);
        }catch (IOException e){
            e.printStackTrace();
        }
    }

    /**
     * Gets the {@code String} constant of the currently set sub-scene.
     * @return the {@code String} constant of the currently set sub-scene
     */
    public static String getCurrentSubScene(){
        return currentSubScene;
    }

    /**
     * Sets {@link #source} to the {@code Node} that triggered the current sub-scene's display.
     */
    private static void setSource(Node node){
        source=node;
    }

    /**
     * Retrieves the {@code Node} that triggered the current sub-scene's display.
     * @return the {@code Node} that triggered the current sub-scene's display
     */
    public static Node getSource(){
        return source;
    }
}
