package com.projectdocupro.mobile.fragments.add_direction;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.location.Location;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.util.Log;
import android.view.DragEvent;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.projectdocupro.mobile.ProjectNavigator;
import com.projectdocupro.mobile.ProjectsDatabase;
import com.projectdocupro.mobile.R;
import com.projectdocupro.mobile.activities.DefectDetailsActivity;
import com.projectdocupro.mobile.activities.DefectsActivity;
import com.projectdocupro.mobile.dao.DefectsDao;
import com.projectdocupro.mobile.dao.PdFlawFLagListDao;
import com.projectdocupro.mobile.dao.PhotoDao;
import com.projectdocupro.mobile.dao.PlansDao;
import com.projectdocupro.mobile.dao.ProjectUsersDao;
import com.projectdocupro.mobile.fragments.DefectsListFragment;
import com.projectdocupro.mobile.interfaces.RetroApiInterface;
import com.projectdocupro.mobile.managers.AppConstantsManager;
import com.projectdocupro.mobile.managers.RetrofitManager;
import com.projectdocupro.mobile.managers.SharedPrefsManager;
import com.projectdocupro.mobile.models.DefectsModel;
import com.projectdocupro.mobile.models.Pdflawflag;
import com.projectdocupro.mobile.models.PhotoModel;
import com.projectdocupro.mobile.models.PlansModel;
import com.projectdocupro.mobile.models.PlansPhotoModel;
import com.projectdocupro.mobile.models.mangel_filters.ProjectUserModel;
import com.projectdocupro.mobile.photoview.OnPhotoTapListener;
import com.projectdocupro.mobile.photoview.PhotoView;
import com.projectdocupro.mobile.photoview.ViewPhotoActivity;
import com.projectdocupro.mobile.repos.AllPlansRepository;
import com.projectdocupro.mobile.repos.DefectRepository;
import com.projectdocupro.mobile.repos.PdFlawFlagRepository;
import com.projectdocupro.mobile.repos.ProjectDetailRepository;
import com.projectdocupro.mobile.utility.Utils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;


import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.projectdocupro.mobile.activities.DefectsActivity.IS_CREATED_MANGEL_KEY;
import static com.projectdocupro.mobile.fragments.DefectsListFragment.ARG_PROJECT_ID;
import static com.projectdocupro.mobile.managers.AppConstantsManager.ACTIVATE_PHOTO_PATH;

@SuppressLint("ValidFragment")
public class DefectPlanDirectionFragment extends Fragment {
    private String projectID = "";
    private String planID = "";
    private Bitmap myBitmap = null;
    private GestureDetector gd;
    private View rootView = null;
    private EnhancedWebView planWebview = null;

    private Fragment fragment = null;
    private int localPhotoId = -1;
    private int rotateIcons = 0;
    private boolean arrowRotation = false;

//    private ProjectDocuMainActivity projectDocuMainActivity = null;


    private LinearLayout planSubmenuButtonsExpanded = null;
    private ImageView buttonDescription = null;
    private ImageView buttonMemo = null;
    private ImageView buttonTags = null;

    private ImageView buttonPlan = null;
    private ImageView buttonConfig = null;
    private ImageView buttonTrash = null;
    private ImageView buttonCancel = null;
    private ImageView buttonSave = null;

    private ImageView buttonCompassRotate = null;
    private ImageView buttonArrowRotate = null;

    private ImageView captureButton = null;
    private ImageView touchHelperView = null;
    private ImageView arrowTouchHelperView = null;
    private ImageView rotationArrow = null;

    private boolean switchArrowMode = false;

    private TextView debugTextView = null;

    private String webviewHtml = null;

    public float planWidth;
    public float planHeight;

    private float touchHelperViewX = 0.0f;
    private float touchHelperViewY = 0.0f;
    private boolean arrowTouched = false;
    private boolean arrowIsTouched = false;

    private boolean blockSingleTouch = false;
    // private int moveCount = 0;
    private long currentTime = 0;

    private float oldCrossHairX = 0.0f;
    private float oldCrossHairY = 0.0f;
    public float tempCrossHairX = 0.0f;
    public float tempCrossHairY = 0.0f;

    private float oldScale = 0;

    private int northDeviationAngle = 0;
    private GeoPoint[] refGeoPoints = null;
    private boolean showGPSAccuracy = true;
    private boolean blockAutoPosition = false;
    private boolean blockAutoDirection = false;

    private boolean blockOrientationChange = false;

    private Timer timer;
    private TimerTask timerTask;

    final Handler handler = new Handler();

    public static DefectPlanDirectionFragment projectDocuShowPlanFragment;
    static final String PHOTO_SAVED_PATH_KEY = "photo_path_key";
    //    private Bitmap bitmap, mask, rotatedMask;
    double bmWidth, bmHeight;
    float touchX, touchY;
    ImageView iv_temp;

    PhotoView mPhotoView;

    private Activity activity = null;

//	private ProjectDocuCameraFragment projectDocuCameraFragment = null;

    final ProjectDocuUtilities projectDocuUtilities = new ProjectDocuUtilities();
    private String photoURL = "/storage/emulated/0/projectDocu/project_plans_44/Download_1565001396991.jpg";
    private ImageView button_save_and_back;
    private String imagePath = "";
    private SharedPreferences pref;
    private SharedPreferences.Editor editor;
    //    private Flags flag;
    private AllPlansRepository plansRepository;
    private PlansModel plansModelOBJ;
    LinearLayout plan_sidesubmenu_buttons;
    private PdFlawFlagRepository pdFlawFlagRepository;
    private DefectRepository mDefectRepository;


    private boolean isFromDefectScreen;
    private List<String> defectStatusList;
    private List<Pdflawflag> flawFlagsList;

    int minPos = 0;
    // short detail resources

    private TextView tv_nr_no;


    private ImageView iv_status_red;

    private ImageView iv_status_orange;

    private ImageView iv_status_green;

    private RelativeLayout rl_art_view;

    private TextView tv_art_text;

    private TextView tv_gewerk;

    private TextView tv_end_date;

    private TextView tv_photo_date;

    private ImageView iv_photo;
    private ImageView iv_plans;

    private TextView tv_description_text;

    private TextView tv_res_user_name_text;


    private RelativeLayout rl_users_view;

    private TextView ll_mangel_detail_view;

    private LinearLayout ll_photos_view;

    private LinearLayout ll_plans_view;


    private LinearLayout ll_hidenView;
    private LinearLayout ll_expand_view;

    private RelativeLayout rl_short_detail_view;
    private TextView tvTitle;
    private SharedPrefsManager sharedPrefsManager;
    private DefectsModel defectsModelObj;
    private ProjectDetailRepository projectDetailRepository;


    @SuppressLint("ValidFragment")
    public DefectPlanDirectionFragment(Fragment fragment, String projectId, String planId, boolean isFromDefect) {
        this.fragment = fragment;
        this.localPhotoId = localPhotoId;
        planID = planId;
        projectID = projectId;
        isFromDefectScreen = isFromDefect;

//        myBitmap = BitmapFactory.decodeFile(photoURL);

    }

    public DefectPlanDirectionFragment() {

    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        this.activity = activity;
        if (activity != null) {
//            activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
            activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }
    }

    public static Point getTouchPositionFromDragEvent(View item, DragEvent event) {
        Rect rItem = new Rect();
        item.getGlobalVisibleRect(rItem);
        return new Point(rItem.left + Math.round(event.getX()), rItem.top + Math.round(event.getY()));
    }


    @Override
    public void onDetach() {
        super.onDetach();


    }

    private void bindView(View bindSource) {
        tv_nr_no = bindSource.findViewById(R.id.tv_nr_no);
        iv_status_red = bindSource.findViewById(R.id.iv_status_red);
        iv_status_orange = bindSource.findViewById(R.id.iv_status_orange);
        iv_status_green = bindSource.findViewById(R.id.iv_status_green);
        rl_art_view = bindSource.findViewById(R.id.rl_art_view);
        tv_art_text = bindSource.findViewById(R.id.art_text);
        tv_gewerk = bindSource.findViewById(R.id.tv_gewerk);
        tv_end_date = bindSource.findViewById(R.id.tv_end_date);
        tv_photo_date = bindSource.findViewById(R.id.tv_photo_date);
        iv_photo = bindSource.findViewById(R.id.iv_photo);
        iv_plans = bindSource.findViewById(R.id.iv_plans);
        tv_description_text = bindSource.findViewById(R.id.tv_description_text);
        tv_res_user_name_text = bindSource.findViewById(R.id.name_text);
        rl_users_view = bindSource.findViewById(R.id.rl_users_view);
        ll_mangel_detail_view = bindSource.findViewById(R.id.ll_mangel_detail_view);
        ll_photos_view = bindSource.findViewById(R.id.ll_photos_view);
        ll_plans_view = bindSource.findViewById(R.id.ll_plans_view);
        ll_hidenView = bindSource.findViewById(R.id.ll_hidenView);
        ll_expand_view = bindSource.findViewById(R.id.ll_expand_view);
        rl_short_detail_view = bindSource.findViewById(R.id.rl_short_detail_view);
        tvTitle = bindSource.findViewById(R.id.tvTitle);
    }

    private class PhotoTapListener implements OnPhotoTapListener {

