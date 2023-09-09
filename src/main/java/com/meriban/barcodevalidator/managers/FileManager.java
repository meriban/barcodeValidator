package com.meriban.barcodevalidator.managers;

import java.io.File;
import java.nio.file.Paths;
import java.util.HashMap;

public class FileManager {
    private static FileManager fileManager;
    private static File dataDirectory;
    private static File backupDirectory;
    private static String database;
    private static String backupDatabase;

    private FileManager() {
        setup();
    }

    public static FileManager getInstance() {
        if (fileManager == null) {
            fileManager = new FileManager();
        }
        return fileManager;
    }

    private void setup() {
        if (PropertiesManager.getInstance().getProperty("data_directory").isEmpty()) {
            useDefaultDataDirectory();
        } else {
            //todo implement custom data directory
        }
        if (PropertiesManager.getInstance().getProperty("backup_directory").isEmpty()) {
            useDefaultBackupDirectory();
        }else{
            //todo implement custom backup directory
        }
        setDataBase(PropertiesManager.getInstance().getProperty("database_name"));
        setBackupDatabase(PropertiesManager.getInstance().getProperty("backup_database_name"));
    }

    private static void useDefaultDataDirectory() {
        String currentWorkingDir = Paths.get("").toAbsolutePath().normalize().toString();
        File dataDir = new File(currentWorkingDir, "/data");
        if (dataDir.exists()) {
            setDataDirectory(dataDir);
        } else {
            if (dataDir.mkdir()) {
                setDataDirectory(dataDir);
                System.out.println("DATA DIRECTORY " + dataDir.getAbsolutePath() + " created");
            } else {
                System.out.println("DATA DIRECTORY could not be created");
            }
        }
    }

    private static void useDefaultBackupDirectory() {
        String currentWorkingDir = Paths.get("").toAbsolutePath().normalize().toString();
        File backupDir = new File(currentWorkingDir, "/backup");
        if (backupDir.exists()) {
            setBackupDirectory(backupDir);
        } else {
            if (backupDir.mkdir()) {
                setBackupDirectory(backupDir);
                System.out.println("BACKUP DIRECTORY " + backupDir.getAbsolutePath() + " created");
            } else {
                System.out.println("BACKUP DIRECTORY could not be created");
            }
        }
    }

    private static void setDataDirectory(File dataDirIn) {
        dataDirectory = dataDirIn;
    }

    private static void setBackupDirectory(File backDirIn) {
        backupDirectory = backDirIn;
    }
    private static void setDataBase(String databaseName){
        database=databaseName;
    }
    private static void setBackupDatabase(String databaseName){
        backupDatabase=databaseName;
    }

    public File getDataDirectory() {
        return dataDirectory;
    }
    public File getBackupDirectory(){
        return backupDirectory;
    }
    public String getDatabaseName(){
        return database;
    }
    public String getBackupDatabaseName(){
        return backupDatabase;
    }
    public HashMap<String, File> getSaveLocations(){
            HashMap<String, File> saveLocations = new HashMap<>();
            saveLocations.put(database, dataDirectory);
            saveLocations.put(backupDatabase, backupDirectory);
            return saveLocations;
    }
}
