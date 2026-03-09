package com.projectdocupro.mobile.activities;

import android.app.Activity;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.MutableLiveData;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.ViewPager;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.tabs.TabLayout;
import com.projectdocupro.mobile.ProjectsDatabase;
import com.projectdocupro.mobile.R;
import com.projectdocupro.mobile.adapters.NavigationViewRecyclerAdapter;
import com.projectdocupro.mobile.adapters.ProjectsPagerAdapter;
import com.projectdocupro.mobile.adapters.ProjectsRecyclerAdapter;
import com.projectdocupro.mobile.dao.ProjectDao;
import com.projectdocupro.mobile.fragments.AllFragment;
import com.projectdocupro.mobile.fragments.FavoritesFragment;
import com.projectdocupro.mobile.fragments.LastUsedFragment;
import com.projectdocupro.mobile.fragments.add_direction.Flags;
import com.projectdocupro.mobile.fragments.add_direction.ProjectDocuUtilities;
import com.projectdocupro.mobile.interfaces.ICacheImagesInDB;
import com.projectdocupro.mobile.interfaces.ISyncTaskComplete;
import com.projectdocupro.mobile.interfaces.ProjectsListItemClickListener;
import com.projectdocupro.mobile.interfaces.RetroApiInterface;
import com.projectdocupro.mobile.managers.AppConstantsManager;
import com.projectdocupro.mobile.managers.RetrofitManager;
import com.projectdocupro.mobile.managers.SharedPrefsManager;
import com.projectdocupro.mobile.models.PhotoModel;
import com.projectdocupro.mobile.models.ProjectModel;
import com.projectdocupro.mobile.receivers.WorkerResultReceiver;
import com.projectdocupro.mobile.repos.LocalPhotosRepository;
import com.projectdocupro.mobile.service.SyncLocalPhotosService;
import com.projectdocupro.mobile.utility.Utils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;


import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HomeActivity extends AppCompatActivity implements ICacheImagesInDB,
        AllFragment.OnFragmentInteractionListener, LastUsedFragment.OnFragmentInteractionListener,
        FavoritesFragment.OnFragmentInteractionListener, ISyncTaskComplete, ProjectsListItemClickListener, WorkerResultReceiver.Receiver {

    TestKotlin testKotlin;
    public static MutableLiveData<Boolean> isAllProjectsFromServerLoaded = new MutableLiveData<>();

    private static final String TAG = "HomeActivity";
    ArrayList<String> listOfLastUpdatedProjectIds = new ArrayList<>();
    ArrayList<String> listOfLastUpdatedProjectIdsFailureCase = new ArrayList<>();
    public MutableLiveData<List<ProjectModel>> allProjectsUpdates = new MutableLiveData<>();

    public ProjectsPagerAdapter projectsPagerAdapter;
    private NavigationView navigationView;
    public static Flags flags = new Flags();
    private ViewPager viewPager;

    private TabLayout pagerTabStrip;

    private RecyclerView navigationViewRV;

    SharedPrefsManager sharedPrefsManager;
    private String imagePath;
    private Dialog customDialog;
    ProjectModel selectedProjectModel;
    ISyncTaskComplete syncTaskComplete;
    ProjectsRecyclerAdapter projectsRecyclerAdapter;
    String projectId = "";

    WorkerResultReceiver mWorkerResultReceiver;

    private View ll_header_info_view;

    private float density;
    private BroadcastReceiver updateProjectData;
    public static final String BR_ACTION_LAST_UPDATED_PROJECT = "br_last_update_project";
    public static final String KEY_LAST_UPDATED_PROJECT_IDS_LIST = "key_list_last_update_project";
    private ProjectsRecyclerAdapter adapter;
    private int position = 0;
    private boolean isAutoServerSyncStart;

    int listSize = 0;
    int cachedImagePos = -1;
    private TextView tv_email,tv_title,tv_company_name;
    ImageView imageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        ll_header_info_view = findViewById(R.id.ll_header_info_view);
        isAllProjectsFromServerLoaded .postValue(false);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        density = getResources().getDisplayMetrics().density;
        Log.d("density", density + "");
        sharedPrefsManager = new SharedPrefsManager(this);
        syncTaskComplete = this;
//        FloatingActionButton fab = findViewById(R.id.fab);
//        fab.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {x
//                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
//                        .setAction("Action", null).show();
//            }
//        });
//        startActivity(new Intent(this, ProjectDocuMainActivity.class));
        mWorkerResultReceiver = new WorkerResultReceiver(new Handler());
        mWorkerResultReceiver.setReceiver(this);
        bindView(ll_header_info_view);
        bindView2();
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);
        addEvent();


        ll_header_info_view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                if (!sharedPrefsManager.getLastProjectId(HomeActivity.this).equals(""))
                showCustomDialogLogoutUser(HomeActivity.this, getResources().getString(R.string.custom_dialog_title), getResources().getString(R.string.user_logout_msg), 2, 0);

            }
        });


        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close) {
            @Override
            public void onDrawerOpened(View drawerView) {
                Utils.hideSoftKeyboard(HomeActivity.this);
                populateLeftMenuHeaderData();
                super.onDrawerOpened(drawerView);
            }
        };
        drawer.addDrawerListener(toggle);
        toggle.syncState();

//        navigationView.setNavigationItemSelectedListener(this);

        projectsPagerAdapter = new ProjectsPagerAdapter(this, getSupportFragmentManager());

       viewPager.setOffscreenPageLimit(3);

        viewPager.setAdapter(projectsPagerAdapter);