        @Override
        public void onPhotoTap(ImageView view, float x, float y) {
//            float xPercentage = x * 100f;
//            float yPercentage = y * 100f;


            Matrix inverse = new Matrix();
            view.getImageMatrix().invert(inverse);

// map touch point from ImageView to image
            float[] touchPoint = new float[]{x, y};
            inverse.mapPoints(touchPoint);
            Bitmap bigImage = ((BitmapDrawable) mPhotoView.getDrawable()).getBitmap();
            // mergStatusOnPhoto2(bigImage,touchPoint[0]-40,touchPoint[1]-40);
            List<Double> pointsDistance = new ArrayList<>();

            if (flawFlagsList != null && flawFlagsList.size() > 0) {
                for (int i = 0; i < flawFlagsList.size(); i++) {

                    double time = 0;
//                           DecimalFormat df = new DecimalFormat("#.##");
//                    pointsDistance.add(meterDistanceBetweenPoints( Math.abs(touchPoint[0]) ,  Math.abs(touchPoint[1]), Math.abs( Float.valueOf(flawFlagsList.get(i).getXcoord())),  Math.abs(Float.valueOf(flawFlagsList.get(i).getYcoord()))));

                    double Xcoord = ((bigImage.getWidth() / 2)-45) + Integer.valueOf(flawFlagsList.get(i).getXcoord()) ;
                    double Ycoord = ((bigImage.getHeight() / 2)-60) + Integer.valueOf(flawFlagsList.get(i).getYcoord()) ;



                    pointsDistance.add(distance(touchPoint[0], touchPoint[1], Xcoord, Ycoord));
//                           pointsDistance[i]= time  ;


                }
                Double minVale = getMinDistance(pointsDistance);

                if (minVale <= 60) {

                    for (int i = 0; i < pointsDistance.size(); i++) {
                        if (minVale.compareTo(pointsDistance.get(i)) == 0) {
                            minPos = i;
                            break;
                        }
                    }
                    if (flawFlagsList != null && flawFlagsList.size() >= minPos) {
                        if (flawFlagsList.get(minPos) != null && flawFlagsList.get(minPos).getLocal_flaw_Id() != null) {
                            new RetrieveDefectAsyncTask(ProjectsDatabase.getDatabase(getActivity()).defectsDao()).execute(projectID, flawFlagsList.get(minPos).getLocal_flaw_Id());

                        }
                    }
                }
            }


            // calculate inverse matrix

        }
    }

    private void mergStatusOnPhoto2(Bitmap myBitmap, float x, float y) {

//        Drawable myIcon1 = getResources().getDrawable( R.drawable.green_circle_selected );
//        Drawable myIcon2 = getResources().getDrawable( R.drawable.green_circle_selected );
//        Drawable myIcon3 = getResources().getDrawable( R.drawable.green_circle_selected );

        Bitmap bigImage = myBitmap;
        Bitmap mergedImages = null;

//        iv_temp.setVisibility(View.VISIBLE);
//        iv_temp.setImageBitmap(mergedImages);
        Bitmap result = Bitmap.createBitmap(bigImage.getWidth(), bigImage.getHeight(), bigImage.getConfig());
        Canvas canvas = new Canvas(result);
        canvas.drawBitmap(bigImage, 0, 0
                , null);
        Bitmap smallImage = null;

        smallImage = BitmapFactory.decodeResource(getResources(), R.drawable.green_circle_selected);


//            canvas.drawBitmap(smallImage, (bigImage.getWidth() / 2) + x - 20, (bigImage.getHeight() / 2) + y - 50, null);
        canvas.drawBitmap(smallImage, x, y, null);

        writeBitMapToDisk2(result, projectID);


    }

    private boolean writeBitMapToDisk2(Bitmap bitmap, String projectId) {

        try {
            // todo change the file location/name according to your needs
            File dir = getActivity().getExternalFilesDir("/projectDocu/project_plans_" + projectId);
            if (dir == null) {
                dir = getActivity().getFilesDir();
            }
//            File dir = new File(Environment.getExternalStorageDirectory() + "/projectDocu/project_plans_" + projectId);
            if (!dir.isDirectory()) {
                dir.mkdirs();
            }
            File photo = new File(dir, "/Download_" + new Date().getTime() + ".jpg");

            imagePath = photo.getAbsolutePath();
            sharedPrefsManager.setStringValue(ACTIVATE_PHOTO_PATH, imagePath);
//            editor.putString(PHOTO_SAVED_PATH_KEY, imagePath);
//            editor.commit();

//            if (plansModelOBJ != null) {
//                plansModelOBJ.setPlanPhotoPathLargeSize(imagePath);
//                new updatePlansAsyncTask(ProjectsDatabase.getDatabase(getActivity()).plansDao()).execute(plansModelOBJ);
//            }

            InputStream inputStream = null;
            OutputStream outputStream = null;
            outputStream = new FileOutputStream(photo);

            try {

                outputStream = new FileOutputStream(photo);
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);


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

                Intent intent = new Intent(getActivity(), ViewPhotoActivity.class);
                startActivity(intent);

            }
        } catch (IOException e) {
            return false;
        }


    }

    double getMinDistance(List<Double> distances) {
        double minDistance = Double.MAX_VALUE;
        for (double distance : distances) {
            minDistance = Math.min(distance, minDistance);
        }
        return minDistance;
    }


    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
