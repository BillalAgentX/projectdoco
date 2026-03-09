package com.projectdocupro.mobile.activities;

import static com.projectdocupro.mobile.service.SyncLocalPhotosService.SHOW_RESULT;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.graphics.drawable.DrawerArrowDrawable;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.material.navigation.NavigationView;
import com.projectdocupro.mobile.ProjectNavigator;
import com.projectdocupro.mobile.ProjectsDatabase;
import com.projectdocupro.mobile.R;
import com.projectdocupro.mobile.adapters.NavigationViewRecyclerAdapter;
import com.projectdocupro.mobile.dao.PhotoDao;
import com.projectdocupro.mobile.fragments.add_direction.ProjectDocuUtilities;
import com.projectdocupro.mobile.managers.AppConstantsManager;
import com.projectdocupro.mobile.managers.SharedPrefsManager;
import com.projectdocupro.mobile.models.PhotoModel;
import com.projectdocupro.mobile.models.ProjectModel;
import com.projectdocupro.mobile.photoview.PhotoView;
import com.projectdocupro.mobile.receivers.WorkerResultReceiver;
import com.projectdocupro.mobile.repos.LocalPhotosRepository;
import com.projectdocupro.mobile.service.SyncLocalPhotosService;
import com.projectdocupro.mobile.utility.Utils;
import com.projectdocupro.mobile.viewModels.LocalPhotosViewModel;
import com.projectdocupro.mobile.viewModels.ProjectDetailViewModel;

import java.io.File;
import java.util.Date;
import java.util.List;


public class ProjectDetailActivity extends AppCompatActivity implements WorkerResultReceiver.Receiver{



    public static final String IS_FROM_DEFECT_KEY = "is_from_defect";
    ProjectDetailViewModel projectDetailViewModel;
    
    LocalPhotosViewModel localPhotosViewModel;
    private Toolbar toolbar;

    private TextView projectName;

    private TextView projectDetail;

    private TextView numberPhotos;

    private TextView numberPlans;

    private TextView numberDefects;

    private PhotoView project_image;

    private LinearLayout ll_parent;

    private LinearLayout ll_section_1;

    private LinearLayout ll_section_2;

    private NavigationView navigationView;

    private RecyclerView navigationViewRV;

    private View ll_header_info_view;

    SharedPrefsManager sharedPrefsManager;
    private DrawerArrowDrawable arrow;
    private String projectId;

    private List<PhotoModel> lastData;

