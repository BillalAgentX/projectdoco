package com.projectdocupro.mobile.activities;

import static com.projectdocupro.mobile.managers.AppConstantsManager.AUTO_LOAD_LAST_PROJECT_DEFAULT_VALUE;

import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.drawable.AnimationDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.projectdocupro.mobile.ProjectsDatabase;
import com.projectdocupro.mobile.R;
import com.projectdocupro.mobile.dao.PhotoDao;
import com.projectdocupro.mobile.fragments.add_direction.ProjectDocuUtilities;
import com.projectdocupro.mobile.interfaces.RetroApiInterface;
import com.projectdocupro.mobile.managers.AppConstantsManager;
import com.projectdocupro.mobile.managers.RetrofitManager;
import com.projectdocupro.mobile.managers.SharedPrefsManager;
import com.projectdocupro.mobile.models.PhotoModel;
import com.projectdocupro.mobile.models.User;
import com.projectdocupro.mobile.repos.LocalPhotosRepository;
import com.projectdocupro.mobile.service.SyncLocalPhotosService;
import com.projectdocupro.mobile.utility.Utils;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Random;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SplashActivity extends AppCompatActivity /*implements LoginDialogCallback*/ {
    Handler myHandler = new Handler();
    Message m = new Message();
    private SharedPrefsManager sharedPrefsManager;
    public static final int PHONE_BOOK_REQUEST_CODE = 1010;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Utils.showLogger("Hello Debugging Bilal");
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_splash);
        startSplashAnimation();
