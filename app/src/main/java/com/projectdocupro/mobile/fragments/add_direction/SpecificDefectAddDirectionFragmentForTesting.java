package com.projectdocupro.mobile.fragments.add_direction;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Picture;
import android.graphics.Point;
import android.graphics.drawable.BitmapDrawable;
import android.location.Location;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.WebSettings;
import android.webkit.WebSettings.RenderPriority;
import android.webkit.WebView;
import android.webkit.WebView.PictureListener;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.projectdocupro.mobile.ProjectNavigator;
import com.projectdocupro.mobile.ProjectsDatabase;
import com.projectdocupro.mobile.R;
import com.projectdocupro.mobile.activities.HomeActivity;
import com.projectdocupro.mobile.activities.SavePictureActivity;
import com.projectdocupro.mobile.dao.DefectsDao;
import com.projectdocupro.mobile.dao.PlansDao;
import com.projectdocupro.mobile.fragments.DefectDetailAllPlansFragment;
import com.projectdocupro.mobile.interfaces.RetroApiInterface;
import com.projectdocupro.mobile.managers.AppConstantsManager;
import com.projectdocupro.mobile.managers.RetrofitManager;
import com.projectdocupro.mobile.managers.SharedPrefsManager;
import com.projectdocupro.mobile.models.DefectsModel;
import com.projectdocupro.mobile.models.Pdflawflag;
import com.projectdocupro.mobile.models.PlansModel;
import com.projectdocupro.mobile.repos.PdFlawFlagRepository;
import com.projectdocupro.mobile.utility.Utils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

import androidx.fragment.app.Fragment;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

@SuppressLint("ValidFragment")
public class SpecificDefectAddDirectionFragmentForTesting extends Fragment {
    private Bitmap myBitmap = null;
    private GestureDetector gd;
    private View rootView = null;
    private EnhancedWebView planWebview = null;

    private Fragment fragment = null;
    private int localPhotoId = -1;
    private int rotateIcons = 0;
    private boolean arrowRotation = false;

    Pdflawflag pdflawflagOBJ;

//    private DefectDetailsActivity projectDocuMainActivity = null;


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

    public static SpecificDefectAddDirectionFragmentForTesting projectDocuShowPlanFragment;
    static final String PHOTO_SAVED_PATH_KEY = "photo_path_key";
    private Bitmap bitmap, mask, rotatedMask;
    double bmWidth, bmHeight;
    float touchX, touchY;
    ImageView iv_temp;

    private Activity activity = null;

//	private ProjectDocuCameraFragment projectDocuCameraFragment = null;

    final ProjectDocuUtilities projectDocuUtilities = new ProjectDocuUtilities();
    private String photoURL = "/storage/emulated/0/projectDocu/project_plans_44/Download_1565001396991.jpg";
    private ImageView button_save_and_back;
    private String imagePath = "";
    private SharedPreferences pref;
    private SharedPreferences.Editor editor;
    private Flags flag = new Flags();

    ImageView iv_gps;
    ImageView iv_compass;
    ImageView iv_plan;
    ImageView iv_delete;

    LinearLayout ll_cancel, ll_save, ll_bottom_tabs, ll_top_tabs;
    private String projectID = "";
    private String planID = "";
    private String flawId = "";
    private String photoId = "";
    private boolean isFromPhoto;
    private boolean isReloadScreen;
    private PlansModel plansModelOBJ = null;
    private ImageButton ib_crossHair;
    private List<String> defectStatusList;
    private boolean isShowAutoEditMode;
    private boolean isLocationUpdate;


    public SpecificDefectAddDirectionFragmentForTesting(Pdflawflag pdflawflag, String projectId, String planId, String flawID, boolean isfromPhoto) {
        pdflawflagOBJ = pdflawflag;
        this.localPhotoId = localPhotoId;
        planID = planId;
        projectID = projectId;
        flawId = flawID;
        isFromPhoto = isfromPhoto;

//        myBitmap = BitmapFactory.decodeFile(photoURL);

    }

    public SpecificDefectAddDirectionFragmentForTesting(Pdflawflag pdflawflag, String projectId, String planId, String flawID, boolean isfromPhoto, boolean isLoadScreen) {
        pdflawflagOBJ = pdflawflag;
        this.localPhotoId = localPhotoId;
        planID = planId;
        projectID = projectId;
        flawId = flawID;
        isFromPhoto = isfromPhoto;
        isReloadScreen = isLoadScreen;

//        myBitmap = BitmapFactory.decodeFile(photoURL);

    }

    public SpecificDefectAddDirectionFragmentForTesting() {

    }

    private void initialzeTimerTask() {
        timerTask = new TimerTask() {
            public void run() {
                handler.post(new Runnable() {
                    public void run() {
                        Calendar calendar = Calendar.getInstance();
                        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd:MMMM:yyyy HH:mm:ss a");
                        final String strDate = simpleDateFormat.format(calendar.getTime());

                        int duration = Toast.LENGTH_SHORT;
                        Toast toast = Toast.makeText((getActivity()), strDate, duration);
                        toast.show();
                    }
                });
            }
        };
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        this.activity = activity;
//        if (activity != null) {
            activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
//            activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
//        }
    }

    @Override
    public void onDetach() {
        super.onDetach();


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
        Bitmap
                dImages = null;

//        iv_temp.setVisibility(View.VISIBLE);
//        iv_temp.setImageBitmap(mergedImages);
        Bitmap   result = Bitmap.createBitmap(bigImage.getWidth(), bigImage.getHeight(), Bitmap.Config.ARGB_8888);
        float centerX=bigImage.getWidth() / 2;
        float centerY=bigImage.getHeight() / 2;
            Canvas  canvas = new Canvas(result);
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
            if(smallImage!=null&&bigImage!=null) {
//                if(!isLocationUpdate)
//                    canvas.drawBitmap(smallImage,((bigImage.getWidth() / 2)-40  )+Integer.valueOf(pdflawflagList.get(i).getXcoord()),((bigImage.getHeight() / 2)-170) +Integer.valueOf(pdflawflagList.get(i).getYcoord()) , null);
//                else

//                int xWithZoom= (int) ((Integer.valueOf(pdflawflagList.get(i).getXcoord()))/pdflawflagList.get(i).getScale_factor());
//                int yWithZoom= (int) ((Integer.valueOf(pdflawflagList.get(i).getYcoord()))/pdflawflagList.get(i).getScale_factor());
                int xWithZoom= 0;
                int yWithZoom=0;

                if(Float.valueOf(pdflawflagList.get(i).getScale_factor())>10){

                    xWithZoom= (int) ((500)/pdflawflagList.get(i).getScale_factor());
                    yWithZoom= (int) ((600)/pdflawflagList.get(i).getScale_factor());
                }else if(Float.valueOf(pdflawflagList.get(i).getScale_factor())>6){

                    xWithZoom= (int) ((200)/pdflawflagList.get(i).getScale_factor());
                    yWithZoom= (int) ((340)/pdflawflagList.get(i).getScale_factor());
                }
                else if(Float.valueOf(pdflawflagList.get(i).getScale_factor())>4){

                    xWithZoom= (int) ((140)/pdflawflagList.get(i).getScale_factor());
                    yWithZoom= (int) ((240)/pdflawflagList.get(i).getScale_factor());
                }
                else if(Float.valueOf(pdflawflagList.get(i).getScale_factor())>2){

                    xWithZoom= (int) ((110)/pdflawflagList.get(i).getScale_factor());
                    yWithZoom= (int) ((210)/pdflawflagList.get(i).getScale_factor());
                } else{
                    xWithZoom= (int) ((50)/pdflawflagList.get(i).getScale_factor());
                    yWithZoom= (int) ((70)/pdflawflagList.get(i).getScale_factor());

                }


//                canvas.drawBitmap(smallImage,((bigImage.getWidth() / 2)-40  )+Integer.valueOf(pdflawflagList.get(i).getXcoord()),((bigImage.getHeight() / 2)-70) +Integer.valueOf(pdflawflagList.get(i).getYcoord()) , null);
                canvas.drawBitmap(smallImage,((bigImage.getWidth() / 2)-(xWithZoom )+Integer.valueOf(pdflawflagList.get(i).getXcoord())),((bigImage.getHeight() / 2)-yWithZoom )+Integer.valueOf(pdflawflagList.get(i).getYcoord() ) , null);
            }

//            canvas.drawBitmap(smallImage, (bigImage.getWidth() / 2-smallImage.getWidth())  , (bigImage.getHeight() / 2-smallImage.getHeight()) , null);
//            canvas.drawBitmap(smallImage, (bigImage.getWidth() / 2) + Integer.valueOf(pdflawflagList.get(i).getXcoord()) , (bigImage.getHeight() / 2) + Integer.valueOf(pdflawflagList.get(i).getYcoord()) , null);
//            canvas.drawBitmap(smallImage, (bigImage.getWidth() / 2) + Integer.valueOf(pdflawflagList.get(i).getXcoord()) - 20, (bigImage.getHeight() / 2) + Integer.valueOf(pdflawflagList.get(i).getYcoord()) - 50, null);

        }

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
        Matrix matrix = new Matrix();
        matrix.reset();
        matrix.postTranslate((bigImage.getWidth() / 2) + flag.xcoord, (bigImage.getHeight() / 2) + flag.ycoord); // Centers image
        matrix.postRotate(0);
//        matrix.postScale()
        canvas.drawBitmap(smallImage, (bigImage.getWidth() / 2) + flag.xcoord - 20, (bigImage.getHeight() / 2) + flag.ycoord - 50, null);

