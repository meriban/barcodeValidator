package com.meriban.barcodevalidator;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.ResourceBundle;

public class BarcodeValidatorMain extends Application {

    private static Properties applicationProperties;

    public static Properties getApplicationProperties() {
        return applicationProperties;
    }

    public static void setApplicationProperties(Properties properties) {
        applicationProperties = properties;
    }

    @Override
    public void start(Stage stage) throws Exception {
        loadPropertiesFile();
        createDataDirectory();
        ResourceBundle languageBundle = ResourceBundle.getBundle("langValidator/" + applicationProperties.getProperty("language"));

        Parent root = FXMLLoader.load(getClass().getResource("/fxmlValidator/barcodeValidatorScene.fxml"), languageBundle);

        Scene scene = new Scene(root);
        //scene.getStylesheets().add("/styles/Styles.css");

        stage.setTitle(languageBundle.getString("APPLICATION_NAME"));
        stage.setScene(scene);
        stage.getIcons().add(new Image(getClass().getResourceAsStream("/iconsValidator/Utilities.png")));
        stage.show();
    }

    private void loadPropertiesFile() throws IOException {
        Properties defaultProps = new Properties();
        InputStream in = getClass().getResourceAsStream("/propsValidator/default.properties");
        defaultProps.load(in);
        in.close();

        // create application properties with default
        applicationProperties = new Properties(defaultProps);

        // now load properties from last invocation
        in = getClass().getResourceAsStream("/propsValidator/last.properties");
        applicationProperties.load(in);
        in.close();
    }

    private void createDataDirectory() {
        File userDir = new File(System.getProperty("user.home") + "/AppData/Local");
        System.out.println(userDir);
        //String currentWorkingDir = Paths.get("").toAbsolutePath().normalize().toString();
        File appDir = new File(userDir, "/BV");
        if (!appDir.exists()) {
            if (appDir.mkdir()) {
                System.out.println(appDir.getAbsolutePath());
            } else {
                System.out.println("APPDIR could not be created ");
            }
        }
        File dataDir = new File(appDir, "/data");
        File backupDir = new File(appDir, "/back");
        if (dataDir.exists()) {
            com.meriban.barcodevalidator.BarcodeValidator.setDataDir(dataDir);
        } else {
            if (dataDir.mkdir()) {
                com.meriban.barcodevalidator.BarcodeValidator.setDataDir(dataDir);
                System.out.println(dataDir.getAbsolutePath());
            } else {
                System.out.println("DATADIR could not be created");
            }
        }
        if (backupDir.exists()) {
            com.meriban.barcodevalidator.BarcodeValidator.setBackupDir(backupDir);
        } else {
            if (backupDir.mkdir()) {
                com.meriban.barcodevalidator.BarcodeValidator.setBackupDir(backupDir);
                System.out.println(backupDir.getAbsolutePath());
            } else {
                System.out.println("BACKUPDIR could not be created");
            }
        }
    }

    /**
     * The main() method is ignored in correctly deployed JavaFX application.
     * main() serves only as fallback in case the application can not be
     * launched through deployment artifacts, e.g., in IDEs with limited FX
     * support. NetBeans ignores main().
     *
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        launch(args);
    }

}