//        startSplashAnimation();
//        Crashlytics.getInstance().crash();
//
        sharedPrefsManager = new SharedPrefsManager(this);

        String dateString = sharedPrefsManager.getStringValue(AppConstantsManager.AUTH_API_TOKEN_EXPIRE, "2019-05-14 06:51:44");
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss", Locale.GERMANY);
        Date currentDate = new Date();
        if (!sharedPrefsManager.getBooleanValue(AppConstantsManager.AUTO_LOAD_LAST_PROJECT, AUTO_LOAD_LAST_PROJECT_DEFAULT_VALUE)) {
            sharedPrefsManager.setLastProjectName(this, "");
            sharedPrefsManager.setLastProjectId(this, "");
        }
        generateUniqueDeviceIdForDocu();
        setLocale(Locale.getDefault().getCountry().toLowerCase());

        sharedPrefsManager.setStringValue(AppConstantsManager.ONLINE_PHOTO_COUNT, "");
       /* try {
            if (dateString.length() > 3 && currentDate.before(simpleDateFormat.parse(dateString.substring(1, dateString.length() - 1)))) {
                if (sharedPrefsManager.getStringValue(AppConstantsManager.APP_LANGUAGE, "").isEmpty()) {
//                    LanguageDialog languageDialog = new LanguageDialog(this, R.style.Dialog_Theme, this);
//                    languageDialog.show();
                    setLocale(Locale.getDefault().getCountry().toLowerCase());
                } else {
                    if (sharedPrefsManager.getStringValue(AppConstantsManager.PD_USER_ID, "").isEmpty()) {
                        startActivity(new Intent(this, LoginActivity.class));
                        this.finish();
                    } else {
//                        startActivity(new Intent(this, ProjectDocuMainActivity.class));
                        Intent intent = new Intent(this, HomeActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
//                        startActivity(new Intent(this, HomeActivity.class));
                    }
                    this.finish();
                }
            } else {
                if (sharedPrefsManager.getStringValue(AppConstantsManager.APP_LANGUAGE, "").isEmpty()) {
//                    LanguageDialog languageDialog = new LanguageDialog(this, R.style.Dialog_Theme, this);
//                    languageDialog.show();
                    setLocale(Locale.getDefault().getCountry().toLowerCase());
                } else {
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
                    sharedPrefsManager.setStringValue(AppConstantsManager.AUTH_API_TOKEN_EXPIRE, "2019-05-14 06:51:44");

                    startActivity(new Intent(this, LoginActivity.class));
                    this.finish();
                }
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }*/

        moveToDashBoardScreen();
    }


    private class GetUnSyncAsyncTask extends AsyncTask<PhotoModel, Void, Void> {
        private PhotoDao mAsyncTaskDao;

        GetUnSyncAsyncTask() {
            ProjectsDatabase projectsDatabase = ProjectsDatabase.getDatabase(getApplication());
            mAsyncTaskDao = projectsDatabase.photoDao();
        }

        @Override
        protected Void doInBackground(final PhotoModel... params) {

            List<PhotoModel> photoModelList = mAsyncTaskDao.markUnSyncUploadingPhotos(LocalPhotosRepository.TYPE_LOCAL_PHOTO);
            ;

            for (int i = 0; i < photoModelList.size(); i++) {
                PhotoModel photoModel = photoModelList.get(i);
                if (photoModel.getFailedCount() < 6)
                    photoModel.setPhotoUploadStatus(LocalPhotosRepository.UPLOADING_PHOTO);
                else
                    photoModel.setPhotoUploadStatus(LocalPhotosRepository.UN_SYNC_PHOTO);
                photoModel.setUserSelectedStatus(false);
                new UpdateAsyncTask().execute(photoModel);
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            SyncLocalPhotosService.enqueueWork(SplashActivity.this);
        }
    }

    private class UpdateAsyncTask extends AsyncTask<PhotoModel, Void, Void> {
        private PhotoDao mAsyncTaskDao;

        UpdateAsyncTask() {
            ProjectsDatabase projectsDatabase = ProjectsDatabase.getDatabase(getApplication());
            mAsyncTaskDao = projectsDatabase.photoDao();
        }

        @Override
        protected Void doInBackground(final PhotoModel... params) {
            mAsyncTaskDao.update(params[0]);
            return null;
        }
    }


    private class MarkUnSyncPendingEntriesAsyncTask extends AsyncTask<PhotoModel, Void, Void> {
        private PhotoDao mAsyncTaskDao;

        MarkUnSyncPendingEntriesAsyncTask() {
            ProjectsDatabase projectsDatabase = ProjectsDatabase.getDatabase(getApplication());
            mAsyncTaskDao = projectsDatabase.photoDao();
        }

        @Override
        protected Void doInBackground(final PhotoModel... params) {

            List<PhotoModel> photoModelList = mAsyncTaskDao.markUnSyncUploadingPhotos(/*LocalPhotosRepository.UN_SYNC_PHOTO,*/ LocalPhotosRepository.TYPE_LOCAL_PHOTO);

            for (int i = 0; i < photoModelList.size(); i++) {
                PhotoModel photoModel = photoModelList.get(i);
                photoModel.setPhotoUploadStatus(LocalPhotosRepository.UN_SYNC_PHOTO);
                photoModel.setUserSelectedStatus(false);
                mAsyncTaskDao.update(photoModel);
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
        }
    }


    public void startSplashAnimation() {
        ImageView frameanimation = (ImageView) findViewById(R.id.image_logo);
        frameanimation.setImageBitmap(null);
        frameanimation.setBackgroundResource(R.drawable.splash_animation);
        AnimationDrawable frame_animation = (AnimationDrawable) frameanimation.getBackground();

        frame_animation.setVisible(true, true);
        frame_animation.start();

//                try {
//            Thread.sleep(4000);
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
    }

    /*@Override
    public void onLoginDialogResponse() {
        startSplashAnimation();
        SharedPrefsManager sharedPrefsManager = new SharedPrefsManager(this);

        String dateString1 = sharedPrefsManager.getStringValue(AppConstantsManager.AUTH_API_TOKEN_EXPIRE, "2019-05-14 06:51:44");
        String dateString = dateString1.replace("\"", "");
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss", Locale.GERMANY);
        Date currentDate = new Date();

        try {
            if (currentDate.before(simpleDateFormat.parse(dateString))) {
                if (sharedPrefsManager.getStringValue(AppConstantsManager.PD_USER_ID, "").isEmpty()) {
                    startActivity(new Intent(this, LoginActivity.class));
                    this.finish();
                } else {
//                    startActivity(new Intent(this, HomeActivity.class));
                    Intent intent = new Intent(this, HomeActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                }
            } else {
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
                sharedPrefsManager.setStringValue(AppConstantsManager.AUTH_API_TOKEN_EXPIRE, "2019-05-14 06:51:44");

                startActivity(new Intent(this, LoginActivity.class));
                this.finish();
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
        this.finish();
    }*/


    private void setLocale(String lang) {
        Locale myLocale = new Locale(lang);
        Resources res = getResources();
        DisplayMetrics dm = res.getDisplayMetrics();
        Configuration conf = res.getConfiguration();
        conf.locale = myLocale;
        res.updateConfiguration(conf, dm);
        sharedPrefsManager.setStringValue(AppConstantsManager.APP_LANGUAGE, lang);
        //onLoginDialogResponse();

    }

    private void callLoginAPI(String email, String password) {

        JsonObject params = new JsonObject();
        params.addProperty("username", email);
        params.addProperty("password", password);
        params.addProperty("deviceid", Utils.DEVICE_ID);

        RetroApiInterface retroApiInterface = RetrofitManager.getInstance().create(RetroApiInterface.class);
        Call<JsonObject> call = retroApiInterface.loginAPI(params);

        Log.d("Details", "sent");
        call.enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {

                if (response.isSuccessful()) {
                    if (response.body() != null) {
                        try {
                            Log.d("Login", "Success : " + response.body());
                            User user = new Gson().fromJson(response.body().getAsJsonObject("data"), User.class);
                            saveUserDetails(user);
                            saveAuthDetails(response.body().getAsJsonObject("auth_params"));
                            moveToNextController();
                        }
                        catch (Exception e){
                            e.printStackTrace();
                        }
                    } else {
                        Log.d("Login", "Empty response");
//                        Toast.makeText(LoginActivity.this, getString(R.string.toast_login_failed), Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Log.d("Login", "Not Success : " + response.toString());
//                    Toast.makeText(LoginActivity.this, getString(R.string.toast_login_failed), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
//                Log.d("Login", "failed : " + t.getMessage());
//                Toast.makeText(SplashActivity.this, getString(R.string.toast_login_failed), Toast.LENGTH_SHORT).show();
                moveToDashBoardScreen();
            }
        });
    }

    public void moveToNextController() {
        if (sharedPrefsManager.getBooleanValue(AppConstantsManager.USER_PRIVACY_POLICY, false)) {
            Intent intent = new Intent(SplashActivity.this, PrivacyPolicyActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            finish();
        } else {
            Intent intent = new Intent(SplashActivity.this, HomeActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            finish();
        }
    }

    private void saveAuthDetails(JsonObject auth_params) {
        SharedPrefsManager sharedPrefsManager = new SharedPrefsManager(this);
        sharedPrefsManager.setStringValue(AppConstantsManager.AUTH_API_TOKEN, auth_params.get("apikey").toString());
        sharedPrefsManager.setStringValue(AppConstantsManager.AUTH_API_TOKEN_EXPIRE, auth_params.get("expire").toString());
    }

    private void saveUserDetails(User user) {
        SharedPrefsManager sharedPrefsManager = new SharedPrefsManager(this);
        sharedPrefsManager.setStringValue(AppConstantsManager.PD_USER_ID, user.getPduserid());
        sharedPrefsManager.setStringValue(AppConstantsManager.USER_EMAIL, user.getEmail());
        sharedPrefsManager.setStringValue(AppConstantsManager.USER_CREATED, user.getCreated());
        sharedPrefsManager.setStringValue(AppConstantsManager.USER_LAST_LOGIN, user.getLastlogin());
        sharedPrefsManager.setStringValue(AppConstantsManager.USER_ACTIVE, user.getActive());
        sharedPrefsManager.setStringValue(AppConstantsManager.USER_STATUS, user.getStatus());
        sharedPrefsManager.setStringValue(AppConstantsManager.USER_GENDER, user.getGender());
        sharedPrefsManager.setStringValue(AppConstantsManager.USER_TITLE, user.getTitle());
        sharedPrefsManager.setStringValue(AppConstantsManager.USER_FIRST_NAME, user.getFirstname());
        sharedPrefsManager.setStringValue(AppConstantsManager.USER_LAST_NAME, user.getLastname());
        sharedPrefsManager.setStringValue(AppConstantsManager.USER_COMPANY, user.getCompany());
        sharedPrefsManager.setStringValue(AppConstantsManager.USER_STREET, user.getStreet());
        sharedPrefsManager.setStringValue(AppConstantsManager.USER_ZIPCODE, user.getZipcode());
        sharedPrefsManager.setStringValue(AppConstantsManager.USER_CITY, user.getCity());
        sharedPrefsManager.setStringValue(AppConstantsManager.USER_COUNTRY, user.getCountry());
        sharedPrefsManager.setStringValue(AppConstantsManager.USER_DESCRIPTION, user.getDescription());
        sharedPrefsManager.setStringValue(AppConstantsManager.USER_LANGUAGE, user.getLanguage());
        sharedPrefsManager.setStringValue(AppConstantsManager.USER_RIGHTS, user.getRights());
        sharedPrefsManager.setStringValue(AppConstantsManager.USER_TOKEN, user.getToken());
        sharedPrefsManager.setStringValue(AppConstantsManager.USER_TOKEN_DATE, user.getToken_date());
        sharedPrefsManager.setStringValue(AppConstantsManager.USER_EMAIL_INFO, user.getEmailinfo());
        // User crednetials
//        sharedPrefsManager.setStringValue(AppConstantsManager.USER_ENTERED_EMAIL, userName.getText().toString());
//        sharedPrefsManager.setStringValue(AppConstantsManager.USER_ENTERED_PASSWORD, password.getText().toString());
    }

    private void moveToDashBoardScreen() {

        if (!sharedPrefsManager.getStringValue(AppConstantsManager.USER_ENTERED_EMAIL, "").equals("")
                && !sharedPrefsManager.getStringValue(AppConstantsManager.USER_ENTERED_PASSWORD, "").equals("")) {
            if (ProjectDocuUtilities.isNetworkConnected(getApplication())) {
                callLoginAPI(sharedPrefsManager.getStringValue(AppConstantsManager.USER_ENTERED_EMAIL, ""), sharedPrefsManager.getStringValue(AppConstantsManager.USER_ENTERED_PASSWORD, ""));
            } else {
                moveToNextController();
            }
        } else {
            startActivity(new Intent(this, LoginActivity.class));
            this.finish();
        }

        if (sharedPrefsManager.getBooleanValue(AppConstantsManager.ALLOW_BACKGROUND_SYNC, false)
                && (sharedPrefsManager.getBooleanValue(AppConstantsManager.SYNC_PHOTOS_MOBILE_DATA, false)
                || sharedPrefsManager.getBooleanValue(AppConstantsManager.SYNC_PHOTOS_WLAN, false))) {
            new GetUnSyncAsyncTask().execute();
        } else {

        }

        if (sharedPrefsManager.getBooleanValue(AppConstantsManager.IF_FIRST_RUN, true)) {
            sharedPrefsManager.setBooleanValue(AppConstantsManager.IF_FIRST_RUN, false);
            sharedPrefsManager.setBooleanValue(AppConstantsManager.ACTIVATE_GPS, true);
            sharedPrefsManager.setBooleanValue(AppConstantsManager.SYNC_PHOTO_ASC_ORDER, AppConstantsManager.SYNC_PHOTO_ASC_ORDER_DEFAULT_VALUE);
            sharedPrefsManager.setBooleanValue(AppConstantsManager.VIEW_DIRECTION_THROUGH_COMPASS, true);
            String selectedAutoGpsDistance = sharedPrefsManager.getGpsAccuracy(this);
            sharedPrefsManager.setStringValue(AppConstantsManager.USER_SELECTED_LOCATION_ACCURACY, selectedAutoGpsDistance);
        }
    }


    public void generateUniqueDeviceIdForDocu() {
        SharedPrefsManager sharedPrefsManager = new SharedPrefsManager(this);
        Random random = new Random();
        int randomNum1 = random.nextInt(100 - 1) + 1;
        int randomNum2 = random.nextInt(500 - 101) + 101;
        int randomNum3 = random.nextInt(1000 - 501) + 501;

        if (sharedPrefsManager.getDeviceId() != null && sharedPrefsManager.getDeviceId().equals("string")) {
            Utils.DEVICE_ID = System.currentTimeMillis() + "#" + randomNum1 + "" + randomNum2 + "" + randomNum3;
            sharedPrefsManager.setDeviceId(Utils.DEVICE_ID);
        } else {
            Utils.DEVICE_ID = sharedPrefsManager.getDeviceId();
        }
//        AppSignatureHelper appSignatureHelper= new AppSignatureHelper(this);
//        appSignatureHelper.getAppSignatures();
    }
}