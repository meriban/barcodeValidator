package com.meriban.barcodevalidator.controllers;

import com.meriban.barcodevalidator.managers.PropertiesManager;
import com.meriban.barcodevalidator.navigators.WindowManager;
import javafx.beans.binding.Bindings;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import java.util.ArrayList;
import java.util.Arrays;

public class SettingsValidationRulesFXMLController {

    @FXML
    ListView<String> regexListView;
    @FXML
    //ListView rows are by default 24px high
    private final int ROW_HEIGHT = 24;
    ObservableList<String> regsObservable = null;
    PropertiesManager propertiesManager;

@FXML
    public void initialize(){
    propertiesManager = PropertiesManager.getInstance();
    String delimiter = propertiesManager.getProperty("regex_delimiter");
    String regString = propertiesManager.getProperty("regex");
    ArrayList<String> regs = new ArrayList<>(Arrays.asList(regString.split(delimiter)));
    regsObservable = FXCollections.observableArrayList(regs);
    regexListView.setItems(regsObservable);
    regexListView.prefHeightProperty().bind(Bindings.size(regsObservable).multiply(ROW_HEIGHT));
}
@FXML
    private void handleOnActionEditRuleButton(Event e){
    int selectedIndex = regexListView.getSelectionModel().getSelectedIndex(); //returns -1 if nothing is selected
    if(selectedIndex>=0){
        String currentRegEx = regexListView.getSelectionModel().getSelectedItem();
        TextInputDialog dialog = new TextInputDialog();
        dialog.initOwner(WindowManager.getStage(SettingsFXMLController.ID)); //this ensures the icon is inherited
        dialog.setHeaderText(null); //remove header nonsense
        dialog.setGraphic(null); //remove header graphic
        dialog.setTitle(propertiesManager.getLanguageBundle().getString("EDIT_VALIDATION_RULE_DIALOG_TITLE"));
        dialog.getEditor().setText(currentRegEx);
        Button okButton = (Button) dialog.getDialogPane().lookupButton(ButtonType.OK);
        okButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                String newRegEx = dialog.getEditor().getText();
                regsObservable.set(regsObservable.indexOf(currentRegEx),newRegEx);
                regexListView.refresh();
                propertiesManager.updateDelimitedApplicationProperty("regex",regsObservable);
            }
        });
        dialog.show();
    }
}
@FXML
    private void handleOnActionAddValidationRuleButton(Event e){
    TextInputDialog dialog = new TextInputDialog();
    dialog.initOwner(WindowManager.getStage(SettingsFXMLController.ID)); //this ensures the icon is inherited
    dialog.setHeaderText(null); //remove header nonsense
    dialog.setGraphic(null); //remove header graphic
    dialog.setTitle(propertiesManager.getLanguageBundle().getString("ADD_VALIDATION_RULE_DIALOG_TITLE"));
    dialog.getEditor().setPromptText(propertiesManager.getLanguageBundle().getString("ADD_VALIDATION_RULE_PROMPT"));
    Button okButton = (Button) dialog.getDialogPane().lookupButton(ButtonType.OK);
    okButton.setOnAction(new EventHandler<ActionEvent>() {
        @Override
        public void handle(ActionEvent actionEvent) {
            String newRegEx = dialog.getEditor().getText();
            if(regsObservable.contains(newRegEx)){
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.initOwner(WindowManager.getStage(SettingsFXMLController.ID));
                alert.setHeaderText(null);
                alert.setContentText(propertiesManager.getLanguageBundle().getString("ADD_VALIDATION_ALREADY_EXISTS_ERROR"));
                alert.show();
            }else{
                regsObservable.add(newRegEx);
                regexListView.refresh();
                propertiesManager.updateDelimitedApplicationProperty("regex",regsObservable);
            }
        }
    });
    dialog.show();
}

@FXML
    private void handleOnActionDeleteValidationRuleButton(Event e){
    int selectedIndex = regexListView.getSelectionModel().getSelectedIndex(); //returns -1 if nothing is selected
    if(selectedIndex>=0) {
        String currentRegEx = regexListView.getSelectionModel().getSelectedItem();
        Alert dialog = new Alert(Alert.AlertType.CONFIRMATION);
        dialog.initOwner(WindowManager.getStage(SettingsFXMLController.ID)); //this ensures the icon is inherited
        dialog.setHeaderText(null); //remove header nonsense
        dialog.setTitle(propertiesManager.getLanguageBundle().getString("DELETE_VALIDATION_RULE_DIALOG_TITLE"));
        dialog.setContentText(propertiesManager.getLanguageBundle().getString("DELETE_VALIDATION_RULE_DIALOG_TEXT")+"\n"+currentRegEx);
        Button okButton = (Button) dialog.getDialogPane().lookupButton(ButtonType.OK);
        okButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                regsObservable.remove(selectedIndex);
                regexListView.refresh();
                propertiesManager.updateDelimitedApplicationProperty("regex",regsObservable);
            }
        });
        dialog.show();
    }
}
@FXML
    private void handleOnActionClearAllRulesButton(Event e){
    Alert dialog = new Alert(Alert.AlertType.CONFIRMATION);
    dialog.initOwner(WindowManager.getStage(SettingsFXMLController.ID)); //this ensures the icon is inherited
    dialog.setHeaderText(null); //remove header nonsense
    dialog.setGraphic(new ImageView(new Image(getClass().getResourceAsStream("/iconsValidator/exclamation.png"))));
    dialog.setTitle(propertiesManager.getLanguageBundle().getString("CLEAR_VALIDATION_RULES_DIALOG_TITLE"));
    dialog.setContentText(propertiesManager.getLanguageBundle().getString("CLEAR_VALIDATION_RULES_DIALOG_TEXT"));
    Button okButton = (Button) dialog.getDialogPane().lookupButton(ButtonType.OK);
    okButton.setOnAction(new EventHandler<ActionEvent>() {
        @Override
        public void handle(ActionEvent actionEvent) {
            regsObservable.clear();
            regexListView.refresh();
            propertiesManager.updateDelimitedApplicationProperty("regex",regsObservable);
        }
    });
    dialog.show();
}
}
