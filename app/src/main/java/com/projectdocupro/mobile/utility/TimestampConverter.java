package com.projectdocupro.mobile.utility;

import androidx.room.TypeConverter;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class TimestampConverter {

   public static String ISO8601_Format= "YYYY-MM-DD HH:MM:SS.SSS";
    static DateFormat df = new SimpleDateFormat(ISO8601_Format);
    @TypeConverter
    public static Date fromTimestamp(String value) {
        if (value != null) {
            try {
                return df.parse(value);
            } catch (ParseException e) {
                e.printStackTrace();
            }
            return null;
        } else {
            return null;
        }
    }
}