//        if (sharedPrefsManager.getFavouriteProjectBooleanValue()) {
//            viewPager.setCurrentItem(0);
//        } else {
//            viewPager.setCurrentItem(1);
//        }
        new SelectTabAsyncTask(this).execute();
        new UnsyncedPhotosAsyncTask(this).execute();
        populateLeftMenuHeaderData();
        pagerTabStrip.setupWithViewPager(viewPager);


    }

    private void bindView2() {
        tv_title = findViewById(R.id.tv_title);
        tv_email = findViewById(R.id.tv_email);
        tv_company_name = findViewById(R.id.tv_company_name);
        imageView = findViewById(R.id.imageView);

    }

    private void populateLeftMenuHeaderData() {
        tv_title.setText(sharedPrefsManager.getStringValue(AppConstantsManager.USER_FIRST_NAME, "") + " " + sharedPrefsManager.getStringValue(AppConstantsManager.USER_LAST_NAME, ""));
        tv_email.setText(sharedPrefsManager.getStringValue(AppConstantsManager.USER_COMPANY, ""));
        if (sharedPrefsManager.getLastProjectName(this) != null && !sharedPrefsManager.getLastProjectName(this).equals("")) {
            tv_company_name.setVisibility(View.VISIBLE);
            tv_company_name.setText(sharedPrefsManager.getLastProjectName(this));

            if (sharedPrefsManager.getLastProjectPhoto(this) != null && !sharedPrefsManager.getLastProjectPhoto(this).equals("")) {

                File imgFile = new File(sharedPrefsManager.getLastProjectPhoto(this));

                if (imgFile.exists()) {

                    Glide.with(this)
                            .asBitmap()
                            .load(imgFile.getAbsolutePath())
                            .apply(RequestOptions.circleCropTransform())
                            .into(imageView);

                }
            }
            navigationViewRV.setLayoutManager(new LinearLayoutManager(this));
            navigationViewRV.setAdapter(new NavigationViewRecyclerAdapter(this, (view, position) -> navigationItemClick(view)));

        } else {
            tv_company_name.setVisibility(View.INVISIBLE);
        }

//        else{
//            includedLayout_ll_header_info_view.tv_company_name.setText(sharedPrefsManager.getStringValue(AppConstantsManager.USER_COMPANY,""));
//
//        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                registerReceiver(updateProjectData, new IntentFilter(BR_ACTION_LAST_UPDATED_PROJECT),RECEIVER_EXPORTED);
            }
        }
        catch (Exception e){
            e.printStackTrace();
        }

        navigationViewRV.setLayoutManager(new LinearLayoutManager(this));
        navigationViewRV.setAdapter(new NavigationViewRecyclerAdapter(this, (view, position) -> navigationItemClick(view)));

