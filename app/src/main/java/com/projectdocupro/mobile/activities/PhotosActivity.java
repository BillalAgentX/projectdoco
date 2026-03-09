package com.projectdocupro.mobile.activities;

import static com.projectdocupro.mobile.activities.SavePictureActivity.BR_ACTION_UPDATE_PHOTO_AND_PLAN_LOC;
import static com.projectdocupro.mobile.activities.SavePictureActivity.PLAN_ATTACH_TO_PHOTO_KEY;
import static com.projectdocupro.mobile.activities.SavePictureActivity.PLAN_ID_KEY;

import android.Manifest;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Point;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.Settings;
import android.util.Log;
import android.view.Display;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.sqlite.db.SimpleSQLiteQuery;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.tabs.TabLayout;
import com.projectdocupro.mobile.ProjectNavigator;
import com.projectdocupro.mobile.ProjectsDatabase;
import com.projectdocupro.mobile.R;
import com.projectdocupro.mobile.adapters.ExpandablePhotosFIlterListAdapter;
import com.projectdocupro.mobile.adapters.PhotosPagerAdapter;
import com.projectdocupro.mobile.dao.OnlinePhotoDao;
import com.projectdocupro.mobile.dao.PhotoDao;
import com.projectdocupro.mobile.dao.ProjectUsersDao;
import com.projectdocupro.mobile.fragments.LocalPhotosFragment;
import com.projectdocupro.mobile.fragments.OnlinePhotosFragment;
import com.projectdocupro.mobile.fragments.add_direction.ProjectDocuUtilities;
import com.projectdocupro.mobile.managers.AppConstantsManager;
import com.projectdocupro.mobile.managers.SharedPrefsManager;
import com.projectdocupro.mobile.models.OnlinePhotoModel;
import com.projectdocupro.mobile.models.PhotoModel;
import com.projectdocupro.mobile.models.PlansModel;
import com.projectdocupro.mobile.models.mangel_filters.ChildRowModel;
import com.projectdocupro.mobile.models.mangel_filters.GroupheadingModel;
import com.projectdocupro.mobile.models.mangel_filters.ProjectUserModel;
import com.projectdocupro.mobile.repos.LocalPhotosRepository;
import com.projectdocupro.mobile.repos.ONlinePhotoRepository;
import com.projectdocupro.mobile.utility.Utils;
import com.projectdocupro.mobile.viewModels.LocalPhotosViewModel;
import com.projectdocupro.mobile.viewModels.OnlinePhotosViewModel;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;



public class PhotosActivity extends AppCompatActivity implements OnlinePhotosFragment.OnFragmentInteractionListener,
        LocalPhotosFragment.OnFragmentInteractionListener {


    PhotosPagerAdapter photosPagerAdapter;


    private ViewPager photosViewPager;

    private TabLayout pagerTabStrip;

    private RelativeLayout toolbar;


    private ExpandableListView simple_expandable_listview;

    private LinearLayout ll_hidenView;

    private LinearLayout ll_expand_view;

    private LinearLayout ll_bottom_tabs;

    private ImageView iv_folder_icon;

    private ImageView iv_info;

    public ImageView iv_sync_all;

    private TextView tv_reset_filter;

    private TextView tv_apply_online_filter;

    public PhotoDao photoDao;
    public OnlinePhotoDao onlinePhotoDao;


    public List<GroupheadingModel> groupheadingModelList = new ArrayList<>();
    ExpandablePhotosFIlterListAdapter expandableListAdapter;
    public MutableLiveData<Boolean> isShowFilterIcon = new MutableLiveData<>();
    public MutableLiveData<Boolean> isShowIconReport = new MutableLiveData<>();
    public MutableLiveData<Boolean> isLoadingFilterData = new MutableLiveData<>();
    public String projectID;
    private Long longMinDate;
    private List<PhotoModel> photoModelListMinDateLocalPhotoList;
    private List<OnlinePhotoModel> photoModelListMinDateOnlinePhotoList;
    private boolean isOnlinePhotoTabSelected;
    public boolean isOnlinePhotoFilterApplied;
    private SharedPrefsManager sharedPrefsManager;
    private ArrayList<ChildRowModel> usersList;
    private BroadcastReceiver updateFlawFlag;
    private static final int STORAGE_RQUEST_CODE = 23;
    private boolean isFilterViewVisible = false;
    public OnlinePhotosViewModel onlinePhotosViewModel;
    public String defaultStartDate = "";
    public String defaultEndDate = "";


    private ImageView ivAdd;

    private ImageView ivFilter;

    private ImageView ivMenu;

    private View llCart;

    private TextView tv_count;
    private TextView tv_count_2;

    private boolean isFilterSetForFirstTime = true;
    ChildRowModel childRowModelDescendingDate = new ChildRowModel();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photos);
        bindView();
        onlinePhotosViewModel = ViewModelProviders.of(this).get(OnlinePhotosViewModel.class);
        ProjectNavigator.isPhotoActivityForground = true;
        Log.d("projectId", getIntent().getStringExtra("projectId"));
        projectID = getIntent().getStringExtra("projectId");
        onlinePhotosViewModel.callGetPhotosAPIForMaxMinDate(this, projectID);
        photosPagerAdapter = new PhotosPagerAdapter(this, getSupportFragmentManager(), getIntent().getStringExtra("projectId"));
        photosViewPager.setAdapter(photosPagerAdapter);
        photosViewPager.setOffscreenPageLimit(1);

        pagerTabStrip.setupWithViewPager(photosViewPager);
        photoDao = ProjectsDatabase.getDatabase(this).photoDao();
        onlinePhotoDao = ProjectsDatabase.getDatabase(this).onlinePhotoDao();
        sharedPrefsManager = new SharedPrefsManager(this);

 /*       if (sharedPrefsManager.getBooleanValue(AppConstantsManager.ALLOW_BACKGROUND_SYNC, false)
                && (sharedPrefsManager.getBooleanValue(AppConstantsManager.SYNC_PHOTOS_MOBILE_DATA, false)
                || sharedPrefsManager.getBooleanValue(AppConstantsManager.SYNC_PHOTOS_WLAN, false))) {

            new UpdatePhotoStatusAsyncTask().execute();
        }*/


        photosViewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                onTabSwitchUpdateFilterList(position);
//                Toast.makeText(PhotosActivity.this,"toast",Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
        addEvent();
        onDeviceRotate();
//        loadFacetsData();



    }

    public void onDeviceRotate() {

        int currentOrientation = getResources().getConfiguration().orientation;

        if (currentOrientation == Configuration.ORIENTATION_LANDSCAPE) {
            Log.v("TAG", "Landscape !!!");
            Display display = getWindowManager().getDefaultDisplay();
            Point size = new Point();
            try {
                display.getRealSize(size);
            } catch (NoSuchMethodError err) {
                display.getSize(size);
            }
            int width = size.x;
            int height = size.y;

//            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams((int)(width/3),
//                    LinearLayout.LayoutParams.MATCH_PARENT);
            RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams((int) (width / 1.5),
                    LinearLayout.LayoutParams.MATCH_PARENT);
            params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
            params.addRule(RelativeLayout.BELOW, toolbar.getId());
//            ll_expand_view.setLayoutParams(params);
//           Toast.makeText(this,"Land",Toast.LENGTH_SHORT).show();
        } else {
//            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
//                    LinearLayout.LayoutParams.MATCH_PARENT);
            RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.MATCH_PARENT);
            params.setMarginStart((int) getResources().getDimension(R.dimen.filter_width));
            params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
            params.addRule(RelativeLayout.BELOW, toolbar.getId());

//            Toast.makeText(this,"Por",Toast.LENGTH_SHORT).show();

//            ll_expand_view.setLayoutParams(params);


        }

    }

    private void addEvent() {


        tv_count.setVisibility(View.GONE);
        tv_count_2.setVisibility(View.INVISIBLE);


        ivMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });


        ivAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (photosViewPager.getCurrentItem() == 0)
                    startActivity(new Intent(PhotosActivity.this, ShareDeleteLocalPhotosActivity.class).putExtra("projectId", projectID));
                else
                    startActivity(new Intent(PhotosActivity.this, ReportPhotosActivity.class).putExtra("projectId", projectID));

            }
        });
        ivFilter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ll_expand_view.getVisibility() != View.VISIBLE) {

                    showFilterView();
                } else {
                    hideFilterView();
                }

            }
        });


        updateFlawFlag = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {

                String action = intent.getAction();
                String planIdd = "";
                boolean isFinishPlanScreen = false;
                boolean isPlanAttachToPhoto = false;


                if (action.equals(BR_ACTION_UPDATE_PHOTO_AND_PLAN_LOC)) {
                    if (intent.getExtras() != null) {
                        boolean isPlanAttachToImage = intent.getExtras().getBoolean(PLAN_ATTACH_TO_PHOTO_KEY, false);
                        String planId = intent.getExtras().getString(PLAN_ID_KEY);
                        if (isPlanAttachToImage) {


                        }
//                        else {
//                            savePictureViewModel.getPhotoModel().setPlanAdded(false);
//                            savePictureViewModel.getPhotoModel().setPlan_id("");
//                            savePictureViewModel.getPhotoModel().setPhotoSynced(false);
//                            savePictureViewModel.getPhotoModel().setPhotoUploadStatus(LocalPhotosRepository.UN_SYNC_PHOTO);
//                            isPhotoUpdate = true;
//                            new updatePhotoAsyncTask(savePictureViewModel.getPhotoModel().getPdphotolocalId(), savePictureViewModel.getProjectId(), savePictureViewModel.getPhotoModel()).execute();
//                            //    Toast.makeText(getApplicationContext(), "Remove Attached: " , Toast.LENGTH_SHORT).show();
//                        }

                    }


                }
            }
        };


        photosViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                if (position == 0) {
                    ll_bottom_tabs.setVisibility(View.VISIBLE);
                    tv_apply_online_filter.setVisibility(View.GONE);
                    isOnlinePhotoTabSelected = false;
                } else {
                    isOnlinePhotoTabSelected = true;
                    ll_bottom_tabs.setVisibility(View.GONE);
                    tv_apply_online_filter.setVisibility(View.VISIBLE);
//                    Fragment fragment = photosPagerAdapter.getM2ndFragment();
//                    if (fragment != null && isOnlinePhotoFilterApplied) {
//                        isOnlinePhotoFilterApplied = false;
//                        ((OnlinePhotosFragment) fragment).onlinePhotosViewModel.callGetPhotosAPI(PhotosActivity.this, projectID, ((OnlinePhotosFragment) fragment).jsonObject, ((OnlinePhotosFragment) fragment).jsonElements);
//                    }

                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        tv_apply_online_filter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hideKeyboard(PhotosActivity.this);
                hideFilterView();
                applyOnlinePhotoFilter();
            }
        });

        isShowFilterIcon.observe(this, new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean aBoolean) {

//                if (toolbar != null) {
//                    if (aBoolean) {
//
//                        toolbar.getMenu().findItem(R.id.filter).setVisible(true);
//                        // loadFacetsData();
//                    } else
//                        toolbar.getMenu().findItem(R.id.filter).setVisible(false);
//
//                }
            }
        });

