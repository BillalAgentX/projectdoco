package com.projectdocupro.mobile.activities;

import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.widget.ContentLoadingProgressBar;

import com.google.gson.JsonObject;

import com.projectdocupro.mobile.BuildConfig;
import com.projectdocupro.mobile.ProjectsDatabase;
import com.projectdocupro.mobile.R;
import com.projectdocupro.mobile.dao.DefectsDao;
import com.projectdocupro.mobile.dao.PhotoDao;
import com.projectdocupro.mobile.dao.PlansDao;
import com.projectdocupro.mobile.dao.ProjectDao;
import com.projectdocupro.mobile.fragments.add_direction.ProjectDocuUtilities;
import com.projectdocupro.mobile.interfaces.RetroApiInterface;
import com.projectdocupro.mobile.managers.AppConstantsManager;
import com.projectdocupro.mobile.managers.RetrofitManager;
import com.projectdocupro.mobile.managers.SharedPrefsManager;
import com.projectdocupro.mobile.models.DefectsModel;
import com.projectdocupro.mobile.models.PhotoModel;
import com.projectdocupro.mobile.models.PlansModel;
import com.projectdocupro.mobile.models.ProjectModel;

import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class InfoActivity extends AppCompatActivity {


    private Toolbar toolbar;

    private TextView tv_report_error;

    private TextView tv_info;
    private SharedPrefsManager sharedPrefsManager;
    private View progress_bar_view;

    RelativeLayout pbar;

    ContentLoadingProgressBar progressBar;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_info);

        pbar = findViewById(R.id.rl_pb_parent);

        progressBar = findViewById(R.id.progressBar);

        TextView version = findViewById(R.id.version);

        version.setText("Version:"+BuildConfig.VERSION_NAME);

        bindView();

        sharedPrefsManager = new SharedPrefsManager(this);
        //tv_info.setText(tv_info.getText()+"\n\n App Version name:"+BuildConfig.VERSION_NAME);
        toolbar.setNavigationOnClickListener(view -> {
            onBackPressed();
        });

        tv_report_error.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new reterivePhotosCountAsyncTask().execute();

            }
        });

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

    }

    List<ProjectModel> projectModelList = new ArrayList<>();
    List<PhotoModel> photoModelList = new ArrayList<>();
    List<DefectsModel> defectsModelList = new ArrayList<>();
    List<PlansModel> plansModelArrayList = new ArrayList<>();

    private void bindView() {
        toolbar = findViewById(R.id.toolbar);
        tv_report_error = findViewById(R.id.tv_report_error);
        tv_info = findViewById(R.id.info);
        progress_bar_view = findViewById(R.id.progress_bar_view);
    }

    private class reterivePhotosCountAsyncTask extends AsyncTask<Void, Void, Void> {
        private PhotoDao mAsyncTaskDao;
        private ProjectDao projectDao;
        private PlansDao plansDao;
        private DefectsDao defectsDao;

        ProjectsDatabase db;
        HashMap<String, List<?>> listHashMapDumpData = new HashMap<>();
        HashMap<String, Object> listHashMap = new HashMap<>();

        reterivePhotosCountAsyncTask() {
            db = ProjectsDatabase.getDatabase(getApplicationContext());
            mAsyncTaskDao = db.photoDao();
            projectDao = db.projectDao();
            plansDao = db.plansDao();
            defectsDao = db.defectsDao();
        }

        @Override
        protected Void doInBackground(final Void... params) {
            projectModelList = projectDao.getAllProjectsList();
            photoModelList = mAsyncTaskDao.getAllPhotos();
            defectsModelList = defectsDao.getAllDefects();
            plansModelArrayList = plansDao.getAllPlansList();


            listHashMapDumpData.put("Projects", projectModelList);
            listHashMapDumpData.put("Plans", plansModelArrayList);
            listHashMapDumpData.put("Defects", defectsModelList);
            listHashMapDumpData.put("LocalImages", photoModelList);


            String projectModelListStr = listHashMapDumpData.toString();

            Log.d("listHashMap", projectModelListStr);

            listHashMap.put("GPS_Bool", sharedPrefsManager.getBooleanValue(AppConstantsManager.ACTIVATE_GPS, true));
            listHashMap.put("loadLastPlan_Bool", sharedPrefsManager.getBooleanValue(AppConstantsManager.AUTO_LOAD_LAST_PLAN, true));
            listHashMap.put("shouldSyncOnWifi_Bool", sharedPrefsManager.getBooleanValue(AppConstantsManager.SYNC_PHOTOS_WLAN, true));
            listHashMap.put("shouldSyncOnMobileData_Bool", sharedPrefsManager.getBooleanValue(AppConstantsManager.SYNC_PHOTOS_MOBILE_DATA, true));
            listHashMap.put("minimizeVideoMobileData_Bool", sharedPrefsManager.getBooleanValue(AppConstantsManager.MINIMIZE_PHOTO_VIA_MOBILE_DATA, false));
            listHashMap.put("planCoordinatesID", sharedPrefsManager.getStringValue(AppConstantsManager.LOAD_LAST_PLAN_ID, ""));
            listHashMap.put("lastPlanID", sharedPrefsManager.getStringValue(AppConstantsManager.USER_LAST_PLAN_ID, ""));

            String flagsStr = listHashMap.toString();


            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

            callReportErrorAPI(listHashMapDumpData.toString(), listHashMap.toString());
        }
    }


    private void callReportErrorAPI(String dbdump, String temporarydata) {
        if (!ProjectDocuUtilities.isNetworkConnected(getApplication())) {
            Toast.makeText(getApplication(), getApplication().getResources().getString(R.string.no_internet_message), Toast.LENGTH_SHORT).show();
            return;
        }
        showProgressbar();

        SharedPrefsManager sharedPrefsManager = new SharedPrefsManager(getApplication());
        RetroApiInterface retroApiInterface = RetrofitManager.getInstance().create(RetroApiInterface.class);
        String authToken = sharedPrefsManager.getStringValue(AppConstantsManager.AUTH_API_TOKEN, "");

        if (authToken.length() > 2) {
            authToken = authToken.substring(1, authToken.length() - 1);
        }

        JsonObject params = new JsonObject();

        String deviceName = android.os.Build.MODEL;
        String deviceMan = android.os.Build.MANUFACTURER;
        Field[] fields = Build.VERSION_CODES.class.getFields();
        String osName = fields[Build.VERSION.SDK_INT].getName();
        int versionCode = BuildConfig.VERSION_CODE;

        params.addProperty("exception", "");
        params.addProperty("dbdump", dbdump);
        params.addProperty("temporarydata", temporarydata);
        params.addProperty("devicemanufacturer", deviceMan);
        params.addProperty("deviceos", "Android");
        params.addProperty("deviceosversion", osName);
        params.addProperty("version", versionCode + "");
        params.addProperty("userid", sharedPrefsManager.getStringValue(AppConstantsManager.PD_USER_ID, ""));
        params.addProperty("projectid", sharedPrefsManager.getLastProjectId(this));
        params.addProperty("sessionid", authToken);

        Call<JsonObject> call = retroApiInterface.errorreport(params);

        call.enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                hideProgressbar();
                if (response.isSuccessful()) {
                    if (response.body() != null) {

//                        if (photoModel.getHash() != null) {
                        try {


                            JSONObject jsonObject = new JSONObject(response.body().toString());
                            if (jsonObject != null && jsonObject.has("data")) {
                                JSONObject jsonObject2 = jsonObject.getJSONObject("data");

                                if (jsonObject2 != null && jsonObject2.has("errorid")) {
                                    String errorId = jsonObject2.get("errorid").toString();
                                    Toast.makeText(InfoActivity.this, "Error report sent successfully. Report No. " + errorId, Toast.LENGTH_LONG).show();
                                }

                            }


//                                JSONObject jsonObject = new JSONObject(response.body().toString());

//                                if (jsonObject.has("versions")) {
//
//                                    if (jsonObject.getJSONObject("versions").has(photoModel.getHash())) {
//                                        JSONObject jsonObject1 = jsonObject.getJSONObject("versions").getJSONObject(photoModel.getHash());
//                                        if (jsonObject1.has("hash")) {
//                                            hash = jsonObject1.getString("hash");
//                                        }
//                                        if (jsonObject1.has("pdphotoid")) {
//                                            pdphotoid = jsonObject1.getString("pdphotoid");
//                                        }
//                                        if (jsonObject1.has("quality")) {
//                                            quality = jsonObject1.getString("quality");
//                                            if (!pdphotoid.equalsIgnoreCase("null"))
//                                                photoModel.setPdphotoid(pdphotoid);
//                                            if (quality != null && (quality.equalsIgnoreCase(LocalPhotosRepository.MISSING_PHOTO_QUALITY) || quality.equalsIgnoreCase(LocalPhotosRepository.REDUCED_PHOTO_QUALITY))) {
//                                                callUploadImageAPI(photoModel);
//                                            } else {
//                                                callUpdatePhotoAPI(photoModel);
//                                            }
//                                        }
//                                    }
//                                }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
//
//                        }


                    } else {
//                        Log.d("Login", "Empty response");
//                        Toast.makeText(getActivity(), getString(R.string.toast_login_failed), Toast.LENGTH_SHORT).show();
                    }
                } else {
//                    Log.d("Login", "Not Success : " + response.toString());
//                    Toast.makeText(getActivity(), getString(R.string.toast_login_failed), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                hideProgressbar();

                Log.d("Login", "failed : " + t.getMessage());
//                Toast.makeText(getActivity(), getString(R.string.toast_login_failed), Toast.LENGTH_SHORT).show();
            }
        });
    }



    private void showProgressbar() {

        pbar.setVisibility(View.VISIBLE);
        progressBar.show();

    }

    private void hideProgressbar() {

        pbar.setVisibility(View.GONE);

    }


}