//        MenuItem    projectItem =   navigationView.getMenu().findItem(R.id.project_item);
//
//        if (sharedPrefsManager.getLastProjectId(this).isEmpty()){
//            projectItem.setVisible(false);
//        }else{
//            projectItem.setVisible(true);
//            projectItem.setTitle(getString(R.string.project)+" "+sharedPrefsManager.getLastProjectName(this));
//        }
//        SpannableString s = new SpannableString(projectItem.getTitle());
//        s.setSpan(new TextAppearanceSpan(this, R.style.TextAppearance44), 0, s.length(), 0);
//        projectItem.setTitle(s);
//
//        projectItem.setOnMenuItemClickListener(item -> {
//            startActivity(new Intent(HomeActivity.this,ProjectDetailActivity.class).putExtra("projectId",sharedPrefsManager.getLastProjectId(HomeActivity.this)));
//            return false;
//        });
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.home, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_add_project) {
//            new DeleteALLProjectsAsyncTask().execute();
            startActivityForResult(new Intent(this, NewProjectActivity.class), AppConstantsManager.REQUEST_CODE_NEW_ACTIVITY);
//            startActivity(new Intent(this, ProjectDocuMainActivity.class));

            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    //    @Override
    private boolean navigationItemClick(View view) {
        // Handle navigation view item clicks here.
        SharedPrefsManager sharedPrefsManager = new SharedPrefsManager(this);
        int id = view.getId();

        if (id == R.id.project_item) {
            // Handle the camera action
            startActivity(new Intent(this, ProjectDetailActivity.class).putExtra("projectId", sharedPrefsManager.getLastProjectId(HomeActivity.this)));
        } else if (id == R.id.nav_camera) {
            // Handle the camera action
            startActivity(new Intent(this, SavePictureActivity.class).putExtra("projectId", sharedPrefsManager.getLastProjectId(HomeActivity.this)));
        } else if (id == R.id.nav_plans) {
            startActivity(new Intent(this, PlansActivity.class).putExtra("projectId", sharedPrefsManager.getLastProjectId(HomeActivity.this)));
        } else if (id == R.id.nav_photos) {
            startActivity(new Intent(this, PhotosActivity.class).putExtra("projectId", sharedPrefsManager.getLastProjectId(HomeActivity.this)));
        } else if (id == R.id.nav_defects_management) {
            startActivity(new Intent(this, DefectsActivity.class).putExtra("projectId", sharedPrefsManager.getLastProjectId(HomeActivity.this)));
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


    @Override
    protected void onDestroy() {


        if (updateProjectData != null) {
            unregisterReceiver(updateProjectData);
            updateProjectData = null;
        }
        super.onDestroy();
    }

    @Override
    public void onAllFragmentInteraction(Uri uri) {

    }

    @Override
    public void onFavoritesFragmentInteraction(Uri uri) {

    }

    @Override
    public void onLastUsedFragmentInteraction(Uri uri) {

    }

    private boolean writeResponseBodyToDisk(ResponseBody body, String projectId) {
        imagePath = "";
        try {
            // todo change the file location/name according to your needs

//            File dir = new File(Environment.getExternalStorageDirectory() + "/projectDocu/project_" + projectId);
            File dir = this.getExternalFilesDir("/projectDocu/project_" + projectId);
            if (dir == null) {
                dir = this.getFilesDir();
            }
            if (!dir.isDirectory()) {
                dir.mkdirs();
            }
            File photo = new File(dir, "/Download_" + new Date().getTime() + ".jpg");

            imagePath = photo.getAbsolutePath();

            InputStream inputStream = null;
            OutputStream outputStream = null;

            try {
                byte[] fileReader = new byte[15000];

                long fileSize = body.contentLength();
                long fileSizeDownloaded = 0;

                inputStream = body.byteStream();
                outputStream = new FileOutputStream(photo);

                while (true) {
                    int read = inputStream.read(fileReader);

                    if (read == -1) {
                        break;
                    }

                    outputStream.write(fileReader, 0, read);

                    fileSizeDownloaded += read;

                    Log.d("A TAG", "file download: " + fileSizeDownloaded + " of " + fileSize);
                }


                outputStream.flush();

                return true;
            } catch (IOException e) {
                return false;
            } finally {
                if (inputStream != null) {
                    inputStream.close();
                }

                if (outputStream != null) {
                    outputStream.close();
                }
            }
        } catch (IOException e) {
            return false;
        }
    }

    @Override
    public void onSuccess(String projectId, boolean isSync) {
        Log.d("HomeActivitySuccess", "Success");
        SharedPrefsManager sharedPrefsManager = new SharedPrefsManager(this);
        if (selectedProjectModel != null) {
            if (selectedProjectModel.isFavorite() && selectedProjectModel.getSyncStatus() != null && selectedProjectModel.getSyncStatus().equals(LocalPhotosRepository.SYNCED_PHOTO)) {
                sharedPrefsManager.setFavouriteProjetBooleanValue(true);
            } else {
                sharedPrefsManager.setFavouriteProjetBooleanValue(false);
            }
            new updateProjectAsyncTask(this, isSync).execute(projectId);
        }


//        if (projectsRecyclerAdapter != null) {
//            projectsRecyclerAdapter.notifyDataSetChanged();
//        }

    }

    private void bindView(View bindSource) {
        viewPager = findViewById(R.id.home_pager);
        pagerTabStrip = findViewById(R.id.tabs);
        navigationViewRV = findViewById(R.id.list_menu_items);
        ll_header_info_view = findViewById(R.id.ll_header_info_view);
    }

    private class updateProjectAsyncTask extends AsyncTask<String, Void, Void> {
        private ProjectsDatabase database;
        private ProjectDao projectDao;
        boolean isSync;
        int position = 0;

        updateProjectAsyncTask(Context context, boolean isSynC) {
            database = ProjectsDatabase.getDatabase(context);
            isSync = isSynC;
            projectDao = database.projectDao();
        }

        @Override
        protected Void doInBackground(final String... params) {
            projectId = params[0];
            ProjectModel projectModel = projectDao.getProjectOBJ(params[0]);
            if (projectModel != null) {
                if (isSync) {
                    projectModel.setFavorite(true);
                    projectModel.setSyncStatus(LocalPhotosRepository.SYNCED_PHOTO);
                } else {
                    projectModel.setFavorite(false);
                    projectModel.setLastOpen(0l);
                    projectModel.setSyncStatus(LocalPhotosRepository.UN_SYNC_PHOTO);
                }
                projectModel.setLastUpdatedProjectStatus(LocalPhotosRepository.SYNCED_PHOTO);

                projectDao.update(projectModel);
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
//            Intent intentt = new Intent(SyncLocalPhotosService.BR_ACTION_UPDATE_PROJECT_LIST);
//            if (projectId != null && !projectId.equals(""))
//                intentt.putExtra(SyncLocalPhotosService.PROJECT_ID, projectId);
//               sendBroadcast(intentt);

            if (listOfLastUpdatedProjectIds != null && listOfLastUpdatedProjectIds.size() > 0) {

                Fragment fragment = projectsPagerAdapter.m2ndFragment;

                if (fragment != null && fragment instanceof AllFragment) {
                    adapter = ((AllFragment) fragment).adapter;
                }

                if (fragment != null && adapter != null && listOfLastUpdatedProjectIds != null && listOfLastUpdatedProjectIds.size() > 0) {
//                    projectId = listOfLastUpdatedProjectIds.get(0);
                    if (projectId != null && !projectId.equals("") && adapter != null) {

                        for (int i = 0; i < adapter.getProjectsData().size(); i++) {
                            if (projectId.equals(adapter.getProjectsData().get(i).getProjectid())) {

                                if (isSync) {
                                    adapter.getProjectsData().get(i).setFavorite(true);
                                    adapter.getProjectsData().get(i).setSyncStatus(LocalPhotosRepository.SYNCED_PHOTO);

                                } else {
                                    adapter.getProjectsData().get(i).setFavorite(false);
                                    adapter.getProjectsData().get(i).setSyncStatus(LocalPhotosRepository.UN_SYNC_PHOTO);
                                }
                                adapter.getProjectsData().get(i).setLastUpdatedProjectStatus(LocalPhotosRepository.SYNCED_PHOTO);
                                selectedProjectModel = adapter.getProjectsData().get(i);
                                position = i;
                                break;
                            }
                        }
//                        adapter.notifyDataSetChanged();
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {

                                if (adapter != null)
                                    adapter.notifyItemChanged(position);
                            }
                        });
                    } else {
                        if (adapter != null)
                            adapter.notifyDataSetChanged();
                    }
                }

                for (int i = 0; i < listOfLastUpdatedProjectIds.size(); i++) {
                    if (projectId != null && projectId.equals(listOfLastUpdatedProjectIds.get(i))) {
                        listOfLastUpdatedProjectIds.remove(i);
                        break;
                    }
                }
                if (listOfLastUpdatedProjectIds != null && listOfLastUpdatedProjectIds.size() > 0)
                    autoSyncWithUpdate(listOfLastUpdatedProjectIds.get(0));

            }
        }
    }


    private class updateProjectOBJImageDataAsyncTask extends AsyncTask<ProjectModel, Void, Void> {
        private ProjectsDatabase database;
        private ProjectDao projectDao;
        int position = 0;
        ProjectModel projectModelOBJ;

        updateProjectOBJImageDataAsyncTask(Context context) {
            database = ProjectsDatabase.getDatabase(context);
            projectDao = database.projectDao();
        }

        @Override
        protected Void doInBackground(final ProjectModel... params) {
            projectModelOBJ = params[0];
            projectDao.update(params[0]);
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            int pos = -1;
            if (adapter != null && adapter.getProjectsData().size() > cachedImagePos) {

                if (projectModelOBJ != null && projectModelOBJ.getProjectid() != null) {
                    for (int i = 0; i < adapter.getProjectsData().size(); i++) {
                        if (adapter.getProjectsData().get(i).getProjectid().equals(projectModelOBJ.getProjectid())) {
                            pos = i;
                            break;
                        }
                    }
                }

                Utils.showLogger2("notifyItemPosition>>"+cachedImagePos);
                if (adapter != null && pos != -1)
                    adapter.notifyItemChanged(pos);
            }
            else
                Utils.showLogger2("updateDataIgnored"+cachedImagePos);
//            if(cachedImagePos==adapter.getProjectsData().size())
//                if (adapter != null)
//                    adapter.notifyDataSetChanged();

        }
//
    }


    private class updateProjectOBJAsyncTask extends AsyncTask<ProjectModel, Void, Void> {
        private ProjectsDatabase database;
        private ProjectDao projectDao;
        int position = 0;
        ProjectModel projectModelOBJ;

        updateProjectOBJAsyncTask(Context context) {
            database = ProjectsDatabase.getDatabase(context);
            projectDao = database.projectDao();
        }

        @Override
        protected Void doInBackground(final ProjectModel... params) {
            projectModelOBJ = params[0];
            projectDao.update(params[0]);
            return null;
        }


    }


    private class SelectTabAsyncTask extends AsyncTask<Void, Void, Integer> {
        private ProjectsDatabase database;

        SelectTabAsyncTask(Context context) {
            database = ProjectsDatabase.getDatabase(context);
        }

        @Override
        protected Integer doInBackground(final Void... params) {
            Integer favCount = database.projectDao().getFavouriteProject().size();

            return favCount;
        }

        @Override
        protected void onPostExecute(Integer integer) {
            super.onPostExecute(integer);

            if (viewPager == null)
                return;
            if (integer > 0) {
                viewPager.setCurrentItem(0);
            } else {
                viewPager.setCurrentItem(1);
            }
        }
    }

    @Override
    public void onFailure(String projectId, boolean isSync) {
        Log.d("HomeActivityFailure", "Failure");
        boolean isAutoSyncProjectFound = false;
        Fragment fragment = projectsPagerAdapter.m2ndFragment;

        if (fragment != null && fragment instanceof AllFragment) {
            adapter = ((AllFragment) fragment).adapter;
        }

        if (fragment != null && adapter != null) {

            if (projectId != null && !projectId.equals("") && adapter != null) {
                for (int i = 0; i < adapter.getProjectsData().size(); i++) {
                    if (projectId.equals(adapter.getProjectsData().get(i).getProjectid())) {

                        if (listOfLastUpdatedProjectIdsFailureCase != null && listOfLastUpdatedProjectIdsFailureCase.size() > 0) {
                            for (int j = 0; j < listOfLastUpdatedProjectIdsFailureCase.size(); j++) {
                                if (projectId.equals(listOfLastUpdatedProjectIdsFailureCase.get(j))) {
                                    isAutoSyncProjectFound = true;
                                    listOfLastUpdatedProjectIdsFailureCase.remove(j);
                                    break;
                                }
                            }
                        }

//                        if(adapter.getProjectsData().get(i).isFavorite()){
//                            adapter.getProjectsData().get(i).setFavorite(true);
//                            adapter.getProjectsData().get(i).setSyncStatus(LocalPhotosRepository.UPLOADING_PHOTO);
//                        }else {

//                        }

                        if (isAutoSyncProjectFound) {
                            adapter.getProjectsData().get(i).setFavorite(true);
                            adapter.getProjectsData().get(i).setSyncStatus(LocalPhotosRepository.UPLOADING_PHOTO);
                        } else {
                            adapter.getProjectsData().get(i).setFavorite(false);
                            adapter.getProjectsData().get(i).setSyncStatus(LocalPhotosRepository.UN_SYNC_PHOTO);
                        }
                        adapter.getProjectsData().get(i).setLastUpdatedProjectStatus(LocalPhotosRepository.SYNCED_PHOTO);
                        selectedProjectModel = adapter.getProjectsData().get(i);
                        position = i;
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {

                                if (adapter != null)
                                    adapter.notifyItemChanged(position);
                            }
                        });
                        new updateProjectOBJAsyncTask(HomeActivity.this).execute(selectedProjectModel);
                        break;
                    }
                }
            } else {
                if (adapter != null)
                    adapter.notifyDataSetChanged();
            }
        }

//        new ProjectSyncManager(HomeActivity.this, projectId
//        ).performUnsyncAction(projectId, false);
    }

    @Override
    public void onListItemClick(ProjectModel projectModel, boolean isMarkFavourite, ProjectsRecyclerAdapter adapter) {


        selectedProjectModel = projectModel;
        // selectedProjectModel.setFavorite(isMarkFavourite);
//        projectsRecyclerAdapter = adapter;

        projectId = projectModel.getProjectid();

        boolean isAlreadyAdded = false;
        if (listOfLastUpdatedProjectIds != null && projectModel != null) {
            for (int i = 0; i < listOfLastUpdatedProjectIds.size(); i++) {
                if (listOfLastUpdatedProjectIds.get(i).equals(projectModel.getProjectid())) {
                    isAlreadyAdded = true;
                }
            }
            //if (!isAlreadyAdded)
             //   listOfLastUpdatedProjectIds.add(projectModel.getProjectid());
        }

        if (isMarkFavourite) {
            if (selectedProjectModel.isFavorite()) {
//                    selectedProjectModel.setFavorite(false);
                showCustomDialogUnSyncData(this, getResources().getString(R.string.custom_dialog_title), getResources().getString(R.string.unsync_project_msg), 2, 0);
            } else {
                if (ProjectDocuUtilities.isNetworkConnected(getApplication())) {
//                        selectedProjectModel.setFavorite(false);
//                        if (listOfLastUpdatedProjectIds != null && listOfLastUpdatedProjectIds.size() > 0) {
//                                autoSyncWithUpdate(listOfLastUpdatedProjectIds.get(0));

                    projectId = adapter.selectedProject.getProjectid();
                    listOfLastUpdatedProjectIds.add(projectId);
                    selectedProjectModel = adapter.selectedProject;
                    new PerformSyncAsyncTask(HomeActivity.this, projectId, true).execute();

//                        new ProjectSyncManager(HomeActivity.this, listOfLastUpdatedProjectIds.get(0)
//                                , syncTaskComplete, true);

//                        }
//                    new ProjectSyncManager(HomeActivity.this, projectId, syncTaskComplete, isFav);
                } else {
                    Toast.makeText(getApplicationContext(), getResources().getString(R.string.no_internet_message), Toast.LENGTH_LONG).show();
                }
//                showCustomDialogSyncProjectData(this, "Project Docu", getResources().getString(R.string.sync_project_msg), 2, 0);

            }
        } else {
//            showCustomDialog(this, "Project Docu", getResources().getString(R.string.sync_project_before_msg), 1, 0);
            if ((selectedProjectModel.getSyncStatus() != null && selectedProjectModel.getSyncStatus().equals(LocalPhotosRepository.UPLOADING_PHOTO)) || (selectedProjectModel.getLastUpdatedProjectStatus() != null && selectedProjectModel.getLastUpdatedProjectStatus().equals(LocalPhotosRepository.UPLOADING_PHOTO)))
                showCustomDialogSyncingState(this, getResources().getString(R.string.custom_dialog_title), getResources().getString(R.string.already_sync_project_msg), 1, 0);
            else
                showCustomDialog(this, getResources().getString(R.string.custom_dialog_title), getResources().getString(R.string.sync_project_before_msg), 2, 0);
        }


    }

    @Override
    public void onSyncActionClick(ProjectModel projectModel) {

    }

    private void cacheProjectImages(Context context, ProjectModel projectModel) {
        callGetPlanImageAPI(context, projectModel, projectModel.getDecoimage());
    }

    public void callGetPlanImageAPI(Context context, ProjectModel projectModel, String fileId) {
        Utils.showLogger2("callGetPlanImageAPI");
        SharedPrefsManager sharedPrefsManager = new SharedPrefsManager(context);
        RetroApiInterface retroApiInterface = RetrofitManager.getInstance().create(RetroApiInterface.class);
        String authToken = sharedPrefsManager.getStringValue(AppConstantsManager.AUTH_API_TOKEN, "");

        if (authToken.length() > 2) {
            authToken = authToken.substring(1, authToken.length() - 1);
        }

        Call<ResponseBody> call = retroApiInterface.getDecoImage(authToken, Utils.DEVICE_ID, fileId);
        Log.d("call url", call.request().url().toString());

        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                cachedImagePos++;
                if (response.isSuccessful()) {
                    if (response.body() != null) {
                        Log.d("List", "Success : " + response.body());
                        if (writeResponseBodyToDisk(response.body(), projectModel.getProjectid())) {
                            if (imagePath != null && !imagePath.equals("")) {
                                Utils.showLogger2("settingImgPath>>"+imagePath);
                                projectModel.setCacheImagePath(imagePath);
                                projectModel.setImageCache(true);
                                new updateProjectOBJImageDataAsyncTask(context).execute(projectModel);
                            }
//                            Bitmap bitmap  =   BitmapFactory.decodeFile(imagePath);
//                            imageView.setImageBitmap(bitmap);
                        }
                    } else {
                        Log.d("List", "Empty response");
                    }
                } else {
                    if (response.errorBody() != null) {
                        try {
                            Log.d("List", "Not Success : " + response.errorBody().string());
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    } else
                        Log.d("List", "Not Success : " + response.toString());
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                cachedImagePos++;
                Log.d("List", "failed : " + t.getMessage());
            }
        });
    }

    @Override
    public void cacheImagesInDB(List<ProjectModel> projectModelList) {
        listSize = projectModelList.size();
        Fragment fragment = projectsPagerAdapter.m2ndFragment;

        if (fragment != null && fragment instanceof AllFragment) {
            adapter = ((AllFragment) fragment).adapter;
        }

        for (int i = 0; i < projectModelList.size(); i++) {
            if (projectModelList.get(i).getDecoimage() != null && !projectModelList.get(i).isImageCache()) {
                cacheProjectImages(this, projectModelList.get(i));
            }
        }
    }


    public void showCustomDialog(final Activity act, String title, String msgToShow, Integer noOfButtons, Integer flag) {
        //customDialog = new Dialog(act, R.style.customDialogTheme);
        customDialog = new Dialog(act, R.style.MyDialogTheme);

        customDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        customDialog.setContentView(R.layout.custom_dialog_message_material);

        if (flag == 2) {
            customDialog.setCancelable(false);

        }
        customDialog.setCanceledOnTouchOutside(true);
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
        bt.setText(getResources().getString(R.string.synchronize));
        bt.setOnClickListener(new View.OnClickListener() {


            @Override
            public void onClick(View v) {
                if (ProjectDocuUtilities.isNetworkConnected(getApplication())) {
//                    selectedProjectModel.setFavorite(true);
                    //if (listOfLastUpdatedProjectIds != null && listOfLastUpdatedProjectIds.size() > 0) {
                        //for (int i = 0; i < listOfLastUpdatedProjectIds.size(); i++) {
                        Fragment fragment = projectsPagerAdapter.m2ndFragment;

                        if (fragment != null && fragment instanceof AllFragment) {
                            adapter = ((AllFragment) fragment).adapter;
                        }

                        if (fragment != null && adapter != null ) {

                            projectId = adapter.selectedProject.getProjectid();
                            selectedProjectModel = adapter.selectedProject;
                            listOfLastUpdatedProjectIds.add(projectId);
                            adapter.getProjectsData().get(adapter.lastSelectedPosition).setLastUpdatedProjectStatus(LocalPhotosRepository.UPLOADING_PHOTO);

                            position = adapter.lastSelectedPosition;
                            /*projectId = listOfLastUpdatedProjectIds.get(0);
                            if (listOfLastUpdatedProjectIds.get(0) != null && !listOfLastUpdatedProjectIds.get(0).equals("") && adapter != null) {
                                for (int i = 0; i < adapter.getProjectsData().size(); i++) {
                                    if (listOfLastUpdatedProjectIds.get(0).equals(adapter.getProjectsData().get(i).getProjectid())) {
                                        selectedProjectModel = adapter.getProjectsData().get(i);
                                        adapter.getProjectsData().get(i).setLastUpdatedProjectStatus(LocalPhotosRepository.UPLOADING_PHOTO);
                                        position = i;
                                        break;
                                    }
                                }
                            }*/
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {

                                    if (adapter != null)
                                        adapter.notifyItemChanged(position);
                                }
                            });
                        } else {
                            if (adapter != null)
                                adapter.notifyDataSetChanged();
                        }

//                        new ProjectSyncManager(HomeActivity.this, listOfLastUpdatedProjectIds.get(0)
//                                , syncTaskComplete, true);
                        new PerformSyncAsyncTask(HomeActivity.this, projectId, true).execute();

                        // }

                } else {
                    Toast.makeText(getApplicationContext(), getResources().getString(R.string.no_internet_message), Toast.LENGTH_LONG).show();
                }
                customDialog.dismiss();

            }
        });
        if (noOfButtons == 2) {
            Button bt1 = (Button) customDialog.findViewById(R.id.customDialog_cancel);
            bt1.setVisibility(View.VISIBLE);
            bt1.setText(getResources().getString(R.string.cancel));
            bt1.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {

                    customDialog.dismiss();
                }
            });

        }
        customDialog.show();
    }

    public void showCustomDialogSyncingState(final Activity act, String title, String msgToShow, Integer noOfButtons, Integer flag) {
        //customDialog = new Dialog(act, R.style.customDialogTheme);
        Utils.showLogger("showCustomDialogSyncingState");
        customDialog = new Dialog(act, R.style.MyDialogTheme);

        customDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        customDialog.setContentView(R.layout.custom_dialog_message_material);

        if (flag == 2) {
            customDialog.setCancelable(false);

        }
        customDialog.setCanceledOnTouchOutside(true);
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
        bt.setText(getResources().getString(R.string.ok));
        bt.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                customDialog.dismiss();

            }
        });
        if (noOfButtons == 2) {
            Button bt1 = (Button) customDialog.findViewById(R.id.customDialog_cancel);
            bt1.setVisibility(View.VISIBLE);
            bt1.setText(getResources().getString(R.string.cancel));
            bt1.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {

                    customDialog.dismiss();
                }
            });

        }
        customDialog.show();
    }

  /*  public void showCustomDialogSyncProjectData(final Activity act, String title, String msgToShow, Integer noOfButtons, Integer flag) {
        //customDialog = new Dialog(act, R.style.customDialogTheme);
        customDialog = new Dialog(act, R.style.MyDialogTheme);
        customDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        customDialog.setContentView(R.layout.custom_dialog_message_material);
        if (flag == 2) {
            customDialog.setCancelable(false);
        }
        customDialog.setCanceledOnTouchOutside(true);
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
        bt.setText(getResources().getString(R.string.sync_label));
        bt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ProjectDocuUtilities.isNetworkConnected(getApplication())) {
                    selectedProjectModel.setFavorite(true);
                    if (listOfLastUpdatedProjectIds != null && listOfLastUpdatedProjectIds.size() > 0) {
                        for (int i = 0; i < listOfLastUpdatedProjectIds.size(); i++) {
                            new ProjectSyncManager(HomeActivity.this, listOfLastUpdatedProjectIds.get(i)
                                    , syncTaskComplete, true);
                        }
                    }
//                    new ProjectSyncManager(HomeActivity.this, projectId, syncTaskComplete, isFav);
                } else {
                    Toast.makeText(getApplicationContext(), getResources().getString(R.string.no_internet_message), Toast.LENGTH_LONG).show();
                }
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
*/

    public void showCustomDialogUnSyncData(final Activity act, String title, String msgToShow, Integer noOfButtons, Integer flag) {
        //customDialog = new Dialog(act, R.style.customDialogTheme);
        customDialog = new Dialog(act, R.style.MyDialogTheme);
        customDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        customDialog.setContentView(R.layout.custom_dialog_message_material);
        if (flag == 2) {
            customDialog.setCancelable(false);
        }
        customDialog.setCanceledOnTouchOutside(true);
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
              //  if (listOfLastUpdatedProjectIds != null && listOfLastUpdatedProjectIds.size() > 0) {


                if(adapter!=null&&adapter.selectedProject!=null) {
                    selectedProjectModel = adapter.selectedProject;
                    listOfLastUpdatedProjectIds.add(adapter.selectedProject.getProjectid());
                    new PerformSyncAsyncTask(HomeActivity.this, adapter.selectedProject.getProjectid(), false).execute();
                }

/*                for (int i = 0; i < listOfLastUpdatedProjectIds.size(); i++) {
//                        new ProjectSyncManager(HomeActivity.this, listOfLastUpdatedProjectIds.get(i)
//                                , syncTaskComplete, false);
                        new PerformSyncAsyncTask(HomeActivity.this, projectId, false).execute();

                    }*/
               // }
//                new ProjectSyncManager(HomeActivity.this, projectId, syncTaskComplete, false);

                customDialog.dismiss();
            }
        });
        if (noOfButtons == 2) {
            Button bt1 = (Button) customDialog.findViewById(R.id.customDialog_cancel);
            bt1.setVisibility(View.VISIBLE);
            bt1.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
//                    if (selectedProjectModel.isFavorite()) {
//                        selectedProjectModel.setFavorite(false);
////                        showCustomDialogUnSyncData(this, "Project Docu", "Are you sure you want to unSync project and delete all data against it.", 2, 0);
//                    } else {
//                        selectedProjectModel.setFavorite(true);
////                        new ProjectSyncManager(this, projectModel.getProjectid(), syncTaskComplete, isMarkFavourite);
//                    }
                    customDialog.dismiss();

                }
            });
        }
        customDialog.show();
    }


    public void showCustomDialogLogoutUser(final Activity act, String title, String msgToShow, Integer noOfButtons, Integer flag) {
        //customDialog = new Dialog(act, R.style.customDialogTheme);
        customDialog = new Dialog(act, R.style.MyDialogTheme);
        customDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        customDialog.setContentView(R.layout.custom_dialog_message_material);
        if (flag == 2) {
            customDialog.setCancelable(false);
        }
        customDialog.setCanceledOnTouchOutside(true);
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
                new logoutUserAsyncTask(HomeActivity.this).execute();
                startActivity(new Intent(HomeActivity.this, LoginActivity.class));
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



    private class logoutUserAsyncTask extends AsyncTask<Void, Void, Void> {
        private ProjectsDatabase database;

        logoutUserAsyncTask(Context context) {
            database = ProjectsDatabase.getDatabase(context);
        }

        @Override
        protected Void doInBackground(final Void... params) {
            ProjectsDatabase.getDatabase(HomeActivity.this).clearAllTables();
            SharedPrefsManager sharedPrefsManager = new SharedPrefsManager(HomeActivity.this);
            sharedPrefsManager.sharedPreferences.edit().clear().commit();

            return null;
        }

    }

    public void  autoSyncWithUpdate(String projectId) {

        isAutoServerSyncStart = false;

        if (listOfLastUpdatedProjectIds != null && listOfLastUpdatedProjectIds.size() > 0) {

            Fragment fragment = projectsPagerAdapter.m2ndFragment;

            if (fragment != null && fragment instanceof AllFragment) {
                adapter = ((AllFragment) fragment).adapter;
            }

            if (fragment != null && adapter != null && listOfLastUpdatedProjectIds != null && listOfLastUpdatedProjectIds.size() > 0) {

                if (projectId != null && !projectId.equals("") && adapter != null) {
                    for (int i = 0; i < adapter.getProjectsData().size(); i++) {
                        if (projectId.equals(adapter.getProjectsData().get(i).getProjectid())) {
                            adapter.getProjectsData().get(i).setFavorite(false);
                            adapter.getProjectsData().get(i).setLastUpdatedProjectStatus(LocalPhotosRepository.UPLOADING_PHOTO);
                            selectedProjectModel = adapter.getProjectsData().get(i);

                            position = i;
                            break;
                        }
                    }
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {

                            if (adapter != null)
                                adapter.notifyItemChanged(position);
                        }
                    });
                } else {
                    if (adapter != null)
                        adapter.notifyDataSetChanged();
                }
            }



            new PerformSyncAsyncTask(HomeActivity.this, projectId, true).execute();
//            new ProjectSyncManager(HomeActivity.this, projectId, syncTaskComplete, isFav);
        }

    }


    private class PerformSyncAsyncTask extends AsyncTask<Void, Void, Void> {
        private ProjectsDatabase database;
        String projectID;
        boolean isSync;
        ProjectModel projectModel = null;

        PerformSyncAsyncTask(Context context, String projectId, boolean isSYnc) {
            projectID = projectId;
            isSync = isSYnc;
            database = ProjectsDatabase.getDatabase(context);

        }

        @Override
        protected Void doInBackground(final Void... params) {
            projectModel = database.projectDao().getProjectOBJ(projectID);


            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            if (projectModel != null) {
                if (isSync) {
                    if (!projectModel.isFavorite()) {
                        if (ProjectDocuUtilities.isNetworkConnected(getApplication())) {
                            new ProjectSyncManager(HomeActivity.this, projectID
                                    , syncTaskComplete, isSync);
                        } else {
                            Toast.makeText(getApplicationContext(), getResources().getString(R.string.no_internet_message), Toast.LENGTH_LONG).show();
                        }
                    }
                } else {
                    if (ProjectDocuUtilities.isNetworkConnected(getApplication())) {
                        new ProjectSyncManager(HomeActivity.this, projectID
                                , syncTaskComplete, isSync);
                    } else {
                        Toast.makeText(getApplicationContext(), getResources().getString(R.string.no_internet_message), Toast.LENGTH_LONG).show();
                    }

                }
            }
        }
    }


    private void addEvent() {
        updateProjectData = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                ProjectsRecyclerAdapter adapter = null;
                int position = 0;
                try {

                    if (intent.getExtras().get(KEY_LAST_UPDATED_PROJECT_IDS_LIST) != null && !intent.getExtras().get(KEY_LAST_UPDATED_PROJECT_IDS_LIST).equals("")) {
                        String str[] = intent.getExtras().get(KEY_LAST_UPDATED_PROJECT_IDS_LIST).toString().split("\\s*,\\s*");
                        listOfLastUpdatedProjectIds.addAll(Arrays.asList(str));
                        listOfLastUpdatedProjectIdsFailureCase.addAll(Arrays.asList(str));
                        isAutoServerSyncStart = true;
                    }

                    autoSyncProjectsDataFromServer();


                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        };


    }

    private class DeleteALLProjectsAsyncTask extends AsyncTask<Void, Void, Void> {
        private ProjectDao mAsyncTaskDao;
        List<ProjectModel> projectModelList = null;

        DeleteALLProjectsAsyncTask() {

        }

        @Override
        protected Void doInBackground(final Void... params) {
            mAsyncTaskDao = ProjectsDatabase.getDatabase(HomeActivity.this).projectDao();
            mAsyncTaskDao.deleteAll();

            return null;
        }

    }


    public void autoSyncProjectsDataFromServer() {

        if (listOfLastUpdatedProjectIds != null && listOfLastUpdatedProjectIds.size() > 0) {
            Fragment fragment = projectsPagerAdapter.m2ndFragment;

            if (fragment != null && fragment instanceof AllFragment) {
                adapter = ((AllFragment) fragment).adapter;
            }
            for (int j = 0; j < listOfLastUpdatedProjectIds.size(); j++) {

                projectId = listOfLastUpdatedProjectIds.get(j);
                if (fragment != null && adapter != null && listOfLastUpdatedProjectIds != null && listOfLastUpdatedProjectIds.size() > 0) {

                    if (projectId != null && !projectId.equals("") && adapter != null) {
                        for (int i = 0; i < adapter.getProjectsData().size(); i++) {
                            if (projectId.equals(adapter.getProjectsData().get(i).getProjectid())) {

//
                                adapter.getProjectsData().get(i).setFavorite(false);
                                adapter.getProjectsData().get(i).setLastUpdatedProjectStatus(LocalPhotosRepository.SYNCED_PHOTO);

//                                if(adapter.getProjectsData().get(i).isFavorite()){
//                                    adapter.getProjectsData().get(i).setFavorite(true);
//                                    adapter.getProjectsData().get(i).setSyncStatus(LocalPhotosRepository.UPLOADING_PHOTO);
//                                }else {
//                                    adapter.getProjectsData().get(i).setFavorite(false);
//                                    adapter.getProjectsData().get(i).setSyncStatus(LocalPhotosRepository.UN_SYNC_PHOTO);
//                                }

                                selectedProjectModel = adapter.getProjectsData().get(i);
                                position = i;
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {

                                        if (adapter != null)
                                            adapter.notifyItemChanged(position);
                                    }
                                });
                                new updateProjectOBJAsyncTask(HomeActivity.this).execute(selectedProjectModel);
                            }
                        }
                    } else {
                        if (adapter != null)
                            adapter.notifyDataSetChanged();
                    }
                }

                new ProjectSyncManager(HomeActivity.this, listOfLastUpdatedProjectIds.get(j)
                ).performUnsyncAction(projectId, false);
            }
            autoSyncWithUpdate(listOfLastUpdatedProjectIds.get(0));

        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == AppConstantsManager.REQUEST_CODE_NEW_ACTIVITY) {
            if (resultCode == Activity.RESULT_OK) {
                if (viewPager != null) {
                    viewPager.setCurrentItem(1);
                }
            }
        }
    }

    private class UnsyncedPhotosAsyncTask extends AsyncTask<Void, Void, List<PhotoModel>> {
        private ProjectsDatabase database;


        UnsyncedPhotosAsyncTask(Context context) {
            database = ProjectsDatabase.getDatabase(context);

        }

        @Override
        protected List<PhotoModel> doInBackground(final Void... params) {
            List<PhotoModel> unsyncedPhotosList = database.photoDao().getAllUnSyncedPhotoCount();
            for (PhotoModel unsyncedPhoto : unsyncedPhotosList) {
                database.photoDao().updateStatus(unsyncedPhoto.getPdphotolocalId(), LocalPhotosRepository.UN_SYNC_PHOTO);
            }

            List<PhotoModel> photosWithFailedCountGreaterThanSix = database.photoDao().getPhotosWithFailedCountGreaterThanSix();
            if (photosWithFailedCountGreaterThanSix != null) {
                if (photosWithFailedCountGreaterThanSix.size() > 0) {
                    for (PhotoModel tempPhotoModel : photosWithFailedCountGreaterThanSix) {
                        tempPhotoModel.setFailedCount(0);
                        database.photoDao().update(tempPhotoModel);
                    }
                }
            }
            return database.photoDao().getAllUnSyncedPhotoCount();
        }

        @Override
        protected void onPostExecute(List<PhotoModel> unsyncedPhotosList) {
            super.onPostExecute(unsyncedPhotosList);

            if (unsyncedPhotosList != null) {
                if (unsyncedPhotosList.size() > 0) {
                    String message = unsyncedPhotosList.size() + " " + getResources().getString(R.string.unscyned_photo_message);
                    new Handler().postDelayed(() -> {
                        showCustomDialogForUnsyncedPhotoCount(HomeActivity.this, getResources().getString(R.string.custom_dialog_title), message, 2, 0, unsyncedPhotosList,getString(R.string.yes),getString(R.string.no));
                    }, 100);

                }

            }
        }
    }

    public void showCustomDialogForUnsyncedPhotoCount(final Activity act, String title, String msgToShow, Integer noOfButtons, Integer flag, List<PhotoModel> photosList) {
        //customDialog = new Dialog(act, R.style.customDialogTheme);
        Dialog customDialog = new Dialog(act, R.style.MyDialogTheme);
        customDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        customDialog.setContentView(R.layout.custom_dialog_message_material);
        List<String> syncedProjectIds = new ArrayList<>();
        if (flag == 2) {
            customDialog.setCancelable(false);
        }
        customDialog.setCanceledOnTouchOutside(true);
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
                for (PhotoModel photoModel : photosList) {
                    if (syncedProjectIds != null) {
                        if (!syncedProjectIds.contains(photoModel.getProjectId())) {
                            startBackgroundTask(HomeActivity.this, mWorkerResultReceiver, photoModel.getProjectId(), false);
                        }
                        syncedProjectIds.add(photoModel.getProjectId());
                    }
                }
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


    public void showCustomDialogForUnsyncedPhotoCount(final Activity act, String title, String msgToShow, Integer noOfButtons, Integer flag, List<PhotoModel> photosList,String yes, String no) {
        //customDialog = new Dialog(act, R.style.customDialogTheme);
        Dialog customDialog = new Dialog(act, R.style.MyDialogTheme);
        customDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        customDialog.setContentView(R.layout.custom_dialog_message_material);
        List<String> syncedProjectIds = new ArrayList<>();
        if (flag == 2) {
            customDialog.setCancelable(false);
        }
        customDialog.setCanceledOnTouchOutside(true);
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
        bt.setText(yes);
        bt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                for (PhotoModel photoModel : photosList) {
                    if (syncedProjectIds != null) {
                        if (!syncedProjectIds.contains(photoModel.getProjectId())) {
                            startBackgroundTask(HomeActivity.this, mWorkerResultReceiver, photoModel.getProjectId(), false);
                        }
                        syncedProjectIds.add(photoModel.getProjectId());
                    }
                }
                customDialog.dismiss();
            }
        });
        if (noOfButtons == 2) {
            Button bt1 = (Button) customDialog.findViewById(R.id.customDialog_cancel);
            bt1.setText(no);
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


    public void startBackgroundTask(Context context, WorkerResultReceiver mWorkerResultReceiver, String projectID, boolean isAutoSyncPhotos) {
        SyncLocalPhotosService.enqueueWork(context, mWorkerResultReceiver, projectID, isAutoSyncPhotos);
    }

    @Override
    public void onReceiveResult(int resultCode, Bundle resultData) {
        Log.d(TAG, "onReceiveResult: ");
    }

}