//        isLoadingFilterData.observe(this, new Observer<Boolean>() {
//            @Override
//            public void onChanged(Boolean aBoolean) {
//
//                if (ivFilter != null && isShowFilterIcon.getValue() != null) {
//                    if (aBoolean) {
//
//                        if (isShowFilterIcon.getValue())
//
//                            ivFilter.setVisibility(View.VISIBLE);
//                        else
//                            ivFilter.setVisibility(View.INVISIBLE);
//                        // loadFacetsData();
//                    } else {
//                        ivFilter.setVisibility(View.INVISIBLE);
//                    }
//
//                }
//            }
//        });

        isShowIconReport.observe(this, new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean aBoolean) {

                if (aBoolean) {
                    ivAdd.setVisibility(View.VISIBLE);
                    //  loadFacetsData();
                } /*else
                    ivAdd.setVisibility(View.GONE);*/
            }
        });


        ll_hidenView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hideFilterView();

            }
        });

        tv_reset_filter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clearFilter();
            }
        });
        iv_folder_icon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    startActivityForResult(new Intent(PhotosActivity.this, SavePictureActivity.class).putExtra("projectId", getIntent().getStringExtra("projectId")).putExtra("isPhotoFromGallary", true), 7890);

                    //startActivityForResult(new Intent(PhotosActivity.this, SavePictureActivity.class).putExtra("projectId", getIntent().getStringExtra("projectId")).putExtra("isPhotoFromGallary", true), 7890);
                    /*if (Environment.isExternalStorageManager())
                        startActivityForResult(new Intent(PhotosActivity.this, SavePictureActivity.class).putExtra("projectId", getIntent().getStringExtra("projectId")).putExtra("isPhotoFromGallary", true), 7890);
                    else {
                        try {
                            Intent intent = new Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
                            intent.addCategory("android.intent.category.DEFAULT");
                            intent.setData(Uri.parse(String.format("package:%s", getApplicationContext().getPackageName())));
                            startActivityForResult(intent, 2296);
                        } catch (Exception e) {
                            Intent intent = new Intent();
                            intent.setAction(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION);
                            startActivityForResult(intent, 2296);
                        }
                    }*/
                } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                        requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE}, STORAGE_RQUEST_CODE);
                        return;
                    }
                    startActivityForResult(new Intent(PhotosActivity.this, SavePictureActivity.class).putExtra("projectId", getIntent().getStringExtra("projectId")).putExtra("isPhotoFromGallary", true), 7890);
                }
                else
                    startActivityForResult(new Intent(PhotosActivity.this, SavePictureActivity.class).putExtra("projectId", getIntent().getStringExtra("projectId")).putExtra("isPhotoFromGallary", true), 7890);


            }
        });

        iv_info.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(PhotosActivity.this, SyncStatusActivity.class).putExtra("projectId", getIntent().getStringExtra("projectId")).putExtra("isPhotoFromGallary", true));

            }
        });

        iv_sync_all.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (ProjectDocuUtilities.isNetworkConnected(PhotosActivity.this) || ProjectNavigator.wlanIsConnected(PhotosActivity.this)) {
                    Utils.showLogger("syncAllPhotos>" + "PhotosActivity 473");
                    syncAllPhotos(false);//syncing from button
                }

            }
        });
//        simple_expandable_listview.setOnGroupClickListener(new ExpandableListView.OnGroupClickListener() {
//            @Override
//            public boolean onGroupClick(ExpandableListView parent, View v, int groupPosition, long id) {
//                if (groupheadingModelList.get(groupPosition).getListChildData().get(groupheadingModelList.get(groupPosition).getType()).equals(getResources().getString(R.string.heading_photo_number))
//                        || groupheadingModelList.get(groupPosition).getListChildData().get(groupheadingModelList.get(groupPosition).getType()).equals(getResources().getString(R.string.heading_photo_keyword))) {
//                    return true;
//                }
//                return false;
//
//            }
//        });
        simple_expandable_listview.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
            @Override
            public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {

                if (groupheadingModelList.get(groupPosition).isMultiSelect()) {
                    ChildRowModel childRowModel = groupheadingModelList.get(groupPosition).getListChildData().get(groupheadingModelList.get(groupPosition).getType()).get(childPosition);
                    if (groupheadingModelList.get(groupPosition).getListChildDataSelected() != null) {
                        if (groupheadingModelList.get(groupPosition).getListChildDataSelected().contains(childRowModel.getId())) {
                            groupheadingModelList.get(groupPosition).getListChildDataSelected().remove(childRowModel.getId());

                        } else {
                            groupheadingModelList.get(groupPosition).getListChildDataSelected().add(childRowModel.getId());
                        }
                    }
                } else {
                    ChildRowModel childRowModel = groupheadingModelList.get(groupPosition).getListChildData().get(groupheadingModelList.get(groupPosition).getType()).get(childPosition);
                    if (groupheadingModelList.get(groupPosition).getListChildDataSelected() != null) {

                        if (groupheadingModelList.get(groupPosition).getListChildDataSelected().size() > 0) {
                            if (groupheadingModelList.get(groupPosition).getListChildDataSelected().get(0).equals(childRowModel.getId())) {
                                groupheadingModelList.get(groupPosition).getListChildDataSelected().remove(childRowModel.getId());
                            } else {
                                groupheadingModelList.get(groupPosition).getListChildDataSelected().set(0, childRowModel.getId());

                            }
                        } else {
                            groupheadingModelList.get(groupPosition).getListChildDataSelected().add(childRowModel.getId());
                        }
                    }
                }
                isFilterSetForFirstTime = false;
                applyFilter();
                if (expandableListAdapter != null)
                    expandableListAdapter.notifyDataSetChanged();

                return false;

            }
        });
//        loadFacetsData();

        //region minMaxDateFromApiObserver
        onlinePhotosViewModel.getMinMaxDateMap().observe(this, new Observer<Map<String, String>>() {
            @Override
            public void onChanged(Map<String, String> dateStringMap) {
                if (dateStringMap != null) {
                    defaultStartDate = dateStringMap.get("min_date");
                    defaultEndDate = dateStringMap.get("max_date");
                    Log.d("DATE", "minMaxDateFromApi: min_date=" + defaultStartDate);
                    Log.d("DATE", "minMaxDateFromApi: max_date=" + defaultEndDate);
                    loadFacetsData();
                }
            }
        });
        //endregion
    }

    public static void hideKeyboard(Activity activity) {
        InputMethodManager imm = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
        //Find the currently focused view, so we can grab the correct window token from it.
        View view = activity.getCurrentFocus();
        //If no view currently has focus, create a new one, just so we can grab a window token from it
        if (view == null) {
            view = new View(activity);
        }
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case STORAGE_RQUEST_CODE:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    startActivityForResult(new Intent(PhotosActivity.this, SavePictureActivity.class).putExtra("projectId", getIntent().getStringExtra("projectId")).putExtra("isPhotoFromGallary", true), 7890);
                }
                break;
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case 7890:
//                if (resultCode == Activity.RESULT_OK) {
//                if (ProjectDocuUtilities.isNetworkConnected(PhotosActivity.this) || ProjectNavigator.wlanIsConnected(PhotosActivity.this)) {

                if (data != null && data.getExtras() != null && data.getExtras().get("photoId") != null && !data.getExtras().get("photoId").equals("")) {
                    int position = data.getExtras().getInt("position");
                    Utils.showLogger("PhotoActivity onActivityResult");
                    //   new ReterivePhotoObjectAsyncTask(position).execute(data.getExtras().get("photoId") + "");

                    Handler handler = new Handler();
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            iv_sync_all.callOnClick();
                        }
                    }, 400);

                }