        Bitmap orgImage = BitmapFactory.decodeResource(getResources(), R.drawable.yellow_circle_selected);

        canvas.drawBitmap(orgImage, (bigImage.getWidth() / 2) + 441 - 20, (bigImage.getHeight() / 2) + 417 - 50, null);
        return result;
    }

    @SuppressWarnings("deprecation")
    @Override
    public View onCreateView(LayoutInflater layoutInflater, ViewGroup viewGroup, Bundle savedInstanceState) {
        //System.out.println("##### ProjectDocuShowPlanFragment:onCreateView()");
        rootView = layoutInflater.inflate(R.layout.specific_defect_add_direction_fragment_layout, viewGroup, false);
        pref = getActivity().getApplicationContext().getSharedPreferences("MyPref", 0); // 0 - for private mode
        editor = pref.edit();
        setRetainInstance(true);

//        flag = HomeActivity.flags;

//        if(flag.xcoord==null){
//            flag.degree=270;
//            flag.xcoord=648;
//            flag.ycoord=416;
//            flag.viewy=219;
//            flag.viewx=86;
//            flag.scale_factor=0.0f;
//            flag.is_arrow_located=1;
//            flag.tiltangle=0;
//
//        }
//        else

        if(pdflawflagOBJ==null)
            isShowAutoEditMode=true;


        if (pdflawflagOBJ != null) {

            if (pdflawflagOBJ.degree != null)
                flag.degree = pdflawflagOBJ.degree;
            else
                flag.degree = 0;

            if (pdflawflagOBJ.getXcoord() != null)
                flag.xcoord = Integer.valueOf(pdflawflagOBJ.getXcoord());
            else
                flag.xcoord = 0;
            if (pdflawflagOBJ.getYcoord() != null)
                flag.ycoord = Integer.valueOf(pdflawflagOBJ.getYcoord());
            else
                flag.ycoord = 0;
//            flag.ycoord = Integer.valueOf(pdflawflagOBJ.getYcoord());
            if (pdflawflagOBJ.getViewx() != null)
                flag.viewx = Integer.valueOf(pdflawflagOBJ.getViewx());
            else
                flag.viewx = 0;

            if (pdflawflagOBJ.getViewy() != null)
                flag.viewy = Integer.valueOf(pdflawflagOBJ.getViewy());
            else
                flag.viewy = 0;
            if (pdflawflagOBJ.scale_factor > 0)
                flag.scale_factor = pdflawflagOBJ.scale_factor;
            else
                flag.scale_factor = 1;
            if (pdflawflagOBJ.is_arrow_located > 0) {
                flag.is_arrow_located = 1;
            } else {
                flag.is_arrow_located = 0;
            }
            flag.tiltangle = 0;

        } else {
            pdflawflagOBJ = new Pdflawflag();
        }

//        flag = HomeActivity.flags;


        if (flag.xcoord == null) {
            flag.degree = 0;
            flag.xcoord = -365;
            flag.ycoord = 103;
            flag.viewy = 100;
            flag.viewx = 0;
            flag.scale_factor = 0.9999f;
            flag.is_arrow_located = 0;
            flag.tiltangle = 0;
        }
//        /else
//            flag.scale_factor=0.f;


        iv_gps = (ImageView) rootView.findViewById(R.id.iv_compass);
        iv_compass = (ImageView) rootView.findViewById(R.id.iv_compass);
        iv_plan = (ImageView) rootView.findViewById(R.id.iv_plan);
        iv_delete = (ImageView) rootView.findViewById(R.id.iv_delete);

        ll_cancel = (LinearLayout) rootView.findViewById(R.id.ll_previous);
        ll_save = (LinearLayout) rootView.findViewById(R.id.ll_next);
        ll_bottom_tabs = (LinearLayout) rootView.findViewById(R.id.ll_bottom_tabs);
        ll_top_tabs = (LinearLayout) rootView.findViewById(R.id.ll_icons);
        ll_bottom_tabs.setVisibility(View.GONE);

//        projectDocuMainActivity = ((DefectDetailsActivity) getActivity());

        rotationArrow = (ImageView) rootView.findViewById(R.id.rotation_arrow);

        planWebview = (EnhancedWebView) rootView.findViewById(R.id.webview_plan);

        touchHelperView = (ImageView) rootView.findViewById(R.id.touch_helper_view);

        captureButton = (ImageView) rootView.findViewById(R.id.button_capture_photo);

        debugTextView = (TextView) rootView.findViewById(R.id.debugTextView);
        debugTextView.setText("DEBUG TEXTVIEW:");
        iv_temp = rootView.findViewById(R.id.iv_temp);

//        if (isFromPhoto)
//            ll_top_tabs.setVisibility(View.GONE);


//        if (((ProjectDocuMainActivity) getActivity()).planCalledFromLocalPhotos == true){
//        	captureButton.setImageResource(R.drawable.pd_button_gallery_012);
//        }

//        captureButton.setOnClickListener(
//			new OnClickListener () {
//				@Override
//				public void onClick(View v) {
//					ProjectDocuDatabaseManager projectDocuDatabaseManager = new ProjectDocuDatabaseManager (getActivity());
//
//					saveLocation();
//
//					((ProjectDocuMainActivity) getActivity()).startSynchronizationService();
//
//					if (localPhotoId == -1 || (Integer) projectDocuDatabaseManager.selectDataFromPreferences(ProjectDocuDatabaseManager.COLUMN_PREFERENCES_LOCATION_ALWAYS_ON) == 1) {
//						((ProjectDocuMainActivity) getActivity()).setContentFragment(new ProjectDocuCameraFragment());
//					}
//					else {
//						((ProjectDocuMainActivity)getActivity()).setContentFragment(new ProjectDocuShowPhotoFragment(null, localPhotoId));
//					}
//				}
//			}
//		);

        projectDocuShowPlanFragment = this;

        arrowTouchHelperView = (ImageView) rootView.findViewById(R.id.arrow_touch_helper_view);

        bitmap = ((BitmapDrawable) arrowTouchHelperView.getDrawable()).getBitmap();
        mask = BitmapFactory.decodeResource(getResources(), R.drawable.rotation_arrow_helper);
        bmWidth = (double) bitmap.getWidth();
        bmHeight = (double) bitmap.getHeight();

        arrowTouchHelperView.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
//				ProjectDocuDatabaseManager projectDocuDatabaseManager = new ProjectDocuDatabaseManager (getActivity());

                if (arrowTouchHelperView.getVisibility() == View.GONE) {
                    return false;
                }

                if (event.getAction() == MotionEvent.ACTION_UP) {
                    arrowIsTouched = false;
                    return false;
                }

                float currentRotation = rotationArrow.getRotation();
                Matrix matrix = new Matrix();
                matrix.postRotate(Math.round(currentRotation));
                rotatedMask = Bitmap.createBitmap(mask, 0, 0, mask.getWidth(), mask.getHeight(), matrix, true);

                if (arrowIsTouched == false) {
                    touchX = event.getX();
                    touchY = event.getY();
                    long maskColor = getColor(touchX, touchY);

                    if (maskColor == 0) {
                        return false;
                    } else {
                        arrowIsTouched = true;
                    }
                }

                if (event.getAction() == MotionEvent.ACTION_DOWN || event.getAction() == MotionEvent.ACTION_MOVE) {
                    moveArrowPosition(event.getX(), event.getY());
//					projectDocuDatabaseManager.updatePreferences(ProjectDocuDatabaseManager.COLUMN_PREFERENCES_IS_LOCATED, 1);
                    flag.is_arrow_located = 1;
                    if (event.getAction() == MotionEvent.ACTION_SCROLL) {
                        setLocatedPlanIcon();
                    }

                    return true;
                }

                return false;
            }
        });

        buttonPlan = (ImageView) rootView.findViewById(R.id.button_plan);
        buttonPlan.setOnClickListener(
                new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        planWebview.scrollingOn = true;
                        arrowRotation = false;

//						((ProjectDocuMainActivity) getActivity()).setContentFragment(new ProjectDocuPlanListFragment(false, localPhotoId));
                    }
                }
        );

        buttonCancel = (ImageView) rootView.findViewById(R.id.button_cancel_and_back);
        ll_cancel.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