    WorkerResultReceiver mWorkerResultReceiver;
    private View mPhotosDetails;
    private View mProjectCamera;
    private View mPlansDetails;
    private View mDefectsDetails;
    private TextView tv_email,tv_title,tv_company_name;
    ImageView imageView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_project_detail_with_nevigation);
        bindView2();
        bindView();
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        setDifferenScreensOrientations();
        navigationView = findViewById(R.id.nav_view);
        sharedPrefsManager = new SharedPrefsManager(this);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close) {
            @Override
            public void onDrawerOpened(View drawerView) {
                Utils.hideSoftKeyboard(ProjectDetailActivity.this);
                populateLeftMenuHeaderData();
                super.onDrawerOpened(drawerView);
            }
        };
        drawer.addDrawerListener(toggle);
        toggle.syncState();
        arrow = toggle.getDrawerArrowDrawable();
        if (arrow != null)
            arrow.setColor(getResources().getColor(R.color.white));

        projectDetailViewModel = ViewModelProviders.of(this).get(ProjectDetailViewModel.class);
        projectDetailViewModel.InitRepo(getIntent().getStringExtra("projectId"));
        projectId = getIntent().getStringExtra("projectId");
        populateLeftMenuHeaderData();
        projectDetailViewModel.getProjectDetailModel().observe(this, projectUserModels -> {

            if (projectUserModels == null || projectUserModels.size() == 0) {
                projectDetailViewModel.getProjectDetailRepository().callGetListAPI(getApplication(), getIntent().getStringExtra("projectId"));
            }
        });
        Log.d("projectId", getIntent().getStringExtra("projectId"));

        Observer observer = new Observer<ProjectModel>() {
            @Override
            public void onChanged(ProjectModel projectModel) {
                if (projectModel != null) {
                    projectModel.setLastOpen(new Date().getTime());
                    projectDetailViewModel.setLastOpen(projectModel);
                }
                // update UI
                updateUI();

                projectDetailViewModel.getProjectModel().removeObservers(ProjectDetailActivity.this);
            }
        };
        projectDetailViewModel.getProjectModel().observe(ProjectDetailActivity.this, observer);

        projectDetailViewModel.getProjectModel().observe(this, projectModel -> {
            if (projectModel != null) {
                projectModel.setLastOpen(new Date().getTime());
                projectDetailViewModel.setLastOpen(projectModel);
            }
            // update UI
            updateUI();
        });

        ll_header_info_view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showCustomDialogUnSyncData(ProjectDetailActivity.this, getResources().getString(R.string.custom_dialog_title), "Are you sure you want to unSync all projects and delete all data against it.", 2, 0);

            }
        });


        new GetCountAsyncTask().execute();

        String projectId = getIntent().getStringExtra("projectId");

        localPhotosViewModel = ViewModelProviders.of(this).get(LocalPhotosViewModel.class);
        localPhotosViewModel.init(projectId);
        localPhotosViewModel.refreshData(projectId);


        localPhotosViewModel.getAllPhotos().observe(this, new Observer<List<PhotoModel>>() {
            @Override
            public void onChanged(List<PhotoModel> photoModels) {
                Utils.showLogger("newDataObserved");
                lastData = photoModels;
            }
        });

        localPhotosViewModel.isStartUploadPhoto.observe(this, new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean aBoolean) {
                Utils.showLogger("PlanDetailActivity syncing started");

                startBackgroundTask(ProjectDetailActivity.this, mWorkerResultReceiver, projectId, false);

            }
        });

        mWorkerResultReceiver = new WorkerResultReceiver(new Handler());
        mWorkerResultReceiver.setReceiver(ProjectDetailActivity.this);


