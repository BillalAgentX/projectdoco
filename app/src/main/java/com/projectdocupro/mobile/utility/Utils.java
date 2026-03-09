package com.projectdocupro.mobile.utility;

import android.app.Activity;
import android.content.Context;
import android.graphics.Typeface;
import android.util.Log;
import android.view.inputmethod.InputMethodManager;


import com.google.gson.JsonObject;

import java.lang.reflect.Field;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import retrofit2.Call;
import retrofit2.Response;

public class Utils {
    public static String ISO8601_Format= "yyyy-MM-dd";
    public static String ISO8601_Format2= "yyyy-MM-dd HH:mm:ss";
    public static String ISO8601_Format_New= "yyyy-MM-dd'T'HH:mm'Z'";
    public static String DEVICE_ID= "string";
    public static String ENGLISH_DATE_FORMAT = "yyyy-MM-dd";
    public static String GERMAN_DATE_FORMAT = "dd.MM.yyyy";

    public static void hideSoftKeyboard(Activity activity) {
        try {
            InputMethodManager inputMethodManager =
                    (InputMethodManager) activity.getSystemService(
                            Activity.INPUT_METHOD_SERVICE);
            if (inputMethodManager != null && activity.getCurrentFocus() != null) {
                inputMethodManager.hideSoftInputFromWindow(
                        activity.getCurrentFocus().getWindowToken(), 0);
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public static void overrideFont(Context context, String defaultFontNameToOverride, String customFontFileNameInAssets) {
        try {
            final Typeface customFontTypeface = Typeface.createFromAsset(context.getAssets(), customFontFileNameInAssets);
            final Field defaultFontTypefaceField = Typeface.class.getDeclaredField(defaultFontNameToOverride);
            defaultFontTypefaceField.setAccessible(true);
            defaultFontTypefaceField.set(null, customFontTypeface);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static Timestamp convertStringToTimestamp(String str_date) {
        try {
            DateFormat formatter;
            formatter = new SimpleDateFormat(ISO8601_Format);
            Date date = (Date) formatter.parse(str_date);
            java.sql.Timestamp timeStampDate = new Timestamp(date.getTime());

            return timeStampDate;
        } catch (ParseException e) {
            System.out.println("Exception :" + e);
            return null;
        }
    }

    public static Timestamp convertStringToTimestampUpdated(String str_date) {
        try {
            DateFormat formatter;
            formatter = new SimpleDateFormat(ISO8601_Format2);
            Date date = (Date) formatter.parse(str_date);
            java.sql.Timestamp timeStampDate = new Timestamp(date.getTime());

            return timeStampDate;
        } catch (ParseException e) {
            System.out.println("Exception :" + e);
            return null;
        }
    }

    public static Timestamp convertStringToTimestampTimeZone(String str_date) {
        try {
            DateFormat formatter;
            TimeZone tz = TimeZone.getTimeZone("UTC");
            formatter = new SimpleDateFormat(ISO8601_Format_New);
            formatter.setTimeZone(tz);
            Date date = (Date) formatter.parse(str_date);
            java.sql.Timestamp timeStampDate = new Timestamp(date.getTime());

            return timeStampDate;
        } catch (ParseException e) {
            System.out.println("Exception :" + e);
            return null;
        }
    }

    public static Timestamp getCurrentTimeStamp() {


        Timestamp timeStampDate = new Timestamp(new Date().getTime());

        return timeStampDate;
    }

    public static String dateFormatConversion(String date, String patternFromConversion, String patternToConversion) {
        String convertedDate = "";
        SimpleDateFormat fromFormat = new SimpleDateFormat(patternFromConversion);
        SimpleDateFormat expectedFormat = new SimpleDateFormat(patternToConversion);

        try {
            convertedDate = expectedFormat.format(fromFormat.parse(date));
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return convertedDate;
    }

    public static void showLogger2(String str){
        Log.d("DEBUG_2",str);
    }

    public static void showLogger(String str){
        Log.d("DEBUG_S",str);
    }



    public static void logResponse(Call<JsonObject> call, Response<JsonObject> response) {
        try {
            Utils.showLogger("Calling URL>>>" + call.request().url() + "");
            Utils.showLogger("Calling RESPONSE CODE>>>" + response.code());

            if (response.code() == 201 || response.code() == 200)
                Utils.showLogger("api response");
            else
                Utils.showLogger("api error"+response.errorBody().string());
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }


    public static int floatToLowerInt(float fl){
        String sFl = fl+"";
        if(sFl.contains(".")) {
            String newV = sFl.substring(0,sFl.indexOf("."));
            return Integer.parseInt(newV);
        }
        else
            return Integer.parseInt(sFl);
    }
}

