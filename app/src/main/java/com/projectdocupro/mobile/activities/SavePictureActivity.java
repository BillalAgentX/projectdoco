package com.projectdocupro.mobile.activities;

import static android.hardware.SensorManager.SENSOR_DELAY_NORMAL;
import static com.projectdocupro.mobile.activities.DefectsActivity.DEFECT_DETAIL_CODE;
import static com.projectdocupro.mobile.activities.DefectsActivity.IS_CREATED_MANGEL_KEY;
import static com.projectdocupro.mobile.fragments.DefectDetailsPhotoFragment.BR_ACTION_UPDATE_DEFECT_PHOTOS;
import static com.projectdocupro.mobile.fragments.DefectDetailsPhotoFragment.BR_KEY_IS_UPLOAD_PHOTOS_AUTO;
import static com.projectdocupro.mobile.fragments.DefectsListFragment.ARG_PROJECT_ID;
import static java.lang.Thread.yield;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.hardware.Camera;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.OrientationEventListener;
import android.view.ScaleGestureDetector;
import android.view.Surface;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.PopupMenu;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.exifinterface.media.ExifInterface;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.gson.Gson;
import com.projectdocupro.mobile.ProjectNavigator;
import com.projectdocupro.mobile.ProjectsDatabase;
import com.projectdocupro.mobile.R;
import com.projectdocupro.mobile.adapters.Item;
import com.projectdocupro.mobile.adapters.MangelMenuAdapter;
import com.projectdocupro.mobile.compass.Compass;
import com.projectdocupro.mobile.dao.DefectsDao;
import com.projectdocupro.mobile.dao.PhotoDao;
import com.projectdocupro.mobile.dao.ReferPointPlansDao;
import com.projectdocupro.mobile.dao.WordDao;
import com.projectdocupro.mobile.dialogs.CalibrateDialog;
import com.projectdocupro.mobile.fragments.AllPlansFragment;
import com.projectdocupro.mobile.fragments.DefectsListFragment;
import com.projectdocupro.mobile.fragments.LocalPhotosFragment;
import com.projectdocupro.mobile.fragments.add_direction.GeoPoint;
import com.projectdocupro.mobile.fragments.add_direction.PhotoAddDirectionMainActivity;
import com.projectdocupro.mobile.fragments.add_direction.ProjectDocuUtilities;
import com.projectdocupro.mobile.interfaces.IUpdatePhotoScale;
import com.projectdocupro.mobile.interfaces.PhotoCaptureCallback;
import com.projectdocupro.mobile.interfaces.RetroApiInterface;
import com.projectdocupro.mobile.managers.AppConstantsManager;
import com.projectdocupro.mobile.managers.RetrofitManager;
import com.projectdocupro.mobile.managers.SharedPrefsManager;
import com.projectdocupro.mobile.models.DefectsModel;
import com.projectdocupro.mobile.models.Pdflawflag;
import com.projectdocupro.mobile.models.PhotoModel;
import com.projectdocupro.mobile.models.PlansModel;
import com.projectdocupro.mobile.models.ReferPointJSONPlanModel;
import com.projectdocupro.mobile.models.WordModel;
import com.projectdocupro.mobile.models.localFilters.ImageId_VS_Input;
import com.projectdocupro.mobile.models.localFilters.WordContentModel;
import com.projectdocupro.mobile.photoview.PhotoView;
import com.projectdocupro.mobile.photoview.ZoomImageView;
import com.projectdocupro.mobile.receivers.WorkerResultReceiver;
import com.projectdocupro.mobile.repos.DefectRepository;
import com.projectdocupro.mobile.repos.DefectTradesRepository;
import com.projectdocupro.mobile.repos.LocalPhotosRepository;
import com.projectdocupro.mobile.repos.PdFlawFlagRepository;
import com.projectdocupro.mobile.service.GPSTracker;
import com.projectdocupro.mobile.service.SyncLocalPhotosService;
import com.projectdocupro.mobile.utility.DrawingViewCameraFocus;
import com.projectdocupro.mobile.utility.OrientationSensorEventListener;
import com.projectdocupro.mobile.utility.RotationHelper;
import com.projectdocupro.mobile.utility.Utils;
import com.projectdocupro.mobile.viewModels.RecentUsedWordsViewModel;
import com.projectdocupro.mobile.viewModels.SavePictureViewModel;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.CountDownLatch;


import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SavePictureActivity extends AppCompatActivity implements PopupMenu.OnMenuItemClickListener, LocationListener, SensorEventListener, PhotoCaptureCallback {

    private MYORIENTATION myorientation;
    private CalibrateDialog myCalibateDialog;

    private void bindView( ) {
        toolbar = findViewById(R.id.toolbar);
        toolbarRotated = findViewById(R.id.toolbar1);
        wordIcon = findViewById(R.id.word);
        commentIcon = findViewById(R.id.comment);
        planIcon = findViewById(R.id.plan);
        defectsIcon = findViewById(R.id.defects);
        recordIcon = findViewById(R.id.record);
        brushIcon = findViewById(R.id.brush);
        wordDoneIcon = findViewById(R.id.word_done);
        commentDoneIcon = findViewById(R.id.comment_done);
        planDoneIcon =  findViewById(R.id.plan_done);
        rl_parent_save_picture =  findViewById(R.id.rl_parent_save_picture);
        defectsDoneIcon =  findViewById(R.id.defects_done);
        recordDoneIcon =  findViewById(R.id.record_done);
        brushDoneIcon =  findViewById(R.id.brush_done);
        captured_image =  findViewById(R.id.captured_image);
        ll_icons =  findViewById(R.id.ll_icons);
        rl_word =  findViewById(R.id.rl_word);
        rl_comment =  findViewById(R.id.rl_comment);
        rl_plan =  findViewById(R.id.rl_plan);
        rl_defects =  findViewById(R.id.rl_defects);
        rl_record =  findViewById(R.id.rl_record);
        rl_brush =  findViewById(R.id.rl_brush);
        simpleList =  findViewById(R.id.simpleListView);
        iv_delete_rotated =  findViewById(R.id.iv_delete_rotated);
        iv_delete =  findViewById(R.id.iv_delete);
        recyclerView =  findViewById(R.id.recent_words_rv);
        cameraViewParent =  findViewById(R.id.cameraViewParent);
        ll_bottom_camera_view_2 =  findViewById(R.id.ll_bottom_camera_view_2);
        ll_icons_2 =  findViewById(R.id.ll_icons_2);
        rlRotatedBackDeleted =  findViewById(R.id.rlRotatedBackDeleted);
        toolbar_3 =  findViewById(R.id.toolbar_3);
        iv_delete_3 =  findViewById(R.id.iv_delete_3);
        rl_word_2 =  findViewById(R.id.rl_word_2);
        word_2 =  findViewById(R.id.word_2);
        word_done_2 =  findViewById(R.id.word_done_2);
        rl_comment_2 =  findViewById(R.id.rl_comment_2);
        comment_2 =  findViewById(R.id.comment_2);
        comment_done_2 =  findViewById(R.id.comment_done_2);
        rl_plan_2 =  findViewById(R.id.rl_plan_2);
        plan_2 =  findViewById(R.id.plan_2);
        plan_done_2 =  findViewById(R.id.plan_done_2);
        rl_defects_2 =  findViewById(R.id.rl_defects_2);
        defects_2 =  findViewById(R.id.defects_2);
        defects_done_2 =  findViewById(R.id.defects_done_2);
        rl_brush_2 =  findViewById(R.id.rl_brush_2);
        brush_2 =  findViewById(R.id.brush_2);
        brush_done_2 =  findViewById(R.id.brush_done_2);
        rl_record_2 =  findViewById(R.id.rl_record_2);
        record_2 =  findViewById(R.id.record_2);
        record_done_2 =  findViewById(R.id.record_done_2);
        mWord =  findViewById(R.id.word);
        mWord2 =  findViewById(R.id.word_2);
        mDefects2 =  findViewById(R.id.defects_2);
        mComment =  findViewById(R.id.comment);
        mComment2 =  findViewById(R.id.comment_2);
        mPlan =  findViewById(R.id.plan);
        mPlan2 =  findViewById(R.id.plan_2);
        mDefects =  findViewById(R.id.defects);
        mRecord =  findViewById(R.id.record);
        mRecord2 =  findViewById(R.id.record_2);
        mBrush =  findViewById(R.id.brush);
        mBrush2 =  findViewById(R.id.brush_2);
        mWord.setOnClickListener(v -> {
            onWordClick();
        });
        mWord2.setOnClickListener(v -> {
            onWordClick2();
        });
        mWord.setOnLongClickListener(v -> {
            onWordLongClick();
            return true;
        });
        mDefects2.setOnLongClickListener(v -> {
            onLongClickWord2();
            return true;
        });
        mComment.setOnClickListener(v -> {
            onCommentClick();
        });
        mComment2.setOnClickListener(v -> {
            onCommentClick2();
        });
        mPlan.setOnClickListener(v -> {
            onPlanClick();
        });
        mPlan2.setOnClickListener(v -> {
            onPlanClick2();
        });
        mDefects.setOnClickListener(v -> {
            onDefectClick(v);
        });
        mDefects2.setOnClickListener(v -> {
            onDefectClick2(v);
        });
        mRecord.setOnClickListener(v -> {
            onRecordClick();
        });
        mRecord2.setOnClickListener(v -> {
            onRecordClick2();
        });
        mBrush.setOnClickListener(v -> {
            onBrushClick();
        });
        mBrush2.setOnClickListener(v -> {
            onBrushClick2();
        });
    }

    enum MYORIENTATION {
        PORTRAIT, LANDSCAPE, REVERSE_LANDSCAPE
    }

    public static final int KEY_ON_ACTIVITY_RESULT = 7890;

    public static final int DELAY_MILLISECONDS = 100;

    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 2323;
    private static final int RESULT_LOAD_IMAGE = 9900;
    public static final String PLAN_ATTACH_TO_PHOTO_KEY = "plan_attached";
    public static final String AUDIO_ATTACH_TO_PHOTO_KEY = "audio_attached";
    public static final String SKETCH_ATTACH_TO_PHOTO_KEY = "sketh_attached";
    public static final String PLAN_ID_KEY = "planId";
    public static final String FINISH_PLAN_SCREEN_KEY = "finish_plan_screen";
    public static final String BR_ACTION_UPDATE_PHOTO_AND_PLAN_LOC = "updateFlawFlagAdPhoto";
    private static final String TAG = SavePictureActivity.class.getCanonicalName();

    SavePictureViewModel savePictureViewModel;
    private static final int TAKE_PICTURE = 1;
    private static final int STORAGE_RQUEST_CODE = 23;
    private boolean isBackCamera;
    CountDownLatch createDefectLatch = null;
    boolean isIgnoreCompassUpdate = false;


    private ImageView toolbar;
    private ImageView toolbarRotated;

    private ImageView wordIcon;

    private ImageView commentIcon;

    private ImageView planIcon;

    private ImageView defectsIcon;

    private ImageView recordIcon;

    private ImageView brushIcon;

    private ImageView wordDoneIcon;

    private ImageView commentDoneIcon;

    private ImageView planDoneIcon;

    private RelativeLayout rl_parent_save_picture;

    private ImageView defectsDoneIcon;

    private ImageView recordDoneIcon;

    private ImageView brushDoneIcon;

    private ZoomImageView captured_image;

    private LinearLayout ll_icons;

    private RelativeLayout rl_word;
    private RelativeLayout rl_comment;
    private RelativeLayout rl_plan;
    private RelativeLayout rl_defects;
    private RelativeLayout rl_record;
    private RelativeLayout rl_brush;

    private ListView simpleList;
    private ImageView iv_delete_rotated;
    private ImageView iv_delete;
    private RecyclerView recyclerView;
    private RelativeLayout cameraViewParent;

    private FrameLayout ll_bottom_camera_view_2;

    private LinearLayout ll_icons_2;
    private RelativeLayout rlRotatedBackDeleted;

    // duplicate layout

    private ImageView toolbar_3;
    private ImageView iv_delete_3;

    private RelativeLayout rl_word_2;
    private ImageView word_2;
    private ImageView word_done_2;

    private RelativeLayout rl_comment_2;
    private ImageView comment_2;
    private ImageView comment_done_2;

    private RelativeLayout rl_plan_2;
    private ImageView plan_2;
    private ImageView plan_done_2;

    private RelativeLayout rl_defects_2;
    private ImageView defects_2;
    private ImageView defects_done_2;

    private RelativeLayout rl_brush_2;
    private ImageView brush_2;
    private ImageView brush_done_2;

    private RelativeLayout rl_record_2;
    private ImageView record_2;
    private ImageView record_done_2;

    // duplicate layout


    private boolean isViewMode;
    private boolean isFromOnlinePhotos;
    private String screenTitle;
    private int column_index;
    private String imagePath;
    private String gallaryOrignalPath;
    private boolean isPhotoFromGallary;
    private String flawId = "";

    private static String POPUP_CONSTANT = "mPopup";
    private static String POPUP_FORCE_SHOW_ICON = "setForceShowIcon";


    ArrayList<Item> animalList = new ArrayList<>();
    private PdFlawFlagRepository pdFlawFlagRepository;
    private Pdflawflag flawFlagObj;
    SharedPrefsManager sharedPrefsManager;


    RecentUsedWordsViewModel recentUsedWordsViewModel;
    private String photoId;
    private BroadcastReceiver updateFlawStatus, brAddPlanListing;
    private List<ReferPointJSONPlanModel> referPointList;
    private Location gpsLocation;
    private float compassDegrees;
    private SensorManager sensorManager;
    private LocationManager locationManager;
    private LocationProvider low;
    private LocationProvider high;
    private PlansModel plansModelObj;
    private boolean isLocationGetFisrtTime;
    private GPSTracker gpsTracker;
    private String projectID;
    private DefectTradesRepository defectTradesRepository;
    private ProjectDocuCameraPreview preview;
    FrameLayout cameraPreview;
    ImageView iv_camera, iv_flash;
    TextView tv_close_camera;
    RelativeLayout pb_loader;
    RelativeLayout rl_photo_preview_section;
    FrameLayout ll_bottom_camera_view;
    private ScaleGestureDetector mScaleGestureDetector;
    private float mScaleFactor = 12.0f;
    boolean isPhotoUpdate;
    private int position = 0;
    private boolean gpsSwitchedOn;
    private BroadcastReceiver updateFlawFlagStatus;
    private String planId;
    private BroadcastReceiver updateFlawFlag;
    public boolean isImageFromGallary;
    public static final String BR_ACTION_ADD_PLAN_LIST = "br_add_plan_list";
    private int deviceOrientation = 0;
    private int rotateIcons = 0;
    private int tagOrientation = 0;
    private int oldOrientation = -666;
    private long lastRotationTime = System.currentTimeMillis();

    private int screenHeight;
    private int screenWidth;
    private Dialog customDialog;
    private View productsView;
    private OrientationEventListener mOrEventListener;
    boolean isLandScape = false;
    private DrawingViewCameraFocus drawingViewCameraFocus;
    private boolean is_90_ori_active;
    private boolean isCapturePhoto;
    private SensorManager mSensorManager;
    private OrientationSensorEventListener mEventListener;
    private boolean isFromLocalPhotos;
    private Compass compass;
    private int current_orientation = 0;
    long local_flaw_id = 0;
    private int mRotation;
    private RotationHelper mRotationHelper;
    List<View> iconsListBeforeTakingPhoto = new ArrayList<>();
    List<View> iconsListAfterTakingPhoto = new ArrayList<>();
    private boolean isPreviewMode = false;
    private long onlineModelID;
    private WordContentModel wordContentModel;
    private ImageId_VS_Input imgIdVsInput;
    private boolean isClockedValuesAdjusted = false;
    private TextView compassAngleView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().requestFeature(Window.FEATURE_ACTION_BAR_OVERLAY);
        Utils.showLogger("SavePictureActivity");
//        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
        //getSupportActionBar().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        // getSupportActionBar().setElevation(0);
        getSupportActionBar();

        mSensorManager = (SensorManager) this.getSystemService(SENSOR_SERVICE);

        mRotationHelper = new RotationHelper();

        setContentView(R.layout.activity_save_picture);
        bindView();
        cameraPreview = findViewById(R.id.fl_camera_area);
        pb_loader = findViewById(R.id.pb_loader);
        rl_photo_preview_section = findViewById(R.id.rl_photo_preview_section);
        ll_bottom_camera_view = findViewById(R.id.ll_bottom_camera_view);
        iv_camera = findViewById(R.id.iv_camera);
        iv_flash = findViewById(R.id.iv_flash);
        tv_close_camera = findViewById(R.id.tv_cancel);
        compassAngleView = findViewById(R.id.compass_status);
        compassAngleView.setVisibility(View.GONE);

//        preview = new ProjectDocuCameraPreview(SavePictureActivity.this, SavePictureActivity.this);
//        cameraPreview.addView(preview);

        setupListOfIconsBeforeTakingPhoto();
        setupListOfIconsAfterTakingPhoto();

        sharedPrefsManager = new SharedPrefsManager(this);
        isBackCamera = getIntent().getBooleanExtra("isBackCamera", false);
        isPhotoFromGallary = getIntent().getBooleanExtra("isPhotoFromGallary", false);
        isViewMode = getIntent().getBooleanExtra("isViewMode", false);
        isFromOnlinePhotos = getIntent().getBooleanExtra("isFromOnlinePhotos", false);
        screenTitle = getIntent().getStringExtra("screenTitle");
        flawId = getIntent().getStringExtra("flawId");
        savePictureViewModel = ViewModelProviders.of(this).get(SavePictureViewModel.class);
        projectID = getIntent().getStringExtra("projectId");

        defectTradesRepository = new DefectTradesRepository(getApplication(), getIntent().getStringExtra("projectId"));

        savePictureViewModel.InitRepo(getIntent().getStringExtra("projectId"));
        savePictureViewModel.setProjectId(getIntent().getStringExtra("projectId"));

        Utils.showLogger("setExistingPhotoModel>>" + getIntent().getLongExtra("photoId", 0));

        onlineModelID = getIntent().getLongExtra("photoId", 0);

        savePictureViewModel.setExistingPhotoModel(getIntent().getLongExtra("photoId", 0));
        savePictureViewModel.getmRepository().allPhotosOfProject(this, getIntent().getStringExtra("projectId"));

        photoId = getIntent().getLongExtra("photoId", 0) + "";
        position = getIntent().getIntExtra("position", 0);
        isFromLocalPhotos = getIntent().getBooleanExtra("isFromLocalPhotos", false);

        recentUsedWordsViewModel = ViewModelProviders.of(this).get(RecentUsedWordsViewModel.class);
        recentUsedWordsViewModel.setPhotoId(getIntent().getLongExtra("photoId", 0L));
        recentUsedWordsViewModel.InitRepo(getIntent().getStringExtra("projectId"), String.valueOf(getIntent().getLongExtra("photoId", 0L)));

        initialize();

        applyObservers();

        drawingViewCameraFocus = new DrawingViewCameraFocus(this);

        recentUsedWordsViewModel.isOpneFieldKeywordWordSelected.observe(this, new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean aBoolean) {
                if (aBoolean && recentUsedWordsViewModel.wordModelObj != null) {
                    showOpenFieldKeywordDialog(SavePictureActivity.this, recentUsedWordsViewModel.wordModelObj);
                }

            }
        });
        sharedPrefsManager.setStringValue(AppConstantsManager.SELECTED_FLASH_MODE, Camera.Parameters.FLASH_MODE_OFF);

        new OrientationEventListener(this) {
            @Override
            public void onOrientationChanged(int orientation) {
//                Log.d(TAG, "Orientation New: " + orientation);


                int currentOrientation = getWindowManager().getDefaultDisplay().getRotation();

                switch (currentOrientation) {
                    case 0:
                        //. SCREEN_ORIENTATION_PORTRAIT
                        //myorientation = MYORIENTATION.PORTRAIT;
                        break;
                    //----------------------------------------
                    case 2:
                        //. SCREEN_ORIENTATION_REVERSE_PORTRAIT

                        //myorientation = MYORIENTATION.PORTRAIT;
                        break;
                    //----------------------------------------
                    case 1:
                        //. SCREEN_ORIENTATION_LANDSCAPE

                        // myorientation = MYORIENTATION.LANDSCAPE;
                        break;
                    //----------------------------------------
                    case 3:
                        //. SCREEN_ORIENTATION_REVERSE_LANDSCAPE
                        myorientation = MYORIENTATION.REVERSE_LANDSCAPE;
                        break;
                    //----------------------------------------
                }

//                deviceOrientation = orientation;
            }
        }.enable();

        recyclerView.setVisibility(View.GONE);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recentUsedWordsViewModel.getWordsList().observe(this, new Observer<List<WordModel>>() {
            @Override
            public void onChanged(List<WordModel> list) {


                Utils.showLogger("changesObserved");


                if (list.size() > 0 && !isViewMode) {
                    recyclerView.setVisibility(View.VISIBLE);
                    recentUsedWordsViewModel.initAdapter(list);
                    recyclerView.setAdapter(recentUsedWordsViewModel.getAdapter());
                    recentUsedWordsViewModel.getWordsList().removeObserver(this);
                } else {
                    recyclerView.setVisibility(View.GONE);

                }
            }
        });

        recentUsedWordsViewModel.isWordSelected.observe(this, aBoolean -> {

            if (aBoolean) {
//                if (photoModel != null) {
                new updatePhotoAsyncTask(savePictureViewModel.getPhotoModel().getPdphotolocalId(), savePictureViewModel.getProjectId(), savePictureViewModel.getPhotoModel()).execute();

//                }

            }

        });

