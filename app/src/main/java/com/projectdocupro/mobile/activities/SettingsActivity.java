package com.projectdocupro.mobile.activities;

import static com.projectdocupro.mobile.managers.AppConstantsManager.AUTO_LOAD_LAST_PROJECT_DEFAULT_VALUE;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.Toolbar;

import androidx.appcompat.app.AppCompatActivity;

import com.projectdocupro.mobile.R;
import com.projectdocupro.mobile.adapters.ProjectDocuAutoGpsDistanceSpinnerAdapter;
import com.projectdocupro.mobile.adapters.ProjectDocuCameraResolutionSpinnerAdapter;
import com.projectdocupro.mobile.managers.AppConstantsManager;
import com.projectdocupro.mobile.managers.SharedPrefsManager;
import com.projectdocupro.mobile.utility.Utils;



public class SettingsActivity extends AppCompatActivity {

    SharedPrefsManager sharedPrefsManager;

    private Toolbar toolbar;

    private Switch activateGps;

    private Switch gpsOnlyGeoRef;

    private Switch viewDirectionCompass;

    private Switch autoLoadLastProject;

    private Switch autoLoadLastPlan;

    private Switch syncPhotosMobNet;

    private Switch syncPhotosBeforeMobNet;

    private Switch syncPhotosWLan;

    private Switch syncPhotosBeforeWLan;

    private Switch reloadOriginalImageOnWLan;

    private Switch allowBackgroundSync;

    private Switch firstLoadNewest;

    private Switch buzzwordFavoritesPreview;

    private Switch sync_minimize_photos_via_mobile_net;

    private Switch sync_photos_ascending;

    private LinearLayout ll_parent;
    private View rowView;
    private Spinner settingsSpinnerView;
    private View mSyncPhotosAscending;
    private View mActivateGps;
    private View mGpsOnlyGeoReferenced;
    private View mSyncMinimizePhotosViaMobileNet;
    private View mViewDirectionCompass;
    private View mAutoLoadLastProject;
    private View mAutoLoadLastPlan;
    private View mSyncPhotosOnMobileNet;
    private View mSyncPhotosBeforeOnMobileNet;
    private View mSyncPhotosOnWlan;
    private View mSyncPhotosBeforeOnWlan;
    private View mReloadImagesOriginalWlan;
    private View mAllowSyncBackground;
    private View mFirstLoadNewest;
    private View mBuzzwordFavoritesInPreview;
    private View mCameraResolution;
    private View mGpsScale;
    private View mLogout;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        bindView();
        sharedPrefsManager = new SharedPrefsManager(this);

        initSwitched();
        toolbar.setNavigationOnClickListener(view -> onBackPressed());

