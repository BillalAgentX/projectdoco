package com.projectdocupro.mobile.fragments.add_direction;

import static android.content.Context.LOCATION_SERVICE;

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
import android.location.LocationManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.provider.Settings;
import android.util.Log;
import android.view.Display;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.OrientationEventListener;
import android.view.ScaleGestureDetector;
import android.view.Surface;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.WebSettings;
import android.webkit.WebSettings.RenderPriority;
import android.webkit.WebView;
import android.webkit.WebView.PictureListener;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;

import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.projectdocupro.mobile.ProjectNavigator;
import com.projectdocupro.mobile.ProjectsDatabase;
import com.projectdocupro.mobile.R;
import com.projectdocupro.mobile.activities.HomeActivity;
import com.projectdocupro.mobile.activities.PlansActivity;
import com.projectdocupro.mobile.activities.SavePictureActivity;
import com.projectdocupro.mobile.dao.DefectsDao;
import com.projectdocupro.mobile.dao.PlansDao;
import com.projectdocupro.mobile.interfaces.RetroApiInterface;
import com.projectdocupro.mobile.managers.AppConstantsManager;
import com.projectdocupro.mobile.managers.RetrofitManager;
import com.projectdocupro.mobile.managers.SharedPrefsManager;
import com.projectdocupro.mobile.models.Pdflawflag;
import com.projectdocupro.mobile.models.PhotoModel;
import com.projectdocupro.mobile.models.PlansModel;
import com.projectdocupro.mobile.models.ReferPointJSONPlanModel;
import com.projectdocupro.mobile.repos.LocalPhotosRepository;
import com.projectdocupro.mobile.repos.PdFlawFlagRepository;
import com.projectdocupro.mobile.utility.Utils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.CountDownLatch;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

@SuppressLint("ValidFragment")
public class PhotoAddDirectionFragment extends Fragment {
    private static final String ARROW_KEY = "arrow_keys";
    private Bitmap myBitmap = null;
    private GestureDetector gd;
    private View rootView = null;
    public EnhancedWebView planWebview = null;

    private Fragment fragment = null;
    private int localPhotoId = -1;
    private int rotateIcons = 0;
    private boolean arrowRotation = false;

    Pdflawflag pdflawflagOBJ;

    private PhotoAddDirectionMainActivity projectDocuMainActivity = null;


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

    private TextView tvAccuracy = null;

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

    public static PhotoAddDirectionFragment projectDocuShowPlanFragment;
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

    ImageView iv_back_arrow;

    LinearLayout ll_cancel, ll_save;
    private String projectID = "";
    private String planID = "";
    private String photoId = "";
    private boolean isFromPhoto;
    private PlansModel plansModelOBJ = null;
    private List<ReferPointJSONPlanModel> referPointList;
    private SharedPrefsManager sharedPrefsManager;
    public boolean isViewCreated;
    private CountDownLatch signal;
    private int widthHelper;
    private int heightHelper;
    private boolean isFromPlanScreen;
    private boolean isLocationAlreadySet = false;
    private TextView compassAngleView;
    public int lastFragmentArrowAngle = 0;
    private boolean keepOnRunning = true;

    public PhotoAddDirectionFragment(Pdflawflag pdflawflag, String projectId, String planId, String photoID, boolean isFromPhotos) {
        pdflawflagOBJ = pdflawflag;
        this.localPhotoId = localPhotoId;
        planID = planId;
        projectID = projectId;
        photoId = photoID;
        isFromPhoto = isFromPhotos;

//        myBitmap = BitmapFactory.decodeFile(photoURL);

    }

    public PhotoAddDirectionFragment() {

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
//            activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
////            activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
//        }
    }

    @Override
    public void onDetach() {
        super.onDetach();

        if (activity != null) {
            activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
//    	((ProjectDocuMainActivity) getActivity()).showMainLayout();
    }


    private void mergStatusOnPhoto() {

//        Drawable myIcon1 = getResources().getDrawable( R.drawable.green_circle_selected );
//        Drawable myIcon2 = getResources().getDrawable( R.drawable.green_circle_selected );
//        Drawable myIcon3 = getResources().getDrawable( R.drawable.green_circle_selected );

        Bitmap bigImage = myBitmap;
        ;
        Bitmap smallImage = BitmapFactory.decodeResource(getResources(), R.drawable.green_circle_selected);
        Bitmap mergedImages = createSingleImageFromMultipleImages(bigImage, smallImage);
//        iv_temp.setVisibility(View.VISIBLE);
//        iv_temp.setImageBitmap(mergedImages);


        writeBitMapToDisk(mergedImages, projectID);


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
        Utils.showLogger("PhotoAddDirectionFragment onCreateView");

        try {
            PhotoAddDirectionMainActivity photoAddDirectionMainActivity = (PhotoAddDirectionMainActivity) getActivity();
            photoAddDirectionMainActivity.liveLocation.observe(getViewLifecycleOwner(), new Observer<Location>() {
                @Override
                public void onChanged(Location location) {
                    setPlanPositionByGps(location);//when observing location change
                }
            });
        } catch (Exception e) {

        }

        rootView = layoutInflater.inflate(R.layout.photo_add_direction_fragment_layout, viewGroup, false);
        pref = getActivity().getApplicationContext().getSharedPreferences("MyPref", 0); // 0 - for private mode
        editor = pref.edit();
        setRetainInstance(true);
        if (getActivity().getIntent().hasExtra("fromPlanScreen")) {
            isFromPlanScreen = getActivity().getIntent().getBooleanExtra("fromPlanScreen", false);
        }
        sharedPrefsManager = new SharedPrefsManager(getActivity());
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

        if (pdflawflagOBJ != null) {

            if (pdflawflagOBJ.degree != null)
                flag.degree = pdflawflagOBJ.degree;
            else
                flag.degree = 0;

            if (pdflawflagOBJ.getXcoord() != null)
                flag.xcoord = Integer.valueOf(pdflawflagOBJ.getXcoord());
            if (pdflawflagOBJ.getYcoord() != null)
                flag.ycoord = Integer.valueOf(pdflawflagOBJ.getYcoord());
            if (pdflawflagOBJ.getViewx() != null)
                flag.viewx = Integer.valueOf(pdflawflagOBJ.getViewx());
            if (pdflawflagOBJ.getViewy() != null)
                flag.viewy = Integer.valueOf(pdflawflagOBJ.getViewy());
            flag.scale_factor = pdflawflagOBJ.scale_factor;
            if (pdflawflagOBJ.is_arrow_located > 0) {
                flag.is_arrow_located = 1;
            } else {
                flag.is_arrow_located = 0;
            }
            flag.tiltangle = 0;
            isLocationAlreadySet = true;

        } else {
            // Utils.showLogger("OldLocationIsNull");
            pdflawflagOBJ = new Pdflawflag();

            isLocationAlreadySet = false;
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


        iv_gps = (ImageView) rootView.findViewById(R.id.iv_satellite);
        tvAccuracy = (TextView) rootView.findViewById(R.id.tv_accuracy);

        compassAngleView = rootView.findViewById(R.id.compass_status);
        compassAngleView.setVisibility(View.GONE);
        iv_compass = (ImageView) rootView.findViewById(R.id.iv_compass);
        iv_plan = (ImageView) rootView.findViewById(R.id.iv_plan);
        iv_delete = (ImageView) rootView.findViewById(R.id.iv_delete);

        ll_cancel = (LinearLayout) rootView.findViewById(R.id.ll_previous);
        ll_save = (LinearLayout) rootView.findViewById(R.id.ll_next);
        iv_back_arrow = (ImageView) rootView.findViewById(R.id.iv_back_arrow);


        projectDocuMainActivity = ((PhotoAddDirectionMainActivity) getActivity());

        rotationArrow = (ImageView) rootView.findViewById(R.id.rotation_arrow);

        // rotationArrow.setRotation(110);
        planWebview = (EnhancedWebView) rootView.findViewById(R.id.webview_plan);

        touchHelperView = (ImageView) rootView.findViewById(R.id.touch_helper_view);

        captureButton = (ImageView) rootView.findViewById(R.id.button_capture_photo);

        debugTextView = (TextView) rootView.findViewById(R.id.debugTextView);
        debugTextView.setText("DEBUG TEXTVIEW:");
        iv_temp = rootView.findViewById(R.id.iv_temp);

        iv_back_arrow.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().onBackPressed();
            }
        });


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

//        moveArrowPosition(0,100);

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

