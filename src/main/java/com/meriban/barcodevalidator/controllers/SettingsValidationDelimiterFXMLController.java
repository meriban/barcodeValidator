package com.meriban.barcodevalidator.controllers;

import com.meriban.barcodevalidator.managers.PropertiesManager;
import com.meriban.barcodevalidator.navigators.WindowManager;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputDialog;

import java.util.Optional;

/**
 * FXML Controller for the Validation Rules Delimiter settings sub-scene.
 *
 * @author meriban
 */
public class SettingsValidationDelimiterFXMLController {

    @FXML
    TextField delimiterTextField;
    /**
     * Carries out post-loading setup. This method is called after the controller has been initialised and all control
     * variables injected.
     * <p>Populates the {@code TextField} with the current validation rules delimiter.</p>
     */
    @FXML
    public void initialize(){
        delimiterTextField.setEditable(false);
        delimiterTextField.setText(PropertiesManager.getInstance().getProperty("regex_delimiter"));
    }

    @FXML
    private void handleOnActionEditDelimiterButton(Event e){
        TextInputDialog dialog = new TextInputDialog();
        dialog.initOwner(WindowManager.getStage(SettingsFXMLController.ID)); //this ensures the icon is inherited
        dialog.setHeaderText(null); //remove header nonsense
        dialog.setGraphic(null); //remove header graphic
        dialog.setTitle(PropertiesManager.getInstance().getLanguageBundle().getString("EDIT_VALIDATION_DELIMITER_TITLE"));
        dialog.getEditor().setText(PropertiesManager.getInstance().getProperty("regex_delimiter"));
        Optional<String> result = dialog.showAndWait();
        result.ifPresent(newDelimiter -> {
            delimiterTextField.setText(newDelimiter);
            PropertiesManager.getInstance().updateApplicationProperty("regex_delimiter",newDelimiter);
        });
    }
}
