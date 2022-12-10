package com.meriban.barcodevalidator.controllers;

import com.meriban.barcodevalidator.managers.PropertiesManager;
import com.meriban.barcodevalidator.navigators.WindowManager;
import javafx.beans.binding.Bindings;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ListView;
import javafx.scene.control.TextInputDialog;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Window;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;

/**
 * FXML Controller for the Validation Rules settings sub-scene.
 *
 * @author meriban
 */
public class SettingsValidationRulesFXMLController {

    @FXML
    ListView<String> regexListView;
    ObservableList<String> regsObservable = null;

    /**
     * Carries out post-loading setup. This method is called after the controller has been initialised and all control
     * variables injected.
     * <p>Retrieves the current validation rules from the application properties and populates the {@code ListView}
     * with them.</p>
     */
    @FXML
    public void initialize() {
        String delimiter = PropertiesManager.getInstance().getProperty("regex_delimiter");
        String regString = PropertiesManager.getInstance().getProperty("regex");
        ArrayList<String> regs = new ArrayList<>(Arrays.asList(regString.split(delimiter)));
        regsObservable = FXCollections.observableArrayList(regs);
        regexListView.setItems(regsObservable);
        //This works to set the size of the ListView to be adaptable to the number of runs displayed; ListView rows are
        // by default 24px high
        final int ROW_HEIGHT = 24;
        regexListView.prefHeightProperty().bind(Bindings.size(regsObservable).multiply(ROW_HEIGHT));
    }

    @FXML
    private void handleOnActionEditRuleButton(Event e) {
        int selectedIndex = regexListView.getSelectionModel().getSelectedIndex(); //returns -1 if nothing is selected
        if (selectedIndex >= 0) {
            String currentRegEx = regexListView.getSelectionModel().getSelectedItem();
            TextInputDialog dialog = makeTextInputDialog(WindowManager.getStage(SettingsFXMLController.ID),
                    null,
                    null,
                    PropertiesManager.getInstance().getLanguageBundle().getString("EDIT_VALIDATION_RULE_DIALOG_TITLE"));
            dialog.getEditor().setText(currentRegEx);
            Optional<String> input = dialog.showAndWait();
            input.ifPresent(newRegEx -> {
                regsObservable.set(regsObservable.indexOf(currentRegEx), newRegEx);
                regexListView.refresh();
                PropertiesManager.getInstance().updateDelimitedApplicationProperty("regex", regsObservable);
            });

        }
    }

    @FXML
    private void handleOnActionAddValidationRuleButton(Event e) {
        TextInputDialog dialog = makeTextInputDialog(WindowManager.getStage(SettingsFXMLController.ID),
                null,
                null,
                PropertiesManager.getInstance().getLanguageBundle().getString("ADD_VALIDATION_RULE_DIALOG_TITLE"));
        dialog.getEditor().setPromptText(PropertiesManager.getInstance().getLanguageBundle().getString("ADD_VALIDATION_RULE_PROMPT"));
        Optional<String> input = dialog.showAndWait();
        input.ifPresent(newRegEx -> {
            if (regsObservable.contains(newRegEx)) {
                Alert alert = makeAlert(Alert.AlertType.ERROR,
                        WindowManager.getStage(SettingsFXMLController.ID),
                        null,
                        null,
                        null,
                        PropertiesManager.getInstance().getLanguageBundle().getString("ADD_VALIDATION_ALREADY_EXISTS_ERROR"));
                alert.showAndWait();
            } else {
                regsObservable.add(newRegEx);
                regexListView.refresh();
                PropertiesManager.getInstance().updateDelimitedApplicationProperty("regex", regsObservable);
            }
        });
    }

    @FXML
    private void handleOnActionDeleteValidationRuleButton(Event e) {
        int selectedIndex = regexListView.getSelectionModel().getSelectedIndex(); //returns -1 if nothing is selected
        if (selectedIndex >= 0) {
            String currentRegEx = regexListView.getSelectionModel().getSelectedItem();
            Alert alert = makeAlert(Alert.AlertType.CONFIRMATION,
                    WindowManager.getStage(SettingsFXMLController.ID),
                    null,
                    null,
                    PropertiesManager.getInstance().getLanguageBundle().getString("DELETE_VALIDATION_RULE_DIALOG_TITLE"),
                    PropertiesManager.getInstance().getLanguageBundle().getString("DELETE_VALIDATION_RULE_DIALOG_TEXT") + "\n" + currentRegEx);
            Optional<ButtonType> result = alert.showAndWait();
            result.ifPresent(buttonType -> {
                if (buttonType == ButtonType.OK) {
                    regsObservable.remove(selectedIndex);
                    regexListView.refresh();
                    PropertiesManager.getInstance().updateDelimitedApplicationProperty("regex", regsObservable);
                }
            });
        }
    }

    @FXML
    private void handleOnActionClearAllRulesButton(Event e) {
        Alert alert = makeAlert(Alert.AlertType.CONFIRMATION,
                WindowManager.getStage(SettingsFXMLController.ID),
                null,
                new ImageView(new Image(Objects.requireNonNull(getClass().getResourceAsStream("/iconsValidator/exclamation.png")))),
                PropertiesManager.getInstance().getLanguageBundle().getString("CLEAR_VALIDATION_RULES_DIALOG_TITLE"),
                PropertiesManager.getInstance().getLanguageBundle().getString("CLEAR_VALIDATION_RULES_DIALOG_TEXT")
        );
        Optional<ButtonType> result = alert.showAndWait();
        result.ifPresent(buttonType -> {
            if (buttonType == ButtonType.OK) {
                regsObservable.clear();
                regexListView.refresh();
                PropertiesManager.getInstance().updateDelimitedApplicationProperty("regex", regsObservable);
            }
        });
    }

    /**
     * Configures a {@link TextInputDialog}.
     *
     * @param owner         the owner Window of the dialog (it'll inherit the window icon from this)
     * @param headerText    the header text. To have no header text pass in {@code null}
     * @param headerGraphic the header graphic. To have no header graphic pass in {@code null}
     * @param title         the dialog title
     * @return the configured {@code TextInputDialog}
     */
    private TextInputDialog makeTextInputDialog(Window owner, String headerText, Node headerGraphic, String title) {
        TextInputDialog dialog = new TextInputDialog();
        dialog.initOwner(owner); //this ensures the icon is inherited
        dialog.setHeaderText(headerText); //remove header nonsense
        dialog.setGraphic(headerGraphic); //remove header graphic
        dialog.setTitle(title);
        return dialog;
    }

    /**
     * Configures a {@link Alert}.
     *
     * @param type          the {@link Alert.AlertType}
     * @param owner         the owner Window of the alert (it'll inherit the window icon from this)
     * @param headerText    the header text. To have no header text pass in {@code null}
     * @param headerGraphic the header graphic. To have no header graphic pass in {@code null}
     * @param title         the alert title
     * @param contentText   the alert's content text
     * @return the configured {@code Alert}
     */
    private Alert makeAlert(Alert.AlertType type, Window owner, String headerText, Node headerGraphic, String title, String contentText) {
        Alert alert = new Alert(type);
        alert.initOwner(owner);
        alert.setHeaderText(headerText);
        alert.setGraphic(headerGraphic);
        alert.setContentText(contentText);
        if (title != null) {
            alert.setTitle(title);
        }
        return alert;
    }
}
