package com.meriban.barcodevalidator.controllers;

import com.meriban.barcodevalidator.BarcodeValidator;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.KeyEvent;

public class BarcodeValidatorFXMLController {

    @FXML
    private TextField shlBarcodeAddTextField;
    @FXML
    private TextField shlBarcodeRemoveTextField;
    @FXML
    private TextField shlBarcodeRefTextField;
    @FXML
    private Label invalidShlAddLabel;
    @FXML
    private Label invalidShlRemoveLabel;
    @FXML
    private Label invalidShlRefLabel;
    @FXML
    private TextArea barcodeValidatorAddTextArea;
    @FXML
    private TextArea barcodeValidatorRemoveTextArea;
    @FXML
    private TextArea barcodeValidatorRefTextArea;
    @FXML
    private Tab removeTab;
    @FXML
    private Tab addTab;
    @FXML
    private Tab refTab;
    @FXML
    private TabPane tabPane;


    BarcodeValidator barcodeValidator;

    /**
     * Carries out post-loading setup. This method is called after the
     * controller has been initialised and all control variables injected.
     */
    @FXML
    public void initialize() {
        barcodeValidator = BarcodeValidator.getInstance();
        Platform.runLater(() -> shlBarcodeAddTextField.requestFocus());
    }

    @FXML
    private void handleShlAddTextFieldEnterInput(ActionEvent event){
        //System.out.println(shlBarcodeAddTextField.getText());
        if(barcodeValidator.setShlInput(shlBarcodeAddTextField.getText(), BarcodeValidator.ADD_ACTION)){
            invalidShlAddLabel.setVisible(false);
            if(barcodeValidator.onWritten()){
//                System.out.println("WRITTEN!!!");
                displayAddInput();
            }


        }else{
            invalidShlAddLabel.setVisible(true);
            shlBarcodeAddTextField.clear();
        }
    }
    @FXML
    private void handleShlRemoveTextFieldEnterInput(ActionEvent event){
        //System.out.println(shlBarcodeRemoveTextField.getText());
        if(barcodeValidator.setShlInput(shlBarcodeRemoveTextField.getText(), BarcodeValidator.REMOVE_ACTION)){
            invalidShlRemoveLabel.setVisible(false);
            if(barcodeValidator.onWritten()){
                displayRemoveInput();
            }


        }else{
            invalidShlRemoveLabel.setVisible(true);
            shlBarcodeRemoveTextField.clear();
        }
    }

    @FXML
    private void handleShlRefTextFieldEnterInput (ActionEvent event){
        if(barcodeValidator.setShlInput(shlBarcodeRefTextField.getText(), BarcodeValidator.REF_ACTION)){
            invalidShlRefLabel.setVisible(false);
            if(barcodeValidator.onWritten()){
                displayRefInput();
            }


        }else{
            invalidShlRefLabel.setVisible(true);
            shlBarcodeRefTextField.clear();
        }
    }

    private void displayAddInput(){
        barcodeValidatorAddTextArea.appendText(shlBarcodeAddTextField.getText()+ "\n");
        shlBarcodeAddTextField.clear();
    }

    private void displayRemoveInput() {
        barcodeValidatorRemoveTextArea.appendText(shlBarcodeRemoveTextField.getText() + "\n");
        shlBarcodeRemoveTextField.clear();
    }

    private void displayRefInput() {
        barcodeValidatorRefTextArea.appendText(shlBarcodeRefTextField.getText() + "\n");
        shlBarcodeRefTextField.clear();
    }
    @FXML
    private void handleAddTabChange(Event event){
        addTab.setStyle("-fx-text-base-color: mediumseagreen;" + "-fx-font-weight: bold;");
        if(removeTab!=null){
            removeTab.setStyle("-fx-text-base-color: black;" + "-fx-font-weight: regular;");
        }
        if(refTab!=null){
            refTab.setStyle("-fx-text-base-color: black;" + "-fx-font-weight: regular;");
        }
        Platform.runLater(() -> shlBarcodeAddTextField.requestFocus());
    }

    @FXML
    private void handleRemoveTabChange(Event event) {
        removeTab.setStyle("-fx-text-base-color: mediumseagreen;" + "-fx-font-weight: bold;");
        addTab.setStyle("-fx-text-base-color: black;" +  "-fx-font-weight: regular;");
        refTab.setStyle("-fx-text-base-color: black;" +  "-fx-font-weight: regular;");
        Platform.runLater(() -> shlBarcodeRemoveTextField.requestFocus());
    }

    @FXML
    private void handleRefTabChange(Event event){
        refTab.setStyle("-fx-text-base-color: mediumseagreen;" + "-fx-font-weight: bold;");
        addTab.setStyle("-fx-text-base-color: black;" +  "-fx-font-weight: regular;");
        removeTab.setStyle("-fx-text-base-color: black;" +  "-fx-font-weight: regular;");
        Platform.runLater(() -> shlBarcodeRefTextField.requestFocus());

    }

    @FXML
    private void handleFKeyTyped(KeyEvent event){
        String key = event.getCode().getName();
        switch(key){
            case "F1":
                tabPane.getSelectionModel().select(addTab);
                break;
            case "F2":
                tabPane.getSelectionModel().select(removeTab);
                break;
            case "F3":
                tabPane.getSelectionModel().select(refTab);
                break;
        }
    }

}