//                (getActivity()).onBackPressed();
                hideEditMode();
            }
        });

        iv_plan.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {


                for (Fragment fragment : getActivity().getSupportFragmentManager().getFragments()) {
                    if (fragment instanceof SpecificDefectAddDirectionFragmentForTesting) {
                        getActivity().getSupportFragmentManager().beginTransaction().remove(fragment).commit();
                        break;
                    }

                }
            }
        });

        iv_delete.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {

                showCustomDialog(getActivity(), getResources().getString(R.string.custom_dialog_title), getResources().getString(R.string.plan_location_delete_msg), 2, 0);

            }
        });

        iv_compass.setOnClickListener(new OnClickListener() {
                                          @Override
                                          public void onClick(View v) {
                                              ll_bottom_tabs.setVisibility(View.VISIBLE);
                                              showEditMode();
                                          }
                                      }
        );

        planSubmenuButtonsExpanded = (LinearLayout) rootView.findViewById(R.id.plan_submenu_buttons_expanded);
        buttonConfig = (ImageView) rootView.findViewById(R.id.button_config);
        buttonConfig.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (planSubmenuButtonsExpanded.getVisibility() == View.VISIBLE) {
                    planSubmenuButtonsExpanded.setVisibility(View.INVISIBLE);
                } else {
                    planSubmenuButtonsExpanded.setVisibility(View.VISIBLE);
                }
            }
        });

        buttonArrowRotate = (ImageView) rootView.findViewById(R.id.button_arrow_rotate);
        buttonArrowRotate.setOnClickListener(
                new OnClickListener() {
                    @Override
                    public void onClick(View v) {


                        if (switchArrowMode == false && rotationArrow.getVisibility() == View.INVISIBLE) {
                            switchArrowMode = true;
                            buttonArrowRotate.setImageResource(R.drawable.pd_button_rotate_on_041);
                            // buttonArrowRotate.setImageResource(R.drawable.pd_button_rotate_off_040);

//            				ProjectDocuDatabaseManager projectDocuDatabaseManager = new ProjectDocuDatabaseManager (getActivity());

                            if (planWebview.rotatingArrowLocation == false) {
                                planWebview.rotatingArrowLocation = true;

//            					projectDocuDatabaseManager.updatePreferences(ProjectDocuDatabaseManager.COLUMN_PREFERENCES_IS_ARROW_LOCATED, 1);
                                flag.is_arrow_located = 1;
                                planWebview.oldRotationAngle = 0.0f;
                                setArrowPosition();

                                rotationArrow.setVisibility(View.VISIBLE);
                                arrowTouchHelperView.setVisibility(View.VISIBLE);
                            }

                        } else {
                            switchArrowMode = false;
                            buttonArrowRotate.setImageResource(R.drawable.pd_button_rotate_on_041);
                            deactivateRotationLocator();
                        }

                        planSubmenuButtonsExpanded.setVisibility(View.INVISIBLE);
                    }
                }
        );

        buttonTrash = (ImageView) rootView.findViewById(R.id.button_delete_location);
        buttonTrash.setOnClickListener(
                new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Builder builder = new Builder(getActivity());
                        builder.setTitle(R.string.attention_dialog_title);
                        builder.setMessage(R.string.delete_flag_dialog);
                        builder.setCancelable(false);

                        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                            }
                        });

                        builder.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int option) {
//    							deleteLocation();
                            }
                        });

                        AlertDialog dialog = builder.create();
                        dialog.show();
                    }
                }
        );

        touchHelperView.setFocusableInTouchMode(false);
        touchHelperView.setFocusable(false);

        touchHelperView.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                planWebview.dispatchTouchEvent(event);
                if ((event.getAction() == MotionEvent.ACTION_MOVE || event.getAction() == MotionEvent.ACTION_SCROLL) && event.getPointerCount() < 2) {
//					if (blockAutoPosition == false) {
//						setBlockAutoPosition(true);
//					}

                }
                // check if screen is touched
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    oldCrossHairX = planWebview.crosshairPositionX;
                    oldCrossHairY = planWebview.crosshairPositionY;
                    oldScale = planWebview.scaleFactor;

                    currentTime = System.currentTimeMillis();
                    blockSingleTouch = false;

                    return true;
                }

                // check for two finger touch
                if (event.getPointerCount() >= 2) {
                    blockSingleTouch = true;
                    // moveCount = 0;

                    return false;

                } else if (planWebview.crosshairPositionX > (oldCrossHairX + 10)
                        || planWebview.crosshairPositionX < (oldCrossHairX - 10)
                        || planWebview.crosshairPositionY > (oldCrossHairY + 10)
                        || planWebview.crosshairPositionY < (oldCrossHairY - 10)
                ) {
                    blockSingleTouch = true;
                    return false;
                }
                /*
                 * Scroll map to long touched position
                 */
                if (event.getAction() == MotionEvent.ACTION_UP && blockSingleTouch == false) {
                    //System.out.println("#### ACTIONUP && blockSingleTouch == false");
                    if (System.currentTimeMillis() - currentTime > 500) {
                        if (blockSingleTouch == false) {
                            float touchX = event.getX() - planWebview.displayWidth / 2.0f;
                            float touchY = event.getY() - planWebview.displayHeight / 2.0f;

                            int x = Math.round((planWebview.resizedPlanWidth / 2 * planWebview.scaleFactor + (touchX + planWebview.scaledCrosshairPositionX)));
                            int y = Math.round((planWebview.resizedPlanHeight / 2 * planWebview.scaleFactor + (touchY + planWebview.scaledCrosshairPositionY)));

                            ObjectAnimator xTranslate = ObjectAnimator.ofInt(planWebview, "scrollX", (x));
                            ObjectAnimator yTranslate = ObjectAnimator.ofInt(planWebview, "scrollY", (y));
                            AnimatorSet animators = new AnimatorSet();
                            animators.setDuration(1000L);
                            animators.playTogether(xTranslate, yTranslate);
                            animators.start();

                            planWebview.scrollBy(Math.round(touchX), Math.round(touchY));
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
        });

//		ProjectDocuDatabaseManager projectDocuDatabaseManager = new ProjectDocuDatabaseManager (getActivity());
//
        if (flag.is_arrow_located == 1) {
            setLocatedPlanIcon();
        }

//        new OrientationEventListener((getActivity())) {
//            @Override
//            public void onOrientationChanged(int orientation) {
//                SpecificDefectAddDirectionFragment.this.onOrientationChanged(orientation);
//            }
//        }.enable();

//        if (localPhotoId > -1) {
//        	((ProjectDocuMainActivity) getActivity()).setMenuBarCameraButtonToPhotoButton();
//        }
//        else {
//        	((ProjectDocuMainActivity) getActivity()).setMenuBarCameraButtonToCameraButton();
//        }
//
//        ((ProjectDocuMainActivity) getActivity()).createActionBar();

        rootView.invalidate();


        planWebview.setOnTouchListener(new OnTouchListener() {
            private GestureDetector gestureDetector = new GestureDetector(getContext(),
                    new GestureDetector.SimpleOnGestureListener() {

                        @Override
                        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
                            //System.out.println("#### FLING FINISHED");
                            tempCrossHairX = planWebview.crosshairPositionX;
                            tempCrossHairY = planWebview.crosshairPositionY;
                            return false;
                        }
                    });

            private ScaleGestureDetector scaleGestureDetector = new ScaleGestureDetector(getContext(),
                    new ScaleGestureDetector.SimpleOnScaleGestureListener() {

                        @Override
                        public void onScaleEnd(ScaleGestureDetector detector) {

                            System.out.println("#### SCALE FINSIHED!");
							/*ProjectDocuDatabaseManager projectDocuDatabaseManager = new ProjectDocuDatabaseManager (getActivity());
							float currentScale = (Float) projectDocuDatabaseManager.selectDataFromPreferences(ProjectDocuDatabaseManager.COLUMN_PREFERENCES_CURRENT_SCALE);
							String debugText = "Debug Textview:\ncurrentScale:" + currentScale +"\nleftMargin:"+planWebview.leftMargin+"\ntopMargin:"+planWebview.topMargin+"\nrightMargin:"+planWebview.rightMargin+"\noldCrosshairX:"+planWebview.oldCrosshairPositionX+"\noldCrosshairY:"+planWebview.oldCrosshairPositionY+"\ncrossHairX:"+planWebview.crosshairPositionX+"\ncrossHairY:"+planWebview.crosshairPositionY+"";
							debugTextView.setText(debugText);
							*/
                        }
                    });

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                gestureDetector.onTouchEvent(event);
                scaleGestureDetector.onTouchEvent(event);
                return false;
            }
        });

        button_save_and_back = rootView.findViewById(R.id.button_save_and_back);
        ll_save.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                isLocationUpdate=true;
                cameraButton();
            }
        });

   /*     if (!pref.getString(PHOTO_SAVED_PATH_KEY, "").equals("")) {
            imagePath = pref.getString(PHOTO_SAVED_PATH_KEY, "");
            myBitmap = BitmapFactory.decodeFile(imagePath);
//                     Bitmap bitmap=myBitmap.createScaledBitmap(myBitmap,myBitmap.getWidth()*2,myBitmap.getHeight()*2,true);
            planWidth = myBitmap.getWidth();
            planHeight = myBitmap.getHeight();
//								new PlansPhotoRepository.UpdateAsyncTask(mDefectsPhotoDao).execute(projectModel);
//            final Handler handler = new Handler();
//            handler.postDelayed(new Runnable() {
//                @Override
//                public void run() {
//                    //Do something after 100ms
//
//                }
//            }, 100);
//            createWebview();
//            mergStatusOnPhoto();

        } else {
            callGetPlanImageAPI(getActivity(), "89");
        }*/

        //  arrowLocating();


        ib_crossHair = rootView.findViewById(R.id.crosshair);
        hideEditMode();