        applyOnCheckChangeListeners();

    }


    private void initSwitched() {
        activateGps.setChecked(sharedPrefsManager.getBooleanValue(AppConstantsManager.ACTIVATE_GPS, true));
        gpsOnlyGeoRef.setChecked(sharedPrefsManager.getBooleanValue(AppConstantsManager.FIND_POSITION_IF_GEO_REFERENCED_PLAN, false));
        viewDirectionCompass.setChecked(sharedPrefsManager.getBooleanValue(AppConstantsManager.VIEW_DIRECTION_THROUGH_COMPASS, true));
        autoLoadLastProject.setChecked(sharedPrefsManager.getBooleanValue(AppConstantsManager.AUTO_LOAD_LAST_PROJECT, AUTO_LOAD_LAST_PROJECT_DEFAULT_VALUE));
        autoLoadLastPlan.setChecked(sharedPrefsManager.getBooleanValue(AppConstantsManager.AUTO_LOAD_LAST_PLAN, true));
        syncPhotosMobNet.setChecked(sharedPrefsManager.getBooleanValue(AppConstantsManager.SYNC_PHOTOS_MOBILE_DATA, true));
        syncPhotosBeforeMobNet.setChecked(sharedPrefsManager.getBooleanValue(AppConstantsManager.SYNC_PHOTOS_BEFORE_SENDING_OVER_MOBILE_DATA, false));
        syncPhotosWLan.setChecked(sharedPrefsManager.getBooleanValue(AppConstantsManager.SYNC_PHOTOS_WLAN, true));
        syncPhotosBeforeWLan.setChecked(sharedPrefsManager.getBooleanValue(AppConstantsManager.SYNC_PHOTOS_BEFORE_SENDING_OVER_WLAN, false));
        reloadOriginalImageOnWLan.setChecked(sharedPrefsManager.getBooleanValue(AppConstantsManager.RELOAD_ORIGINAL_IMAGES_WLAN, false));
        allowBackgroundSync.setChecked(sharedPrefsManager.getBooleanValue(AppConstantsManager.ALLOW_BACKGROUND_SYNC, true));
        firstLoadNewest.setChecked(sharedPrefsManager.getBooleanValue(AppConstantsManager.FIRST_LOAD_NEWEST_IMAGES, false));
        buzzwordFavoritesPreview.setChecked(sharedPrefsManager.getBooleanValue(AppConstantsManager.BUZZWORD_FAVORITES_VIEW_PREVIEW, false));
        sync_minimize_photos_via_mobile_net.setChecked(sharedPrefsManager.getBooleanValue(AppConstantsManager.MINIMIZE_PHOTO_VIA_MOBILE_DATA, false));


/*        if (!sharedPrefsManager.getBooleanValue(AppConstantsManager.ALLOW_BACKGROUND_SYNC, false)) {
            allowBackgroundSync.setChecked(false);
            syncPhotosMobNet.setEnabled(false);
            syncPhotosWLan.setEnabled(false);
        }*/

        if (!sharedPrefsManager.getBooleanValue(AppConstantsManager.ACTIVATE_GPS, false)) {
            activateGps.setChecked(false);
            gpsOnlyGeoRef.setEnabled(false);

        }

        if (sharedPrefsManager.getBooleanValue(AppConstantsManager.SYNC_PHOTO_ASC_ORDER, AppConstantsManager.SYNC_PHOTO_ASC_ORDER_DEFAULT_VALUE)) {
            sync_photos_ascending.setChecked(true);
        } else {


            sync_photos_ascending.setChecked(false);
        }
        addGpsAccuracyView();
        addCameraResolutionView();

    }

    public void addGpsAccuracyView() {
        LayoutInflater inflater = this.getLayoutInflater();


        rowView = inflater.inflate(R.layout.project_docu_settings_auto_gps_distance_list_spinner_entry, null, true);

        String[] autoGpsDistanceSpinnerStrings = new String[8];
        autoGpsDistanceSpinnerStrings[0] = new String("off");
        autoGpsDistanceSpinnerStrings[1] = new String("5m");
        autoGpsDistanceSpinnerStrings[2] = new String("10m");
        autoGpsDistanceSpinnerStrings[3] = new String("15m");
        autoGpsDistanceSpinnerStrings[4] = new String("20m");
        autoGpsDistanceSpinnerStrings[5] = new String("30m");
        autoGpsDistanceSpinnerStrings[6] = new String("50m");
        autoGpsDistanceSpinnerStrings[7] = new String("100m");

        ProjectDocuAutoGpsDistanceSpinnerAdapter projectDocuAutoGpsDistanceSpinnerAdapter = new ProjectDocuAutoGpsDistanceSpinnerAdapter(this, autoGpsDistanceSpinnerStrings);

        settingsSpinnerView = (Spinner) rowView.findViewById(R.id.settings_spinner_auto_gps_distance);
        settingsSpinnerView.setAdapter(projectDocuAutoGpsDistanceSpinnerAdapter);
        settingsSpinnerView.setOnItemSelectedListener(projectDocuAutoGpsDistanceSpinnerAdapter);


        SharedPrefsManager projectDocuDatabaseManager = new SharedPrefsManager(this);

        String selectedAutoGpsDistance = projectDocuDatabaseManager.getGpsAccuracy(this);

        if (selectedAutoGpsDistance != null) {
            int selectedSpinnerPosition = projectDocuAutoGpsDistanceSpinnerAdapter.getPosition(selectedAutoGpsDistance);
            settingsSpinnerView.setSelection(selectedSpinnerPosition);
        }

        sharedPrefsManager.setStringValue(AppConstantsManager.USER_SELECTED_LOCATION_ACCURACY, selectedAutoGpsDistance);
        ll_parent.addView(rowView, 4);

        if (!sharedPrefsManager.getBooleanValue(AppConstantsManager.ACTIVATE_GPS, false)) {
            rowView.setAlpha(0.5f);
            settingsSpinnerView.setEnabled(false);
        } else {
            settingsSpinnerView.setEnabled(sharedPrefsManager.getBooleanValue(AppConstantsManager.FIND_POSITION_IF_GEO_REFERENCED_PLAN, false));
        }
    }


    public void addCameraResolutionView() {
        LayoutInflater inflater = this.getLayoutInflater();
        rowView = inflater.inflate(R.layout.project_docu_settings_camera_resolution_list_spinner_entry, null, true);

        String[] autoGpsDistanceSpinnerStrings = new String[7];
        autoGpsDistanceSpinnerStrings[0] = new String(getResources().getString(R.string.settings_resolution));
        autoGpsDistanceSpinnerStrings[1] = new String("50%");
        autoGpsDistanceSpinnerStrings[2] = new String("60%");
        autoGpsDistanceSpinnerStrings[3] = new String("70%");
        autoGpsDistanceSpinnerStrings[4] = new String("80%");
        autoGpsDistanceSpinnerStrings[5] = new String("90%");
        autoGpsDistanceSpinnerStrings[6] = new String("100%");

        ProjectDocuCameraResolutionSpinnerAdapter projectDocuAutoGpsDistanceSpinnerAdapter = new ProjectDocuCameraResolutionSpinnerAdapter(this, autoGpsDistanceSpinnerStrings);

        SharedPrefsManager projectDocuDatabaseManager = new SharedPrefsManager(this);

        String selectedAutoGpsDistance = projectDocuDatabaseManager.getStringValue(AppConstantsManager.USER_SELECTED_CAMERA_RESOLUTION, "100");

        Spinner settingsSpinnerResolutionView = (Spinner) rowView.findViewById(R.id.settings_spinner_auto_gps_distance);

        settingsSpinnerResolutionView.setAdapter(projectDocuAutoGpsDistanceSpinnerAdapter);
        settingsSpinnerResolutionView.setOnItemSelectedListener(projectDocuAutoGpsDistanceSpinnerAdapter);

        if (!selectedAutoGpsDistance.equals(autoGpsDistanceSpinnerStrings[0])) {
            selectedAutoGpsDistance = selectedAutoGpsDistance + "%";
            int selectedSpinnerPosition = projectDocuAutoGpsDistanceSpinnerAdapter.getPosition(selectedAutoGpsDistance);
            settingsSpinnerResolutionView.setSelection(selectedSpinnerPosition);
        } else {
            settingsSpinnerResolutionView.setSelection(0);
        }
        ll_parent.addView(rowView);

    }


    void applyOnCheckChangeListeners(){
        sync_photos_ascending.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isCheck) {
                sharedPrefsManager.setBooleanValue(AppConstantsManager.SYNC_PHOTO_ASC_ORDER, isCheck);
            }
        });

        activateGps.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean check) {
                sharedPrefsManager.setBooleanValue(AppConstantsManager.ACTIVATE_GPS, check);
                activateGps.setChecked(check);
                gpsOnlyGeoRef.setEnabled(check);
                gpsOnlyGeoRef.setChecked(false);

                sharedPrefsManager.setBooleanValue(AppConstantsManager.VIEW_DIRECTION_THROUGH_COMPASS, check);

                if (settingsSpinnerView != null) {
                    if (check) {
                      //  rowView.setAlpha(1f);
                        settingsSpinnerView.setEnabled(true);

                    } else {
                       // rowView.setAlpha(0.5f);
                        settingsSpinnerView.setEnabled(false);
                        gpsOnlyGeoRef.setChecked(false);
                    }
                }
            }
        });

        gpsOnlyGeoRef.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean check) {
                sharedPrefsManager.setBooleanValue(AppConstantsManager.FIND_POSITION_IF_GEO_REFERENCED_PLAN, check);

                if (settingsSpinnerView != null) {
                    if (check) {
                     //   rowView.setAlpha(1f);
                        settingsSpinnerView.setEnabled(true);
                    } else {
                      //  rowView.setAlpha(0.5f);
                        settingsSpinnerView.setEnabled(false);
                    }
                }
            }
        });


        sync_minimize_photos_via_mobile_net.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean check) {
                sharedPrefsManager.setBooleanValue(AppConstantsManager.MINIMIZE_PHOTO_VIA_MOBILE_DATA, check);

            }
        });


        viewDirectionCompass.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean check) {
                sharedPrefsManager.setBooleanValue(AppConstantsManager.VIEW_DIRECTION_THROUGH_COMPASS, check);

            }
        });


        autoLoadLastProject.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean check) {
                sharedPrefsManager.setBooleanValue(AppConstantsManager.AUTO_LOAD_LAST_PROJECT, check);

            }
        });

        autoLoadLastPlan.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean check) {
                sharedPrefsManager.setBooleanValue(AppConstantsManager.AUTO_LOAD_LAST_PLAN, check);

            }
        });

        syncPhotosMobNet.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean check) {
                sharedPrefsManager.setBooleanValue(AppConstantsManager.SYNC_PHOTOS_MOBILE_DATA, check);

            }
        });

        syncPhotosBeforeMobNet.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean check) {
                sharedPrefsManager.setBooleanValue(AppConstantsManager.SYNC_PHOTOS_BEFORE_SENDING_OVER_MOBILE_DATA, check);

            }
        });

        syncPhotosWLan.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean check) {
                sharedPrefsManager.setBooleanValue(AppConstantsManager.SYNC_PHOTOS_WLAN, check);

            }
        });

        syncPhotosBeforeWLan.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean check) {
                sharedPrefsManager.setBooleanValue(AppConstantsManager.SYNC_PHOTOS_BEFORE_SENDING_OVER_WLAN, check);

            }
        });

        reloadOriginalImageOnWLan.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean check) {
                sharedPrefsManager.setBooleanValue(AppConstantsManager.RELOAD_ORIGINAL_IMAGES_WLAN, check);

            }
        });

        allowBackgroundSync.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean check) {
                if (check) {
                    syncPhotosMobNet.setEnabled(true);
                    syncPhotosWLan.setEnabled(true);
                } else {
                    syncPhotosMobNet.setEnabled(false);
                    syncPhotosWLan.setEnabled(false);
                }
                sharedPrefsManager.setBooleanValue(AppConstantsManager.ALLOW_BACKGROUND_SYNC, check);

            }
        });

        firstLoadNewest.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean check) {
                sharedPrefsManager.setBooleanValue(AppConstantsManager.FIRST_LOAD_NEWEST_IMAGES, check);

            }
        });

        buzzwordFavoritesPreview.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean check) {
                sharedPrefsManager.setBooleanValue(AppConstantsManager.BUZZWORD_FAVORITES_VIEW_PREVIEW, check);

            }
        });
    }




    private void onCameraResolutionClick() {

    }

    private void onGpsScaleClick() {

    }

    private void onLogoutClick() {
        SharedPrefsManager sharedPrefsManager = new SharedPrefsManager(this);
        sharedPrefsManager.setStringValue(AppConstantsManager.PD_USER_ID, "");
        sharedPrefsManager.setStringValue(AppConstantsManager.USER_EMAIL, "");
        sharedPrefsManager.setStringValue(AppConstantsManager.USER_CREATED, "");
        sharedPrefsManager.setStringValue(AppConstantsManager.USER_LAST_LOGIN, "");
        sharedPrefsManager.setStringValue(AppConstantsManager.USER_ACTIVE, "");
        sharedPrefsManager.setStringValue(AppConstantsManager.USER_STATUS, "");
        sharedPrefsManager.setStringValue(AppConstantsManager.USER_GENDER, "");
        sharedPrefsManager.setStringValue(AppConstantsManager.USER_TITLE, "");
        sharedPrefsManager.setStringValue(AppConstantsManager.USER_FIRST_NAME, "");
        sharedPrefsManager.setStringValue(AppConstantsManager.USER_LAST_NAME, "");
        sharedPrefsManager.setStringValue(AppConstantsManager.USER_COMPANY, "");
        sharedPrefsManager.setStringValue(AppConstantsManager.USER_STREET, "");
        sharedPrefsManager.setStringValue(AppConstantsManager.USER_ZIPCODE, "");
        sharedPrefsManager.setStringValue(AppConstantsManager.USER_CITY, "");
        sharedPrefsManager.setStringValue(AppConstantsManager.USER_COUNTRY, "");
        sharedPrefsManager.setStringValue(AppConstantsManager.USER_DESCRIPTION, "");
        sharedPrefsManager.setStringValue(AppConstantsManager.USER_LANGUAGE, "");
        sharedPrefsManager.setStringValue(AppConstantsManager.USER_RIGHTS, "");
        sharedPrefsManager.setStringValue(AppConstantsManager.USER_TOKEN, "");
        sharedPrefsManager.setStringValue(AppConstantsManager.USER_TOKEN_DATE, "");
        sharedPrefsManager.setStringValue(AppConstantsManager.USER_EMAIL_INFO, "");

        sharedPrefsManager.setStringValue(AppConstantsManager.AUTH_API_TOKEN, "");
        sharedPrefsManager.setStringValue(AppConstantsManager.AUTH_API_TOKEN_EXPIRE, "");

        Intent logoutIntent = new Intent(this, LoginActivity.class);
        logoutIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(logoutIntent);
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();

    }

    private void bindView() {
        toolbar =   findViewById(R.id.toolbar);
        activateGps =   findViewById(R.id.activate_gps);
        gpsOnlyGeoRef =   findViewById(R.id.gps_only_geo_referenced);
        viewDirectionCompass =   findViewById(R.id.view_direction_compass);
        autoLoadLastProject =   findViewById(R.id.auto_load_last_project);
        autoLoadLastPlan =   findViewById(R.id.auto_load_last_plan);
        syncPhotosMobNet =   findViewById(R.id.sync_photos_on_mobile_net);
        syncPhotosBeforeMobNet =   findViewById(R.id.sync_photos_before_on_mobile_net);
        syncPhotosWLan =   findViewById(R.id.sync_photos_on_wlan);
        syncPhotosBeforeWLan =   findViewById(R.id.sync_photos_before_on_wlan);
        reloadOriginalImageOnWLan =   findViewById(R.id.reload_images_original_wlan);
        allowBackgroundSync =   findViewById(R.id.allow_sync_background);
        firstLoadNewest =   findViewById(R.id.first_load_newest);
        buzzwordFavoritesPreview =   findViewById(R.id.buzzword_favorites_in_preview);
        sync_minimize_photos_via_mobile_net =   findViewById(R.id.sync_minimize_photos_via_mobile_net);
        sync_photos_ascending =   findViewById(R.id.sync_photos_ascending);
        ll_parent =   findViewById(R.id.ll_parent);
        mSyncPhotosAscending =   findViewById(R.id.sync_photos_ascending);
        mActivateGps =   findViewById(R.id.activate_gps);
        mGpsOnlyGeoReferenced =   findViewById(R.id.gps_only_geo_referenced);
        mSyncMinimizePhotosViaMobileNet =   findViewById(R.id.sync_minimize_photos_via_mobile_net);
        mViewDirectionCompass =   findViewById(R.id.view_direction_compass);
        mAutoLoadLastProject =   findViewById(R.id.auto_load_last_project);
        mAutoLoadLastPlan =   findViewById(R.id.auto_load_last_plan);
        mSyncPhotosOnMobileNet =   findViewById(R.id.sync_photos_on_mobile_net);
        mSyncPhotosBeforeOnMobileNet =   findViewById(R.id.sync_photos_before_on_mobile_net);
        mSyncPhotosOnWlan =   findViewById(R.id.sync_photos_on_wlan);
        mSyncPhotosBeforeOnWlan =   findViewById(R.id.sync_photos_before_on_wlan);
        mReloadImagesOriginalWlan =   findViewById(R.id.reload_images_original_wlan);
        mAllowSyncBackground =   findViewById(R.id.allow_sync_background);
        mFirstLoadNewest =   findViewById(R.id.first_load_newest);
        mBuzzwordFavoritesInPreview =   findViewById(R.id.buzzword_favorites_in_preview);
        mCameraResolution =   findViewById(R.id.camera_resolution);
        mGpsScale =   findViewById(R.id.gps_scale);
        mLogout =   findViewById(R.id.logout);
        mCameraResolution.setOnClickListener(v -> {
            onCameraResolutionClick();
        });
        mGpsScale.setOnClickListener(v -> {
            onGpsScaleClick();
        });
        mLogout.setOnClickListener(v -> {
            onLogoutClick();
        });
    }
}