//    	((ProjectDocuMainActivity) getActivity()).showMainLayout();
    }


    private void mergStatusOnPhoto(List<Pdflawflag> pdflawflagList) {

//        Drawable myIcon1 = getResources().getDrawable( R.drawable.green_circle_selected );
//        Drawable myIcon2 = getResources().getDrawable( R.drawable.green_circle_selected );
//        Drawable myIcon3 = getResources().getDrawable( R.drawable.green_circle_selected );

        Bitmap bigImage = myBitmap;
        Bitmap mergedImages = null;

//        iv_temp.setVisibility(View.VISIBLE);
//        iv_temp.setImageBitmap(mergedImages);
        Bitmap result = Bitmap.createBitmap(bigImage.getWidth(), bigImage.getHeight(), bigImage.getConfig());
        Canvas canvas = new Canvas(result);
        canvas.drawBitmap(bigImage, 0, 0
                , null);
        Bitmap smallImage = null;
        for (int i = 0; i < pdflawflagList.size(); i++) {
            if (defectStatusList != null && pdflawflagList.size() == defectStatusList.size()) {

                if (defectStatusList.get(i).equals("0")) {
                    smallImage = BitmapFactory.decodeResource(getResources(), R.drawable.green_circle_selected);
                } else if (defectStatusList.get(i).equals("1")) {
                    smallImage = BitmapFactory.decodeResource(getResources(), R.drawable.yellow_circle_selected);
                } else {
                    smallImage = BitmapFactory.decodeResource(getResources(), R.drawable.red_circle_selected);
                }
            }
            if (smallImage != null && bigImage != null){
                int xWithZoom = 0;
                int yWithZoom = 0;


//                if (Float.valueOf(pdflawflagList.get(i).getScale_factor()) > 10) {
//
//                    xWithZoom = (int) ((500) / pdflawflagList.get(i).getScale_factor());
//                    yWithZoom = (int) ((600) / pdflawflagList.get(i).getScale_factor());
//                } else if (Float.valueOf(pdflawflagList.get(i).getScale_factor()) > 6) {
//
//                    xWithZoom = (int) ((200) / pdflawflagList.get(i).getScale_factor());
//                    yWithZoom = (int) ((340) / pdflawflagList.get(i).getScale_factor());
//                } else if (Float.valueOf(pdflawflagList.get(i).getScale_factor()) > 4) {
//
//                    xWithZoom = (int) ((140) / pdflawflagList.get(i).getScale_factor());
//                    yWithZoom = (int) ((240) / pdflawflagList.get(i).getScale_factor());
//                } else if (Float.valueOf(pdflawflagList.get(i).getScale_factor()) > 2) {
//
//                    xWithZoom = (int) ((110) / pdflawflagList.get(i).getScale_factor());
//                    yWithZoom = (int) ((210) / pdflawflagList.get(i).getScale_factor());
//                } else {
//                    xWithZoom = (int) ((50) / pdflawflagList.get(i).getScale_factor());
//                    yWithZoom = (int) ((70) / pdflawflagList.get(i).getScale_factor());
//
//                }
//                canvas.drawBitmap(smallImage, ((bigImage.getWidth() / 2) - (xWithZoom) + Integer.valueOf(pdflawflagList.get(i).getXcoord())), ((bigImage.getHeight() / 2) - yWithZoom) + Integer.valueOf(pdflawflagList.get(i).getYcoord()), null);

            }
                canvas.drawBitmap(smallImage,((bigImage.getWidth() / 2)-45  )+Integer.valueOf(pdflawflagList.get(i).getXcoord()),((bigImage.getHeight() / 2)-60) +Integer.valueOf(pdflawflagList.get(i).getYcoord()) , null);

//                canvas.drawBitmap(smallImage, (bigImage.getWidth() / 2) + Integer.valueOf(pdflawflagList.get(i).getXcoord()) , (bigImage.getHeight() / 2) + Integer.valueOf(pdflawflagList.get(i).getYcoord()) , null);

        }
        mPhotoView.setImageBitmap(result);

        writeBitMapToDisk(result, projectID);


    }

    private Bitmap createSingleImageFromMultipleImages(Bitmap bigImage, Bitmap smallImage) {

        Bitmap result = Bitmap.createBitmap(bigImage.getWidth(), bigImage.getHeight(), bigImage.getConfig());
        Canvas canvas = new Canvas(result);

//        Bitmap resultn=   scaleDown(bigImage,flag.scale_factor,false);

        canvas.drawBitmap(bigImage, 0, 0
                , null);
//        canvas.drawBitmap(smallImage, 21, 254, null);

//        EnhancedWebView enhancedWebView= new EnhancedWebView(getActivity());
//        enhancedWebView. planResizeFactor=flag.scale_factor;
//        enhancedWebView.calculateSizes(flag.xcoord,flag.ycoord,true);
//        Matrix matrix = new Matrix();
//        matrix.reset();
//        matrix.postTranslate((bigImage.getWidth() / 2) + flag.xcoord, (bigImage.getHeight() / 2) + flag.ycoord); // Centers image
//        matrix.postRotate(0);
////        matrix.postScale()
//        canvas.drawBitmap(smallImage, (bigImage.getWidth() / 2) + flag.xcoord - 20, (bigImage.getHeight() / 2) + flag.ycoord - 50, null);

        Bitmap orgImage = BitmapFactory.decodeResource(getResources(), R.drawable.yellow_circle_selected);

        canvas.drawBitmap(orgImage, (bigImage.getWidth() / 2) + 441 - 20, (bigImage.getHeight() / 2) + 417 - 50, null);
        return result;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @SuppressWarnings("deprecation")
    @Override
    public View onCreateView(LayoutInflater layoutInflater, ViewGroup viewGroup, Bundle savedInstanceState) {
        //System.out.println("##### ProjectDocuShowPlanFragment:onCreateView()");

        Utils.showLogger("DefectPlanDirectionFragment");

        rootView = layoutInflater.inflate(R.layout.defect_plan_direction_layout, viewGroup, false);
        bindView(rootView);
        pref = getActivity().getApplicationContext().getSharedPreferences("MyPref", 0); // 0 - for private mode
        editor = pref.edit();

        sharedPrefsManager = new SharedPrefsManager(getActivity());
        projectDetailRepository = new ProjectDetailRepository(getActivity().getApplication(), projectID);

        mPhotoView = rootView.findViewById(R.id.iv_photo_view);
        mPhotoView.setOnPhotoTapListener(new PhotoTapListener());
        ll_hidenView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ll_hidenView.setVisibility(View.GONE);
                ll_expand_view.setVisibility(View.GONE);
                rl_short_detail_view.setVisibility(View.GONE);

            }
        });

       /* mPhotoView.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
//                planWebview.dispatchTouchEvent(event);
                if ((event.getAction() == MotionEvent.ACTION_MOVE || event.getAction() == MotionEvent.ACTION_SCROLL) && event.getPointerCount() < 2) {
//					if (blockAutoPosition == false) {
//						setBlockAutoPosition(true);
//					}

                }
                // check if screen is touched
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
//                    oldCrossHairX = planWebview.crosshairPositionX;
//                    oldCrossHairY = planWebview.crosshairPositionY;
//                    oldScale = planWebview.scaleFactor;

                    currentTime = System.currentTimeMillis();
                    blockSingleTouch = false;




                    Matrix inverse = new Matrix();
                    mPhotoView.getImageMatrix().invert(inverse);

// map touch point from ImageView to image
                    float[] touchPoint = new float[] {event.getX(), event.getY()};
                    inverse.mapPoints(touchPoint);
                   List<Double> pointsDistance= new ArrayList<>();
                   if(flawFlagsList!=null&&flawFlagsList.size()>0){
                       for (int i = 0; i < flawFlagsList.size(); i++) {

                           double time = 0;
//                           DecimalFormat df = new DecimalFormat("#.##");
                           pointsDistance.add (meterDistanceBetweenPoints(touchPoint[0],touchPoint[1],Float.valueOf(flawFlagsList.get(i).getXcoord()),Float.valueOf(flawFlagsList.get(i).getYcoord())));
//                           pointsDistance[i]= time  ;




                       }
                   }


//                    mergStatusOnPhoto(((BitmapDrawable) mPhotoView.getDrawable()).getBitmap(),touchPoint[0],touchPoint[1]);
                    return true;
                }

                // check for two finger touch
                if (event.getPointerCount() >= 2) {
                    blockSingleTouch = true;
                    // moveCount = 0;

                    return false;

                }

//                else if (planWebview.crosshairPositionX > (oldCrossHairX + 10)
//                        || planWebview.crosshairPositionX < (oldCrossHairX - 10)
//                        || planWebview.crosshairPositionY > (oldCrossHairY + 10)
//                        || planWebview.crosshairPositionY < (oldCrossHairY - 10)
//                ) {
//                    blockSingleTouch = true;
//                    return false;
//                }

//                 * Scroll map to long touched position
//
                if (event.getAction() == MotionEvent.ACTION_UP && blockSingleTouch == false) {
                    //System.out.println("#### ACTIONUP && blockSingleTouch == false");
                    if (System.currentTimeMillis() - currentTime > 500) {
                        if (blockSingleTouch == false) {
//                            float touchX = event.getX() - planWebview.displayWidth / 2.0f;
//                            float touchY = event.getY() - planWebview.displayHeight / 2.0f;
//
//                            int x = Math.round((planWebview.resizedPlanWidth / 2 * planWebview.scaleFactor + (touchX + planWebview.scaledCrosshairPositionX)));
//                            int y = Math.round((planWebview.resizedPlanHeight / 2 * planWebview.scaleFactor + (touchY + planWebview.scaledCrosshairPositionY)));
//
//                            ObjectAnimator xTranslate = ObjectAnimator.ofInt(planWebview, "scrollX", (x));
//                            ObjectAnimator yTranslate = ObjectAnimator.ofInt(planWebview, "scrollY", (y));
//                            AnimatorSet animators = new AnimatorSet();
//                            animators.setDuration(1000L);
//                            animators.playTogether(xTranslate, yTranslate);
//                            animators.start();
//
//                            planWebview.scrollBy(Math.round(touchX), Math.round(touchY));

                        }
                    }
                    blockSingleTouch = false;
                }

                if (event.getAction() == MotionEvent.ACTION_UP) {
                    //System.out.println("#### ACTIONUP");
                    if (android.os.Build.VERSION.SDK_INT < 19) {
                        //	this.loadUrl("javascript:setTableMargin("+Math.round(defaultMarginWidth/scaleFactor)+","+Math.round(defaultMarginHeight/scaleFactor)+");");
                    } else {
                        //System.out.println("hier !!!");
                        //planWebview.evaluateJavascript("setTableMargin("+Math.round(planWebview.defaultMarginWidth/planWebview.scaleFactor)+","+Math.round(planWebview.defaultMarginHeight/planWebview.scaleFactor)+");", null);


                        //this.evaluateJavascript("setTableMargin("+Math.round(defaultMarginWidth/1.5)+","+Math.round(defaultMarginHeight/1.5)+");", null);
                    }

                    blockSingleTouch = false;
                    return true;
                }

                return false;
            }
        });*/

        ll_mangel_detail_view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (defectsModelObj != null && defectsModelObj.getDefectLocalId() != 0) {

                    DefectsModel defectsModel = defectsModelObj;
                    Intent intent = new Intent(getActivity(), DefectDetailsActivity.class);
                    intent.putExtra(ARG_PROJECT_ID, defectsModel.getProjectId());
                    intent.putExtra(DefectsListFragment.ARG_PARAM2, defectsModel.getDefectLocalId() + "");
                    intent.putExtra("flaw_id", defectsModel.getDefectId());
                    intent.putExtra("photoId", "");
                    intent.putExtra(IS_CREATED_MANGEL_KEY, false);

                    intent.putExtra(DefectsActivity.TYPE_DEFECT_KEY, DefectsActivity.TYPE_DEFECT_UPDATE);

                    startActivity(intent);
                    ll_hidenView.performClick();
                }
            }
        });

        addEvent();

        return rootView;

    }

    private void addEvent() {
        updatePlanData = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                try {
                    if (getActivity() != null) {
                        String dfectsIdList = intent.getExtras().getString(DefectsActivity.BR_ACTION_UPDATE_PLAN_DEFECTS);

                        if (intent.getAction().equals(DefectsActivity.BR_ACTION_UPDATE_PLAN_DEFECTS)) {

                            if (dfectsIdList != null && !dfectsIdList.equals("")) {
                                List<String> items = Arrays.asList(dfectsIdList.split("\\s*,\\s*"));
                                applyPlanFilterAccordingToDefects(items);
                            } else {
                                new RetrievePlansUsingPLanIDTask(getActivity(), projectID, planID).execute();
                            }
//
                        }
                    }
                } catch (Exception e) {
                }

            }
        };
    }


    private class RetrievePlansUsingPLanIDTask extends AsyncTask<Void, Void, PlansModel> {
        private PlansDao mAsyncTaskDao;
        List<String> stringList = new ArrayList<>();
        ProjectsDatabase projectsDatabase;
        String planId;
        String projectId;

        RetrievePlansUsingPLanIDTask(Context context, String project_id, String plan_id) {
            projectsDatabase = ProjectsDatabase.getDatabase(context);
            mAsyncTaskDao = projectsDatabase.plansDao();
            projectId = project_id;
            planId = plan_id;

        }

        @Override
        protected PlansModel doInBackground(final Void... params) {


            PlansModel plansModel = mAsyncTaskDao.getPlansUsingPlanID(projectId, planID);


            return plansModel;
        }

        @Override
        protected void onPostExecute(PlansModel plansModelList) {
            super.onPostExecute(plansModelList);
            plansModelOBJ = plansModelList;

            if (plansModelOBJ != null && plansModelOBJ.getPlanPhotoPathLargeSize() != null && !plansModelOBJ.getPlanPhotoPathLargeSize().equals("")) {
                new GetAllDefects(mDefectRepository.getmDefectDao()).execute(projectID);
            } else {
                callGetPlanImageAPI(getActivity(), planID);
            }
        }
    }


    private class GetAllDefects extends AsyncTask<String, Void, List<Pdflawflag>> {
        private DefectsDao mAsyncTaskDao;

        GetAllDefects(DefectsDao dao) {
            mAsyncTaskDao = dao;
        }

        @Override
        protected List<Pdflawflag> doInBackground(final String... params) {

            List<Long> longList = mAsyncTaskDao.getDefectsUniqueDefectIds(params[0]);

            List<String> stringList = new ArrayList<>();
            for (int i = 0; i < longList.size(); i++) {
                stringList.add(longList.get(i) + "");
            }
            pdFlawFlagRepository = new PdFlawFlagRepository(getContext(), projectID, stringList);

            List<Pdflawflag> flawFlagList = pdFlawFlagRepository.getmDefectsPhotoDao().getFlawFlagListWithPlanID(projectID, planID, stringList);

            if (flawFlagList != null) {
                defectStatusList = new ArrayList<>();
                for (int i = 0; i < flawFlagList.size(); i++) {
                    DefectsModel defectsModel = mAsyncTaskDao.getDefectsOBJ(projectID, flawFlagList.get(i).getLocal_flaw_Id());
                    defectStatusList.add(defectsModel.getStatus());
                }
            }

            return flawFlagList;
        }

        @Override
        protected void onPostExecute(List<Pdflawflag> flawFlagList) {
            super.onPostExecute(flawFlagList);
            flawFlagsList = flawFlagList;
            if (flawFlagList.size() > 0 && isFromDefectScreen) {
                if (plansModelOBJ.getPlanPhotoPathLargeSize() != null && !plansModelOBJ.getPlanPhotoPathLargeSize().equals("")) {
                    myBitmap = BitmapFactory.decodeFile(plansModelOBJ.getPlanPhotoPathLargeSize());
                    planWidth = myBitmap.getWidth();
                    planHeight = myBitmap.getHeight();
                }
                mergStatusOnPhoto(flawFlagList);

            } else {
                if (plansModelOBJ.getPlanPhotoPathLargeSize() != null && !plansModelOBJ.getPlanPhotoPathLargeSize().equals("")) {
                    myBitmap = BitmapFactory.decodeFile(plansModelOBJ.getPlanPhotoPathLargeSize());
                    planWidth = myBitmap.getWidth();
                    planHeight = myBitmap.getHeight();
                    imagePath = plansModelOBJ.getPlanPhotoPathLargeSize();


                    mPhotoView.setImageBitmap(myBitmap);
                }
            }

            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    mPhotoView.setScale(3.0f);
                }
            }, 100);

        }
    }


    private double distance(double lat1, double lon1, double lat2, double lon2) {

        // dist = dist * 60 * 1.1515;
        double distance = Math.sqrt(Math.pow(lat2 - lat1, 2) + Math.pow(lon2 - lon1, 2));

        return (distance);
    }

    private double deg2rad(double deg) {
        return (deg * Math.PI / 180.0);
    }

    private double rad2deg(double rad) {
        return (rad * 180.0 / Math.PI);
    }

    private double meterDistanceBetweenPoints(float lat_a, float lng_a, float lat_b, float lng_b) {
        float pk = (float) (180.f / Math.PI);

        float a1 = lat_a / pk;
        float a2 = lng_a / pk;
        float b1 = lat_b / pk;
        float b2 = lng_b / pk;

        double t1 = Math.cos(a1) * Math.cos(a2) * Math.cos(b1) * Math.cos(b2);
        double t2 = Math.cos(a1) * Math.sin(a2) * Math.cos(b1) * Math.sin(b2);
        double t3 = Math.sin(a1) * Math.sin(b1);
        double tt = Math.acos(t1 + t2 + t3);

        return tt;
    }


    public static Point getLocationOnScreen(View view) {
        int[] location = new int[2];
        view.getLocationOnScreen(location);
        return new Point(location[0], location[1]);
    }

    private BroadcastReceiver updatePlanData = null;

    @Override
    public void onResume() {
        super.onResume();
        getActivity().registerReceiver(updatePlanData, new IntentFilter(DefectsActivity.BR_ACTION_UPDATE_PLAN_DEFECTS));//Fixed

        //System.out.println("##### ProjectDocuShowPlanFragment:onResume() scale:"+ planWebview.scaleFactor);

        //planWebview.setInitialScale((Math.round(100.0f)));

//		ProjectDocuDatabaseManager projectDocuDatabaseManager = new ProjectDocuDatabaseManager (getActivity());
//
//		if ((Integer) projectDocuDatabaseManager.selectDataFromPreferences(ProjectDocuDatabaseManager.COLUMN_PREFERENCES_COMPASS_VIEW_DIRECTION) == 1) {
//			((ProjectDocuMainActivity) getActivity()).activateCompass();
//		} else {
//			((ProjectDocuMainActivity) getActivity()).deactivateCompass();
//		}
//
//		if ((Integer) projectDocuDatabaseManager.selectDataFromPreferences(ProjectDocuDatabaseManager.COLUMN_PREFERENCES_LOCATION_VIA_GPS) == 1) {
//			((ProjectDocuMainActivity) getActivity()).activateGPS();
//			((ProjectDocuMainActivity) getActivity()).activateGPSButton();
//		} else {
//			((ProjectDocuMainActivity) getActivity()).deactivateGPS();
//		}
//
//        if (getReferenceArray() == null) {
//        	((ProjectDocuMainActivity) getActivity()).deactivateCompassButton();
//        	((ProjectDocuMainActivity) getActivity()).deactivateGPSButton();
//        	((ProjectDocuMainActivity) getActivity()).deactivateGPS();
//        	((ProjectDocuMainActivity) getActivity()).deactivateCompass();
//        	((ProjectDocuMainActivity) getActivity()).deactivateCompassNonStop();
//        	((ProjectDocuMainActivity) getActivity()).deactivateGPSNonStop();
//
//        	int duration = Toast.LENGTH_LONG;
//			Toast toast = Toast.makeText(((ProjectDocuMainActivity) getActivity()), getResources().getString(R.string.toast_plan_no_gps_reference), duration);
//			toast.show();
//        }
//
//        ((ProjectDocuMainActivity) getActivity()).menuBarLayout.setVisibility(View.INVISIBLE);


        //float currentScale = (Float) projectDocuDatabaseManager.selectDataFromPreferences(ProjectDocuDatabaseManager.COLUMN_PREFERENCES_CURRENT_SCALE);

		/*if (android.os.Build.VERSION.SDK_INT < 19) {
			//this.loadUrl("javascript:setTableMargin("+Math.round(defaultMarginWidth/scaleFactor)+","+Math.round(defaultMarginHeight/scaleFactor)+");");
		} else {
			//System.out.println("hier !!!");
			planWebview.scaleFactor = currentScale;
			planWebview.defaultMarginWidth = planWebview.defaultMarginWidth / currentScale;
			planWebview.defaultMarginHeight = planWebview.defaultMarginHeight / currentScale;
			planWebview.evaluateJavascript("setTableMargin(" + Math.round(planWebview.defaultMarginWidth / currentScale) + "," + Math.round(planWebview.defaultMarginHeight / currentScale) + ");", null);
		}*/


    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        //System.out.println("##### ProjectDocuShowPlanFragment:onViewCreated() scale:"+ planWebview.scaleFactor);
//		ProjectDocuDatabaseManager projectDocuDatabaseManager = new ProjectDocuDatabaseManager (getActivity());


//		ArrayList <Plan> planObjectList = projectDocuDatabaseManager.selectDataFromPlan(((ProjectDocuMainActivity) getActivity()).currentPlanId);

//		if (planObjectList != null && planObjectList.size() > 0) {
//			northDeviationAngle = -planObjectList.get(0).north_deviation_angle;
//		}

        blockAutoPosition = false;
        blockAutoDirection = false;

        plansRepository = new AllPlansRepository(getContext(), projectID);
        mDefectRepository = new DefectRepository(getActivity(), projectID);

        if (getActivity() != null && getActivity() instanceof DefectsActivity) {
            String filterDefectIds = ((DefectsActivity) getActivity()).filterDefectsIds;
            if (filterDefectIds != null && !filterDefectIds.equals("")) {
                List<String> items = Arrays.asList(filterDefectIds.split("\\s*,\\s*"));
                applyPlanFilterAccordingToDefects(items);
            } else {
                new RetrievePlansUsingPLanIDTask(getActivity(), projectID, planID).execute();
            }
        }
//        createWebview();

        //System.out.println("##### scaling: "+planWebview.getScaleX() + " - "+planWebview.getScaleY()+ " - " +planWebview.scaledCrosshairPositionX + " - " +planWebview.scaledCrosshairPositionY + " - " +planWebview.scaleFactor  + " - " +planWebview.getScale() + " - " + getResources().getDisplayMetrics().density);


//        if (flag.is_arrow_located == 1) {
//            planWebview.arrowRotationAngle = planWebview.oldRotationAngle;
//
//            setArrowPosition();
//
//            rotationArrow.setVisibility(View.VISIBLE);
//            arrowTouchHelperView.setVisibility(View.VISIBLE);
//            buttonArrowRotate.setImageResource(R.drawable.pd_button_rotate_on_041);
//        }

//		((ProjectDocuMainActivity) getActivity()).showMainLayout();
//		((ProjectDocuMainActivity) getActivity()).menuBarLayout.setVisibility(View.INVISIBLE);
    }

    public static Bitmap scaleDown(Bitmap realImage, float maxImageSize,
                                   boolean filter) {
        float ratio = Math.min(
                (float) maxImageSize / realImage.getWidth(),
                (float) maxImageSize / realImage.getHeight());
        int width = Math.round((float) ratio * realImage.getWidth());
        int height = Math.round((float) ratio * realImage.getHeight());

        Bitmap newBitmap = Bitmap.createScaledBitmap(realImage, width,
                height, filter);
        return newBitmap;
    }

    private void deactivateRotationLocator() {
//		ProjectDocuDatabaseManager projectDocuDatabaseManager = new ProjectDocuDatabaseManager (getActivity());

        planWebview.rotatingArrowLocation = false;
//		projectDocuDatabaseManager.updatePreferences(ProjectDocuDatabaseManager.COLUMN_PREFERENCES_IS_ARROW_LOCATED, 0);
//        flag.is_arrow_located = 0;
        rotationArrow.setVisibility(View.INVISIBLE);

        planWebview.arrowRotationAngle = 0.0f;
        planWebview.oldRotationAngle = 0.0f;

        planWebview.scrollingOn = true;
        arrowRotation = false;
    }

    /**
     * Speichert nur die Mapposition ab, damit die Karte auch ohne Abspeichern bei neuem Aufruf des Plans wieder an der selben Stelle steht.
     */
