package com.meriban.barcodevalidator.controllers;

import com.meriban.barcodevalidator.BarcodeValidator;
import com.meriban.barcodevalidator.managers.PropertiesManager;
import com.meriban.barcodevalidator.navigators.WindowManager;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.TransferMode;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

public class MainFXMLController {
    public static final String ID = "main";

    @FXML
    private TextField shlBarcodeAddTextField, shlBarcodeRemoveTextField, shlBarcodeRefTextField, shlBarcodeLoanTextField;
    @FXML
    private Tab removeTab, addTab, refTab, loanTab;
    @FXML
    private TabPane tabPane;
    @FXML
    private Button settingsButton;
    @FXML
    private ImageView dragFileImage;
    @FXML
    private ImageView dragImage;

    BarcodeValidator barcodeValidator;
    PropertiesManager propertiesManager;

    /**
     * Carries out post-loading setup. This method is called after the
     * controller has been initialised and all control variables injected.
     */
    @FXML
    public void initialize() {
        barcodeValidator = BarcodeValidator.getInstance();
        propertiesManager = PropertiesManager.getInstance();
        shlBarcodeAddTextField.setUserData(BarcodeValidator.ADD);
        shlBarcodeRemoveTextField.setUserData(BarcodeValidator.REMOVE);
        shlBarcodeRefTextField.setUserData(BarcodeValidator.TO_LOAN);
        shlBarcodeLoanTextField.setUserData(BarcodeValidator.TO_REF);
        Platform.runLater(() -> shlBarcodeAddTextField.requestFocus());
    }

    @FXML
    private void handleTextFieldInput(ActionEvent event) {

        TextField target = (TextField) event.getTarget();
        Integer action = (Integer) target.getUserData();
        String targetID = target.getId();
        String textID = "#"+targetID.substring(0,2) + "Label";
        Node label = target.getParent().lookup(textID);
        if (barcodeValidator.validateInput(target.getText(),action)){
            label.setVisible(false);
            if (barcodeValidator.writeToDatabases()){
                displayInput(target);
            }
        }else{
            label.setVisible(true);
            target.clear();
        }
    }

    private void displayInput(TextField target){
        String targetID = target.getId();
        String textID = "#"+targetID.substring(0,2) + "Display";
        Node textAreaNode = target.getParent().getParent().lookup(textID);
        TextArea textArea = (TextArea) textAreaNode;
        textArea.appendText(target.getText()+"\n");
        target.clear();
    }

    @FXML
    private void handleTabChange(Event event){
        Tab target = (Tab)event.getTarget();
        TabPane pane = target.getTabPane();
        if(pane!=null) {
            ObservableList<Tab> tabs = pane.getTabs();
            for (Tab tab : tabs) {
                if (tab.getId() == target.getId()){
                    target.setStyle("-fx-text-base-color: mediumseagreen;" + "-fx-font-weight: bold;");
                }else{
                    tab.setStyle("-fx-text-base-color: black;" + "-fx-font-weight: regular;");

                }
            }
        }
        String targetID = target.getId();
        String textID = "#"+targetID.substring(0,2) + "Field";
        Parent parent = target.getTabPane();
        if(parent!=null){
            Node text = parent.lookup(textID);
            Platform.runLater(text::requestFocus);
        }
    }

    @FXML
    private void handleFKeyTyped(KeyEvent event) {
        String key = event.getCode().getName();
        switch (key) {
            case "F1":
                tabPane.getSelectionModel().select(addTab);
                break;
            case "F2":
                tabPane.getSelectionModel().select(removeTab);
                break;
            case "F3":
                tabPane.getSelectionModel().select(refTab);
                break;
            case "F4":
                tabPane.getSelectionModel().select(loanTab);
        }
    }

    @FXML
    private void handleCreateLogButtonOnAction(Event event){
        String windowName = CreateLogFXMLController.ID;
        WindowManager.showStage(windowName);
    }
    @FXML
    private void handleSettingsButtonOnAction(Event e){
        String windowName = SettingsFXMLController.ID;
        WindowManager.showStage(windowName);
    }
    @FXML
    private void handleSendLogButtonOnAction(Event event) {
        Desktop desktop;
        File logfile = barcodeValidator.getFile();
        //String path = logfile.get;
        try {
            if (Desktop.isDesktopSupported()
                    && (desktop = Desktop.getDesktop()).isSupported(Desktop.Action.MAIL)) {
                URI mailto = new URI("mailto:fran.frenzel@london.ac.uk?subject=Withdrawal%20log%20file");
                desktop.mail(mailto);
            } else {
                // TODO fallback to some Runtime.exec(..) voodoo?
                throw new RuntimeException("desktop doesn't support mailto; mail is dead anyway ;)");
            }
        } catch (URISyntaxException e) {
            System.out.println(e);
        } catch (IOException e) {
            System.out.println(e);
        }

    }

    @FXML
    private void handleOnFileDragDetected(DragEvent event) {
        System.out.println("DRag detected");
        Dragboard dragboard = dragFileImage.startDragAndDrop(TransferMode.MOVE);
//        ArrayList<File> files = new ArrayList<>();
//        File file = barcodeValidator.getFile();
//        files.add(file);
//        ClipboardContent clipboardContent = new ClipboardContent();
//        clipboardContent.putFiles(files);
//        dragboard.setContent(clipboardContent);
//
//        event.consume();
    }

    @FXML
    private void handeOnFileDragDone(DragEvent event) {
        event.consume();
    }

}
