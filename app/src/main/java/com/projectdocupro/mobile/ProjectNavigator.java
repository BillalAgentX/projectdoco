package com.projectdocupro.mobile;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.net.ConnectivityManager;
import android.os.Build;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;

import androidx.multidex.MultiDexApplication;

import com.projectdocupro.mobile.utility.Utils;

public class ProjectNavigator extends MultiDexApplication implements LocationListener, SensorEventListener{

    public static Location gpsLocation;
    public static float compassDegrees;
    public static SensorManager sensorManager;
    public static LocationManager locationManager;
    public static LocationProvider low;
    public static LocationProvider high;
    static public  Context context;

    public static boolean mobileNetworkIsConnected (Context context) {
        try {
            return ((ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE)).getNetworkInfo(ConnectivityManager.TYPE_MOBILE).isConnected();
        } catch (Exception e) {
            return false;
        }
    }

    public static boolean wlanIsConnected (Context context) {
        try {
            return ((ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE)).getNetworkInfo(ConnectivityManager.TYPE_WIFI).isConnected();
        } catch (Exception e) {
            return false;
        }
    }

    public static boolean isPhotoActivityForground=false;
    @Override
    public void onCreate() {
        super.onCreate();
        context=this;
        Utils.overrideFont(getApplicationContext(), "SERIF", "fonts/Roboto-Regular.ttf"); // font from assets: "assets/fonts/Roboto-Regular.ttf

    }

    public static Dialog showCustomProgress(final Context context, String Msg, boolean isCancelable) {
        if (((Activity) context).isFinishing()) {
            return null;
        }

        Dialog customProgressDialog = new Dialog(context, android.R.style.Theme_Translucent);
        customProgressDialog.setOwnerActivity((Activity) context);
        customProgressDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        customProgressDialog.setContentView(R.layout.waitlayout);
        customProgressDialog.setCancelable(isCancelable);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            customProgressDialog.getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            customProgressDialog.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            customProgressDialog.getWindow().setStatusBarColor(context.getResources().getColor(R.color.status_bar));
        }
//		if (!Msg.equals(""))
//		{
//        ProgressBar tv = (ProgressBar) customProgressDialog.findViewById(R.id.progressBar_cyclic);
//			tv.setVisibility(View.VISIBLE);
//			tv.setText(Msg);
//		}

        customProgressDialog.show();
        return customProgressDialog;
    }


    @Override
    public void onLocationChanged(Location location) {
        gpsLocation = location;


    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }

    public Location getGpsLocation() {
        return gpsLocation;
    }

    public float getCompassDegrees() {
        return compassDegrees;
    }

    @Override
    public void onSensorChanged(SensorEvent event) {

        compassDegrees = event.values[0];

    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    public static Criteria createCoarseCriteria() {
        Criteria c = new Criteria();

        c.setAccuracy(Criteria.ACCURACY_COARSE);
        c.setAltitudeRequired(false);
        c.setBearingRequired(false);
        c.setSpeedRequired(false);
        c.setCostAllowed(true);
        c.setPowerRequirement(Criteria.POWER_HIGH);

        return c;
    }

    public static Criteria createFineCriteria() {
        Criteria c = new Criteria();

        c.setAccuracy(Criteria.ACCURACY_FINE);
        c.setAltitudeRequired(false);
        c.setBearingRequired(false);
        c.setSpeedRequired(false);
        c.setCostAllowed(true);
        c.setPowerRequirement(Criteria.POWER_HIGH);

        return c;
    }

    private static final int PERMISSION_REQUEST_CODE = 1240;
    String[] appPermissions = {
//            Manifest.permission.WRITE_EXTERNAL_STORAGE,
//            Manifest.permission.CAMERA,
//            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION

    };


    public void deactivateGPS() {
        if (locationManager != null) {
            locationManager.removeUpdates(this);
        }

    }
}