//        toolbar.setNavigationOnClickListener(view -> {
//            onBackPressed();
//        });
//
//        if (screenTitle != null && !screenTitle.equalsIgnoreCase("")) {
//            toolbar.setTitle(screenTitle);
//        }
//        toolbar.setNavigationOnClickListener(v -> onBackPressed());

//        if (sharedPrefsManager.getBooleanValue(AppConstantsManager.ACTIVATE_GPS, false)
//                && sharedPrefsManager.getBooleanValue(AppConstantsManager.FIND_POSITION_IF_GEO_REFERENCED_PLAN, false)) {
//
        if (sharedPrefsManager.getBooleanValue(AppConstantsManager.ACTIVATE_GPS, true)) {
            activateGPS();
        } else {
            deactivateGPS();
        }
        if (sharedPrefsManager.getBooleanValue(AppConstantsManager.VIEW_DIRECTION_THROUGH_COMPASS, false)) {
            activateCompass();
        } else {
            deactivateCompass();
        }

        if (isPhotoFromGallary) {
            hideCameraView();
            getImageFromAlbum();
        } else if (!isBackCamera) {
            takePhoto();
        } else {
            hideCameraView();

            savePictureViewModel.setImagePath(getIntent().getStringExtra("path"));
            if (getIntent().getStringExtra("path") != null && !getIntent().getStringExtra("path").equals("")) {

                File photo = new File(savePictureViewModel.getImagePath());
                savePictureViewModel.setImageUri(FileProvider.getUriForFile(this, getApplicationContext().getPackageName() + ".fileprovider", photo));
                try {
                    BitmapFactory.Options bmOptions = new BitmapFactory.Options();
                    // bmOptions.inJustDecodeBounds = true;
                    Bitmap bitmap = BitmapFactory.decodeFile(savePictureViewModel.getImagePath(), bmOptions);



                    bitmap = getRotatedBitmap(bitmap);
                    captured_image.setScale(1.0f);
                    captured_image.setImageBitmap(bitmap);
                    Utils.showLogger("setPhotoModel");
                    savePictureViewModel.setPhotoModel((PhotoModel) getIntent().getSerializableExtra("photoModel"));
                    savePictureViewModel.getPhotoModel().setPdphotolocalId(getIntent().getLongExtra("photoId", 0L));
                    //                setDoneIcons();

                    if (wordDoneIcon != null) {
                        if (savePictureViewModel.getPhotoModel().isWordAdded()) {
                            wordDoneIcon.setVisibility(View.VISIBLE);
                            word_done_2.setVisibility(View.VISIBLE);
                        } else {
                            wordDoneIcon.setVisibility(View.INVISIBLE);
                            word_done_2.setVisibility(View.INVISIBLE);
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

        }

        if (isViewMode) {
            ll_icons.setVisibility(View.GONE);
//            ll_tick.setVisibility(View.GONE);
            recyclerView.setVisibility(View.GONE);
            ll_bottom_camera_view.setVisibility(View.GONE);
            if (photoId != null && !photoId.equals("") && isFromOnlinePhotos)
                callGetPlanImageAPI(this, photoId);
        } else {
            ll_bottom_camera_view.setVisibility(View.VISIBLE);
            showPreviewMode();
            if (!isBackCamera) ll_icons.setVisibility(View.GONE);
//            ll_tick.setVisibility(View.VISIBLE);

            if (!sharedPrefsManager.getShowRecentWordScreenBooleanValue()) {
                recyclerView.setVisibility(View.GONE);
            } else {
                recyclerView.setVisibility(View.VISIBLE);
            }
        }

//        if (sharedPrefsManager.getBooleanValue(AppConstantsManager.ACTIVATE_GPS, false) && !sharedPrefsManager.getLastUsedPlanId(this).equals(""))
//            new RetrievePlansReferPointAsyncTask(this, getIntent().getStringExtra("projectId"), sharedPrefsManager.getLastUsedPlanId(this)).execute();
        addEvent();

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                captured_image.setScale(1.0f);
            }
        }, 50);

//        mEventListener = new OrientationSensorEventListener() {
//            public void onReactToOrientationChange(){
//                String orientStr="Current Angles: ";
//                for (float angle: mValuesOrientation) {
//                    orientStr += String.format("%6.2f,	", angle);
//                }
//                Log.d("SaveSensor",orientStr +" compass angle "+compassDegrees);
//
//            }
//        };
    }

/*    private void adjustClockedValue(List<WordModel> list) {
        Utils.showLogger("adjustClockedValue");
        isClockedValuesAdjusted = true;

        for(WordModel wordModel:list){

            if(wordModel.getType().equals("1"))
            {
                if(wordModel.isClocked())
                {
                    wordModel.addOrUpdateInputField(photoId,wordModel.getValue());
                }
            }
        }

    }*/

    private void applyObservers() {
        savePictureViewModel.getNewPhotoInsert().observe(SavePictureActivity.this, new Observer<Long>() {
            @Override
            public void onChanged(Long aLong) {
                photoId = aLong.toString();
                recentUsedWordsViewModel.setPhotoId(aLong);
            }
        });
    }

    //region initialize
    private void initialize() {
    }
    //endregion

    public void makePhoto() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE}, STORAGE_RQUEST_CODE);
                return;
            }
        }
        if (preview == null) {
            preview = new ProjectDocuCameraPreview(SavePictureActivity.this, SavePictureActivity.this);
            if (cameraPreview != null) {
                cameraPreview.removeAllViews();
                cameraPreview.addView(preview);
                cameraPreview.addView(drawingViewCameraFocus);
                preview.setDrawingView(drawingViewCameraFocus);

            }
        }

        if (preview.app_is_in_capture_mode == true) {
            if (preview != null) {
                preview.app_is_in_capture_mode = false;
                preview.app_is_saving_photo = true;

                System.gc();

                preview.takePicturePressed();

            }
        } else {
            if (preview.app_is_saving_photo == false) {
                System.gc();

                preview.startCameraPreview();
                preview.zoomLocked = false;
//                cameraPreview.removeAllViews();


            }
        }
    }

    private class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {
        @Override
        public boolean onScale(ScaleGestureDetector scaleGestureDetector) {
            mScaleFactor *= scaleGestureDetector.getScaleFactor();
            mScaleFactor = Math.max(0.1f, Math.min(mScaleFactor, 10.0f));
            captured_image.setScaleX(mScaleFactor);
            captured_image.setScaleY(mScaleFactor);

            return true;
        }
    }

    //region addEvent
    private void addEvent() {

        iv_camera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                rl_parent_save_picture.setVisibility(View.VISIBLE);
                showCameraView();
                hidePreviewMode();
                makePhoto();
            }
        });
        tv_close_camera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                hideCameraView();
//                showPreviewMode();
                onBackPressed();

            }
        });
        iv_flash.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                preview.setFlashStatus(iv_flash);
            }
        });

        toolbar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Utils.showLogger("toolbar.setOnClickListener");
                onBackPressed();
            }
        });

        toolbar_3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        toolbarRotated.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        iv_delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deleteImage();
//                showCustomDialog(SavePictureActivity.this, "projectdocu", getResources().getString(R.string.image_delete_msg), 2, 0);

            }
        });

        iv_delete_3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deleteImage();
//                showCustomDialog(SavePictureActivity.this, "projectdocu", getResources().getString(R.string.image_delete_msg), 2, 0);
            }
        });


        iv_delete_rotated.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deleteImage();
//                showCustomDialog(SavePictureActivity.this, "projectdocu", getResources().getString(R.string.image_delete_msg), 2, 0);

            }
        });


        updateFlawStatus = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent.getExtras().get("local_flaw_id") != null) {
                    flawId = intent.getExtras().get("local_flaw_id") + "";
                }
                if (savePictureViewModel.getPhotoModel() != null) {
                    savePictureViewModel.getPhotoModel().setDefectAdded(true);
                    savePictureViewModel.getPhotoModel().setLocal_flaw_id(flawId);
                    Utils.showLogger("iden842");
                    savePictureViewModel.getPhotoModel().setPhotoSynced(false);//Done update flaw status
                    savePictureViewModel.getPhotoModel().setPhotoUploadStatus(LocalPhotosRepository.UN_SYNC_PHOTO);
                    isPhotoUpdate = true;
                    flawId = "";
                    setDoneIcons();
                }

            }

        };


        brAddPlanListing = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent.getAction().equals(BR_ACTION_ADD_PLAN_LIST)) {
                    boolean ignoreLastPlanLoading = intent.getBooleanExtra(PlansActivity.IGNORE_LOADING_LAST_PLAN, false);
                    Utils.showLogger("ignoreLastPlanLoading" + ignoreLastPlanLoading + ":");
                    startActivityForResult(new Intent(SavePictureActivity.this, PlansActivity.class).putExtra("projectId", savePictureViewModel.getProjectId()).putExtra("photoId", savePictureViewModel.getPhotoModel().getPdphotolocalId()).putExtra(PlansActivity.IGNORE_LOADING_LAST_PLAN, ignoreLastPlanLoading)
//                            .putExtra(AllPlansFragment.ARG_PLAN_ID, savePictureViewModel.getPhotoModel().getPlan_id())
                            .putExtra("fromPhoto", true), 102);
                }
            }
        };
/*        captured_image.getAttacher().setiUpdatePhotoScale(new IUpdatePhotoScale() {
            @Override
            public void updatePhotoScale(float scale) {
                captured_image.setScale(scale);
            }
        });*/

/*
        capturedImage.setOnDoubleTapListener(new GestureDetector.OnDoubleTapListener() {
            @Override
            public boolean onSingleTapConfirmed(MotionEvent e) {
//                capturedImage.getAttacher().getScale();
                Log.d(TAG, "onSingleTapConfirmed: "+"called");
                return false;
            }

            @Override
            public boolean onDoubleTap(MotionEvent e) {
                Log.d(TAG, "onDoubleTap: "+"called");
                return false;
            }

            @Override
            public boolean onDoubleTapEvent(MotionEvent e) {
                Log.d(TAG, "onDoubleTapEvent: "+"called");
                return false;
            }
        });
*/
    }
    //endregion

    //region loadMenu
    private void loadMenu() {
        if (animalList != null) animalList.clear();


        animalList.add(new Item(getString(R.string.add_mangel), R.drawable.plus_round_icon));
        animalList.add(new Item(getString(R.string.mangel_listing), R.drawable.flaw_listing));
        animalList.add(new Item(getString(R.string.quick_mangel), R.drawable.flash));

        MangelMenuAdapter myAdapter = new MangelMenuAdapter(this, R.layout.flaw_menu_item_view, animalList);
        simpleList.setAdapter(myAdapter);

        simpleList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                boolean isCreateMangel = false;
                if (position == 0) {
                    isCreateMangel = true;
                    new CreateLocalDefectAsyncTask(false).execute();
                } else if (position == 1) {
                    Intent intent = new Intent(SavePictureActivity.this, DefectsActivity.class);
                    intent.putExtra("photoId", savePictureViewModel.getPhotoModel().getPdphotolocalId() + "");
                    intent.putExtra("isCreateMangel", false);
                    intent.putExtra("projectId", savePictureViewModel.getProjectId());
                    startActivityForResult(intent, 103);
                } else {
                    //asdfasdfa
                    new CreateLocalDefectAsyncTask(true).execute();
                    defectsDoneIcon.setVisibility(View.VISIBLE);
                }

                simpleList.setVisibility(View.GONE);
            }
        });

        if (simpleList.getVisibility() == View.VISIBLE) {
            simpleList.setVisibility(View.GONE);
        } else showMenu();


    }
    //endregion

    private void showMenu() {
        simpleList.setVisibility(View.VISIBLE);
    }

    private void hideMenu() {
        simpleList.setVisibility(View.GONE);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
//        MenuInflater inflater = getMenuInflater();

        super.onCreateOptionsMenu(menu);
        if (savePictureViewModel.getPhotoModel() != null && !savePictureViewModel.getPhotoModel().getPhotoUploadStatus().equalsIgnoreCase(LocalPhotosRepository.UPLOADING_PHOTO))
            if (!isViewMode) {
                getMenuInflater().inflate(R.menu.delete_action_menu, menu);
            }


        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.delete_action:
                // Set the text color to red

                showCustomDialog(SavePictureActivity.this, getResources().getString(R.string.custom_dialog_title), getResources().getString(R.string.image_delete_msg), 2, 0);


                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void showCustomDialog(final Activity act, String title, String msgToShow, Integer noOfButtons, Integer flag) {
        //customDialog = new Dialog(act, R.style.customDialogTheme);

        Utils.showLogger("imageid>>" + photoId);

        customDialog = new Dialog(act, R.style.MyDialogTheme);
//        customDialog.setContentView(R.layout.custom_dialog_message_material_notification);
        customDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
//        customDialog.setContentView(R.layout.custom_dialog_message_material);

        LayoutInflater inflater = LayoutInflater.from(this);
        productsView = inflater.inflate(R.layout.custom_dialog_message_material_notification, null);
        customDialog.setContentView(productsView);
        if (isLandScape) {
            if (is_90_ori_active) {
                is_90_ori_active = false;

                productsView.setRotation(270);
                FrameLayout.LayoutParams lpparent = new FrameLayout.LayoutParams(1200, 1350);
                lpparent.gravity = Gravity.CENTER;
                lpparent.leftMargin = 300;
                lpparent.topMargin = 0;

                productsView.setLayoutParams(lpparent);

            } else {
                productsView.setRotation(90);
                FrameLayout.LayoutParams lpparent = new FrameLayout.LayoutParams(1100, 1200);
                lpparent.gravity = Gravity.CENTER;
                lpparent.rightMargin = 350;
                productsView.setLayoutParams(lpparent);
            }
        } else {
            productsView.setRotation(0);
        }

        if (flag == 2) {
            customDialog.setCancelable(false);

        }
        customDialog.setCanceledOnTouchOutside(false);
        WindowManager.LayoutParams params = customDialog.getWindow().getAttributes();
        params.y = 10;

        // set the custom dialog components - text, image and button
        TextView titleTxt = (TextView) customDialog.findViewById(R.id.customDialog_titleText);
        if (!title.equals("")) titleTxt.setText(title);
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

//                    Fragment fragment = defectDetailsPagerAdapter.getM1stFragment();
//                    if(fragment!=null){
//                        ((DefectDetailsDatesFragment) fragment).saveDefect();
//
//                    }
                if (noOfButtons != 1) {
                    new DeletePhotoAsyncTask().execute(savePictureViewModel.getPhotoModel());
                }


//                Intent intent = new Intent(LocalPhotosFragment.DELETE_PHOTO);
//                intent.putExtra(LocalPhotosFragment.PHOTO_MODEL, savePictureViewModel.getPhotoModel());
//                sendBroadcast(intent);

                customDialog.dismiss();

                if (noOfButtons != 1) {
                    // finish();//Do not delete items delte after removing from db
                }


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

    //region setDoneIcons
    private void setDoneIcons() {

        if (wordDoneIcon != null && savePictureViewModel.getPhotoModel().isWordAdded()) {
            wordDoneIcon.setVisibility(View.VISIBLE);
            word_done_2.setVisibility(View.VISIBLE);
        } else {
            wordDoneIcon.setVisibility(View.INVISIBLE);
            word_done_2.setVisibility(View.INVISIBLE);
        }
        if (savePictureViewModel.getPhotoModel() != null && savePictureViewModel.getPhotoModel().getDescription() != null && !savePictureViewModel.getPhotoModel().getDescription().isEmpty()) {
            commentDoneIcon.setVisibility(View.VISIBLE);
            comment_done_2.setVisibility(View.VISIBLE);
        } else {
            commentDoneIcon.setVisibility(View.INVISIBLE);
            comment_done_2.setVisibility(View.INVISIBLE);
        }

        if (savePictureViewModel.getPhotoModel().isDefectAdded()) {
            defectsDoneIcon.setVisibility(View.VISIBLE);
            defects_done_2.setVisibility(View.VISIBLE);
        } else {
            defectsDoneIcon.setVisibility(View.INVISIBLE);
            defects_done_2.setVisibility(View.INVISIBLE);
        }

        if (savePictureViewModel.getPhotoModel().isPlanAdded()) {
            planDoneIcon.setVisibility(View.VISIBLE);
            plan_done_2.setVisibility(View.VISIBLE);
        } else {
            planDoneIcon.setVisibility(View.INVISIBLE);
            plan_done_2.setVisibility(View.INVISIBLE);
        }

        if (savePictureViewModel.getPhotoModel().isRecordingAdded()) {
            recordDoneIcon.setVisibility(View.VISIBLE);
            record_done_2.setVisibility(View.VISIBLE);
        } else {
            recordDoneIcon.setVisibility(View.INVISIBLE);
            record_done_2.setVisibility(View.INVISIBLE);
        }

        if (savePictureViewModel.getPhotoModel().isBrushImageAdded()) {
            brushDoneIcon.setVisibility(View.VISIBLE);
            brush_done_2.setVisibility(View.VISIBLE);
        } else {
            brushDoneIcon.setVisibility(View.INVISIBLE);
            brush_done_2.setVisibility(View.INVISIBLE);
        }

        Intent intentt = new Intent(SyncLocalPhotosService.BR_ACTION_UPDATE_PROJECT_LIST);
        intentt.putExtra(SyncLocalPhotosService.PROJECT_ID, projectID);
        sendBroadcast(intentt);

    }
    //endregion

    private void getImageFromAlbum() {
        try {
            Intent i = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(i, RESULT_LOAD_IMAGE);
        } catch (Exception exp) {
            Log.i("Error", exp.toString());
        }
    }

    public void takePhoto() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE}, STORAGE_RQUEST_CODE);
                return;
            }
        }
        preview = new ProjectDocuCameraPreview(SavePictureActivity.this, SavePictureActivity.this);
        if (cameraPreview != null) {
            cameraPreview.removeAllViews();
            cameraPreview.addView(preview);
            cameraPreview.addView(drawingViewCameraFocus);
            preview.setDrawingView(drawingViewCameraFocus);
        }