//	public void saveMapPositionTemporary() {
//		//System.out.println("#### ProjectDocuShowPlanFragment:saveMapPositionTemporary()");
//		int viewX = (int)planWebview.viewX;
//		int viewY = (int)planWebview.viewY;
//
//		ProjectDocuDatabaseManager projectDocuDatabaseManager = new ProjectDocuDatabaseManager (getActivity());
//
//		int rotationAngleForBackend = 180;
//
//		if ((Integer) projectDocuDatabaseManager.selectDataFromPreferences(ProjectDocuDatabaseManager.COLUMN_PREFERENCES_IS_ARROW_LOCATED) == 1) {
//			rotationAngleForBackend = (int)Math.round(planWebview.arrowRotationAngle);
//		}
//
//		// Koordinaten Korrektur wenn eine Pfeil-Verortung vorliegt
//		if ((Integer) projectDocuDatabaseManager.selectDataFromPreferences(ProjectDocuDatabaseManager.COLUMN_PREFERENCES_IS_ARROW_LOCATED) == 1) {
//			viewY = viewY * -1;
//		}
//
//		// InsertIntoFlags mit PhotoID = -1 wird benutzt, um die Verortung temporär zu speichern, wenn noch kein Photo gemacht wurde, bzw. um die letzte Verortung auch nach dem gemachten Photo beizubehalten.
//		// Wenn Verortung temporär, zuweisen wenn eine PlanId vorliegt
//		if (((ProjectDocuMainActivity) getActivity()).currentPlanId > -1) {
//			projectDocuDatabaseManager.insertIntoFlags(-666, null, ((ProjectDocuMainActivity) getActivity()).currentPlanId, 0, "", "", 0, "", rotationAngleForBackend, viewX, viewY, (int)planWebview.crosshairPositionY, (int)planWebview.crosshairPositionX, -666, "com\\projectdocu\\FlagVO", 0);
//		}
//		//System.out.println("##### SAVING: crossposX:"+planWebview.crosshairPositionX+" crossposY:"+planWebview.crosshairPositionY);
//	}
  /*  private void saveLocation() {
        //System.out.println("##### ProjectDocuShowPlanFragment:saveLocation()");

//		ProjectDocuDatabaseManager projectDocuDatabaseManager = new ProjectDocuDatabaseManager (getActivity());
//


        int[] location = new int[2];
        rotationArrow.getLocationOnScreen(location);
        Toast.makeText(getActivity(), "X axis is " + location[0] + "and Y axis is " + location[1], Toast.LENGTH_LONG).show();

        ImageButton closeImageButton = new ImageButton(getContext());
        Drawable image = getResources().getDrawable(R.drawable.green_circle_selected);
        closeImageButton.setBackgroundDrawable(image);
        FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(55, 55, Gravity.RIGHT | Gravity.TOP);
        lp.leftMargin = 500;
        lp.topMargin = 500;
        closeImageButton.setLayoutParams(lp);
        planWebview.addView(closeImageButton);
        if (flag.is_arrow_located == 1) {
            int rotationAngleForBackend = 180;

            if (flag.is_arrow_located == 1) {
                rotationAngleForBackend = (int) Math.round(planWebview.arrowRotationAngle);
            }

            int viewX = (int) planWebview.viewX;
            int viewY = (int) planWebview.viewY;
            HomeActivity.flags.degree = (int) Math.round(planWebview.arrowRotationAngle);
            HomeActivity.flags.viewx = viewX;
            HomeActivity.flags.viewy = viewY;
            HomeActivity.flags.xcoord = (int) planWebview.crosshairPositionX;
            HomeActivity.flags.ycoord = (int) planWebview.crosshairPositionY;
            HomeActivity.flags.scale_factor = planWebview.scaleFactor;
            HomeActivity.flags.tiltangle = 0;


            for (int i = 0; i < getActivity().getSupportFragmentManager().getBackStackEntryCount(); i++) {
                getActivity().getSupportFragmentManager().popBackStack();
            }

//            if (flag.is_arrow_located == 1) {
//                viewY = viewY * -1;
//            }

//			if (((ProjectDocuMainActivity) getActivity()).currentPlanId > -1) {
//				projectDocuDatabaseManager.deleteFlagWithId(((ProjectDocuMainActivity) getActivity()).currentPlanId,-666);
//
//				projectDocuDatabaseManager.insertIntoFlags(0, null, ((ProjectDocuMainActivity) getActivity()).currentPlanId, 0, "", "", 0, "", rotationAngleForBackend, viewX, viewY, (int)planWebview.crosshairPositionY, (int)planWebview.crosshairPositionX, -1, "com\\projectdocu\\FlagVO", 0);
//
//				if (localPhotoId != -1) {
//					projectDocuDatabaseManager.insertIntoFlags(0, null, ((ProjectDocuMainActivity) getActivity()).currentPlanId, 0, "", "", 0, "", rotationAngleForBackend, viewX, viewY, (int)planWebview.crosshairPositionY, (int)planWebview.crosshairPositionX, localPhotoId, "com\\projectdocu\\FlagVO", 1);
//				}
//			}


            //System.out.println("##### positionText SAVE in DB: "+planWebview.crosshairPositionX + " --- "+planWebview.crosshairPositionY + " --- "+viewX + " ---- " +viewY);
            //Toast.makeText(getActivity(),  "saved x:"+planWebview.crosshairPositionX + " y:"+planWebview.crosshairPositionY, Toast.LENGTH_LONG).show();
            Toast.makeText(getActivity(), getResources().getString(R.string.toast_location_saved), Toast.LENGTH_LONG).show();
        }
    }*/
    private void setArrowPosition() {
        getTwoPointsFromAngle(planWebview.oldRotationAngle);

        if (planWebview.oldRotationAngle > 0) {
            rotationArrow.setRotation((float) planWebview.oldRotationAngle);
            planWebview.arrowRotationAngle = (float) planWebview.oldRotationAngle;
        } else {
            rotationArrow.setRotation(0.0f);
            planWebview.arrowRotationAngle = 0.0f;

        }
    }

    // get the degree value from settings for the compass correction
    // (Needed for some devices which have wrong compass orientation)
