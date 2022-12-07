package com.meriban.barcodevalidator.managers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.io.*;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.*;

/**
 * Manages reading, writing and formatting of the application's properties. This is a singleton class and should
 * be instantiated using its {@code getInstance()} method.
 * @author meriban
 */
public class PropertiesManager {
    // static variable single_instance of type Singleton
    private static PropertiesManager propertiesManager = null;
    private static Properties applicationProperties = null;

    //private constructor restricted to this class itself
    private PropertiesManager() {
    }

    //static method to create instance of Singleton class
    public static PropertiesManager getInstance() {
        if (propertiesManager == null) {
            propertiesManager = new PropertiesManager();
        }
        loadProperties();
        return propertiesManager;
    }

    /**
     * Loads the hard-coded default properties and modifiable ones from the application's last run.
     */
    private static void loadProperties() {
        Properties defaultProps = new Properties();
        //Can do this with InputStream as the file is part of the .jar when packaged and never written to
        InputStream in = PropertiesManager.class.getResourceAsStream("/propsValidator/default.properties");
        try {
            defaultProps.load(in);
            if (in != null) {
                in.close();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        // create application properties with default
        applicationProperties = new Properties(defaultProps);
        // now load properties from last invocation
        //have to do this using File and FileReader as this file is not part of the .jar and needs to be read from
        //and written to
        File lastProperties = new File("properties/last.properties");
        if(!lastProperties.exists()){
            makePropertiesFile();
        }
        try (Reader reader = new FileReader(lastProperties)) {
            applicationProperties.load(reader);
        } catch (IOException e) {
            throw new RuntimeException();
        }
    }

    /**
     * Creates a properties folder and the modifiable .properties file if it does not exist yet.
     * @return the .properties file created
     */
    //fine to be static as this is not depending on applicationProperties having been loaded.
    private static File makePropertiesFile(){
        File propsFile = null;
        String currentWorkingDir = Paths.get("").toAbsolutePath().normalize().toString();
        File propsDir = new File(currentWorkingDir, "/properties");
        if (propsDir.exists()) {
            propsFile = new File(propsDir, "last.properties");
            if (!propsFile.exists()) {
                try {
                    if (propsFile.createNewFile()) {
                        System.out.println("PROPERTY FILE "+propsFile.getPath() + " created");
                    }
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }
            }
        } else {
            if (propsDir.mkdir()) {
                propsFile = new File(propsDir, "last.properties");
                if (!propsFile.exists()) {
                    try {
                        if (propsDir.createNewFile()) {
                            System.out.println("PROPERTY FILE "+propsFile.getPath() + " created");
                        }
                    } catch (IOException ex) {
                        throw new RuntimeException(ex);
                    }
                }
            } else {
                System.out.println("PROPERTY DIRECTORY could not be created");
            }
        }
        return propsFile;
    }

    /**
     * Retrieves the application's properties. Call this using {@code getInstance()}.
     * @return the application's properties
     */
    //by this not being a static method it has to be called via getInstance() and thus applicationProperties will
    //have always been loaded. It can't be called via PropertiesManager.getApplicationProperties()
    public Properties getApplicationProperties() {
        return applicationProperties;
    }

    /**
     * Gets a particular property value from the application's properties. Call this using {@code getInstance()}.
     * @param property the property to retrieve.
     * @return the value of the respective property.
     */
    //by this not being a static method it has to be called via getInstance() and thus applicationProperties will
    //have always been loaded. It can't be called via PropertiesManager.getProperty()
    public String getProperty(String property){
        return applicationProperties.getProperty(property);
    }

    /**
     * Gets the language bundle for the respective language. Call this using {@code getInstance()}.
     * @return the language bundle
     */
    //by this not being a static method it has to be called via getInstance() and thus applicationProperties will
    //have always been loaded. It can't be called via PropertiesManager.getLanguageBundle()
    public ResourceBundle getLanguageBundle() {
        return ResourceBundle.getBundle("langValidator/" + applicationProperties.getProperty("language"));
    }

    /**
     * Updates the given property to the provided value, which also results in the new value being saved to the
     * modifiable .properties file. Call this using {@code getInstance()}.
     * @param property the property to update
     * @param newValue the new property value
     */
    //by this not being a static method it has to be called via getInstance() and thus applicationProperties will
    //have always been loaded. It can't be called via PropertiesManager.updateApplicationProperty()
    public void updateApplicationProperty(String property, String newValue) {
        if(Objects.equals(property, "regex_delimiter")){
            String newDelimiter = newValue;
            newValue  = reformatDelimitedPropertyValue("regex", applicationProperties.getProperty("regex_delimiter"),newDelimiter);
        }
        applicationProperties.setProperty(property,newValue);
        writeApplicationProperties();
    }
    /**
     * Updates the given property to the provided value list, which also results in the new value being saved to the
     * modifiable .properties file. Call this using {@code getInstance()}.
     * @param property the property to update
     * @param newValues the new property value list
     */
    //todo this relies on only being called in the correct circumstances and then manually filtering for the property, not ideal
    //by this not being a static method it has to be called via getInstance() and thus applicationProperties will
    //have always been loaded. It can't be called via PropertiesManager.updateApplicationProperty()
    public void updateDelimitedApplicationProperty(String property, ObservableList<String> newValues){
        StringBuilder newRegStringBuilder = new StringBuilder();
        if(Objects.equals(property, "regex")){
            for (String regex:newValues){
                if(newValues.indexOf(regex)==0){
                    newRegStringBuilder.append(regex);
                }else{
                    newRegStringBuilder.append(applicationProperties.getProperty("regex_delimiter")).append(regex);
                }
            }
        }
        updateApplicationProperty(property,newRegStringBuilder.toString());
    }

    /**
     * Writes current state of applicationProperties to .properties file.
     */
    private void writeApplicationProperties(){
            File propsFile = Paths.get("properties", "last.properties").toFile();
            if(!propsFile.exists()){
                propsFile= makePropertiesFile();
            }
            try (FileWriter w = new FileWriter(propsFile)) {
                applicationProperties.store(w, null);
            } catch (IOException e) {
                throw new RuntimeException();
            }
    }

    /**
     * Reformats a property value using delimiters to using a different delimiter.
     * @param property the property whose value needs to be reformatted
     * @param oldDelimiter the old delimiter
     * @param newDelimiter the new delimiter
     * @return the new property value using the new delimiter
     */
    //not static as it relies on applicationProperties to have been loaded.
    private String reformatDelimitedPropertyValue(String property, String oldDelimiter, String newDelimiter){
        StringBuilder regexStringBuilder = new StringBuilder();
        String regexString = applicationProperties.getProperty(property);
        ArrayList<String> regexList = new ArrayList<>(Arrays.asList(regexString.split(oldDelimiter)));
        for (String regex : regexList) {
            if(regexList.indexOf(regex)==0){
                regexStringBuilder.append(regex);
            }else{
                regexStringBuilder.append(newDelimiter).append(regex);
            }
        }
        return regexStringBuilder.toString();
    }

    /**
     * Gets the history of log runs in reverse order, i.e. the most recent first. Call this using {@code getInstance()}.
     * @return a {@code String} of dates and times a log has been created in the format "yyyy-mm-dd hh:mm:ss" delimited
     * by ";".
     */
    //not static as it relies on applicationProperties to have been loaded.
    public ObservableList<String> getLogRunsHistory(){
        ObservableList<String> runs;
        String runsString = applicationProperties.getProperty("runs");
        ArrayList<String> runsArrayList = new ArrayList<>(Arrays.asList(runsString.split(";")));
        runsArrayList.sort(Comparator.reverseOrder());
        System.out.println("Sorted runs: "+runsArrayList);
        runs = FXCollections.observableArrayList(runsArrayList);
        return runs;
    }

    /**
     * Adds a new date and time to the history of log runs. A maximum of 10 dates and times are kept, the oldest one
     * being removed if this is exceeded. Call this using {@code getInstance()}.
     * @param newDateTime the new date and time to be added. Must be in the format "yyyy-mm-ddThh:mm:ss.sss" or
     *                    "yyyy-mm-dd hh:mm:ss".
     */
    //not static as it relies on applicationProperties to have been loaded.
    public void addToLogRunsHistory(LocalDateTime newDateTime){
        String runsString = applicationProperties.getProperty("runs");
        ArrayList<String> runsArrayList = new ArrayList<>(Arrays.asList(runsString.split(";")));
        runsArrayList.add(newDateTime.toString().replace("T", " ").substring(0,19));
        runsArrayList.sort(Comparator.reverseOrder());
        if (runsArrayList.size()>10){
            runsArrayList.remove(10);
        }
        StringBuilder stringBuilder = new StringBuilder();
        for(String string : runsArrayList){
            if(runsArrayList.indexOf(string)==0){
                stringBuilder.append(string);
            }else{
                stringBuilder.append(";").append(string);
            }
        }
        updateApplicationProperty("runs",stringBuilder.toString());
    }
}