                if (!arrowIsTouched) {
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
                (getActivity()).onBackPressed();
            }
        });

        iv_plan.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isFromPhoto && !isFromPlanScreen) {
                    Intent intentt = new Intent(SavePictureActivity.BR_ACTION_ADD_PLAN_LIST);
                    intentt.putExtra(PlansActivity.IGNORE_LOADING_LAST_PLAN, true);
                    getActivity().sendBroadcast(intentt);
                }
                (getActivity()).finish();

            }
        });

        iv_delete.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {

                showCustomDialog(getActivity(), getResources().getString(R.string.custom_dialog_title), "Are you sure you want to delete plan location?", 2, 0);


            }
        });

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
                                setArrowPosition();//On Click button rotate

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
                    if (!blockAutoPosition) {
                        setBlockAutoPosition(true);
                    }

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
                if (event.getAction() == MotionEvent.ACTION_UP && !blockSingleTouch) {
                    //System.out.println("#### ACTIONUP && blockSingleTouch == false");
                    if (System.currentTimeMillis() - currentTime > 50000000) {
                        if (!blockSingleTouch) {
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

        new OrientationEventListener((getActivity())) {
            @Override
            public void onOrientationChanged(int orientation) {
//                PhotoAddDirectionFragment.this.onOrientationChanged(orientation);


                if (orientation == OrientationEventListener.ORIENTATION_UNKNOWN || blockOrientationChange) {
                    return;
                }

                orientation = (orientation + 45) / 90 * 90;

                if (orientation >= 360) {
                    orientation = 0;
                }

                try {
                    WindowManager wm = (WindowManager) ((PhotoAddDirectionMainActivity) getActivity()).getSystemService(Context.WINDOW_SERVICE);

                    Display display = wm.getDefaultDisplay();

                    if (display.getOrientation() == Surface.ROTATION_0) {
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

                } catch (Exception e) {
                }
            }
        }.enable();

//        if (localPhotoId > -1) {
//        	((ProjectDocuMainActivity) getActivity()).setMenuBarCameraButtonToPhotoButton();
//        }
//        else {
//        	((ProjectDocuMainActivity) getActivity()).setMenuBarCameraButtonToCameraButton();
//        }
//
//        ((ProjectDocuMainActivity) getActivity()).createActionBar();

        //rootView.invalidate();


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

                        @Override
                        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
                            return super.onScroll(e1, e2, distanceX, distanceY);
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
        iv_gps.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!isGpsEnabled())
                {
                    showGPSEnableDialog();
                    return;
                }

//                previous implelentation
                if (!sharedPrefsManager.getBooleanValue(AppConstantsManager.ACTIVATE_GPS_MANUAL_STATE, false)) {
                    sharedPrefsManager.setBooleanValue(AppConstantsManager.ACTIVATE_GPS_MANUAL_STATE, true);//On GPS Select
                    iv_gps.setImageResource(R.drawable.active_gps);//GPS CLICK
//                    if (!sharedPrefsManager.getBooleanValue(AppConstantsManager.ACTIVATE_GPS_MANUAL_STATE, false)) {
                    setShowGPSAccuracy(true);
                    setBlockAutoPosition(false);
                    if (((PhotoAddDirectionMainActivity) getActivity()).gpsTracker != null) {
                        setPlanPositionByGps(((PhotoAddDirectionMainActivity) getActivity()).gpsTracker.getLocation());//on press button
                    }
                   // startLocationSchedule();
//                    }
                } else {
                    tvAccuracy.setVisibility(View.INVISIBLE);
                    setBlockAutoPosition(true);
                    iv_gps.setImageResource(R.drawable.satellite_icon);///onclick
                    sharedPrefsManager.setBooleanValue(AppConstantsManager.ACTIVATE_GPS_MANUAL_STATE, false);

                }

            }
        });
//        iv_gps.setOnLongClickListener(new View.OnLongClickListener() {
//            @Override
//            public boolean onLongClick(View v) {
//                if (!sharedPrefsManager.getBooleanValue(AppConstantsManager.ACTIVATE_GPS_MANUAL_STATE, false)) {
//                    sharedPrefsManager.setBooleanValue(AppConstantsManager.ACTIVATE_GPS_MANUAL_STATE, true);
//                    iv_gps.setImageResource(R.drawable.active_gps);
//                } else {
//                    iv_gps.setImageResource(R.drawable.satellite_icon);
//                    sharedPrefsManager.setBooleanValue(AppConstantsManager.ACTIVATE_GPS_MANUAL_STATE, false);
//                }
//
//
//                return false;
//            }
//        });

        if (sharedPrefsManager.getBooleanValue(AppConstantsManager.ACTIVATE_GPS_MANUAL_STATE, false)) {
            Utils.showLogger2("ACTIVATE_GPS_MANUAL_STATE");
            iv_gps.setImageResource(R.drawable.active_gps);//On Create
        } else {
            iv_gps.setImageResource(R.drawable.satellite_icon);//onCreatePrefCheck
        }

        boolean oldCompassValue = sharedPrefsManager.getBooleanValue(AppConstantsManager.VIEW_DIRECTION_THROUGH_COMPASS_MANUAL_STATE, false);

        //Utils.showLogger("oldCompassVALUE>>"+oldCompassValue);

        if (sharedPrefsManager.getBooleanValue(AppConstantsManager.VIEW_DIRECTION_THROUGH_COMPASS_MANUAL_STATE, true)) {//onCreate settings icons
            iv_compass.setImageResource(R.drawable.activte_compass_icon);
            blockAutoPosition = false;
            blockAutoDirection = false;
        } else {
            iv_compass.setImageResource(R.drawable.compas_icon);
            blockAutoPosition = true;
            blockAutoDirection = true;
            if (lastFragmentArrowAngle != 0)
                rotationArrow.setRotation(lastFragmentArrowAngle);
        }

        iv_compass.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                //previous implementation
//                setArrowByCompass(((PhotoAddDirectionMainActivity) getActivity()).compassDegrees);
//                setBlockAutoDirection(false);

                if (!sharedPrefsManager.getBooleanValue(AppConstantsManager.VIEW_DIRECTION_THROUGH_COMPASS_MANUAL_STATE, true)) {
                    sharedPrefsManager.setBooleanValue(AppConstantsManager.VIEW_DIRECTION_THROUGH_COMPASS_MANUAL_STATE, true);//Setting on click
                    iv_compass.setImageResource(R.drawable.activte_compass_icon);
                    blockAutoPosition = false;
                    blockAutoDirection = false;
                } else {
                    iv_compass.setImageResource(R.drawable.compas_icon);
                    sharedPrefsManager.setBooleanValue(AppConstantsManager.VIEW_DIRECTION_THROUGH_COMPASS_MANUAL_STATE, false);//Setting on click
                    blockAutoPosition = true;
                    blockAutoDirection = true;
                }
                boolean oldCompassValue = sharedPrefsManager.getBooleanValue(AppConstantsManager.VIEW_DIRECTION_THROUGH_COMPASS_MANUAL_STATE, false);
                Utils.showLogger("valuesOnClick>>" + oldCompassValue);

            }
        });
