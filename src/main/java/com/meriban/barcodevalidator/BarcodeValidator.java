package com.meriban.barcodevalidator;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class BarcodeValidator {

    // static variable single_instance of type Singleton
    private static BarcodeValidator barcodeValidator = null;

    public static final int ADD_ACTION = 1;
    public static final int REMOVE_ACTION = -1;
    public static final int REF_ACTION = 2;

    private String shlBarcode = null;
    private boolean written = false;
    private static File file = null;
    private static File logFile= null;
    private static LocalDate date;
    private int action =0;

       // private constructor restricted to this class itself
    private BarcodeValidator(){

    }

    // static method to create instance of Singleton class
    public static BarcodeValidator getInstance()
    {
        if (barcodeValidator == null)
            barcodeValidator = new BarcodeValidator();
        return barcodeValidator;
    }
    //VALIDATORS
    private boolean validateShlInput(String input){
        Pattern pattern10 = Pattern.compile("^1[xX0-9]{9}$");
        Pattern pattern14 = Pattern.compile("^30200[0-9]{9}$");
        Matcher matcher10 = pattern10.matcher(input);
        Matcher matcher14 = pattern14.matcher(input);
        return matcher10.matches() || matcher14.matches();
    }

    //BARCODE GETTERS AND SETTERS
    public void setShlBarcode(String barcode){
        shlBarcode=barcode;
        onDataComplete();
    }
    //INPUT GETTERS AND SETTERS
    public boolean setShlInput(String input, int actionIn){
        action = actionIn;
        if(validateShlInput(input)){
            setShlBarcode(input);
            return true;
        }
        return false;
    }

    private void onDataComplete(){
        if(shlBarcode!=null){
            writeBarcodes(shlBarcode);
        }
    }

    public static void setDataDir(File dataDirIn){
        file = new File (dataDirIn, "/dataLog.txt");
        date = LocalDate.now();
    }

    public static void setBackupDir(File backUpDirIn){
        logFile = new File (backUpDirIn, "/logB.txt");
    }
    public boolean onWritten(){
        if(written){
            resetValidator();
            return true;
        }
        return false;
    }
    private void resetValidator(){
        shlBarcode=null;
        written = false;
        action =0;
    }

    private void writeBarcodes(String s1){
        if(action==0){
            writeError(s1);
            return;
        }
        try{
            FileWriter fileWriter = new FileWriter(file, true);
            fileWriter.write(s1 + "," + date + "," + action + "\n");
            fileWriter.close();
            written =true;
        } catch (IOException e) {
            e.printStackTrace();
        }
        try{
            FileWriter fileWriter = new FileWriter(logFile, true);
            fileWriter.write(s1 + "," + date + "," + action + "\n");
            fileWriter.close();
            written =true;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void writeError(String s){
        try{
            FileWriter fileWriter = new FileWriter(logFile, true);
            fileWriter.write(s + "," + date + ",could not be written as no action was communicated from GUI." + "\n");
        fileWriter.close();
        }catch (IOException e){
            e.printStackTrace();
        }
    }
}