//        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
//        intent.putExtra("android.intent.extra.quickCapture", true);
//        File dir = this.getExternalFilesDir("/projectDocu/project_" + savePictureViewModel.getProjectId());
//        if (dir == null) {
//            dir = this.getFilesDir();
//        }
////        File dir = new File(Environment.getExternalStorageDirectory() +"/projectDocu/project_"+savePictureViewModel.getProjectId());
//        if (!dir.isDirectory()) {
//            dir.mkdirs();
//        }
//        File photo = new File(dir, "/Image_" + new Date().getTime() + ".jpg");
//        intent.putExtra(MediaStore.EXTRA_OUTPUT, FileProvider.getUriForFile(this, getApplicationContext().getPackageName() + ".fileprovider", photo));
//        savePictureViewModel.setImageUri(FileProvider.getUriForFile(this, getApplicationContext().getPackageName() + ".fileprovider", photo));
//        savePictureViewModel.setImagePath(photo.getAbsolutePath());
//        startActivityForResult(intent, TAKE_PICTURE);


        showCameraView();
        hidePreviewMode();
    }

    //region onWordClick
    private void onWordClick() {


//        showCustomDialogTemp(SavePictureActivity.this, "Project Docu", "Compass value "+  compassDegrees+" orientaion "+deviceOrientation, 2, 0);

        startActivityForResult(new Intent(this, WordActivity.class).putExtra("projectId", savePictureViewModel.getProjectId()).putExtra("photoId", savePictureViewModel.getPhotoModel().getPdphotolocalId()), 104);
    }

    private void onWordClick2() {
        startActivityForResult(new Intent(this, WordActivity.class).putExtra("projectId", savePictureViewModel.getProjectId()).putExtra("photoId", savePictureViewModel.getPhotoModel().getPdphotolocalId()), 104);
    }
    //endregion

    private void onWordLongClick() {
        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
            @Override
            public void run() {
                if (recyclerView.getVisibility() == ViewGroup.VISIBLE) {
                    recyclerView.setVisibility(View.GONE);
                } else {
                    recyclerView.setVisibility(View.VISIBLE);
                }

            }
        }, 2000);




       /* if (!sharedPrefsManager.getShowRecentWordScreenBooleanValue()) {
            sharedPrefsManager.setShowRecentWordScreenBooleanValue(true);
            recyclerView.setVisibility(View.VISIBLE);
        } else {
            sharedPrefsManager.setShowRecentWordScreenBooleanValue(false);
            recyclerView.setVisibility(View.GONE);
        }
*/
//        Intent intent = new Intent(this, RecentUsedWordsActivity.class);
//        intent.putExtra("path", savePictureViewModel.getImagePath());
//        intent.putExtra("projectId", savePictureViewModel.getProjectId());
//        intent.putExtra("photoId", savePictureViewModel.getPhotoModel().getPdphotolocalId());
//        startActivityForResult(intent, 1122);
    }

    private void onLongClickWord2() {
        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
            @Override
            public void run() {
                if (recyclerView.getVisibility() == ViewGroup.VISIBLE) {
                    recyclerView.setVisibility(View.GONE);
                } else {
                    recyclerView.setVisibility(View.VISIBLE);
                }

            }
        }, 2000);
    }


    //region onCommentClick


    private void onCommentClick() {
        startActivityForResult(new Intent(this, AddCommentActivity.class).putExtra("photoId", savePictureViewModel.getPhotoModel().getPdphotolocalId()).putExtra("comments", savePictureViewModel.getPhotoModel().getDescription()), 101);
    }

    private void onCommentClick2() {
        startActivityForResult(new Intent(this, AddCommentActivity.class).putExtra("photoId", savePictureViewModel.getPhotoModel().getPdphotolocalId()).putExtra("comments", savePictureViewModel.getPhotoModel().getDescription()), 101);
    }
    //endregion

    //region onPlanClick

    private void onPlanClick() {


        if (savePictureViewModel.getPhotoModel().getPlan_id() != null && !savePictureViewModel.getPhotoModel().getPlan_id().equals("")) {

            new Handler().postDelayed(() -> {
                //your code here
                sharedPrefsManager.setBooleanValue(AppConstantsManager.ACTIVATE_GPS_MANUAL_STATE, false);

                new GetFlawFlagUsingPhotoId().execute();
            }, 200);

        } else {
            startActivityForResult(new Intent(this, PlansActivity.class).putExtra("projectId", savePictureViewModel.getProjectId()).putExtra("photoId", savePictureViewModel.getPhotoModel().getPdphotolocalId()).putExtra(AllPlansFragment.ARG_PLAN_ID, savePictureViewModel.getPhotoModel().getPlan_id()).putExtra("fromPhoto", true), 102);
        }
    }

    private void onPlanClick2() {
        if (savePictureViewModel.getPhotoModel().getPlan_id() != null && !savePictureViewModel.getPhotoModel().getPlan_id().equals("")) {

            new Handler().postDelayed(() -> {
                //your code here
                new GetFlawFlagUsingPhotoId().execute();
            }, 100);

        } else {
            startActivityForResult(new Intent(this, PlansActivity.class).putExtra("projectId", savePictureViewModel.getProjectId()).putExtra("photoId", savePictureViewModel.getPhotoModel().getPdphotolocalId()).putExtra(AllPlansFragment.ARG_PLAN_ID, savePictureViewModel.getPhotoModel().getPlan_id()).putExtra("fromPhoto", true), 102);
        }
    }
    //endregion

    private void showPlanListingScreen() {

        onPlanClick();


//        if (savePictureViewModel.getPhotoModel().getPlan_id() != null && !savePictureViewModel.getPhotoModel().getPlan_id().equals("")) {
//
//            new Handler().postDelayed(() -> {
//                //your code here
//                new GetFlawFlagUsingPhotoId().execute();
//            }, 100);
//
//        } else {
//            startActivityForResult(new Intent(this, PlansActivity.class)
//                    .putExtra("projectId", savePictureViewModel.getProjectId())
//                    .putExtra("photoId", savePictureViewModel.getPhotoModel().getPdphotolocalId())
//                    .putExtra(AllPlansFragment.ARG_PLAN_ID, savePictureViewModel.getPhotoModel().getPlan_id())
//                    .putExtra("fromPhoto", true), 102);
//        }

    }

    //region onClickDefect
    private void onDefectClick(View view) {

        loadMenu();
    }

    private void onDefectClick2(View view) {
        loadMenu();
    }

    //endregion

    //region onRecordClick
    private void onRecordClick() {
        Intent recordIntent = new Intent(this, RecordAudioActivity.class);
        recordIntent.putExtra("projectId", savePictureViewModel.getProjectId());
        recordIntent.putExtra("photoId", savePictureViewModel.getPhotoModel().getPdphotolocalId());
        startActivityForResult(recordIntent, 105);
    }

    private void onRecordClick2() {
        Intent recordIntent = new Intent(this, RecordAudioActivity.class);
        recordIntent.putExtra("projectId", savePictureViewModel.getProjectId());
        recordIntent.putExtra("photoId", savePictureViewModel.getPhotoModel().getPdphotolocalId());
        startActivityForResult(recordIntent, 105);
    }

    //endregion

    //region onBrushClick
    private void onBrushClick() {
//        Intent brushIntent = new Intent(this, BrushActivity2.class);
        Intent brushIntent = new Intent(this, BrushActivity3.class);
        brushIntent.putExtra("path", savePictureViewModel.getImagePath());
        brushIntent.putExtra("projectId", savePictureViewModel.getProjectId());
        brushIntent.putExtra("photoId", savePictureViewModel.getPhotoModel().getPdphotolocalId());
        brushIntent.putExtra("photoModel", savePictureViewModel.getPhotoModel());
        startActivityForResult(brushIntent, 106);
    }

    private void onBrushClick2() {
        Intent brushIntent = new Intent(this, BrushActivity3.class);
        brushIntent.putExtra("path", savePictureViewModel.getImagePath());
        brushIntent.putExtra("projectId", savePictureViewModel.getProjectId());
        brushIntent.putExtra("photoId", savePictureViewModel.getPhotoModel().getPdphotolocalId());
        brushIntent.putExtra("photoModel", savePictureViewModel.getPhotoModel());
        startActivityForResult(brushIntent, 106);
    }

    //endregion

    //region onDestroy
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (preview != null) {
            preview.closeCamera();
        }

        if (updateFlawStatus != null) {
            unregisterReceiver(updateFlawStatus);
            updateFlawStatus = null;
        }
        if (brAddPlanListing != null) {
            unregisterReceiver(brAddPlanListing);
            brAddPlanListing = null;
        }


//        if (updateFlawFlagStatus != null) {
//            unregisterReceiver(updateFlawFlagStatus);
//            updateFlawFlagStatus = null;
//        }

    }
    //endregion

    //region onFinishSuccess
    @Override
    public void onFinishSuccess(String photoPath) {

        if (photoPath != null && !photoPath.equals("")) {

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    onCapturePhoto(photoPath);
                    hideCameraView();
                    showPreviewMode();
                    onShowProgressBar();
                }
            });
        }
    }
    //endregion

    //region hideCameraView
    public void hideCameraView() {
        cameraPreview.setVisibility(View.GONE);
        iv_camera.setVisibility(View.VISIBLE);
        iv_flash.setVisibility(View.GONE);
        tv_close_camera.setVisibility(View.GONE);
        ll_icons.setVisibility(View.VISIBLE);
        if (!isViewMode) {
            iv_flash.setVisibility(View.GONE);
            iv_delete.setVisibility(View.GONE);
            if (isLandScape) {
                iv_delete.setVisibility(View.GONE);
                iv_delete_rotated.setVisibility(View.VISIBLE);
            } else {
                iv_flash.setVisibility(View.GONE);
            }
        }
    }
    //endregion

    //region showCameraView
    public void showCameraView() {
        cameraPreview.setVisibility(View.VISIBLE);
        iv_flash.setVisibility(View.VISIBLE);
        tv_close_camera.setVisibility(View.VISIBLE);
        ll_icons.setVisibility(View.GONE);
        simpleList.setVisibility(View.GONE);
        iv_delete.setVisibility(View.GONE);
        iv_delete_rotated.setVisibility(View.GONE);
    }
    //endregion

    //region showPreviewMode
    public void showPreviewMode() {
        isPreviewMode = true;
        rl_photo_preview_section.setVisibility(View.VISIBLE);
        ll_icons.setVisibility(View.VISIBLE);
        iv_delete.setVisibility(View.VISIBLE);
    }
    //endregion

    //region hidePreviewMode
    public void hidePreviewMode() {
        isPreviewMode = false;
        rl_photo_preview_section.setVisibility(View.GONE);
        ll_icons.setVisibility(View.GONE);

    }
    //endregion

    //region onCapturePhoto
    public void onCapturePhoto(String photoPath) {
        Utils.showLogger("onCapturePhoto>>Called");
//        flawId = "";
        try {
            savePictureViewModel.setImagePath(photoPath);
            Bitmap bitmap = BitmapFactory.decodeFile(photoPath);
            bitmap = getRotatedBitmap(bitmap);

            captured_image.setImageBitmap(bitmap);

/*            Glide.with(SavePictureActivity.this).load(photoPath).placeholder(R.drawable.border_photos).diskCacheStrategy(DiskCacheStrategy.ALL)
                    .dontAnimate().into(captured_image);*/


            SimpleDateFormat simpleDateFormat = null;

//            if (sharedPrefsManager.getStringValue(AppConstantsManager.APP_LANGUAGE, "de").equals("de")) {
//                simpleDateFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
//            } else {
//                simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
//            }

            simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

            String photoDate = simpleDateFormat.format(new Date());

            long created_date = 0;
            if (photoDate != null && !photoDate.equals("")) {
                created_date = Utils.getCurrentTimeStamp().getTime();
            }
            String currentTime = new SimpleDateFormat("HH:mm", Locale.getDefault()).format(new Date());

            PhotoModel photoModel = new PhotoModel("", savePictureViewModel.getProjectId(), sharedPrefsManager.getStringValue(AppConstantsManager.PD_USER_ID, ""), ProjectDocuUtilities.givenFile_MD5_Hash(savePictureViewModel.getImagePath()), "", "", "", photoDate, "", "", photoDate, "", "", "", "", "", "", "", photoDate, "", "", "", LocalPhotosRepository.MISSING_PHOTO_QUALITY, "", "", "", "", "", "", "", created_date, currentTime, LocalPhotosRepository.TYPE_LOCAL_PHOTO);

            if (flawId != null && !flawId.equals("")) {
                photoModel.setLocal_flaw_id(flawId);
                photoModel.setDefectAdded(true);
                if (savePictureViewModel.getPhotoModel() != null) {
                    savePictureViewModel.getPhotoModel().setDefectAdded(true);
                    Utils.showLogger("iden1524");
                    savePictureViewModel.getPhotoModel().setPhotoSynced(false);//Done
                    savePictureViewModel.getPhotoModel().setPhotoUploadStatus(LocalPhotosRepository.UN_SYNC_PHOTO);
                    isPhotoUpdate = true;
                }
                new UpdateFlawAsyncTask(this).execute(flawId);
            }
            if (!isFromLocalPhotos) {
//                if (ProjectNavigator.wlanIsConnected(SavePictureActivity.this) || ProjectNavigator.mobileNetworkIsConnected(SavePictureActivity.this)) {
//                    photoModel.setPhotoUploadStatus(LocalPhotosRepository.UPLOADING_PHOTO);
//                } else {
//                    photoModel.setPhotoUploadStatus(LocalPhotosRepository.UN_SYNC_PHOTO);
//                }
                Utils.showLogger("UploadingFromHere setPhotoUploadStatus");
                //      photoModel.setPhotoUploadStatus(LocalPhotosRepository.UPLOADING_PHOTO);//
            } else {
                photoModel.setPhotoUploadStatus(LocalPhotosRepository.UN_SYNC_PHOTO);
            }

            // Exif params set

            int photoWidth = 0;
            int photoHeight = 0;
            int orientation = 0;
            int gpsEnable = 0;

            double lat = 0, lng = 0;

            float compassDirectionDegree = 0;
            int compassEnable = 0;


            ExifInterface exifInterface = new ExifInterface(photoPath);
            exifInterface.getAttribute(ExifInterface.TAG_GPS_ALTITUDE);
            orientation = Integer.parseInt(exifInterface.getAttribute(ExifInterface.TAG_ORIENTATION));
            photoWidth = Integer.parseInt(exifInterface.getAttribute(ExifInterface.TAG_IMAGE_WIDTH));
            photoHeight = Integer.parseInt(exifInterface.getAttribute(ExifInterface.TAG_IMAGE_LENGTH));

            photoModel.setExifWidth(photoWidth + "");
            photoModel.setExifHeight(photoHeight + "");
            photoModel.setExifOrientation(orientation + "");
//            photoModel.setExifOrientation(ProjectDocuCameraPreview.currentDeviceOrientation+"");

            if (sharedPrefsManager.getBooleanValue(AppConstantsManager.ACTIVATE_GPS, false)) {
                if (gpsTracker != null && gpsTracker.getLatitude() != 0.0f) {
                    gpsEnable = 1;
                    lat = gpsTracker.getLatitude();
                    lng = gpsTracker.getLongitude();

                    photoModel.setExifHasGps(gpsEnable + "");
                    photoModel.setExifGpsX(lng + "");
                    photoModel.setExifGpsY(lat + "");
                    if (gpsTracker.getAccurecy() != 0.0f)
                        photoModel.setGpsAccuracy(gpsTracker.getAccurecy() + " meter");
                }
                compassEnable = 1;


                compassDirectionDegree = getRotationForCompass(deviceOrientation, compassDegrees);

                photoModel.setExifHasGpsDirection(compassEnable + "");
                photoModel.setExifGpsDirection(compassDirectionDegree + "");
            }

            photoModel.setExifUseOrientation("0");

            HashMap<String, ExifInterface> listHashMapDumpData = new HashMap<>();
            listHashMapDumpData.put("exifDump", exifInterface);
            photoModel.setExifDump(listHashMapDumpData.toString());


            savePictureViewModel.setInsertPhotoModel(photoModel);

//            new Handler().postDelayed(new Runnable() {
//            new Handler().postDelayed(new Runnable() {
//                @Override
//                public void run() {

            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    photoId = savePictureViewModel.getmRepository().getPhotoModel().getPdphotolocalId() + "";
                    Utils.showLogger("newPhotoId>>>" + photoId);
                }
            }, 1000);


            photoId = recentUsedWordsViewModel.getPhotoId() + "";
            ///Utils.showLogger("newPhotoId2>>>"+photoId);

            Log.d(TAG, "Local Photo Id: " + photoId);
            recentUsedWordsViewModel.setPhotoId(savePictureViewModel.getmRepository().getPhotoModel().getPdphotolocalId());

//                }
//            }, 50);


            if (flawId != null && !flawId.equals("")) {
                Intent intent = new Intent(BR_ACTION_UPDATE_DEFECT_PHOTOS);
                intent.putExtra(BR_KEY_IS_UPLOAD_PHOTOS_AUTO, true);
                sendBroadcast(intent);
            }


            new updateRecentWordsDataAsyncTask().execute(savePictureViewModel.getProjectId());
//                        isLocationGetFisrtTime = true;
            if (!sharedPrefsManager.getGpsAccuracy(this).equals("") && !sharedPrefsManager.getGpsAccuracy(this).equals("0m") && sharedPrefsManager.getBooleanValue(AppConstantsManager.FIND_POSITION_IF_GEO_REFERENCED_PLAN, false) && !sharedPrefsManager.getLastUsedPlanId(this).equals("-1") && sharedPrefsManager.getBooleanValue(AppConstantsManager.AUTO_LOAD_LAST_PLAN, true)) {
//                            isLocationGetFisrtTime = true;
//                            Toast.makeText(this, "onLocationChanged", Toast.LENGTH_SHORT).show();
                new Handler().postDelayed(() -> {
                    //your code here
                    new RetrievePlansReferPointAsyncTask(this, getIntent().getStringExtra("projectId"), sharedPrefsManager.getLastUsedPlanId(this)).execute();
                }, 2000);

            }