//        getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        if(isShowAutoEditMode) {
            showEditMode();
            ll_bottom_tabs.setVisibility(View.VISIBLE);
        }

        return rootView;
    }

    private void hideEditMode() {
        rotationArrow.setVisibility(View.GONE);
        arrowTouchHelperView.setVisibility(View.GONE);
        touchHelperView.setVisibility(View.GONE);
        ib_crossHair.setVisibility(View.GONE);
    }

    private void showEditMode() {
        rotationArrow.setVisibility(View.GONE);
        arrowTouchHelperView.setVisibility(View.VISIBLE);
        touchHelperView.setVisibility(View.VISIBLE);
        ib_crossHair.setVisibility(View.VISIBLE);

    }

    public void setShowGPSAccuracy(boolean show) {
        showGPSAccuracy = show;
    }

//	public void setBlockAutoPosition(boolean block) {
//		blockAutoPosition = block;
//
//		ProjectDocuDatabaseManager projectDocuDatabaseManager = new ProjectDocuDatabaseManager (getActivity());
//
//		if ((Integer) projectDocuDatabaseManager.selectDataFromPreferences(ProjectDocuDatabaseManager.COLUMN_PREFERENCES_GPS_ALWAYS_ON) == 1) {
//			int duration = Toast.LENGTH_SHORT;
//
//			Toast toast;
//
//			if (blockAutoPosition == true) {
//				toast = Toast.makeText(((ProjectDocuMainActivity) getActivity()), getResources().getString(R.string.toast_auto_position_off), duration);
//			}
//			else {
//				toast = Toast.makeText(((ProjectDocuMainActivity) getActivity()), getResources().getString(R.string.toast_auto_position_on), duration);
//			}
//
//			toast.show();
//		}
//	}

//	public void setBlockAutoDirection(boolean block) {
//		blockAutoDirection = block;
//
//		ProjectDocuDatabaseManager projectDocuDatabaseManager = new ProjectDocuDatabaseManager (getActivity());
//
//		if ((Integer) projectDocuDatabaseManager.selectDataFromPreferences(ProjectDocuDatabaseManager.COLUMN_PREFERENCES_COMPASS_ALWAYS_ON) == 1) {
//			int duration = Toast.LENGTH_SHORT;
//
//			Toast toast;
//
//			if (blockAutoDirection == true) {
//				toast = Toast.makeText(((ProjectDocuMainActivity) getActivity()), getResources().getString(R.string.toast_auto_direction_off), duration);
//			}
//			else {
//				toast = Toast.makeText(((ProjectDocuMainActivity) getActivity()), getResources().getString(R.string.toast_auto_direction_on), duration);
//			}
//
//			toast.show();
//		}
//	}

//	public ArrayList <Plan> getReferenceArray() {
//        ProjectDocuDatabaseManager projectDocuDatabaseManager = new ProjectDocuDatabaseManager (getActivity());
//
//		ArrayList <Plan> planObjectList = projectDocuDatabaseManager.selectDataFromPlan(((ProjectDocuMainActivity) getActivity()).currentPlanId);
//
//		if (planObjectList != null && planObjectList.size() > 0) {
//			if (planObjectList.get(0).refpointsjson != null && planObjectList.get(0).refpointsjson.length() > 0) {
//				try {
//					JSONArray dataForRefPointArray = (JSONArray)JSONValue.parse(planObjectList.get(0).refpointsjson);
//
//					if (dataForRefPointArray.size() >= 2) {
//						return dataForRefPointArray;
//					}
//					else {
//						return null;
//					}
//				}catch (Exception e) {
//					return null;
//				}
//			}
//			else {
//				return null;
//			}
//		}
//		else {
//			return null;
//		}
//	}

