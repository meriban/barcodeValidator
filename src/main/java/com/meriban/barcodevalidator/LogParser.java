package com.meriban.barcodevalidator;

import com.meriban.barcodevalidator.managers.FileManager;
import com.meriban.barcodevalidator.managers.PropertiesManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;

import static com.meriban.barcodevalidator.DateParser.FROM;
import static com.meriban.barcodevalidator.DateParser.TO;

/**
 * Creates a log file depending on the respective {@link LogMode}.
 */
public class LogParser {
    public enum LogMode {
        THIS_WEEK,
        LAST_WEEK,
        TODAY,
        CUSTOM,
        SINCE_LAST,
        ALL
    }


    /**
     * Creates a log if no end date is known. This should only be used with {@code LogModes} {@link LogMode#SINCE_LAST}
     * and {@link LogMode#ALL}. For all other {@code LogModes} use
     * {@link #createLog(File, LogMode, boolean, LocalDate, LocalDate)}.
     *
     * @param saveFile the file to save the log to
     * @param modeIn   the {@link LogMode}. This must be {@link LogMode#SINCE_LAST} or {@link LogMode#ALL}.
     * @param removals whether values associated with {@link Action#REMOVE} are should be included in the log
     * @param fromIn   the from date. This can be {@code null} in which case all entries in the database will be
     *                 included in the log.
     */
    public static void createLog(@NotNull File saveFile, @NotNull LogMode modeIn, boolean removals, @Nullable LocalDateTime fromIn) {
        if (modeIn == LogMode.SINCE_LAST || modeIn == LogMode.ALL) {
            File dataDir = FileManager.getInstance().getDataDirectory();
            StringBuilder builder = new StringBuilder();
            builder.append(parseHeader()); //header line
            if (fromIn != null) {
                //just getting rid of the "T" and replacing it with " " again for the query
                try (ResultSet results = DatabaseHandler.betweenDateTimeQuery(
                        dataDir,
                        FileManager.getInstance().getDatabaseName(),
                        fromIn,
                        removals)) {
                    builder.append(parseResultSet(results));
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
            } else {
                try (ResultSet results = DatabaseHandler.getAll(
                        dataDir,
                        FileManager.getInstance().getDatabaseName(),
                        removals)) {
                    builder.append(parseResultSet(results));
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
            }
            write(builder, saveFile);
        } else { //this shouldn't happen if implemented correctly, but just in case
            throw new ModeException("Method LogParser.createLog(File, LogMode, boolean, LocalDateTime) cannot be used with LogMode " + modeIn);
        }
    }

    /**
     * Creates a log where both from and to dates are known. This method must <b>not</b> be used with {@code LogModes}
     * {@link LogMode#SINCE_LAST} and {@link LogMode#ALL}, use {@link #createLog(File, LogMode, boolean, LocalDateTime)}
     * instead.
     *
     * @param saveFile            the file to save the log to
     * @param modeIn              the {@link LogMode}. This must <b>not</b> be {@code LogMode.SINCE_LAST} or {@code LogMode.ALL}.
     * @param removals            whether values associated with {@link Action#REMOVE} are should be included in the log
     * @param fromDatePickerValue the from date as retrieved from the respective {@code DatePicker}. If the {@code mode}
     *                            is not {@link LogMode#CUSTOM} this value will be ignored
     * @param toDatePickerValue   the to date as retrieved from the respective {@code DatePicker}. If the {@code mode} is
     *                            not {@link LogMode#CUSTOM} this value will be ignored
     */
    public static void createLog(@NotNull File saveFile, @NotNull LogMode modeIn, boolean removals,  LocalDate fromDatePickerValue,  LocalDate toDatePickerValue) {
        File dataDir = FileManager.getInstance().getDataDirectory();
        String fromDate;
        String toDate;
        if (modeIn == LogMode.SINCE_LAST || modeIn == LogMode.ALL) {
            throw new ModeException("Method LogParser.createLog(File, LogMode, boolean, LocalDate, LocalDate) cannot be used with LogMode " + modeIn);
        } else if (modeIn == LogMode.CUSTOM) {
            fromDate = fromDatePickerValue.toString();
            toDate = toDatePickerValue.plusDays(1).toString();
        } else {
            String startOfWeek = PropertiesManager.getInstance().getProperty("start_of_week");
            HashMap<Integer, String> dates = DateParser.computeDatesForQuery(modeIn, DayOfWeek.valueOf(startOfWeek));
            fromDate = dates.get(FROM);
            toDate = dates.get(TO);
        }
        StringBuilder builder = new StringBuilder();
        builder.append(parseHeader());
        try (ResultSet results = DatabaseHandler.betweenDateQuery(
                dataDir,
                FileManager.getInstance().getDatabaseName(),
                fromDate,
                toDate,
                removals)) {
            builder.append(parseResultSet(results));
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        write(builder, saveFile);
    }

    /**
     * Writes the log to file.
     * @param builder the {@code StringBuilder} containing the log information
     * @param saveFile the file to save to
     */
    private static void write(StringBuilder builder, File saveFile) {
        try {
            FileWriter writer = new FileWriter(saveFile, false);
            writer.write(builder.toString());
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Parses a {@code ResultSet} for .csv export
     * @param results the {@code ResulSet} to parse
     * @return the parsed String
     * @throws SQLException
     */
    private static String parseResultSet(ResultSet results) throws SQLException {
        StringBuilder builder = new StringBuilder();
        while (results.next()) { //iterate through results. At start cursor is before the first result thus calling .next() doesn't slip any
            builder.append(results.getString(1)).append(",");
            builder.append(results.getString(2)).append(",");
            builder.append(results.getInt(3)).append("\n");
        }
        return builder.toString();
    }

    /**
     * Parses the header for .csv export
     * @return the header string
     */
    private static String parseHeader() {
        return "BARCODE, DATE, ACTION\n";
    }
}
