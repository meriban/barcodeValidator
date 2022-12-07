package com.meriban.barcodevalidator.controllers;

import com.meriban.barcodevalidator.managers.PropertiesManager;
import com.meriban.barcodevalidator.navigators.WindowManager;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputDialog;

public class SettingsValidationDelimiterFXMLController {

    @FXML
    TextField delimiterTextField;
    PropertiesManager propertiesManager;


    @FXML
    public void initialize(){
        propertiesManager = PropertiesManager.getInstance();
        delimiterTextField.setEditable(false);
        delimiterTextField.setText(propertiesManager.getProperty("regex_delimiter"));
    }

    @FXML
    private void handleOnActionEditDelimiterButton(Event e){
        TextInputDialog dialog = new TextInputDialog();
        dialog.initOwner(WindowManager.getStage(SettingsFXMLController.ID)); //this ensures the icon is inherited
        dialog.setHeaderText(null); //remove header nonsense
        dialog.setGraphic(null); //remove header graphic
        dialog.setTitle(PropertiesManager.getInstance().getLanguageBundle().getString("EDIT_VALIDATION_DELIMITER_TITLE"));
        dialog.getEditor().setText(PropertiesManager.getInstance().getProperty("regex_delimiter"));
        Button okButton = (Button) dialog.getDialogPane().lookupButton(ButtonType.OK);
        okButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                String newDelimiter = dialog.getEditor().getText();
                delimiterTextField.setText(newDelimiter);
                propertiesManager.updateApplicationProperty("regex_delimiter",newDelimiter);
            }
        });
        dialog.show();
    }
}