//                        callUploadImageAPI();
            isCapturePhoto = true;
            setDoneIcons();
        } catch (Exception e) {
//            Toast.makeText(this, "Failed to load " + e.toString(), Toast.LENGTH_LONG).show();
            Log.e("Camera", e.toString());
            finish();
            e.printStackTrace();
        }

        if (deviceOrientation == 90) {
            cameraPreview.setVisibility(View.GONE);
            reverseLandscapeHandling();
        }
    }
    //endregion

    //region onFinishFailure
    @Override
    public void onFinishFailure(Bitmap bitmap) {

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                captured_image.setImageBitmap(bitmap);
                hideCameraView();
                showPreviewMode();
            }
        });

    }
    //endregion

    /*class UpdatePhotosAsyncTask extends AsyncTask<String, Void, Void> {
        private PhotoDao mAsyncTaskDao;
        String projectID, photoID;

        UpdatePhotosAsyncTask(String projectId, String photoId) {
            mAsyncTaskDao = ProjectsDatabase.getDatabase(getApplication()).photoDao();
            projectID = projectId;
            photoID = photoId;
        }

        @Override
        protected Void doInBackground(final String... params) {

            List<PhotoModel> defectTradeModelListt = mAsyncTaskDao.getDefectPhotosListUsingLoalID(projectID, photoID);

            if (defectTradeModelListt != null && defectTradeModelListt.size() > 0) {

                defectTradeModelListt.get(0).setPhotoSynced(false);
                defectTradeModelListt.get(0).setPhotoUploadStatus(LocalPhotosRepository.UN_SYNC_PHOTO);
                isPhotoUpdate = true;
                mAsyncTaskDao.update(defectTradeModelListt.get(0));

            }

            return null;
        }
    }*/


    private class GetFlawFlagUsingPhotoId extends AsyncTask<Void, Void, Void> {
        private DefectsDao mAsyncTaskDao;

        GetFlawFlagUsingPhotoId() {
        }

        @Override
        protected Void doInBackground(final Void... params) {

            pdFlawFlagRepository = new PdFlawFlagRepository(SavePictureActivity.this, savePictureViewModel.getProjectId());

            Utils.showLogger("GetFlawFlagUsingPhotoId doInBackground");

            flawFlagObj = pdFlawFlagRepository.getmDefectsPhotoDao().getFlawFlagObjUsingPhotoID(savePictureViewModel.getProjectId(), savePictureViewModel.getPhotoModel().getPlan_id(), savePictureViewModel.getPhotoModel().getPdphotolocalId() + "");

            if (flawFlagObj == null) Utils.showLogger("flaFlagObj null");
            else Utils.showLogger("flaFlagObj not null");

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

            if (flawFlagObj != null && flawFlagObj.getXcoord() != null) {

                sharedPrefsManager.setBooleanValue(AppConstantsManager.VIEW_DIRECTION_THROUGH_COMPASS_MANUAL_STATE, false);

                sharedPrefsManager.setBooleanValue(AppConstantsManager.ACTIVATE_GPS_MANUAL_STATE, false);

                startActivity(new Intent(SavePictureActivity.this, PhotoAddDirectionMainActivity.class)//fixed--
                        .putExtra("projectId", savePictureViewModel.getProjectId()).putExtra("photoId", savePictureViewModel.getPhotoModel().getPdphotolocalId()).putExtra("planId", savePictureViewModel.getPhotoModel().getPlan_id()).putExtra("flawFlagObj", flawFlagObj).putExtra("fromPhoto", true).putExtra("fromPlanScreen", false));
            } else {

                startActivityForResult(new Intent(SavePictureActivity.this, PlansActivity.class).putExtra("projectId", savePictureViewModel.getProjectId()).putExtra("photoId", savePictureViewModel.getPhotoModel().getPdphotolocalId()).putExtra(AllPlansFragment.ARG_PLAN_ID, savePictureViewModel.getPhotoModel().getPlan_id()).putExtra("fromPhoto", true), 102);
            }
        }
    }

    //region onBackPressed
    @Override
    public void onBackPressed() {
//        startActivity(new Intent(this,CameraActivity.class));
//          super.onBackPressed();
        Utils.showLogger("onBackPressed=>" + isCapturePhoto + ":" + isViewMode);
        if (isCapturePhoto && !isViewMode) {
            if (isFromLocalPhotos) {
                Intent intent = new Intent(LocalPhotosFragment.START_SYNC_PHOTOS_BACK_FROM_PHOTO_SCREEN);
                sendBroadcast(intent);
                finish();
                return;
            } else {
                Utils.showLogger("calling from on back presss");
                //startBackgroundTask(SavePictureActivity.this, null, projectID, true);//If not from local photoes on back press
                Intent output = new Intent();
                output.putExtra("photoId", savePictureViewModel.getPhotoModel().getPdphotolocalId());
                output.putExtra("position", position);
                setResult(7890, output);
                Utils.showLogger("calling from on back presss");
                finish();
            }
        }

        if (isPhotoUpdate) {
            Intent output = new Intent();
            output.putExtra("photoId", savePictureViewModel.getPhotoModel().getPdphotolocalId());
            output.putExtra("position", position);
            setResult(7890, output);
            finish();
            return;
        }

       /* if (isBackCamera)
            super.onBackPressed();
        else*/
        if (isPhotoFromGallary || isPhotoUpdate || isBackCamera) {
            Intent output = new Intent();
            Utils.showLogger("calling that");
            if (savePictureViewModel.getPhotoModel() != null)
                output.putExtra("photoId", savePictureViewModel.getPhotoModel().getPdphotolocalId());
            else output.putExtra("photoId", onlineModelID);

            output.putExtra("position", position);
            setResult(7890, output);
            finish();
        } else super.onBackPressed();

//        else takePhoto();
    }
    //endregion

    //region onActivityResult
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case TAKE_PICTURE:
                if (resultCode == Activity.RESULT_OK) {
                    getContentResolver().notifyChange(savePictureViewModel.getImageUri(), null);
                    ContentResolver cr = getContentResolver();
                    Bitmap bitmap;
                    try {
                        bitmap = MediaStore.Images.Media.getBitmap(cr, savePictureViewModel.getImageUri());
                        bitmap = getRotatedBitmap(bitmap);
                        captured_image.setImageBitmap(bitmap);

//                        Bitmap bitmapOrg = bitmap;
//                        ByteArrayOutputStream stream = new ByteArrayOutputStream();
//                        bitmapOrg.compress(Bitmap.CompressFormat.JPEG, 70, stream);
//                        byte[] imageInByte = stream.toByteArray();
//                        long lengthbmpOrg = imageInByte.length;
//
//                        Bitmap bitmapObj = bitmap;
//                        ByteArrayOutputStream streamm = new ByteArrayOutputStream();
//                        bitmapObj.compress(Bitmap.CompressFormat.JPEG, 40, streamm);
//                        byte[] imageInBytee = streamm.toByteArray();
//                        long lengthbmp = imageInBytee.length;

//                        Log.d("photo_comp",lengthbmp+"");
//                        Log.d("photo_org",lengthbmpOrg+"");
                        SimpleDateFormat simpleDateFormat = null;

                        if (sharedPrefsManager.getStringValue(AppConstantsManager.APP_LANGUAGE, "de").equals("de")) {
                            simpleDateFormat = new SimpleDateFormat("dd-MM-yyyy");

                        } else {
                            simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
                        }

                        isImageFromGallary = true;


                        String photoDate = simpleDateFormat.format(new Date());

                        long created_date = 0;
                        if (photoDate != null && !photoDate.equals("")) {
                            created_date = Utils.getCurrentTimeStamp().getTime();
                        }
                        String currentTime = new SimpleDateFormat("HH:mm", Locale.getDefault()).format(new Date());

                        PhotoModel photoModel = new PhotoModel("", savePictureViewModel.getProjectId(), sharedPrefsManager.getStringValue(AppConstantsManager.PD_USER_ID, ""), ProjectDocuUtilities.givenFile_MD5_Hash(savePictureViewModel.getImagePath()), "", "", "", photoDate, "", "", photoDate, "", "", "", "", "", "", "", photoDate, "", "", "", LocalPhotosRepository.MISSING_PHOTO_QUALITY, "", "", "", "", "", "", "", created_date, currentTime, LocalPhotosRepository.TYPE_LOCAL_PHOTO);

                        if (flawId != null && !flawId.equals("")) {
                            photoModel.setLocal_flaw_id(flawId);
                            photoModel.setDefectAdded(true);
                            if (savePictureViewModel.getPhotoModel() != null) {
                                savePictureViewModel.getPhotoModel().setDefectAdded(true);
                                Utils.showLogger("iden1863");
                                savePictureViewModel.getPhotoModel().setPhotoSynced(false);
                                savePictureViewModel.getPhotoModel().setPhotoUploadStatus(LocalPhotosRepository.UN_SYNC_PHOTO);
                                isPhotoUpdate = true;
                            }
                        }
                        savePictureViewModel.setInsertPhotoModel(photoModel);


                        photoId = savePictureViewModel.getmRepository().getPhotoModel().getPdphotolocalId() + "";
                        recentUsedWordsViewModel.setPhotoId(savePictureViewModel.getmRepository().getPhotoModel().getPdphotolocalId());
                        setDoneIcons();
                        if (flawId != null && !flawId.equals("")) {
                            Intent intent = new Intent("updateDefectPhotos");
                            sendBroadcast(intent);
                        }


                        new updateRecentWordsDataAsyncTask().execute(savePictureViewModel.getProjectId());
//                        isLocationGetFisrtTime = true;
                        if (!sharedPrefsManager.getGpsAccuracy(this).equals("") && !sharedPrefsManager.getGpsAccuracy(this).equals("0m") && sharedPrefsManager.getBooleanValue(AppConstantsManager.FIND_POSITION_IF_GEO_REFERENCED_PLAN, false) && !sharedPrefsManager.getLastUsedPlanId(this).equals("-1")) {
//                            isLocationGetFisrtTime = true;
//                            Toast.makeText(this, "onLocationChanged", Toast.LENGTH_SHORT).show();
                            new RetrievePlansReferPointAsyncTask(this, getIntent().getStringExtra("projectId"), sharedPrefsManager.getLastUsedPlanId(this)).execute();


                        }
//                        callUploadImageAPI();
                        Utils.showLogger("calling from take photoes");
                        startBackgroundTask(SavePictureActivity.this, null, projectID, true);//When auto syncing photoes


                    } catch (Exception e) {
                        Toast.makeText(this, "Failed to load " + e.toString(), Toast.LENGTH_LONG).show();
                        Log.e("Camera", e.toString());
                        e.printStackTrace();
                    }
                } else {
                    Log.d("Result", "not okay");
                    SavePictureActivity.this.finish();
                }

                break;

            case RESULT_LOAD_IMAGE:
                if (resultCode == Activity.RESULT_OK) {
                    Uri selectedImage = data.getData();
                    try {
                        Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), selectedImage);
                        String path = getPath(selectedImage);


                        InputStream inputStream = getContentResolver().openInputStream(selectedImage);
                        ExifInterface exifInterface = new ExifInterface(inputStream);
                        int newOrientation = exifInterface.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
                        Utils.showLogger("newOrientatiaon>>" + newOrientation);
                        /*
                        int bitmapOrientation =
                                getOrientation(selectedImage);

                        Utils.showLogger2("bitmapOrientation>>>"+bitmapOrientation);
*/

                        boolean isNoDiskAccess = false;
                        Bitmap myBitmap = null;
                        try {
                            myBitmap = getRotatedBitmapWithPath(bitmap, path);
                        } catch (IOException e) {
                            e.printStackTrace();
                            isNoDiskAccess = true;
                            //myBitmap = bitmap;
                            myBitmap = getRotatedBitmapWithInputStream(bitmap, exifInterface);
                        }
//                        Bitmap myBitmap= bitmap;

                        captured_image.setImageBitmap(myBitmap);


//                        Matrix matrix = new Matrix();
//                        matrix.postRotate(90);
//                        Bitmap  bitmap = Bitmap.createBitmap(sourceBitmap, 0, 0, sourceBitmap.getWidth(), sourceBitmap.getHeight(), matrix, true);


                        DateFormat f = null;
                        f = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                        String[] filePathColumn = {MediaStore.Images.Media.DATE_TAKEN};
                        Date startDate = null;
                        try {
                            Cursor cursor = getContentResolver().query(selectedImage, filePathColumn, null, null, null);
                            cursor.moveToFirst();
                            int columnIndex = cursor.getColumnIndex(filePathColumn[0]);

                            String pictureDate = cursor.getString(columnIndex);

//                        if (sharedPrefsManager.getStringValue(AppConstantsManager.APP_LANGUAGE, "de").equals("de")) {
//                            f = new SimpleDateFormat("dd-MM-yyyy");
//
//                        } else {
//                            f = new SimpleDateFormat("yyyy-MM-dd");
//                        }
                            f = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                            if (pictureDate != null && !pictureDate.equals("") && !pictureDate.equals("0")) {
                                startDate = new Date(Long.valueOf(pictureDate));
                            } else {
                                startDate = new Date();
                            }
                        }
                        catch (Exception e){
                            startDate = new Date();
                            e.printStackTrace();
                        }
                        if (isNoDiskAccess)
                            writeResponseBodyToDisk(path, savePictureViewModel.getProjectId(), bitmap);
                        else
                            writeResponseBodyToDisk(path, savePictureViewModel.getProjectId(), null);


//                        SharedPrefsManager sharedPrefsManager = new SharedPrefsManager(this);
//                        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.GERMANY);
                        String photoDate = f.format(startDate);
                        ;
                        long created_date = 0;
                        if (photoDate != null && !photoDate.equals("")) {
                            created_date = new Timestamp(startDate.getTime()).getTime();
                        }


                        String currentTime = new SimpleDateFormat("HH:mm", Locale.getDefault()).format(startDate);
                        PhotoModel photoModel = new PhotoModel("", savePictureViewModel.getProjectId(), sharedPrefsManager.getStringValue(AppConstantsManager.PD_USER_ID, ""), ProjectDocuUtilities.givenFile_MD5_Hash(path), "", "", "", photoDate, "", "", photoDate,
                                "", "", "", "", "", "", "", photoDate, "", "", "", LocalPhotosRepository.MISSING_PHOTO_QUALITY, "", "", "",
                                "", "", "",
                                "", created_date, currentTime, LocalPhotosRepository.TYPE_LOCAL_PHOTO);
                        photoModel.setFromGallery(true);

                        // Exif params set

                        int photoWidth = 0;
                        int photoHeight = 0;
                        int orientation = 0;
                        int gpsEnable = 0;

                        double lat = 0, lng = 0;

                        float compassDirectionDegree = 0;
                        int compassEnable = 0;


                        try {
                     /*
                      ExifInterface exifInterface = null;
                            if (isNoDiskAccess) {
                                exifInterface = new ExifInterface(imagePath);
                            }
                            else
                                exifInterface = new ExifInterface(path);*/
                            //exifInterface.getAttribute(ExifInterface.TAG_GPS_ALTITUDE);
                            orientation = Integer.parseInt(exifInterface.getAttribute(ExifInterface.TAG_ORIENTATION));
                            Utils.showLogger2("newOrientation>>" + orientation);
                            photoWidth = Integer.parseInt(exifInterface.getAttribute(ExifInterface.TAG_IMAGE_WIDTH));
                            photoHeight = Integer.parseInt(exifInterface.getAttribute(ExifInterface.TAG_IMAGE_LENGTH));
                        } catch (Exception e) {
                            orientation = ExifInterface.ORIENTATION_NORMAL;
                            photoWidth = bitmap.getWidth();
                            photoHeight = bitmap.getHeight();
                            e.printStackTrace();
                        }

                        photoModel.setExifWidth(photoWidth + "");
                        photoModel.setExifHeight(photoHeight + "");
                        photoModel.setExifOrientation(orientation + "");

                        if (sharedPrefsManager.getBooleanValue(AppConstantsManager.ACTIVATE_GPS, false)) {//when adding attributes
                            if (gpsTracker != null && gpsTracker.getLatitude() != 0.0f) {
                                gpsEnable = 1;
                                lat = gpsTracker.getLatitude();
                                lng = gpsTracker.getLongitude();

                                photoModel.setExifHasGps(gpsEnable + "");
                                photoModel.setExifGpsX(lng + "");
                                photoModel.setExifGpsY(lat + "");
                            }


                            compassEnable = 1;
                            compassDirectionDegree = getRotationForCompass(deviceOrientation, compassDegrees);

                            photoModel.setExifHasGpsDirection(compassEnable + "");
                            Log.d(TAG, "CompassDirectionDegree: " + compassDirectionDegree);
                            photoModel.setExifGpsDirection(compassDirectionDegree + "");
                            if (gpsTracker != null && gpsTracker.getAccurecy() != 0.0f)
                                photoModel.setGpsAccuracy(gpsTracker.getAccurecy() + " meter");
                        }

                        photoModel.setExifUseOrientation(0 + "");

                        HashMap<String, ExifInterface> listHashMapDumpData = new HashMap<>();
                        if (exifInterface != null) {
                            listHashMapDumpData.put("exifDump", exifInterface);
                            photoModel.setExifDump(listHashMapDumpData.toString());
                        }
                        savePictureViewModel.setInsertPhotoModel(photoModel);
                        photoId = savePictureViewModel.getmRepository().getPhotoModel().getPdphotolocalId() + "";
                        new updateRecentWordsDataAsyncTask().execute(savePictureViewModel.getProjectId());

//                        Thread.sleep(500);
                        if (!sharedPrefsManager.getGpsAccuracy(this).equals("") && !sharedPrefsManager.getGpsAccuracy(this).equals("0m") && sharedPrefsManager.getBooleanValue(AppConstantsManager.FIND_POSITION_IF_GEO_REFERENCED_PLAN, false) && !sharedPrefsManager.getLastUsedPlanId(this).equals("-1") && sharedPrefsManager.getBooleanValue(AppConstantsManager.AUTO_LOAD_LAST_PLAN, true)) {
//                            isLocationGetFisrtTime = true;
//                            Toast.makeText(this, "onLocationChanged", Toast.LENGTH_SHORT).show();

                            new Handler().postDelayed(() -> {
                                //your code here
                                new RetrievePlansReferPointAsyncTask(this, getIntent().getStringExtra("projectId"), sharedPrefsManager.getLastUsedPlanId(this)).execute();

                            }, 2000);

                        }
                        ll_icons.setVisibility(View.VISIBLE);
//                        callUploadImageAPI();
//                        startBackgroundTask(SavePictureActivity.this, null, projectID, true);
                        setDoneIcons();

                    } catch (IOException e) {
                        Log.i("TAG", "Some exception " + e);
                    }
                    if (deviceOrientation == 90) {
                        reverseLandscapeHandling();
                    }
                } else SavePictureActivity.this.finish();
                break;
            case 101:
                if (data != null) {
                    String comments = data.getStringExtra("comments");
                    if (comments != null && !savePictureViewModel.getPhotoModel().getDescription().equals(comments)) {
                        savePictureViewModel.getPhotoModel().setDescription(comments);
                        savePictureViewModel.getPhotoModel().setPhotoSynced(false);
                        savePictureViewModel.getPhotoModel().setPhotoUploadStatus(LocalPhotosRepository.UN_SYNC_PHOTO);
                        savePictureViewModel.updatePhotoModel();
                        isPhotoUpdate = true;
                        if (comments.isEmpty()) {
                            commentDoneIcon.setVisibility(View.GONE);
                        } else {
                            commentDoneIcon.setVisibility(View.VISIBLE);
                        }
                    }
                }
                break;
            case 1122:

                savePictureViewModel.getUpdatedPhotoModel().observe(this, new Observer<PhotoModel>() {
                    @Override
                    public void onChanged(PhotoModel photoModel) {
                        if (photoModel != null) {
                            new updatePhotoAsyncTask(savePictureViewModel.getPhotoModel().getPdphotolocalId(), savePictureViewModel.getProjectId(), photoModel).execute();
                            savePictureViewModel.getUpdatedPhotoModel().removeObserver(this);
                        }
                    }
                });

                break;
            case 102:
                if (resultCode == Activity.RESULT_OK) {
                    if (data != null) {
                        boolean isPlanAttachToImage = data.getBooleanExtra(PLAN_ATTACH_TO_PHOTO_KEY, false);
                        String planId = data.getStringExtra(PLAN_ID_KEY);
                        if (isPlanAttachToImage) {
                            savePictureViewModel.getPhotoModel().setPlanAdded(true);
                            savePictureViewModel.getPhotoModel().setPlan_id(planId);
                            savePictureViewModel.getPhotoModel().setPhotoSynced(false);
                            savePictureViewModel.getPhotoModel().setPhotoUploadStatus(LocalPhotosRepository.UN_SYNC_PHOTO);
                            isPhotoUpdate = true;
                            new updatePhotoAsyncTask(savePictureViewModel.getPhotoModel().getPdphotolocalId(), savePictureViewModel.getProjectId(), savePictureViewModel.getPhotoModel()).execute();
                            //    Toast.makeText(getApplicationContext(), "Attached: " , Toast.LENGTH_SHORT).show();

                        } else {
                            savePictureViewModel.getPhotoModel().setPlanAdded(false);
                            savePictureViewModel.getPhotoModel().setPlan_id("");
                            savePictureViewModel.getPhotoModel().setPhotoSynced(false);
                            savePictureViewModel.getPhotoModel().setPhotoUploadStatus(LocalPhotosRepository.UN_SYNC_PHOTO);
                            isPhotoUpdate = true;
                            new updatePhotoAsyncTask(savePictureViewModel.getPhotoModel().getPdphotolocalId(), savePictureViewModel.getProjectId(), savePictureViewModel.getPhotoModel()).execute();
                            //    Toast.makeText(getApplicationContext(), "Remove Attached: " , Toast.LENGTH_SHORT).show();
                        }
                        // Toast.makeText(getApplicationContext(), "RESULT_OK: " , Toast.LENGTH_SHORT).show();

                    }
                }
                break;
            case 103:
            case 104:

                new updateRecentWordsDataAsyncTask().execute(savePictureViewModel.getProjectId());
                new updatePhotoAsyncTask(savePictureViewModel.getPhotoModel().getPdphotolocalId(), savePictureViewModel.getProjectId(), savePictureViewModel.getPhotoModel()).execute();
                break;
            case 105:

                if (data != null) {
                    boolean isPlanAttachToImage = data.getBooleanExtra(AUDIO_ATTACH_TO_PHOTO_KEY, false);
                    if (isPlanAttachToImage) {
                        savePictureViewModel.getPhotoModel().setRecordingAdded(true);
                        savePictureViewModel.getPhotoModel().setPhotoSynced(false);
                        savePictureViewModel.getPhotoModel().setPhotoUploadStatus(LocalPhotosRepository.UN_SYNC_PHOTO);
                        isPhotoUpdate = true;
                        new updatePhotoAsyncTask(savePictureViewModel.getPhotoModel().getPdphotolocalId(), savePictureViewModel.getProjectId(), savePictureViewModel.getPhotoModel()).execute();
                        //    Toast.makeText(getApplicationContext(), "Attached: " , Toast.LENGTH_SHORT).show();
                    }
                    // Toast.makeText(getApplicationContext(), "RESULT_OK: " , Toast.LENGTH_SHORT).show();
                }

                break;
            case 106:

                if (data != null) {
                    boolean isPlanAttachToImage = data.getBooleanExtra(SKETCH_ATTACH_TO_PHOTO_KEY, false);
                    Utils.showLogger("billal SKETCH_ATTACH_TO_PHOTO_KEY");

                    if (isPlanAttachToImage && savePictureViewModel.getPhotoModel() != null) {
                        //savePictureViewModel.getPhotoModel().setBrushImageAdded(true);//brush added 1
                        //savePictureViewModel.getPhotoModel().setPhotoSynced(false);
                        // savePictureViewModel.getPhotoModel().setPhotoUploadStatus(LocalPhotosRepository.UN_SYNC_PHOTO);
                        //isPhotoUpdate = true;
                        //new updatePhotoAsyncTask(savePictureViewModel.getPhotoModel().getPdphotolocalId(), savePictureViewModel.getProjectId(), savePictureViewModel.getPhotoModel()).execute();
                        //    Toast.makeText(getApplicationContext(), "Attached: " , Toast.LENGTH_SHORT).show();
                    }
                    // Toast.makeText(getApplicationContext(), "RESULT_OK: " , Toast.LENGTH_SHORT).show();
                }