//	public int getCompassCorrectionValue()
//	{
//		ProjectDocuDatabaseManager projectDocuDatabaseManager = new ProjectDocuDatabaseManager (getActivity());
//		String selectedCorrectionValue = (String) projectDocuDatabaseManager.selectDataFromPreferences(ProjectDocuDatabaseManager.COLUMN_PREFERENCES_SELECTED_COMPASS_CORRECTION);
//		int correctionValue;
//		if (selectedCorrectionValue != null) {
//			if (selectedCorrectionValue.equals("0 Grad")) {
//				correctionValue = 0;
//			} else if (selectedCorrectionValue.equals("90 Grad")) {
//				correctionValue= 90;
//			} else if (selectedCorrectionValue.equals("180 Grad")) {
//				correctionValue = 180;
//			}
//			// 270 Grad
//			else {
//				correctionValue = 270;
//			}
//		}
//		else{
//			correctionValue = 0;
//		}
//
//		return correctionValue;
//	}

//	public void setArrowByCompass(float compassDegree) {
//		if (blockAutoDirection == true) {
//			return;
//		}
//
//		// corrects the compassDegree by the settings value (just need for some devices)
//		float compassCorrectionValue =  getCompassCorrectionValue();
//		float fixedDegree = 270.0f + (compassDegree + 180.0f) + northDeviationAngle + rotateIcons + compassCorrectionValue;
//        //		float fixedDegree = 270.0f + (compassDegree + 180.0f) + northDeviationAngle;
//
//		if (fixedDegree > 0) {
//			planWebview.oldRotationAngle = fixedDegree;
//		}
//		else {
//			planWebview.oldRotationAngle = 360.0f + fixedDegree;
//		}
//
//		setArrowPosition();
//	}

    private void populateShortMangelDetail(DefectsModel defectsModel) {
        ll_hidenView.setVisibility(View.VISIBLE);
        rl_short_detail_view.setVisibility(View.VISIBLE);
        ll_expand_view.setVisibility(View.VISIBLE);

        ll_photos_view.setVisibility(View.GONE);
        ll_plans_view.setVisibility(View.GONE);


        if (defectsModel != null) {
            defectsModelObj = defectsModel;

            pdFlawFlagRepository = new PdFlawFlagRepository(getActivity(), defectsModelObj.getProjectId(), defectsModelObj.getDefectLocalId() + "");
            List<String> flawList = new ArrayList<>();
            flawList.add(defectsModel.defectLocalId + "");

            new RetrieveDefectFlagAsyncTask(pdFlawFlagRepository.getmDefectsPhotoDao(), flawList).execute(projectID);

            new RetrieveAsyncTask(ProjectsDatabase.getDatabase(getActivity()).photoDao()).execute(defectsModelObj.getProjectId(), defectsModelObj.getDefectLocalId() + "");

            if (defectsModel.getRunId() != null && !defectsModel.getRunId().equals("")) {
                tv_nr_no.setText("Nr. " + defectsModel.getRunId());
            } else {
                tv_nr_no.setText("Nr. ");
            }

            if(defectsModel.getDefectName() != null) {
                tvTitle.setText(defectsModel.getDefectName());
            }

            if (defectsModel.getDefectType() != null && !defectsModel.getDefectType().equals("")) {
                if (defectsModel.getDefectType().equals("1")) {
                    tv_art_text.setText(getResources().getString(R.string.mangel_art));
                } else if (defectsModel.getDefectType().equals("2")) {
                    tv_art_text.setText(getResources().getString(R.string.restleistung_art));
                }
            } else {
//                tv_art_text.setText(sharedPrefsManager.getStringValue(AppConstantsManager.USER_LAST_USED_FLAW_TYPE,""));
                String flawType = sharedPrefsManager.getStringValue(AppConstantsManager.USER_LAST_USED_FLAW_TYPE, "1");
                if (flawType.equals("1")) {
                    tv_art_text.setText(getResources().getString(R.string.mangel_art));
                } else if (flawType.equals("2")) {
                    tv_art_text.setText(getResources().getString(R.string.restleistung_art));
                }
            }

            if (defectsModel.getDescription() != null) {

                tv_description_text.setText(defectsModel.getDescription());
            }

            if (defectsModel.getFristDate() != null)
                tv_end_date.setText(defectsModel.getFristDate());
            if (defectsModel.getDiscipline() != null)
                tv_gewerk.setText(defectsModel.getDiscipline());


            if (defectsModel.getStatus() != null && defectsModel.getStatus().equalsIgnoreCase("1")) {
                iv_status_orange.setImageResource(R.drawable.yellow_circle_selected);
                iv_status_red.setImageResource(R.drawable.red_circle_background);
                iv_status_green.setImageResource(R.drawable.green_circle_background);
            } else if (defectsModel.getStatus() != null && defectsModel.getStatus().equalsIgnoreCase("2")) {
                iv_status_red.setImageResource(R.drawable.red_circle_selected);
                iv_status_orange.setImageResource(R.drawable.orange_circle_background);
                iv_status_green.setImageResource(R.drawable.green_circle_background);
            } else if (defectsModel.getStatus() != null && defectsModel.getStatus().equalsIgnoreCase("0")) {
                iv_status_green.setImageResource(R.drawable.green_circle_selected);
                iv_status_red.setImageResource(R.drawable.red_circle_background);
                iv_status_orange.setImageResource(R.drawable.orange_circle_background);
            }

            if (defectsModel.getResponsibleUser() != null && !defectsModel.getResponsibleUser().equals("")) {
                new RetrProjectUsersAsyncTask(projectDetailRepository.getWordDao()).executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, projectID, defectsModel.getResponsibleUser());
            }

        }
    }


    public void setPlanPositionByGps(Location planLocation) {
        //System.out.println("##### ProjectDocuShowPlanFragment:setPlanPositionByGps()");
        if (blockAutoPosition == true) {
            return;
        }

        if (planLocation == null) {
//			((ProjectDocuMainActivity) getActivity()).deactivateGPSButton();
            //   ((ProjectDocuMainActivity) getActivity()).deactivateGPSNonStop();

            int duration = Toast.LENGTH_LONG;

            Toast toast = Toast.makeText((getActivity()), getResources().getString(R.string.toast_gps_no_signal), duration);
            toast.show();

            return;
        }

        // Checken ob GPS-Genauigkeit über 100m liegt!
        if (planLocation.getAccuracy() > 100.0) {
//			((ProjectDocuMainActivity) getActivity()).deactivateGPSButton();
//			((ProjectDocuMainActivity) getActivity()).deactivateGPSNonStop();

            int duration = Toast.LENGTH_LONG;

            String strText = "" + getResources().getString(R.string.toast_gps_accuraccy_imprecise_info) + " " + planLocation.getAccuracy() + "m";

            Toast toast = Toast.makeText((getActivity()), strText, duration);
            toast.show();

            return;
        }

        GeoPoint locationPoint = new GeoPoint();

        locationPoint.lat = planLocation.getLatitude();
        locationPoint.lon = planLocation.getLongitude();
/*
	ProjectDocuDatabaseManager projectDocuDatabaseManager = new ProjectDocuDatabaseManager (getActivity());

		ArrayList <Plan> planObjectList = projectDocuDatabaseManager.selectDataFromPlan(((ProjectDocuMainActivity) getActivity()).currentPlanId);

		if (planObjectList != null && planObjectList.size() > 0) {
			if (planObjectList.get(0).refpointsjson != null && planObjectList.get(0).refpointsjson.length() > 0) {
				try {
					// Starte das Parsen des Check-for-Image-JSON vom Backend
					JSONArray dataForRefPointArray = (JSONArray)JSONValue.parse(planObjectList.get(0).refpointsjson);

					// Geopoint array nur initialisieren wenn mindestens zwei RefPoints vom Backend kommen
					if (dataForRefPointArray.size() >= 2) {
						refGeoPoints = new GeoPoint [2];

						refGeoPoints[0] = new GeoPoint ();
						refGeoPoints[1] = new GeoPoint ();

						JSONObject refGeoPoint1 = (JSONObject) dataForRefPointArray.get(0);
						JSONObject refGeoPoint2 = (JSONObject) dataForRefPointArray.get(1);

						if (refGeoPoint1 != null && refGeoPoint2 != null) {
							refGeoPoints[0].x	= (refGeoPoint1.get("x")	instanceof Long) ? (Long) refGeoPoint1.get("x")		: (Double) refGeoPoint1.get("x");
							refGeoPoints[0].y	= (refGeoPoint1.get("y")	instanceof Long) ? (Long) refGeoPoint1.get("y")		: (Double) refGeoPoint1.get("y");
							refGeoPoints[0].lon	= (refGeoPoint1.get("lon")	instanceof Long) ? (Long) refGeoPoint1.get("lon")	: (Double) refGeoPoint1.get("lon");
							refGeoPoints[0].lat	= (refGeoPoint1.get("lat")	instanceof Long) ? (Long) refGeoPoint1.get("lat")	: (Double) refGeoPoint1.get("lat");

							refGeoPoints[1].x	= (refGeoPoint2.get("x")	instanceof Long) ? (Long) refGeoPoint2.get("x")		: (Double) refGeoPoint2.get("x");
							refGeoPoints[1].y	= (refGeoPoint2.get("y")	instanceof Long) ? (Long) refGeoPoint2.get("y")		: (Double) refGeoPoint2.get("y");
							refGeoPoints[1].lon	= (refGeoPoint2.get("lon")	instanceof Long) ? (Long) refGeoPoint2.get("lon")	: (Double) refGeoPoint2.get("lon");
							refGeoPoints[1].lat	= (refGeoPoint2.get("lat")	instanceof Long) ? (Long) refGeoPoint2.get("lat")	: (Double) refGeoPoint2.get("lat");

							locationPoint = ProjectDocuUtilities.getLocations(locationPoint, refGeoPoints[0], refGeoPoints[1]);

							// Wenn GPS Position innerhalb des gewählten Plans ist, Position auf Karte setzen
							if (Math.abs(locationPoint.y) > 0 && Math.abs(locationPoint.y) < planWebview.planHeight / 2 && Math.abs(locationPoint.x) > 0 && Math.abs(locationPoint.x) < planWebview.planWidth) {
								setPlanPosition((float)locationPoint.x, (float)locationPoint.y);
							}

							else {
								((ProjectDocuMainActivity) getActivity()).deactivateGPSNonStop();

								int duration = Toast.LENGTH_LONG;

								Toast toast = Toast.makeText(((ProjectDocuMainActivity) getActivity()), getResources().getString(R.string.toast_gps_out_of_range), duration);
								toast.show();
							}
						} else {
							int duration = Toast.LENGTH_LONG;

							Toast toast = Toast.makeText(((ProjectDocuMainActivity) getActivity()), getResources().getString(R.string.toast_plan_no_gps_reference), duration);
							toast.show();

							refGeoPoints = null;
						}
					}
					else {
//						((ProjectDocuMainActivity) getActivity()).deactivateCompassButton();
//						((ProjectDocuMainActivity) getActivity()).deactivateGPSButton();
//						((ProjectDocuMainActivity) getActivity()).deactivateCompass();
//						((ProjectDocuMainActivity) getActivity()).deactivateGPS();
					}
				} catch (Exception e) {
					refGeoPoints = null;
				}
			} else {
				refGeoPoints = null;
			}
		} else {
			refGeoPoints = null;
		}*/
    }


    public void setLocatedPlanIcon() {
        ImageView planButton = (ImageView) rootView.findViewById(R.id.button_plan);
        planButton.setImageResource(R.drawable.pd_plan_overview_028);
    }


