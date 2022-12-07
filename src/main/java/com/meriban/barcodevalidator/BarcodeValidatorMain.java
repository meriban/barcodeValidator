package com.meriban.barcodevalidator;

import com.meriban.barcodevalidator.controllers.MainFXMLController;
import com.meriban.barcodevalidator.navigators.WindowManager;
import javafx.application.Application;
import javafx.stage.Stage;

import java.io.File;
import java.nio.file.Paths;

/**
 * Application to validate input data, write it to an SQLite database and generate logs from this.
 */
public class BarcodeValidatorMain extends Application {
    @Override
    public void start(Stage stage) throws Exception {
        createDataDirectory();
        WindowManager.showStage(MainFXMLController.ID);
    }

    /**
     * Creates directories for storing data and backing up data collected by the application. They are in the "bin"
     * folder after jlink
     */
    private void createDataDirectory(){
        String currentWorkingDir = Paths.get("").toAbsolutePath().normalize().toString();
        File dataDir = new File(currentWorkingDir, "/data");
        File backupDir = new File (currentWorkingDir, "/backup");
        if(dataDir.exists()){
            BarcodeValidator.setDataDirDB(dataDir);
        }else{
            if(dataDir.mkdir()){
                BarcodeValidator.setDataDirDB(dataDir);
                System.out.println("DATA DIRECTORY "+dataDir.getAbsolutePath() + " created");
            }else{
                System.out.println("DATA DIRECTORY could not be created");
            }
        }
        if(backupDir.exists()){
            BarcodeValidator.setBackupDirDB(backupDir);
        }else{
            if(backupDir.mkdir()){
                BarcodeValidator.setBackupDirDB(backupDir);
                System.out.println("BACKUP DIRECTORY " + backupDir.getAbsolutePath()+ " created");
            }else{
                System.out.println("BACKUP DIRECTORY could not be created");
            }
        }
    }
    //works! just no good for deploy at work at the moment
    //TODO integrate with createDataDirectory()
    private void createDataDirectoryAppData() {
        File userDir = new File(System.getProperty("user.home") + "/AppData/Local");
        File appDir = new File(userDir, "/BV");
        if (!appDir.exists()) {
            if (!appDir.mkdir()) {
                System.out.println("APP DIRECTORY could not be created ");
            }
        }
        File dataDir = new File(appDir, "/data");
        File backupDir = new File(appDir, "/back");
        if (dataDir.exists()) {
            BarcodeValidator.setDataDirDB(dataDir);
        } else {
            if (dataDir.mkdir()) {
                BarcodeValidator.setDataDirDB(dataDir);
                System.out.println("DATA DIRECTORY "+dataDir.getAbsolutePath() + " created");
            } else {
                System.out.println("DATA DIRECTORY could not be created");
            }
        }
        if (backupDir.exists()) {
            BarcodeValidator.setBackupDirDB(backupDir);
        } else {
            if (backupDir.mkdir()) {
                BarcodeValidator.setBackupDirDB(backupDir);
                System.out.println("BACKUP DIRECTORY " + backupDir.getAbsolutePath()+ " created");
            } else {
                System.out.println("BACKUP DIRECTORY could not be created");
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
