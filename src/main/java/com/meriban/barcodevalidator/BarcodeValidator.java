package com.meriban.barcodevalidator;

import com.meriban.barcodevalidator.managers.PropertiesManager;

import java.io.File;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Validates input against a defined set of validation rules passing it to being stored together with its associated
 * action and date and time of entering if the tests are passed.
 * @author meriban
 */
public class BarcodeValidator {

    // static variable single_instance of type Singleton
    private static BarcodeValidator barcodeValidator = null;
    private static DatabaseHandler handler = null;
    public static final int ADD = 1;
    public static final int REMOVE = -1;
    public static final  int TO_LOAN = 2;
    public static final int TO_REF = 3;
    private String barcode = null;
    private boolean validated = false;
    private static File file = null;
    private static File dataDir = null;
    private static File logFile = null;
    private static File backupDir = null;
    private int action = 0;
    private static final ArrayList<Pattern> regex = new ArrayList<>();

    /**
     * Loads the validation rules and
     */
    private BarcodeValidator() {
        parseRegexFromProps();
        handler = DatabaseHandler.getInstance();
    }

    //GET INSTANCE
    // static method to create instance of Singleton class
    public static BarcodeValidator getInstance() {
        if (barcodeValidator == null)
            barcodeValidator = new BarcodeValidator();
        return barcodeValidator;
    }

    //VALIDATOR
    public boolean validateInput(String barcodeIn, int actionIn) {
        if (validateBarcode(barcodeIn)) {
            barcode = barcodeIn;
            if (validateAction(actionIn)) {
                action = actionIn;
                validated = true;
                return true;
            }
        }
        resetValidator();
        return false;
    }

    private boolean validateBarcode(String input) {
        if (input != null) {
            for (Pattern pattern : regex) {
                Matcher matcher = pattern.matcher(input);
                if (matcher.matches()) {
                    return true;
                }
            }
        }
        return false;
    }

    //TODO this could be more specific, e.g. fish out nulls and invalid actions
    private boolean validateAction(int action) {
        if (action == 0) {
            System.out.println(barcode + " cannot pass validation as no action was communicated from GUI.");
            return false;
        }
        return true;
    }

    @Deprecated
    public static void setDataDir(File dataDirIn) {
        file = new File(dataDirIn, "/dataLog.txt");
    }

    public static void setDataDirDB(File dataDirIn) {
        dataDir = dataDirIn;
    }
    public File getDataDir(){
        return dataDir;
    }

    @Deprecated
    public static void setBackupDir(File backUpDirIn) {
        logFile = new File(backUpDirIn, "/logB.txt");
    }

    public static void setBackupDirDB(File backupDirIn) {
        backupDir = backupDirIn;
    }
    public boolean writeToDatabases() {
        if (validated) {
            if(writeToDB(dataDir,"testdb.db")){
                if(writeToDB(backupDir, "backupTestdb.db")){
                    resetValidator();
                    return true;
                }
            }
        }
        return false;
    }

    private boolean writeToDB(File directory, String databaseName){
        int success;
        try {
            success = handler.connectToDatabase(directory, databaseName).write(barcode, action);
            handler.closeConnection();
            switch (success) {
                case DatabaseHandler.CONNECTION_CLOSED:
                    System.out.println(barcode + " with action " + action + " could not be written as SQL Connection was closed on write attempt.");
                    return false;
                case DatabaseHandler.NOT_CONNECTED:
                    System.out.println(barcode + " with action " + action + " could not be written as SQL Connection was null on write attempt.");
                    return false;
                case DatabaseHandler.TABLE_NOT_EXIST:
                    System.out.println(barcode + " with action " + action + " could not be written as SQL table does not exist on write attempt.");
                    return false;
                case DatabaseHandler.DUPLICATE:
                    System.out.println(barcode + " with action " + action + " could not be written as there is more than one entry with this barcode in table.");
                    return false;
                case DatabaseHandler.DONE:
                    return true;
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return false;
    }

    private void parseRegexFromProps() {
        PropertiesManager propertiesManager = PropertiesManager.getInstance();
        String delimiter = propertiesManager.getProperty("regex_delimiter");
        String regString = propertiesManager.getProperty("regex");
        String[] regs = regString.split(delimiter);
        for (String reg : regs) {
            Pattern pattern = Pattern.compile(reg);
            regex.add(pattern);
        }
    }

    private void resetValidator() {
        barcode = null;
        validated = false;
        action = 0;
    }

    @Deprecated
    public File getFile() {
        return logFile;
    }
}
