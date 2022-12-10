package com.meriban.barcodevalidator;

import com.meriban.barcodevalidator.managers.PropertiesManager;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.TemporalField;
import java.time.temporal.WeekFields;
import java.util.HashMap;
import java.util.Properties;


public class LogParser {
    public enum Mode{
        THIS_WEEK,
        LAST_WEEK,
        TODAY,
        CUSTOM,
        SINCE_LAST
    }
    public static final int FROM = 0;
    public static final int TO = 1;
    // static variable single_instance of type Singleton
    private static LogParser logParser = null;
    private static DatabaseHandler databaseHandler;
    private static Validator barcodeValidator;

    //private constructor restricted to this class itself
    private LogParser() {
    }

    //static method to create instance of Singleton class
    public static LogParser getInstance() {
        if (logParser == null)
            logParser = new LogParser();
        return logParser;
    }

    public void createLog(File saveFile, Mode modeIn, boolean removals, LocalDateTime fromIn){
        if(modeIn==Mode.SINCE_LAST){
            databaseHandler = DatabaseHandler.getInstance();
            barcodeValidator = Validator.getInstance();
            File dataDir = barcodeValidator.getDataDir();
            StringBuilder builder = new StringBuilder();
            builder.append("BARCODE, DATE, ACTION\n");
            //just getting rid of the "T" and replacing it with " " again for the query
            try (ResultSet results = databaseHandler.connectToDatabase(dataDir, "testdb.db").betweenDateTimeQuery(fromIn.toString().replace("T"," "), removals)) {
                while (results.next()){
                    builder.append(results.getString(1)).append(",");
                    builder.append(results.getString(2)).append(",");
                    builder.append(results.getInt(3)).append("\n");
                }
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
            write(builder, saveFile);
        }else{ //this shouldn't happen, but just in case
            createLog(saveFile, modeIn, removals, fromIn.toLocalDate(), null);
        }
    }

    public void createLog(File saveFile, Mode modeIn, boolean removals, LocalDate fromIn, LocalDate toIn) {
        databaseHandler = DatabaseHandler.getInstance();
        barcodeValidator = Validator.getInstance();
        File dataDir = barcodeValidator.getDataDir();
        String fromDate;
        String toDate;
        if (modeIn == Mode.CUSTOM) {
            fromDate = fromIn.toString();
            toDate = toIn.plusDays(1).toString();
        } else {
            Properties applicationProperties = PropertiesManager.getInstance().getApplicationProperties();
            String startOfWeek = applicationProperties.getProperty("start_of_week");
            HashMap<Integer, String> dates = computeDatesForQuery(modeIn, DayOfWeek.valueOf(startOfWeek));
            fromDate = dates.get(FROM);
            toDate = dates.get(TO);
        }
        StringBuilder builder = new StringBuilder();
        builder.append("BARCODE, DATE, ACTION\n");
        try (ResultSet results = databaseHandler.connectToDatabase(dataDir, "testdb.db").betweenDateQuery(fromDate, toDate, removals)) {
            while (results.next()){
                builder.append(results.getString(1)).append(",");
                builder.append(results.getString(2)).append(",");
                builder.append(results.getInt(3)).append("\n");
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        write(builder, saveFile);
    }

    //if one passes 12/12/2022 into the query as end date of BETWEEN it'll interpret this as "before the first second of
    //12/12/22, so to get the entries created on 12/12/22 as well 13/12/2022 must be passed into the query
    private HashMap<Integer, String> computeDatesForQuery(Mode mode, DayOfWeek startDayOfWeek) {
        HashMap<Integer, String> map = new HashMap<>();
        String fromDate = null;
        String toDate = null;
        LocalDate today = LocalDate.now();
        //Create WeekFields instance feeding in what the first day of the week should be
        WeekFields weekFields = WeekFields.of(startDayOfWeek,4);
        //Create a variable that can be used instead of "regular" getFirstDayOfWeek(); to be used with get(variable);
        // returns the int value for the position of the day in the week (1 to 7)
        TemporalField customDaysOfWeek = weekFields.dayOfWeek();
        switch (mode) {
            case TODAY:
                fromDate = today.toString();
                toDate = today.plusDays(1).toString();
                break;
            case THIS_WEEK:
                LocalDate firstDayOfWeek = today.minusDays(today.get(customDaysOfWeek)-1);
                fromDate = firstDayOfWeek.toString();
                toDate = firstDayOfWeek.plusDays(7).toString();
                break;
            case LAST_WEEK:
                LocalDate firstDayOfLastWeek = today.minusDays(today.get(customDaysOfWeek)-1).minusWeeks(1);
                fromDate = firstDayOfLastWeek.toString();
                toDate = firstDayOfLastWeek.plusDays(7).toString();
                break;
        }
        map.put(FROM, fromDate);
        map.put(TO, toDate);
        return map;
    }

    public HashMap<Integer, LocalDate> computeDatesForDisplay(Mode mode, DayOfWeek startDayOfWeek) {
        HashMap<Integer, LocalDate> map = new HashMap<>();
        LocalDate fromDate = null;
        LocalDate toDate = null;
        LocalDate today = LocalDate.now();
        //Create WeekFields instance feeding in what the first day of the week should be
        WeekFields weekFields = WeekFields.of(startDayOfWeek,4);
        //Create a variable that can be used instead of "regular" getFirstDayOfWeek(); to be used with get(variable);
        // returns the int value for the position of the day in the week (1 to 7)
        TemporalField customDaysOfWeek = weekFields.dayOfWeek();
        switch (mode) {
            case TODAY:
            case SINCE_LAST:
                fromDate = today;
                break;
            case THIS_WEEK:
                LocalDate firstDayOfWeek = today.minusDays(today.get(customDaysOfWeek)-1);
                fromDate = firstDayOfWeek;
                toDate = firstDayOfWeek.plusDays(6);
                break;
            case LAST_WEEK:
                LocalDate firstDayOfLastWeek = today.minusDays(today.get(customDaysOfWeek)-1).minusWeeks(1);
                fromDate = firstDayOfLastWeek;
                toDate = firstDayOfLastWeek.plusDays(6);
                break;
        }
        map.put(FROM, fromDate);
        map.put(TO, toDate);
        return map;
    }

    private void write(StringBuilder builder, File saveFile){
        try {
            FileWriter writer = new FileWriter(saveFile, true);
            writer.write(builder.toString());
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