//        iv_compass.setOnLongClickListener(new View.OnLongClickListener() {
//            @Override
//            public boolean onLongClick(View v) {


//                if (!sharedPrefsManager.getBooleanValue(AppConstantsManager.VIEW_DIRECTION_THROUGH_COMPASS_MANUAL_STATE, false)) {
//                    sharedPrefsManager.setBooleanValue(AppConstantsManager.VIEW_DIRECTION_THROUGH_COMPASS_MANUAL_STATE, true);
//                    iv_compass.setImageResource(R.drawable.activte_compass_icon);
//                } else {
//                    iv_compass.setImageResource(R.drawable.compas_icon);
//                    sharedPrefsManager.setBooleanValue(AppConstantsManager.VIEW_DIRECTION_THROUGH_COMPASS_MANUAL_STATE, false);
//                }


//                return false;
//            }
//        });
        isViewCreated = true;
        View layout = (View) rootView.findViewById(R.id.rotation_arrow_helper);
        ViewTreeObserver vto = layout.getViewTreeObserver();
        vto.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
                    layout.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                } else {
                    layout.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                }
                widthHelper = layout.getMeasuredWidth();
                heightHelper = layout.getMeasuredHeight();

            }
        });
        return rootView;
    }

    private void showGPSEnableDialog() {

            new Builder(getActivity())
                    .setTitle("GPS not enabled")
                    .setMessage("Enable GPS to proceed")
                    .setPositiveButton("Enable GPS", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            // Open location settings
                            Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                            int REQUEST_ENABLE_GPS=2;
                            startActivityForResult(intent, REQUEST_ENABLE_GPS);
                        }
                    })
                    .setNegativeButton("Cancel", null)
                    .show();

    }

    public void setShowGPSAccuracy(boolean show) {
        showGPSAccuracy = show;
    }

    public void setBlockAutoPosition(boolean block) {
        blockAutoPosition = block;


        if (sharedPrefsManager.getBooleanValue(AppConstantsManager.ACTIVATE_GPS, false)) {
            int duration = Toast.LENGTH_SHORT;

            Toast toast;

            if (blockAutoPosition) {
                sharedPrefsManager.setBooleanValue(AppConstantsManager.ACTIVATE_GPS_MANUAL_STATE, false);
                iv_gps.setImageResource(R.drawable.satellite_icon);//When scrolls
                toast = Toast.makeText(((PhotoAddDirectionMainActivity) getActivity()), getResources().getString(R.string.toast_auto_position_off), duration);
                tvAccuracy.setVisibility(View.INVISIBLE);
            } else {
                iv_gps.setImageResource(R.drawable.active_gps);//SET BOCK AUTI FUNCTION
                toast = Toast.makeText(((PhotoAddDirectionMainActivity) getActivity()), getResources().getString(R.string.toast_auto_position_on), duration);
            }

            toast.show();
        }
    }

    public void setBlockAutoDirection(boolean block) {
        blockAutoDirection = block;


        Utils.showLogger("setBlockAutoDirection");

        if (!sharedPrefsManager.getBooleanValue(AppConstantsManager.VIEW_DIRECTION_THROUGH_COMPASS_MANUAL_STATE, false)) {
            sharedPrefsManager.setBooleanValue(AppConstantsManager.VIEW_DIRECTION_THROUGH_COMPASS_MANUAL_STATE, true);
            iv_compass.setImageResource(R.drawable.activte_compass_icon);
        } else {
            iv_compass.setImageResource(R.drawable.compas_icon);
            sharedPrefsManager.setBooleanValue(AppConstantsManager.VIEW_DIRECTION_THROUGH_COMPASS_MANUAL_STATE, false);
        }


        if (sharedPrefsManager.getBooleanValue(AppConstantsManager.VIEW_DIRECTION_THROUGH_COMPASS, false)) {
            int duration = Toast.LENGTH_SHORT;

            Toast toast;

            if (blockAutoDirection) {
                toast = Toast.makeText(((PhotoAddDirectionMainActivity) getActivity()), getResources().getString(R.string.toast_auto_direction_off), duration);

            } else {
                toast = Toast.makeText(((PhotoAddDirectionMainActivity) getActivity()), getResources().getString(R.string.toast_auto_direction_on), duration);
            }

            toast.show();
        }
    }


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

    /*public void arrowLocating() {
//		ProjectDocuDatabaseManager projectDocuDatabaseManager = new ProjectDocuDatabaseManager (getActivity());

        if (planWebview.rotatingArrowLocation == false) {
            planWebview.rotatingArrowLocation = true;

//			projectDocuDatabaseManager.updatePreferences(ProjectDocuDatabaseManager.COLUMN_PREFERENCES_IS_ARROW_LOCATED, 1);
            flag.is_arrow_located = 1;
            planWebview.oldRotationAngle = 0.0f;
            setArrowPosition();//not used

            rotationArrow.setVisibility(View.VISIBLE);
            arrowTouchHelperView.setVisibility(View.VISIBLE);
        } else if (planWebview.rotatingArrowLocation == true) {
            deactivateRotationLocator();
        }
    }*/

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
        bt.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

//                    Fragment fragment = defectDetailsPagerAdapter.getM1stFragment();
//                    if(fragment!=null){
//                        ((DefectDetailsDatesFragment) fragment).saveDefect();
//
//                    }
                if (pdflawflagOBJ != null)
                    new DeleteLocalFlawFlag().execute(pdflawflagOBJ);
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