//	private void clearLocatedPlanIcon() {
//		ImageView planButton = (ImageView) rootView.findViewById(R.id.button_plan);
//		planButton.setImageResource(R.drawable.selector_plan_overview_button);
//	}

    private void moveArrowPosition(float touchPosX, float touchPosY) {
        if (blockAutoDirection == false) {
//			setBlockAutoDirection(true);
        }

        float centerX = (float) rotationArrow.getWidth() / 2.0f;
        float centerY = (float) rotationArrow.getHeight() / 2.0f;

        double rotation = getAngleFromTwoPoints(centerX, centerY, touchPosX, touchPosY);
        double arrowRotation = rotation;

        rotationArrow.setRotation(Math.round(arrowRotation));

        if (arrowRotation > 360.0f) {
            planWebview.arrowRotationAngle = (float) arrowRotation - 360.0f;
        } else {
            planWebview.arrowRotationAngle = (float) arrowRotation;
        }
    }

    private void getTwoPointsFromAngle(double angle) {
        double y = (Math.cos(angle * (Math.PI / 180)) * 100);
        double x = (Math.sin(angle * (Math.PI / 180)) * 100);

        planWebview.viewX = (float) x;
        planWebview.viewY = (float) y;
    }

    private double getAngleFromTwoPoints(float centerX, float centerY, float touchX, float touchY) {
        double dx = touchX - centerX;
        double dy = -(touchY - centerY);

        double inRads = Math.atan2(dy, dx);

        if (inRads < 0) {
            inRads = Math.abs(inRads);
        } else {
            inRads = 2 * Math.PI - inRads;
        }

        planWebview.viewX = (float) dx;
        planWebview.viewY = (float) dy;

        return Math.toDegrees(inRads) + 90;
    }