//                savePictureViewModel.getUpdatedPhotoModel().observe(this, new Observer<PhotoModel>() {
//                    @Override
//                    public void onChanged(PhotoModel photoModel) {
//                        if (photoModel != null) {
//                            savePictureViewModel.updatePhotoModelInViewModel(photoModel);
//                            setDoneIcons();
//                            savePictureViewModel.getUpdatedPhotoModel().removeObserver(this);
//                        }
//                    }
//                });
//                finish();
                break;

            case DEFECT_DETAIL_CODE:
                if (resultCode == Activity.RESULT_OK) {
                    setDoneIcons();
                } else if (resultCode == Activity.RESULT_CANCELED) {
                    if (savePictureViewModel.getPhotoModel() != null) {
                        if (savePictureViewModel.getPhotoModel().getLocal_flaw_id() != null) {
                            savePictureViewModel.getPhotoModel().setLocal_flaw_id(null);
                            savePictureViewModel.getPhotoModel().setDefectAdded(false);
                            new updatePhotoAsyncTask2(savePictureViewModel.getPhotoModel().getPdphotolocalId(), savePictureViewModel.getProjectId(), savePictureViewModel.getPhotoModel(), true).execute();
                        }
                    }
                }

                break;
        }
    }
    //endregion

    //region onMenuItemClick
    @Override
    public boolean onMenuItemClick(MenuItem item) {
        switch (item.getItemId()) {

            case R.id.dropp3:
//                Toast.makeText(getApplicationContext(), "dropp3: ", Toast.LENGTH_SHORT).show();
                break;
            case R.id.droppy4:
//                Toast.makeText(getApplicationContext(), "droppy4 : ", Toast.LENGTH_SHORT).show();
                break;
        }

        return false;
    }
    //endregion

    //region callGetPlanImageApi
    private void callGetPlanImageAPI(Context context, String pdPhotoId) {

        SharedPrefsManager sharedPrefsManager = new SharedPrefsManager(context);
        RetroApiInterface retroApiInterface = RetrofitManager.getInstance().create(RetroApiInterface.class);
        String authToken = sharedPrefsManager.getStringValue(AppConstantsManager.AUTH_API_TOKEN, "");

        if (authToken.length() > 2) {
            authToken = authToken.substring(1, authToken.length() - 1);
        }

        Call<ResponseBody> call = retroApiInterface.getDefectPhotosWithSize(authToken, Utils.DEVICE_ID, pdPhotoId, "xl");
        Log.d("call url", call.request().url().toString());

        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {

                if (response.isSuccessful()) {
                    if (response.body() != null) {
                        Log.d("List", "Success : " + response.body());
                        Bitmap bmp = BitmapFactory.decodeStream(response.body().byteStream());
                        if (captured_image != null && bmp != null) {
                            captured_image.setImageBitmap(bmp);//plan image
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
                    } else Log.d("List", "Not Success : " + response.toString());
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Log.d("List", "failed : " + t.getMessage());
            }
        });
    }
    //endregion

    //region DeletePhotoaAsyncTask
    private class DeletePhotoAsyncTask extends AsyncTask<PhotoModel, Void, Void> {
        private PhotoDao photoDao;


        DeletePhotoAsyncTask() {
            ProjectsDatabase projectsDatabase = ProjectsDatabase.getDatabase(getApplicationContext());
            this.photoDao = projectsDatabase.photoDao();

        }

        @Override
        protected Void doInBackground(PhotoModel... params) {
            Utils.showLogger("DeletePhotoAsyncTask  doInBackground");
            photoDao.deleteUsingPhotoId(params[0].getProjectId(), String.valueOf(params[0].getPdphotolocalId()));
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            finish();
        }
    }
    //endregion

    private void saveBitmap(Bitmap bmp, File filename) {
        try (FileOutputStream out = new FileOutputStream(filename)) {
            bmp.compress(Bitmap.CompressFormat.JPEG, 100, out); // bmp is your Bitmap instance
            // PNG is a lossless format, the compression factor (100) is ignored
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //region writeResponseBodyToDisk
    private boolean writeResponseBodyToDisk(String path, String projectId, Bitmap bitmap) {
        imagePath = "";
        try {
            // todo change the file location/name according to your needs

//            File dir = new File(Environment.getExternalStorageDirectory() + "/projectDocu/project_defects_" + projectId);

            File dir = this.getExternalFilesDir("/projectDocu/project_online_photos" + projectId);
            if (dir == null) {
                dir = this.getFilesDir();
            }
            if (!dir.isDirectory()) {
                dir.mkdirs();
            }
            File photo = new File(dir, "/Download_" + new Date().getTime() + ".jpg");
            // photo=  saveBitmapToFile(photo);

            if (bitmap != null) {
                saveBitmap(bitmap, photo);
                imagePath = photo.getAbsolutePath();
                savePictureViewModel.setImagePath(imagePath);
                return true;
            }

            imagePath = photo.getAbsolutePath();

            InputStream inputStream = null;
            OutputStream outputStream = null;

            savePictureViewModel.setImagePath(imagePath);

            try {
                inputStream = new FileInputStream(path);
                byte[] fileReader = new byte[15000];

                long fileSize = inputStream.available();
                long fileSizeDownloaded = 0;


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
    //endregion

    public String getPath(Uri uri) {
        String[] projection = {MediaStore.MediaColumns.DATA};
        Cursor cursor = managedQuery(uri, projection, null, null, null);
        column_index = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA);
        cursor.moveToFirst();
        imagePath = cursor.getString(column_index);

        return cursor.getString(column_index);
    }

    public int getOrientation(Uri uri) {
        String[] projection = new String[0];
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
            projection = new String[]{MediaStore.MediaColumns.ORIENTATION};
        }
        Cursor cursor = managedQuery(uri, projection, null, null, null);
        column_index = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.ORIENTATION);
        cursor.moveToFirst();
        //orientation = cursor.getString(column_index);

        return cursor.getInt(column_index);
    }

    //region onStop
    @Override
    protected void onStop() {
        super.onStop();
        if (compass != null) {
            compass.stop();
        }
    }
    //endregion

    //region onResume
    @Override
    protected void onResume() {
        super.onResume();
        if (compass != null) {
            compass.start();
        }
//        registerOrientationSensorForUpdates(true);
//        if(getResources().getConfiguration().orientation!=1){
        startOrientationChangeListener();
        if (mOrEventListener != null) mOrEventListener.enable();
//        }else{
//            startOrientationChangeListener();
//            if(mOrEventListener!=null)
//            mOrEventListener.disable();
//        }


//        Toast.makeText(SavePictureActivity.this, "Device Orrr"+getResources().getConfiguration().orientation,Toast.LENGTH_SHORT).show();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            registerReceiver(updateFlawStatus, new IntentFilter("updateFlawStatus"), Context.RECEIVER_EXPORTED);
            registerReceiver(brAddPlanListing, new IntentFilter(BR_ACTION_ADD_PLAN_LIST), Context.RECEIVER_EXPORTED);

        }

        if (savePictureViewModel.getPhotoModel() != null) {

            setDoneIcons();
        }

        registersSensors();
    }
    //endregion

/*
    private void callUploadImageAPI() {
        showProgressbar();
        SharedPrefsManager sharedPrefsManager = new SharedPrefsManager(this);
        String authToken = sharedPrefsManager.getStringValue(AppConstantsManager.AUTH_API_TOKEN, "");
        if (authToken.length() > 2) {
            authToken = authToken.substring(1, authToken.length() - 1);
        }

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.GERMAN);
        String photoDate = simpleDateFormat.format(new Date());

        File imageFile = new File(savePictureViewModel.getImagePath());

        // Create a request body with file and image media type
        RequestBody fileReqBody = RequestBody.create(MediaType.parse("image/*"), imageFile);

        MultipartBody.Part part = MultipartBody.Part.createFormData("file", imageFile.getName(), fileReqBody);

        Map<String, RequestBody> map = new HashMap<>();

        //Create request body with text description and text media type
        RequestBody hash = RequestBody.create(MediaType.parse("text/plain"), ProjectDocuUtilities.givenFile_MD5_Hash(savePictureViewModel.getImagePath()));
        RequestBody photodate = RequestBody.create(MediaType.parse("text/plain"), photoDate);
        RequestBody quality = RequestBody.create(MediaType.parse("text/plain"), "original");
        RequestBody filetype = RequestBody.create(MediaType.parse("text/plain"), "photo");

        map.put("quality", quality);
        map.put("hash", hash);
        map.put("photodate", photodate);
        map.put("filetype", filetype);

        Log.d("date", photoDate);

        RetroApiInterface retroApiInterface = RetrofitManager.getInstance().create(RetroApiInterface.class);
        Call<ResponseBody> call = retroApiInterface.addPhotoAPI(authToken, Utils.DEVICE_ID, sharedPrefsManager.getLastProjectId(this), part, quality,filetype,hash);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                hideProgressbar();
                if (response.isSuccessful()) {
                    if (response.body() != null) {
                        try {
                            String photoId="";
                            JSONObject jsonObject= new JSONObject(response.body().string());
                            if(jsonObject.has("data")){
                                JSONObject jsonObjectData=jsonObject.getJSONObject("data");
                                if(jsonObjectData.has("photoid")){
                                    photoId   =jsonObject.getJSONObject("data").getString("photoid");
                                }
                            }
                            Log.d("upload Image", "Success : " + response.body().string());
                            Toast.makeText(getApplicationContext(), "Image Uploaded", Toast.LENGTH_SHORT).show();
                        } catch (IOException e) {
                            e.printStackTrace();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    } else {
                        Log.d("upload Image", "Empty response");
                        Toast.makeText(SavePictureActivity.this, getString(R.string.toast_login_failed), Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Log.d("upload Image", "Not Success : " + response.toString());
                    Toast.makeText(SavePictureActivity.this, getString(R.string.toast_login_failed), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                hideProgressbar();
                Log.d("upload Image", "failed : " + t.getMessage());
                Toast.makeText(SavePictureActivity.this, getString(R.string.toast_login_failed), Toast.LENGTH_SHORT).show();
            }
        });
    }
*/

    //region onRequestPermissionsResult
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case STORAGE_RQUEST_CODE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    if (preview != null) {
                        cameraPreview.removeView(preview);
                        cameraPreview.addView(preview);
                        cameraPreview.addView(drawingViewCameraFocus);
                        preview.setDrawingView(drawingViewCameraFocus);

                    }
                    takePhoto();
                } else {
//                    finish();
                }
                break;

            case PERMISSION_REQUEST_CODE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    gpsTracker = new GPSTracker(this);
                }
                break;

            case PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION:

                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Enable the my location layer if the permission has been granted.
                    activateGPS();

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        if (checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                            requestPermissions(new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE}, STORAGE_RQUEST_CODE);
                            return;
                        }
                    }


                }

                break;
        }
    }
    //endregion

    private Bitmap getRotatedBitmap(Bitmap bitmap) throws IOException {
        int photoWidth = 0;
        int photoHeight = 0;
        File file = new File(savePictureViewModel.getImagePath());
        ExifInterface exifInterface = new ExifInterface(file.getAbsolutePath());
        int orientation = Integer.parseInt(exifInterface.getAttribute(ExifInterface.TAG_ORIENTATION));
        photoWidth = Integer.parseInt(exifInterface.getAttribute(ExifInterface.TAG_IMAGE_WIDTH));
        photoHeight = Integer.parseInt(exifInterface.getAttribute(ExifInterface.TAG_IMAGE_LENGTH));

        Bitmap rotatedBitmap = null;
        switch (orientation) {
            case ExifInterface.ORIENTATION_ROTATE_90:
                rotatedBitmap = rotateImage(bitmap, 90, photoWidth, photoHeight);
                break;
            case ExifInterface.ORIENTATION_ROTATE_180:
                rotatedBitmap = rotateImage(bitmap, 180, photoWidth, photoHeight);
                break;
            case ExifInterface.ORIENTATION_ROTATE_270:
                rotatedBitmap = rotateImage(bitmap, 270, photoWidth, photoHeight);
                break;
            case ExifInterface.ORIENTATION_NORMAL:
            default:
                rotatedBitmap = bitmap;
        }
        return rotatedBitmap;
    }

    public static Bitmap rotateImage(Bitmap source, float angle) {
        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        return Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(), matrix, true);
    }

    public static Bitmap rotateImage(Bitmap source, float angle, int sourceWidth, int sourceHeight) {
        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        return Bitmap.createBitmap(source, 0, 0, sourceWidth, sourceHeight, matrix, true);
    }


    private ProjectsDatabase db;

    public class updatePhotoAsyncTask extends AsyncTask<Void, Void, Void> {
        boolean isQuick = false;

        private long photoId;
        private String projectID;
        private PhotoModel photoModelObj;
        boolean isFromDefectCreate = false;
        List<String> defectTradeModelListt = null;

        updatePhotoAsyncTask(long photoId, String projectId, PhotoModel photoModel) {
            this.photoId = photoId;
            projectID = projectId;
            photoModelObj = photoModel;

        }

        updatePhotoAsyncTask(boolean isQuick, long photoId, String projectId, PhotoModel photoModel, boolean isFromDefetcCreate) {
            this.photoId = photoId;
            projectID = projectId;
            photoModelObj = photoModel;
            isFromDefectCreate = isFromDefetcCreate;
            this.isQuick = isQuick;

        }

        @Override
        protected Void doInBackground(Void... params) {
            db = ProjectsDatabase.getDatabase(getApplication());

            List<WordModel> wordModels = db.wordDao().getWordsListIncludesPhotoIdWithTypeZero("%," + photoId + "%", projectID);
            if (wordModels.size() > 0) {
                photoModelObj.setWordAdded(true);
            } else {
                photoModelObj.setWordAdded(false);
            }
            Utils.showLogger("identity 2588");
            photoModelObj.setPhotoSynced(false);
            photoModelObj.setPhotoUploadStatus(LocalPhotosRepository.UN_SYNC_PHOTO);
            db.photoDao().update(photoModelObj);

//             defectTradeModelListt = db.photoDao().getDefectIdsAttachedWithPhotos(projectID, photoModelObj.getLocal_flaw_id() + "", photoModelObj.getPdphotolocalId()+"");
//             PhotoModel photoModel = db.photoDao().getDefectPhotosAndLocalPhotosOBj(projectID, photoModelObj.getLocal_flaw_id() + "");
//             DefectsModel defectsModel= db.defectsDao().getDefectsOBJ(projectID, photoModelObj.getLocal_flaw_id());
//             Log.d("before_defect_attach","projectID "+projectID+" Local_flaw_id "+photoModelObj.getLocal_flaw_id()+" photoId "+photoModelObj.getPdphotolocalId());
            isPhotoUpdate = true;

            return null;
        }


        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
//            setDoneIcons();
            if (isFromDefectCreate) {
//                try {
//                Thread.sleep(500);
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            }
//                if (defectTradeModelListt.size() > 0) {
//                   Toast.makeText(SavePictureActivity.this,"Defect Found",Toast.LENGTH_SHORT).show();
//                }

                if (!isQuick) {
                    Intent intent = new Intent(SavePictureActivity.this, DefectDetailsActivity2.class);
                    intent.putExtra(ARG_PROJECT_ID, projectID);

                    intent.putExtra(DefectsListFragment.ARG_PARAM2, photoModelObj.getLocal_flaw_id() + "");
                    intent.putExtra(DefectsActivity.TYPE_DEFECT_KEY, DefectsActivity.TYPE_DEFECT_ADD);
                    intent.putExtra(DefectsActivity.PHOTO_ID_KEY, savePictureViewModel.getPhotoModel().getPdphotolocalId() + "");
                    intent.putExtra(IS_CREATED_MANGEL_KEY, true);
                    startActivityForResult(intent, DEFECT_DETAIL_CODE);
                } else defects_done_2.setVisibility(View.VISIBLE);
            }

        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (compass != null) compass.stop();
    }


    public void showOpenFieldKeywordDialog(final Context act, WordModel wordModel) {
        Utils.showLogger("showOpenFieldKeywordDialog>>" + photoId);
        Gson gson = new Gson();

        //customDialog = new Dialog(act, R.style.customDialogTheme);
        Dialog customDialog = new Dialog(act, R.style.MyDialogTheme);
        customDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        customDialog.setContentView(R.layout.custom_dialog_open_field_keyword_material);
        //TextView text = (TextView) customDialog.findViewById(R.id.customDialog_content_Text);


        customDialog.setCanceledOnTouchOutside(false);
        WindowManager.LayoutParams params = customDialog.getWindow().getAttributes();
        params.y = 10;
        // set the custom dialog components - text, image and button
        TextView titleTxt = (TextView) customDialog.findViewById(R.id.customDialog_titleText);

        Utils.showLogger("keyword is>>" + wordModel.getName());

        titleTxt.setText(wordModel.getName());

        EditText et_search_number = (EditText) customDialog.findViewById(R.id.et_search_number);
        //if (wordModel != null && wordModel.getName() != null)
        //  titleTxt.setText(wordModel.getName());

        try {
            wordContentModel = gson.fromJson(wordModel.getOpen_field_content(), WordContentModel.class);

        } catch (Exception e) {
            e.printStackTrace();
        }

        if (wordContentModel == null) wordContentModel = new WordContentModel();


        imgIdVsInput = wordContentModel.findByImageId(photoId, wordModel.getName());

        if (imgIdVsInput != null) et_search_number.setText(imgIdVsInput.getInputFields());

        LinearLayout bt = (LinearLayout) customDialog.findViewById(R.id.ll_previous);
        bt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                new ProjectSyncManager(HomeActivity.this, projectId, syncTaskComplete, false);

                customDialog.dismiss();
            }
        });

        LinearLayout bt1 = (LinearLayout) customDialog.findViewById(R.id.ll_next);
        bt1.setVisibility(View.VISIBLE);
        bt1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String newInput = et_search_number.getText().toString();
                {

                    if (et_search_number.getText().toString().equals("")) {
                        return;

//                            return false;
                    }


                    if (imgIdVsInput == null) {
                        imgIdVsInput = new ImageId_VS_Input(photoId, newInput, wordModel.getName());
                        wordContentModel.getInputsList().add(imgIdVsInput);
                    } else imgIdVsInput.setInputFields(newInput);


                    wordModel.setOpen_field_content(gson.toJson(wordContentModel));
                    wordModel.setPhotoIds(wordModel.getPhotoIds() + "," + photoId);
                    wordModel.setUseCount(wordModel.getUseCount() + 1);
                    new updateAsyncTask(ProjectsDatabase.getDatabase(SavePictureActivity.this).wordDao()).execute(wordModel);

                }

                new updateRecentWordsDataAsyncTask().execute(savePictureViewModel.getProjectId());
                customDialog.dismiss();

            }
        });

        customDialog.show();
    }

    public void startBackgroundTask(Context context, WorkerResultReceiver mWorkerResultReceiver, String projectID, boolean isAutoSyncPhotos) {
        if (ProjectDocuUtilities.isNetworkConnected(SavePictureActivity.this) || ProjectNavigator.wlanIsConnected(SavePictureActivity.this)) {


            SyncLocalPhotosService.enqueueWork(context, mWorkerResultReceiver, projectID, isAutoSyncPhotos);
        }
    }

    private class updateAsyncTask extends AsyncTask<WordModel, Void, Void> {
        private WordDao mAsyncTaskDao;

        updateAsyncTask(WordDao dao) {
            mAsyncTaskDao = dao;
        }

        @Override
        protected Void doInBackground(final WordModel... params) {
            mAsyncTaskDao.update(params[0]);
            return null;
        }
    }

    public class updateRecentWordsDataAsyncTask extends AsyncTask<String, Void, List<WordModel>> {
        private long photoId;
        private String projectID;
        private PhotoModel photoModelObj;

        @Override
        protected List<WordModel> doInBackground(String... params) {

            List<WordModel> wordModels = ProjectsDatabase.getDatabase(SavePictureActivity.this).wordDao().getFavoriteWordsSimpleList(params[0]);
            photoId = savePictureViewModel.getmRepository().getPhotoModel().getPdphotolocalId();
            if (wordModels != null && sharedPrefsManager.getBooleanValue(AppConstantsManager.IS_OPEN_FIELD_KEYWORD_CLOCKED, false)) {
                for (int i = 0; i < wordModels.size(); i++) {

                    if (wordModels.get(i).getType() != null && wordModels.get(i).getType().equals("1")) {
                        WordModel wordModel = wordModels.get(i);
                        if (wordModels.get(i).getOpen_field_content() != null && wordModels.get(i).getOpen_field_content().contains(String.valueOf(photoId))) {

                        } else {

                            wordModel.setPhotoIds("," + photoId + "");

                            Utils.showLogger("SavedPictureActivity 2701");
                            wordModel.setOpen_field_content(wordModel.getOpen_field_content());
                            wordModel.setUseCount(wordModel.getUseCount() + 1);
                            wordModel.setPhotoIds(wordModel.getPhotoIds() + "," + photoId);
                            ProjectsDatabase.getDatabase(SavePictureActivity.this).wordDao().update(wordModel);
                        }
                    }
                }
            }
            List<WordModel> wordModelSelected = ProjectsDatabase.getDatabase(SavePictureActivity.this).wordDao().getWordsListIncludesPhotoIdWithTypeZero("%," + photoId + "%", savePictureViewModel.getProjectId());
            if (wordModelSelected != null && wordModelSelected.size() > 0) {
                savePictureViewModel.getPhotoModel().setWordAdded(true);
            } else {
                savePictureViewModel.getPhotoModel().setWordAdded(false);
            }
            return wordModels;
        }

        @Override
        protected void onPostExecute(List<WordModel> wordModels) {
            super.onPostExecute(wordModels);
            //adjustClockedValue(wordModels);
            if (recentUsedWordsViewModel.getAdapter() != null) {
                recentUsedWordsViewModel.getAdapter().wordModels.clear();
                recentUsedWordsViewModel.getAdapter().wordModels.addAll(wordModels);
                Utils.showLogger("onPostExecute>>updateAdapter");
                recentUsedWordsViewModel.getAdapter().setPhotoId(savePictureViewModel.getmRepository().getPhotoModel().getPdphotolocalId());
                recentUsedWordsViewModel.getAdapter().notifyDataSetChanged();
                setDoneIcons();
            }
        }
    }

