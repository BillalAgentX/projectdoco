package com.projectdocupro.mobile.activities;

import android.content.res.Configuration;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toolbar;

import androidx.appcompat.app.AppCompatActivity;

import com.projectdocupro.mobile.ProjectsDatabase;
import com.projectdocupro.mobile.R;
import com.projectdocupro.mobile.dao.PhotoDao;
import com.projectdocupro.mobile.managers.SharedPrefsManager;



public class SyncStatusActivity extends AppCompatActivity {
    
    SharedPrefsManager sharedPrefsManager;

    private Toolbar toolbar;

    private TextView tv_total_photo;
    private TextView tv_total_photo_count;
    private TextView tv_sync_photo;
    private TextView tv_sync_photo_count;
    private TextView tv_un_sync_photo;
    private TextView tv_un_sync_photo_count;

    private TextView tv_project_total_photo;
    private TextView tv_project_total_photo_count;
    private TextView tv_project_sync_photo;
    private TextView tv_project_sync_photo_count;
    private TextView tv_project_un_sync_photo;
    private TextView tv_project_un_sync_photo_count;


    private TextView tv_project_shortly_sync_photo_count;

    private TextView tv_shortly_sync_photo_count;

    private LinearLayout llParent;
    private LinearLayout llTotalPhotos;
    private LinearLayout llProjectPhotos;

    long allProjectSyncCount = 0;
    long allProjectUNSyncCount = 0;
    private long allProjectShortlySyncCount = 0;

    long projectSyncCount = 0;
    long projectUNSyncCount = 0;
    private long projectShortlySyncCount = 0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photo_info);
        bindView();
        sharedPrefsManager = new SharedPrefsManager(this);
        setDifferenScreensOrientations();

        toolbar.setNavigationOnClickListener(view -> onBackPressed());

        new reterivePhotosCountAsyncTask().execute(getIntent().getStringExtra("projectId"));

    }

    private void populateCountData() {
        tv_total_photo_count.setText(Long.valueOf(allProjectSyncCount + allProjectUNSyncCount ) + "");
        tv_sync_photo_count.setText(Long.valueOf(allProjectSyncCount) + "");
        tv_un_sync_photo_count.setText(Long.valueOf(allProjectUNSyncCount-allProjectShortlySyncCount) + "");
        tv_shortly_sync_photo_count.setText(Long.valueOf(allProjectShortlySyncCount) + "");


        tv_project_total_photo_count.setText(Long.valueOf(projectSyncCount + projectUNSyncCount ) + "");
        tv_project_sync_photo_count.setText(Long.valueOf(projectSyncCount) + "");
        tv_project_un_sync_photo_count.setText(Long.valueOf(projectUNSyncCount-projectShortlySyncCount) + "");
        tv_project_shortly_sync_photo_count.setText(Long.valueOf(projectShortlySyncCount) + "");
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
        } else if ((screenSize == Configuration.SCREENLAYOUT_SIZE_NORMAL) && orientation == Configuration.ORIENTATION_LANDSCAPE) {
            landScapeMode();
        }
    }

    private void landScapeMode() {

        LinearLayout.LayoutParams lpParent = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
//        ll_parent.setLayoutParams(lpParent);
        llParent.setWeightSum(2);
        llParent.setOrientation(LinearLayout.HORIZONTAL);

        LinearLayout.LayoutParams lpSection1 = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1);
        llTotalPhotos.setLayoutParams(lpSection1);
        llTotalPhotos.setOrientation(LinearLayout.VERTICAL);

        LinearLayout.LayoutParams lpSection2 = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1);
//        lpSection2.setMargins(25, 90, 5, 10);
        llProjectPhotos.setLayoutParams(lpSection2);
        llProjectPhotos.setOrientation(LinearLayout.VERTICAL);

    }


    private void portraitMode() {

        LinearLayout.LayoutParams lpParent = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
//        ll_parent.setLayoutParams(lpParent);
        llParent.setOrientation(LinearLayout.VERTICAL);

        LinearLayout.LayoutParams lpSection1 = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        llTotalPhotos.setLayoutParams(lpSection1);
        llTotalPhotos.setOrientation(LinearLayout.VERTICAL);

        LinearLayout.LayoutParams lpSection2 = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        llProjectPhotos.setLayoutParams(lpSection2);
        llProjectPhotos.setOrientation(LinearLayout.VERTICAL);

    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        
    }

    private void bindView() {
        toolbar =  findViewById(R.id.toolbar);
        tv_total_photo =  findViewById(R.id.tv_total_photo);
        tv_total_photo_count =  findViewById(R.id.tv_total_photo_count);
        tv_sync_photo =  findViewById(R.id.tv_sync_photo);
        tv_sync_photo_count =  findViewById(R.id.tv_sync_photo_count);
        tv_un_sync_photo =  findViewById(R.id.tv_un_sync_photo);
        tv_un_sync_photo_count =  findViewById(R.id.tv_un_sync_photo_count);
        tv_project_total_photo =  findViewById(R.id.tv_project_total_photo);
        tv_project_total_photo_count =  findViewById(R.id.tv_project_total_photo_count);
        tv_project_sync_photo =  findViewById(R.id.tv_project_sync_photo);
        tv_project_sync_photo_count =  findViewById(R.id.tv_project_sync_photo_count);
        tv_project_un_sync_photo =  findViewById(R.id.tv_project_un_sync_photo);
        tv_project_un_sync_photo_count =  findViewById(R.id.tv_project_un_sync_photo_count);
        tv_project_shortly_sync_photo_count =  findViewById(R.id.tv_project_shortly_sync_photo_count);
        tv_shortly_sync_photo_count =  findViewById(R.id.tv_shortly_sync_photo_count);
        llParent =  findViewById(R.id.llParent);
        llTotalPhotos =  findViewById(R.id.llTotalPhotos);
        llProjectPhotos =  findViewById(R.id.llProjectPhotos);
    }

    private class reterivePhotosCountAsyncTask extends AsyncTask<String, Void, Void> {
        private PhotoDao mAsyncTaskDao;
        ProjectsDatabase db;

        reterivePhotosCountAsyncTask() {
            db = ProjectsDatabase.getDatabase(getApplicationContext());
            mAsyncTaskDao = db.photoDao();
        }

        @Override
        protected Void doInBackground(final String... params) {

            projectSyncCount = mAsyncTaskDao.getSyncedPhotoCount(params[0]);
            projectUNSyncCount = mAsyncTaskDao.getUnSyncedPhotoCount(params[0]);
            projectShortlySyncCount = mAsyncTaskDao.getShortlySyncedPhotoCount(params[0]);

            allProjectSyncCount = mAsyncTaskDao.getSyncedPhotoCountAllProject();
            allProjectUNSyncCount = mAsyncTaskDao.getUnSyncedPhotoCountAllProject();
            allProjectShortlySyncCount = mAsyncTaskDao.getShortlySyncedPhotoCountAllProject();

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            populateCountData();
        }
    }
}
