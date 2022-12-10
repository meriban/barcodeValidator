package com.meriban.barcodevalidator;

import org.junit.Test;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalField;
import java.time.temporal.WeekFields;

import static com.meriban.barcodevalidator.LogParser.Mode.TODAY;

public class LogParserTest {
    @Test
    public void computeDateTest(){
        LogParser.Mode mode = TODAY;
        String fromDate = null;
        String toDate = null;
        LocalDate today = LocalDate.now();
        //Create WeekFields instance feeding in what the first day of the week should be
        WeekFields weekFields = WeekFields.of(DayOfWeek.FRIDAY,4);
        //Create a variable that can be used instead of "regular" getFirstDayOfWeek(); to be used with get(variable);
        // returns the int value for the position of the day in the week (1 to 7)
        TemporalField customDaysofWeek = weekFields.dayOfWeek();
        switch (mode) {
            case TODAY:
                fromDate = today.toString();
                toDate = today.plusDays(1).toString();
                System.out.println("Mode TODAY, start "+fromDate);
                System.out.println("Mode TODAY, end "+toDate);
                break;
            case THIS_WEEK:
                LocalDate firstDayOfWeek = today.minusDays(today.get(customDaysofWeek)-1);
                fromDate = firstDayOfWeek.toString();
                toDate = firstDayOfWeek.plusDays(7).toString();
                System.out.println("Mode THIS_WEEK, start "+fromDate);
                System.out.println("Mode THIS_WEEK, end "+toDate);
                break;
            case LAST_WEEK:
                LocalDate firstDayOfLastWeek = today.minusDays(today.get(customDaysofWeek)-1).minusWeeks(1);
                fromDate = firstDayOfLastWeek.toString();
                toDate = firstDayOfLastWeek.plusDays(7).toString();
                System.out.println("Mode LAST_WEEK, start "+fromDate);
                System.out.println("Mode LAST_WEEK, end "+toDate);
                break;
        }
    }

}