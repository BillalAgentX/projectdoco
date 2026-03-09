package com.projectdocupro.mobile.fragments.add_direction;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Criteria;
import android.location.GpsStatus;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.projectdocupro.mobile.ProjectsDatabase;
import com.projectdocupro.mobile.R;
import com.projectdocupro.mobile.activities.SavePictureActivity;
import com.projectdocupro.mobile.dao.ReferPointPlansDao;
import com.projectdocupro.mobile.managers.AppConstantsManager;
import com.projectdocupro.mobile.managers.SharedPrefsManager;
import com.projectdocupro.mobile.models.ReferPointJSONPlanModel;
import com.projectdocupro.mobile.utility.Utils;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import androidx.annotation.Nullable;
import androidx.annotation.UiThread;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentTransaction;


public class PhotoAddDirectionMainActivity2Testing extends AppCompatActivity implements LocationListener, SensorEventListener, GpsStatus.Listener {
    //    public static ProjectDocuMainActivity projectDocuMainActivity = null;
    private LocationManager locationManager = null;
    public Location gpsLocation = null;
    private LocationProvider low = null;
    private LocationProvider high = null;
    private SharedPrefsManager sharedPrefsManager;
    private PhotoAddDirectionFragment2Testing newFragment;
    Toolbar toolbar;
    Toolbar toolbar1;
    private SensorManager sensorManager;
    public float compassDegrees;
    String planName = "";
    public List<ReferPointJSONPlanModel> referPointList;
    CountDownLatch latch = new CountDownLatch(1);
    public int actionBarHeight;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_direction_with_frag_testing);
        //projectDocuMainActivity = this;
        sharedPrefsManager = new SharedPrefsManager(this);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar1 = (Toolbar) findViewById(R.id.toolbar1);
//        toolbar.setVisibility(View.GONE);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        // Calculate ActionBar height
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
            TypedValue tv = new TypedValue();
            if (getTheme().resolveAttribute(android.R.attr.actionBarSize, tv, true)) {
                actionBarHeight = TypedValue.complexToDimensionPixelSize(tv.data, getResources().getDisplayMetrics());
            }
        }
        try {
            if (getIntent().hasExtra("planName")) {
                planName = getIntent().getStringExtra("planName");
            }

            if (!planName.equals("") && getIntent().hasExtra("fromPhoto") && getIntent().getBooleanExtra("fromPhoto", false)) {
                toolbar.setTitle(planName);
            } else {
                toolbar.setTitle(getResources().getString(R.string.plan_detail));
            }

        } catch (Exception e) {

        }

        toolbar.setTitle(getResources().getString(R.string.plan_detail));
        toolbar.setNavigationIcon(R.drawable.ic_back);

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        sharedPrefsManager.setBooleanValue(AppConstantsManager.ACTIVATE_GPS_MANUAL_STATE, false);
        sharedPrefsManager.setBooleanValue(AppConstantsManager.VIEW_DIRECTION_THROUGH_COMPASS_MANUAL_STATE, false);


        if (sharedPrefsManager.getBooleanValue(AppConstantsManager.VIEW_DIRECTION_THROUGH_COMPASS, false)) {
            activateCompass();
        } else {
            deactivateCompass();
        }


//            ProjectDocuShowPlanFragment newFragment = new ProjectDocuShowPlanFragment(null,0,0,0);
//            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
//            ft.add(R.id.content_frame, newFragment).addToBackStack("ProjectDocuShowPlanFragment").commit();

        Button button = findViewById(R.id.btn_add);
        button.setVisibility(View.GONE);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        new RetrievePlansReferPointAsyncTask(this, getIntent().getStringExtra("projectId"), getIntent().getStringExtra("planId")).execute();
//        try {
//            latch.wait();
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }

        if (sharedPrefsManager.getBooleanValue(AppConstantsManager.ACTIVATE_GPS, false)) {
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
        } else {
            locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

            locationManager.addGpsStatusListener(this);
            boolean statusOfGPS = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);

            if (locationManager.getBestProvider(createCoarseCriteria(), statusOfGPS) != null && locationManager.getBestProvider(createFineCriteria(), statusOfGPS) != null) {
                low = locationManager.getProvider(locationManager.getBestProvider(createCoarseCriteria(), statusOfGPS));

                high = locationManager.getProvider(locationManager.getBestProvider(createFineCriteria(), statusOfGPS));

            } else {
                Toast.makeText(this, "Location Provider is not available.", Toast.LENGTH_LONG).show();

            }

            if (low != null) {
                locationManager.requestLocationUpdates(low.getName(), 0, 0, this);
            }

            if (high != null) {
                locationManager.requestLocationUpdates(high.getName(), 0, 0, this);
            }
        }
    }

    public void activateCompass() {
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);

        if (sensorManager != null) {
            sensorManager.registerListener(this, sensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION), SensorManager.SENSOR_DELAY_GAME);
        }

        activateCompassButton();
    }

    public void deactivateCompass() {
        if (sensorManager != null) {
            sensorManager.unregisterListener(this);
        }

        if (newFragment != null) {
            newFragment.deActivateCompassButton();
        }

    }

    public void activateCompassButton() {
        if (newFragment != null) {
            newFragment.activateCompassButton();
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

    public boolean checkAndRequestPermissions() {

        List<String> listPermissionsNeeded = new ArrayList<>();
        for (String perm : appPermissions) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && ContextCompat.checkSelfPermission(this, perm) != PackageManager.PERMISSION_GRANTED) {
                // Permission is not granted
                if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                        perm)) {
//                    Toast.makeText(this, "The app needs more permissions! Please activate these in the system settings of the mobile phone!", Toast.LENGTH_LONG).show();
                } else {
                    // No explanation needed; request the permission
                    listPermissionsNeeded.add(perm);
                }
            }
        }
        if (!listPermissionsNeeded.isEmpty()) {
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
            locationManager.removeGpsStatusListener(this);
            locationManager.removeUpdates(this);
        }

