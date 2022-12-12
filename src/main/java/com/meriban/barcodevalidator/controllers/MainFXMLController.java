package com.meriban.barcodevalidator.controllers;

import com.meriban.barcodevalidator.Action;
import com.meriban.barcodevalidator.DatabaseHandler;
import com.meriban.barcodevalidator.Validator;
import com.meriban.barcodevalidator.managers.FileManager;
import com.meriban.barcodevalidator.navigators.WindowManager;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyEvent;

import java.util.Objects;

/**
 * FXML Controller for the application's main window scene.
 *
 * @author meriban
 */
public class MainFXMLController {
    /**
     * This is this {@code Scene}'s id. It is used to register or deregister it with the {@link WindowManager} and to
     * load the .fxml file for it. For this to work the name of the .fxml file this class controls must be in the format
     * {@code idScene.fxml}. A window title property must be created in the language properties and must be in the
     * format {@code ID_NAME}, with the value of ID being in all caps.
     */
    public static final String ID = "main";

    @FXML
    private TextField shlBarcodeAddTextField, shlBarcodeRemoveTextField, shlBarcodeRefTextField, shlBarcodeLoanTextField;
    @FXML
    private Tab removeTab, addTab, refTab, loanTab;
    @FXML
    private TabPane tabPane;

    /**
     * Carries out post-loading setup. This method is called after the
     * controller has been initialised and all control variables injected.
     * <p>Associates tab {@code TextField}s with an {@link Action} using user data.</p>
     */
    @FXML
    public void initialize() {
        shlBarcodeAddTextField.setUserData(Action.ADD);
        shlBarcodeRemoveTextField.setUserData(Action.REMOVE);
        shlBarcodeRefTextField.setUserData(Action.TO_LOAN);
        shlBarcodeLoanTextField.setUserData(Action.TO_REF);
        Platform.runLater(() -> shlBarcodeAddTextField.requestFocus());
    }

    /**
     * Handles input into any of this scene's {@code TextField}'s.
     * <p>The input is passed into validation. If it passes
     * validation a method to attempt writing the input and its associated action (see {@link #initialize()} into the
     * SQLite database is called and if this is also successful the {@link #displayInput(TextField)} method is triggered.
     * If validation fails a {@code Label} is made visible alerting the user that the input was invalid and the
     * {@code TextField}'s content cleared. Finally the {@code TextField} is cleared.</p>
     * <p>Each {@code TextField} has been given an ID using its tab's associated F key and "Field", e.g. "F1Field".
     * The "F?" part is used to locate both the {@code Label} (IDs have pattern "F?Label") and {@code TextArea} (IDs
     * have pattern "F?Display") in the same tab as the {@code TextField} so they can be made visible or the input
     * displayed in them. The same pattern is applied to the respective tabs holding them so a link can be made there
     * as well (pattern "F?Tab").</p>
     *
     * @param event the input event
     */
    @FXML
    private void handleTextFieldInput(ActionEvent event) {
        TextField target = (TextField) event.getTarget();
        Action action = (Action) target.getUserData();
        String targetID = target.getId(); //get the TextField's ID (pattern F1TextField)
        String labelID = "#" + targetID.substring(0, 2) + "Label"; //prefix # need for lookup() ; F? + Label = node ID of label in this tab
        Node label = target.getParent().lookup(labelID); //retrieve label by its ID
        if (Validator.validateInput(Objects.requireNonNull(target.getText()))) {
            label.setVisible(false);
            if (DatabaseHandler.writeToDatabases(FileManager.getInstance().getSaveLocations(), "LOG",target.getText(),action)) {
                displayInput(target);
            }
        } else {
            label.setVisible(true);

        }
        target.clear();
    }

    /**
     * Handles display of successfully validated and written input in respective tab's {@code TextArea}.
     * <p>The link between {@code TextField} and {@code TextArea} is made via their node IDs (see
     * {@link #handleTextFieldInput(ActionEvent)}</p>
     *
     * @param target the {@code TextField} receiving the input
     */
    private void displayInput(TextField target) {
        String targetID = target.getId(); //get the TextField's ID (pattern F1TextField)
        String textAreaID = "#" + targetID.substring(0, 2) + "Display"; //prefix # need for lookup() ; F? + Display = node ID of TextArea in this tab
        Node textAreaNode = target.getParent().getParent().lookup(textAreaID); //need to go two containers up to get branch that contains the TextArea
        TextArea textArea = (TextArea) textAreaNode;
        textArea.appendText(target.getText() + "\n");
    }

    /**
     * Handles the font change in a {@code Tab}'s text and requests focus for the selected tab's {@code TextField}
     * when a tab change occurs.
     * <p>The link between {@code Tab} and {@code TextField} is made via their node IDs (see
     * {@link #handleTextFieldInput(ActionEvent)}</p>
     *
     * @param event the tab change event
     */
    @FXML
    private void handleTabChange(Event event) {
        Tab target = (Tab) event.getTarget();
        TabPane pane = target.getTabPane();
        if (pane != null) {
            ObservableList<Tab> tabs = pane.getTabs();
            for (Tab tab : tabs) {
                if (tab.getId() == target.getId()) {
                    target.setStyle("-fx-text-base-color: mediumseagreen;" + "-fx-font-weight: bold;");
                } else {
                    tab.setStyle("-fx-text-base-color: black;" + "-fx-font-weight: regular;");

                }
            }
        }
        String targetID = target.getId(); //get the tab's ID
        String textID = "#" + targetID.substring(0, 2) + "Field"; //prefix # need for lookup() ; F? + Field = node ID of TextField in this tab
        Parent parent = target.getTabPane();
        if (parent != null) {
            Node text = parent.lookup(textID); //retrieve TexTField by its ID
            Platform.runLater(text::requestFocus);
        }
    }

    /**
     * Handles keyboard shortcuts for tab change.
     *
     * @param event the key release event
     */
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

    /**
     * Brings up the window to generate an output file (log) from the data saved in the SQLite database.
     *
     * @param event the button interaction event
     */
    @FXML
    private void handleCreateLogButtonOnAction(Event event) {
        WindowManager.showStage(CreateLogFXMLController.ID);
    }

    /**
     * Brings up the settings window.
     *
     * @param event the button interaction event
     */
    @FXML
    private void handleSettingsButtonOnAction(Event event) {
        WindowManager.showStage(SettingsFXMLController.ID);
    }
}
