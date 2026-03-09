package com.projectdocupro.mobile.fragments.add_direction;

import android.Manifest;

import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentTransaction;

import com.projectdocupro.mobile.R;
import com.projectdocupro.mobile.managers.AppConstantsManager;
import com.projectdocupro.mobile.managers.SharedPrefsManager;
import com.projectdocupro.mobile.utility.Utils;

import java.util.ArrayList;
import java.util.List;


public class ProjectDocuMainActivity extends AppCompatActivity implements LocationListener{
//    public static ProjectDocuMainActivity projectDocuMainActivity = null;
    private LocationManager locationManager = null;
    private Location gpsLocation = null;
    private LocationProvider low = null;
    private LocationProvider high = null;
    private SharedPrefsManager sharedPrefsManager;
    private ProjectDocuShowPlanFragment newFragment;
    public final static int DIALOG_PHOTO_COULD_NOT_BE_SAVED = 8;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_direction_with_frag);
         //projectDocuMainActivity = this;
        sharedPrefsManager  =   new SharedPrefsManager(this);

//            ProjectDocuShowPlanFragment newFragment = new ProjectDocuShowPlanFragment(null,0,0,0);
//            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
//            ft.add(R.id.content_frame, newFragment).addToBackStack("ProjectDocuShowPlanFragment").commit();

        Button button= findViewById(R.id.btn_add);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                 newFragment = new ProjectDocuShowPlanFragment(null,0,0,0);
                FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
                ft.add(R.id.content_frame, newFragment).addToBackStack("ProjectDocuShowPlanFragment").commit();
            }
        });

        if (sharedPrefsManager.getBooleanValue(AppConstantsManager.ACTIVATE_GPS,true)) {
            activateGPS();
        } else {
            deactivateGPS();
        }

    }

    public void activateGPS() {
        // PERMISSION FOR GPS FOR DEVICES WITH ANDROID 6 or higher

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            //ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, PERMISSON_REQUEST_CODE_COARSE_LOCATION);
            checkAndRequestPermissions();
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            //ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSON_REQUEST_CODE_FINE_LOCATION);
            checkAndRequestPermissions();
        }
        else {
            locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);



            low = locationManager.getProvider(locationManager.getBestProvider(createCoarseCriteria(), true));

            high = locationManager.getProvider(locationManager.getBestProvider(createFineCriteria(), true));

            if (low != null) {
                locationManager.requestLocationUpdates(low.getName(), 0, 0, this);
            }

            if (high != null) {
                locationManager.requestLocationUpdates(high.getName(), 0, 0, this);
            }
        }
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
    public boolean checkAndRequestPermissions()
    {

        List<String> listPermissionsNeeded = new ArrayList<>();
        for(String perm : appPermissions)
        {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && ContextCompat.checkSelfPermission(this, perm) != PackageManager.PERMISSION_GRANTED) {
                // Permission is not granted
                if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                        perm)) {
                    Toast.makeText(this, "The app needs more permissions! Please activate these in the system settings of the mobile phone!", Toast.LENGTH_LONG).show();
                } else {
                    // No explanation needed; request the permission
                    listPermissionsNeeded.add(perm);
                }
            }
        }
        if(!listPermissionsNeeded.isEmpty())
        {
            ActivityCompat.requestPermissions(this,
                    listPermissionsNeeded.toArray(new String[listPermissionsNeeded.size()]),
                    PERMISSION_REQUEST_CODE
            );
            return false;
        }
        return true;
    }

    public void deactivateGPS() {
        if (locationManager != null) {

            locationManager.removeUpdates(this);
        }

//        deactivateGPSButton();

//        if (gps_button_animation != null) {
            // gps_button_animation.stop();
//        }
    }



    @Override
    public void onLocationChanged(Location location) {
        gpsLocation = location;
        if (newFragment!=null&&newFragment instanceof ProjectDocuShowPlanFragment) {
            if (sharedPrefsManager.getBooleanValue(AppConstantsManager.ACTIVATE_GPS,true)) {
                ((ProjectDocuShowPlanFragment) newFragment).setPlanPositionByGps(gpsLocation);
            }
        }
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

    public void deactivateGPSNonStop() {
        sharedPrefsManager.setBooleanValue(AppConstantsManager.ACTIVATE_GPS, false);
        Utils.showLogger2("ProjectDocuMainActivity>>deactiviteGPSNonSto");

    }
}
