package com.projectdocupro.mobile.managers;

import android.content.Context;
import android.content.SharedPreferences;

public class SharedPrefsManager {

    public SharedPreferences sharedPreferences;
    public SharedPreferences appSharedPreferences;

    public SharedPrefsManager(Context context) {
        sharedPreferences = context.getSharedPreferences(AppConstantsManager.SHARED_PREFS_NAME, Context.MODE_PRIVATE);
        appSharedPreferences = context.getSharedPreferences(AppConstantsManager.APP_SHARED_PREFS_NAME, Context.MODE_PRIVATE);
    }

    public void setIntegerValue(String key, int value) {
        sharedPreferences.edit().putInt(key, value).apply();
    }

    public int getIntegerValue(String key, int defaultValue) {
        return sharedPreferences.getInt(key, defaultValue);
    }

    public void setBooleanValue(String key, boolean value) {
        sharedPreferences.edit().putBoolean(key, value).apply();
    }

    public boolean getBooleanValue(String key, boolean defaultValue) {
        return sharedPreferences.getBoolean(key, defaultValue);
    }

    public void setStringValue(String key, String value) {
        sharedPreferences.edit().putString(key, value).apply();
    }

    public String getStringValue(String key, String defaultValue) {
        return sharedPreferences.getString(key, defaultValue);
    }


    public void setLastProjectId(Context c, String count) {
        sharedPreferences.edit().putString(AppConstantsManager.USER_LAST_PROJECT_ID, count).apply();
    }

    public String getLastProjectId(Context c) {
        return sharedPreferences.getString(AppConstantsManager.USER_LAST_PROJECT_ID, "");
    }


    public void setFavouriteProjetBooleanValue(boolean value) {
        sharedPreferences.edit().putBoolean("USER_FAVOURITE_PROJECT", value).apply();

    }

    public boolean getFavouriteProjectBooleanValue() {
        return sharedPreferences.getBoolean("USER_FAVOURITE_PROJECT", false);
    }

    public void setShowRecentWordScreenBooleanValue(boolean value) {
        sharedPreferences.edit().putBoolean(AppConstantsManager.SHOW_RECENT_WORDS, value).apply();

    }

    public boolean getShowRecentWordScreenBooleanValue() {
        return sharedPreferences.getBoolean(AppConstantsManager.SHOW_RECENT_WORDS, false);
    }


//    public void setLastProjectName(Context c, String count) {
//        sharedPreferences = c.getSharedPreferences("LastProjectName", 0);
//        SharedPreferences.Editor prefs_editor = sharedPreferences.edit();
//        prefs_editor.putString("LastProjectName", count);
//        prefs_editor.apply();
//    }

    public void setLastProjectName(Context c, String count) {

        sharedPreferences.edit().putString(AppConstantsManager.USER_LAST_PROJECT_NAME, count).apply();
    }

    public void setLastProjectPhoto(Context c, String count) {
        sharedPreferences.edit().putString(AppConstantsManager.USER_LAST_PROJECT_PHOTO_URL, count).apply();
    }

    public String getLastProjectPhoto(Context c) {
//        sharedPreferences = c.getSharedPreferences("LastProjectPhoto", 0);
        return sharedPreferences.getString(AppConstantsManager.USER_LAST_PROJECT_PHOTO_URL, "");
    }

    public String getLastProjectName(Context c) {
        return sharedPreferences.getString(AppConstantsManager.USER_LAST_PROJECT_NAME, "");
    }

    public void setLastUsedGewerk(Context c, String count) {
        sharedPreferences.edit().putString("LastUsedGewerk", count).apply();
    }

    public String getLastUsedGewerk(Context c) {
        return sharedPreferences.getString("LastUsedGewerk", "");
    }

    public void setLastUsedPlanId(Context c, String count) {
        sharedPreferences.edit().putString(AppConstantsManager.USER_LAST_PLAN_ID, count).apply();
    }

    public String getLastUsedPlanId(Context c) {
        return sharedPreferences.getString(AppConstantsManager.USER_LAST_PLAN_ID, "-1");
    }


    public void setisGPSActive(Context c, boolean flag) {
        sharedPreferences.edit().putBoolean(AppConstantsManager.GPS_ACTIVE_SETTING_SCREEN, flag).apply();
    }

    public boolean getisGPSActive(Context c) {
        return sharedPreferences.getBoolean(AppConstantsManager.GPS_ACTIVE_SETTING_SCREEN,false);
    }

    public void setGpsAccuracy(Context c, String count) {
        sharedPreferences.edit().putString(AppConstantsManager.GPS_ACCURACY_SETTING_SCREEN, count).apply();
    }

    public String getGpsAccuracy(Context c) {
        return sharedPreferences.getString(AppConstantsManager.GPS_ACCURACY_SETTING_SCREEN, "off");
    }

    public void setGpsLatLng(Context c, String count) {
        sharedPreferences.edit().putString(AppConstantsManager.GPS_LAT_LNG, count).apply();
    }

    public String getGpsLatLng(Context c) {
        return sharedPreferences.getString(AppConstantsManager.GPS_LAT_LNG, "");
    }

    public void setisLoadLastProject(Context c, boolean flag) {
        sharedPreferences.edit().putBoolean(AppConstantsManager.LOAD_LAST_PROJECT_ID, flag).apply();
    }

    public boolean getisLoadLastProject(Context c) {
        return sharedPreferences.getBoolean(AppConstantsManager.LOAD_LAST_PROJECT_ID,false);
    }

    public void setisLoadLastPlan(Context c, boolean flag) {
        sharedPreferences.edit().putBoolean(AppConstantsManager.LOAD_LAST_PLAN_ID, flag).apply();
    }

    public boolean getisLoadLastPlan(Context c) {
        return sharedPreferences.getBoolean(AppConstantsManager.LOAD_LAST_PLAN_ID,false);
    }


    public void setDeviceId( String count) {

        appSharedPreferences.edit().putString(AppConstantsManager.DEVICE_ID, count).apply();
    }

    public String getDeviceId() {
        return appSharedPreferences.getString(AppConstantsManager.DEVICE_ID,"string");
    }

}
