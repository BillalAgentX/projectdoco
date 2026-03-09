package com.projectdocupro.mobile.activities;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatEditText;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.projectdocupro.mobile.SettingsConstants;
import com.projectdocupro.mobile.R;
import com.projectdocupro.mobile.fragments.add_direction.ProjectDocuUtilities;
import com.projectdocupro.mobile.managers.AppConstantsManager;
import com.projectdocupro.mobile.managers.RetrofitManager;
import com.projectdocupro.mobile.managers.SharedPrefsManager;
import com.projectdocupro.mobile.models.User;
import com.projectdocupro.mobile.interfaces.RetroApiInterface;
import com.projectdocupro.mobile.utility.Utils;

import java.util.Objects;

import androidx.core.app.ActivityCompat;
import androidx.core.widget.ContentLoadingProgressBar;


import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity {

    private static final String TAG = LoginActivity.class.getCanonicalName();


    private AppCompatEditText userName;

    private AppCompatEditText password;



    private LinearLayout ll_parent;

    private LinearLayout ll_section_1;

    private LinearLayout ll_section_2;

    private TextView label_forgot_password;
    private View mLogin;

    RelativeLayout pbar;

    ContentLoadingProgressBar progressBar;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        bindView();
        setDifferenScreensOrientations();
//        checkTheDeviceResolution();
        pbar = findViewById(R.id.rl_pb_parent);

        progressBar = findViewById(R.id.progressBar);


        adjustSettings();

        isWriteStoragePermissionGranted();
        addEvent();
    }

    private void adjustSettings() {
        if(SettingsConstants.
                IS_TESTING_MODULE) {
        userName.setText("dev001@projectdocu.de");
        password.setText("devzain2");
        }
    }

    private void addEvent() {
//        label_forgot_password.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Intent intent= new Intent(LoginActivity.this, ForgotPasswordWebviewActivity.class);
//
//                startActivity(intent);
//            }
//        });
    }

    private void setDifferenScreensOrientations() {
        int orientation = getResources().getConfiguration().orientation;
        int screenSize = getResources().getConfiguration().screenLayout &
                Configuration.SCREENLAYOUT_SIZE_MASK;

        /*1- Configuration.SCREENLAYOUT_SIZE_LARGE
         * 2- Configuration.SCREENLAYOUT_SIZE_XLARGE
         * 3- Configuration.SCREENLAYOUT_SIZE_NORMAL*/
        if ((screenSize == Configuration.SCREENLAYOUT_SIZE_XLARGE || screenSize == Configuration.SCREENLAYOUT_SIZE_LARGE
                || screenSize == Configuration.SCREENLAYOUT_SIZE_NORMAL)
                && orientation == Configuration.ORIENTATION_PORTRAIT) {
            portraitMode();
        } else if ((screenSize == Configuration.SCREENLAYOUT_SIZE_XLARGE || screenSize == Configuration.SCREENLAYOUT_SIZE_LARGE)
                && orientation == Configuration.ORIENTATION_LANDSCAPE) {
            portraitMode();
        } else if ((screenSize == Configuration.SCREENLAYOUT_SIZE_NORMAL || screenSize == Configuration.SCREENLAYOUT_SIZE_SMALL) && orientation == Configuration.ORIENTATION_LANDSCAPE) {
            landScapeMode();
        }

    }

    private void checkTheDeviceResolution() {
        int orientation = getResources().getConfiguration().orientation;
        if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
            // In landscape
        } else {
            // In portrait
        }
        int screenSize = getResources().getConfiguration().screenLayout &
                Configuration.SCREENLAYOUT_SIZE_MASK;

        String toastMsg;
        switch (screenSize) {
            case Configuration.SCREENLAYOUT_SIZE_XLARGE:
                toastMsg = "Extra Large screen";
                break;
            case Configuration.SCREENLAYOUT_SIZE_LARGE:
                toastMsg = "Large screen";
                break;
            case Configuration.SCREENLAYOUT_SIZE_NORMAL:
                toastMsg = "Normal screen";
                break;
            case Configuration.SCREENLAYOUT_SIZE_SMALL:
                toastMsg = "Small screen";
                break;
            default:
                toastMsg = "Screen size is neither large, normal or small";
        }
        Toast.makeText(this, toastMsg, Toast.LENGTH_LONG).show();

    }

