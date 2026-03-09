package com.projectdocupro.mobile.fragments.add_direction;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.MutableLiveData;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.projectdocupro.mobile.ProjectsDatabase;
import com.projectdocupro.mobile.R;
import com.projectdocupro.mobile.activities.SavePictureActivity;
import com.projectdocupro.mobile.compass.Compass;
import com.projectdocupro.mobile.dao.ReferPointPlansDao;
import com.projectdocupro.mobile.managers.AppConstantsManager;
import com.projectdocupro.mobile.managers.SharedPrefsManager;
import com.projectdocupro.mobile.models.ReferPointJSONPlanModel;
import com.projectdocupro.mobile.service.GPSTracker;
import com.projectdocupro.mobile.utility.Utils;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;


public class PhotoAddDirectionMainActivity extends AppCompatActivity implements LocationListener, SensorEventListener {
    private static final String IS_ROTATE = "is_rotate";
    //    public static ProjectDocuMainActivity projectDocuMainActivity = null;
    private LocationManager locationManager = null;
    public Location gpsLocation = null;
    private LocationProvider low = null;
    private LocationProvider high = null;
    private SharedPrefsManager sharedPrefsManager;
    public PhotoAddDirectionFragment newFragment;
    Toolbar toolbar;
    private SensorManager sensorManager;
    public float compassDegrees;
    String planName = "";
    public List<ReferPointJSONPlanModel> referPointList;
    CountDownLatch latch = new CountDownLatch(1);
    public int actionBarHeight;
    private int deviceOrientation;
    private String TAG = PhotoAddDirectionMainActivity.class.getCanonicalName();
    private boolean portraitPitch;
    private boolean landscapePitch;
    private boolean portraitRoll;
    private boolean portrait;
    private boolean landscape;
    private boolean landscapeRoll;
    public GPSTracker gpsTracker;

    // private MYORIENTATION myorientation;

    private int mAzimuth = 0; // degree

    public MutableLiveData<Location> liveLocation = new MutableLiveData<>();


    private Sensor mAccelerometer;
    private Sensor mMagnetometer;

    boolean haveAccelerometer = false;
    boolean haveMagnetometer = false;

    private Sensor mGravity;
    boolean haveGravity = false;
    private Compass compass;
    private boolean ifAppRestart = false;
    public int lastArrowAngle;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private LocationCallback locationCallback;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_direction_with_frag);

        Utils.showLogger("PhotoAddDirectionMainActivity OnCreate");

        //projectDocuMainActivity = this;
        sharedPrefsManager = new SharedPrefsManager(this);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setVisibility(View.GONE);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);


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

        // sharedPrefsManager.setBooleanValue(AppConstantsManager.ACTIVATE_GPS_MANUAL_STATE, false);
        // sharedPrefsManager.setBooleanValue(AppConstantsManager.VIEW_DIRECTION_THROUGH_COMPASS_MANUAL_STATE, false);


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

   /*     if (sharedPrefsManager.getBooleanValue(AppConstantsManager.ACTIVATE_GPS, false)) {
            activateGPS();//OnCreate
        } else {
            deactivateGPS();
        }*/

   /*     new OrientationEventListener(this) {
            @Override
            public void onOrientationChanged(int orientation) {
//                Log.d(TAG, "Orientation New: " + orientation);




                int currentOrientation = getWindowManager().getDefaultDisplay().getRotation();

                switch (currentOrientation) {
                    case 0:
                        //. SCREEN_ORIENTATION_PORTRAIT
                        Log.d(TAG, "Orientation New: Portrait");
                        myorientation = MYORIENTATION.PORTRAIT;
                        break;
                    //----------------------------------------
                    case 2:
                        //. SCREEN_ORIENTATION_REVERSE_PORTRAIT
                        Log.d(TAG, "Orientation New: Reverse_Portrait");
                        myorientation = MYORIENTATION.PORTRAIT;
                        break;
                    //----------------------------------------
                    case 1:
                        //. SCREEN_ORIENTATION_LANDSCAPE
                        Log.d(TAG, "Orientation New: Landscape");
                        myorientation = MYORIENTATION.LANDSCAPE;
                        break;
                    //----------------------------------------
                    case 3:
                        //. SCREEN_ORIENTATION_REVERSE_LANDSCAPE
                        Log.d(TAG, "Orientation New: Reverse_Landscape");
                        myorientation = MYORIENTATION.REVERSE_LANDSCAPE;
                        break;
                    //----------------------------------------
                }

//                deviceOrientation = orientation;
            }
        }.enable();*/

        setupCompass();

    }

    @SuppressLint("MissingPermission")
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