/*
    public File saveBitmapToFile(File file) {
        try {

            // BitmapFactory options to downsize the image
            BitmapFactory.Options o = new BitmapFactory.Options();
            o.inJustDecodeBounds = true;
            o.inSampleSize = 6;
            // factor of downsizing the image

            FileInputStream inputStream = new FileInputStream(file);
            //Bitmap selectedBitmap = null;
            BitmapFactory.decodeStream(inputStream, null, o);
            inputStream.close();

            // The new size we want to scale to
            final int REQUIRED_SIZE = 75;

            // Find the correct scale value. It should be the power of 2.
            int scale = 1;
            while (o.outWidth / scale / 2 >= REQUIRED_SIZE && o.outHeight / scale / 2 >= REQUIRED_SIZE) {
                scale *= 2;
            }

            BitmapFactory.Options o2 = new BitmapFactory.Options();
            o2.inSampleSize = scale;
            inputStream = new FileInputStream(file);

            Bitmap selectedBitmap = BitmapFactory.decodeStream(inputStream, null, o2);
            inputStream.close();

            // here i override the original image file
            file.createNewFile();
            FileOutputStream outputStream = new FileOutputStream(file);

            selectedBitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);

            return file;
        } catch (Exception e) {
            return null;
        }
    }
*/

    public void activateGPS() {
        // PERMISSION FOR GPS FOR DEVICES WITH ANDROID 6 or higher

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED || Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
//            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
//            checkAndRequestPermissions();
            getLocationPermission();
        } else {

            locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

            //locationManager.addGpsStatusListener(this);
            //locationManager.
            if (locationManager.getBestProvider(createCoarseCriteria(), true) != null && locationManager.getBestProvider(createFineCriteria(), true) != null) {
                low = locationManager.getProvider(locationManager.getBestProvider(createCoarseCriteria(), true));

                high = locationManager.getProvider(locationManager.getBestProvider(createFineCriteria(), true));

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
        gpsTracker = new GPSTracker(this);
        setupCompass();
    }

    public void activateCompass() {
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);

        if (sensorManager != null) {
            sensorManager.registerListener(this, sensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION), SensorManager.SENSOR_DELAY_GAME);
        }
    }

    public void deactivateCompass() {
        if (sensorManager != null) {
            sensorManager.unregisterListener(this);
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
                    Toast.makeText(this, getResources().getString(R.string.app_permission_msg), Toast.LENGTH_LONG).show();
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
            //locationManager.removeGpsStatusListener(this);
            locationManager.removeUpdates(this);
        }

//        deactivateGPSButton();

//        if (gps_button_animation != null) {
        // gps_button_animation.stop();
//        }
    }


    @Override
    public void onLocationChanged(Location location) {
        gpsLocation = location;
//
//                                if (sharedPrefsManager.getBooleanValue(AppConstantsManager.FIND_POSITION_IF_GEO_REFERENCED_PLAN, false) && !sharedPrefsManager.getLastUsedPlanId(this).equals("-1")&&isLocationGetFisrtTime) {
//                            isLocationGetFisrtTime = false;
////                            Toast.makeText(this, "onLocationChanged", Toast.LENGTH_SHORT).show();
//                            new RetrievePlansReferPointAsyncTask(this, getIntent().getStringExtra("projectId"), sharedPrefsManager.getLastUsedPlanId(this)).execute();
//
//
//                        }

//        if (sharedPrefsManager.getBooleanValue(AppConstantsManager.ACTIVATE_GPS, false)) {
//            activateGPS();
//        } else {
//            deactivateGPS();
//        }
//        if(!isLocationGetFisrtTime){

//            if (sharedPrefsManager.getBooleanValue(AppConstantsManager.ACTIVATE_GPS, false) && !sharedPrefsManager.getLastUsedPlanId(this).equals("")){
//                isLocationGetFisrtTime=true;
//            Toast.makeText(this, "onLocationChanged", Toast.LENGTH_SHORT).show();
//            new RetrievePlansReferPointAsyncTask(this, getIntent().getStringExtra("projectId"), sharedPrefsManager.getLastUsedPlanId(this)).execute();
//
//
//        }
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


    public float getCompassDegrees() {
        return compassDegrees;
    }

    boolean isLandscape;

    @Override
    public void onSensorChanged(SensorEvent event) {


        float X_Axis = event.values[0];
        float Y_Axis = event.values[1];

        if ((X_Axis <= 6 && X_Axis >= -6) && Y_Axis > 5) {
            isLandscape = false;
        } else if (X_Axis >= 6 || X_Axis <= -6) {
            isLandscape = true;
        }

        double angle = Math.atan2(X_Axis, Y_Axis) / (Math.PI / 180);

        //compassDegrees = event.values[0];

        // float newvalues = getRotationForCompass(deviceOrientation, compassDegrees);

        //Utils.showLogger("compassDegreeID>>"+newvalues);
        // Log.d("compass_bef_corr", angle + " orient" + deviceOrientation);
        //Log.d("compass_bef_orien", getRotationForCompass(deviceOrientation, compassDegrees) + "");

//        setArrowByCompass(compassDegrees);


    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

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
            plansModelObj = projectsDatabase.plansDao().getPlansUsingPlanID(projectId, planID);
            savePictureViewModel.getPhotoModel().setPlan_id(sharedPrefsManager.getLastUsedPlanId(SavePictureActivity.this));

//            latch.countDown();
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

            runOnUiThread(new Runnable() {
                @Override
                public void run() {

                    int duration = Toast.LENGTH_LONG;

                    // Wenn permanente GPS Verortung angeschaltet ist (GPS Button permanent, Kompass Button permanent, PlanButton in Camera permanent)
                    if (sharedPrefsManager.getBooleanValue(AppConstantsManager.FIND_POSITION_IF_GEO_REFERENCED_PLAN, false) && referPointList != null && referPointList.size() > 0) {

                        if (ProjectDocuUtilities.stringToInt(sharedPrefsManager.getLastUsedPlanId(SavePictureActivity.this)) < 0) {
                            Toast toast = Toast.makeText(SavePictureActivity.this, getResources().getString(R.string.toast_plan_not_loaded), duration);
                            toast.show();
                            return;

                        } else {

                            LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
                            gpsSwitchedOn = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);

                            // Eingestellte GPS-Genauigkeit ermitteln
                            String selectedAutoGpsDistance = sharedPrefsManager.getGpsAccuracy(SavePictureActivity.this);
                            selectedAutoGpsDistance = selectedAutoGpsDistance.substring(0, selectedAutoGpsDistance.length() - 1);

                            Location location = getGpsLocation();
                            if (location == null) {
                                if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                                    // TODO: Consider calling
                                    //    Activity#requestPermissions
                                    // here to request the missing permissions, and then overriding
                                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                                    //                                          int[] grantResults)
                                    // to handle the case where the user grants the permission. See the documentation
                                    // for Activity#requestPermissions for more details.
                                    return;
                                }
                                location = gpsTracker.getLocation();
                                if (location == null) {
                                    location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);

                                }

                            }
                            // Wenn eine gültige Location vorhanden ist...
//                                    if (location != null && gpsSwitchedOn == true) {
//                                        // Wenn permanente GPS Genauigkeit kleiner-gleich reale GPS Genauigkeit ist, dann Geopoint Daten ans Backend senden
//                                        if (!(ProjectDocuUtilities.isNumeric(selectedAutoGpsDistance) && ProjectDocuUtilities.stringToInt(selectedAutoGpsDistance) >= (int) location.getAccuracy())) {
//
//                                            // GPS zu ungenau, springe in den Plan
//                                            //Toast toast = Toast.makeText(((ProjectDocuMainActivity)getActivity()), ((ProjectDocuMainActivity)getActivity()).getResources().getString(R.string.toast_gps_accuraccy_imprecise_info), duration);
//                                            Toast toast = Toast.makeText(SavePictureActivity.this, getResources().getString(R.string.toast_plan_no_auto_gps) + "\nMindestgenauigkeit:" + ProjectDocuUtilities.stringToInt(selectedAutoGpsDistance) + "m\nGPS-Accuracy:" + (int) location.getAccuracy() + "m", duration);
//                                            toast.show();
//                                            showPlanListingScreen();
//                                            return;
////                                            ((ProjectDocuMainActivity) context).setContentFragment(new ProjectDocuPlanListFragment(true, -1));
//                                        }
//                                    } else {
//                                        // keine GPS Location ermittelt, spring in den Plan
//                                        Toast toast = Toast.makeText(SavePictureActivity.this, getResources().getString(R.string.toast_gps_no_signal), duration);
//                                        toast.show();
//
////                                        ((ProjectDocuMainActivity) context).setContentFragment(new ProjectDocuPlanListFragment(true, -1));
//                                        showPlanListingScreen();
//                                        return;
//                                    }
                        }

                        // Eingestellte GPS-Genauigkeit ermitteln
                        String selectedAutoGpsDistance = sharedPrefsManager.getGpsAccuracy(SavePictureActivity.this);
                        selectedAutoGpsDistance = selectedAutoGpsDistance.substring(0, selectedAutoGpsDistance.length() - 1);

                        Location location = getGpsLocation();
                        if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                            // TODO: Consider calling
                            //    Activity#requestPermissions
                            // here to request the missing permissions, and then overriding
                            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                            //                                          int[] grantResults)
                            // to handle the case where the user grants the permission. See the documentation
                            // for Activity#requestPermissions for more details.
                            return;
                        }
                        location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);


                        // Wenn eine gültige Location vorhanden ist...
                        if (location != null && gpsSwitchedOn == true) {
                                    /* Testing Logs
                                    String latlng = sharedPrefsManager.getGpsLatLng(SavePictureActivity.this);
                                    if (latlng != null && !latlng.equals("")) {
                                        latlng = latlng + "#" + location.getLatitude() + " ," + location.getLongitude();
                                    } else {
                                        latlng = location.getLatitude() + " ," + location.getLongitude();
                                    }
                                    sharedPrefsManager.setGpsLatLng(SavePictureActivity.this, latlng);*/
                            // ACHTUNG: Wenn kein Plan geladen wurde, kann auch keine geoPointLocation ermittelt werden
                            GeoPoint geoPointLocation = ProjectDocuUtilities.getPlanLocationFromGps(SavePictureActivity.this, location, plansModelObj, referPointList);

                            // Wenn permanente GPS Genauigkeit kleiner-gleich reale GPS Genauigkeit ist, dann Geopoint Daten ans Backend senden
                            if (ProjectDocuUtilities.isNumeric(selectedAutoGpsDistance) && ProjectDocuUtilities.stringToInt(selectedAutoGpsDistance) >= (int) location.getAccuracy() && geoPointLocation != null) {

                                // Winkel für Verortungs-Pfeil berechnen
                                int northDeviationAngle = 0;
                                float rotationAngleForBackend = 0;
                                if (plansModelObj != null) {
                                    northDeviationAngle = -plansModelObj.degree;
                                }

                                float compassDegree = getCompassDegrees();

                                //float fixedDegree = 270.0f + (compassDegree + 180.0f) + northDeviationAngle;
                                // fix for wrong angle degree for backend
                                float fixedDegree = 270.0f + (compassDegree + 180.0f) + northDeviationAngle;

                                if (fixedDegree > 0) {
                                    rotationAngleForBackend = fixedDegree;
//System.out.println("###### Storing Degree in Camera Preview: fixed Degree available");
                                } else {
                                    rotationAngleForBackend = 360.0f + fixedDegree;
//System.out.println("###### Storing Degree in Camera Preview: fixed Degree NOT available");
                                }

                                // Bberechne 2 Punkte für Pfeillänge
                                double y = (Math.cos(rotationAngleForBackend * (Math.PI / 180)) * 100);
                                double x = (Math.sin(rotationAngleForBackend * (Math.PI / 180)) * 100);

                                int viewX = (int) x;
                                int viewY = (int) y;

                                // x und y Koordinaten auf dem Plan
                                int xcoord = (int) geoPointLocation.x;
                                int ycoord = (int) geoPointLocation.y;

//System.out.println("###### Storing Degree in Camera Preview: " + rotationAngleForBackend);

//                                        projectDocuDatabaseManager.insertIntoFlags(0, null, ((ProjectDocuMainActivity) context).currentPlanId, 0, "", "", 0, "", (int) rotationAngleForBackend, viewX, viewY, ycoord, xcoord, localPhotoId, "com\\projectdocu\\FlagVO", projectDocuDatabaseManager.FLAG_SYNC_STATUS_NOT_SYNCED);
                                Pdflawflag pdflawflagOBJ = new Pdflawflag();
                                pdflawflagOBJ.setLocal_photo_id(photoId);
                                pdflawflagOBJ.setPdProjectid(projectId);
                                pdflawflagOBJ.setPdplanid(planID);

                                pdflawflagOBJ.setXcoord((int) xcoord - 5 + "");
                                pdflawflagOBJ.setYcoord((int) ycoord - 5 + "");
//        pdflawflagOBJ.setXcoord((int) planWebview.crosshairPositionX + "");
//        pdflawflagOBJ.setYcoord((int) planWebview.crosshairPositionY + "");
                                pdflawflagOBJ.setViewx(viewX + "");
                                pdflawflagOBJ.setViewy(viewY + "");
                                pdflawflagOBJ.setScale_factor(0.9999f);
                                pdflawflagOBJ.setDegree((int) Math.round((int) rotationAngleForBackend));
                                pdflawflagOBJ.setIs_arrow_located(0);
                                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.GERMANY);
                                String photoDate = simpleDateFormat.format(new Date());
                                pdflawflagOBJ.setCreated(photoDate);

                                new CreateOrUpdateLocalFlawFlag().execute(pdflawflagOBJ);

                                Toast toast = Toast.makeText(SavePictureActivity.this, getResources().getString(R.string.toast_plan_auto_gps_located), duration);
                                toast.show();

                            } else {
                                Toast toast = Toast.makeText(SavePictureActivity.this, getResources().getString(R.string.toast_plan_no_auto_gps) + "\nMindestgenauigkeit:" + ProjectDocuUtilities.stringToInt(selectedAutoGpsDistance) + "m\nGPS-Accuracy:" + (int) location.getAccuracy() + "m", duration);
                                toast.show();
                                showPlanListingScreen();
                                return;
                            }
                            // sonst Warnmeldung ausgeben das Auto GPS nicht möglich ist
//                                    else if (geoPointLocation == null) {
//                                        Toast toast = Toast.makeText(SavePictureActivity.this, getResources().getString(R.string.toast_gps_no_signal), duration);
//                                        toast.show();
//                                    } else {
//                                        Toast toast = Toast.makeText(SavePictureActivity.this, getResources().getString(R.string.toast_plan_no_auto_gps) + "\nMindestgenauigkeit:" + ProjectDocuUtilities.stringToInt(selectedAutoGpsDistance) + "m\nGPS-Accuracy:" + (int) location.getAccuracy() + "m", duration);
//                                        toast.show();
//                                    }
                        } else {
                            Toast toast = Toast.makeText(SavePictureActivity.this, getResources().getString(R.string.toast_gps_no_signal), duration);
                            toast.show();

//                                        ((ProjectDocuMainActivity) context).setContentFragment(new ProjectDocuPlanListFragment(true, -1));
                            showPlanListingScreen();
                        }
                    }
                    // Temporäre (letzte) Verortung an Backend senden
                    else {

//                                Flags flag = new Flags();
//                                flag = projectDocuDatabaseManager.selectDataFromFlagsForCurrentPhoto(((ProjectDocuMainActivity) context).currentPlanId, -1);
//
//                                if (flag.xcoord != null && (Integer) projectDocuDatabaseManager.selectDataFromPreferences(ProjectDocuDatabaseManager.COLUMN_PREFERENCES_IS_LOCATED) == 1) {
//                                    int rotationAngleForBackend = flag.degree;
//                                    int viewX = flag.viewx;
//                                    int viewY = flag.viewy;
//
//                                    projectDocuDatabaseManager.insertIntoFlags(0, null, ((ProjectDocuMainActivity) context).currentPlanId, 0, "", "", 0, "", rotationAngleForBackend, viewX, viewY, (int) flag.ycoord, (int) flag.xcoord, localPhotoId, "com\\projectdocu\\FlagVO", projectDocuDatabaseManager.FLAG_SYNC_STATUS_NOT_SYNCED);
//                                    // 180,0,100 => Keine Pfeilverortung! also 180 Grad default und länge 100
//                                    // projectDocuDatabaseManager.insertIntoFlags(0, null, ((ProjectDocuMainActivity) context).currentPlanId, 0, "", "", 0, "", 180, 0, 100, (int)flag.ycoord, (int)flag.xcoord, localPhotoId, "com\\projectdocu\\FlagVO", projectDocuDatabaseManager.FLAG_SYNC_STATUS_NOT_SYNCED);
//                                }
                    }
                }

            });

            System.gc();
            //yield();
            deactivateGPS();
        }
    }

    private class CreateOrUpdateLocalFlawFlag extends AsyncTask<Pdflawflag, Void, Void> {
        private DefectsDao mAsyncTaskDao;

        CreateOrUpdateLocalFlawFlag() {
        }

        @Override
        protected Void doInBackground(final Pdflawflag... params) {

            Pdflawflag pdflawflagg = params[0];
            ProjectsDatabase.getDatabase(SavePictureActivity.this).pdFlawFLagDao().insert(params[0]);

            plansModelObj.setDegree((int) Math.round(pdflawflagg.degree));
            plansModelObj.setXcoord(Integer.valueOf(pdflawflagg.getXcoord()));
            plansModelObj.setYcoord(Integer.valueOf(pdflawflagg.getYcoord()));
            plansModelObj.setViewx(Integer.valueOf(pdflawflagg.getViewx()));
            plansModelObj.setViewy(Integer.valueOf(pdflawflagg.getViewy()));
            plansModelObj.setScale_factor(pdflawflagg.getScale_factor());
            plansModelObj.setIs_arrow_located(pdflawflagg.getIs_arrow_located());
            sharedPrefsManager.setLastUsedPlanId(SavePictureActivity.this, pdflawflagg.getPdplanid());
            ProjectsDatabase.getDatabase(SavePictureActivity.this).plansDao().update(plansModelObj);

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

            savePictureViewModel.getPhotoModel().setPlanAdded(true);
            savePictureViewModel.getPhotoModel().setPlan_id(plansModelObj.getPlanId());
            savePictureViewModel.getPhotoModel().setPhotoSynced(false);
            savePictureViewModel.getPhotoModel().setPhotoUploadStatus(LocalPhotosRepository.UN_SYNC_PHOTO);
            isPhotoUpdate = true;
            new updatePhotoAsyncTask(savePictureViewModel.getPhotoModel().getPdphotolocalId(), savePictureViewModel.getProjectId(), savePictureViewModel.getPhotoModel()).execute();

        }
    }


    private class CreateLocalDefectAsyncTask extends AsyncTask<Void, Void, Void> {
        boolean isQuick;

        public CreateLocalDefectAsyncTask(boolean isQuick) {
            this.isQuick = isQuick;

        }

        @Override
        protected Void doInBackground(final Void... params) {
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            Date date = new Date();
            ProjectsDatabase db = ProjectsDatabase.getDatabase(SavePictureActivity.this);
            PdFlawFlagRepository pdFlawFlagRepository = new PdFlawFlagRepository(SavePictureActivity.this);
            DefectTradesRepository mRepositoryDefecttrade = new DefectTradesRepository(SavePictureActivity.this, projectID);
            SharedPrefsManager sharedPrefsManager = new SharedPrefsManager(SavePictureActivity.this);
            final Calendar c = Calendar.getInstance();
            int yearCurrent = c.get(Calendar.YEAR);
            int monthCurrent = c.get(Calendar.MONTH);
            int dayCurrent = c.get(Calendar.DAY_OF_MONTH);
            String creator = sharedPrefsManager.getStringValue(AppConstantsManager.USER_FIRST_NAME, "") + " " + sharedPrefsManager.getStringValue(AppConstantsManager.USER_LAST_NAME, "");
            String createdDate = yearCurrent + "-" + (monthCurrent + 1) + "-" + dayCurrent;
            String dateString1 = sharedPrefsManager.getStringValue(AppConstantsManager.PD_USER_ID, "");
//            DefectsModel defectsModel = new DefectsModel("", "", projectID, dateString1, "", "", createdDate,
//                    "2", createdDate, "", dateString1, "", "", "", "", "", "", dateString1);


            String defectName = "";
            if (savePictureViewModel.getPhotoModel() != null && savePictureViewModel.getPhotoModel().getDescription() != null && !savePictureViewModel.getPhotoModel().getDescription().equals("")) {
                defectName = savePictureViewModel.getPhotoModel().getDescription();
            }
            DefectsModel defectsModel = new DefectsModel("", "", projectID, dateString1, "", defectName, dateFormat.format(date), "2", creator, "", dateFormat.format(date), "", "", dateFormat.format(date), "0000-00-00 00:00:00", "0000-00-00 00:00:00", "0000-00-00 00:00:00", dateString1, "");
            defectsModel.setCreateDate_df(date.getTime());
            defectsModel.setRunidInt(0);
            defectsModel.setDeleted("0");

            Calendar calendar = Calendar.getInstance();
            calendar.add(Calendar.DAY_OF_MONTH, 14);


            if (isQuick) {
                SimpleDateFormat simpleDateFormat2;

                SimpleDateFormat nameDateFormat;


                boolean isGermenLanguage = false;

                if (sharedPrefsManager.getStringValue(AppConstantsManager.APP_LANGUAGE, "de").equals("de")) {
                    isGermenLanguage = true;
                    simpleDateFormat2 = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");
                    nameDateFormat = new SimpleDateFormat("dd.MM.yyyy - HH:mm");
                } else {
                    isGermenLanguage = false;
                    simpleDateFormat2 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    nameDateFormat = new SimpleDateFormat("yyyy-MM-dd - HH:mm");

                }

                //SimpleDateFormat userDate = new SimpleDateFormat("dd MMM, yyyy");

                String deadLineStr = simpleDateFormat2.format(calendar.getTime());

                String description = savePictureViewModel.getPhotoModel().getDescription();

                String currentDate;

                currentDate = simpleDateFormat2.format(new Date());

                String myDate = nameDateFormat.format(new Date());


                defectsModel.setFristDate(deadLineStr);
                defectsModel.setFristdate_df(calendar.getTime().getTime());
                if (description == null || description.isEmpty()) {
                    if (isGermenLanguage)
                        defectsModel.setDefectName("Mangel vom " + myDate + " Uhr");
                    else defectsModel.setDefectName("Defect from " + myDate);
                } else defectsModel.setDefectName(description);


                defectsModel.setDefectType("1");
                defectsModel.setStatus("2");

                // defectsModel.setDefectTradeModelList();
            }


            local_flaw_id = db.defectsDao().insert(defectsModel);
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            defectTradesRepository.addLocalMangelGewerk(defectTradesRepository.getmDefectsTradeDao(), projectID, local_flaw_id + "");

            if (savePictureViewModel.getPhotoModel().isPlanAdded()) {

                pdFlawFlagRepository = new PdFlawFlagRepository(SavePictureActivity.this, projectID);

                flawFlagObj = pdFlawFlagRepository.getmDefectsPhotoDao().getFlawFlagObjUsingPhotoID(projectID, sharedPrefsManager.getLastUsedPlanId(SavePictureActivity.this), savePictureViewModel.getPhotoModel().getPdphotolocalId() + "");

                if (flawFlagObj != null) {

                    flawFlagObj.setLocal_flaw_Id(local_flaw_id + "");
                    flawFlagObj.setFlaw_Id(local_flaw_id + "");
                    flawFlagObj.setLocal_photo_id(savePictureViewModel.getPhotoModel().getPdphotolocalId() + "");
//                    flawFlagObj.setPdFlawFlagServerId("");
                    pdFlawFlagRepository.getmDefectsPhotoDao().insert(flawFlagObj);


                }
            }
            savePictureViewModel.getPhotoModel().setDefectAdded(true);
            savePictureViewModel.getPhotoModel().setLocal_flaw_id(local_flaw_id + "");

            new updatePhotoAsyncTask(isQuick, savePictureViewModel.getPhotoModel().getPdphotolocalId(), savePictureViewModel.getProjectId(), savePictureViewModel.getPhotoModel(), true).execute();


            return null;
        }
    }


    /**
     *
     * Entfernt die ProgressBar vom Bildschirm
     *
     */

    /**
     * Setzt den Status zur&uuml;ck nachdem ein Foto gespeichert wurde, so dass die App bereit für das n&auml;chste Foto ist
     */
    public void resetAppSavingPhotoStatus() {
        preview.app_is_saving_photo = false;
    }

    /**
     * Setzt den Kamera Focus
     *
     * @param makePhoto Boolean Wert der angibt ob die App bereit ist ein Foto zu machen
     */
    public void setCameraFocus(final boolean makePhoto) {
        preview.setCameraFocus(makePhoto);
    }

    /**
     * Aktiviert Zoom In
     */
    public void zoomIn() {
        preview.zoomIn();
    }

    /**
     * Aktiviert Zoom out
     */
    public void zoomOut() {
        preview.zoomOut();
    }

    /**
     *
     * Gibt die Instanz der ProjectDocuTagOverlay Klasse zum Anzeigen von Tags &uuml;ber dem Foto-Preview zur&uuml;ck
     *
     * @return Instanz der ProjectDocuTagOverlay Klasse
     *
     *
     */

    /**
     * Gibt die in der Datenbank gespeicherte Id des aktuellen Fotos zur&uuml;ck
     *
     * @return Id des aktuellen Fotos
     */
    public int getLocalPhotoId() {
        return preview.getLocalPhotoId();
    }

    /**
     * Schaltet den Blitz der Kamera ein oder aus
     */
    public void switchFlash() {
//        preview.switchFlash(ivFlash);
    }


    private class UpdateFlawAsyncTask extends AsyncTask<String, Void, Void> {
        private DefectsDao defectsDao;
        List<String> stringList = new ArrayList<>();
        ProjectsDatabase projectsDatabase;
        boolean isFromDefectListing;

        UpdateFlawAsyncTask(Context context) {
            projectsDatabase = ProjectsDatabase.getDatabase(context);
            defectsDao = projectsDatabase.defectsDao();
        }

        @Override
        protected Void doInBackground(final String... params) {

            if (params[0] != null && !params[0].equals("")) {

                DefectsModel defectsModel = defectsDao.getDefectsOBJ(projectID, params[0]);
                if (defectsModel != null) {
                    defectsModel.setUploadStatus(DefectRepository.UPLOADING_PHOTO);
                    defectsDao.update(defectsModel);
                }
            }

            return null;
        }


    }

    @Override
    public void onShowProgressBar() {
        if (pb_loader != null && pb_loader.getVisibility() == View.VISIBLE) {
            pb_loader.setVisibility(View.GONE);
        } else pb_loader.setVisibility(View.VISIBLE);
    }

    //region onConfigurationChanged
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        // Log.d("ITEST", "onConfigurationChanged: ");

        int newOrientation = newConfig.orientation;
        //Log.d(TAG, "onConfigurationChanged: " + newOrientation);