//	public void deleteLocation() {
//		//System.out.println("##### ProjectDocuShowPlanFragment:deleteLocation()");
//		ProjectDocuDatabaseManager projectDocuDatabaseManager = new ProjectDocuDatabaseManager (getActivity());
//
//		int currentPlanID = ((ProjectDocuMainActivity) getActivity()).currentPlanId;
//
//		projectDocuDatabaseManager.deleteFlagWithId(currentPlanID,-666);
//
//		projectDocuDatabaseManager.deleteFlagWithId(currentPlanID, -1);
//
//		if (localPhotoId > 0) {
//			projectDocuDatabaseManager.deleteFlagWithId(currentPlanID, localPhotoId);
//		}
//
//		clearLocatedPlanIcon();
//
//		planWebview.oldCrosshairPositionX = 0.0f;
//		planWebview.oldCrosshairPositionY = 0.0f;
//
//		planWebview.arrowRotationAngle = 0.0f;
//		planWebview.oldRotationAngle = 0.0f;
//		setArrowPosition();
//
//		projectDocuDatabaseManager.updatePreferences(ProjectDocuDatabaseManager.COLUMN_PREFERENCES_IS_LOCATED, 0);
//		projectDocuDatabaseManager.updatePreferences(ProjectDocuDatabaseManager.COLUMN_PREFERENCES_CURRENT_SCALE, 1.0f);
//
//	  	Toast.makeText(getActivity(), getResources().getString(R.string.toast_location_deleted), Toast.LENGTH_LONG).show();
//	  	deactivateRotationLocator();
//
//		if (((ProjectDocuPlanListFragment) fragment).getLocalPhotoId() == -1) {
//			((ProjectDocuMainActivity) getActivity()).setContentFragment(new ProjectDocuCameraFragment());
//		}
//		else {
//			((ProjectDocuMainActivity) getActivity()).setContentFragment(new ProjectDocuShowPhotoFragment(null, ((ProjectDocuPlanListFragment) fragment).getLocalPhotoId()));
//		}
//	}

    public void arrowLocating() {
//		ProjectDocuDatabaseManager projectDocuDatabaseManager = new ProjectDocuDatabaseManager (getActivity());

        if (planWebview.rotatingArrowLocation == false) {
            planWebview.rotatingArrowLocation = true;

//			projectDocuDatabaseManager.updatePreferences(ProjectDocuDatabaseManager.COLUMN_PREFERENCES_IS_ARROW_LOCATED, 1);
            flag.is_arrow_located = 1;
            planWebview.oldRotationAngle = 0.0f;
            setArrowPosition();

            rotationArrow.setVisibility(View.VISIBLE);
            arrowTouchHelperView.setVisibility(View.VISIBLE);
        } else if (planWebview.rotatingArrowLocation == true) {
            deactivateRotationLocator();
        }
    }

    public void showCustomDialog(final Activity act, String title, String msgToShow, Integer noOfButtons, Integer flag) {
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
        bt.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {

//                    Fragment fragment = defectDetailsPagerAdapter.getM1stFragment();
//                    if(fragment!=null){
//                        ((DefectDetailsDatesFragment) fragment).saveDefect();
//
//                    }
                new DeleteLocalFlawFlag().execute();
                customDialog.dismiss();


            }
        });
        if (noOfButtons == 2) {
            Button bt1 = (Button) customDialog.findViewById(R.id.customDialog_cancel);
            bt1.setVisibility(View.VISIBLE);
            bt1.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View v) {

                    customDialog.dismiss();
                }
            });

        }
        customDialog.show();
    }


    private long getColor(float x, float y) {
        if (x < 0 || y < 0 || x > (float) bitmap.getWidth() || y > (float) bitmap.getHeight()) {
            return 0;
        } else {
            int xBm = (int) (x * (bmWidth / (double) bitmap.getWidth()));
            int yBm = (int) (y * (bmHeight / (double) bitmap.getHeight()));

            return rotatedMask.getPixel(xBm, yBm);
        }
    }

    public void planSelection() {
        //System.out.println("##### ProjectDocuShowPlanFragment:planSelection()");

        planWebview.scrollingOn = true;
        arrowRotation = false;

        saveLocation();

//		((ProjectDocuMainActivity) getActivity()).setContentFragment(new ProjectDocuPlanListFragment(false, localPhotoId));
    }

    // save button !
    public void cameraButton() {
//		ProjectDocuDatabaseManager projectDocuDatabaseManager = new ProjectDocuDatabaseManager (getActivity());

        if (planWebview.crosshairIsOutsidePlan == true) {
            Toast.makeText(getActivity(), "Location Outside the Map", Toast.LENGTH_LONG).show();
            return;
        }

        saveLocation();

//		((ProjectDocuMainActivity) getActivity()).startSynchronizationService();
//
//		if (localPhotoId == -1 || (Integer) projectDocuDatabaseManager.selectDataFromPreferences(ProjectDocuDatabaseManager.COLUMN_PREFERENCES_LOCATION_ALWAYS_ON) == 1) {
//			if (((ProjectDocuMainActivity)getActivity()).menuBarSlidedOut == false) {
//				blockOrientationChange = true;
//				((ProjectDocuMainActivity)getActivity()).blockBlueMenuReset = true;
//			}
//
//			((ProjectDocuMainActivity) getActivity()).setContentFragment(new ProjectDocuCameraFragment());
//			((ProjectDocuMainActivity)getActivity()).blockBlueMenuReset = false;
//		}
//		else {
//			((ProjectDocuMainActivity)getActivity()).setContentFragment(new ProjectDocuShowPhotoFragment(null, localPhotoId));
//		}
    }


    @SuppressWarnings("deprecation")
    private void onOrientationChanged(int orientation) {
//        if (orientation == OrientationEventListener.ORIENTATION_UNKNOWN || blockOrientationChange == true) {
//            return;
//        }

        orientation = (orientation + 45) / 90 * 90;

        if (orientation >= 360) {
            orientation = 0;
        }

//        try{
//			WindowManager wm = (WindowManager) ((ProjectDocuMainActivity)getActivity()).getSystemService(Context.WINDOW_SERVICE);
//
//			Display display = wm.getDefaultDisplay();
//
//			if (display.getOrientation() == Surface.ROTATION_0) {
//		        if (orientation == 0  || orientation == 360) {
//		        	((ProjectDocuMainActivity)getActivity()).setMenuBarOrientation(LinearLayout.VERTICAL,0);
//		        	rotateIcons = 0;
//		        }
//		        else if (orientation == 90) {
//		        	((ProjectDocuMainActivity)getActivity()).setMenuBarOrientation(LinearLayout.VERTICAL,-90);
//		        	rotateIcons = -90;
//		        }
//		        else if (orientation == 180) {
//		        	((ProjectDocuMainActivity)getActivity()).setMenuBarOrientation(LinearLayout.VERTICAL,180);
//		        	rotateIcons = 180;
//		        }
//		        else if (orientation == 270) {
//		        	((ProjectDocuMainActivity)getActivity()).setMenuBarOrientation(LinearLayout.VERTICAL,90);
//		        	rotateIcons = 90;
//		        }
//		        else {
//		        	((ProjectDocuMainActivity)getActivity()).setMenuBarOrientation(LinearLayout.VERTICAL,0);
//		        	rotateIcons = 0;
//		        }
//			} else {
//		        if (orientation == 0  || orientation == 360) {
//		        	((ProjectDocuMainActivity)getActivity()).setMenuBarOrientation(LinearLayout.VERTICAL,-90);
//		        	rotateIcons = -90;
//		        }
//		        else if (orientation == 90) {
//		        	((ProjectDocuMainActivity)getActivity()).setMenuBarOrientation(LinearLayout.VERTICAL,180);
//		        	rotateIcons = 180;
//		        }
//		        else if (orientation == 180) {
//		        	((ProjectDocuMainActivity)getActivity()).setMenuBarOrientation(LinearLayout.VERTICAL,90);
//		        	rotateIcons = 90;
//		        }
//		        else if (orientation == 270) {
//		        	((ProjectDocuMainActivity)getActivity()).setMenuBarOrientation(LinearLayout.VERTICAL,0);
//		        	rotateIcons = 0;
//		        }
//		        else {
//		        	((ProjectDocuMainActivity)getActivity()).setMenuBarOrientation(LinearLayout.VERTICAL,-90);
//		        	rotateIcons = -90;
//		        }
//			}
//
//			buttonCancel.setRotation(rotateIcons);
//			buttonTrash.setRotation(rotateIcons);
//			buttonArrowRotate.setRotation(rotateIcons);
//			buttonConfig.setRotation(rotateIcons);
//			buttonPlan.setRotation(rotateIcons);
//
//			ImageView buttonSave = (ImageView) rootView.findViewById(R.id.button_save_and_back);
//			buttonSave.setRotation(rotateIcons);
//        } catch (Exception e) {}
    }

    private void createWebview() {
        //System.out.println("##### ProjectDocuShowPlanFragment:createWebview()");


        planWebview.noMoreScroll = false;
        planWebview = null;
        planWebview = (EnhancedWebView) rootView.findViewById(R.id.webview_plan);
//		ProjectDocuDatabaseManager projectDocuDatabaseManager = new ProjectDocuDatabaseManager (getActivity());


//        flag = projectDocuDatabaseManager.selectDataFromFlagsForCurrentPhoto(((ProjectDocuMainActivity) getActivity()).currentPlanId, localPhotoId);
//
//        if (flag.xcoord == null) {
//	        flag = projectDocuDatabaseManager.selectDataFromFlagsForCurrentPhoto(((ProjectDocuMainActivity) getActivity()).currentPlanId, -1);
//        }
//
//        if (flag.xcoord == null) {
//	        flag = projectDocuDatabaseManager.selectDataFromFlagsForCurrentPhoto(((ProjectDocuMainActivity) getActivity()).currentPlanId, -666);
//        }


        if (flag != null && flag.xcoord != null) {
            planWebview.oldCrosshairPositionX = flag.xcoord;
            planWebview.oldCrosshairPositionY = flag.ycoord;

            planWebview.oldViewX = flag.viewx;
            planWebview.oldViewY = flag.viewy;
            planWebview.oldRotationAngle = (float) flag.degree;

           /* if (planWebview.rotatingArrowLocation == true) {
                setArrowPosition();
                rotationArrow.setVisibility(View.VISIBLE);
                arrowTouchHelperView.setVisibility(View.VISIBLE);
                buttonArrowRotate.setImageResource(R.drawable.pd_button_rotate_on_041);
                // buttonArrowRotate.setImageResource(R.drawable.pd_button_rotate_off_040);
            }*/
        } else {
            planWebview.oldCrosshairPositionX = 0;
            planWebview.oldCrosshairPositionY = 0;

            planWebview.arrowRotationAngle = 0.0f;
            planWebview.oldRotationAngle = 0.0f;

//			projectDocuDatabaseManager.updatePreferences(ProjectDocuDatabaseManager.COLUMN_PREFERENCES_IS_LOCATED, 0);
            flag.is_arrow_located = 0;
//            rotationArrow.setVisibility(View.INVISIBLE);
//            arrowTouchHelperView.setVisibility(View.INVISIBLE);
        }

        planWebview.planWidth = this.planWidth;
        planWebview.planHeight = this.planHeight;
        planWebview.resizedPlanWidth = this.planWidth;
        planWebview.resizedPlanHeight = this.planHeight;

        planWebview.getSettings().setRenderPriority(RenderPriority.HIGH);

        planWebview.clearCache(true);
        planWebview.getSettings().setCacheMode(WebSettings.LOAD_NO_CACHE);
     //   planWebview.getSettings().setAppCacheEnabled(false);

        planWebview.getSettings().setLayoutAlgorithm(WebSettings.LayoutAlgorithm.NORMAL);
        planWebview.calculateSizes(0, 0, true);
//        if(flag!=null&&flag.scale_factor!=null){
//            planWebview.setInitialScale(Math.round(10 * 100.0f));

//        }
//        webviewHtml = planWebview.setRecreateHTMLPlan(ProjectDocuUtilities.getPlanPath((ProjectDocuMainActivity) getActivity()));


        webviewHtml = planWebview.createHTMLPlan(imagePath);

        //System.out.println("getPlanPath:"+ProjectDocuUtilities.getPlanPath((ProjectDocuMainActivity) getActivity()));

        /*BitmapFactory.Options options = new BitmapFactory.Options();
        options.inPreferredConfig = Bitmap.Config.ARGB_8888;
        try{
        Bitmap bitmap = BitmapFactory.decodeFile(ProjectDocuUtilities.getPlanPath((ProjectDocuMainActivity) getActivity()), options);
        	System.out.println("Bitmap:"+bitmap);
        }catch(Exception e){
        	System.out.println("Exception:"+e);
        }*/
        
        
        /*
        if (android.os.Build.VERSION.SDK_INT < 19) {
        */
        planWebview.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        /*
        }
        */

/////////////////////////////////////////////////////////////////////////////////
/////////////////////////////////////////////////////////////////////////////////
/////////////////////////////////////////////////////////////////////////////////
/////////////////////////////////////////////////////////////////////////////////
/////////////////////////////////////////////////////////////////////////////////

        if (flag.scale_factor != 1.0f) {
            planWebview.setInitialScale(Math.round(flag.scale_factor * 100.0f));
        } else {
            planWebview.setInitialScale(100);
        }

/////////////////////////////////////////////////////////////////////////////////
/////////////////////////////////////////////////////////////////////////////////
/////////////////////////////////////////////////////////////////////////////////
/////////////////////////////////////////////////////////////////////////////////
/////////////////////////////////////////////////////////////////////////////////

       // planWebview.getSettings().setAppCacheEnabled(false);
        planWebview.setScaleX(1.0f);
        planWebview.setScaleY(1.0f);
        planWebview.getSettings().setJavaScriptEnabled(true);

        planWebview.getSettings().setSupportZoom(true);
        planWebview.getSettings().setBuiltInZoomControls(true);
        planWebview.getSettings().setDisplayZoomControls(false);
        planWebview.getSettings().setUseWideViewPort(true);
        //planWebview.getSettings().setDefaultZoom(ZoomDensity.FAR);
        planWebview.getSettings().setLoadWithOverviewMode(true);

        planWebview.clearCache(true);
        planWebview.clearView();


        planWebview.loadDataWithBaseURL("file:///android_asset/", webviewHtml, "text/html", "utf-8", "");

        planWebview.getSettings().setLayoutAlgorithm(WebSettings.LayoutAlgorithm.NORMAL);


        planWebview.setPictureListener(new PictureListener() {
            @Override
            public void onNewPicture(WebView view, Picture picture) {
                if (planWebview.noMoreScroll == false) {
                    //System.out.println("#### positionText Init after view loaded old Crosshairposition: X Y:"+planWebview.oldCrosshairPositionX + "--- "+planWebview.oldCrosshairPositionY);
                    setPlanPosition(planWebview.oldCrosshairPositionX, planWebview.oldCrosshairPositionY);
                    planWebview.noMoreScroll = true;
                }
            }
        });

        WebViewClient myWebViewClient = new WebViewClient() {
            @Override
            public void onScaleChanged(WebView view, float oldScale, float newScale) {
                super.onScaleChanged(view, oldScale, newScale);
                //System.out.println("Scale Changed:"+oldScale + " --- " + newScale + " --- " +view.getScaleX() + " --- " +view.getScaleY());
                //planWebview.scaleFactor = newScale;
                //planWebview.oldScaleFactor = oldScale;
                //planWebview.scaleFactor = newScale / oldScale;
                view.getScaleX();
                view.getScaleY();

            }
        };

        planWebview.setWebViewClient(myWebViewClient);


    }

    public static Point getLocationOnScreen(View view) {
        int[] location = new int[2];
        view.getLocationOnScreen(location);
        return new Point(location[0], location[1]);
    }

    public class JavaScriptHandler {
        public float getScaleFactor() {
            //System.out.println("!!! ScaleFactorWebview:"+planWebview.scaleFactor);
            return planWebview.scaleFactor;
        }

        public JavaScriptHandler(SpecificDefectAddDirectionFragmentForTesting activity) {
        }
    }

    @Override
    public void onResume() {
        super.onResume();
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

        new RetrievePlansUsingPLanIDTask(getActivity(), projectID, planID).execute();


     /*   createWebview();

        //System.out.println("##### scaling: "+planWebview.getScaleX() + " - "+planWebview.getScaleY()+ " - " +planWebview.scaledCrosshairPositionX + " - " +planWebview.scaledCrosshairPositionY + " - " +planWebview.scaleFactor  + " - " +planWebview.getScale() + " - " + getResources().getDisplayMetrics().density);


        if (flag.is_arrow_located == 1) {
            planWebview.arrowRotationAngle = planWebview.oldRotationAngle;

            setArrowPosition();

            rotationArrow.setVisibility(View.VISIBLE);
            arrowTouchHelperView.setVisibility(View.VISIBLE);
            buttonArrowRotate.setImageResource(R.drawable.pd_button_rotate_on_041);
        }*/

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
        flag.is_arrow_located = 0;
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
    private void saveLocation() {
        //System.out.println("##### ProjectDocuShowPlanFragment:saveLocation()");

//		ProjectDocuDatabaseManager projectDocuDatabaseManager = new ProjectDocuDatabaseManager (getActivity());
//
        int viewX = (int) planWebview.viewX;
        int viewY = (int) planWebview.viewY;

        int[] location = new int[2];
//        rotationArrow.getLocationOnScreen(location);
//        Toast.makeText(getActivity(), "X axis is " + location[0] + "and Y axis is " + location[1], Toast.LENGTH_LONG).show();

        // if (flag.is_arrow_located == 1) {
        int rotationAngleForBackend = 180;

        if (flag.is_arrow_located == 1) {
            rotationAngleForBackend = (int) Math.round(planWebview.arrowRotationAngle);
        }


        if (flag.is_arrow_located == 1) {
            viewY = viewY * -1;
        }
        HomeActivity.flags.degree = (int) Math.round(planWebview.arrowRotationAngle);
        HomeActivity.flags.viewx = viewX;
        HomeActivity.flags.viewy = viewY;
        HomeActivity.flags.xcoord = (int) planWebview.crosshairPositionX;
        HomeActivity.flags.ycoord = (int) planWebview.crosshairPositionY;
        HomeActivity.flags.scale_factor = planWebview.scaleFactor;
        HomeActivity.flags.tiltangle = 0;


        pdflawflagOBJ.setPdProjectid(projectID);
        pdflawflagOBJ.setPdplanid(planID);
        pdflawflagOBJ.setLocal_flaw_Id(flawId);
//        pdflawflagOBJ.setXcoord((int) planWebview.crosshairPositionX - 12 + "");
//        pdflawflagOBJ.setYcoord((int) planWebview.crosshairPositionY - 90 + "");

        pdflawflagOBJ.setXcoord((int) planWebview.crosshairPositionX + "");
        pdflawflagOBJ.setYcoord((int) planWebview.crosshairPositionY + "");
        pdflawflagOBJ.setViewx(viewX + "");
        pdflawflagOBJ.setViewy(viewY + "");
        pdflawflagOBJ.setScale_factor(planWebview.scaleFactor);
        pdflawflagOBJ.setDegree((int) Math.round(planWebview.arrowRotationAngle));
        pdflawflagOBJ.setIs_arrow_located(flag.is_arrow_located);
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.GERMANY);
        String photoDate = simpleDateFormat.format(new Date());
        pdflawflagOBJ.setCreated(photoDate);
        pdflawflagOBJ.setLastupdated(photoDate);

        new CreateOrUpdateLocalFlawFlag().execute(pdflawflagOBJ);




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
//        Toast.makeText(getActivity(), getResources().getString(R.string.toast_location_saved), Toast.LENGTH_LONG).show();
        //   }
    }


    private class DeleteLocalFlawFlag extends AsyncTask<Void, Void, Void> {
        private DefectsDao mAsyncTaskDao;

        DeleteLocalFlawFlag() {
        }

        @Override
        protected Void doInBackground(final Void... params) {

            PdFlawFlagRepository pdFlawFlagRepository = new PdFlawFlagRepository(getContext(), projectID);
            pdFlawFlagRepository.getmDefectsPhotoDao().deleteUsingLocalFlaw_id(flawId + "");
            for (Fragment fragment : getActivity().getSupportFragmentManager().getFragments()) {
                if (fragment instanceof SpecificDefectAddDirectionFragmentForTesting) {
                    getActivity().getSupportFragmentManager().beginTransaction().remove(fragment).commit();
                    break;
                }
            }
            Intent intent = new Intent("updateFlawFlag");
            intent.putExtra(SavePictureActivity.PLAN_ATTACH_TO_PHOTO_KEY, false);
            intent.putExtra(SavePictureActivity.PLAN_ID_KEY, planID);
            getActivity().sendBroadcast(intent);
//            getActivity().finish();
            return null;
        }
    }

    PdFlawFlagRepository pdFlawFlagRepository = new PdFlawFlagRepository(getContext(), projectID);

    private class CreateOrUpdateLocalFlawFlag extends AsyncTask<Pdflawflag, Void, Pdflawflag> {
        private DefectsDao mAsyncTaskDao;

        CreateOrUpdateLocalFlawFlag() {
        }

        @Override
        protected Pdflawflag doInBackground(final Pdflawflag... params) {
            Pdflawflag pdflawflag = params[0];


            int viewY=0;
            Pdflawflag pdflawflagg = pdFlawFlagRepository.getmDefectsPhotoDao().getFlawFlagOBJExist(params[0].getPdProjectid(), params[0].getLocal_flaw_Id());
            if (pdflawflagg == null) {
                pdFlawFlagRepository.getmDefectsPhotoDao().insert(params[0]);
            } else {
                viewY=(int) planWebview.viewY;
                if (flag.is_arrow_located == 1) {
                    viewY = viewY * -1;
                }
                pdflawflag = params[0];
                pdflawflag.setFlaw_Id(pdflawflagg.getFlaw_Id());
                pdflawflag.setFlaw_status(pdflawflagg.getFlaw_status());
                pdflawflag.setPdflawflagid(pdflawflagg.getPdflawflagid());

                pdflawflag.setPdProjectid(projectID);
                pdflawflag.setPdplanid(planID);
                pdflawflag.setLocal_flaw_Id(flawId);
                pdflawflag.setXcoord((int) planWebview.crosshairPositionX  + "");
                pdflawflag.setYcoord((int) planWebview.crosshairPositionY  + "");
                pdflawflag.setViewx((int) planWebview.viewX + "");
                pdflawflag.setViewy( viewY+ "");
                pdflawflag.setScale_factor(planWebview.scaleFactor);
                pdflawflag.setDegree((int) Math.round(planWebview.arrowRotationAngle));
                pdflawflag.setIs_arrow_located(flag.is_arrow_located);
                pdFlawFlagRepository.getmDefectsPhotoDao().deleteUsingLocalFlaw_id(flawId);
                pdFlawFlagRepository.getmDefectsPhotoDao().insert(pdflawflag);

            }
            defectStatusList = new ArrayList<>();
            DefectsModel defectsModel = ProjectsDatabase.getDatabase(getActivity()).defectsDao().getDefectsOBJ(projectID, pdflawflagOBJ.getLocal_flaw_Id());
            defectStatusList.add(defectsModel.getStatus());
            if (getActivity() != null) {
//                Intent intent = new Intent("updateDefectdetailFlawFlag");
//                intent.putExtra("planId", params[0].getPdplanid());
//                intent.putExtra("flawId", params[0].getLocal_flaw_Id());
//                getActivity().sendBroadcast(intent);

                Intent intentt = new Intent("updateFlawStatus");
                intentt.putExtra("local_flaw_id", params[0].getLocal_flaw_Id());
                getActivity().sendBroadcast(intentt);
            }


//            getActivity().finish();
            return pdflawflag;
        }

        @Override
        protected void onPostExecute(Pdflawflag pdflawflag) {
            super.onPostExecute(pdflawflag);
            if(pdflawflag!=null&&pdflawflag.getXcoord()!=null) {
//                updateDefectLocation(pdflawflag);
                Intent intent = new Intent(DefectDetailAllPlansFragment.BR_UPDATE_PLAN_LOCATION);
                getActivity().sendBroadcast(intent);
            }

        }
    }

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

    public void setPlanPositionByGps(Location planLocation) {
        //System.out.println("##### ProjectDocuShowPlanFragment:setPlanPositionByGps()");
        if (blockAutoPosition == true) {
            return;
        }

        if (planLocation == null) {
//			((ProjectDocuMainActivity) getActivity()).deactivateGPSButton();
            ((PhotoAddDirectionMainActivity) getActivity()).deactivateGPSNonStop();

            int duration = Toast.LENGTH_LONG;

            Toast toast = Toast.makeText(getActivity(), getResources().getString(R.string.toast_gps_no_signal), duration);
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

    // sets the plan position to a specific x/y koordinate
    // ATTENTION: the x/y position in the plan is not the crosshair or oldCrosshair position.
    public void setPlanPosition(float x, float y) {
        //System.out.println("##### ProjectDocuShowPlanFragment:setPlanPosition()");

//		ProjectDocuDatabaseManager projectDocuDatabaseManager = new ProjectDocuDatabaseManager (getActivity());

        planWebview.oldCrosshairPositionX = x;
        planWebview.oldCrosshairPositionY = y;

        float currentScale = flag.scale_factor;

        //currentScale = planWebview.oldScaleFactor;
        //System.out.println("###### positionText SetPlanPos !!! currentScale:"+currentScale + " x:"+x+" y:"+y + " oldCrossHairPositionX:"+planWebview.oldCrosshairPositionX+ " oldCrossHairPositionY:"+planWebview.oldCrosshairPositionY+ "planWebview.leftMagrin:"+planWebview.leftMargin + " planWebview.topMargin:"+planWebview.topMargin);
        //System.out.println("###### positionText SetPlanPos !!! initscale: "+Math.round(currentScale*100)+ "--- "+(int)(Math.round(planWebview.oldScaleFactor*100)) + " planWebview.resizedPlanWidth:"+planWebview.resizedPlanWidth+ " planWebview.resizedPlanHeight:"+planWebview.resizedPlanHeight + "planWebview.planWidth:"+planWebview.planWidth+" planWebview.planHeight:"+planWebview.planHeight);
        //planWebview.setInitialScale((int)(Math.round(150.0f)));
        planWebview.setInitialScale(Math.round(currentScale * 100.0f));

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

			/*if (scrollX < 0)
			{
					System.out.println("SAVING OUTSIDE MAP!");
			}*/


            //orig
            //x = Math.round((planWebview.leftMargin*currentScale + planWebview.resizedPlanWidth/2*currentScale - planWebview.displayWidth/2*currentScale + planWebview.oldCrosshairPositionX*currentScale));
            //y = Math.round((planWebview.topMargin*currentScale + planWebview.resizedPlanHeight/2*currentScale - planWebview.displayHeight/2*currentScale + planWebview.oldCrosshairPositionY*currentScale));

			/*if (android.os.Build.VERSION.SDK_INT < 19) {
				//this.loadUrl("javascript:setTableMargin(" + Math.round(defaultMarginWidth / scaleFactor) + "," + Math.round(defaultMarginHeight / scaleFactor) + ");");
			} else {

				planWebview.evaluateJavascript("setTableMargin(" + Math.round(planWebview.defaultMarginWidth / planWebview.scaleFactor) + "," + Math.round(planWebview.defaultMarginHeight / planWebview.scaleFactor) + ");", null);
			}*/
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


/*		planWebview.scrollTo(
				Math.round((planWebview.leftMargin*currentScale + planWebview.resizedPlanWidth/2*currentScale - planWebview.displayWidth/2*currentScale + planWebview.oldCrosshairPositionX*currentScale)),
				Math.round((planWebview.topMargin*currentScale + planWebview.resizedPlanHeight/2*currentScale - planWebview.displayHeight/2*currentScale + planWebview.oldCrosshairPositionY*currentScale)));

				Math.round((planWebview.leftMargin*currentScale + planWebview.resizedPlanWidth/2*currentScale - planWebview.displayWidth/2*currentScale + planWebview.oldCrosshairPositionX*currentScale)),
				Math.round((planWebview.topMargin*currentScale + planWebview.resizedPlanHeight/2*currentScale - planWebview.displayHeight/2*currentScale + planWebview.oldCrosshairPositionY*currentScale)));
*/
		/*
		System.out.println("!!! planWebview.leftMargin:"+planWebview.leftMargin);
		System.out.println("!!! planWebview.resizedPlanWidth:"+planWebview.resizedPlanWidth);
		System.out.println("!!! planWebview.planWebview.displayWidth:"+planWebview.displayWidth);

		System.out.println("!!! planWebview.topMargin:"+planWebview.topMargin);
		System.out.println("!!! planWebview.resizedPlanHeight:"+planWebview.resizedPlanHeight);
	    System.out.println("!!! planWebview.planWebview.displayHeight:"+planWebview.displayHeight);
		*/

        //System.out.println("!!! x y:"+x + "---" +y);
        //System.out.println("!!! planWebview.oldCrosshairPositionY*currentScale):"+planWebview.oldCrosshairPositionX*currentScale+" --- "+planWebview.oldCrosshairPositionY*currentScale);


    }

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
                if (pbDialog != null)
                    pbDialog.dismiss();
                if (response.isSuccessful()) {
                    if (response.body() != null) {
                        Log.d("List", "Success : " + response.body());
                        if (writeResponseBodyToDisk(response.body(), "44")) {
                            if (imagePath != null && !imagePath.equals("")) {

//                                myBitmap = BitmapFactory.decodeFile(imagePath);
                                myBitmap = BitmapFactory.decodeFile(imagePath);
//                     Bitmap bitmap=myBitmap.createScaledBitmap(myBitmap,myBitmap.getWidth()*2,myBitmap.getHeight()*2,true);
                                planWidth = myBitmap.getWidth();
                                planHeight = myBitmap.getHeight();
//								new PlansPhotoRepository.UpdateAsyncTask(mDefectsPhotoDao).execute(projectModel);

                                List<Pdflawflag> flawFlagList = new ArrayList<>();
                                if (pdflawflagOBJ != null && pdflawflagOBJ.getXcoord() != null) {
                                    flawFlagList.add(pdflawflagOBJ);
                                    mergStatusOnPhoto(flawFlagList);
                                }
                                createWebview();
                                planWebview.scaleFactor = 1;
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
                if (pbDialog != null)
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
            if (pdflawflagOBJ != null && pdflawflagOBJ.getLocal_flaw_Id() != null) {
                defectStatusList = new ArrayList<>();
                DefectsModel defectsModel = ProjectsDatabase.getDatabase(getActivity()).defectsDao().getDefectsOBJ(projectID, pdflawflagOBJ.getLocal_flaw_Id());
                defectStatusList.add(defectsModel.getStatus());
            }
            defectStatusList = new ArrayList<>();
            defectStatusList.add("0");

            return plansModel;
        }

        @Override
        protected void onPostExecute(PlansModel plansModelList) {
            super.onPostExecute(plansModelList);
            plansModelOBJ = plansModelList;

            if (plansModelOBJ != null && plansModelOBJ.getPlanPhotoPathLargeSize() != null && !plansModelOBJ.getPlanPhotoPathLargeSize().equals("")) {
                myBitmap = BitmapFactory.decodeFile(plansModelOBJ.getPlanPhotoPathLargeSize());
                planWidth = myBitmap.getWidth();
                planHeight = myBitmap.getHeight();
                imagePath = plansModelOBJ.getPlanPhotoPathLargeSize();
                if (flag.xcoord == null || flag.xcoord == 0) {
                    flag.xcoord = planWebview.getWidth() / 2;
                    flag.ycoord = planWebview.getHeight() / 2;
                    planWebview.scaleFactor = 1;
                }
                List<Pdflawflag> flawFlagList = new ArrayList<>();
                if (pdflawflagOBJ != null && pdflawflagOBJ.getXcoord() != null) {
                    flawFlagList.add(pdflawflagOBJ);

                    mergStatusOnPhoto(flawFlagList);
                }
                createWebview();
                planWebview.scaleFactor = 1;
//                if (flag.is_arrow_located == 1) {
//                    planWebview.arrowRotationAngle = planWebview.oldRotationAngle;
//
//                    setArrowPosition();
//
//                    rotationArrow.setVisibility(View.VISIBLE);
//                    arrowTouchHelperView.setVisibility(View.VISIBLE);
//                    buttonArrowRotate.setImageResource(R.drawable.pd_button_rotate_on_041);
//                }
            } else {
                callGetPlanImageAPI(getActivity(), planID);
            }
        }
    }

    private void updateDefectLocation(Pdflawflag pdflawflagOBJ) {
        if (plansModelOBJ != null && plansModelOBJ.getPlanPhotoPathLargeSize() != null && !plansModelOBJ.getPlanPhotoPathLargeSize().equals("")) {
            myBitmap = BitmapFactory.decodeFile(plansModelOBJ.getPlanPhotoPathLargeSize());
            planWidth = myBitmap.getWidth();
            planHeight = myBitmap.getHeight();
            imagePath = plansModelOBJ.getPlanPhotoPathLargeSize();
            if (flag.xcoord == null || flag.xcoord == 0) {
                flag.xcoord = planWebview.getWidth() / 2;
                flag.ycoord = planWebview.getHeight() / 2;
                planWebview.scaleFactor = 1;
            }
            List<Pdflawflag> flawFlagList = new ArrayList<>();
            if (pdflawflagOBJ != null && pdflawflagOBJ.getXcoord() != null) {
                flawFlagList.add(pdflawflagOBJ);

                mergStatusOnPhoto(flawFlagList);

                if (pdflawflagOBJ != null) {

                    if (pdflawflagOBJ.degree != null)
                        flag.degree = pdflawflagOBJ.degree;
                    else
                        flag.degree = 0;

                    if (pdflawflagOBJ.getXcoord() != null)
                        flag.xcoord = Integer.valueOf(pdflawflagOBJ.getXcoord());
                    else
                        flag.xcoord = 0;
                    if (pdflawflagOBJ.getYcoord() != null)
                        flag.ycoord = Integer.valueOf(pdflawflagOBJ.getYcoord());
                    else
                        flag.ycoord = 0;
//            flag.ycoord = Integer.valueOf(pdflawflagOBJ.getYcoord());
                    if (pdflawflagOBJ.getViewx() != null)
                        flag.viewx = Integer.valueOf(pdflawflagOBJ.getViewx());
                    else
                        flag.viewx = 0;

                    if (pdflawflagOBJ.getViewy() != null)
                        flag.viewy = Integer.valueOf(pdflawflagOBJ.getViewy());
                    else
                        flag.viewy = 0;
                    flag.scale_factor=0.999f;
//                    if (pdflawflagOBJ.scale_factor > 0)
//                        flag.scale_factor = pdflawflagOBJ.scale_factor;
//                    else
//                        flag.scale_factor = 1;
                    if (pdflawflagOBJ.is_arrow_located > 0) {
                        flag.is_arrow_located = 1;
                    } else {
                        flag.is_arrow_located = 0;
                    }
                    flag.tiltangle = 0;

                } else {
                    pdflawflagOBJ = new Pdflawflag();
                }

//        flag = HomeActivity.flags;


                if (flag.xcoord == null) {
                    flag.degree = 0;
                    flag.xcoord = -365;
                    flag.ycoord = 103;
                    flag.viewy = 100;
                    flag.viewx = 0;
                    flag.scale_factor = 0.9999f;
                    flag.is_arrow_located = 0;
                    flag.tiltangle = 0;
                }
            }
            hideEditMode();
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    createWebview();
                    ll_bottom_tabs.setVisibility(View.GONE);
                }
            });

        }

    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        myBitmap = null;
        bitmap = null;
    }

  /*  public String compressImage(String imageUri) {

        String filePath = getRealPathFromURI(imageUri);
        Bitmap scaledBitmap = null;

        BitmapFactory.Options options = new BitmapFactory.Options();

//      by setting this field as true, the actual bitmap pixels are not loaded in the memory. Just the bounds are loaded. If
//      you try the use the bitmap here, you will get null.
        options.inJustDecodeBounds = true;
        Bitmap bmp = BitmapFactory.decodeFile(filePath, options);

        if(bmp==null)
            return imageUri;

        int actualHeight = options.outHeight;
        int actualWidth = options.outWidth;

//      max Height and width values of the compressed image is taken as 816x612

        float maxHeight = actualWidth;
        float maxWidth = actualHeight;
        float imgRatio = actualWidth / actualHeight;
        float maxRatio = maxWidth / maxHeight;

//      width and height values are set maintaining the aspect ratio of the image

        if (actualHeight > maxHeight || actualWidth > maxWidth) {
            if (imgRatio < maxRatio) {
                imgRatio = maxHeight / actualHeight;
                actualWidth = (int) (imgRatio * actualWidth);
                actualHeight = (int) maxHeight;
            } else if (imgRatio > maxRatio) {
                imgRatio = maxWidth / actualWidth;
                actualHeight = (int) (imgRatio * actualHeight);
                actualWidth = (int) maxWidth;
            } else {
                actualHeight = (int) maxHeight;
                actualWidth = (int) maxWidth;

            }
        }

//      setting inSampleSize value allows to load a scaled down version of the original image

        options.inSampleSize = calculateInSampleSize(options, actualWidth, actualHeight);

//      inJustDecodeBounds set to false to load the actual bitmap
        options.inJustDecodeBounds = false;

//      this options allow android to claim the bitmap memory if it runs low on memory
        options.inPurgeable = true;
        options.inInputShareable = true;
        options.inTempStorage = new byte[16 * 1024];

        try {
//          load the bitmap from its path
            bmp = BitmapFactory.decodeFile(filePath, options);
        } catch (OutOfMemoryError exception) {
            exception.printStackTrace();

        }
        try {
            scaledBitmap = Bitmap.createBitmap(actualWidth, actualHeight, Bitmap.Config.ARGB_8888);
        } catch (OutOfMemoryError exception) {
            exception.printStackTrace();
        }

        float ratioX = actualWidth / (float) options.outWidth;
        float ratioY = actualHeight / (float) options.outHeight;
        float middleX = actualWidth / 2.0f;
        float middleY = actualHeight / 2.0f;

        Matrix scaleMatrix = new Matrix();
        scaleMatrix.setScale(ratioX, ratioY, middleX, middleY);

        Canvas canvas = new Canvas(scaledBitmap);
        canvas.setMatrix(scaleMatrix);
        canvas.drawBitmap(bmp, middleX - bmp.getWidth() / 2, middleY - bmp.getHeight() / 2, new Paint(Paint.FILTER_BITMAP_FLAG));

//      check the rotation of the image and display it properly
        ExifInterface exif;
        try {
            exif = new ExifInterface(filePath);

            int orientation = exif.getAttributeInt(
                    ExifInterface.TAG_ORIENTATION, 0);
            Log.d("EXIF", "Exif: " + orientation);
            Matrix matrix = new Matrix();
            if (orientation == 6) {
                matrix.postRotate(90);
                Log.d("EXIF", "Exif: " + orientation);
            } else if (orientation == 3) {
                matrix.postRotate(180);
                Log.d("EXIF", "Exif: " + orientation);
            } else if (orientation == 8) {
                matrix.postRotate(270);
                Log.d("EXIF", "Exif: " + orientation);
            }
            scaledBitmap = Bitmap.createBitmap(scaledBitmap, 0, 0,
                    scaledBitmap.getWidth(), scaledBitmap.getHeight(), matrix,
                    true);
        } catch (IOException e) {
            e.printStackTrace();
        }

        FileOutputStream out = null;
        String filename = getFilename();
        try {
            out = new FileOutputStream(filename);

//          write the compressed bitmap at the destination specified by filename.
            scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 60, out);

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        return filename;

    }*/

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

}