/*    // sets the plan position to a specific x/y koordinate
    // ATTENTION: the x/y position in the plan is not the crosshair or oldCrosshair position.
    public void setPlanPosition(float x, float y) {
        //System.out.println("##### ProjectDocuShowPlanFragment:setPlanPosition()");

//		ProjectDocuDatabaseManager projectDocuDatabaseManager = new ProjectDocuDatabaseManager (getActivity());

        planWebview.oldCrosshairPositionX = x;
        planWebview.oldCrosshairPositionY = y;


        float currentScale = 1;

        //currentScale = planWebview.oldScaleFactor;
        //System.out.println("###### positionText SetPlanPos !!! currentScale:"+currentScale + " x:"+x+" y:"+y + " oldCrossHairPositionX:"+planWebview.oldCrosshairPositionX+ " oldCrossHairPositionY:"+planWebview.oldCrosshairPositionY+ "planWebview.leftMagrin:"+planWebview.leftMargin + " planWebview.topMargin:"+planWebview.topMargin);
        //System.out.println("###### positionText SetPlanPos !!! initscale: "+Math.round(currentScale*100)+ "--- "+(int)(Math.round(planWebview.oldScaleFactor*100)) + " planWebview.resizedPlanWidth:"+planWebview.resizedPlanWidth+ " planWebview.resizedPlanHeight:"+planWebview.resizedPlanHeight + "planWebview.planWidth:"+planWebview.planWidth+" planWebview.planHeight:"+planWebview.planHeight);
        //planWebview.setInitialScale((int)(Math.round(150.0f)));
        //  planWebview.setInitialScale(Math.round(currentScale * 100.0f));

//		orig
//		planWebview.setInitialScale(Math.round((Float) projectDocuDatabaseManager.selectDataFromPreferences(ProjectDocuDatabaseManager.COLUMN_PREFERENCES_CURRENT_SCALE) * 100.0f));

        //x = Math.round(0+planWebview.leftMargin*currentScale);
        //y = Math.round(0+planWebview.topMargin*currentScale);
        //x = 0.0f;
        //y = 0.0f;

        //x = Math.round((planWebview.oldCrosshairPositionX*currentScale));
        //y = Math.round((planWebview.oldCrosshairPositionY*currentScale));

        //x = Math.round((planWebview.leftMargin + planWebview.resizedPlanWidth/2 - planWebview.displayWidth/2 + planWebview.oldCrosshairPositionX));
        //y = Math.round((planWebview.topMargin*currentScale + planWebview.resizedPlanHeight/2*currentScale - planWebview.displayHeight/2*currentScale + planWebview.oldCrosshairPositionY*currentScale));

        // currentScale = 1.0f;
        //planWebview.scaleFactor = currentScale;
        float marginCorrectionX = (planWebview.leftMargin * currentScale) - planWebview.leftMargin;
        float marginCorrectionY = (planWebview.topMargin * currentScale) - planWebview.topMargin;

        // when the plan was zoomed in our out we need to recalculate the x/y values we have to scroll the plan back to.
        if (currentScale > 1.1 || currentScale < 0.99) {
            //System.out.println("SCALED! currentScale:"+currentScale+" resizedPalnWidth:"+planWebview.resizedPlanWidth + " leftMargin:"+planWebview.leftMargin + " marginCorrectionX:"+marginCorrectionX+ " marginCorrectionY:"+marginCorrectionY);

            float scrollX = ((planWebview.oldCrosshairPositionX + (planWebview.planWidth / 2)) * currentScale);
            float scrollY = ((planWebview.oldCrosshairPositionY + (planWebview.planHeight / 2)) * currentScale);

            //System.out.println("SCALED! scrollX:"+scrollX+" scrollY:"+scrollY);

            //x = Math.round((scrollX+marginCorrectionX) * currentScale - planWebview.planWidth / 2);
            //y = Math.round((scrollY+marginCorrectionY) * currentScale - planWebview.planHeight / 2);

            //System.out.println("##### CHECK scrollX:"+scrollX+" marginCorrectionX:"+marginCorrectionX);

            x = scrollX + marginCorrectionX;
            y = scrollY + marginCorrectionY;

			*//*if (scrollX < 0)
			{
					System.out.println("SAVING OUTSIDE MAP!");
			}*//*


            //orig
            //x = Math.round((planWebview.leftMargin*currentScale + planWebview.resizedPlanWidth/2*currentScale - planWebview.displayWidth/2*currentScale + planWebview.oldCrosshairPositionX*currentScale));
            //y = Math.round((planWebview.topMargin*currentScale + planWebview.resizedPlanHeight/2*currentScale - planWebview.displayHeight/2*currentScale + planWebview.oldCrosshairPositionY*currentScale));

			*//*if (android.os.Build.VERSION.SDK_INT < 19) {
				//this.loadUrl("javascript:setTableMargin(" + Math.round(defaultMarginWidth / scaleFactor) + "," + Math.round(defaultMarginHeight / scaleFactor) + ");");
			} else {

				planWebview.evaluateJavascript("setTableMargin(" + Math.round(planWebview.defaultMarginWidth / planWebview.scaleFactor) + "," + Math.round(planWebview.defaultMarginHeight / planWebview.scaleFactor) + ");", null);
			}*//*
        }
        // if the plan is not zoomed we just need to calculate the x/y position by adding the margin, planwidth
        else {
            //System.out.println("NOT SCALED! currentScaleFactor:"+currentScale+" odlScaleFactor:"+planWebview.oldScaleFactor + " x:"+x+" (int)x:"+x);
//			x = Math.round((planWebview.leftMargin*currentScale + planWebview.resizedPlanWidth/2*currentScale - planWebview.displayWidth/2*currentScale + planWebview.oldCrosshairPositionX*currentScale));
//			y = Math.round((planWebview.topMargin*currentScale + planWebview.resizedPlanHeight/2*currentScale - planWebview.displayHeight/2*currentScale + planWebview.oldCrosshairPositionY*currentScale));
            x = Math.round((planWebview.leftMargin + planWebview.resizedPlanWidth / 2 - planWebview.displayWidth / 2 + planWebview.oldCrosshairPositionX));
            y = Math.round((planWebview.topMargin + planWebview.resizedPlanHeight / 2 - planWebview.displayHeight / 2 + planWebview.oldCrosshairPositionY));
        }


        planWebview.scrollTo((int) x, (int) y);


*//*		planWebview.scrollTo(
				Math.round((planWebview.leftMargin*currentScale + planWebview.resizedPlanWidth/2*currentScale - planWebview.displayWidth/2*currentScale + planWebview.oldCrosshairPositionX*currentScale)),
				Math.round((planWebview.topMargin*currentScale + planWebview.resizedPlanHeight/2*currentScale - planWebview.displayHeight/2*currentScale + planWebview.oldCrosshairPositionY*currentScale)));

				Math.round((planWebview.leftMargin*currentScale + planWebview.resizedPlanWidth/2*currentScale - planWebview.displayWidth/2*currentScale + planWebview.oldCrosshairPositionX*currentScale)),
				Math.round((planWebview.topMargin*currentScale + planWebview.resizedPlanHeight/2*currentScale - planWebview.displayHeight/2*currentScale + planWebview.oldCrosshairPositionY*currentScale)));
*//*
		*//*
		System.out.println("!!! planWebview.leftMargin:"+planWebview.leftMargin);
		System.out.println("!!! planWebview.resizedPlanWidth:"+planWebview.resizedPlanWidth);
		System.out.println("!!! planWebview.planWebview.displayWidth:"+planWebview.displayWidth);

		System.out.println("!!! planWebview.topMargin:"+planWebview.topMargin);
		System.out.println("!!! planWebview.resizedPlanHeight:"+planWebview.resizedPlanHeight);
	    System.out.println("!!! planWebview.planWebview.displayHeight:"+planWebview.displayHeight);
		*//*

        //System.out.println("!!! x y:"+x + "---" +y);
        //System.out.println("!!! planWebview.oldCrosshairPositionY*currentScale):"+planWebview.oldCrosshairPositionX*currentScale+" --- "+planWebview.oldCrosshairPositionY*currentScale);


    }*/