//    @OnClick(R.id.exit)
//    public void exitApp(){
//        this.finish();
//    }

    private void loginApp() {
        if (isDataVerified())
            if (ProjectDocuUtilities.isNetworkConnected(getApplication())) {
                callLoginAPI(Objects.requireNonNull(userName.getText()).toString(), Objects.requireNonNull(password.getText()).toString());
            } else {
                Toast.makeText(getApplication(), getApplication().getResources().getString(R.string.no_internet_message), Toast.LENGTH_SHORT).show();
            }
    }

    private boolean isDataVerified() {
        boolean res = true;

        if (userName.getText().toString().isEmpty()) {
            res = false;
            Toast.makeText(this, getString(R.string.error_enter_username), Toast.LENGTH_SHORT).show();
        } else if (password.getText().toString().isEmpty()) {
            res = false;
            Toast.makeText(this, getString(R.string.error_enter_password), Toast.LENGTH_SHORT).show();
        }

        return res;
    }


    private void callLoginAPI(String email, String password) {

        showProgressbar();
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
                hideProgressbar();
                if (response.isSuccessful()) {
                    if (response.body() != null) {
                        Log.d("Login", "Success : " + response.body());
                        if (response.body().get("data").isJsonObject()) {
                            User user = new Gson().fromJson(response.body().getAsJsonObject("data"), User.class);
                            saveUserDetails(user);
                            saveAuthDetails(response.body().getAsJsonObject("auth_params"));
                            SharedPrefsManager sharedPrefsManager = new SharedPrefsManager(LoginActivity.this);
                            if (sharedPrefsManager.getBooleanValue(AppConstantsManager.USER_PRIVACY_POLICY, false)) {
                                Intent intent = new Intent(LoginActivity.this, PrivacyPolicyActivity.class);
                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                startActivity(intent);
                                finish();
                            } else {
                                Intent intent = new Intent(LoginActivity.this, HomeActivity.class);
                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                startActivity(intent);
                                finish();
                            }
                        }else {
                            Toast.makeText(LoginActivity.this, getString(R.string.check_credentials), Toast.LENGTH_SHORT).show();
                        }

                    } else {
                        Log.d("Login", "Empty response");
                        Toast.makeText(LoginActivity.this, getString(R.string.toast_login_failed), Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Log.d("Login", "Not Success : " + response.toString());
                    Toast.makeText(LoginActivity.this, getString(R.string.toast_login_failed), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                hideProgressbar();
                Log.d("Login", "failed : " +call.request().url().url().toString()+ t.getMessage());
                Toast.makeText(LoginActivity.this, getString(R.string.toast_login_failed), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void landScapeMode() {

        LinearLayout.LayoutParams lpParent = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
//        ll_parent.setLayoutParams(lpParent);
        ll_parent.setWeightSum(2);
        ll_parent.setOrientation(LinearLayout.HORIZONTAL);

        LinearLayout.LayoutParams lpSection1 = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, (float) 0.8);
        lpSection1.setMargins(0, 90, 0, 0);
        ll_section_1.setLayoutParams(lpSection1);
        ll_section_1.setOrientation(LinearLayout.VERTICAL);

        LinearLayout.LayoutParams lpSection2 = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, (float) 1.2);
        lpSection2.setMargins(0, 90, 0, 0);
        ll_section_2.setLayoutParams(lpSection2);
        ll_section_2.setOrientation(LinearLayout.VERTICAL);

    }


    private void portraitMode() {

        LinearLayout.LayoutParams lpParent = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
//        ll_parent.setLayoutParams(lpParent);
        ll_parent.setOrientation(LinearLayout.VERTICAL);

        LinearLayout.LayoutParams lpSection1 = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        ll_section_1.setLayoutParams(lpSection1);
        ll_section_1.setOrientation(LinearLayout.VERTICAL);

        LinearLayout.LayoutParams lpSection2 = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        ll_section_2.setLayoutParams(lpSection2);
        ll_section_2.setOrientation(LinearLayout.VERTICAL);

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
        sharedPrefsManager.setBooleanValue(AppConstantsManager.USER_PRIVACY_POLICY, true);

        // User crednetials
        sharedPrefsManager.setStringValue(AppConstantsManager.USER_ENTERED_EMAIL, userName.getText().toString());
        sharedPrefsManager.setStringValue(AppConstantsManager.USER_ENTERED_PASSWORD, password.getText().toString());

    }

    @Override
    protected void onDestroy() {

        super.onDestroy();
    }

    private void bindView() {
        userName =  findViewById(R.id.user_name);
        password =  findViewById(R.id.password);

        ll_parent =  findViewById(R.id.ll_parent);
        ll_section_1 =  findViewById(R.id.ll_logo);
        ll_section_2 =  findViewById(R.id.ll_fields);
        label_forgot_password =  findViewById(R.id.label_forgot_password);
        mLogin =  findViewById(R.id.login);
        mLogin.setOnClickListener(v -> {
            loginApp();
        });
    }



    private void showProgressbar() {

        pbar.setVisibility(View.VISIBLE);
        progressBar.show();

    }

    private void hideProgressbar() {

        pbar.setVisibility(View.GONE);

    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null && getCurrentFocus() != null) {
            imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
        }
        return true;
    }

    public boolean isReadStoragePermissionGranted() {
        if (Build.VERSION.SDK_INT >= 23) {
            if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED) {
                Log.v(TAG, "Permission is granted1");
                return true;
            } else {

                Log.v(TAG, "Permission is revoked1");
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 3);
                return false;
            }
        } else { //permission is automatically granted on sdk<23 upon installation
            Log.v(TAG, "Permission is granted1");
            return true;
        }
    }

    public boolean isWriteStoragePermissionGranted() {
        if (Build.VERSION.SDK_INT >= 23) {
            if (checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED) {
                Log.v(TAG, "Permission is granted2");
                return true;
            } else {

                Log.v(TAG, "Permission is revoked2");
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 2);
                return false;
            }
        } else { //permission is automatically granted on sdk<23 upon installation
            Log.v(TAG, "Permission is granted2");
            return true;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case 2:
                Log.d(TAG, "External storage2");
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.v(TAG, "Permission: " + permissions[0] + "was " + grantResults[0]);
                    //resume tasks needing this permission
//                    downloadPdfFile();
                } else {
//                    progress.dismiss();
                }
                break;

            case 3:
                Log.d(TAG, "External storage1");
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.v(TAG, "Permission: " + permissions[0] + "was " + grantResults[0]);
                    //resume tasks needing this permission
//                    SharePdfFile();
                } else {
//                    progress.dismiss();
                }
                break;
        }
    }

}