/*    @SuppressWarnings("deprecation")
    private void onOrientationChanged(int orientation) {
        if (orientation == OrientationEventListener.ORIENTATION_UNKNOWN || blockOrientationChange == true) {
            return;
        }

        orientation = (orientation + 45) / 90 * 90;

        if (orientation >= 360) {
            orientation = 0;
        }

        try {
            WindowManager wm = (WindowManager) ((ProjectDocuMainActivity) getActivity()).getSystemService(Context.WINDOW_SERVICE);

            Display display = wm.getDefaultDisplay();

            if (display.getOrientation() == Surface.ROTATION_0) {
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

        } catch (Exception e) {
        }
    }*/

    private void createWebview() {
        //Utils.showLogger("onCreateWebView");
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
//            flag.xcoord=658;
//            flag.ycoord=-14;
//
//            flag.viewx=88;
//            flag.viewy=46;
//            flag.degree=118;

            planWebview.oldCrosshairPositionX = flag.xcoord;
            planWebview.oldCrosshairPositionY = flag.ycoord;

            planWebview.oldViewX = flag.viewx;
            planWebview.oldViewY = flag.viewy;

//            planWebview.oldCrosshairPositionX = 658;
//            planWebview.oldCrosshairPositionY = -14;
//
//            planWebview.oldViewX = 88;
//            planWebview.oldViewY = 46;
            planWebview.oldRotationAngle = (float) flag.degree;//OnCreateWebView
            //setArrowPosition();
            Utils.showLogger("oldDegree>>>"+planWebview.oldRotationAngle);
            if (planWebview.rotatingArrowLocation) {
                setArrowPosition();//onCreateWebView
                rotationArrow.setVisibility(View.VISIBLE);
                arrowTouchHelperView.setVisibility(View.VISIBLE);
                buttonArrowRotate.setImageResource(R.drawable.pd_button_rotate_on_041);
                // buttonArrowRotate.setImageResource(R.drawable.pd_button_rotate_off_040);
            }
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
        // planWebview.getSettings().setAppCacheEnabled(false);

        planWebview.getSettings().setLayoutAlgorithm(WebSettings.LayoutAlgorithm.NORMAL);
        planWebview.calculateSizes(0, 0, false);
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
//        planWebview.scaleFactor=flag.scale_factor;

/////////////////////////////////////////////////////////////////////////////////
/////////////////////////////////////////////////////////////////////////////////
/////////////////////////////////////////////////////////////////////////////////
/////////////////////////////////////////////////////////////////////////////////
/////////////////////////////////////////////////////////////////////////////////

        /// planWebview.getSettings().setAppCacheEnabled(false);
        planWebview.setScaleX(1.0f);
        planWebview.setScaleY(1.0f);
        planWebview.getSettings().setJavaScriptEnabled(true);

        planWebview.getSettings().setSupportZoom(true);
        planWebview.getSettings().setBuiltInZoomControls(true);
        planWebview.getSettings().setDisplayZoomControls(false);
        planWebview.getSettings().setUseWideViewPort(true);
        //planWebview.getSettings().setDefaultZoom(ZoomDensity.FAR);
        planWebview.getSettings().setLoadWithOverviewMode(true);
        planWebview.getSettings().setAllowFileAccess(true);

        planWebview.clearCache(true);
        planWebview.clearView();
        planWebview.loadDataWithBaseURL("file:///android_asset/", webviewHtml, "text/html", "utf-8", "");
        planWebview.getSettings().setLayoutAlgorithm(WebSettings.LayoutAlgorithm.NORMAL);


        planWebview.setPictureListener(new PictureListener() {
            @Override
            public void onNewPicture(WebView view, Picture picture) {
                //  Utils.showLogger("onNewPicture");
                if (!planWebview.noMoreScroll) {
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

        public JavaScriptHandler(PhotoAddDirectionFragment activity) {
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

//        if (sharedPrefsManager.getBooleanValue(AppConstantsManager.VIEW_DIRECTION_THROUGH_COMPASS, false)) {
//            ((PhotoAddDirectionMainActivity) getActivity()).activateCompass();
//            deActivateCompassButton();
//        } else {
//            ((PhotoAddDirectionMainActivity) getActivity()).deactivateCompass();
//            activateCompassButton();
//        }
//
//
//        if (sharedPrefsManager.getBooleanValue(AppConstantsManager.ACTIVATE_GPS, false)) {
//            ((PhotoAddDirectionMainActivity) getActivity()).activateGPS();
//            activateGPSButton();
//        } else {
//            ((PhotoAddDirectionMainActivity) getActivity()).deactivateGPS();
//            deactivateGPSButton();
//        }
//
//		if ((Integer) projectDocuDatabaseManager.selectDataFromPreferences(ProjectDocuDatabaseManager.COLUMN_PREFERENCES_LOCATION_VIA_GPS) == 1) {
//			((ProjectDocuMainActivity) getActivity()).activateGPS();
//			((ProjectDocuMainActivity) getActivity()).activateGPSButton();
//		} else {
//			((ProjectDocuMainActivity) getActivity()).deactivateGPS();
//		}
//
        if (((PhotoAddDirectionMainActivity) getActivity()).referPointList == null || ((PhotoAddDirectionMainActivity) getActivity()).referPointList != null && ((PhotoAddDirectionMainActivity) getActivity()).referPointList.size() == 0) {
            deActivateCompassButton();
            deactivateGPSButton();//When refferal poits null
            ((PhotoAddDirectionMainActivity) getActivity()).deactivateGPS();
            ((PhotoAddDirectionMainActivity) getActivity()).deactivateCompass();
            iv_gps.setEnabled(false);
//                int duration = Toast.LENGTH_LONG;
//                Toast toast = Toast.makeText(((PhotoAddDirectionMainActivity) getActivity()), getResources().getString(R.string.toast_plan_no_gps_reference), duration);
//                toast.show();
        } else {
            if (!sharedPrefsManager.getBooleanValue(AppConstantsManager.ACTIVATE_GPS, false)) {
                deactivateGPSButton();//ON RESUME
                iv_gps.setEnabled(false);
                deactiveBothLocationAndCompass();
            }
            if (!sharedPrefsManager.getBooleanValue(AppConstantsManager.FIND_POSITION_IF_GEO_REFERENCED_PLAN, false)) {
                deactiveBothLocationAndCompass();
                iv_gps.setEnabled(false);

                //iv_compass.setEnabled(false);
                // Utils.showLogger("deactivate1");
            } else {

            }

        }
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

    private void deactiveBothLocationAndCompass() {
        Utils.showLogger("deactiveBothLocationAndCompass");
        deactivateGPSButton();//DEACTIVTE BOTH
        activateCompassButton();
        iv_compass.setEnabled(false);
        iv_compass.setAlpha(0.2f);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        //System.out.println("##### ProjectDocuShowPlanFragment:onViewCreated() scale:"+ planWebview.scaleFactor);
//		ProjectDocuDatabaseManager projectDocuDatabaseManager = new ProjectDocuDatabaseManager (getActivity());


//		ArrayList <Plan> planObjectList = projectDocuDatabaseManager.selectDataFromPlan(((ProjectDocuMainActivity) getActivity()).currentPlanId);

//		if (planObjectList != null && planObjectList.size() > 0) {
//			northDeviationAngle = -planObjectList.get(0).north_deviation_angle;
//		}
//
        blockAutoPosition = false;
        //blockAutoDirection = false;


        new RetrievePlansUsingPLanIDTask(getActivity(), projectID, planID).execute();


     /*   createWebview();

        //System.out.println("##### scaling: "+planWebview.getScaleX() + " - "+planWebview.getScaleY()+ " - " +planWebview.scaledCrosshairPositionX + " - " +planWebview.scaledCrosshairPositionY + " - " +planWebview.scaleFactor  + " - " +planWebview.getScale() + " - " + getResources().getDisplayMetrics().density);


        if (flag.is_arrow_located == 1) {
            planWebview.arrowRotationAngle = planWebview.oldRotationAngle;

            setArrowPosition();

                    .setVisibility(View.VISIBLE);
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
//        getTwoPointsFromAngle(Math.round(planWebview.arrowRotationAngle));


        int viewX = (int) planWebview.viewX;
        int viewY = (int) planWebview.viewY;

        if (flag.is_arrow_located == 1) {
            viewY = viewY * -1;
        }

//        if (blockAutoDirection) {
//            planWebview.arrowRotationAngle = planWebview.arrowRotationAngle + 90;
//        }

//        int[] location = new int[2];
//        rotationArrow.getLocationOnScreen(location);
//        Toast.makeText(getActivity(), "X axis is " + location[0] + "and Y axis is " + location[1], Toast.LENGTH_LONG).show();

        // if (flag.is_arrow_located == 1) {
        int rotationAngleForBackend = 180;

        if (flag.is_arrow_located == 1) {
            rotationAngleForBackend = (int) Math.round(planWebview.arrowRotationAngle);
        }

        HomeActivity.flags.degree = (int) Math.round(planWebview.arrowRotationAngle);
//        HomeActivity.flags.degree = 180;
        HomeActivity.flags.viewx = viewX;
        HomeActivity.flags.viewy = viewY;
        HomeActivity.flags.xcoord = (int) planWebview.crosshairPositionX;
        HomeActivity.flags.ycoord = (int) planWebview.crosshairPositionY;
        HomeActivity.flags.scale_factor = planWebview.scaleFactor;
        HomeActivity.flags.tiltangle = 0;


        pdflawflagOBJ.setLocal_photo_id(photoId);
        pdflawflagOBJ.setPdProjectid(projectID);
        pdflawflagOBJ.setPdplanid(planID);

        pdflawflagOBJ.setXcoord((int) planWebview.crosshairPositionX + "");
        pdflawflagOBJ.setYcoord((int) planWebview.crosshairPositionY + "");
//        pdflawflagOBJ.setXcoord((int) planWebview.crosshairPositionX + "");
//        pdflawflagOBJ.setYcoord((int) planWebview.crosshairPositionY + "");
        pdflawflagOBJ.setViewx(viewX + "");
        pdflawflagOBJ.setViewy(viewY + "");
        pdflawflagOBJ.setScale_factor(HomeActivity.flags.scale_factor);
        pdflawflagOBJ.setDegree((int) Math.round(planWebview.arrowRotationAngle));
        pdflawflagOBJ.setIs_arrow_located(flag.is_arrow_located);
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.GERMANY);
        String photoDate = simpleDateFormat.format(new Date());
        pdflawflagOBJ.setCreated(photoDate);
        pdflawflagOBJ.setLastupdated(photoDate);


//        callUpdatePlanFlagsAPI(pdflawflagOBJ.getXcoord(),pdflawflagOBJ.getYcoord(),viewX+"",viewY+"");

        new CreateOrUpdateLocalFlawFlag().execute(pdflawflagOBJ);
        signal = new CountDownLatch(1);
        try {
            signal.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }


        if (getActivity() != null) {
            Intent intent = new Intent("updateFlawFlag");
            intent.putExtra(SavePictureActivity.PLAN_ATTACH_TO_PHOTO_KEY, true);
            intent.putExtra(SavePictureActivity.PLAN_ID_KEY, planID);
            getActivity().sendBroadcast(intent);

            new updatePhotoAsyncTask(Long.valueOf(photoId), projectID).execute();

            (getActivity()).onBackPressed();
        }
//        for (int i = 0; i < getActivity().getSupportFragmentManager().getBackStackEntryCount(); i++) {
//            getActivity().finish();
//        }

//        if (flag.is_arrow_located == 1) {
//            viewY = viewY * -1;
//        }

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

    public class updatePhotoAsyncTask extends AsyncTask<Void, Void, Void> {
        private long photoId;
        private String projectID;
        private PhotoModel photoModelObj;

        updatePhotoAsyncTask(long photoId, String projectId) {
            this.photoId = photoId;
            projectID = projectId;

        }

        @Override
        protected Void doInBackground(Void... params) {
            ProjectsDatabase db = ProjectsDatabase.getDatabase(getActivity());
            PhotoModel photoModelObj = db.photoDao().getPhotosOBJ(photoId + "");
            if (photoModelObj != null) {
                photoModelObj.setPhotoSynced(false);
                photoModelObj.setPhotoUploadStatus(LocalPhotosRepository.UN_SYNC_PHOTO);
                db.photoDao().update(photoModelObj);
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            Intent intentt = new Intent("updateProfile");
            getActivity().sendBroadcast(intentt);
        }
    }

    private void callUpdatePlanFlagsAPI(String xCoor, String yCoor, String xView, String yView) {
        if (!ProjectDocuUtilities.isNetworkConnected(getActivity())) {
            Toast.makeText(getActivity(), getActivity().getResources().getString(R.string.no_internet_message), Toast.LENGTH_SHORT).show();
            return;
        }
        SharedPrefsManager sharedPrefsManager = new SharedPrefsManager(getActivity());
        RetroApiInterface retroApiInterface = RetrofitManager.getInstance().create(RetroApiInterface.class);
        String authToken = sharedPrefsManager.getStringValue(AppConstantsManager.AUTH_API_TOKEN, "");

        if (authToken.length() > 2) {
            authToken = authToken.substring(1, authToken.length() - 1);
        }
        SimpleDateFormat yyyMMddHHmmssFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.GERMAN);
        String photoDate = yyyMMddHHmmssFormat.format(new Date());
        JsonObject params = new JsonObject();


        params.addProperty("pdflagid", Integer.valueOf("1913"));
//
        params.addProperty("pdphotoid", Integer.valueOf("6057"));
        params.addProperty("pdplanid", Integer.valueOf("115"));

        params.addProperty("xcoord", xCoor);

        params.addProperty("ycoord", yCoor);

        params.addProperty("viewx", xView);

        params.addProperty("viewy", yView);


        Call<JsonObject> call = retroApiInterface.getUpdatePlansParamsAPI(authToken, Utils.DEVICE_ID, projectID, params);

        call.enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                String pdflagid = "";
                if (response.isSuccessful()) {
                    if (response.body() != null) {
                        JsonObject jsonObject = response.body();
                        try {
                            if (jsonObject.has("data")) {
                                JsonObject jsonObjectData = null;

                                jsonObjectData = jsonObject.getAsJsonObject("data");

                                if (jsonObjectData.has("pdflagid")) {
                                    pdflagid = jsonObject.getAsJsonObject("data").get("pdflagid").getAsString();

//                                    new Handler(Looper.getMainLooper()).post(new Runnable() {
//                                        @Override
//                                        public void run() {
//                                            Toast.makeText(getApplicationContext(), "CreateOrUpdateLocalFlawFlag", Toast.LENGTH_SHORT).show();
//                                        }
//                                    });


                                }

                            }
                        } catch (JsonParseException e) {
                        } catch (ClassCastException e) {
                        }
                    } else {
//                        Log.d("Login", "Empty response");
//                        Toast.makeText(getActivity(), getString(R.string.toast_login_failed), Toast.LENGTH_SHORT).show();
                    }
                } else {

//                    Log.d("Login", "Not Success : " + response.toString());
                    //  Toast.makeText(context, getString(R.string.toast_login_failed), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                Log.d("Login", "failed : " + t.getMessage());
//                Toast.makeText(getApplication(), "Failures Success", Toast.LENGTH_SHORT).show();
            }
        });
    }


    private class DeleteLocalFlawFlag extends AsyncTask<Pdflawflag, Void, Void> {
        private DefectsDao mAsyncTaskDao;

        DeleteLocalFlawFlag() {
        }

        @Override
        protected Void doInBackground(final Pdflawflag... params) {

            PdFlawFlagRepository pdFlawFlagRepository = new PdFlawFlagRepository(getContext(), projectID);
            pdFlawFlagRepository.getmDefectsPhotoDao().deleteUsingLocalFlawFlagID(params[0].getPdProjectid(), params[0].getLocalPdflawflagId() + "");

            if (getActivity() != null) {
                Intent intent = new Intent("updateFlawFlag");
                intent.putExtra(SavePictureActivity.PLAN_ATTACH_TO_PHOTO_KEY, false);
                intent.putExtra(SavePictureActivity.PLAN_ID_KEY, planID);
                getActivity().sendBroadcast(intent);
                getActivity().finish();
            }
            return null;
        }
    }

    PdFlawFlagRepository pdFlawFlagRepository = new PdFlawFlagRepository(getContext(), projectID);

    private class CreateOrUpdateLocalFlawFlag extends AsyncTask<Pdflawflag, Void, Void> {
        private DefectsDao mAsyncTaskDao;

        CreateOrUpdateLocalFlawFlag() {
        }

        @Override
        protected Void doInBackground(final Pdflawflag... params) {
            int viewY = (int) planWebview.viewY;
//            pdFlawFlagRepository.getmDefectsPhotoDao().insert(params[0]);
// Commented by usman issue to update flawFlags
//            Pdflawflag pdflawflagg = pdFlawFlagRepository.getmDefectsPhotoDao().getFlawFlagOBJExist(params[0].getPdProjectid(), params[0].getLocal_flaw_Id());
            Pdflawflag pdflawflagg = pdFlawFlagRepository.getmDefectsPhotoDao().getFlawFlagOBJExistWithPlanId(params[0].getPdProjectid(), params[0].getPdplanid(), params[0].getLocal_photo_id());
            if (pdflawflagg == null) {
                pdFlawFlagRepository.getmDefectsPhotoDao().insert(params[0]);
            } else {
                Pdflawflag pdflawflag = new Pdflawflag();
                pdflawflag = params[0];
                pdflawflag.setFlaw_Id(pdflawflagg.getFlaw_Id());
                pdflawflag.setFlaw_status(pdflawflagg.getFlaw_status());
                pdflawflag.setPdflawflagid(pdflawflagg.getPdflawflagid());

                if (flag.is_arrow_located == 1) {
                    viewY = viewY * -1;
                }

                pdflawflag.setPdProjectid(projectID);
                pdflawflag.setPdplanid(planID);
                pdflawflag.setLocal_photo_id(photoId);
                pdflawflag.setXcoord((int) planWebview.crosshairPositionX + "");
                pdflawflag.setYcoord((int) planWebview.crosshairPositionY + "");
                pdflawflag.setViewx((int) planWebview.viewX + "");
                pdflawflag.setViewy(viewY + "");
                pdflawflag.setScale_factor(planWebview.scaleFactor);
                pdflawflag.setDegree((int) Math.round(planWebview.arrowRotationAngle));
                pdflawflag.setIs_arrow_located(flag.is_arrow_located);
                pdFlawFlagRepository.getmDefectsPhotoDao().deleteUsingLocalDeletePhotoId(projectID, photoId);
                pdFlawFlagRepository.getmDefectsPhotoDao().insert(pdflawflag);

            }

            plansModelOBJ.setDegree((int) Math.round(planWebview.arrowRotationAngle));
            plansModelOBJ.setXcoord((int) planWebview.crosshairPositionX);
            plansModelOBJ.setYcoord((int) planWebview.crosshairPositionY);
            plansModelOBJ.setViewx((int) planWebview.viewX);

            if (flag.is_arrow_located == 1) {
                viewY = viewY * -1;
            }
            plansModelOBJ.setViewy(viewY);
            plansModelOBJ.setScale_factor(planWebview.scaleFactor);
            plansModelOBJ.setIs_arrow_located(flag.is_arrow_located);
            sharedPrefsManager.setLastUsedPlanId(getActivity(), planID);
            ProjectsDatabase.getDatabase(getActivity()).plansDao().update(plansModelOBJ);
            signal.countDown();

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

        }
    }


    private void setArrowPosition() {
        flag.is_arrow_located = 1;

        Utils.showLogger("setArrowPosition");
        getTwoPointsFromAngle(planWebview.oldRotationAngle);

        if (planWebview.oldRotationAngle > 0) {
            Utils.showLogger("angleRotation>>"+ planWebview.oldRotationAngle);
            rotationArrow.setRotation((float) planWebview.oldRotationAngle);//from compass
            planWebview.arrowRotationAngle = (float) planWebview.oldRotationAngle;
        } else {
            rotationArrow.setRotation(0.0f);
            planWebview.arrowRotationAngle = 0.0f;
        }
    }

    public void activateCompassButton() {
        iv_compass.setEnabled(true);
        // iv_compass.setAlpha(0f);
    }

    public void deActivateCompassButton() {
        //   iv_compass.setEnabled(false);
        //Utils.showLogger("deactivate2");
        iv_compass.setAlpha(0.2f);
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

    public void activateGPSButton() {
        Utils.showLogger("activateGPSButton");
        iv_gps.setEnabled(true);

    }

    public void activateGPSButton2() {
        Utils.showLogger("activateGPSButton");


    }

    public void deactivateGPSButton() {
        Utils.showLogger("deactivateGPSButton");
        iv_gps.setEnabled(false);
        iv_gps.setAlpha(0.2f);
        tvAccuracy.setVisibility(View.INVISIBLE);
    }


    public void setArrowByCompass(float compassDegree) {

        Utils.showLogger2(compassDegree+"");
        Utils.showLogger2((compassDegree+275)+"+275");
        //Utils.showLogger2((compassDegree-90)+"-90");
        if (blockAutoDirection) {
            return;
        }
        //Utils.showLogger("setArrowByCompass");
        //compassAngleView.setText(compassDegree+"");
        // corrects the compassDegree by the settings value (just need for some devices)
//		float compassCorrectionValue =  getCompassCorrectionValue();
        float compassCorrectionValue = 0;
        Log.d("plan_compass_angle_bef", compassDegree + "");
        Log.d("deviation angle", northDeviationAngle + "");
        Log.d("rotate icons", rotateIcons + "");
//        float fixedDegree = 270.0f + (compassDegree + 180.0f) + northDeviationAngle + rotateIcons + compassCorrectionValue;
//        float fixedDegree = compassDegree + northDeviationAngle;
        float fixedDegree = compassDegree + northDeviationAngle;
        //		float fixedDegree = 270.0f + (compassDegree + 180.0f) + northDeviationAngle;
//        if (fixedDegree > 0) {
//            planWebview.oldRotationAngle = fixedDegree;
//        } else {
//            planWebview.oldRotationAngle = 360.0f + fixedDegree;
//        }
        if (fixedDegree > 360) {
            fixedDegree = fixedDegree - 360;
        } else if (fixedDegree < 0) {
            fixedDegree = fixedDegree + 360;
        }

        planWebview.oldRotationAngle = fixedDegree;//From compass

        planWebview.arrowRotationAngle  = fixedDegree;
        setArrowPosition();//when angle from compas received
    }





    public void setPlanPositionByGps(Location planLocation) {
        Utils.showLogger("setPlanPositionByGps>>" + blockAutoPosition);
        //  planLocation.setLatitude(	44.500000f);
        // planLocation.setLongitude(	-89.500000f);

//        Utils.showLogger("setPlanPositionByGps"+planLocation.getLatitude()+"::"+planLocation.getLongitude());
        //System.out.println("##### ProjectDocuShowPlanFragment:setPlanPositionByGps()");
        if (blockAutoPosition) {
            return;
        }

        if (planLocation == null) {
//			((ProjectDocuMainActivity) getActivity()).deactivateGPSButton();
           // ((PhotoAddDirectionMainActivity) getActivity()).deactivateGPSNonStop();//when plan location is null
          //  deactivateGPSButton();//When location is null gps
            int duration = Toast.LENGTH_LONG;

         //   Toast toast = Toast.makeText(getActivity(), getResources().getString(R.string.toast_gps_no_signal), duration);
           // toast.show();

            return;
        }
        tvAccuracy.setVisibility(View.VISIBLE);

        Float accuracy = planLocation.getAccuracy();
        DecimalFormat decimalFormat = new DecimalFormat("#.#");
        String formattedNumber = decimalFormat.format(accuracy);
        Utils.showLogger("accracy>>"+accuracy);
        try {
            if (planLocation.getAccuracy() < 30.0) {
                tvAccuracy.setText(formattedNumber + "");
                tvAccuracy.setBackgroundResource(R.drawable.bg_accuracy_green);
            } else if (planLocation.getAccuracy() < 70.0) {
                tvAccuracy.setText(formattedNumber + "");
                tvAccuracy.setBackgroundResource(R.drawable.bg_accuracy_yellow);
            } else {
                tvAccuracy.setText(formattedNumber + "");

                tvAccuracy.setBackgroundResource(R.drawable.bg_accuracy_red);
            }
        } catch (Exception e) {
            e.printStackTrace();
            Utils.showLogger2(e.getMessage());
        }

/*        // Checken ob GPS-Genauigkeit über 100m liegt!
        if (planLocation.getAccuracy() > 100.0) {
//			((ProjectDocuMainActivity) getActivity()).deactivateGPSButton();
//			((ProjectDocuMainActivity) getActivity()).deactivateGPSNonStop();

            int duration = Toast.LENGTH_LONG;

            String strText = "" + getResources().getString(R.string.toast_gps_accuraccy_imprecise_info) + " " + planLocation.getAccuracy() + "m";

            Toast toast = Toast.makeText((getActivity()), strText, duration);
            toast.show();

            return;
        }*/

        GeoPoint locationPoint = new GeoPoint();

        locationPoint.lat = planLocation.getLatitude();
        locationPoint.lon = planLocation.getLongitude();

//        locationPoint.lat = 31.504538874537484;
//        locationPoint.lon = 74.32911536294087;


        if (getActivity() != null && ((PhotoAddDirectionMainActivity) getActivity()).referPointList != null && ((PhotoAddDirectionMainActivity) getActivity()).referPointList.size() > 0)
            referPointList = ((PhotoAddDirectionMainActivity) getActivity()).referPointList;
        if (referPointList != null && referPointList.size() > 0) {
            try {
                // Starte das Parsen des Check-for-Image-JSON vom Backend

                // Geopoint array nur initialisieren wenn mindestens zwei RefPoints vom Backend kommen
                if (referPointList.size() >= 2) {

                    refGeoPoints = new GeoPoint[2];

                    refGeoPoints[0] = new GeoPoint();
                    refGeoPoints[1] = new GeoPoint();

                    if (referPointList.get(0) != null && referPointList.get(1) != null) {
                        refGeoPoints[0].x = referPointList.get(0).getxCoord();
                        refGeoPoints[0].y = referPointList.get(0).getyCoord();
                        refGeoPoints[0].lon = referPointList.get(0).getLon();
                        refGeoPoints[0].lat = referPointList.get(0).getLat();

                        refGeoPoints[1].x = referPointList.get(1).getxCoord();
                        refGeoPoints[1].y = referPointList.get(1).getyCoord();
                        refGeoPoints[1].lon = referPointList.get(1).getLon();
                        refGeoPoints[1].lat = referPointList.get(1).getLat();

                        locationPoint = ProjectDocuUtilities.getLocations(locationPoint, refGeoPoints[0], refGeoPoints[1]);

                        // Wenn GPS Position innerhalb des gewählten Plans ist, Position auf Karte setzen
                        if (Math.abs(locationPoint.y) > 0 && Math.abs(locationPoint.y) < planWebview.planHeight / 2 && Math.abs(locationPoint.x) > 0 && Math.abs(locationPoint.x) < planWebview.planWidth) {
                            //planWebview.invalidate();

                            flag.scale_factor = planWebview.scaleFactor;
                            planWebview.scrollTo(0, 0);
                            setPlanPosition((float) locationPoint.x, (float) locationPoint.y);
                        } else {
                            try {
                                //((ProjectDocuMainActivity) getActivity()).deactivateGPSNonStop();

                                //iv_gps.callOnClick();
                            } catch (Exception e) {
                                e.printStackTrace();
                            }

                            int duration = Toast.LENGTH_LONG;

                            Toast toast = Toast.makeText((getActivity()), getResources().getString(R.string.toast_gps_out_of_range), duration);
                            toast.show();
                        }
                    } else {
                        int duration = Toast.LENGTH_LONG;

                        Toast toast = Toast.makeText((getActivity()), getResources().getString(R.string.toast_plan_no_gps_reference), duration);
                        toast.show();

                        refGeoPoints = null;
                    }
                } else {
                    deActivateCompassButton();
                    deactivateGPSButton();
                    ((PhotoAddDirectionMainActivity) getActivity()).deactivateCompass();
                    ((PhotoAddDirectionMainActivity) getActivity()).deactivateGPS();
                }
            } catch (Exception e) {
                refGeoPoints = null;
            }

        } else {
            refGeoPoints = null;
        }
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
        if (!blockAutoDirection) {
            setBlockAutoDirection(true);
        }

        float centerX = (float) rotationArrow.getWidth() / 2.0f;
        float centerY = (float) rotationArrow.getHeight() / 2.0f;

        double rotation = getAngleFromTwoPoints(centerX, centerY, touchPosX, touchPosY);
        double arrowRotation = rotation + 90;

        rotationArrow.setRotation(Math.round(arrowRotation));//manual rotate

        if (arrowRotation > 360.0f) {
            planWebview.arrowRotationAngle = (float) arrowRotation - 360.0f;
        } else {
            planWebview.arrowRotationAngle = (float) arrowRotation;
        }

//        getTwoPointsFromAngle(angle);
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        outState.putDouble(ARROW_KEY, planWebview.arrowRotationAngle);
        super.onSaveInstanceState(outState);

        Utils.showLogger("Fragment onSaveInstanceState>>>" + planWebview.arrowRotationAngle);
    }

    @Override
    public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);

        if (savedInstanceState == null)
            return;

        Double lastAngle = savedInstanceState.getDouble(ARROW_KEY, 0);
        lastFragmentArrowAngle = lastAngle.intValue();
        PhotoAddDirectionMainActivity photoAddDirectionMainActivity = (PhotoAddDirectionMainActivity) getActivity();
        photoAddDirectionMainActivity.lastArrowAngle = lastFragmentArrowAngle;


        Utils.showLogger("Fragment onViewStateRestored");

    }

    private void getTwoPointsFromAngle(double currentAngle) {
       // double y = (Math.cos(angle * (Math.PI / 180)) * 100);
        //double x = (Math.sin(angle * (Math.PI / 180)) * 100);

        //double angle = Math.toRadians(currentAngle-90);

        double radius = 200;
       double angle = (currentAngle) % 360;

        double target_x = radius * Math.sin(Math.toRadians(angle));
        double target_y = radius * Math.cos(Math.toRadians(angle));

        //double viewx = Math.round(target_x);
        //double viewy = Math.round(target_y) * -1;

//        x = (angle * .5) * Math.cos(360) + planWebview.viewX;
//        y = (angle * .5) * Math.sin(360) + planWebview.viewY;

//
//        int radius = (int) (angle * 0.0174532925);
//        int radius = 60;
     double  viewx = radius * Math.sin(Math.PI  * angle / 180);
      double   viewy = radius * Math.cos(Math.PI  * angle / 180)*-1;

        planWebview.viewX = (float) viewx;
        planWebview.viewY = (float) viewy;


        planWebview.viewX = (float) target_x;
        planWebview.viewY = (float) target_y;

       // float centerX = (float) rotationArrow.getWidth() / 2.0f;
       // float centerY = (float) rotationArrow.getHeight() / 2.0f;

        Double d = currentAngle+275;
        Utils.showLogger2("viewXYAuto>>"+planWebview.viewX+"::"+planWebview.viewY+"=="+d.intValue());

//        double rotation = getAngleFromTwoPoints(centerX, centerY, planWebview.viewX, planWebview.viewY);
//        double arrowRotation = rotation + 90;
//        Log.d("plan_compass_angle", angle + " x " + y + " y " + y + "");
    }

    private double getAngleFromTwoPoints(float centerX, float centerY, float touchX, float touchY) {
        ///  Utils.showLogger("getAngleFromTwoPoints");
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

        Utils.showLogger2("viewXYManual>>"+planWebview.viewX+"::"+planWebview.viewY+"=="+Math.toDegrees(inRads));

        return Math.toDegrees(inRads);
    }

    // sets the plan position to a specific x/y koordinate
    // ATTENTION: the x/y position in the plan is not the crosshair or oldCrosshair position.
    public void setPlanPosition(float x, float y) {
        Utils.showLogger("setPlanPosition" + x + "::" + y);

        if (planWebview.getScreenOrientation() == Configuration.ORIENTATION_PORTRAIT)
            y = y - planWebview.getStatusBarHeight() / 2;


        // Utils.showLogger("oldXPosition>>"+x);
        //Utils.showLogger("oldYPosition>>"+y);


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

//        x = Math.round((planWebview.oldCrosshairPositionX * currentScale));
//        y = Math.round((planWebview.oldCrosshairPositionY * currentScale));

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

            //x = scrollX ;
//            y = scrollY ;

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

//        planWebview.scrollTo((int) x, (int) y);
        planWebview.scrollBy((int) x, (int) y);



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
                        if (writeResponseBodyToDisk(response.body(), projectID)) {
                            if (imagePath != null && !imagePath.equals("")) {

//                                myBitmap = BitmapFactory.decodeFile(imagePath);
                                myBitmap = BitmapFactory.decodeFile(imagePath);
//                     Bitmap bitmap=myBitmap.createScaledBitmap(myBitmap,myBitmap.getWidth()*2,myBitmap.getHeight()*2,true);
                                planWidth = myBitmap.getWidth();
                                planHeight = myBitmap.getHeight();
//								new PlansPhotoRepository.UpdateAsyncTask(mDefectsPhotoDao).execute(projectModel);
                                createWebview();
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


            return plansModel;
        }

        @Override
        protected void onPostExecute(PlansModel plansModelList) {
            super.onPostExecute(plansModelList);

            //Utils.showLogger("gettingPlanModel>>>");

            plansModelOBJ = plansModelList;

            if (!isLocationAlreadySet) {
                try {
                    if (sharedPrefsManager.getBooleanValue(AppConstantsManager.AUTO_LOAD_LAST_PLAN, true)) {

                        //  Utils.showLogger("");

                        //added by billal bilal

                        Integer lastYCoordintate = plansModelOBJ.getYcoord();
                        Integer lastXCoordintate = plansModelOBJ.getXcoord();


                        if (lastXCoordintate == null)
                            lastXCoordintate = 0;

                        if (lastYCoordintate == null)
                            lastYCoordintate = 0;


                        if (lastXCoordintate != 0 && lastYCoordintate != 0) {
                            flag.ycoord = lastYCoordintate;
                            flag.xcoord = lastXCoordintate;

                            Utils.showLogger("bilalYCoor>>" + flag.ycoord);


                            planWebview.oldCrosshairPositionX = flag.xcoord;
                            planWebview.oldCrosshairPositionY = flag.ycoord;

                            planWebview.crosshairPositionX = planWebview.oldCrosshairPositionX;
                            planWebview.crosshairPositionY = planWebview.oldCrosshairPositionY;

                            Utils.showLogger("oldYPosition>>" + flag.ycoord);
                        }

                    }
                } catch (Exception e) {
                    Utils.showLogger("Error while PhotoActivityDir>>" + e.getMessage());
                    e.printStackTrace();
                }
            }
            if (plansModelOBJ != null && plansModelOBJ.getNorthDeviationAngle() != null && !plansModelOBJ.getNorthDeviationAngle().equals("")) {
                northDeviationAngle = -Integer.valueOf(plansModelOBJ.getNorthDeviationAngle());
            }

            if (plansModelOBJ != null && plansModelOBJ.getPlanPhotoPathLargeSize() != null && !plansModelOBJ.getPlanPhotoPathLargeSize().equals("")) {
                myBitmap = BitmapFactory.decodeFile(plansModelOBJ.getPlanPhotoPathLargeSize());
                planWidth = myBitmap.getWidth();
                planHeight = myBitmap.getHeight();

//                planWidth = 3536;
//                planWidth = 5000;
//                planHeight = 5000;
                imagePath = plansModelOBJ.getPlanPhotoPathLargeSize();
                if (flag.xcoord == null || flag.xcoord == 0) {
                    if (plansModelOBJ != null && plansModelOBJ.getXcoord() != null && plansModelOBJ.getXcoord() != 0) {
                        flag.xcoord = plansModelOBJ.getXcoord();
                        flag.ycoord = plansModelOBJ.getYcoord();
                        flag.scale_factor = plansModelOBJ.scale_factor;

                        flag.viewx = plansModelOBJ.getViewx();
                        flag.viewy = plansModelOBJ.getYcoord();
                        flag.degree = plansModelOBJ.getDegree();
                        flag.is_arrow_located = plansModelOBJ.is_arrow_located;

                    } else {
                        flag.xcoord = (int) planWidth / 2;
                        flag.ycoord = (int) planHeight / 2;
//                        flag.scale_factor = 1f;
                    }
                }

                //   Utils.showLogger("my_y_recor>>"+flag.ycoord);

                createWebview();

                if (flag.is_arrow_located == 1) {
                    planWebview.arrowRotationAngle = planWebview.oldRotationAngle;
                    setArrowPosition();//When retrieve from local db
                    rotationArrow.setVisibility(View.VISIBLE);
                    arrowTouchHelperView.setVisibility(View.VISIBLE);
                    buttonArrowRotate.setImageResource(R.drawable.pd_button_rotate_on_041);
                }
            } else {
                callGetPlanImageAPI(getActivity(), planID);
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        myBitmap = null;
        bitmap = null;
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

    public void startLocationSchedule() {
        Utils.showLogger2("startLocationSchedule");
        Handler handler1 = new Handler();
        handler1.postDelayed(new Runnable() {
            @Override
            public void run() {

                if (!keepOnRunning) {
                    return;
                }
                if (getActivity() == null) {
                    keepOnRunning = false;
                    return;

                }
                if (sharedPrefsManager.getBooleanValue(AppConstantsManager.ACTIVATE_GPS_MANUAL_STATE, false)) {
                    Utils.showLogger2("startLocationScheduleInside");
                    if (((PhotoAddDirectionMainActivity) getActivity()).gpsTracker != null) {
                        setPlanPositionByGps(((PhotoAddDirectionMainActivity) getActivity()).gpsTracker.getLocation());
                        Utils.showLogger2("startLocationScheduleInside2");
                    }else
                        Utils.showLogger2("startLocationScheduleOutsid2");
                }
                else
                    Utils.showLogger2("startLocationScheduleOutside");

                startLocationSchedule();
            }
        }, 800);
    }

    private boolean isGpsEnabled() {
        LocationManager locationManager = (LocationManager) getActivity().getSystemService(LOCATION_SERVICE);
        return locationManager != null && locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
    }
}