//	public Fragment getFragmentToReturnTo() {
////		ProjectDocuDatabaseManager projectDocuDatabaseManager = new ProjectDocuDatabaseManager (getActivity());
//
////		if (fragment == null) {
////			return new ProjectDocuCameraFragment();
////		}
////
//		return fragment;
//	}

    // obsolete ?
    private class ProjectDocuWebViewClient extends WebViewClient {
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            view.loadUrl(url);
            return false;
        }
    }

    public int getLocalPhotoId() {
        return this.localPhotoId;
    }

    private void callGetPlanImageAPI(Context context, String fileId) {
        Dialog pbDialog = ProjectNavigator.showCustomProgress(context, "", false);
        SharedPrefsManager sharedPrefsManager = new SharedPrefsManager(context);
        RetroApiInterface retroApiInterface = RetrofitManager.getInstance().create(RetroApiInterface.class);
        String authToken = sharedPrefsManager.getStringValue(AppConstantsManager.AUTH_API_TOKEN, "");

        if (authToken.length() > 2) {
            authToken = authToken.substring(1, authToken.length() - 1);
        }

        Call<ResponseBody> call = retroApiInterface.getPlanImageWithSize(authToken, Utils.DEVICE_ID, fileId, "xl");

        Log.d("call url", call.request().url().toString());

        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                pbDialog.dismiss();
                if (response.isSuccessful()) {
                    if (response.body() != null) {
                        Log.d("List", "Success : " + response.body());
                        if (writeResponseBodyToDisk(response.body(), projectID)) {
                            if (imagePath != null && !imagePath.equals("")) {

//                                myBitmap = BitmapFactory.decodeFile(imagePath);
                                myBitmap = BitmapFactory.decodeFile(imagePath);
//                     Bitmap bitmap=myBitmap.createScaledBitmap(myBitmap,myBitmap.getWidth()*2,myBitmap.getHeight()*2,true);
                                planWidth = myBitmap.getWidth();
                                planHeight = myBitmap.getHeight();
//								new PlansPhotoRepository.UpdateAsyncTask(mDefectsPhotoDao).execute(projectModel);
                                new GetAllDefects(mDefectRepository.getmDefectDao()).execute(projectID);
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
                Log.d("List", "failed : " + t.getMessage());
                pbDialog.dismiss();
            }
        });
    }


    private boolean writeResponseBodyToDisk(ResponseBody body, String projectId) {

        try {
            // todo change the file location/name according to your needs
            File dir = getActivity().getExternalFilesDir("/projectDocu/project_plans_" + projectId);
            if (dir == null) {
                dir = getActivity().getFilesDir();
            }
//            File dir = new File(Environment.getExternalStorageDirectory() + "/projectDocu/project_plans_" + projectId);
            if (!dir.isDirectory()) {
                dir.mkdirs();
            }
            File photo = new File(dir, "/Download_" + new Date().getTime() + ".jpg");

            imagePath = photo.getAbsolutePath();
//            editor.putString(PHOTO_SAVED_PATH_KEY, imagePath);
//            editor.commit();
            if (plansModelOBJ != null) {
                plansModelOBJ.setPlanPhotoPathLargeSize(imagePath);
                new updatePlansAsyncTask(ProjectsDatabase.getDatabase(getActivity()).plansDao()).execute(plansModelOBJ);
            }

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

    private class updatePlansAsyncTask extends AsyncTask<PlansModel, Void, Void> {
        private PlansDao mAsyncTaskDao;

        updatePlansAsyncTask(PlansDao dao) {
            mAsyncTaskDao = dao;
        }

        @Override
        protected Void doInBackground(final PlansModel... params) {
            mAsyncTaskDao.update(params[0]);
            return null;
        }
    }

    private boolean writeBitMapToDisk(Bitmap bitmap, String projectId) {

        try {
            // todo change the file location/name according to your needs
            File dir = getActivity().getExternalFilesDir("/projectDocu/project_plans_" + projectId);
            if (dir == null) {
                dir = getActivity().getFilesDir();
            }
//            File dir = new File(Environment.getExternalStorageDirectory() + "/projectDocu/project_plans_" + projectId);
            if (!dir.isDirectory()) {
                dir.mkdirs();
            }
            File photo = new File(dir, "/Download_" + new Date().getTime() + ".jpg");

            imagePath = photo.getAbsolutePath();
//            editor.putString(PHOTO_SAVED_PATH_KEY, imagePath);
//            editor.commit();

//            if (plansModelOBJ != null) {
//                plansModelOBJ.setPlanPhotoPathLargeSize(imagePath);
//                new updatePlansAsyncTask(ProjectsDatabase.getDatabase(getActivity()).plansDao()).execute(plansModelOBJ);
//            }

            InputStream inputStream = null;
            OutputStream outputStream = null;
            outputStream = new FileOutputStream(photo);

            try {

                outputStream = new FileOutputStream(photo);
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);


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
    public void onDestroy() {
        super.onDestroy();
        myBitmap = null;


        if (updatePlanData != null) {
            getActivity().unregisterReceiver(updatePlanData);
            updatePlanData = null;
        }
    }



    public String getFilename() {

        File file = getActivity().getExternalFilesDir("/projectDocu/project_plans_" + projectID);
        if (file == null) {
            file = getActivity().getFilesDir();
        }
//        File file = new File(Environment.getExternalStorageDirectory().getPath(), "MyFolder/Images");
        if (!file.exists()) {
            file.mkdirs();
        }
        String uriSting = (file.getAbsolutePath() + "/" + System.currentTimeMillis() + ".jpg");
        return uriSting;

    }

    private String getRealPathFromURI(String contentURI) {
        Uri contentUri = Uri.parse(contentURI);
        Cursor cursor = getActivity().getContentResolver().query(contentUri, null, null, null, null);
        if (cursor == null) {
            return contentUri.getPath();
        } else {
            cursor.moveToFirst();
            int index = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA);
            return cursor.getString(index);
        }
    }

    public int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {
            final int heightRatio = Math.round((float) height / (float) reqHeight);
            final int widthRatio = Math.round((float) width / (float) reqWidth);
            inSampleSize = heightRatio < widthRatio ? heightRatio : widthRatio;
        }
        final float totalPixels = width * height;
        final float totalReqPixelsCap = reqWidth * reqHeight * 2;
        while (totalPixels / (inSampleSize * inSampleSize) > totalReqPixelsCap) {
            inSampleSize++;
        }

        return inSampleSize;
    }

    private class RetrievePlansUsingFlawFlagAsyncTask extends AsyncTask<List<Pdflawflag>, Void, List<PlansModel>> {
        private PlansDao mAsyncTaskDao;
        List<String> stringList = new ArrayList<>();
        ProjectsDatabase projectsDatabase;
        boolean isFromDefectListing;
        String projectId = "";

        RetrievePlansUsingFlawFlagAsyncTask(Context context, String project_id) {
            projectsDatabase = ProjectsDatabase.getDatabase(context);
            mAsyncTaskDao = projectsDatabase.plansDao();
            projectId = project_id;


        }

        @Override
        protected List<PlansModel> doInBackground(final List<Pdflawflag>... params) {

            for (Pdflawflag plansModel : params[0]) {
                stringList.add(plansModel.getPdplanid());
            }
            List<PlansModel> plansModelList = mAsyncTaskDao.getPlansUsingDefect(projectId, stringList);
            if (plansModelList != null && plansModelList.size() > 0)
                new loadPlansImagesAsyncTask(getActivity()).execute(plansModelList.get(0).getPlanId());
            return plansModelList;


        }

        @Override
        protected void onPostExecute(List<PlansModel> plansModelList) {
            super.onPostExecute(plansModelList);


        }
    }

    private class loadPlansImagesAsyncTask extends AsyncTask<String, Void, PlansPhotoModel> {
        private ProjectsDatabase database;
        PlansPhotoModel plansPhotoModel;
        boolean isOnpostCalled;

        loadPlansImagesAsyncTask(Context context) {
            database = ProjectsDatabase.getDatabase(context);
        }

        @Override
        protected PlansPhotoModel doInBackground(final String... params) {
            PlansPhotoModel plansModel = database.planPhotosDao().getPlansPhotoObject(params[0]);

            return plansModel;
        }

        @Override
        protected void onPostExecute(PlansPhotoModel aVoid) {
            super.onPostExecute(aVoid);
            isOnpostCalled = true;
            plansPhotoModel = aVoid;

            if (plansPhotoModel != null) {
                ll_plans_view.setVisibility(View.VISIBLE);
                if (plansPhotoModel.getPohotPath() != null) {
                    Glide.with(getActivity()).load(plansPhotoModel.getPohotPath()).into(iv_plans);
                }
            }


        }


    }

    private class RetrieveDefectFlagAsyncTask extends AsyncTask<String, Void, List<Pdflawflag>> {
        private PdFlawFLagListDao mAsyncTaskDao;
        List<String> defectlist;

        RetrieveDefectFlagAsyncTask(PdFlawFLagListDao dao, List<String> stringList) {
            mAsyncTaskDao = dao;
            defectlist = stringList;
        }

        @Override
        protected List<Pdflawflag> doInBackground(final String... params) {
            List<Pdflawflag> flawFlagList = mAsyncTaskDao.getFlawFlagList(params[0], defectlist);
            return flawFlagList;
        }

        @Override
        protected void onPostExecute(List<Pdflawflag> pdflawflags) {
            super.onPostExecute(pdflawflags);

            new RetrievePlansUsingFlawFlagAsyncTask(getActivity(), projectID).execute(pdflawflags);
        }
    }

    private class RetrieveAsyncTask extends AsyncTask<String, Void, PhotoModel> {
        private PhotoDao mAsyncTaskDao;

        RetrieveAsyncTask(PhotoDao dao) {
            mAsyncTaskDao = dao;
        }

        @Override
        protected PhotoModel doInBackground(final String... params) {
//            setmDefectedPhotos( mAsyncTaskDao.getAllDefectPhotoModel());
            // if( getmDefectedPhotos().getValue()!=null)

            PhotoModel photoModel = mAsyncTaskDao.getDefectPhotosAndLocalPhotosOBj(params[0], params[1]);


            return photoModel;
        }

        @Override
        protected void onPostExecute(PhotoModel photoModel) {
            super.onPostExecute(photoModel);

            if (photoModel != null) {


                if (photoModel.getPath() != null && !photoModel.getPath().equals("")) {
                    Glide.with(getActivity()).load(photoModel.getPath()).into(iv_photo);
                    ll_photos_view.setVisibility(View.VISIBLE);
                } else {
                    ll_photos_view.setVisibility(View.GONE);
                }
                if (photoModel.getCreated() != null && !photoModel.getCreated().equals("")) {
                    tv_photo_date.setText(photoModel.getCreated());
                    tv_photo_date.setVisibility(View.VISIBLE);
                } else {
                    tv_photo_date.setVisibility(View.GONE);
                }

            }
        }
    }

    private class RetrProjectUsersAsyncTask extends AsyncTask<String, Void, ProjectUserModel> {

        private ProjectUsersDao mAsyncTaskDao;

        RetrProjectUsersAsyncTask(ProjectUsersDao dao) {
            mAsyncTaskDao = dao;
        }

        @Override
        protected ProjectUserModel doInBackground(final String... params) {
            ProjectUserModel stringList = mAsyncTaskDao.getProjectUserInfo(params[0], params[1]);
            return stringList;
        }

        @Override
        protected void onPostExecute(ProjectUserModel params) {
            super.onPostExecute(params);
            if (params != null) {

                if (params.getFirstname() != null && !params.getFirstname().equals("")) {
                    SharedPrefsManager sharedPrefsManager = new SharedPrefsManager(getActivity());
                    String firstName = sharedPrefsManager.getStringValue(AppConstantsManager.USER_FIRST_NAME, "");
                    String lastName = sharedPrefsManager.getStringValue(AppConstantsManager.USER_LAST_NAME, "");

//                    if (params.getLastname() != null && !params.getLastname().equals("")) {
//                        tv_res_user_name_text.setText(params.getFirstname() + " " + params.getLastname());
//                    } else
//                        tv_res_user_name_text.setText(params.getFirstname());

                    if (firstName != null && !lastName.equals("")) {
                        tv_res_user_name_text.setText(firstName + " " + lastName);
                    } else
                        tv_res_user_name_text.setText(lastName);

                }


            }

        }


    }


    private class RetrieveDefectAsyncTask extends AsyncTask<String, Void, DefectsModel> {
        private DefectsDao mAsyncTaskDao;

        RetrieveDefectAsyncTask(DefectsDao dao) {
            mAsyncTaskDao = dao;
        }

        @Override
        protected DefectsModel doInBackground(final String... params) {
//            setmDefectedPhotos( mAsyncTaskDao.getAllDefectPhotoModel());
            // if( getmDefectedPhotos().getValue()!=null)

            defectsModelObj = mAsyncTaskDao.getDefectsOBJ(params[0], params[1]);


            return defectsModelObj;
        }

        @Override
        protected void onPostExecute(DefectsModel defectsModelObj) {
            super.onPostExecute(defectsModelObj);

            if (defectsModelObj != null) {

                populateShortMangelDetail(defectsModelObj);
            }
        }
    }


    public void applyPlanFilterAccordingToDefects(List<String> stringList) {

        if (stringList != null && stringList.size() > 0) {

            new FilterDefectsUsingFlawFlagAsyncTask(getActivity()).execute(stringList);
        }
    }

    private class FilterDefectsUsingFlawFlagAsyncTask extends AsyncTask<List<String>, Void, List<Pdflawflag>> {
        private DefectsDao mAsyncTaskDao;
        private PlansDao mAsyncTaskPlanDao;
        List<String> stringList = new ArrayList<>();
        ProjectsDatabase projectsDatabase;
        boolean isFromDefectListing;

        FilterDefectsUsingFlawFlagAsyncTask(Context context) {
            projectsDatabase = ProjectsDatabase.getDatabase(context);
            mAsyncTaskDao = projectsDatabase.defectsDao();
            mAsyncTaskPlanDao = projectsDatabase.plansDao();

        }

        @Override
        protected List<Pdflawflag> doInBackground(final List<String>... params) {


            stringList = params[0];
            plansModelOBJ = mAsyncTaskPlanDao.getPlansUsingPlanID(projectID, planID);

            pdFlawFlagRepository = new PdFlawFlagRepository(getContext(), projectID, stringList);

            List<Pdflawflag> flawFlagList = pdFlawFlagRepository.getmDefectsPhotoDao().getFlawFlagListWithPlanID(projectID, planID, stringList);

            if (flawFlagList != null) {
                defectStatusList = new ArrayList<>();
                for (int i = 0; i < flawFlagList.size(); i++) {
                    DefectsModel defectsModel = mAsyncTaskDao.getDefectsOBJ(projectID, flawFlagList.get(i).getLocal_flaw_Id());
                    defectStatusList.add(defectsModel.getStatus());
                }
            }

            return flawFlagList;
        }

        @Override
        protected void onPostExecute(List<Pdflawflag> flawFlagList) {
            super.onPostExecute(flawFlagList);
            flawFlagsList = flawFlagList;
            if (flawFlagList.size() > 0 && isFromDefectScreen) {
                if (plansModelOBJ.getPlanPhotoPathLargeSize() != null && !plansModelOBJ.getPlanPhotoPathLargeSize().equals("")) {
                    myBitmap = BitmapFactory.decodeFile(plansModelOBJ.getPlanPhotoPathLargeSize());
                    planWidth = myBitmap.getWidth();
                    planHeight = myBitmap.getHeight();
                }
                mergStatusOnPhoto(flawFlagList);

            } else {
                if (plansModelOBJ.getPlanPhotoPathLargeSize() != null && !plansModelOBJ.getPlanPhotoPathLargeSize().equals("")) {
                    myBitmap = BitmapFactory.decodeFile(plansModelOBJ.getPlanPhotoPathLargeSize());
                    planWidth = myBitmap.getWidth();
                    planHeight = myBitmap.getHeight();
                    imagePath = plansModelOBJ.getPlanPhotoPathLargeSize();


                    mPhotoView.setImageBitmap(myBitmap);
                }
            }

            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    mPhotoView.setScale(3.0f);
                }
            }, 100);

        }
    }


}
