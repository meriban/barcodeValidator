package com.meriban.barcodevalidator.navigators;

import com.meriban.barcodevalidator.managers.PropertiesManager;
import com.meriban.barcodevalidator.controllers.SettingsFXMLController;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;

import java.io.IOException;

public class SettingsSubSceneManager {
    public final static String VALIDATION_RULES_SCENE = "/fxmlValidator/settingsValidationRulesScene.fxml";
    public final static String VALIDATION_DELIMITER_SCENE = "/fxmlValidator/settingValidationDelimiterScene.fxml";
    public final static String START_OF_WEEK_SCENE= "/fxmlValidator/settingsDayOfWeekScene.fxml";

    private static SettingsFXMLController settingsFXMLController;
    private static String currentSubScene;

    private static Node source;

    public static void setController(SettingsFXMLController controller){
        settingsFXMLController = controller;
    }

    public static SettingsFXMLController getController(){
        return settingsFXMLController;
    }

    public static void loadSubScene(String scene){
        try{
            Node subScene = FXMLLoader.load(SettingsSubSceneManager.class.getResource(scene), PropertiesManager.getInstance().getLanguageBundle());
            settingsFXMLController.setSubScene(subScene, scene);
            currentSubScene=scene;
        }catch (IOException e){
            e.printStackTrace();
        }
    }

    public static String getCurrentSubScene(){
        return currentSubScene;
    }

    public static void setSource(Node node){
        source=node;
    }
    public static Node getSource(){
        return source;
    }
}