//                }//                }
                break;
        }


    }


    public void syncAllPhotos(boolean isAutoSync) {
        Utils.showLogger("PhotoActivity syncAllPhotos" + isAutoSync);
        boolean isBreakOuterLoop = false;
        boolean isCalledBackgroundtask = false;

        Fragment fragment = photosPagerAdapter.getM1stFragment();
        if (fragment == null)
            return;

        LocalPhotosFragment localPhotosFragment = (LocalPhotosFragment) fragment;
        LocalPhotosViewModel localPhotosViewModel = localPhotosFragment.getLocalPhotosViewModel();

        localPhotosViewModel.isAutoSyncPhoto = isAutoSync;

        List<PhotoModel> photoModelList = localPhotosFragment.lastData;
        if (photoModelList == null)
            return;
        if (photoModelList != null && photoModelList.size() > 0) {

            boolean isTrue = sharedPrefsManager.getBooleanValue(AppConstantsManager.SYNC_PHOTO_ASC_ORDER, AppConstantsManager.SYNC_PHOTO_ASC_ORDER_DEFAULT_VALUE);
            if (sharedPrefsManager.getBooleanValue(AppConstantsManager.SYNC_PHOTO_ASC_ORDER, AppConstantsManager.SYNC_PHOTO_ASC_ORDER_DEFAULT_VALUE)) {

                Utils.showLogger("yes this code works>>");

                for (int i = photoModelList.size() - 1; i >= 0; i--) {
                    if (localPhotosViewModel != null && isAutoSync) {
                        PhotoModel photoModel = photoModelList.get(i);
                        //Utils.showLogger("old_status_is"+photoModel.getPhotoUploadStatus()+photoModel.getFailedCount());
                        if (ProjectNavigator.mobileNetworkIsConnected(PhotosActivity.this) || ProjectNavigator.wlanIsConnected(PhotosActivity.this)) {

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

                            Utils.showLogger("WhileSynching1" + photoModel.isPhotoSynced());
                            Utils.showLogger("WhileSynching2" + photoModel.getPhotoUploadStatus());


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
                        if (ProjectNavigator.mobileNetworkIsConnected(PhotosActivity.this) || ProjectNavigator.wlanIsConnected(PhotosActivity.this)) {

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


        Utils.showLogger("isCalledBackgroundtask>>" + isCalledBackgroundtask);
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

    public void syncAllPhotos2(boolean isAutoSync) {
        Utils.showLogger("PhotoActivity syncAllPhotos" + isAutoSync);
        boolean isBreakOuterLoop = false;
        boolean isCalledBackgroundtask = false;
        Fragment fragment = photosPagerAdapter.getM1stFragment();
        if (fragment == null)
            return;
        LocalPhotosFragment localPhotosFragment = (LocalPhotosFragment) fragment;
        LocalPhotosViewModel localPhotosViewModel = localPhotosFragment.getLocalPhotosViewModel();
        if (localPhotosViewModel == null)
            return;
        localPhotosViewModel.isAutoSyncPhoto = isAutoSync;
        Utils.showLogger("PhotoActivity2 syncAllPhotos" + isAutoSync);
        if (localPhotosFragment != null && localPhotosViewModel.getAdapter() != null && localPhotosViewModel.getAdapter().getPhotosGroups() != null) {

            Utils.showLogger("PhotoActivity3 syncAllPhotos" + isAutoSync);
            for (Date key : localPhotosViewModel.getAdapter().getPhotosGroups().keySet()) {
                Utils.showLogger("PhotoActivity4 syncAllPhotos" + isAutoSync);
                List<PhotoModel> photoModelList = localPhotosViewModel.getAdapter().getPhotosGroups().get(key);
                if (photoModelList != null && photoModelList.size() > 0) {
                    Utils.showLogger("PhotoActivity5 syncAllPhotos" + isAutoSync);
                    boolean isTrue = sharedPrefsManager.getBooleanValue(AppConstantsManager.SYNC_PHOTO_ASC_ORDER, AppConstantsManager.SYNC_PHOTO_ASC_ORDER_DEFAULT_VALUE);
                    if (sharedPrefsManager.getBooleanValue(AppConstantsManager.SYNC_PHOTO_ASC_ORDER, AppConstantsManager.SYNC_PHOTO_ASC_ORDER_DEFAULT_VALUE)) {

                        for (int i = photoModelList.size() - 1; i >= 0; i--) {
                            if (localPhotosViewModel != null && isAutoSync) {
                                PhotoModel photoModel = photoModelList.get(i);
                                //Utils.showLogger("old_status_is"+photoModel.getPhotoUploadStatus()+photoModel.getFailedCount());
                                if (ProjectNavigator.mobileNetworkIsConnected(PhotosActivity.this) || ProjectNavigator.wlanIsConnected(PhotosActivity.this)) {

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
                                Utils.showLogger("CHECKING_THEIR>>>" + photoModel.isPhotoSynced());
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
                                if (ProjectNavigator.mobileNetworkIsConnected(PhotosActivity.this) || ProjectNavigator.wlanIsConnected(PhotosActivity.this)) {

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
                if (isBreakOuterLoop)
                    break;
            }
        }

        Utils.showLogger("isCalledBackgroundtask>>" + isCalledBackgroundtask);

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

    @Override
    protected void onResume() {
        super.onResume();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            registerReceiver(updateFlawFlag, new IntentFilter(BR_ACTION_UPDATE_PHOTO_AND_PLAN_LOC), Context.RECEIVER_EXPORTED);
        }
        ProjectNavigator.isPhotoActivityForground = true;
    }

    private void clearFilter() {
        Context mContext = this;
        if (groupheadingModelList != null) {
            for (int i = 0; i < groupheadingModelList.size(); i++) {
                if (groupheadingModelList.get(i).getType().equalsIgnoreCase(mContext.getResources().getString(R.string.heading_sorting))) {
                    groupheadingModelList.get(i).getListChildDataSelected().clear();

                    groupheadingModelList.get(i).getListChildDataSelected().add(childRowModelDescendingDate.getId());
                    isFilterSetForFirstTime = true;

                } else if (groupheadingModelList.get(i).getType().equalsIgnoreCase(mContext.getResources().getString(R.string.heading_photo_desc))) {
                    if (groupheadingModelList.get(i).getKeyword() != null && !groupheadingModelList.get(i).getKeyword().equals("")) {
                        groupheadingModelList.get(i).setKeyword("");
                    }

                } else if (groupheadingModelList.get(i).getType().equalsIgnoreCase(mContext.getResources().getString(R.string.heading_photo_number))) {
                    if (groupheadingModelList.get(i).getKeyword() != null && !groupheadingModelList.get(i).getKeyword().equals("")) {
                        groupheadingModelList.get(i).setKeyword("");
                    }
                } else if (groupheadingModelList.get(i).getType().equalsIgnoreCase(mContext.getResources().getString(R.string.heading_deadline))) {

                    groupheadingModelList.get(i).getListChildDataSelected().clear();


                } else if (groupheadingModelList.get(i).getType().equalsIgnoreCase(mContext.getResources().getString(R.string.heading_photo_creator))) {
                    groupheadingModelList.get(i).getListChildDataSelected().clear();

                } else if (groupheadingModelList.get(i).getType().equalsIgnoreCase(mContext.getResources().getString(R.string.heading_photo_switch_decs))) {
                    if (groupheadingModelList.get(i).isSwitchOn()) {
                        groupheadingModelList.get(i).setSwitchOn(false);
                    }


                } else if (groupheadingModelList.get(i).getType().equalsIgnoreCase(mContext.getResources().getString(R.string.heading_photo_switch_keyword))) {
                    if (groupheadingModelList.get(i).isSwitchOn()) {

                        groupheadingModelList.get(i).setSwitchOn(false);
                    }


                } else if (groupheadingModelList.get(i).getType().equalsIgnoreCase(mContext.getResources().getString(R.string.heading_localized_photo))) {
                    if (groupheadingModelList.get(i).isSwitchOn()) {

                        groupheadingModelList.get(i).setSwitchOn(false);
                    }


                } else if (groupheadingModelList.get(i).getType().equalsIgnoreCase(mContext.getResources().getString(R.string.heading_photo_keyword))) {

                    groupheadingModelList.get(i).getListChildDataSelected().clear();
                    groupheadingModelList.get(i).getWordModelArrayList().clear();

                } else if (groupheadingModelList.get(i).getType().equalsIgnoreCase(mContext.getResources().getString(R.string.heading_created_date))) {

//                        if (groupheadingModelList.get(i).getListChildDataSelected().size() > 0) {
//                            selectedDate.addAll(groupheadingModelList.get(i).getListChildDataSelected());
                    SimpleDateFormat simpleDateFormat;

                    Date startDate = null;
                    Date endDate = null;
                    String dateOf5YearsBack;
                    String currentDate;
                    Date defaultStartDateObject = null;
                    Date defaultEndDateObject = null;

                    final Calendar c = Calendar.getInstance();
                    c.add(Calendar.DAY_OF_MONTH, +1);
                    int year = c.get(Calendar.YEAR);
                    int month = c.get(Calendar.MONTH);
                    int day = c.get(Calendar.DAY_OF_MONTH);

                    currentDate = year + "-" + month + "-" + day;

                    final Calendar c2 = Calendar.getInstance();
                    c2.add(Calendar.YEAR, -5);
                    int year2 = c2.get(Calendar.YEAR);
                    int month2 = c2.get(Calendar.MONTH);
                    int day2 = c2.get(Calendar.DAY_OF_MONTH);

                    dateOf5YearsBack = year2 + "-" + month2 + "-" + day2;

                    if (sharedPrefsManager.getStringValue(AppConstantsManager.APP_LANGUAGE, "de").equals("de")) {
                        dateOf5YearsBack = day2 + "." + month2 + "." + year2;
                        currentDate = day + "." + month + "." + year;
                    } else {
                        dateOf5YearsBack = year2 + "-" + month2 + "-" + day2;
                        currentDate = year + "-" + month + "-" + day;
                    }

                    startDate = null;
                    endDate = null;

                    if (sharedPrefsManager.getStringValue(AppConstantsManager.APP_LANGUAGE, "de").equals("de")) {
                        simpleDateFormat = new SimpleDateFormat("dd.MM.yyyy");

                    } else {
                        simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
                    }


                    try {
                        startDate = simpleDateFormat.parse(dateOf5YearsBack);
                        endDate = simpleDateFormat.parse(currentDate);
                        defaultStartDateObject = simpleDateFormat.parse(defaultStartDate);
                        defaultEndDateObject = simpleDateFormat.parse(defaultEndDate);
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }


//                    groupheadingModelList.get(i).setStart_date(0);
//                    groupheadingModelList.get(i).setEnd_date(0);
                    if (defaultStartDateObject != null && defaultEndDateObject != null) {
                        groupheadingModelList.get(i).setStart_date(defaultStartDateObject.getTime());
                        groupheadingModelList.get(i).setEnd_date(defaultEndDateObject.getTime());
                    } else {
                        groupheadingModelList.get(i).setStart_date(0);
                        groupheadingModelList.get(i).setEnd_date(0);
                    }


//                        }
                } else if (groupheadingModelList.get(i).getType().equalsIgnoreCase(mContext.getResources().getString(R.string.heading_photo_plans))) {
                    groupheadingModelList.get(i).getListChildDataSelected().clear();

                }
            }

            applyFilter();


            if (expandableListAdapter != null)
                expandableListAdapter.notifyDataSetChanged();

        }
        if (!isOnlinePhotoTabSelected) {
            isOnlinePhotoFilterApplied = true;
        } else {
            applyOnlinePhotoFilter();
            hideFilterView();
        }
        filterCount = 0;
        if (tv_count != null)
            tv_count.setVisibility(View.GONE);
        if (tv_count_2 != null)
            tv_count_2.setVisibility(View.INVISIBLE);

    }

    private void loadFacetsData() {
        groupheadingModelList.clear();
        loadSortingAndCreatedHeader();
        expandableListAdapter = new ExpandablePhotosFIlterListAdapter(this, groupheadingModelList, defaultStartDate, defaultEndDate);
        simple_expandable_listview.setAdapter(expandableListAdapter);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.toolbar_option_menu_photo_filter, menu);


        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.filter:
                // Set the text color to red
                if (ll_expand_view.getVisibility() != View.VISIBLE) {
                    showFilterView();
//                    loadFacetsData();
                } else {
                    hideFilterView();
                }
                return true;
            case R.id.report:

                if (photosViewPager.getCurrentItem() == 0)
                    startActivity(new Intent(this, ShareDeleteLocalPhotosActivity.class).putExtra("projectId", projectID));
                else
                    startActivity(new Intent(this, ReportPhotosActivity.class).putExtra("projectId", projectID));


                //              new DeletedLocalPhotoAsyncTask().execute();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }


    private void showFilterView() {
        ll_hidenView.setVisibility(View.VISIBLE);
        ll_expand_view.setVisibility(View.VISIBLE);
    }

    private void hideFilterView() {
        ll_hidenView.setVisibility(View.GONE);
        ll_expand_view.setVisibility(View.GONE);
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();

        ProjectNavigator.isPhotoActivityForground = false;
        if (updateFlawFlag != null) {
            unregisterReceiver(updateFlawFlag);
            updateFlawFlag = null;
        }
    }

    @Override
    public void onLcoalPhotoFragmentInteraction(Uri uri) {

    }

    @Override
    public void onOnlineFragmentInteraction(Uri uri) {

    }


    public void loadSortingAndCreatedHeader() {
        List<ChildRowModel> childRowModelList = new ArrayList<>();

        ChildRowModel childRowModel = new ChildRowModel();
        childRowModel.setId("1");
        childRowModel.setTitle(getResources().getString(R.string.sort_asc_by_date));
        childRowModelList.add(childRowModel);

        childRowModelDescendingDate = new ChildRowModel();
        childRowModelDescendingDate.setId("2");
        childRowModelDescendingDate.setTitle(getResources().getString(R.string.sort_des_by_date));
        childRowModelList.add(childRowModelDescendingDate);

        ChildRowModel childRowModel1 = new ChildRowModel();
        childRowModel1.setId("3");
        childRowModel1.setTitle(getResources().getString(R.string.sort_run_id_des_by_date));
        childRowModelList.add(childRowModel1);

        childRowModel = new ChildRowModel();
        childRowModel.setId("4");
        childRowModel.setTitle(getResources().getString(R.string.sort_run_id_asc_by_date));
        childRowModelList.add(childRowModel);


//        childRowModel = new ChildRowModel();
//        childRowModel.setId("3");
//        childRowModel.setTitle(getResources().getString(R.string.sort_run_id_asc_by_date));
//        childRowModelList.add(childRowModel);
//
//        childRowModel1 = new ChildRowModel();
//        childRowModel1.setId("4");
//        childRowModel1.setTitle(getResources().getString(R.string.sort_run_id_des_by_date));
//        childRowModelList.add(childRowModel1);

        groupheadingModelList.add(new GroupheadingModel(getResources().getString(R.string.heading_sorting), getResources().getString(R.string.heading_sorting), false, new HashMap<String, List<ChildRowModel>>() {{
            put(getResources().getString(R.string.heading_sorting), childRowModelList);
        }}));

        groupheadingModelList.get(0).getListChildDataSelected().add(childRowModelDescendingDate.getId());


//        List<ChildRowModel> childRowModelList3 = new ArrayList<>();
//
////        ChildRowModel childRowModel3 = new ChildRowModel(a);
////        childRowModel3.setId("1");
////        childRowModel3.setTitle(getResources().getString(R.string.sort_asc_by_date));
////        childRowModelList3.add(childRowModel3);
//
//
//        groupheadingModelList.add(new GroupheadingModel(getResources().getString(R.string.heading_photo_desc), getResources().getString(R.string.heading_photo_desc), false, new HashMap<String, List<ChildRowModel>>() {{
//            put(getResources().getString(R.string.heading_photo_desc), childRowModelList3);
//        }}));
//        groupheadingModelList.add(new GroupheadingModel(getResources().getString(R.string.heading_photo_number), getResources().getString(R.string.heading_photo_number), false, new HashMap<String, List<ChildRowModel>>() {{
//            put(getResources().getString(R.string.heading_photo_number), childRowModelList3);
//        }}));
//        groupheadingModelList.add(new GroupheadingModel(getResources().getString(R.string.heading_localized_photo), getResources().getString(R.string.heading_localized_photo), false, new HashMap<String, List<ChildRowModel>>() {{
//            put(getResources().getString(R.string.heading_localized_photo), childRowModelList3);
//        }}));
//        groupheadingModelList.add(new GroupheadingModel(getResources().getString(R.string.heading_photo_switch_keyword), getResources().getString(R.string.heading_photo_switch_keyword), false, new HashMap<String, List<ChildRowModel>>() {{
//            put(getResources().getString(R.string.heading_photo_switch_keyword), childRowModelList3);
//        }}));
//
//        groupheadingModelList.add(new GroupheadingModel(getResources().getString(R.string.heading_photo_switch_decs), getResources().getString(R.string.heading_photo_switch_decs), false, new HashMap<String, List<ChildRowModel>>() {{
//            put(getResources().getString(R.string.heading_photo_switch_decs), childRowModelList3);
//        }}));
//
//        groupheadingModelList.add(new GroupheadingModel(projectID, getResources().getString(R.string.heading_photo_keyword), getResources().getString(R.string.heading_photo_keyword), false, new HashMap<String, List<ChildRowModel>>() {{
//            put(getResources().getString(R.string.heading_photo_keyword), childRowModelList3);
//        }}));


        loadPhotoCreatedHeader();
//        if (expandableListAdapter != null) {
//            expandableListAdapter.notifyDataSetChanged();
//        }
    }

    public void loadPhotoCreatedHeader() {

        new GetAllUserCreatePhotosAsyncTask().execute(projectID);
    }

    private void bindView() {
        photosViewPager =    findViewById(R.id.photo_pager_view);
        pagerTabStrip =    findViewById(R.id.photos_tab_strip);
        toolbar =    findViewById(R.id.toolbar);
        simple_expandable_listview =    findViewById(R.id.simple_expandable_listview);
        ll_hidenView =    findViewById(R.id.ll_hidenView);
        ll_expand_view =    findViewById(R.id.ll_expand_view);
        ll_bottom_tabs =    findViewById(R.id.ll_bottom_tabs);
        iv_folder_icon =    findViewById(R.id.iv_folder_icon);
        iv_info =    findViewById(R.id.iv_info);
        iv_sync_all =    findViewById(R.id.iv_sync_all);
        tv_reset_filter =    findViewById(R.id.tv_reset_filter);
        tv_apply_online_filter =    findViewById(R.id.tv_apply_online_filter);
        ivAdd =    findViewById(R.id.ivAdd);
        ivFilter =    findViewById(R.id.ivFilter);
        ivMenu =    findViewById(R.id.ivMenu);
        llCart =    findViewById(R.id.ll_cart);
        tv_count =    findViewById(R.id.tv_count);
        tv_count_2 =    findViewById(R.id.tv_count_2);
    }


//    private class GetAllPhotosProjectAsyncTask extends AsyncTask<String, Void, List<PhotoModel> > {
//
//
//        @Override
//        protected List<PhotoModel>  doInBackground(String... params) {
//            List<PhotoModel> photosList = photoDao.getPhotosList(params[0]);
//            return photosList;
//        }
//
//        @Override
//        protected void onPostExecute(List<PhotoModel> photoModels) {
//            super.onPostExecute(photoModels);
//
//            for (int i = 0; i <photoModels.size() ; i++) {
//
//            }
//
//        }
//    }


    private class GetAllUserCreatePhotosAsyncTask extends AsyncTask<String, Void, List<String>> {


        @Override
        protected List<String> doInBackground(String... params) {
            List<String> localPhotosList = photoDao.getPhotosUserIdList(params[0], LocalPhotosRepository.TYPE_LOCAL_PHOTO);
            List<String> OnlinePhotosList = onlinePhotoDao.getPhotosUserIdList(params[0], LocalPhotosRepository.TYPE_ONLINE_PHOTO);
            photoModelListMinDateLocalPhotoList = photoDao.getMinCreatedPhoto(params[0], LocalPhotosRepository.TYPE_LOCAL_PHOTO);
            photoModelListMinDateOnlinePhotoList = onlinePhotoDao.getMinCreatedPhoto(params[0], LocalPhotosRepository.TYPE_ONLINE_PHOTO);
            List<PlansModel> allPlansList = ProjectsDatabase.getDatabase(PhotosActivity.this).plansDao().getAllPlansList(projectID);

            long minDate = 0;
            long minDateOnline = 0;
            String[] tempStrArray = new String[2];

            List<ChildRowModel> childRowModelListt = new ArrayList<>();
            ChildRowModel childRowModel = new ChildRowModel();

            if (photoModelListMinDateLocalPhotoList != null && photoModelListMinDateLocalPhotoList.size() > 0) {
                minDate = photoModelListMinDateLocalPhotoList.get(0).created_df;

            }

            if (photoModelListMinDateOnlinePhotoList != null && photoModelListMinDateOnlinePhotoList.size() > 0) {
                minDateOnline = photoModelListMinDateOnlinePhotoList.get(0).created_df;
            }

            if (minDate < minDateOnline) {
                childRowModel.setId(String.valueOf(minDate));
                if (photoModelListMinDateLocalPhotoList.size() > 0 && photoModelListMinDateLocalPhotoList.get(0).getCreated() != null) {
                    childRowModel.setTitle(photoModelListMinDateLocalPhotoList.get(0).getCreated().replace("-", "/"));
                }
            } else {
                childRowModel.setId(String.valueOf(minDateOnline));
                if (photoModelListMinDateOnlinePhotoList.size() > 0 && photoModelListMinDateOnlinePhotoList.get(0).getCreated() != null) {
                    tempStrArray = photoModelListMinDateOnlinePhotoList.get(0).getCreated().split(" ");
                    childRowModel.setTitle(tempStrArray[0].replace("-", "/"));
                }
            }
            childRowModelListt.add(childRowModel);

//            List<ChildRowModel> childRowModelList2 = new ArrayList<>();
//
//            ChildRowModel childRowModel2 = new ChildRowModel();
//            childRowModel2.setId("1");
//            childRowModel2.setTitle(getResources().getString(R.string.heading_created_date));
//            childRowModelList2.add(childRowModel2);


            //your code here
            groupheadingModelList.add(new GroupheadingModel(getResources().getString(R.string.heading_created_date), getResources().getString(R.string.heading_created_date), false, new HashMap<String, List<ChildRowModel>>() {{
                put(getResources().getString(R.string.heading_created_date), childRowModelListt);
            }}));

            List<ChildRowModel> childRowModelList3 = new ArrayList<>();

//        ChildRowModel childRowModel3 = new ChildRowModel(a);
//        childRowModel3.setId("1");
//        childRowModel3.setTitle(getResources().getString(R.string.sort_asc_by_date));
//        childRowModelList3.add(childRowModel3);


            groupheadingModelList.add(new GroupheadingModel(getResources().getString(R.string.heading_photo_number), getResources().getString(R.string.heading_photo_number), false, new HashMap<String, List<ChildRowModel>>() {{
                put(getResources().getString(R.string.heading_photo_number), childRowModelList3);
            }}));

            groupheadingModelList.add(new GroupheadingModel(getResources().getString(R.string.heading_photo_desc), getResources().getString(R.string.heading_photo_desc), false, new HashMap<String, List<ChildRowModel>>() {{
                put(getResources().getString(R.string.heading_photo_desc), childRowModelList3);
            }}));

            groupheadingModelList.add(new GroupheadingModel(projectID, getResources().getString(R.string.heading_photo_keyword), getResources().getString(R.string.heading_photo_keyword), false, new HashMap<String, List<ChildRowModel>>() {{
                put(getResources().getString(R.string.heading_photo_keyword), childRowModelList3);
            }}));


            if (allPlansList != null) {
                List<ChildRowModel> groupheadingModelList1 = new ArrayList<>();
                for (int i = 0; i < allPlansList.size(); i++) {
                    ChildRowModel groupheadingModel = new ChildRowModel();
                    groupheadingModel.setId(allPlansList.get(i).getPlanId());
                    groupheadingModel.setTitle(allPlansList.get(i).getDescription());

                    groupheadingModelList1.add(groupheadingModel);
                }
                groupheadingModelList.add(new GroupheadingModel(projectID, getResources().getString(R.string.heading_photo_plans), getResources().getString(R.string.heading_photo_plans), true, new HashMap<String, List<ChildRowModel>>() {{
                    put(getResources().getString(R.string.heading_photo_plans), groupheadingModelList1);
                }}));
            }

            List<String> photosList = new ArrayList<>();
            if (localPhotosList != null)
                photosList.addAll(localPhotosList);
            if (OnlinePhotosList != null)
                photosList.addAll(OnlinePhotosList);
            List<ProjectUserModel> usersProjectList = ProjectsDatabase.getDatabase(PhotosActivity.this).projectUsersDao().getProjectUserListInfo(projectID, photosList);


            usersList = new ArrayList<>();
            for (int i = 0; i < usersProjectList.size(); i++) {
                ChildRowModel childRowModel1 = new ChildRowModel();
                childRowModel1.setId(usersProjectList.get(i).getPduserid());
                childRowModel1.setTitle(usersProjectList.get(i).getFirstname() + " " + usersProjectList.get(i).getLastname());

                usersList.add(childRowModel1);
            }


            groupheadingModelList.add(new GroupheadingModel(getResources().getString(R.string.heading_photo_creator), getResources().getString(R.string.heading_photo_creator), true, new HashMap<String, List<ChildRowModel>>() {{
                put(getResources().getString(R.string.heading_photo_creator), usersList);
            }}));

            groupheadingModelList.add(new GroupheadingModel(getResources().getString(R.string.heading_localized_photo), getResources().getString(R.string.heading_localized_photo), false, new HashMap<String, List<ChildRowModel>>() {{
                put(getResources().getString(R.string.heading_localized_photo), childRowModelList3);
            }}));

            groupheadingModelList.add(new GroupheadingModel(getResources().getString(R.string.heading_photo_switch_decs), getResources().getString(R.string.heading_photo_switch_decs), false, new HashMap<String, List<ChildRowModel>>() {{
                put(getResources().getString(R.string.heading_photo_switch_decs), childRowModelList3);
            }}));

            groupheadingModelList.add(new GroupheadingModel(getResources().getString(R.string.heading_photo_switch_keyword), getResources().getString(R.string.heading_photo_switch_keyword), false, new HashMap<String, List<ChildRowModel>>() {{
                put(getResources().getString(R.string.heading_photo_switch_keyword), childRowModelList3);
            }}));


            onTabSwitchUpdateFilterList(0);


            return photosList;
        }

        @Override
        protected void onPostExecute(List<String> photoModels) {
            super.onPostExecute(photoModels);

            if (expandableListAdapter != null) {
                expandableListAdapter.notifyDataSetChanged();
            }
            if (isLoadingFilterData != null)
                isLoadingFilterData.setValue(true);


//            new RetriveProjectUsersAsyncTask(PhotosActivity.this).execute(photoModels);

        }
    }

    private class RetriveProjectUsersAsyncTask extends AsyncTask<List<String>, Void, List<ProjectUserModel>> {

        private ProjectUsersDao mAsyncTaskDao;

        RetriveProjectUsersAsyncTask(Context context) {
            mAsyncTaskDao = ProjectsDatabase.getDatabase(context).projectUsersDao();
        }

        @Override
        protected List<ProjectUserModel> doInBackground(final List<String>... params) {
            List<ProjectUserModel> stringList = mAsyncTaskDao.getProjectUserListInfo(projectID, params[0]);
            return stringList;
        }

        @Override
        protected void onPostExecute(List<ProjectUserModel> params) {
            super.onPostExecute(params);
            usersList = new ArrayList<>();
            for (int i = 0; i < params.size(); i++) {
                ChildRowModel childRowModel = new ChildRowModel();
                childRowModel.setId(params.get(i).getPduserid());
                childRowModel.setTitle(params.get(i).getFirstname() + " " + params.get(i).getLastname());

                usersList.add(childRowModel);
            }
            long minDate = 0;
            long minDateOnline = 0;
            String[] tempStrArray = new String[2];

            List<ChildRowModel> childRowModelListt = new ArrayList<>();
            ChildRowModel childRowModel = new ChildRowModel();

            if (photoModelListMinDateLocalPhotoList != null && photoModelListMinDateLocalPhotoList.size() > 0) {
                minDate = photoModelListMinDateLocalPhotoList.get(0).created_df;

            }

            if (photoModelListMinDateOnlinePhotoList != null && photoModelListMinDateOnlinePhotoList.size() > 0) {
                minDateOnline = photoModelListMinDateOnlinePhotoList.get(0).created_df;
            }

            if (minDate < minDateOnline) {
                childRowModel.setId(String.valueOf(minDate));
                if (photoModelListMinDateLocalPhotoList.size() > 0 && photoModelListMinDateLocalPhotoList.get(0).getCreated() != null) {
                    childRowModel.setTitle(photoModelListMinDateLocalPhotoList.get(0).getCreated().replace("-", "/"));
                }
            } else {
                childRowModel.setId(String.valueOf(minDateOnline));
                if (photoModelListMinDateOnlinePhotoList.size() > 0 && photoModelListMinDateOnlinePhotoList.get(0).getCreated() != null) {
                    tempStrArray = photoModelListMinDateOnlinePhotoList.get(0).getCreated().split(" ");
                    childRowModel.setTitle(tempStrArray[0].replace("-", "/"));
                }
            }
            childRowModelListt.add(childRowModel);

//            List<ChildRowModel> childRowModelList2 = new ArrayList<>();
//
//            ChildRowModel childRowModel2 = new ChildRowModel();
//            childRowModel2.setId("1");
//            childRowModel2.setTitle(getResources().getString(R.string.heading_created_date));
//            childRowModelList2.add(childRowModel2);
            groupheadingModelList.add(new GroupheadingModel(getResources().getString(R.string.heading_created_date), getResources().getString(R.string.heading_created_date), false, new HashMap<String, List<ChildRowModel>>() {{
                put(getResources().getString(R.string.heading_created_date), childRowModelListt);
            }}));

            groupheadingModelList.add(new GroupheadingModel(getResources().getString(R.string.heading_photo_creator), getResources().getString(R.string.heading_photo_creator), true, new HashMap<String, List<ChildRowModel>>() {{
                put(getResources().getString(R.string.heading_photo_creator), usersList);
            }}));
//            Fragment fragment = photosPagerAdapter.getM2ndFragment();
//            if (fragment!=null&&((OnlinePhotosFragment) fragment).onlinePhotosViewModel.adapter!=null&&((OnlinePhotosFragment) fragment).onlinePhotosViewModel.adapter.getItemCount()>0) {
//
//                   isShowFilterIcon.setValue(true);
//            } else {
//                   isShowIconReport.setValue(false);
//            }

            if (expandableListAdapter != null) {
                expandableListAdapter.notifyDataSetChanged();
            }
            if (isLoadingFilterData != null)
                isLoadingFilterData.setValue(true);


        }

    }

    public void onTabSwitchUpdateFilterList(int position) {
        boolean isCreatorAdd = false;

        if (groupheadingModelList != null) {
            for (int i = 0; i < groupheadingModelList.size(); i++) {
                if (position == 0) {
                    if (groupheadingModelList.get(i).getType().equals(getResources().getString(R.string.heading_photo_creator))) {
                        groupheadingModelList.remove(i);
                        isCreatorAdd = true;
                        break;
                    }
                }
            }
            if (!isCreatorAdd && usersList != null && usersList.size() > 0) {
                groupheadingModelList.add(new GroupheadingModel(getResources().getString(R.string.heading_photo_creator), getResources().getString(R.string.heading_photo_creator), true, new HashMap<String, List<ChildRowModel>>() {{
                    put(getResources().getString(R.string.heading_photo_creator), usersList);
                }}));
            }

        }
        if (expandableListAdapter != null) {
            expandableListAdapter.notifyDataSetChanged();
        }

    }

    //region applyFilter
    public void applyFilter() {

        new RetriveFilteredDataAsyncTask(this).execute(groupheadingModelList);
//
//        if (groupheadingModelList != null) {
//            Fragment fragment = photosPagerAdapter.getM2ndFragment();
//            ((OnlinePhotosFragment) fragment).applyFilterOnlinePhotos(groupheadingModelList, true);
//        }
    }
    //endregion

    //region applyOnlinePhotoFilter
    public void applyOnlinePhotoFilter() {

//        new RetriveFilteredDataAsyncTask(this).execute(groupheadingModelList);

        if (groupheadingModelList != null) {
            Fragment fragment = photosPagerAdapter.getM2ndFragment();
            ((OnlinePhotosFragment) fragment).applyFilterOnlinePhotos(groupheadingModelList, true);
        }

    }
    //endregion

    int filterCount = 0;

    class RetriveFilteredDataAsyncTask extends AsyncTask<List<GroupheadingModel>, Void, List<PhotoModel>> {

        Context mContext;

        RetriveFilteredDataAsyncTask(Context context/*, DefectsDao dao*/) {
            mContext = context;
        }

        @Override
        protected List<PhotoModel> doInBackground(final List<GroupheadingModel>... params) {


            // Single Selection
            // Art
            // Date
            // Deadline

            List<String> selectedStatus = new ArrayList<>();
            List<String> selectedArt = new ArrayList<>();
            List<String> selectedGewerk = new ArrayList<>();
            List<String> selectedDeadline = new ArrayList<>();
            List<String> selectedResponsible = new ArrayList<>();
            List<String> selectedCreator = new ArrayList<>();
            List<String> selectedDate = new ArrayList<>();
            List<String> selectedPlan = new ArrayList<>();

            String PREDICATE_PHOTO_TYPE = LocalPhotosRepository.TYPE_LOCAL_PHOTO;
            String PREDICATE_SORTING = "";
            String PREDICATE_DESCRIPTION = "";
            String PREDICATE_DESCRIPTION_SWITCH = "";
            String PREDICATE_KEYWORD = "";
            String PREDICATE_KEYWORD_SWITCH = "";
            String PREDICATE_LOCALIZED_SWITCH = "";
            String PREDICATE_DEADLINE = "";
            String PREDICATE_KEYWORDS = "";
            String PREDICATE_CREATOR = "";
            String PREDICATE_CREATED_DATE = "";

            long date = 0;
            long start_date = 0;
            long end_date = 0;

            String art = "";
            String tag_keyword = "";

            boolean isApplyGewerk = false;
            filterCount = 0;
            List<GroupheadingModel> groupadheadingModelList = params[0];
            if (groupheadingModelList != null) {

                for (int i = 0; i < groupheadingModelList.size(); i++) {
                    if (groupheadingModelList.get(i).getType().equalsIgnoreCase(mContext.getResources().getString(R.string.heading_photo_desc))) {
                        if (groupheadingModelList.get(i).getKeyword() != null && !groupheadingModelList.get(i).getKeyword().equals("")) {
                            art = groupheadingModelList.get(i).getKeyword();
//                            PREDICATE_DESCRIPTION = " AND description LIKE '%" + art+"%'" ;
                            PREDICATE_DESCRIPTION = " AND description LIKE '%" + art + "%'";
                            filterCount++;
                        }

                    } else if (groupheadingModelList.get(i).getType().equalsIgnoreCase(mContext.getResources().getString(R.string.heading_photo_number))) {
                        if (groupheadingModelList.get(i).getKeyword() != null && !groupheadingModelList.get(i).getKeyword().equals("")) {
                            tag_keyword = groupheadingModelList.get(i).getKeyword();
                            PREDICATE_KEYWORD = " AND pdphotolocalId  =" + tag_keyword;
                            filterCount++;
                        }

                    } else if (groupheadingModelList.get(i).getType().equalsIgnoreCase(mContext.getResources().getString(R.string.heading_deadline))) {
                        if (groupheadingModelList.get(i).getListChildDataSelected().size() > 0) {
                            selectedDeadline.addAll(groupheadingModelList.get(i).getListChildDataSelected());
                            date = groupheadingModelList.get(i).getStart_date();
                            PREDICATE_DEADLINE = " AND fristdate_df <= " + date;
                        }


                    } else if (groupheadingModelList.get(i).getType().equalsIgnoreCase(mContext.getResources().getString(R.string.heading_photo_creator))) {
                        if (groupheadingModelList.get(i).getListChildDataSelected().size() > 0) {
                            selectedResponsible.addAll(groupheadingModelList.get(i).getListChildDataSelected());
                            String inClause = selectedResponsible.toString();
                            inClause = inClause.replace("[", "(");
                            inClause = inClause.replace("]", ")");
                            PREDICATE_CREATOR = " AND pdUserId in " + inClause;
                            filterCount = filterCount + groupheadingModelList.get(i).getListChildDataSelected().size();

                        }


                    } else if (groupheadingModelList.get(i).getType().equalsIgnoreCase(mContext.getResources().getString(R.string.heading_photo_switch_decs))) {
                        if (groupheadingModelList.get(i).isSwitchOn()) {

                            PREDICATE_DESCRIPTION_SWITCH = " AND description !='' ";
                            filterCount++;
                        }


                    } else if (groupheadingModelList.get(i).getType().equalsIgnoreCase(mContext.getResources().getString(R.string.heading_localized_photo))) {
                        if (groupheadingModelList.get(i).isSwitchOn()) {

                            PREDICATE_LOCALIZED_SWITCH = " AND plan_id !='' ";
                            filterCount++;
                        }


                    } else if (groupheadingModelList.get(i).getType().equalsIgnoreCase(mContext.getResources().getString(R.string.heading_photo_switch_keyword))) {
                        if (groupheadingModelList.get(i).isSwitchOn()) {

                            PREDICATE_KEYWORD_SWITCH = " AND wordAdded = 1 ";
                            filterCount++;
                        }


                    } else if (groupheadingModelList.get(i).getType().equalsIgnoreCase(mContext.getResources().getString(R.string.heading_photo_keyword))) {

                        if (groupheadingModelList.get(i).getListChildDataSelected().size() > 0) {
                            selectedCreator.addAll(groupheadingModelList.get(i).getListChildDataSelected());
                            String inClause = selectedCreator.toString();
                            inClause = inClause.replace("[", "(");
                            inClause = inClause.replace("]", ")");
                            PREDICATE_KEYWORDS = " AND pdphotolocalId in " + inClause;
                            filterCount = filterCount + groupheadingModelList.get(i).getListChildDataSelected().size();

                        }

                    } else if (groupheadingModelList.get(i).getType().equalsIgnoreCase(mContext.getResources().getString(R.string.heading_photo_plans))) {
                        if (groupheadingModelList.get(i).getListChildDataSelected().size() > 0) {
                            selectedPlan.addAll(groupheadingModelList.get(i).getListChildDataSelected());
                            String inClause = selectedPlan.toString();
                            inClause = inClause.replace("[", "(");
                            inClause = inClause.replace("]", ")");
                            PREDICATE_CREATOR = " AND pdUserId in " + inClause;
//                            "operand": „EQ", "field": "pdplanid", "value": „26"
                            filterCount = filterCount + groupheadingModelList.get(i).getListChildDataSelected().size();

                        }


                    } else if (groupheadingModelList.get(i).getType().equalsIgnoreCase(mContext.getResources().getString(R.string.heading_created_date))) {

//                        if (groupheadingModelList.get(i).getListChildDataSelected().size() > 0) {
//                            selectedDate.addAll(groupheadingModelList.get(i).getListChildDataSelected());
                        start_date = groupheadingModelList.get(i).getStart_date();
                        end_date = groupheadingModelList.get(i).getEnd_date();

                        if (start_date > 0 && end_date > 0) {
                            PREDICATE_CREATED_DATE = " AND created_df >= " + start_date + " AND created_df <= " + end_date;
                            filterCount++;
                        }
//                        }
                    } else if (groupheadingModelList.get(i).getType().equalsIgnoreCase(mContext.getResources().getString(R.string.heading_sorting))) {

                        if (groupheadingModelList.get(i).getListChildDataSelected().size() > 0) {
                            selectedStatus.addAll(groupheadingModelList.get(i).getListChildDataSelected());

//                            String inClause = selectedStatus.toString();
//                            inClause = inClause.replace("[", "(");
//                            inClause = inClause.replace("]", ")");
//                            PREDICATE_SORTING = " AND status in " + inClause;

                            if (selectedStatus.size() > 0 && selectedStatus.get(0).equals("1")) {
                                PREDICATE_SORTING = "  ORDER BY created_df ASC ";

                            } else if (selectedStatus.size() > 0 && selectedStatus.get(0).equals("2")) {
                                PREDICATE_SORTING = "  ORDER BY created_df DESC ";
                            } else if (selectedStatus.size() > 0 && selectedStatus.get(0).equals("3")) {
                                PREDICATE_SORTING = "  ORDER BY created_df DESC ";
                            } else if (selectedStatus.size() > 0 && selectedStatus.get(0).equals("4")) {
                                PREDICATE_SORTING = "  ORDER BY created_df ASC ";
                            }

                            if (!isFilterSetForFirstTime) {
                                filterCount = filterCount + groupheadingModelList.get(i).getListChildDataSelected().size();
                            }


                        }

                    }
                }
            }
            if (PREDICATE_SORTING.equals("")) {
                PREDICATE_SORTING = "  ORDER BY created_df DESC ";
            }
            if (!isOnlinePhotoTabSelected) {
                isOnlinePhotoFilterApplied = true;
            }
            List<PhotoModel> defectsModelList = new ArrayList<>();
            SimpleSQLiteQuery query = new SimpleSQLiteQuery("SELECT * FROM  PhotoModel WHERE projectId = ? AND photo_type='" + PREDICATE_PHOTO_TYPE + "'" + PREDICATE_KEYWORD + PREDICATE_DESCRIPTION + PREDICATE_KEYWORDS + PREDICATE_CREATOR + PREDICATE_DEADLINE + PREDICATE_CREATED_DATE + PREDICATE_DESCRIPTION_SWITCH + PREDICATE_KEYWORD_SWITCH + PREDICATE_LOCALIZED_SWITCH + PREDICATE_SORTING,
                    new Object[]{projectID});
            List<PhotoModel> defectsModels = ProjectsDatabase.getDatabase(PhotosActivity.this).photoDao().getFilterListViaQuery(query);

            defectsModelList = defectsModels;

            return defectsModelList;
        }


        @Override
        protected void onPostExecute(List<PhotoModel> defectsModels) {
            // super.onPostExecute(list);
            if (tv_count != null && tv_count_2 != null) {
                if (filterCount > 0) {
                    tv_count.setText(filterCount + "");
                    tv_count_2.setText(filterCount + "");
                    tv_count.setVisibility(View.VISIBLE);
                    tv_count_2.setVisibility(View.VISIBLE);
                } else {
                    tv_count.setText(filterCount + "");
                    tv_count_2.setText(filterCount + "");
                    tv_count.setVisibility(View.INVISIBLE);
                    tv_count_2.setVisibility(View.INVISIBLE);
                }

            }
            if (defectsModels != null) {
                Fragment fragment = photosPagerAdapter.getM1stFragment();
                ((LocalPhotosFragment) fragment).updatePhotosResults(defectsModels);
            }
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


    class RetriveFilteredDataAsyncTaskOnlinePhotos extends AsyncTask<List<GroupheadingModel>, Void, List<OnlinePhotoModel>> {

        Context mContext;

        RetriveFilteredDataAsyncTaskOnlinePhotos(Context context/*, DefectsDao dao*/) {
            mContext = context;
        }

        @Override
        protected List<OnlinePhotoModel> doInBackground(final List<GroupheadingModel>... params) {


            // Single Selection
            // Art
            // Date
            // Deadline

            List<String> selectedStatus = new ArrayList<>();
            List<String> selectedArt = new ArrayList<>();
            List<String> selectedGewerk = new ArrayList<>();
            List<String> selectedDeadline = new ArrayList<>();
            List<String> selectedResponsible = new ArrayList<>();
            List<String> selectedCreator = new ArrayList<>();
            List<String> selectedDate = new ArrayList<>();
            List<String> selectedPlan = new ArrayList<>();

            String PREDICATE_PHOTO_TYPE = LocalPhotosRepository.TYPE_ONLINE_PHOTO;
            String PREDICATE_SORTING = "";
            String PREDICATE_DESCRIPTION = "";
            String PREDICATE_DESCRIPTION_SWITCH = "";
            String PREDICATE_KEYWORD = "";
            String PREDICATE_LOCALIZED_SWITCH = "";
            String PREDICATE_KEYWORD_SWITCH = "";
            String PREDICATE_DEADLINE = "";
            String PREDICATE_KEYWORDS = "";
            String PREDICATE_CREATOR = "";
            String PREDICATE_CREATED_DATE = "";

            long date = 0;
            long start_date = 0;
            long end_date = 0;

            String art = "";
            String tag_keyword = "";

            boolean isApplyGewerk = false;

            List<GroupheadingModel> groupadheadingModelList = params[0];
            if (groupheadingModelList != null) {

                for (int i = 0; i < groupheadingModelList.size(); i++) {
                    if (groupheadingModelList.get(i).getType().equalsIgnoreCase(mContext.getResources().getString(R.string.heading_photo_desc))) {
                        if (groupheadingModelList.get(i).getKeyword() != null && !groupheadingModelList.get(i).getKeyword().equals("")) {
                            art = groupheadingModelList.get(i).getKeyword();
//                            PREDICATE_DESCRIPTION = " AND description LIKE '%" + art+"%'" ;
                            PREDICATE_DESCRIPTION = " AND description LIKE '%" + art + "%'";
                        }

                    } else if (groupheadingModelList.get(i).getType().equalsIgnoreCase(mContext.getResources().getString(R.string.heading_photo_number))) {
                        if (groupheadingModelList.get(i).getKeyword() != null && !groupheadingModelList.get(i).getKeyword().equals("")) {
                            tag_keyword = groupheadingModelList.get(i).getKeyword();
                            PREDICATE_KEYWORD = " AND pdphotolocalId  =" + tag_keyword;
                        }

                    } else if (groupheadingModelList.get(i).getType().equalsIgnoreCase(mContext.getResources().getString(R.string.heading_deadline))) {
                        if (groupheadingModelList.get(i).getListChildDataSelected().size() > 0) {
                            selectedDeadline.addAll(groupheadingModelList.get(i).getListChildDataSelected());
                            date = groupheadingModelList.get(i).getStart_date();
                            PREDICATE_DEADLINE = " AND fristdate_df <= " + date;
                        }


                    } else if (groupheadingModelList.get(i).getType().equalsIgnoreCase(mContext.getResources().getString(R.string.heading_photo_creator))) {
                        if (groupheadingModelList.get(i).getListChildDataSelected().size() > 0) {
                            selectedResponsible.addAll(groupheadingModelList.get(i).getListChildDataSelected());
                            String inClause = selectedResponsible.toString();
                            inClause = inClause.replace("[", "(");
                            inClause = inClause.replace("]", ")");
                            PREDICATE_CREATOR = " AND pdUserId in " + inClause;
                        }


                    } else if (groupheadingModelList.get(i).getType().equalsIgnoreCase(mContext.getResources().getString(R.string.heading_photo_plans))) {
                        if (groupheadingModelList.get(i).getListChildDataSelected().size() > 0) {
                            selectedPlan.addAll(groupheadingModelList.get(i).getListChildDataSelected());
                            String inClause = selectedPlan.toString();
                            inClause = inClause.replace("[", "(");
                            inClause = inClause.replace("]", ")");
                            PREDICATE_CREATOR = " AND pdUserId in " + inClause;
//                            "operand": „EQ", "field": "pdplanid", "value": „26"

                        }


                    } else if (groupheadingModelList.get(i).getType().equalsIgnoreCase(mContext.getResources().getString(R.string.heading_photo_switch_decs))) {
                        if (groupheadingModelList.get(i).isSwitchOn()) {

                            PREDICATE_DESCRIPTION_SWITCH = " AND description !='' ";
                        }


                    } else if (groupheadingModelList.get(i).getType().equalsIgnoreCase(mContext.getResources().getString(R.string.heading_photo_switch_keyword))) {
                        if (groupheadingModelList.get(i).isSwitchOn()) {

                            PREDICATE_DESCRIPTION_SWITCH = " AND params != '' ";
                        }


                    } else if (groupheadingModelList.get(i).getType().equalsIgnoreCase(mContext.getResources().getString(R.string.heading_photo_keyword))) {

                        if (groupheadingModelList.get(i).getListChildDataSelected().size() > 0) {
                            selectedCreator.addAll(groupheadingModelList.get(i).getListChildDataSelected());
                            String inClause = selectedCreator.toString();
                            inClause = inClause.replace("[", "(");
                            inClause = inClause.replace("]", ")");
                            PREDICATE_KEYWORDS = " AND pdphotoid in " + inClause;
                        }

                    } else if (groupheadingModelList.get(i).getType().equalsIgnoreCase(mContext.getResources().getString(R.string.heading_localized_photo))) {


                        if (groupheadingModelList.get(i).isSwitchOn()) {
                            PREDICATE_LOCALIZED_SWITCH = " AND plan_id !='' ";
                        }

                    } else if (groupheadingModelList.get(i).getType().equalsIgnoreCase(mContext.getResources().getString(R.string.heading_created_date))) {

//                        if (groupheadingModelList.get(i).getListChildDataSelected().size() > 0) {
//                            selectedDate.addAll(groupheadingModelList.get(i).getListChildDataSelected());
                        start_date = groupheadingModelList.get(i).getStart_date();
                        end_date = groupheadingModelList.get(i).getEnd_date();

                        if (start_date > 0 && end_date > 0) {
                            PREDICATE_CREATED_DATE = " AND created_df >= " + start_date + " AND created_df <= " + end_date;
                        }
//                        }
                    } else if (groupheadingModelList.get(i).getType().equalsIgnoreCase(mContext.getResources().getString(R.string.heading_sorting))) {

                        if (groupheadingModelList.get(i).getListChildDataSelected().size() > 0) {
                            selectedStatus.addAll(groupheadingModelList.get(i).getListChildDataSelected());

//                            String inClause = selectedStatus.toString();
//                            inClause = inClause.replace("[", "(");
//                            inClause = inClause.replace("]", ")");
//                            PREDICATE_SORTING = " AND status in " + inClause;
                            if (selectedStatus.size() > 0 && selectedStatus.get(0).equals("1")) {
                                PREDICATE_SORTING = "  ORDER BY created_df ASC ";
                            } else if (selectedStatus.size() > 0 && selectedStatus.get(0).equals("2")) {
                                PREDICATE_SORTING = "  ORDER BY created_df DESC ";
                            } else if (selectedStatus.size() > 0 && selectedStatus.get(0).equals("3")) {
                                PREDICATE_SORTING = "  ORDER BY runid ASC ";
                            } else if (selectedStatus.size() > 0 && selectedStatus.get(0).equals("4")) {
                                PREDICATE_SORTING = "  ORDER BY runid DESC ";
                            }
                        } else {
                            PREDICATE_SORTING = "  ORDER BY created_df DESC ";
                        }

                    }
                }
            }

            List<OnlinePhotoModel> defectsModelList = new ArrayList<>();
            SimpleSQLiteQuery query = new SimpleSQLiteQuery("SELECT distinct * FROM  OnlinePhotoModel WHERE projectId = ? AND photo_type='" + PREDICATE_PHOTO_TYPE + "'" + PREDICATE_KEYWORD + PREDICATE_DESCRIPTION + PREDICATE_KEYWORDS + PREDICATE_CREATOR + PREDICATE_DEADLINE + PREDICATE_CREATED_DATE + PREDICATE_DESCRIPTION_SWITCH + PREDICATE_KEYWORD_SWITCH + PREDICATE_SORTING,
                    new Object[]{projectID});
            List<OnlinePhotoModel> defectsModels = ProjectsDatabase.getDatabase(PhotosActivity.this).onlinePhotoDao().getFilterListViaQuery(query);

            defectsModelList = defectsModels;

            return defectsModelList;
        }


        @Override
        protected void onPostExecute(List<OnlinePhotoModel> defectsModels) {
            // super.onPostExecute(list);
            if (defectsModels != null) {
                Fragment fragment = photosPagerAdapter.getM2ndFragment();
                ((OnlinePhotosFragment) fragment).updatePhotosResults(defectsModels, true);
            }
        }

    }

    public void UpDatePhotoStatus() {


    }

/*
    private class UpdatePhotoStatusAsyncTask extends AsyncTask<Void, Void, Void> {
        private PhotoDao mAsyncTaskDao;

        UpdatePhotoStatusAsyncTask() {
            ProjectsDatabase projectsDatabase = ProjectsDatabase.getDatabase(getApplication());
            mAsyncTaskDao = projectsDatabase.photoDao();
        }

        @Override
        protected Void doInBackground(final Void... params) {
            List<PhotoModel> unSyncedPhotoList = null;
            if (sharedPrefsManager.getBooleanValue(AppConstantsManager.SYNC_PHOTO_ASC_ORDER, AppConstantsManager.SYNC_PHOTO_ASC_ORDER_DEFAULT_VALUE)) {
                unSyncedPhotoList = mAsyncTaskDao.getUnSyncedPhotoListASC();
            } else {
                unSyncedPhotoList = mAsyncTaskDao.getUnSyncedPhotoListDESC();
            }

            if (unSyncedPhotoList != null) {
                for (int i = 0; i < unSyncedPhotoList.size(); i++) {

                    if (unSyncedPhotoList.get(i).getPhotoUploadStatus().equals(LocalPhotosRepository.UPLOADING_PHOTO)) {

                        PhotoModel photoModel = unSyncedPhotoList.get(i);
                        photoModel.setPhotoUploadStatus(LocalPhotosRepository.UN_SYNC_PHOTO);
                        mAsyncTaskDao.update(photoModel);
                    }
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            if (sharedPrefsManager.getBooleanValue(AppConstantsManager.ALLOW_BACKGROUND_SYNC, false)
                    && (sharedPrefsManager.getBooleanValue(AppConstantsManager.SYNC_PHOTOS_MOBILE_DATA, false)
                    || sharedPrefsManager.getBooleanValue(AppConstantsManager.SYNC_PHOTOS_WLAN, false))) {
                if (ProjectDocuUtilities.isNetworkConnected(PhotosActivity.this) || ProjectNavigator.wlanIsConnected(PhotosActivity.this)) {

                    new Handler().postDelayed(() -> {
                        //your code here
                        Utils.showLogger("PhotoActivity 1805");
                        syncAllPhotos(true);//synching ignore
                    }, 200);
                }
            }
        }
    }
*/


    private class ReterivePhotoObjectAsyncTask extends AsyncTask<String, Void, PhotoModel> {
        private PhotoDao mAsyncTaskDao;
        int clickedPosition = 0;

        ReterivePhotoObjectAsyncTask(int position) {
            ProjectsDatabase projectsDatabase = ProjectsDatabase.getDatabase(PhotosActivity.this);
            mAsyncTaskDao = projectsDatabase.photoDao();
            clickedPosition = position;
        }

        @Override
        protected PhotoModel doInBackground(final String... params) {

            PhotoModel photoModelList = mAsyncTaskDao.getPhotosOBJ(params[0]);

            return photoModelList;
        }

        @Override
        protected void onPostExecute(PhotoModel photoModelList) {
            super.onPostExecute(photoModelList);

            if (photoModelList != null) {
                Fragment fragment = photosPagerAdapter.getM1stFragment();
                if (fragment == null)
                    return;
                LocalPhotosFragment localPhotosFragment = (LocalPhotosFragment) fragment;
                LocalPhotosViewModel localPhotosViewModel = localPhotosFragment.getLocalPhotosViewModel();
                if (localPhotosViewModel == null)
                    return;
                photoModelList.setClickedPosition(clickedPosition);
                localPhotosViewModel.onListItemClick(photoModelList);

            }
        }
    }


    private class DeletedLocalPhotoAsyncTask extends AsyncTask<String, Void, Void> {
        private PhotoDao mAsyncTaskDao;
        int clickedPosition = 0;

        DeletedLocalPhotoAsyncTask() {
            ProjectsDatabase projectsDatabase = ProjectsDatabase.getDatabase(PhotosActivity.this);
            mAsyncTaskDao = projectsDatabase.photoDao();
        }

        @Override
        protected Void doInBackground(final String... params) {

            mAsyncTaskDao.deleteUsingProjectId(projectID);
            return null;
        }

    }

    @Override
    public void onBackPressed() {
        ONlinePhotoRepository oNlinePhotoRepository = new ONlinePhotoRepository(this);
        oNlinePhotoRepository.deleteAllROws();
        super.onBackPressed();
    }




  /*  private void checkCount() {
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                ProjectsDatabase projectsDatabase = ProjectsDatabase.getDatabase(getActivity());
                PhotoDao mAsyncTaskDao = projectsDatabase.photoDao();

                int autoSyncFalseCount =mAsyncTaskDao .userActionUploadinglocalPhotosCount(projectId, LocalPhotosRepository.UPLOADING_PHOTO, LocalPhotosRepository.TYPE_LOCAL_PHOTO).size();

                Utils.showLogger("before count"+autoSyncFalseCount);
            }
        });

        thread.start();
    }*/
}