//        deactivateGPSButton();

//        if (gps_button_animation != null) {
        // gps_button_animation.stop();
//        }
    }


    @Override
    public void onGpsStatusChanged(int event) {

        if (event == GpsStatus.GPS_EVENT_STARTED || event == GpsStatus.GPS_EVENT_STOPPED) {
            if (sharedPrefsManager.getBooleanValue(AppConstantsManager.ACTIVATE_GPS, false) && event == GpsStatus.GPS_EVENT_STARTED) {

            } else {
                if (newFragment != null) {

                    newFragment.deactivateGPSButton();
                }
            }
        }

    }

    @Override
    public void onLocationChanged(Location location) {
        gpsLocation = location;
        if (newFragment != null && newFragment instanceof PhotoAddDirectionFragment2Testing) {

            if (sharedPrefsManager.getBooleanValue(AppConstantsManager.ACTIVATE_GPS, false)) {
                activateGPS();
            } else {
                deactivateGPS();
            }
            if (referPointList == null) {
//                Toast toast = Toast.makeText(this, "referPointList null", Toast.LENGTH_SHORT);
//                toast.show();
                return;
            }
            if (sharedPrefsManager.getBooleanValue(AppConstantsManager.ACTIVATE_GPS, false) && newFragment != null && newFragment.isViewCreated && referPointList != null && referPointList.size() > 0) {
                newFragment.activateGPSButton();
            } else {
                if (newFragment != null && newFragment.isViewCreated)
                    newFragment.deactivateGPSButton();
            }
            if (sharedPrefsManager.getBooleanValue(AppConstantsManager.ACTIVATE_GPS, false) && newFragment != null && newFragment.isViewCreated && referPointList != null && referPointList.size() > 0 && sharedPrefsManager.getBooleanValue(AppConstantsManager.ACTIVATE_GPS_MANUAL_STATE, false)) {
                newFragment.setPlanPositionByGps(gpsLocation);
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
        Utils.showLogger2("PhotoAddDirectionMainActivity2Testing");
    }

    @Override
    public void onSensorChanged(SensorEvent event) {

        compassDegrees = event.values[0];

        if (sharedPrefsManager.getBooleanValue(AppConstantsManager.VIEW_DIRECTION_THROUGH_COMPASS, false) && newFragment != null && newFragment.isViewCreated && referPointList != null && referPointList.size() > 0) {
            newFragment.activateCompassButton();
        } else {
            if (newFragment != null && newFragment.isViewCreated)
                newFragment.deActivateCompassButton();
        }

        if (sharedPrefsManager.getBooleanValue(AppConstantsManager.VIEW_DIRECTION_THROUGH_COMPASS, false) && newFragment != null && newFragment.isViewCreated && referPointList != null && referPointList.size() > 0 && sharedPrefsManager.getBooleanValue(AppConstantsManager.VIEW_DIRECTION_THROUGH_COMPASS_MANUAL_STATE, false)) {
            newFragment.setArrowByCompass(compassDegrees);
        }

    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }


    @Override
    public void onBackPressed() {
        Intent intent = new Intent("updateFlawFlag");
        intent.putExtra(SavePictureActivity.FINISH_PLAN_SCREEN_KEY, true);
        sendBroadcast(intent);
        finish();
    }

    private class RetrievePlansReferPointAsyncTask extends AsyncTask<Void, Void, Void> {
        private ReferPointPlansDao mAsyncTaskDao;
        List<ReferPointJSONPlanModel> stringList = new ArrayList<>();
        ProjectsDatabase projectsDatabase;
        String projectId;
        String planID;

        RetrievePlansReferPointAsyncTask(Context context, String project_id, String planId) {
            projectsDatabase = ProjectsDatabase.getDatabase(context);
            mAsyncTaskDao = projectsDatabase.referPointPlansDao();
            projectId = project_id;
            planID = planId;
        }

        @Override
        protected Void doInBackground(final Void... params) {

            referPointList = mAsyncTaskDao.getReferPointList(projectId, planID);
//            latch.countDown();
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
//            newFragment = new PhotoAddDirectionFragment2Testing(getIntent().getParcelableExtra("flawFlagObj"), getIntent().getStringExtra("projectId"), getIntent().getStringExtra("planId"), String.valueOf(getIntent().getLongExtra("photoId", 0)), getIntent().getBooleanExtra("fromPhoto", false));
//            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
//            ft.add(R.id.content_frame, newFragment).addToBackStack("PhotoAddDirectionFragment").commit();

                SpecificDefectAddDirectionFragment newFragment = new SpecificDefectAddDirectionFragment(getIntent().getParcelableExtra("flawFlagObj"), getIntent().getStringExtra("projectId"), getIntent().getStringExtra("planId"), String.valueOf(getIntent().getStringExtra("photoId")), getIntent().getBooleanExtra("fromPhoto", false));
                FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
                ft.add(R.id.content_frame, newFragment).addToBackStack("SpecificDefectAddDirectionFragment").commit();

        }
    }

}