/*
            locationManager.registerGnssStatusCallback(new GnssStatus.Callback() {
                @Override
                public void onStarted() {
                    super.onStarted();

                    if (newFragment != null) {

                        newFragment.deactivateGPSButton();
                    }
                }

                @Override
                public void onStopped() {
                    super.onStopped();

                    if (newFragment != null) {

                        newFragment.deactivateGPSButton();
                    }
                }

                @Override
                public void onFirstFix(int ttffMillis) {
                    super.onFirstFix(ttffMillis);
                }

                @Override
                public void onSatelliteStatusChanged(@NonNull GnssStatus status) {
                    super.onSatelliteStatusChanged(status);
                }
            });


            locationManager.registerGnssStatusCallback(new GnssStatus.Callback() {
                @Override
                public void onStarted() {
                    super.onStarted();
                    if (newFragment != null) {

                        newFragment.deactivateGPSButton();
                    }
                }

                @Override
                public void onStopped() {
                    super.onStopped();

                    if (newFragment != null) {

                        newFragment.deactivateGPSButton();
                    }
                }

                @Override
                public void onFirstFix(int ttffMillis) {
                    super.onFirstFix(ttffMillis);
                }

                @Override
                public void onSatelliteStatusChanged(@NonNull GnssStatus status) {
                    super.onSatelliteStatusChanged(status);


                }
            });
*/


            boolean statusOfGPS = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);

            if (locationManager.getBestProvider(createCoarseCriteria(), statusOfGPS) != null && locationManager.getBestProvider(createFineCriteria(), statusOfGPS) != null) {
                low = locationManager.getProvider(locationManager.getBestProvider(createCoarseCriteria(), statusOfGPS));

                high = locationManager.getProvider(locationManager.getBestProvider(createFineCriteria(), statusOfGPS));

            } else {
                Toast.makeText(this, "Location Provider is not available.", Toast.LENGTH_LONG).show();

            }

            if (low != null) {
                locationManager.requestLocationUpdates(low.getName(), 1000, 0, this);
            }

            if (high != null) {
                locationManager.requestLocationUpdates(high.getName(), 1000, 0, this);
            }

            fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);


        }
    }


    public void registersSensors() {
        try {
            if (sensorManager != null) {
                sensorManager.registerListener(this, sensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION), SensorManager.SENSOR_DELAY_GAME);


            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void activateCompass() {
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);


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
            Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION

    };

    public boolean checkAndRequestPermissions() {

        List<String> listPermissionsNeeded = new ArrayList<>();
        for (String perm : appPermissions) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && ContextCompat.checkSelfPermission(this, perm) != PackageManager.PERMISSION_GRANTED) {
                // Permission is not granted
                if (ActivityCompat.shouldShowRequestPermissionRationale(this, perm)) {
//                    Toast.makeText(this, "The app needs more permissions! Please activate these in the system settings of the mobile phone!", Toast.LENGTH_LONG).show();
                } else {
                    // No explanation needed; request the permission
                    listPermissionsNeeded.add(perm);
                }
            }
        }
        if (!listPermissionsNeeded.isEmpty()) {
            ActivityCompat.requestPermissions(this, listPermissionsNeeded.toArray(new String[listPermissionsNeeded.size()]), PERMISSION_REQUEST_CODE);
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
        Utils.showLogger2("locationAccuracy>>" + location.getAccuracy());
        onLocationChangedHandle(location);

    }

    private void onLocationChangedHandle(Location location) {

        Utils.showLogger2("onLocationChanged>>" + location.getAccuracy());

        gpsLocation = location;
        if (gpsTracker != null) gpsTracker.location = location;
//                             if(gpsLocation!=null) {
//                                 Toast.makeText(this, "LatLng"+gpsLocation.getLatitude()+" "+gpsLocation.getLongitude(), Toast.LENGTH_SHORT).show();
//                             }else{
//                                 Toast.makeText(this, "LatLng not found", Toast.LENGTH_SHORT).show();
//
//                             }

        if (newFragment != null) {

/*            if (sharedPrefsManager.getBooleanValue(AppConstantsManager.ACTIVATE_GPS, false)) {
                activateGPS();
            } else {
                deactivateGPS();
            }*/

            if (sharedPrefsManager.getBooleanValue(AppConstantsManager.ACTIVATE_GPS, false) && newFragment != null && newFragment.isViewCreated && referPointList != null && referPointList.size() > 0) {
                newFragment.activateGPSButton2();
            } else {
                if (newFragment != null && newFragment.isViewCreated)
                    newFragment.deactivateGPSButton();
            }
            if (sharedPrefsManager.getBooleanValue(AppConstantsManager.ACTIVATE_GPS, false) && newFragment != null && newFragment.isViewCreated && referPointList != null && referPointList.size() > 0) {
//                                                 Toast.makeText(this, "setPlanPositionByGps"+gpsLocation.getLatitude()+" "+gpsLocation.getLongitude(), Toast.LENGTH_SHORT).show();
                if (sharedPrefsManager.getBooleanValue(AppConstantsManager.ACTIVATE_GPS, false)) {
                    if (sharedPrefsManager.getBooleanValue(AppConstantsManager.ACTIVATE_GPS_MANUAL_STATE, false)) {
                        newFragment.setPlanPositionByGps(gpsLocation);
                    }
                    //liveLocation.postValue(gpsLocation);
                }
            }

            if (referPointList == null) {
//                Toast toast = Toast.makeText(this, "referPointList null", Toast.LENGTH_SHORT);
//                toast.show();
                return;
            }
        } else {
            Utils.showLogger("newFragmentNull");
        }
        gpsTracker = new GPSTracker(PhotoAddDirectionMainActivity.this);
        gpsTracker.location = location;
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

    }

    @Override
    public void onSensorChanged(SensorEvent values) {

        // compassDegrees = values.values[0];

        String testOrientation = "";


        Log.d(TAG, "compassDegrees NEW: " + testOrientation);
        Log.d(TAG, "compassDegrees NEW: " + compassDegrees + "");


        // Movement
        float azimuth = values.values[0];
        float pitch = values.values[1];
        float roll = values.values[2];

        if ((-110 <= pitch && pitch <= -70) || (70 <= pitch && pitch <= 110)) {
            //PORTRAIT MODE
            portraitPitch = true;
            landscapePitch = false;
            Log.d(TAG, "portrait mode: pitch = " + pitch);
        } else if ((-20 <= pitch && pitch <= 20) || (-200 <= pitch && pitch <= -160) || (160 <= pitch && pitch <= 200)) {
            //LANDSCAPE MODE
            portraitPitch = false;
            landscapePitch = true;
            Log.d(TAG, "landscape mode : pitch = " + pitch);
        }

        if ((-20 <= roll && roll <= 20)) {
            //PORTRAIT MODE
            portraitRoll = true;
            landscapePitch = false;
            Log.d(TAG, "portrait mode: roll = " + roll);
//            compassDegrees=compassDegrees-90;
        } else if ((-110 <= roll && roll <= -70) || (70 <= roll && roll <= 110)) {
            //LANDSCAPE MODE
            portraitRoll = false;
            landscapePitch = true;
            Log.d(TAG, "landscape mode : roll = " + roll);
        }

        if (portraitPitch && portraitRoll && !portrait) {
            portrait = true;
            landscape = false;
//            rotateIconsToPortraitMode();
//            compassDegrees=compassDegrees-90;
            Log.d(TAG, "portrait mode for icons: pitch = " + pitch + ", roll = " + roll);
        }

        if (landscapePitch && landscapeRoll && !landscape) {
            landscape = true;
            portrait = false;
//            rotateIconsToLandscapeMode();
            Log.d(TAG, "landscape mode for icons: pitch = " + pitch + ", roll = " + roll);
        }
        if (sharedPrefsManager.getBooleanValue(AppConstantsManager.VIEW_DIRECTION_THROUGH_COMPASS, false) && newFragment != null && newFragment.isViewCreated && referPointList != null && referPointList.size() > 0) {
            newFragment.activateCompassButton();
        } else {
            if (newFragment != null && newFragment.isViewCreated)
                newFragment.deActivateCompassButton();
        }


    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        Log.e("", "");
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
            try {
                Thread.sleep(400);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            referPointList = mAsyncTaskDao.getReferPointList(projectId, planID);
//            latch.countDown();
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

            getSupportFragmentManager().popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);

            newFragment = new PhotoAddDirectionFragment(getIntent().getParcelableExtra("flawFlagObj"), getIntent().getStringExtra("projectId"), getIntent().getStringExtra("planId"), String.valueOf(getIntent().getLongExtra("photoId", 0)), getIntent().getBooleanExtra("fromPhoto", false));

            newFragment.lastFragmentArrowAngle = lastArrowAngle;
            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            ft.add(R.id.content_frame, newFragment).addToBackStack("PhotoAddDirectionFragment").commit();
            //     ft.replace(R.id.content_frame, newFragment).commit();
            Utils.showLogger("adding direction fragment");
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        // Checks the orientation of the screen
        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
//            Toast.makeText(this, "landscape", Toast.LENGTH_SHORT).show();
            deviceOrientation = newConfig.orientation;
        } else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
