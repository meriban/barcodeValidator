package com.meriban.barcodevalidator;

import com.meriban.barcodevalidator.managers.PropertiesManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.TemporalField;
import java.time.temporal.WeekFields;
import java.util.HashMap;

/**
 * Utility class to deal with date computations
 */
public class DateParser {
    public static final int FROM = 0;
    public static final int TO = 1;
    /**
     * Computes log timeframe dates for {@link LogParser.LogMode#TODAY}, {@link LogParser.LogMode#THIS_WEEK} and {@link LogParser.LogMode#LAST_WEEK}
     * for the database query. This must <b>not</b> be used with other {@code LogModes} as this will result in a
     * {@link ModeException} thrown at runtime.
     * <p>The computing of log timeframe dates for the database must be different than the one for display, because
     * if one passes e.g. 2022-12-12 into the query as end date of BETWEEN it'll interpret this as "before the
     * first second of 2020-12-12", so to get the entries created on 2022-12-12 as well, 2022-12-13 must be passed into
     * the query. For display however 2022-12-12 is what one wants.</p>
     * @param mode the {@code LogMode}. This must be {@link LogParser.LogMode#TODAY}, {@link LogParser.LogMode#THIS_WEEK} or
     * {@link LogParser.LogMode#LAST_WEEK}
     * @param startDayOfWeek the day treated as the start of the week
     * @return a {@code HashMap} containing the computed from-date at key {@link #FROM} and the to-date at key
     * {@link #TO}.
     */
    public static @NotNull HashMap<Integer, String> computeDatesForQuery(@NotNull LogParser.LogMode mode, @NotNull DayOfWeek startDayOfWeek) {
        HashMap<Integer, String> map = new HashMap<>();
        String fromDate;
        String toDate;
        LocalDate today = LocalDate.now();
        //Create WeekFields instance feeding in what the first day of the week should be
        WeekFields weekFields = WeekFields.of(startDayOfWeek,4);
        //Create a variable that can be used instead of "regular" getFirstDayOfWeek(); to be used with get(variable);
        // returns the int value for the position of the day in the week (1 to 7)
        TemporalField customDaysOfWeek = weekFields.dayOfWeek();
        switch (mode) {
            case TODAY:
                fromDate = today.toString();
                toDate = today.plusDays(1).toString(); //see method description note
                break;
            case THIS_WEEK:
                LocalDate firstDayOfWeek = today.minusDays(today.get(customDaysOfWeek)-1);
                //EXAMPLE:
                //defined first day of week = FRIDAY, FRIDAY = position 1
                //today = MONDAY, MONDAY = position 4
                //today - position (i.e. 4) days = position 0 -> i.e. position 7, that's wrong!
                //today - (position - 1 (i.e. 3)) days = position 1 -> correct!
                fromDate = firstDayOfWeek.toString();
                toDate = firstDayOfWeek.plusDays(7).toString(); //see method description note
                break;
            case LAST_WEEK:
                LocalDate firstDayOfLastWeek = today.minusDays(today.get(customDaysOfWeek)-1).minusWeeks(1);//see Example above
                fromDate = firstDayOfLastWeek.toString();
                toDate = firstDayOfLastWeek.plusDays(7).toString(); //see method description note
                break;
            default:
                throw new ModeException("Method LogParser.computeDatesForQuery(LogMode, DayOfWeek) cannot be used with LogMode " + mode);
        }
        map.put(FROM, fromDate);
        map.put(TO, toDate);
        return map;
    }

    /**
     * Computes log timeframe dates for use in automatic log file name creation. Don't use this with
     * {@link LogParser.LogMode#CUSTOM} as this one depends entirely on user input and cannot be calculated.
     * <p>The computing of log timeframe dates for the database must be different than the one for display, because
     * if one passes e.g. 2022-12-12 into the query as end date of BETWEEN it'll interpret this as "before the
     * first second of 2020-12-12", so to get the entries created on 2022-12-12 as well, 2022-12-13 must be passed into
     * the query. For display however 2022-12-12 is what one wants.</p>
     * @param mode the {@code LogMode}. Do <b>not</b> use {@link LogParser.LogMode#CUSTOM}!
     * @param startDayOfWeek the day treated as the start of the week
     * @return a {@code HashMap} containing the computed from-date at key {@link #FROM} and the to-date at key
     *       {@link #TO}.
     */
    public static @NotNull HashMap<Integer, LocalDate> computeDatesForDisplay(@NotNull LogParser.LogMode mode, @NotNull DayOfWeek startDayOfWeek) {
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
            case ALL: //leave both dates at null, there will be no date element in the file name
                break;
            case TODAY:
            case SINCE_LAST:
                fromDate = today; // only from-date will be part of the file name
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
    /**
     * Assigns dates from {@code DatePickers} or computes them based on the {@link LogParser.LogMode}.
     *
     * @param mode the {@link LogParser.LogMode}
     * @return a {@code HashMap} containing the from date (key {@link #FROM}) and to date (key {@link #TO}) as
     * {@link LocalDate}.
     */
    public static @NotNull HashMap<Integer, LocalDate> getFileNameDates(@NotNull LogParser.LogMode mode,
                                                                        @Nullable LocalDate fromDatePickerValue,
                                                                        @Nullable LocalDate toDatePickerValue){
        HashMap<Integer, LocalDate> dates = new HashMap<>();
        if (mode == LogParser.LogMode.CUSTOM) {
            dates.put(FROM, fromDatePickerValue);
            dates.put(TO, toDatePickerValue);
        } else {
            String startOfWeek = PropertiesManager.getInstance().getApplicationProperties().getProperty("start_of_week");
            dates = DateParser.computeDatesForDisplay(mode, DayOfWeek.valueOf(startOfWeek));
        }
        return dates;
    }
    public static LocalDateTime adjustForDST(LocalDateTime dateTime){
        return dateTime.minusHours(1);
    }

    public static String computeDateForQuery(LocalDateTime dateTime){
        return dateTime.toString().replace("T", " ").substring(0,19);
    }
}