//        toolbar.setNavigationOnClickListener(view -> {
//            onBackPressed();
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
        } else if ((screenSize == Configuration.SCREENLAYOUT_SIZE_XLARGE || screenSize == Configuration.SCREENLAYOUT_SIZE_LARGE
                || screenSize == Configuration.SCREENLAYOUT_SIZE_NORMAL)
                && orientation == Configuration.ORIENTATION_LANDSCAPE) {
            landScapeMode();
        }

    }

    private void landScapeMode() {

        LinearLayout.LayoutParams lpParent = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
//        ll_parent.setLayoutParams(lpParent);
        ll_parent.setWeightSum(2);
        ll_parent.setOrientation(LinearLayout.HORIZONTAL);

        LinearLayout.LayoutParams lpSection1 = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1);
        ll_section_1.setLayoutParams(lpSection1);
        ll_section_1.setOrientation(LinearLayout.VERTICAL);

        LinearLayout.LayoutParams lpSection2 = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1);
        lpSection2.setMargins(25, 90, 5, 10);
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


    public void showCustomDialogUnSyncData(final Activity act, String title, String msgToShow, Integer noOfButtons, Integer flag) {
        //customDialog = new Dialog(act, R.style.customDialogTheme);
        Dialog customDialog = new Dialog(act, R.style.MyDialogTheme);
        customDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        customDialog.setContentView(R.layout.custom_dialog_message_material);
        if (flag == 2) {
            customDialog.setCancelable(false);
        }
        customDialog.setCanceledOnTouchOutside(false);
        WindowManager.LayoutParams params = customDialog.getWindow().getAttributes();
        params.y = 10;
        // set the custom dialog components - text, image and button
        TextView titleTxt = (TextView) customDialog.findViewById(R.id.customDialog_titleText);
        if (!title.equals(""))
            titleTxt.setText(title);
        TextView text = (TextView) customDialog.findViewById(R.id.movie_name);
        text.setText(msgToShow);
        //		if(Navigator.showCustomDialogType ==3)
        //		{
        //			text.setTextSize(getDipValue(8));
        //		}
        Button bt = (Button) customDialog.findViewById(R.id.customDialog_okBtn);
        bt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                new ProjectSyncManager(HomeActivity.this, projectId, syncTaskComplete, false);
                new logoutUserAsyncTask(ProjectDetailActivity.this).execute();
                Intent intent = new Intent(ProjectDetailActivity.this, LoginActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                finish();
                customDialog.dismiss();
            }
        });
        if (noOfButtons == 2) {
            Button bt1 = (Button) customDialog.findViewById(R.id.customDialog_cancel);
            bt1.setVisibility(View.VISIBLE);
            bt1.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    customDialog.dismiss();
                }
            });
        }
        customDialog.show();
    }

    //    @Override
    private boolean navigationItemClick(View view) {
        // Handle navigation view item clicks here.
        SharedPrefsManager sharedPrefsManager = new SharedPrefsManager(this);
        int id = view.getId();

        if (id == R.id.project_item) {
            finish();
            // Handle the camera action
//            startActivity(new Intent(this, ProjectDetailActivity.class).putExtra("projectId", sharedPrefsManager.getLastProjectId(ProjectDetailActivity.this)));
        } else if (id == R.id.nav_camera) {
            // Handle the camera action
            openCameraActivity();
        } else if (id == R.id.nav_plans) {
            startActivity(new Intent(this, PlansActivity.class).putExtra("projectId", sharedPrefsManager.getLastProjectId(ProjectDetailActivity.this)));
        } else if (id == R.id.nav_photos) {
            startActivity(new Intent(this, PhotosActivity.class).putExtra("projectId", sharedPrefsManager.getLastProjectId(ProjectDetailActivity.this)));
        } else if (id == R.id.nav_defects_management) {
            startActivity(new Intent(this, DefectsActivity.class).putExtra("projectId", sharedPrefsManager.getLastProjectId(ProjectDetailActivity.this)));
        } else if (id == R.id.nav_settings) {
            startActivity(new Intent(this, SettingsActivity.class));
        } else if (id == R.id.nav_about) {
            startActivity(new Intent(this, InfoActivity.class));
        } else if (id == R.id.nav_manual) {
            startActivity(new Intent(this, AppManualActivity.class));
        }

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void openCameraActivity() {
        startActivityForResult(new Intent(this, SavePictureActivity.class).putExtra("projectId", getIntent().getStringExtra("projectId")),SavePictureActivity.KEY_ON_ACTIVITY_RESULT);
    }

    @Override
    protected void onResume() {
        super.onResume();
        navigationViewRV.setLayoutManager(new LinearLayoutManager(this));
        navigationViewRV.setAdapter(new NavigationViewRecyclerAdapter(this, (view, position) -> navigationItemClick(view)));
    }

    private void updateUI() {
        ProjectModel project = projectDetailViewModel.getProjectModel().getValue();
        if (project != null) {
            SharedPrefsManager sharedPrefsManager = new SharedPrefsManager(this);
            sharedPrefsManager.setLastProjectId(this, project.getProjectid());
            sharedPrefsManager.setLastProjectName(this, project.getProject_name());
            sharedPrefsManager.setLastProjectPhoto(this, project.getCacheImagePath());
            projectName.setText(project.getProject_name());
//            projectDetail.setText(project.getCompany_name());
            projectDetail.setText(project.getCity());
//            if (project.getFlawcount() == null || project.getFlawcount().isEmpty()) {
//                numberDefects.setText("0");
//            } else {
//                numberDefects.setText(project.getFlawcount());
//            }
            if (project.getPhotocount() == null || project.getPhotocount().isEmpty()) {
                numberPhotos.setText("0");
            } else {
                numberPhotos.setText(project.getPhotocount());
            }

            if (project != null && project.getCacheImagePath() != null && !project.getCacheImagePath().equals("")) {

                File imgFile = new File(project.getCacheImagePath());

                if (imgFile.exists()) {
                    Bitmap myBitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
                    project_image.setImageBitmap(myBitmap);

                }
            }
        }

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                project_image.setScale(1.7f);
            }
        }, 50);

        project_image.setZoomable(false);
    }

    private void bindView2() {
        tv_title = findViewById(R.id.tv_title);
        tv_email = findViewById(R.id.tv_email);
        tv_company_name = findViewById(R.id.tv_company_name);
        imageView = findViewById(R.id.imageView);

    }

    private void populateLeftMenuHeaderData() {
        tv_title.setText(sharedPrefsManager.getStringValue(AppConstantsManager.USER_FIRST_NAME, "") + " " + sharedPrefsManager.getStringValue(AppConstantsManager.USER_LAST_USED_FLAW_TYPE, ""));
        tv_email.setText(sharedPrefsManager.getStringValue(AppConstantsManager.USER_COMPANY, ""));
        if (sharedPrefsManager.getLastProjectName(this) != null && !sharedPrefsManager.getLastProjectName(this).equals("")) {
            tv_company_name.setText(sharedPrefsManager.getLastProjectName(this));

        }

        if (sharedPrefsManager.getLastProjectPhoto(this) != null && !sharedPrefsManager.getLastProjectPhoto(this).equals("")) {

            File imgFile = new File(sharedPrefsManager.getLastProjectPhoto(this));

            if (imgFile.exists()) {

                Glide.with(this)
                        .asBitmap()
                        .load(imgFile.getAbsolutePath())
                        .apply(RequestOptions.circleCropTransform())
                        .into(imageView);

//                    Glide.with(this) .asBitmap()
//                            .load(imgFile.getAbsolutePath()).centerCrop().into(new BitmapImageViewTarget(includedLayout_ll_header_info_view.imageView) {
//                        @Override
//                        protected void setResource(Bitmap resource) {
//                            RoundedBitmapDrawable circularBitmapDrawable =
//                                    RoundedBitmapDrawableFactory.create(getResources(), resource);
//                            circularBitmapDrawable.setCircular(true);
//                            includedLayout_ll_header_info_view.imageView.setImageDrawable(circularBitmapDrawable);
//                        }
//                    });

            }
        }
        navigationViewRV.setLayoutManager(new LinearLayoutManager(this));
        navigationViewRV.setAdapter(new NavigationViewRecyclerAdapter(this, (view, position) -> navigationItemClick(view)));

    }


    private void onPhotoDetailsClick() {
        startActivity(new Intent(this, PhotosActivity.class).putExtra("projectId", getIntent().getStringExtra("projectId")));
    }

    private void onCameraDetailsClick() {
       openCameraActivity();
        // startActivity(new Intent(this, SavePictureActivity.class).putExtra("projectId", getIntent().getStringExtra("projectId")));
    }

    private void onPlansDetailsClick() {
        startActivity(new Intent(this, PlansActivity.class).putExtra("projectId", getIntent().getStringExtra("projectId")));
    }

    private void onDefectsDetailsClick() {
        startActivity(new Intent(this, DefectsActivity.class).putExtra("projectId", getIntent().getStringExtra("projectId")).putExtra(IS_FROM_DEFECT_KEY, true));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

    }

    private void bindView() {
        toolbar =   findViewById(R.id.toolbar);
        projectName =   findViewById(R.id.project_name);
        projectDetail =   findViewById(R.id.project_detail);
        numberPhotos =   findViewById(R.id.number_photos);
        numberPlans =   findViewById(R.id.number_plans);
        numberDefects =   findViewById(R.id.number_defects);
        project_image =   findViewById(R.id.project_image);
        ll_parent =   findViewById(R.id.ll_parent);
        ll_section_1 =   findViewById(R.id.ll_section_1);
        ll_section_2 =   findViewById(R.id.ll_section_2);
        navigationViewRV =   findViewById(R.id.list_menu_items);
        ll_header_info_view =   findViewById(R.id.ll_header_info_view);
        mPhotosDetails =   findViewById(R.id.photos_details);
        mProjectCamera =   findViewById(R.id.project_camera);
        mPlansDetails =   findViewById(R.id.plans_details);
        mDefectsDetails =   findViewById(R.id.defects_details);
        mPhotosDetails.setOnClickListener(v -> {
            onPhotoDetailsClick();
        });
        mProjectCamera.setOnClickListener(v -> {
            onCameraDetailsClick();
        });
        mPlansDetails.setOnClickListener(v -> {
            onPlansDetailsClick();
        });
        mDefectsDetails.setOnClickListener(v -> {
            onDefectsDetailsClick();
        });
    }


    private class logoutUserAsyncTask extends AsyncTask<Void, Void, Void> {
        private ProjectsDatabase database;

        logoutUserAsyncTask(Context context) {
            database = ProjectsDatabase.getDatabase(context);
        }

        @Override
        protected Void doInBackground(final Void... params) {
            ProjectsDatabase.getDatabase(ProjectDetailActivity.this).clearAllTables();
            SharedPrefsManager sharedPrefsManager = new SharedPrefsManager(ProjectDetailActivity.this);
            sharedPrefsManager.sharedPreferences.edit().clear().commit();

            return null;
        }

    }


    private class GetCountAsyncTask extends AsyncTask<Void, Void, Void> {

        long photoCount = 0;
        long defectCount = 0;
        long planCount = 0;

        @Override
        protected Void doInBackground(Void... params) {
            planCount = ProjectsDatabase.getDatabase(ProjectDetailActivity.this).plansDao().getAllPlansAgaintProject(projectId);
            photoCount = ProjectsDatabase.getDatabase(ProjectDetailActivity.this).photoDao().getAllPhotoCountProject(projectId);
            defectCount = ProjectsDatabase.getDatabase(ProjectDetailActivity.this).defectsDao().getdefectCountofProject(projectId);
            return null;
        }

        @Override
        protected void onPostExecute(Void integer) {
            super.onPostExecute(integer);

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (numberPlans != null) {
                        if (planCount > 0) {
                            numberPlans.setText(planCount + "");
                        } else {
                            numberPlans.setText("0");
                        }
                    }

//                    if(photoCount>0){
//                        numberPhotos.setText(photoCount+"");
//                    }else{
//                        numberPhotos.setText("0");
//                    }
                    if (numberDefects != null) {
                        if (defectCount > 0) {
                            numberDefects.setText(defectCount + "");
                        } else {
                            numberDefects.setText("0");
                        }
                    }
                }
            });
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case SavePictureActivity.KEY_ON_ACTIVITY_RESULT:
//                if (resultCode == Activity.RESULT_OK) {
//                if (ProjectDocuUtilities.isNetworkConnected(PhotosActivity.this) || ProjectNavigator.wlanIsConnected(PhotosActivity.this)) {

                if (data != null && data.getExtras() != null && data.getExtras().get("photoId") != null && !data.getExtras().get("photoId").equals("")) {
                    int position = data.getExtras().getInt("position");
                    Utils.showLogger("PhotoActivity onActivityResult");
                    //   new ReterivePhotoObjectAsyncTask(position).execute(data.getExtras().get("photoId") + "");

                    if (ProjectDocuUtilities.isNetworkConnected(ProjectDetailActivity.this) || ProjectNavigator.wlanIsConnected(ProjectDetailActivity.this)) {
                        Utils.showLogger("syncAllPhotos>"+"PhotosActivity 473");
                        Handler handler = new Handler();
                        handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {


                                    syncAllPhotos(false);//syncing from button

                            }
                        },400);
                    }
                }

