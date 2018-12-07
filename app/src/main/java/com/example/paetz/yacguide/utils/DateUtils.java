package com.example.paetz.yacguide.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class DateUtils {

    public final static String UNKNOWN_DATE = "0000-00-00";

    public static String formatDate(String date) {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        try {
            Date formattedDate = format.parse(date);
            Calendar cal = Calendar.getInstance();
            cal.setTime(formattedDate);
            return cal.get(Calendar.DAY_OF_MONTH) + "." + (cal.get(Calendar.MONTH) + 1) + "." + cal.get(Calendar.YEAR);
        } catch (ParseException e) {
            return "";
        }
    }
}