//            Toast.makeText(this, "portrait", Toast.LENGTH_SHORT).show();
            deviceOrientation = newConfig.orientation;
        }
    }


    @Override
    protected void onPause() {
        super.onPause();
        compass.stop();

        try {
            if (fusedLocationProviderClient != null)
                fusedLocationProviderClient.removeLocationUpdates(locationCallback);
        } catch (Exception e) {

        }

    }


    @Override
    protected void onResume() {
        super.onResume();
        // registersSensors();
        //compassManager.onResume();
        compass.start();

        if (sharedPrefsManager.getBooleanValue(AppConstantsManager.ACTIVATE_GPS, false))
            startLocationListener();
    }

    private void startLocationListener() {
        Utils.showLogger2("startLocationListener");

        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                Utils.showLogger2("onLocationResult");
                if (locationResult != null) {
                    if (locationResult == null) {
                        return;
                    }
                    //Showing the latitude, longitude and accuracy on the home screen.
                    for (Location location : locationResult.getLocations()) {
                        onLocationChangedHandle(location);

                    }
                }
            }
        };

        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setInterval(1000);
        locationRequest.setFastestInterval(1000);

        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        gpsTracker = new GPSTracker(this);


        try {
            if (fusedLocationProviderClient == null)
                fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
            fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper());

        } catch (Exception e) {
            e.printStackTrace();
            Utils.showLogger2("Excepiton>>" + e.getMessage());
        }
    }


    private void setupCompass() {


        compass = new Compass(this);
        Compass.CompassListener cl = getCompassListener();
        compass.setListener(cl);


    }

    private Compass.CompassListener getCompassListener() {
        return new Compass.CompassListener() {
            @Override
            public void onNewAzimuth(final float azimuth) {
                // UI updates only in UI thread
                // https://stackoverflow.com/q/11140285/444966
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        int aziF = Utils.floatToLowerInt(azimuth);
//                        Utils.showLogger("myOrientation>>>"+myorientation);

                        compassDegrees = aziF;


                        if (sharedPrefsManager.getBooleanValue(AppConstantsManager.VIEW_DIRECTION_THROUGH_COMPASS, false) && newFragment != null && newFragment.isViewCreated && referPointList != null && referPointList.size() > 0 && sharedPrefsManager.getBooleanValue(AppConstantsManager.VIEW_DIRECTION_THROUGH_COMPASS_MANUAL_STATE, false)) {
                            newFragment.setArrowByCompass(compassDegrees);
                        }

                    }
                });
            }

            @Override
            public void onAccuracyCorrect() {

            }

            @Override
            public void onAccuracyWrong() {

            }
        };
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        //getSupportFragmentManager().popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
        //newFragment = null;
        outState.putBoolean(IS_ROTATE, true);
        super.onSaveInstanceState(outState);

    }

    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        if (savedInstanceState.getBoolean(IS_ROTATE)) {
            ifAppRestart = true;
        }
    }
}