//                }//                }
                break;
        }


    }


    public void syncAllPhotos(boolean isAutoSync) {
        Utils.showLogger("PhotoActivity syncAllPhotos"+isAutoSync);
        boolean isBreakOuterLoop = false;
        boolean isCalledBackgroundtask = false;


        localPhotosViewModel.isAutoSyncPhoto = isAutoSync;

        List<PhotoModel> photoModelList = lastData;
        if(photoModelList==null)
            return;
        if (photoModelList != null && photoModelList.size() > 0) {

            boolean isTrue = sharedPrefsManager.getBooleanValue(AppConstantsManager.SYNC_PHOTO_ASC_ORDER, AppConstantsManager.SYNC_PHOTO_ASC_ORDER_DEFAULT_VALUE);
            if (sharedPrefsManager.getBooleanValue(AppConstantsManager.SYNC_PHOTO_ASC_ORDER, AppConstantsManager.SYNC_PHOTO_ASC_ORDER_DEFAULT_VALUE)) {

                for (int i = photoModelList.size() - 1; i >= 0; i--) {
                    if (localPhotosViewModel != null && isAutoSync) {
                        PhotoModel photoModel = photoModelList.get(i);
                        //Utils.showLogger("old_status_is"+photoModel.getPhotoUploadStatus()+photoModel.getFailedCount());
                        if (ProjectNavigator.mobileNetworkIsConnected(ProjectDetailActivity.this) || ProjectNavigator.wlanIsConnected(ProjectDetailActivity.this)) {

                            if (!photoModel.isPhotoSynced() && (photoModel.getPhotoUploadStatus().equalsIgnoreCase(LocalPhotosRepository.UN_SYNC_PHOTO) && photoModel.getFailedCount() <= LocalPhotosRepository.MAX_PHOTO_FAILED_COUNT
                                    || photoModel.getPhotoUploadStatus().equalsIgnoreCase(LocalPhotosRepository.UPLOADING_PHOTO) && photoModel.getFailedCount() <= LocalPhotosRepository.MAX_PHOTO_FAILED_COUNT)) {



                                photoModel.setPhotoUploadStatus(LocalPhotosRepository.UPLOADING_PHOTO);
                                photoModel.setUserSelectedStatus(false);
                                new UpdateAsyncTask().execute(photoModel);
                                isCalledBackgroundtask = true;
                                isBreakOuterLoop = true;
                                break;
                            }
                        } else {
                            if (!photoModel.isPhotoSynced() && !photoModel.getPhotoUploadStatus().equalsIgnoreCase(LocalPhotosRepository.SYNCED_PHOTO) && photoModel.getFailedCount() <= LocalPhotosRepository.MAX_PHOTO_FAILED_COUNT) {
                                photoModel.setPhotoUploadStatus(LocalPhotosRepository.UPLOADING_PHOTO);
                                photoModel.setUserSelectedStatus(false);
                                new UpdateAsyncTask().execute(photoModel);
                                isCalledBackgroundtask = true;
                                isBreakOuterLoop = true;
                                break;
                            }
                        }
                    } else {
                        PhotoModel photoModel = photoModelList.get(i);
                        if (!photoModel.isPhotoSynced() && !photoModel.getPhotoUploadStatus().equalsIgnoreCase(LocalPhotosRepository.SYNCED_PHOTO) && photoModel.getFailedCount() <= LocalPhotosRepository.MAX_PHOTO_FAILED_COUNT) {
                            photoModel.setPhotoUploadStatus(LocalPhotosRepository.UPLOADING_PHOTO);
                            photoModel.setUserSelectedStatus(true);
                            isCalledBackgroundtask = true;
                            new UpdateAsyncTask().execute(photoModel);
                        }
                    }
                }
            } else {
                for (int i = 0; i < photoModelList.size(); i++) {
//                        for (int i = photoModelList.size() - 1; i >= 0; i--) {
                    if (localPhotosViewModel != null && isAutoSync) {
                        PhotoModel photoModel = photoModelList.get(i);
                        if (ProjectNavigator.mobileNetworkIsConnected(ProjectDetailActivity.this) || ProjectNavigator.wlanIsConnected(ProjectDetailActivity.this)) {

                            if (!photoModel.isPhotoSynced() && (photoModel.getPhotoUploadStatus().equalsIgnoreCase(LocalPhotosRepository.UN_SYNC_PHOTO) && photoModel.getFailedCount() <= LocalPhotosRepository.MAX_PHOTO_FAILED_COUNT
                                    || photoModel.getPhotoUploadStatus().equalsIgnoreCase(LocalPhotosRepository.UPLOADING_PHOTO) && photoModel.getFailedCount() <= LocalPhotosRepository.MAX_PHOTO_FAILED_COUNT)) {
                                photoModel.setPhotoUploadStatus(LocalPhotosRepository.UPLOADING_PHOTO);
                                photoModel.setUserSelectedStatus(false);
                                new UpdateAsyncTask().execute(photoModel);
                                isCalledBackgroundtask = true;
                                isBreakOuterLoop = true;
                                break;
                            }
                        } else {
                            if (!photoModel.isPhotoSynced() && !photoModel.getPhotoUploadStatus().equalsIgnoreCase(LocalPhotosRepository.SYNCED_PHOTO) && photoModel.getFailedCount() <= LocalPhotosRepository.MAX_PHOTO_FAILED_COUNT) {
                                photoModel.setPhotoUploadStatus(LocalPhotosRepository.UPLOADING_PHOTO);
                                photoModel.setUserSelectedStatus(false);
                                new UpdateAsyncTask().execute(photoModel);
                                isCalledBackgroundtask = true;
                                isBreakOuterLoop = true;
                                break;
                            }
                        }
                    } else {
                        PhotoModel photoModel = photoModelList.get(i);
                        if (!photoModel.isPhotoSynced() && !photoModel.getPhotoUploadStatus().equalsIgnoreCase(LocalPhotosRepository.SYNCED_PHOTO) && photoModel.getFailedCount() <= LocalPhotosRepository.MAX_PHOTO_FAILED_COUNT) {
                            photoModel.setPhotoUploadStatus(LocalPhotosRepository.UPLOADING_PHOTO);
                            photoModel.setUserSelectedStatus(true);
                            isCalledBackgroundtask = true;
                            new UpdateAsyncTask().execute(photoModel);
                        }
                    }
                }
            }

        }



        Utils.showLogger("isCalledBackgroundtask>>"+isCalledBackgroundtask);
        if (isCalledBackgroundtask) {
            Intent intent = new Intent("updateProfile");
            sendBroadcast(intent);

            if (localPhotosViewModel != null) {
                localPhotosViewModel.isStartUploadPhoto.postValue(true);
            }
        }

//        Intent msgIntent = new Intent(getApplication(), SyncLocalPhotosService.class);
//        msgIntent.putExtra(SyncLocalPhotosService.PROJECT_ID,projectID);
//        if(localPhotosViewModel!=null)
//        msgIntent.putExtra(SyncLocalPhotosService.IS_PHOTOS_AUTO_SYNC,localPhotosViewModel.isAutoSyncPhoto);
//        getApplication().startService(msgIntent);
    }


    public void startBackgroundTask(Context context, WorkerResultReceiver mWorkerResultReceiver, String projectID, boolean isAutoSyncPhotos) {
        Utils.showLogger("startBackgroundTask");
        SyncLocalPhotosService.enqueueWork(context, mWorkerResultReceiver, projectID, isAutoSyncPhotos);
    }

    @Override
    public void onReceiveResult(int resultCode, Bundle resultData) {
        switch (resultCode) {
            case SHOW_RESULT:
                boolean isAutoSyncPhoto = false;

                break;
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
            Log.d("up_id_t_clk", params[0].getPdphotolocalId() + " - " + params[0].getCreated());

            return null;
        }
    }


}