//        onScreenOrientationChanged(newOrientation);


//        if (newOrientation == Configuration.ORIENTATION_LANDSCAPE) {
//            // Do certain things when the user has switched to landscape.
//        }
//                    Toast.makeText(SavePictureActivity.this,"onConfigurationChanged callback"+newOrientation, Toast.LENGTH_SHORT).show();
//        onOrientationChanged(newOrientation);
    }
    //endregion

    public int mainRotation = 0;

    //region startOrientationChangeListener
    private void startOrientationChangeListener() {
        mOrEventListener = new OrientationEventListener(this, SensorManager.SENSOR_DELAY_NORMAL) {
            @Override
            public void onOrientationChanged(int orientation) {
                //  Utils.showLogger2("onOrientationChanged>>"+orientation);
//
////                Toast.makeText(SavePictureActivity.this, "Device Or"+getResources().getConfiguration().orientation+"Screen Or"+rotation,Toast.LENGTH_SHORT).show();
//                Log.d("ITEST", "rotation: " + rotation);
//
//                if (((rotation >= 0) && (rotation <= 45)) || (rotation > 315)) {
//                    rotation = 0;
//                } else if ((rotation > 45) && (rotation <= 135)) {
//                    rotation = 90;
//                } else if ((rotation > 135) && (rotation <= 225)) {
//                    rotation = 180;
//                } else if ((rotation > 225) && (rotation <= 315)) {
//                    rotation = 270;
//                } else {
//                    rotation = 0;
//                }
//
//
//
//                mOrEventListener.enable();
//                deviceOrientation = rotation;
////                Toast.makeText(SavePictureActivity.this, "onOrientationChanged "+rotation, Toast.LENGTH_SHORT).show();
//
//                onScreenOrientationChanged(rotation);

                if ((orientation <= 5 && orientation >= 0) || (orientation <= 360 && orientation >= 355)) {
                    if (mRotation != 0) {
                        if (mRotation == 90) {
                            mRotationHelper.rotate(-90, 0, iconsListBeforeTakingPhoto);
                            mRotationHelper.rotate(-90, 0, iconsListAfterTakingPhoto);

                        } else if (mRotation == 270) {
                            mRotationHelper.rotate(90, 0, iconsListBeforeTakingPhoto);
                            mRotationHelper.rotate(90, 0, iconsListAfterTakingPhoto);


                        } else {
                            mRotationHelper.rotate(0, 0, iconsListBeforeTakingPhoto);
                            mRotationHelper.rotate(0, 0, iconsListAfterTakingPhoto);

                        }
                        mRotation = 0;
                    }
                } else if (orientation <= 95 && orientation >= 85) {
                    if (mRotation != 90) {
                        if (mRotation == 0 || mRotation == 180) {
                            mRotationHelper.rotate(360, 270, iconsListBeforeTakingPhoto);
                            mRotationHelper.rotate(360, 270, iconsListAfterTakingPhoto);
                        } else {
                            mRotationHelper.rotate(0, 270, iconsListBeforeTakingPhoto);
                            mRotationHelper.rotate(0, 270, iconsListAfterTakingPhoto);
                        }
                        mRotation = 90;
                    }
                } else if (orientation <= 185 && orientation >= 175) {
                    if (mRotation != 180) {
                        if (mRotation == 90) {
                            mRotationHelper.rotate(270, 180, iconsListBeforeTakingPhoto);
                            mRotationHelper.rotate(270, 180, iconsListAfterTakingPhoto);
                        } else if (mRotation == 270) {
                            mRotationHelper.rotate(90, 180, iconsListBeforeTakingPhoto);
                            mRotationHelper.rotate(90, 180, iconsListAfterTakingPhoto);
                        } else {
                            mRotationHelper.rotate(0, 180, iconsListBeforeTakingPhoto);
                            mRotationHelper.rotate(0, 180, iconsListAfterTakingPhoto);
                        }
                        mRotation = 180;
                    }
                } else if (orientation <= 275 && orientation >= 265) {
                    if (mRotation != 270) {
                        if (mRotation == 180) {
                            mRotationHelper.rotate(180, 90, iconsListBeforeTakingPhoto);
                            mRotationHelper.rotate(180, 90, iconsListAfterTakingPhoto);
                        } else {
                            mRotationHelper.rotate(0, 90, iconsListBeforeTakingPhoto);
                            mRotationHelper.rotate(0, 90, iconsListAfterTakingPhoto);
                        }
                        mRotation = 270;
                    }
                }

                //Utils.showLogger2("SavePictureActivity>>>" + mRotation);

                switch (mRotation) {
                    case 0:
                        myorientation = MYORIENTATION.PORTRAIT;
                        break;
                    case 270:
                        myorientation = MYORIENTATION.REVERSE_LANDSCAPE;
                        break;
                    case 90:
                        myorientation = MYORIENTATION.LANDSCAPE;

                }

                showHideIcons(mRotation);

            }

        };

    }
    //endregion

    //region onScreenOrientationChanged
    public void onScreenOrientationChanged(int orientation) {
        //Log.d("ITEST", "onScreenOrientationChanged: " + orientation);
        Context context = this;
        if (orientation == OrientationEventListener.ORIENTATION_UNKNOWN) {
            return;
        }

        orientation = (orientation + 45) / 90 * 90;
        if (orientation >= 360) {
            orientation = 0;
        }

        deviceOrientation = orientation;
        //Log.d("THIS_ORIENTATION", "MyOrientation: " + orientation);

        WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        Configuration config = getResources().getConfiguration();

        int rotation = windowManager.getDefaultDisplay().getRotation();

        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        screenHeight = displayMetrics.heightPixels;
        screenWidth = displayMetrics.widthPixels;

        if (rotation == Surface.ROTATION_0) {
            Log.d("ITEST", "Surface.ROTATION_0");
            if (orientation == 0 || orientation == 360) {
                rotateIcons = 0;
            } else if (orientation == 90) {
                rotateIcons = -90;
            } else if (orientation == 180) {
                rotateIcons = 180;
            } else if (orientation == 270) {
                rotateIcons = 90;
            } else {
                rotateIcons = 0;
            }
        } else {
            Log.d("ITEST", "Surface.ROTATION_0 else");
            if (orientation == 0 || orientation == 360) {
                rotateIcons = -90;
            } else if (orientation == 90) {
                rotateIcons = 180;
            } else if (orientation == 180) {
                rotateIcons = 90;
            } else if (orientation == 270) {
                rotateIcons = 0;
            } else {
                rotateIcons = -90;
            }
        }


        if (((rotation == Surface.ROTATION_0 || rotation == Surface.ROTATION_180) && config.orientation == Configuration.ORIENTATION_LANDSCAPE) || ((rotation == Surface.ROTATION_90 || rotation == Surface.ROTATION_270) && config.orientation == Configuration.ORIENTATION_PORTRAIT)) {
            if (deviceOrientation == 0 || deviceOrientation == 180) {
                tagOrientation = LinearLayout.HORIZONTAL;
            } else {
                tagOrientation = LinearLayout.VERTICAL;
            }
        } else {
            if (deviceOrientation == 0 || deviceOrientation == 180) {
                tagOrientation = LinearLayout.VERTICAL;
            } else {
                tagOrientation = LinearLayout.HORIZONTAL;
            }
        }


//        if (orientation == oldOrientation && System.currentTimeMillis() - lastRotationTime > 3000) {
//            return;
//        }

        oldOrientation = orientation;

        try {
            WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
            Display display = wm.getDefaultDisplay();
//            Toast.makeText(SavePictureActivity.this, orientation+"", Toast.LENGTH_SHORT).show();

            if (((display.getOrientation() == Surface.ROTATION_0 || display.getOrientation() == Surface.ROTATION_180) && config.orientation == Configuration.ORIENTATION_LANDSCAPE) || ((display.getOrientation() == Surface.ROTATION_90 || display.getOrientation() == Surface.ROTATION_270) && config.orientation == Configuration.ORIENTATION_PORTRAIT)) {

                if (orientation == 0 || orientation == 360) {
                    iv_camera.setRotation(0);
                    iv_flash.setRotation(0);
                    tv_close_camera.setRotation(0);

                } else if (orientation == 90) {
                    iv_camera.setRotation(90);
                    iv_flash.setRotation(90);
                    tv_close_camera.setRotation(90);

                } else if (orientation == 180) {

                } else if (orientation == 270) {
                    iv_camera.setRotation(270);
                    iv_flash.setRotation(270);
                    tv_close_camera.setRotation(270);

                }

            } else {
                if (orientation == 0 || orientation == 360) {
                    Log.d("THIS_ORIENTATION", "In orientation == 0 check: ");
                    rl_parent_save_picture.setVisibility(View.VISIBLE);
                    isLandScape = false;
                    if (iv_camera != null) iv_camera.setRotation(0);
                    if (iv_flash != null) iv_flash.setRotation(0);
                    if (tv_close_camera != null) tv_close_camera.setRotation(0);
                    if (ll_icons != null) ll_icons.setRotation(0);
                    if (rl_word != null) rl_word.setRotation(0);
                    if (rl_comment != null) rl_comment.setRotation(0);
                    if (rl_plan != null) rl_plan.setRotation(0);
                    if (rl_defects != null) rl_defects.setRotation(0);
                    if (rl_record != null) rl_record.setRotation(0);
                    if (rl_brush != null) rl_brush.setRotation(0);
                    if (recyclerView != null) {
                        recyclerView.setRotation(0);
                    }
                    if (simpleList != null) simpleList.setRotation(0);

                    if (toolbar != null) toolbar.setRotation(0);

                    if (toolbar != null) toolbar.setVisibility(View.VISIBLE);
                    if (toolbarRotated != null) toolbarRotated.setVisibility(View.GONE);

                    if (simpleList != null) {
                        ViewGroup.LayoutParams pSimpleList = simpleList.getLayoutParams();
                        if (pSimpleList instanceof RelativeLayout.LayoutParams) {
                            RelativeLayout.LayoutParams lpp = (RelativeLayout.LayoutParams) pSimpleList;
                            lpp.setMargins(0, 0, 16, 20);
                            simpleList.setLayoutParams(lpp);
                        }
                    }

                    if (captured_image != null) captured_image.setRotation(0);
                    if (productsView != null) productsView.setRotation(0);

                    if (productsView != null) {
                        productsView.setRotation(0);
                        FrameLayout.LayoutParams lpparent = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.WRAP_CONTENT);
                        lpparent.gravity = Gravity.CENTER;
                        productsView.setLayoutParams(lpparent);
                    }

                    if (!isViewMode) {
                        if (cameraPreview != null && cameraPreview.getVisibility() != View.VISIBLE) {
                            if (iv_flash != null) iv_flash.setVisibility(View.INVISIBLE);
                            if (iv_delete != null) iv_delete.setVisibility(View.VISIBLE);
                            if (iv_delete_rotated != null)
                                iv_delete_rotated.setVisibility(View.GONE);
                        } else {
                            if (iv_flash != null) iv_flash.setVisibility(View.VISIBLE);
                            if (iv_delete != null) iv_delete.setVisibility(View.GONE);
                            if (iv_delete_rotated != null)
                                iv_delete_rotated.setVisibility(View.GONE);
                        }
                    }

                } else if (orientation == 270) {
                    Log.d("THIS_ORIENTATION", "In orientation == 270 check: ");
                    rl_parent_save_picture.setVisibility(View.VISIBLE);
                    isLandScape = true;
                    if (iv_camera != null) iv_camera.setRotation(90);
                    if (iv_flash != null) iv_flash.setRotation(90);
                    if (tv_close_camera != null) tv_close_camera.setRotation(90);
                    if (ll_icons != null) ll_icons.setRotation(180);
                    if (rl_word != null) rl_word.setRotation(270);
                    if (rl_comment != null) rl_comment.setRotation(270);
                    if (rl_plan != null) rl_plan.setRotation(270);
                    if (rl_defects != null) rl_defects.setRotation(270);
                    if (rl_record != null) rl_record.setRotation(270);
                    if (rl_brush != null) rl_brush.setRotation(270);

                    if (recyclerView != null) {
                        recyclerView.setRotation(90);

                        ViewGroup.LayoutParams pSimpleList = recyclerView.getLayoutParams();
//                    if (p instanceof ListView.LayoutParams) {
                        RelativeLayout.LayoutParams lpp = (RelativeLayout.LayoutParams) pSimpleList;
                        lpp.setMargins(180, 300, 180, 300);
                        recyclerView.setLayoutParams(lpp);
                    }
                    if (simpleList != null) simpleList.setRotation(90);
                    if (captured_image != null) captured_image.setRotation(90);
                    if (toolbar != null) toolbar.setVisibility(View.GONE);
                    toolbarRotated.setVisibility(View.VISIBLE);

                    if (!isViewMode) {
                        if (cameraPreview != null && cameraPreview.getVisibility() != View.VISIBLE) {
                            if (iv_flash != null) iv_flash.setVisibility(View.GONE);
                            if (iv_delete != null) iv_delete.setVisibility(View.GONE);
                            if (iv_delete_rotated != null)
                                iv_delete_rotated.setVisibility(View.VISIBLE);
                        } else {
                            if (iv_flash != null) iv_flash.setVisibility(View.VISIBLE);
                            if (iv_delete != null) iv_delete.setVisibility(View.GONE);
                            if (iv_delete_rotated != null)
                                iv_delete_rotated.setVisibility(View.GONE);
                        }

                    }

                    if (productsView != null) {
                        productsView.setRotation(90);
                        FrameLayout.LayoutParams lpparent = new FrameLayout.LayoutParams(1000, 1200);
                        lpparent.gravity = Gravity.CENTER;
                        lpparent.rightMargin = 350;
                        productsView.setLayoutParams(lpparent);
                    }
                    ViewGroup.LayoutParams pSimpleList = simpleList.getLayoutParams();
//                    if (p instanceof ListView.LayoutParams) {
                    RelativeLayout.LayoutParams lpp = (RelativeLayout.LayoutParams) pSimpleList;
                    lpp.setMargins(0, 0, 16, 200);
                    simpleList.setLayoutParams(lpp);
                } else if (orientation == 90) {
                    //       Log.d("THIS_ORIENTATION", "In orientation == 90 check: ");
                    isLandScape = true;
                    iv_camera.setRotation(270);
                    iv_flash.setRotation(270);
                    tv_close_camera.setRotation(270);
                    reverseLandscapeHandling();

                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }


    }
    //endregion

    //region reverseLandscapeHandling
    private void reverseLandscapeHandling() {
        if (cameraPreview.getVisibility() != View.VISIBLE) {
            if (productsView != null) {

                if (productsView != null) {
                    productsView.setRotation(270);
                    FrameLayout.LayoutParams lpparent = new FrameLayout.LayoutParams(1200, 1350);
                    lpparent.gravity = Gravity.CENTER;
                    lpparent.leftMargin = 300;
                    lpparent.topMargin = 0;

                    productsView.setLayoutParams(lpparent);
                }
            }
            rl_parent_save_picture.setVisibility(View.GONE);
            if (!isViewMode) {
                if (isLandScape) {
                } else {
                    iv_flash.setVisibility(View.INVISIBLE);
                }
            }

        } else {
            rl_parent_save_picture.setVisibility(View.VISIBLE);
        }

    }
    //endregion

    private void getLocationPermission() {
        /*
         * Request location permission, so that we can get the location of the
         * device. The result of the permission request is handled by a callback,
         * onRequestPermissionsResult.
         */
        if (ContextCompat.checkSelfPermission(this.getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
        }
    }

    public float setArrowByCompass(float compassDegree) {


        // corrects the compassDegree by the settings value (just need for some devices)
//		float compassCorrectionValue =  getCompassCorrectionValue();
        float compassCorrectionValue = 0;
        float fixedDegree = 0;
//         if(rotateIcons>0){
//              fixedDegree = (compassDegree)  - rotateIcons ;
//         }else{
//             fixedDegree = (compassDegree)  + rotateIcons ;
//         }

        fixedDegree = 270.0f + (compassDegree + 180.0f) + rotateIcons;
        //		float fixedDegree = 270.0f + (compassDegree + 180.0f) + northDeviationAngle;

        if (fixedDegree > 0) {
            compassCorrectionValue = fixedDegree;
        } else {
            compassCorrectionValue = 360.0f + fixedDegree;
        }


        // Log.d("compass_af_corr", fixedDegree + "");
        //   Log.d("compass_af_corr", fixedDegree + "");

        return compassCorrectionValue;

    }


    private void registerOrientationSensorForUpdates(Boolean register) {
        if (register) {
            mSensorManager.registerListener(mEventListener, mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SENSOR_DELAY_NORMAL);
            mSensorManager.registerListener(mEventListener, mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD), SENSOR_DELAY_NORMAL);
            mSensorManager.registerListener(mEventListener, mSensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION), SENSOR_DELAY_NORMAL);
        } else {
            mSensorManager.unregisterListener(mEventListener);
        }
    }


    private String getRotatedBitmapFromGallary(Context context, Bitmap bitmap) {


        String filePath = "";

        Bitmap originalBitmap = null;
        Date date2 = new Date();

        File dir = context.getExternalFilesDir("/projectDocu/captured_photos");
        if (dir == null) {
            dir = context.getFilesDir();
        }
        if (!dir.isDirectory()) {
            dir.mkdirs();
        }
        File photo = new File(dir, "/PD_" + new Date().getTime() + ".jpg");
        SimpleDateFormat photoNameDateFormat = new SimpleDateFormat("yyyyMMdd_HHmmss");
        String photoName = "PD_" + photoNameDateFormat.format(date2) + ".jpg";
//        try {
//            originalBitmap = getRotatedBitmapGallary(bitmap);
        originalBitmap = bitmap;
//        } catch (IOException e) {
//            e.printStackTrace();
//        }

        FileOutputStream fOut = null;
        try {
            fOut = new FileOutputStream(photo);

            SharedPrefsManager sharedPrefsManager = new SharedPrefsManager(context);
            int photoQuality = 100;
//            if (!sharedPrefsManager.getStringValue(AppConstantsManager.USER_SELECTED_CAMERA_RESOLUTION, "100").equals("")) {
//                photoQuality = Integer.valueOf(sharedPrefsManager.getStringValue(AppConstantsManager.USER_SELECTED_CAMERA_RESOLUTION, "100"));
//            }

            originalBitmap.compress(Bitmap.CompressFormat.JPEG, photoQuality, fOut);
            fOut.flush();
            fOut.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        ExifInterface exifInterface = null;
        try {
            exifInterface = new ExifInterface(photo.getAbsolutePath());


            switch (Integer.valueOf(exifInterface.getAttribute(ExifInterface.TAG_ORIENTATION))) {
                case 0:
                    exifInterface.setAttribute(ExifInterface.TAG_ORIENTATION, String.valueOf(ExifInterface.ORIENTATION_ROTATE_180));
                    break;
//                case 90:
//                    exifInterface.setAttribute(ExifInterface.TAG_ORIENTATION, String.valueOf(ExifInterface.ORIENTATION_ROTATE_90));
//                    break;
//                case 180:
//                    exifInterface.setAttribute(ExifInterface.TAG_ORIENTATION, String.valueOf(ExifInterface.ORIENTATION_ROTATE_180));
//                    break;
//                case 270:
//                    exifInterface.setAttribute(ExifInterface.TAG_ORIENTATION, String.valueOf(ExifInterface.ORIENTATION_ROTATE_270));
//                    break;
            }

            exifInterface.saveAttributes();
        } catch (IOException e) {
            e.printStackTrace();
        }


        if (photo != null && photo.getAbsolutePath() != null) filePath = photo.getAbsolutePath();
//                                        mPhotoCaptureCallback.onFinishSuccess("/storage/emulated/0/Android/data/com.test.projectdocu.debug/files/projectDocu/captured_photos/PD_1589533842044.jpg");
        return filePath;
    }

    private Bitmap getRotatedBitmapGallary(Bitmap bitmap) throws IOException {
//        ExifInterface ei = new ExifInterface(path.getAbsolutePath());
//        int orientation = ei.getAttributeInt(ExifInterface.TAG_ORIENTATION,
//                ExifInterface.ORIENTATION_UNDEFINED);
        int orientation = 6;

        Bitmap rotatedBitmap = null;
        switch (orientation) {
            case ExifInterface.ORIENTATION_ROTATE_90:
                rotatedBitmap = rotateImage(bitmap, 90);
                break;
            case ExifInterface.ORIENTATION_ROTATE_180:
                rotatedBitmap = rotateImage(bitmap, 180);
                break;
            case ExifInterface.ORIENTATION_ROTATE_270:
                rotatedBitmap = rotateImage(bitmap, 270);
                break;
            case ExifInterface.ORIENTATION_NORMAL:
            default:
                rotatedBitmap = bitmap;
        }
        return rotatedBitmap;
    }


    private Bitmap getRotatedBitmapWithInputStream(Bitmap bitmap, ExifInterface exifInterface) throws IOException {
        int photoWidth = 0;
        int photoHeight = 0;

//        ExifInterface exifInterface = new ExifInterface(path);
        int orientation = exifInterface.getAttributeInt(ExifInterface.TAG_ORIENTATION,0);
        photoWidth = exifInterface.getAttributeInt(ExifInterface.TAG_IMAGE_WIDTH,0);
        photoHeight = exifInterface.getAttributeInt(ExifInterface.TAG_IMAGE_LENGTH,0);

        Bitmap rotatedBitmap = null;
        switch (orientation) {
            case ExifInterface.ORIENTATION_ROTATE_90:
                rotatedBitmap = rotateImage(bitmap, 90, photoWidth, photoHeight);
                break;
            case ExifInterface.ORIENTATION_ROTATE_180:
                rotatedBitmap = rotateImage(bitmap, 180, photoWidth, photoHeight);
                break;
            case ExifInterface.ORIENTATION_ROTATE_270:
                rotatedBitmap = rotateImage(bitmap, 270, photoWidth, photoHeight);
                break;
            case ExifInterface.ORIENTATION_NORMAL:
            default:
                rotatedBitmap = bitmap;
        }
        return rotatedBitmap;
    }

    private Bitmap getRotatedBitmapWithPath(Bitmap bitmap, String path) throws IOException {
        int photoWidth = 0;
        int photoHeight = 0;
        File file = new File(path);
        boolean isExists = file.exists();
        ExifInterface exifInterface = new ExifInterface(file.getAbsolutePath());
        int orientation = Integer.parseInt(exifInterface.getAttribute(ExifInterface.TAG_ORIENTATION));
        photoWidth = Integer.parseInt(exifInterface.getAttribute(ExifInterface.TAG_IMAGE_WIDTH));
        photoHeight = Integer.parseInt(exifInterface.getAttribute(ExifInterface.TAG_IMAGE_LENGTH));

        Bitmap rotatedBitmap = null;
        switch (orientation) {
            case ExifInterface.ORIENTATION_ROTATE_90:
                rotatedBitmap = rotateImage(bitmap, 90, photoWidth, photoHeight);
                break;
            case ExifInterface.ORIENTATION_ROTATE_180:
                rotatedBitmap = rotateImage(bitmap, 180, photoWidth, photoHeight);
                break;
            case ExifInterface.ORIENTATION_ROTATE_270:
                rotatedBitmap = rotateImage(bitmap, 270, photoWidth, photoHeight);
                break;
            case ExifInterface.ORIENTATION_NORMAL:
            default:
                rotatedBitmap = bitmap;
        }
        return rotatedBitmap;
    }


    private float currentAzimuth;
    SharedPreferences prefs;
    GPSTracker gps;
    private View mWord;
    private View mWord2;
    private View mDefects2;
    private View mComment;
    private View mComment2;
    private View mPlan;
    private View mPlan2;
    private View mDefects;
    private View mRecord;
    private View mRecord2;
    private View mBrush;
    private View mBrush2;

    public void fetch_GPS() {
        gps = gpsTracker;
        double result = 0;

        if (gps.canGetLocation()) {
            double latitude = gps.getLatitude();
            double longitude = gps.getLongitude();
            // \n is for new line
//            text_bawah.setText(getResources().getString(R.string.your_location) + "\nLat: " + latitude + " Long: " + longitude);
            // Toast.makeText(getApplicationContext(), "Lokasi anda: - \nLat: " + latitude + "\nLong: " + longitude, Toast.LENGTH_LONG).show();
            //  Log.e("TAG", "GPS is on");
            double lat_saya = gps.getLatitude();
            double lon_saya = gps.getLongitude();
            if (lat_saya < 0.001 && lon_saya < 0.001) {
                // arrowViewQiblat.isShown(false);

                // Toast.makeText(getApplicationContext(), "Location not ready, Please Restart Application", Toast.LENGTH_LONG).show();
            } else {

                //North ploe latlng  90,0
//                double longitude2 = 39.826206; // ka'bah Position https://www.latlong.net/place/kaaba-mecca-saudi-arabia-12639.html
                double longitude2 = 0; // ka'bah Position https://www.latlong.net/place/kaaba-mecca-saudi-arabia-12639.html
                double longitude1 = lon_saya;
//                double latitude2 = Math.toRadians(21.422487); // ka'bah Position https://www.latlong.net/place/kaaba-mecca-saudi-arabia-12639.html
                double latitude2 = Math.toRadians(90); // ka'bah Position https://www.latlong.net/place/kaaba-mecca-saudi-arabia-12639.html
                double latitude1 = Math.toRadians(lat_saya);
                double longDiff = Math.toRadians(longitude2 - longitude1);
                double y = Math.sin(longDiff) * Math.cos(latitude2);
                double x = Math.cos(latitude1) * Math.sin(latitude2) - Math.sin(latitude1) * Math.cos(latitude2) * Math.cos(longDiff);
                result = (Math.toDegrees(Math.atan2(y, x)) + 360) % 360;
                float result2 = (float) result;

            }
            //  Toast.makeText(getApplicationContext(), "lat_saya: "+lat_saya + "\nlon_saya: "+lon_saya, Toast.LENGTH_LONG).show();
        } else {
            // can't get location
            // GPS or Network is not enabled
            // Ask user to enable GPS/network in settings
            gps.showSettingsAlert();


            // Toast.makeText(getApplicationContext(), "Please enable Location first and Restart Application", Toast.LENGTH_LONG).show();
        }
    }

    private void setupCompass() {
        fetch_GPS();

        compass = new Compass(this);
        Compass.CompassListener cl = getCompassListener();
        compass.setListener(cl);
    }

    public float getRotationForCompass(int rotation, float compassDegrees) {

        return compassDegrees;
   /*     switch (rotation) {
            case 0:
//                return "portrait "+compassDegrees;
                return compassDegrees;
            case 270:
                //3
                compassDegrees = compassDegrees + 90;
                if (compassDegrees > 360)
                    compassDegrees = compassDegrees - 360;
//                return "landscape "+compassDegrees ;
                return compassDegrees;
            case 180:
                compassDegrees = (compassDegrees + 180);
                if (compassDegrees > 360) {
                    compassDegrees = compassDegrees - 360;
                }
//                return "reverse portrait "+compassDegrees;
                return compassDegrees;
            case 90:

                compassDegrees = (compassDegrees - 90);
                if (compassDegrees < 0)
                    compassDegrees = Math.abs(360 + compassDegrees);
//                return "reverse landscape "+compassDegrees;
                return compassDegrees;
            case 360:
//                return "portrait "+compassDegrees;
                return compassDegrees;
            default:
//                return "reverse landscape";
                return compassDegrees;
        }*/
    }

    public class updatePhotoAsyncTask2 extends AsyncTask<Void, Void, Void> {
        private long photoId;
        private String projectID;
        private PhotoModel photoModelObj;
        boolean isFromDefectCreate = false;
        List<String> defectTradeModelListt = null;

        updatePhotoAsyncTask2(long photoId, String projectId, PhotoModel photoModel) {
            this.photoId = photoId;
            projectID = projectId;
            photoModelObj = photoModel;

        }

        updatePhotoAsyncTask2(long photoId, String projectId, PhotoModel photoModel, boolean isFromDefetcCreate) {
            this.photoId = photoId;
            projectID = projectId;
            photoModelObj = photoModel;
            isFromDefectCreate = isFromDefetcCreate;

        }

        @Override
        protected Void doInBackground(Void... params) {
            db = ProjectsDatabase.getDatabase(getApplication());

            if (local_flaw_id != 0) {
                db.defectsDao().deleteUsingLocalDefectId(local_flaw_id);
            }
            db.photoDao().update(photoModelObj);

//             defectTradeModelListt = db.photoDao().getDefectIdsAttachedWithPhotos(projectID, photoModelObj.getLocal_flaw_id() + "", photoModelObj.getPdphotolocalId()+"");
//             PhotoModel photoModel = db.photoDao().getDefectPhotosAndLocalPhotosOBj(projectID, photoModelObj.getLocal_flaw_id() + "");
//             DefectsModel defectsModel= db.defectsDao().getDefectsOBJ(projectID, photoModelObj.getLocal_flaw_id());
//             Log.d("before_defect_attach","projectID "+projectID+" Local_flaw_id "+photoModelObj.getLocal_flaw_id()+" photoId "+photoModelObj.getPdphotolocalId());
//            isPhotoUpdate = true;

            return null;
        }


        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
//            setDoneIcons();
            if (savePictureViewModel != null) {
                if (savePictureViewModel.getPhotoModel() != null) {
                    savePictureViewModel.getPhotoModel().setDefectAdded(false);
                    setDoneIcons();
                }
            }
        }
    }

    //region setupIconsList
    private void setupListOfIconsBeforeTakingPhoto() {
        iconsListBeforeTakingPhoto.add(tv_close_camera);
        iconsListBeforeTakingPhoto.add(iv_camera);
        iconsListBeforeTakingPhoto.add(iv_flash);
    }

    private void setupListOfIconsAfterTakingPhoto() {
        iconsListAfterTakingPhoto.add(rl_word);
        iconsListAfterTakingPhoto.add(rl_comment);
        iconsListAfterTakingPhoto.add(rl_plan);
        iconsListAfterTakingPhoto.add(rl_defects);
        iconsListAfterTakingPhoto.add(rl_brush);
        iconsListAfterTakingPhoto.add(rl_record);
        iconsListAfterTakingPhoto.add(recyclerView);
        iconsListAfterTakingPhoto.add(captured_image);
    }
    //endregion

    //region showHideIcons
    private void showHideIcons(int degrees) {
        switch (degrees) {
            case 0:
                isLandScape = false;
                is_90_ori_active = false;
                if (ll_icons != null) {
                    if (ll_icons.getVisibility() == View.VISIBLE) {
                        iv_delete.setVisibility(View.VISIBLE);
                        iv_delete_rotated.setVisibility(View.GONE);
                        toolbar.setVisibility(View.VISIBLE);
                        toolbarRotated.setVisibility(View.GONE);

                        if (ll_bottom_camera_view_2 != null) {
                            ll_bottom_camera_view_2.setVisibility(View.GONE);
                        }

                        if (ll_bottom_camera_view != null) {
                            ll_bottom_camera_view.setVisibility(View.VISIBLE);
                        }

                        if (ll_icons_2 != null) {
                            ll_icons_2.setVisibility(View.GONE);
                        }

                        if (rlRotatedBackDeleted != null) {
                            rlRotatedBackDeleted.setVisibility(View.GONE);
                        }
                    }
                }


                break;

            case 90:
                //   Log.d(TAG, "showHideIcons: 90");
                isLandScape = true;
                is_90_ori_active = true;
                if (ll_icons != null) {

                    if (ll_icons.getVisibility() == View.VISIBLE) {
                        if (ll_bottom_camera_view_2 != null) {
                            ll_bottom_camera_view_2.setVisibility(View.VISIBLE);
                        }
                        if (ll_bottom_camera_view != null) {
                            ll_bottom_camera_view.setVisibility(View.GONE);
                        }
                        if (ll_icons_2 != null) {
                            ll_icons_2.setVisibility(View.VISIBLE);
                        }
                        if (rlRotatedBackDeleted != null) {
                            rlRotatedBackDeleted.setVisibility(View.VISIBLE);
                        }
                    }
                }

                break;

            case 180:
                isLandScape = false;
                //Log.d(TAG, "showHideIcons: 180");
                break;

            case 270:
                isLandScape = true;
                is_90_ori_active = false;
                if (ll_icons != null) {
                    if (ll_icons.getVisibility() == View.VISIBLE) {
                        iv_delete.setVisibility(View.GONE);
                        iv_delete_rotated.setVisibility(View.VISIBLE);
                        toolbar.setVisibility(View.GONE);
                        toolbarRotated.setVisibility(View.VISIBLE);
                    }
                }


                break;


        }

    }
    //endregion

