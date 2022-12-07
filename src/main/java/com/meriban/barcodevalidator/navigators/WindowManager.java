package com.meriban.barcodevalidator.navigators;

import com.meriban.barcodevalidator.controllers.MainFXMLController;
import com.meriban.barcodevalidator.managers.PropertiesManager;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

import java.io.IOException;
import java.util.HashMap;
import java.util.Objects;
import java.util.ResourceBundle;

/**
 * Manages application's windows, keeping tabs on which windows are currently open and bringing them to the foreground
 * or closing them as needed.
 * If the application's main window is closed all other windows that might be open at the time are closed as well (see
 * {@link #deregisterStage(String)}.
 *
 * @author meriban
 */
public class WindowManager {
    /**
     * Holds references to windows currently shown
     */
    //static as it belong to the class, not an instance of it; final as it doesn't need to be reassigned at any point,
    //but it remains mutable.
    private static final HashMap<String, Stage> registeredStages = new HashMap<>();
    private static int unnamedWindowCount = 0;
    private static ResourceBundle languageBundle = PropertiesManager.getInstance().getLanguageBundle();

    /**
     * Should not be used as class is not intended to be instantiated.
     */
    //If one doesn't give this a default no argument constructor is provided by Object class effectively allowing
    //instantiation.
    private WindowManager() {
    }

    /**
     * Registers a window with the {@code WindowManager} in the {@link #registeredStages} {@code HashMap}. The key is
     * derived from the user data associated with the window's stage ({@link Stage#setUserData(Object)}. If the stage
     * does not have any user data associated with it the key is set to "Unnamed Window [number]", where number is the
     * current value of {@link #unnamedWindowCount} and this is also provided as user data for the stage.
     *
     * @param stage the window to be registered
     */
    public static void registerStage(Stage stage) {
        String id = (String) stage.getUserData();
        if (id == null || id.isEmpty()) {
            id = "Unnamed Window " + unnamedWindowCount;
            unnamedWindowCount++;
            stage.setUserData(id); //so stage can be identified if e.g. being passed for de-registration by ID
        }
        stage.setOnCloseRequest(new EventHandler<WindowEvent>() {
            @Override
            public void handle(WindowEvent windowEvent) {
                deregisterStage(stage);
            }
        });
        registeredStages.put(id, stage);
        System.out.println(stage.getUserData() + " registered");
    }

    /**
     * Removes a window from the {@code WindowManager}'s {@link #registeredStages} {@code HashMap}, provided an entry
     * with the parameter's user data ({@link Stage#setUserData(Object)}) as key exists.
     *
     * @param stage the window to be deregistered
     */
    public static void deregisterStage(Stage stage) {
        deregisterStage((String) stage.getUserData());
    }

    /**
     * Removes a window from the {@code WindowManager}'s {@link #registeredStages} {@code HashMap}, provided an entry
     * with the parameter as key exists. This method should only be used if the respective window's user data value
     * ({@link Stage#setUserData(Object)}) is already known; else use {@link #deregisterStage(Stage)}.
     *
     * @param id the window's user data value previously used to register it
     */
    public static void deregisterStage(String id) {
        if (registeredStages.remove(id) != null) {
            System.out.println(id + " deregistered");
        }
        //close all if main window is closed
        if (Objects.equals(id, MainFXMLController.ID)) {
            for (Stage stage : registeredStages.values()) {
                deregisterStage(stage);
                stage.close();
            }
        }
    }

    /**
     * Checks if a {@code Stage} with this id is registered with the {@code WindowManager}.
     *
     * @param id the id
     * @return {@code true} if a {@code Stage} with this id is registered, {@code false} if not
     */
    public static boolean isRegistered(String id) {
        Stage s = registeredStages.get(id);
        return s != null;
    }

    /**
     * Shows the window with the given id. If the window ids already registered and thus has a {@code Stage} instance
     * associated with it, this {@code Stage} will be shown. Else the .fxml file for the scene will be loaded, a new
     * {@code Stage} object created and scene set, window title and icon assigned, the {@code Stage} registered and
     * shown.
     * <p>The .fxml file names should have the pattern [id]Scene.fxml and each file's controller file should have a
     * constant with the value of the [id].</p>
     * <p>Window title properties should have the pattern [ID]_NAME, with the id being in all caps. </p>
     * <p>The icon used is the default application icon.</p>
     *
     * @param id the id of the {@code Stage} to show.
     */
    public static void showStage(String id) {
        if (isRegistered(id)) {
            Stage stage = getStage(id);
            forceShowStage(stage);
        } else {
            String fxmlFile = "/fxmlValidator/" + id + "Scene.fxml";
            Parent root;
            try {
                root = FXMLLoader.load(WindowManager.class.getResource(fxmlFile), languageBundle);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            Scene scene = new Scene(root);
            Stage newStage = new Stage();
            newStage.setUserData(id);
            newStage.setScene(scene);
            newStage.setTitle(languageBundle.getString(id.toUpperCase() + "_NAME"));
            newStage.getIcons().add(new javafx.scene.image.Image(WindowManager.class.getResourceAsStream("/iconsValidator/Utilities.png")));
            registerStage(newStage);
            newStage.show();
        }
    }

    /**
     * Pushes the given {@code Stage} into the foreground, maximises it should it be minimised, and gives it the
     * focus.
     *
     * @param stage the {@code Stage} to push into foreground
     */
    public static void forceShowStage(Stage stage) {
        stage.show();
        stage.setIconified(false);
        stage.toFront();
        stage.requestFocus();
    }

    /**
     * Gets the {@code Stage} with this id from {@link #registeredStages}.
     *
     * @param ID the id
     * @return the {@code Stage}, {@code null} if no {@code Stage} with this id is registered with the {@code
     * WindowManager}.
     */
    public static Stage getStage(String ID) {
        return registeredStages.get(ID);
    }

}