//    private void setLayoutInverse() {
//        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) ll_bottom_camera_view.getLayoutParams();
//        params.addRule(RelativeLayout.ALIGN_PARENT_TOP);
//        ll_bottom_camera_view.setLayoutParams(params);
//    }

    //region deleteImage
    private void deleteImage() {
        if (isFromLocalPhotos) {
            if (savePictureViewModel.getPhotoModel() != null) {
                if (savePictureViewModel.getPhotoModel().getPhotoUploadStatus().equals(LocalPhotosRepository.UPLOADING_PHOTO)) {
                    showCustomDialog(SavePictureActivity.this, "projectdocu", getResources().getString(R.string.image_syncing), 1, 1);
                } else {
                    showCustomDialog(SavePictureActivity.this, "projectdocu", getResources().getString(R.string.image_delete_msg), 2, 0);
                }
            }
        } else {
            showCustomDialog(SavePictureActivity.this, "projectdocu", getResources().getString(R.string.image_delete_msg), 2, 0);
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


    private void disableCompassValuesForOneSecond() {
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                isIgnoreCompassUpdate = false;
            }


        }, 1000);
    }


    /**
     * A low-pass filter for smoothing out noisy sensor values.
     *
     * @param oldVal      The previous value.
     * @param newVal      The new value.
     * @param decayFactor Decay factor. (1 / decayFactor) = number of samples to smooth over.
     * @return The smoothed value.
     */
    private float smoothSensorValues(float oldVal, float newVal, float decayFactor) {
        return oldVal * (1 - decayFactor) + newVal * decayFactor;
    }

  /*  private void setupCompass() {
        compass = new Compass(this);
        Compass.CompassListener cl = getCompassListener();
        compass.setListener(cl);
    }*/

    private Compass.CompassListener getCompassListener() {
        return new Compass.CompassListener() {
            @Override
            public void onNewAzimuth(final float azimuth) {
                // UI updates only in UI thread
                // https://stackoverflow.com/q/11140285/444966
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Integer aziF = Utils.floatToLowerInt(azimuth);

                        // Float aziF = azimuth;
                        //  Utils.showLogger("lololo>>>"+aziF.intValue());
                        compassDegrees = aziF.intValue();

                        if (myorientation != null) {
                            if (myorientation == MYORIENTATION.PORTRAIT) {
                                //testOrientation = "PORTRAIT";
                                //  compassDegrees = values.values[0];
                                //  Utils.showLogger2("PORTRAIT");
                            } else if (myorientation == MYORIENTATION.LANDSCAPE) {
                                /// testOrientation = "LANDSCAPE";
                                //  Utils.showLogger2("LANDSCAPE");
                                compassDegrees = compassDegrees - 90;
                            } else if (myorientation == MYORIENTATION.REVERSE_LANDSCAPE) {
                                //  testOrientation = "REVERSE_LANDSCAPE";
                                //    Utils.showLogger2("REVERSE_LANDSCAPE");
                                compassDegrees = compassDegrees + 90;
                                //  Utils.showLogger("reveresLand");
                            }
                        }


                        if (compassDegrees < 0) compassDegrees = compassDegrees + 360;
                        else if (compassDegrees > 360) compassDegrees = compassDegrees - 360;


                        //    compassAngleView.setText("Angle:" + compassDegrees + compass.getDirection(compassDegrees));


                    }
                });
            }

            @Override
            public void onAccuracyCorrect() {
                if (myCalibateDialog != null) myCalibateDialog.cancel();
            }

            @Override
            public void onAccuracyWrong() {
                showCalibationDialog();

            }
        };
    }


    private void showCalibationDialog() {

        myCalibateDialog = new CalibrateDialog(SavePictureActivity.this);
        myCalibateDialog.setCancelable(false);
        myCalibateDialog.show();


    }

}
