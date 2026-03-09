package com.projectdocupro.mobile.activities;

import android.Manifest;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.MemoryInfo;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.hardware.Camera;
import android.hardware.Camera.Face;
import android.hardware.Camera.Parameters;
import android.hardware.Camera.Size;
import android.media.ExifInterface;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.MotionEvent;
import android.view.OrientationEventListener;
import android.view.ScaleGestureDetector;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.WindowManager;
import android.webkit.WebView;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.core.content.ContextCompat;

import com.projectdocupro.mobile.R;
import com.projectdocupro.mobile.interfaces.PhotoCaptureCallback;
import com.projectdocupro.mobile.managers.AppConstantsManager;
import com.projectdocupro.mobile.managers.SharedPrefsManager;
import com.projectdocupro.mobile.utility.DrawingViewCameraFocus;
import com.projectdocupro.mobile.utility.Utils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Vector;

/**
 * Erweiterung der Klasse SurfaceView zur Anzeige des Previews des Kamera-Bildes vor der Aufnahme eines Fotos
 * bzw. der Anzeige eines Previews einer Foto-Aufnahme
 *
 * @see SurfaceView
 */
public class ProjectDocuCameraPreview extends SurfaceView implements SurfaceHolder.Callback {
    private static final int BUTTON_REPEAT_ACTION_NONE = 0;
    private static final int BUTTON_REPEAT_ACTION_ZOOMIN = 1;
    private static final int BUTTON_REPEAT_ACTION_ZOOMOUT = -1;
    private final SharedPrefsManager sharedPrefsManager;

    private Context context = null;
    private SurfaceHolder mHolder = null;
    // public ProgressBar progressBar = null;
    private int photoOrientation = 0;
    private int deviceOrientation = 0;
    private int tagOrientation = 0;
    private WebView webViewForWrapper = null;
    public static ProjectDocuCameraPreview projectDocuCameraPreview = null;

    private static final int FOCUS_WAITING = 0;
    private static final int FOCUS_SUCCESS = 1;
    private static final int FOCUS_FAILED = 2;
    private static final int FOCUS_DONE = 3;
    private ScaleGestureDetector scaleGestureDetector;
    private boolean has_zoom = false;
    private int zoom_factor = 0;
    private int max_zoom_factor = 0;
    private List<Integer> zoom_ratios = null;
    private int lastZoom = 0;
    public boolean zoomLocked = false;
    private boolean has_focus_area = false;
    private int focus_success = FOCUS_DONE;
    private boolean successfully_focused = false;
    private long focus_complete_time = -1;
    private long successfully_focused_time = -1;
    private String set_flash_after_autofocus = "";
    private boolean using_face_detection = false;
    private boolean touch_was_multitouch = false;
    private int focus_screen_x = 0;
    private int focus_screen_y = 0;
    public int count_cameraAutoFocus = 0;
    private Matrix camera_to_preview_matrix = new Matrix();
    private Matrix preview_to_camera_matrix = new Matrix();
    private Camera.CameraInfo camera_info = new Camera.CameraInfo();
    private int cameraId = 0;
    private int display_orientation = 0;
    private int current_size_index = -1;
    private List<String> scene_modes = null;
    private boolean supports_face_detection = false;
    private List<Size> supported_preview_sizes = null;
    private List<Size> sizes = null;
    private Face[] faces_detected = null;
    private List<String> color_effects = null;
    private List<String> white_balances = null;
    private int current_flash_index = -1;
    private List<String> supported_flash_values = null;
    private int min_exposure = 0;
    private int max_exposure = 0;
    private List<String> exposures = null;

    private Camera camera = null;
    private boolean has_surface = false;
    private String TAG = ProjectDocuCameraPreview.class.getCanonicalName();
    private boolean listenecrSet;
    private DrawingViewCameraFocus drawingView;

    public Camera getCamera() {
        return camera;
    }

    public void setCamera(Camera camera) {
        this.camera = camera;
    }

    private boolean app_is_paused = false;

    public boolean app_is_in_capture_mode = true;
    public boolean app_is_saving_photo = false;
    private String photoName;

    private Timer zoomCheckTimer = null;
    private Timer zoomButtonRepeatTimer = null;
    private Timer zoomButtonLockTimer = null;
    private int zoomButtonRepeatAction = BUTTON_REPEAT_ACTION_NONE;
    private boolean zoomInPressed = false;
    private boolean zoomOutPressed = false;

    private LinearLayout previewFunctionButtons = null;
    private boolean showPreviewFunctionButtons = false;
    private ImageView buttonDescription = null;
    private ImageView buttonMemo = null;
    private ImageView buttonTags = null;
    public ImageView buttonPlan = null;
    private ImageView buttonTrash = null;

    public int exifwidth = 0;
    public int exifheight = 0;
    public int exiforientation = 0;
    public boolean exifhasgps = false;
    public double exifgpsx = 0.0d;
    public double exifgpsy = 0.0d;
    public boolean exifhasgpsdirection = false;
    public int exifgpsdirection = 0;
    public String exifdate = null;

    private int oldOrientation = -666;
    public static int currentDeviceOrientation = -1;
    private int countOrientationChangeCalls = 0;
    private long lastRotationTime = System.currentTimeMillis();


    private int localPhotoId = -1;

    OrientationEventListener orientationListener = null;

    private int rotateIcons = 0;
    PhotoCaptureCallback mPhotoCaptureCallback = null;

    public ProjectDocuCameraPreview(Context context) {
        this(context, null);
        this.projectDocuCameraPreview = this;
    }

    public ProjectDocuCameraPreview(Context context, PhotoCaptureCallback photoCaptureCallback) {

        this(context, null, photoCaptureCallback);
        this.projectDocuCameraPreview = this;

    }

    @SuppressWarnings("deprecation")
    ProjectDocuCameraPreview(Context context, Bundle savedInstanceState, PhotoCaptureCallback photoCaptureCallback) {
        super(context);

        this.context = context;
        sharedPrefsManager = new SharedPrefsManager(context);
        this.projectDocuCameraPreview = this;
        mPhotoCaptureCallback = photoCaptureCallback;
        mHolder = getHolder();
        mHolder.addCallback(ProjectDocuCameraPreview.this);
        mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
//        setCameraFocus(true);
        setFocusable(true);
//        setFocusableInTouchMode(true);
        requestFocus();
        scaleGestureDetector = new ScaleGestureDetector(context, new ScaleListener());
        // Fragment locked in portrait screen orientation
        ((Activity) context).setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
    }

    /**
     * Private Klasse erweitert von ScaleGestureDetector.SimpleOnScaleGestureListener zur Steuerung der Zoom-Funktion mittels Touch Gesten
     */
    private class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {
        @Override
        public boolean onScale(ScaleGestureDetector detector) {
            if (ProjectDocuCameraPreview.this.camera != null && ProjectDocuCameraPreview.this.has_zoom) {
                ProjectDocuCameraPreview.this.scaleZoom(detector.getScaleFactor());
            }
            return true;
        }
    }

    public void scaleZoom(float scale_factor) {
        //Utils.showLogger2("newZoomFactor>>>"+scale_factor);
        if (this.camera != null && this.has_zoom) {
            float zoom_ratio = this.zoom_ratios.get(zoom_factor) / 100.0f;
            zoom_ratio *= scale_factor;

            int new_zoom_factor = zoom_factor;
            if (zoom_ratio <= 1.0f) {
                new_zoom_factor = 0;
            } else if (zoom_ratio >= zoom_ratios.get(max_zoom_factor) / 100.0f) {
                new_zoom_factor = max_zoom_factor;
            } else {
                if (scale_factor > 1.0f) {
                    for (int i = zoom_factor; i < zoom_ratios.size(); i++) {
                        if (zoom_ratios.get(i) / 100.0f >= zoom_ratio) {
                            new_zoom_factor = i;
                            break;
                        }
                    }
                } else {
                    for (int i = zoom_factor; i >= 0; i--) {
                        if (zoom_ratios.get(i) / 100.0f <= zoom_ratio) {
                            new_zoom_factor = i;
                            break;
                        }
                    }
                }
            }
            zoomTo(new_zoom_factor, true);
        }
    }

    class MyGestureDetector extends SimpleOnGestureListener {
        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            try {
                final ViewConfiguration vc = ViewConfiguration.get(context);
                final float scale = getResources().getDisplayMetrics().density;
                final int swipeMinDistance = (int) (160 * scale + 0.5f);
                final int swipeThresholdVelocity = vc.getScaledMinimumFlingVelocity();
                float xdist = e1.getX() - e2.getX();
                float ydist = e1.getY() - e2.getY();
                float dist2 = xdist * xdist + ydist * ydist;
                float vel2 = velocityX * velocityX + velocityY * velocityY;
            } catch (Exception e) {
            }
            return false;
        }

        @Override
        public boolean onDown(MotionEvent e) {
            return true;
        }
    }


    public void zoomTo(int new_zoom_factor, boolean update_seek_bar) {
        zoomLocked = true;

        clearFocusAreas();

        if (new_zoom_factor < 0) {
            new_zoom_factor = 0;
        }

        if (new_zoom_factor > max_zoom_factor) {
            new_zoom_factor = max_zoom_factor;
        }

        if (new_zoom_factor != zoom_factor && camera != null) {
            Parameters parameters = camera.getParameters();

            if (parameters.isZoomSupported()) {
                zoom_factor = new_zoom_factor;

               // parameters.set(Camera.Parameters.WI, true);


                Utils.showLogger2("newZoomFactor>>"+new_zoom_factor);
                //change here
                parameters.setZoom(new_zoom_factor);
                setCameraParameters(parameters);
                camera.getParameters();
            }
        }

        zoomButtonLockTimer = new Timer();
        zoomButtonLockTimer.schedule(
                new TimerTask() {
                    @Override
                    public void run() {
                        zoomLocked = false;
                    }
                },
                175
        );
    }

    public void clearFocusAreas() {
        if (camera == null) {
            return;
        }

        cancelAutoFocus();
        Parameters parameters = camera.getParameters();

        boolean update_parameters = false;

        if (parameters.getMaxNumFocusAreas() > 0) {
            parameters.setFocusAreas(null);
            update_parameters = true;
        }

        if (parameters.getMaxNumMeteringAreas() > 0) {
            parameters.setMeteringAreas(null);
            update_parameters = true;
        }

        if (update_parameters) {
            setCameraParameters(parameters);
        }

        has_focus_area = false;
        focus_success = FOCUS_DONE;
        successfully_focused = false;
    }

    private void cancelAutoFocus() {
        if (camera != null) {
            try {
                camera.cancelAutoFocus();
            } catch (RuntimeException e) {
                e.printStackTrace();
            }
            autoFocusCompleted(false, false, true);
        }
    }

    private void autoFocusCompleted(boolean manual, boolean success, boolean cancelled) {
        if (cancelled) {
            focus_success = FOCUS_DONE;
        } else {
            focus_success = success ? FOCUS_SUCCESS : FOCUS_FAILED;
            focus_complete_time = System.currentTimeMillis();
        }

        if (manual && !cancelled && (success)) {
            successfully_focused = true;
            successfully_focused_time = focus_complete_time;
        }

//        if (set_flash_after_autofocus.length() > 0 && camera != null) {
//            Parameters parameters = camera.getParameters();
//            parameters.setFlashMode(set_flash_after_autofocus);
//            set_flash_after_autofocus = "";
//            setCameraParameters(parameters);
//        }

        if (this.using_face_detection && !cancelled) {
            if (camera != null) {
                try {
                    camera.cancelAutoFocus();
                } catch (RuntimeException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void setCameraParameters(Parameters parameters) {
        if (camera == null) {
            return;
        }

        try {
            camera.setParameters(parameters);
        } catch (RuntimeException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        this.has_surface = true;
        this.getHolder().setKeepScreenOn(true);


        // API 23 requires us to check for permissions at runtime.
        int permissionCheck = ContextCompat.checkSelfPermission(getContext(), Manifest.permission.CAMERA);
        if (permissionCheck == PackageManager.PERMISSION_GRANTED) {
            camera = Camera.open(Camera.CameraInfo.CAMERA_FACING_BACK);
            try {
                camera.setPreviewDisplay(holder);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        Parameters params = camera.getParameters();

//        if (this.getResources().getConfiguration().orientation != Configuration.ORIENTATION_LANDSCAPE)
//        {
//            params.set("orientation", "portrait");
//            camera.setDisplayOrientation(90);
//        }

        if (Integer.parseInt(Build.VERSION.SDK) >= 8)
            setDisplayOrientation(camera, 90);
        else {
            if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
                params.set("orientation", "portrait");
                params.set("rotation", 90);
            }
            if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
                params.set("orientation", "landscape");
                params.set("rotation", 90);
            }
        }

        new OrientationEventListener(context) {
            @Override
            public void onOrientationChanged(int orientation) {
                Log.d("orientation", orientation + "");
                if (context instanceof SavePictureActivity) {
//                    ((SavePictureActivity) context).onScreenOrientationChanged(orientation);
                }

            }
        }.enable();

        this.openCamera();
    }

    protected void setDisplayOrientation(Camera camera, int angle) {
        Method downPolymorphic;
        try {
            downPolymorphic = camera.getClass().getMethod("setDisplayOrientation", new Class[]{int.class});
            if (downPolymorphic != null)
                downPolymorphic.invoke(camera, new Object[]{angle});
        } catch (Exception e1) {
        }
    }

    // TODO: Maybe not used anymore ? moved to ProjectDocuMainActivity.java
    private Bitmap getRotatedBitmap(Bitmap bitmap) throws IOException {
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

    public static Bitmap rotateImage(Bitmap source, float angle) {
        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        return Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(),
                matrix, true);
    }

    public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
        if (mHolder.getSurface() == null) {
            return;
        }

        if (camera == null) {
            return;
        }
//        camera.setDisplayOrientation(90);
        Parameters parameters = camera.getParameters();

        boolean autoFocus = getContext().getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_AUTOFOCUS);

        // Get best preview resolution.
        List<Camera.Size> previewSizes = parameters.getSupportedPreviewSizes();

        Camera.Size highestRes = previewSizes.get(0);
        int highestPixels = highestRes.width * highestRes.height;

        for (Camera.Size size : previewSizes) {
            int currentPixels = size.width * size.height;
            if (currentPixels > highestPixels) {
                highestRes = size;
                highestPixels = currentPixels;
            }
        }

        parameters.setPreviewSize(highestRes.width, highestRes.height);
        parameters.setPreviewFormat(ImageFormat.NV21);

        // Set scene mode for scanning barcodes.
        if (parameters.getSupportedSceneModes() != null && parameters.getSupportedSceneModes().contains(Parameters.SCENE_MODE_AUTO)) {
            parameters.setSceneMode(Parameters.SCENE_MODE_AUTO);
        }

        camera.setParameters(parameters);
        camera.startPreview();
    }

    public void surfaceDestroyed(SurfaceHolder holder) {
        this.has_surface = false;
//        this.closeCamera();
    }

    public void closeCamera() {
        if (zoomButtonRepeatTimer != null) {
            zoomButtonRepeatTimer.cancel();
        }

        if (camera != null) {
            if (camera != null) {
                pausePreview();
                camera.release();
                camera = null;
            }
        }
//        Toast.makeText(context,"release camera",Toast.LENGTH_SHORT).show();
    }

    void pausePreview() {
        if (camera == null) {
            return;
        }

        camera.stopPreview();
    }

    private void openCamera() {
        zoomLocked = false;
//		if (isUserAndProjectDataStillValid() == false) {
//			return;
//		}

        zoomButtonRepeatTimer = new Timer();
        zoomButtonRepeatTimer.schedule(
                new TimerTask() {
                    @Override
                    public void run() {
                        if (zoomButtonRepeatAction == BUTTON_REPEAT_ACTION_ZOOMIN) {
                            zoomIn();
                        } else if (zoomButtonRepeatAction == BUTTON_REPEAT_ACTION_ZOOMOUT) {
                            zoomOut();
                        }
                    }
                },
                25,
                25
        );

        using_face_detection = false;
        scene_modes = null;
        sizes = null;
        color_effects = null;
        min_exposure = 0;
        max_exposure = 0;

        if (!this.has_surface) {
            return;
        }

        if (this.app_is_paused) {
            return;
        }

//        try {
//            camera = Camera.open();
//
//            Parameters params = camera.getParameters();
//
////			((ProjectDocuMainActivity) getContext()).cameraResolutionsList = params.getSupportedPictureSizes();
////			((ProjectDocuMainActivity) getContext()).cameraPreviewResoutionsList = params.getSupportedPreviewSizes();
//
////			ProjectDocuDatabaseManager projectDocuDatabaseManager = new ProjectDocuDatabaseManager (context);
////			String selectedCameraResolution = (String) projectDocuDatabaseManager.selectDataFromPreferences(ProjectDocuDatabaseManager.COLUMN_PREFERENCES_SELECTED_CAMERA_RESOLUTION);
//
//            // TODO: replace with function which detects the best preview-size for this display
//			/*for (int i = 0; i < ((ProjectDocuMainActivity) getContext()).cameraPreviewResoutionsList.size(); i++)
//			{
//				System.out.println("SIZE: w:"+((ProjectDocuMainActivity) getContext()).cameraPreviewResoutionsList.get(i).width + "h;"+((ProjectDocuMainActivity) getContext()).cameraPreviewResoutionsList.get(i).height);
//			}*/
//            //int wvalue = ((ProjectDocuMainActivity) getContext()).cameraPreviewResoutionsList.get(0).width;
//            //int hvalue = ((ProjectDocuMainActivity) getContext()).cameraPreviewResoutionsList.get(0).height;
//            String selectedCameraResolution="4048 x 3036";
//			if (selectedCameraResolution != null) {
//				try {
//					String [] sizeArray = selectedCameraResolution.split(" x ");
//					params.setPictureSize(Integer.valueOf(sizeArray[0]), Integer.valueOf(sizeArray[1]));
//					//params.setPreviewSize(wvalue,hvalue);
//					camera.setParameters(params);
//				} catch (Exception e) {}
//			}
//        } catch (Exception e) {
//            e.printStackTrace();
//            camera = null;
//        }

        if (camera != null) {
//            try {
//                camera.setPreviewDisplay(mHolder);
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//
            Activity activity = (Activity) this.getContext();
//
            new OrientationEventListener(activity) {
                @Override
                public void onOrientationChanged(int orientation) {
                    ProjectDocuCameraPreview.this.onOrientationChanged(orientation);
                }
            }.enable();

            setupCamera();
        }
    }

    @SuppressWarnings("deprecation")
    private void onOrientationChanged(int orientation) {
        if (orientation == OrientationEventListener.ORIENTATION_UNKNOWN) {
            return;
        }

        if (camera == null) {
            return;
        }

        orientation = (orientation + 45) / 90 * 90;
        if (orientation >= 360) {
            orientation = 0;
        }

        deviceOrientation = orientation;

        WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        Configuration config = getResources().getConfiguration();

        int rotation = windowManager.getDefaultDisplay().getRotation();


        if (rotation == Surface.ROTATION_0) {
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


        if (((rotation == Surface.ROTATION_0 || rotation == Surface.ROTATION_180) && config.orientation == Configuration.ORIENTATION_LANDSCAPE)
                || ((rotation == Surface.ROTATION_90 || rotation == Surface.ROTATION_270) && config.orientation == Configuration.ORIENTATION_PORTRAIT)) {
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

        photoOrientation = orientation;

        if (orientation == oldOrientation && System.currentTimeMillis() - lastRotationTime > 3000) {
            return;
        }

        oldOrientation = orientation;

//    	ImageView buttonPlan = (ImageView) ((Activity) context).findViewById(R.id.button_plan);
//    	FrameLayout.LayoutParams layoutParamsButtonPlan = (FrameLayout.LayoutParams) buttonPlan.getLayoutParams();
//
//    	ImageView buttonTrash = (ImageView) ((Activity) context).findViewById(R.id.button_trash);
//    	FrameLayout.LayoutParams layoutParamsButtonTrash = (FrameLayout.LayoutParams) buttonTrash.getLayoutParams();
//
//        LinearLayout layout_functionbuttons = (LinearLayout) ((Activity) context).findViewById(R.id.preview_function_buttons);
//
//        FrameLayout.LayoutParams layoutParamsFunctionButtons = (FrameLayout.LayoutParams) layout_functionbuttons.getLayoutParams();
//        ImageView buttonDescription = (ImageView) ((Activity) context).findViewById(R.id.button_description);
//        ImageView buttonMemo 		= (ImageView) ((Activity) context).findViewById(R.id.button_memo);
//        ImageView buttonTags 		= (ImageView) ((Activity) context).findViewById(R.id.button_tags);
//
//	    ImageView captureButton = (ImageView) ((Activity) context).findViewById(R.id.capture_button);
//        FrameLayout.LayoutParams layoutParamsCaptureButton=new FrameLayout.LayoutParams(captureButton.getWidth(),captureButton.getHeight());
//        layoutParamsCaptureButton.setMargins(60,60,60,60);
//
//    	ImageView buttonFlash = (ImageView) ((Activity) context).findViewById(R.id.button_flash);
//        FrameLayout.LayoutParams layoutParamsFlashButton=new FrameLayout.LayoutParams(buttonFlash.getWidth(),buttonFlash.getHeight());
//        layoutParamsFlashButton.setMargins(60,60,60,60);
//
//        SeekBar seekbarZoom = (SeekBar) ((Activity) context).findViewById(R.id.zoom_seekbar);
//        FrameLayout.LayoutParams layoutParamsSeekbar=new FrameLayout.LayoutParams(seekbarZoom.getWidth(),seekbarZoom.getHeight());
//        layoutParamsSeekbar.setMargins(60,60,60,60);
//
//        LinearLayout layout_zoombuttons = (LinearLayout) ((Activity) context).findViewById(R.id.layout_zoombuttons);
//	    ImageView zoomButtonIn = (ImageView) ((Activity) context).findViewById(R.id.zoom_in_button);
//	    ImageView zoomButtonOut = (ImageView) ((Activity) context).findViewById(R.id.zoom_out_button);
//        FrameLayout.LayoutParams layoutParamsZoomButtons = (FrameLayout.LayoutParams) layout_zoombuttons.getLayoutParams();
//
//        try {
//			WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
//			Display display = wm.getDefaultDisplay();
//
//		    if (((display.getOrientation() == Surface.ROTATION_0 || display.getOrientation() == Surface.ROTATION_180) && config.orientation == Configuration.ORIENTATION_LANDSCAPE)
//		        || ((display.getOrientation() == Surface.ROTATION_90 || display.getOrientation() == Surface.ROTATION_270) && config.orientation == Configuration.ORIENTATION_PORTRAIT))
//		    {
//		        if (orientation == 0  || orientation == 360) {
//		        	((ProjectDocuMainActivity) getContext()).setMenuBarOrientation(LinearLayout.VERTICAL,0);
//
//		        	projectDocuTagOverlay.rotateTagsView(0.0f,LinearLayout.HORIZONTAL);
//
//		        	layout_functionbuttons.setOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
//		        	layout_functionbuttons.setRotation(0.0f);
//		        	layoutParamsFunctionButtons.gravity = Gravity.TOP|Gravity.LEFT;
//		        	buttonDescription.setRotation(0.0f);
//		        	buttonMemo.setRotation(0.0f);
//		        	buttonTags.setRotation(0.0f);
//		        	layoutParamsFunctionButtons.setMargins(10,10, 10, 10);
//
//		        	layoutParamsCaptureButton.gravity=Gravity.RIGHT|Gravity.CENTER_VERTICAL;
//		        	captureButton.setLayoutParams(layoutParamsCaptureButton);
//		        	captureButton.setRotation(0.0f);
//
//		        	layoutParamsButtonPlan.gravity = Gravity.TOP|Gravity.RIGHT;
//		        	layoutParamsButtonPlan.setMargins(20,20,20,20);
//		        	buttonPlan.setRotation(0.0f);
//
//		        	layoutParamsButtonTrash.gravity = Gravity.BOTTOM|Gravity.RIGHT;
//		        	layoutParamsButtonTrash.setMargins(20,20,20,20);
//		        	buttonTrash.setRotation(0.0f);
//
//		        	layoutParamsFlashButton.gravity=Gravity.TOP|Gravity.LEFT;
//		        	buttonFlash.setLayoutParams(layoutParamsFlashButton);
//		        	buttonFlash.setRotation(0.0f);
//
//		        	layoutParamsSeekbar.gravity=Gravity.BOTTOM|Gravity.RIGHT;
//		        	seekbarZoom.setLayoutParams(layoutParamsSeekbar);
//		        	seekbarZoom.setRotation(0.0f);
//
//		        	layout_zoombuttons.setRotation(0.0f);
//		        	layoutParamsZoomButtons.gravity=Gravity.BOTTOM|Gravity.RIGHT;
//		        	layout_zoombuttons.setOrientation(LinearLayout.VERTICAL);
//		        	layoutParamsZoomButtons.setMargins(10,10,65,10);
//		        	zoomButtonIn.setRotation(0.0f);
//		        	zoomButtonOut.setRotation(0.0f);
//		        }
//		        else if (orientation == 90) {
//		        	((ProjectDocuMainActivity) getContext()).setMenuBarOrientation(LinearLayout.VERTICAL,-90);
//
//		        	projectDocuTagOverlay.rotateTagsView(270.0f,LinearLayout.VERTICAL);
//
//		        	layout_functionbuttons.setOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
//		        	layout_functionbuttons.setRotation(0.0f);
//		        	// layout_functionbuttons.setGravity(Gravity.BOTTOM|Gravity.LEFT);
//		        	layoutParamsFunctionButtons.gravity = Gravity.BOTTOM|Gravity.LEFT;
//		        	layoutParamsFunctionButtons.setMargins(50, 10, 10, 10);
//		        	buttonDescription.setRotation(270.0f);
//		        	buttonMemo.setRotation(270.0f);
//		        	buttonTags.setRotation(270.0f);
//
//		        	layoutParamsButtonPlan.gravity = Gravity.TOP|Gravity.LEFT;
//		        	buttonPlan.setRotation(270.0f);
//		        	layoutParamsButtonPlan.setMargins(20,20,20,20);
//
//		        	layoutParamsButtonTrash.gravity = Gravity.TOP|Gravity.RIGHT;
//		        	layoutParamsButtonTrash.setMargins(20,20,20,20);
//		        	buttonTrash.setRotation(270.0f);
//
//		        	layoutParamsCaptureButton.gravity=Gravity.RIGHT|Gravity.CENTER_VERTICAL;
//		        	captureButton.setLayoutParams(layoutParamsCaptureButton);
//		        	// captureButton.setRotation(90.0f);
//		        	captureButton.setRotation(270.0f);
//
//		        	layoutParamsFlashButton.gravity=Gravity.BOTTOM|Gravity.LEFT;
//		        	buttonFlash.setLayoutParams(layoutParamsFlashButton);
//		        	buttonFlash.setRotation(270.0f);
//
//		        	layoutParamsSeekbar.gravity=Gravity.TOP|Gravity.RIGHT;
//		        	seekbarZoom.setLayoutParams(layoutParamsSeekbar);
//		        	seekbarZoom.setRotation(0.0f);
//
//		        	layout_zoombuttons.setRotation(0.0f);
//		        	layoutParamsZoomButtons.gravity=Gravity.TOP|Gravity.RIGHT;
//		        	layout_zoombuttons.setOrientation(LinearLayout.HORIZONTAL);
//		        	layoutParamsZoomButtons.setMargins(10,10,50,10);
//		        	zoomButtonIn.setRotation(270.0f);
//		        	zoomButtonOut.setRotation(270.0f);
//		        }
//		        else if (orientation == 180) {
//		        	((ProjectDocuMainActivity) getContext()).setMenuBarOrientation(LinearLayout.VERTICAL,180);
//
//		        	projectDocuTagOverlay.rotateTagsView(180.0f,LinearLayout.HORIZONTAL);
//
//		        	layout_functionbuttons.setOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
//		        	layout_functionbuttons.setRotation(180.0f);
//		        	// layout_functionbuttons.setGravity(Gravity.BOTTOM|Gravity.RIGHT);
//		        	layoutParamsFunctionButtons.gravity = Gravity.BOTTOM|Gravity.RIGHT;
//		        	layoutParamsFunctionButtons.setMargins(10, 10, 10, 10);
//		        	buttonDescription.setRotation(0.0f);
//		        	buttonMemo.setRotation(0.0f);
//		        	buttonTags.setRotation(0.0f);
//
//		        	layoutParamsButtonPlan.gravity = Gravity.BOTTOM|Gravity.LEFT;
//		        	layoutParamsButtonPlan.setMargins(50,20,20,20);
//		        	buttonPlan.setRotation(180.0f);
//
//		        	layoutParamsButtonTrash.gravity = Gravity.TOP|Gravity.LEFT;
//		        	layoutParamsButtonTrash.setMargins(50,20,20,20);
//		        	buttonTrash.setRotation(180.0f);
//
//		        	layoutParamsCaptureButton.gravity=Gravity.LEFT|Gravity.CENTER_VERTICAL;
//		        	captureButton.setLayoutParams(layoutParamsCaptureButton);
//		        	captureButton.setRotation(180.0f);
//
//		        	layoutParamsFlashButton.gravity=Gravity.BOTTOM|Gravity.RIGHT;
//		        	buttonFlash.setLayoutParams(layoutParamsFlashButton);
//		        	buttonFlash.setRotation(180.0f);
//
//		        	layoutParamsSeekbar.gravity=Gravity.TOP|Gravity.LEFT;
//		        	seekbarZoom.setLayoutParams(layoutParamsSeekbar);
//		        	seekbarZoom.setRotation(180.0f);
//
//		        	layout_zoombuttons.setRotation(180.0f);
//		        	layoutParamsZoomButtons.gravity=Gravity.TOP|Gravity.LEFT;
//		        	layout_zoombuttons.setOrientation(LinearLayout.VERTICAL);
//		        	layoutParamsZoomButtons.setMargins(65,10,10,10);
//		        	zoomButtonIn.setRotation(0.0f);
//		        	zoomButtonOut.setRotation(0.0f);
//		        }
//		        else if (orientation == 270) {
//		        	((ProjectDocuMainActivity) getContext()).setMenuBarOrientation(LinearLayout.VERTICAL,90);
//
//		        	projectDocuTagOverlay.rotateTagsView(90.0f,LinearLayout.VERTICAL);
//
//		        	layout_functionbuttons.setOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
//		        	layout_functionbuttons.setRotation(180.0f);
//		        	// layout_functionbuttons.setGravity(Gravity.TOP|Gravity.RIGHT);
//		        	layoutParamsFunctionButtons.gravity = Gravity.TOP|Gravity.RIGHT;
//		        	layoutParamsFunctionButtons.setMargins(10, 10, 10, 10);
//		        	buttonDescription.setRotation(270.0f);
//		        	buttonMemo.setRotation(270.0f);
//		        	buttonTags.setRotation(270.0f);
//
//		        	layoutParamsButtonPlan.gravity = Gravity.BOTTOM|Gravity.RIGHT;
//		        	buttonPlan.setRotation(90.0f);
//		        	layoutParamsButtonPlan.setMargins(20,20,20,20);
//
//		        	layoutParamsButtonTrash.gravity = Gravity.BOTTOM|Gravity.LEFT;
//		        	layoutParamsButtonTrash.setMargins(80,20,20,20);
//		        	buttonTrash.setRotation(90.0f);
//
//		        	layoutParamsCaptureButton.gravity=Gravity.LEFT|Gravity.CENTER_VERTICAL;
//		        	captureButton.setLayoutParams(layoutParamsCaptureButton);
//		        	captureButton.setRotation(270.0f);
//
//		        	layoutParamsFlashButton.gravity=Gravity.TOP|Gravity.RIGHT;
//		        	buttonFlash.setLayoutParams(layoutParamsFlashButton);
//		        	buttonFlash.setRotation(90.0f);
//
//		        	layoutParamsSeekbar.gravity=Gravity.BOTTOM|Gravity.LEFT;
//		        	seekbarZoom.setLayoutParams(layoutParamsSeekbar);
//		        	seekbarZoom.setRotation(180.0f);
//
//		        	layout_zoombuttons.setRotation(180.0f);
//		        	layout_zoombuttons.setOrientation(LinearLayout.HORIZONTAL);
//		        	layoutParamsZoomButtons.gravity=Gravity.BOTTOM|Gravity.LEFT;
//		        	layoutParamsZoomButtons.setMargins(50,10,50,10);
//		        	zoomButtonIn.setRotation(270.0f);
//		        	zoomButtonOut.setRotation(270.0f);
//		        }
//
//			} else {
//		        if (orientation == 0  || orientation == 360) {
//		        	((ProjectDocuMainActivity) getContext()).setMenuBarOrientation(LinearLayout.VERTICAL,-90);
//
//		        	projectDocuTagOverlay.rotateTagsView(270.0f,LinearLayout.VERTICAL);
//
//		        	layout_functionbuttons.setOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
//		        	layout_functionbuttons.setRotation(0.0f);
//		        	layoutParamsFunctionButtons.gravity = Gravity.BOTTOM|Gravity.LEFT;
//		        	buttonDescription.setRotation(270.0f);
//		        	buttonMemo.setRotation(270.0f);
//		        	buttonTags.setRotation(270.0f);
//		        	layoutParamsFunctionButtons.setMargins(50, 10, 10, 10);
//
//		        	layoutParamsButtonPlan.gravity = Gravity.TOP|Gravity.LEFT;
//		        	layoutParamsButtonPlan.setMargins(20,20,20,20);
//		        	buttonPlan.setRotation(270.0f);
//
//		        	layoutParamsButtonTrash.gravity = Gravity.TOP|Gravity.RIGHT;
//		        	layoutParamsButtonTrash.setMargins(20,20,20,20);
//		        	buttonTrash.setRotation(270.0f);
//
//		        	layoutParamsCaptureButton.gravity=Gravity.RIGHT|Gravity.CENTER_VERTICAL;
//		        	captureButton.setLayoutParams(layoutParamsCaptureButton);
//		        	// captureButton.setRotation(0.0f);
//		        	captureButton.setRotation(270.0f);
//		        	// captureButton.setRotation(180.0f);
//
//		        	layoutParamsFlashButton.gravity=Gravity.BOTTOM|Gravity.LEFT;
//		        	buttonFlash.setLayoutParams(layoutParamsFlashButton);
//		        	buttonFlash.setRotation(270.0f);
//
//		        	layoutParamsSeekbar.gravity=Gravity.TOP|Gravity.RIGHT;
//		        	seekbarZoom.setLayoutParams(layoutParamsSeekbar);
//		        	seekbarZoom.setRotation(0.0f);
//
//		        	layout_zoombuttons.setRotation(0.0f);
//		        	layoutParamsZoomButtons.gravity=Gravity.TOP|Gravity.RIGHT;
//		        	layout_zoombuttons.setOrientation(LinearLayout.HORIZONTAL);
//		        	layoutParamsZoomButtons.setMargins(10,65,10,10);
//		        	zoomButtonIn.setRotation(270.0f);
//		        	zoomButtonOut.setRotation(270.0f);
//		        }
//		        else if (orientation == 90) {
//		        	((ProjectDocuMainActivity) getContext()).setMenuBarOrientation(LinearLayout.VERTICAL,180);
//
//		        	projectDocuTagOverlay.rotateTagsView(180.0f,LinearLayout.HORIZONTAL);
//
//		        	layout_functionbuttons.setOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
//		        	layout_functionbuttons.setRotation(180.0f);
//		        	layoutParamsFunctionButtons.gravity = Gravity.BOTTOM|Gravity.RIGHT;
//		        	buttonDescription.setRotation(0.0f);
//		        	buttonMemo.setRotation(0.0f);
//		        	buttonTags.setRotation(0.0f);
//		        	layoutParamsFunctionButtons.setMargins(10, 10, 10, 10);
//
//		        	layoutParamsButtonPlan.gravity = Gravity.BOTTOM|Gravity.LEFT;
//		        	buttonPlan.setRotation(180.0f);
//		        	layoutParamsButtonPlan.setMargins(70,20,20,20);
//
//		        	layoutParamsButtonTrash.gravity = Gravity.TOP|Gravity.LEFT;
//		        	layoutParamsButtonTrash.setMargins(70,20,20,20);
//		        	buttonTrash.setRotation(180.0f);
//
//		        	layoutParamsCaptureButton.gravity=Gravity.LEFT|Gravity.CENTER_VERTICAL;
//		        	captureButton.setLayoutParams(layoutParamsCaptureButton);
//		        	// captureButton.setRotation(90.0f);
//		        	// captureButton.setRotation(270.0f);
//		        	captureButton.setRotation(180.0f);
//
//		        	layoutParamsFlashButton.gravity=Gravity.BOTTOM|Gravity.RIGHT;
//		        	buttonFlash.setLayoutParams(layoutParamsFlashButton);
//		        	buttonFlash.setRotation(180.0f);
//
//		        	layoutParamsSeekbar.gravity=Gravity.TOP|Gravity.LEFT;
//		        	seekbarZoom.setLayoutParams(layoutParamsSeekbar);
//		        	seekbarZoom.setRotation(180.0f);
//
//		        	layout_zoombuttons.setRotation(180.0f);
//		        	layoutParamsZoomButtons.gravity=Gravity.TOP|Gravity.LEFT;
//		        	layout_zoombuttons.setOrientation(LinearLayout.VERTICAL);
//		        	layoutParamsZoomButtons.setMargins(65,10,65,10);
//		        	zoomButtonIn.setRotation(0.0f);
//		        	zoomButtonOut.setRotation(0.0f);
//		        }
//		        else if (orientation == 180) {
//		        	((ProjectDocuMainActivity) getContext()).setMenuBarOrientation(LinearLayout.VERTICAL,90);
//
//		        	projectDocuTagOverlay.rotateTagsView(90.0f,LinearLayout.VERTICAL);
//
//		        	layout_functionbuttons.setOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
//		        	layout_functionbuttons.setRotation(180.0f);
//		        	layoutParamsFunctionButtons.gravity = Gravity.TOP|Gravity.RIGHT;
//		        	buttonDescription.setRotation(270.0f);
//		        	buttonMemo.setRotation(270.0f);
//		        	buttonTags.setRotation(270.0f);
//		        	layoutParamsFunctionButtons.setMargins(10, 10, 10, 10);
//
//		        	layoutParamsButtonPlan.gravity = Gravity.BOTTOM|Gravity.RIGHT;
//		        	buttonPlan.setRotation(90.0f);
//		        	layoutParamsButtonPlan.setMargins(20,20,20,20);
//
//		        	layoutParamsButtonTrash.gravity = Gravity.BOTTOM|Gravity.LEFT;
//		        	layoutParamsButtonTrash.setMargins(80,20,20,20);
//		        	buttonTrash.setRotation(90.0f);
//
//		        	layoutParamsCaptureButton.gravity=Gravity.LEFT|Gravity.CENTER_VERTICAL;
//		        	captureButton.setLayoutParams(layoutParamsCaptureButton);
//		        	// captureButton.setRotation(0.0f);
//		        	captureButton.setRotation(90.0f);
//
//		        	layoutParamsFlashButton.gravity=Gravity.TOP|Gravity.RIGHT;
//		        	buttonFlash.setLayoutParams(layoutParamsFlashButton);
//		        	buttonFlash.setRotation(90.0f);
//
//		        	layoutParamsSeekbar.gravity=Gravity.BOTTOM|Gravity.LEFT;
//		        	seekbarZoom.setLayoutParams(layoutParamsSeekbar);
//		        	seekbarZoom.setRotation(180.0f);
//
//		        	layout_zoombuttons.setRotation(180.0f);
//		        	layoutParamsZoomButtons.gravity=Gravity.BOTTOM|Gravity.LEFT;
//		        	layout_zoombuttons.setOrientation(LinearLayout.HORIZONTAL);
//		        	layoutParamsZoomButtons.setMargins(65,50,65,10);
//		        	zoomButtonIn.setRotation(270.0f);
//		        	zoomButtonOut.setRotation(270.0f);
//		        }
//		        else if (orientation == 270) {
//		        	((ProjectDocuMainActivity) getContext()).setMenuBarOrientation(LinearLayout.VERTICAL,0);
//
//		        	projectDocuTagOverlay.rotateTagsView(0.0f,LinearLayout.HORIZONTAL);
//
//		        	layout_functionbuttons.setOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
//		        	layout_functionbuttons.setRotation(0.0f);
//		        	layoutParamsFunctionButtons.gravity = Gravity.TOP|Gravity.LEFT;
//		        	buttonDescription.setRotation(0.0f);
//		        	buttonMemo.setRotation(0.0f);
//		        	buttonTags.setRotation(0.0f);
//		        	layoutParamsFunctionButtons.setMargins(10, 10, 00, 10);
//
//		        	layoutParamsButtonPlan.gravity = Gravity.TOP|Gravity.RIGHT;
//		        	buttonPlan.setRotation(0.0f);
//		        	layoutParamsButtonPlan.setMargins(20,20,20,20);
//
//		        	layoutParamsButtonTrash.gravity = Gravity.BOTTOM|Gravity.RIGHT;
//		        	layoutParamsButtonTrash.setMargins(20,20,20,20);
//		        	buttonTrash.setRotation(0.0f);
//
//		        	layoutParamsCaptureButton.gravity=Gravity.RIGHT|Gravity.CENTER_VERTICAL;
//		        	captureButton.setLayoutParams(layoutParamsCaptureButton);
//		        	// captureButton.setRotation(90.0f);
//		        	captureButton.setRotation(0.0f);
//
//		        	layoutParamsFlashButton.gravity=Gravity.TOP|Gravity.LEFT;
//		        	buttonFlash.setLayoutParams(layoutParamsFlashButton);
//		        	buttonFlash.setRotation(0.0f);
//
//		        	layoutParamsSeekbar.gravity=Gravity.BOTTOM|Gravity.RIGHT;
//		        	seekbarZoom.setLayoutParams(layoutParamsSeekbar);
//		        	seekbarZoom.setRotation(0.0f);
//
//		        	layout_zoombuttons.setRotation(0.0f);
//		        	layoutParamsZoomButtons.gravity=Gravity.BOTTOM|Gravity.RIGHT;
//		        	layout_zoombuttons.setOrientation(LinearLayout.VERTICAL);
//		        	layoutParamsZoomButtons.setMargins(10,10,65,10);
//		        	zoomButtonIn.setRotation(0.0f);
//		        	zoomButtonOut.setRotation(0.0f);
//
//		        }
//		        else {
//		        	((ProjectDocuMainActivity) getContext()).setMenuBarOrientation(LinearLayout.VERTICAL,0);
//
//		        	projectDocuTagOverlay.rotateTagsView(0.0f,LinearLayout.HORIZONTAL);
//
//		        	layout_functionbuttons.setOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
//		        	layout_functionbuttons.setRotation(0.0f);
//		        	layoutParamsFunctionButtons.gravity = Gravity.TOP|Gravity.LEFT;
//		        	buttonDescription.setRotation(0.0f);
//		        	buttonMemo.setRotation(0.0f);
//		        	buttonTags.setRotation(0.0f);
//		        	layoutParamsFunctionButtons.setMargins(10,10, 10, 10);
//
//		        	layoutParamsCaptureButton.gravity=Gravity.RIGHT|Gravity.CENTER_VERTICAL;
//		        	captureButton.setLayoutParams(layoutParamsCaptureButton);
//		        	captureButton.setRotation(0.0f);
//
//		        	layoutParamsButtonPlan.gravity = Gravity.TOP|Gravity.RIGHT;
//		        	layoutParamsButtonPlan.setMargins(20,20,20,20);
//		        	buttonPlan.setRotation(0.0f);
//
//		        	layoutParamsButtonTrash.gravity = Gravity.BOTTOM|Gravity.RIGHT;
//		        	layoutParamsButtonTrash.setMargins(20,20,20,20);
//		        	buttonTrash.setRotation(0.0f);
//
//		        	layoutParamsFlashButton.gravity=Gravity.TOP|Gravity.LEFT;
//		        	buttonFlash.setLayoutParams(layoutParamsFlashButton);
//		        	buttonFlash.setRotation(0.0f);
//
//		        	layoutParamsSeekbar.gravity=Gravity.BOTTOM|Gravity.RIGHT;
//		        	seekbarZoom.setLayoutParams(layoutParamsSeekbar);
//		        	seekbarZoom.setRotation(0.0f);
//
//		        	layout_zoombuttons.setRotation(0.0f);
//		        	layoutParamsZoomButtons.gravity=Gravity.BOTTOM|Gravity.RIGHT;
//		        	layout_zoombuttons.setOrientation(LinearLayout.VERTICAL);
//		        	layoutParamsZoomButtons.setMargins(10,10,65,10);
//		        	zoomButtonIn.setRotation(0.0f);
//		        	zoomButtonOut.setRotation(0.0f);
//		        }
//			}
//        } catch(Exception e){}
    }

    void setupCamera() {
        if (camera == null) {
            return;
        }

        camera.setZoomChangeListener(new Camera.OnZoomChangeListener() {
            @Override
            public void onZoomChange(int zoomValue, boolean stopped, Camera camera) {
            }
        });

        setupCameraParameters();

//        startCameraPreview();
    }

    void resetTakePictureStatus() {
        //progressBar.setVisibility(View.INVISIBLE);

        app_is_in_capture_mode = true;
        app_is_saving_photo = false;
    }

    public void takePicturePressed() {
        currentDeviceOrientation = deviceOrientation;

        if (camera == null) {
            resetTakePictureStatus();
            return;
        }

        if (!this.has_surface) {
            resetTakePictureStatus();
            return;
        }

        takePicture();
    }

    public static String getSceneModePreferenceKey() {
        return "preference_scene_mode";
    }

    private String setupValuesPref(List<String> values, String key, String default_value) {
        if (values != null && values.size() > 0) {
            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this.getContext());

            String value = sharedPreferences.getString(key, default_value);

            if (!values.contains(value)) {
                if (values.contains(default_value)) {
                    value = default_value;
                } else {
                    value = values.get(0);
                }
            }

            SharedPreferences.Editor editor = sharedPreferences.edit();

            editor.putString(key, value);
            editor.apply();

            return value;
        } else {
            return null;
        }
    }


    public void setFlashStatus(ImageView buttonFlash) {
        this.has_surface = true;
//        this.getHolder().setKeepScreenOn(true);
//        this.openCamera();


        if (camera != null && buttonFlash != null) {
            final Parameters parameters = camera.getParameters();

            if (context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH)) {
//                buttonFlash.performClick();
//                buttonFlash.setOnClickListener(
//                        new OnClickListener() {
//                            @Override
//                            public void onClick(View v) {
                List<String> supported_flash_modes = parameters.getSupportedFlashModes();
                String flash_mode = sharedPrefsManager.getStringValue(AppConstantsManager.SELECTED_FLASH_MODE, Parameters.FLASH_MODE_OFF);

                if (flash_mode == null || flash_mode.equals(""))
                    flash_mode = parameters.getFlashMode();

                if (flash_mode.contains(Parameters.FLASH_MODE_OFF)) {
                    parameters.setFlashMode(Parameters.FLASH_MODE_AUTO);
                    buttonFlash.setImageResource(R.drawable.flash_auto);
                    sharedPrefsManager.setStringValue(AppConstantsManager.SELECTED_FLASH_MODE, Parameters.FLASH_MODE_AUTO);
                } else if ((flash_mode.contains(Parameters.FLASH_MODE_AUTO))) {
                    parameters.setFlashMode(Parameters.FLASH_MODE_ON);
                    buttonFlash.setImageResource(R.drawable.flash_on);
                    sharedPrefsManager.setStringValue(AppConstantsManager.SELECTED_FLASH_MODE, Parameters.FLASH_MODE_ON);

                } else if ((flash_mode.contains(Parameters.FLASH_MODE_ON))) {
                    parameters.setFlashMode(Parameters.FLASH_MODE_OFF);
                    buttonFlash.setImageResource(R.drawable.flash_off);
                    sharedPrefsManager.setStringValue(AppConstantsManager.SELECTED_FLASH_MODE, Parameters.FLASH_MODE_OFF);

                }


            }
            try {
                camera.cancelAutoFocus();
                camera.setParameters(parameters);
                tryAutoFocus(false, true);
            } catch (Exception e) {
            }


//                            }
//                        }
//                );
        } else {
            buttonFlash.setVisibility(View.INVISIBLE);
        }

    }


    private void setupCameraParameters() {
        long debug_time = 0;

        Activity activity = (Activity) this.getContext();

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(activity);

        Parameters parameters = camera.getParameters();

//        parameters.setFlashMode(sharedPrefsManager.getStringValue(AppConstantsManager.SELECTED_FLASH_MODE,Parameters.FLASH_MODE_OFF));
//        setCameraParameters(parameters);
        scene_modes = parameters.getSupportedSceneModes();

        String scene_mode = setupValuesPref(scene_modes, getSceneModePreferenceKey(), Parameters.SCENE_MODE_AUTO);
        if (scene_mode != null && !parameters.getSceneMode().equals(scene_mode)) {
            parameters.setSceneMode(scene_mode);
            setCameraParameters(parameters);
        }

        List<String> supported_flash_modes = null;
        List<String> supported_focus_modes = null;

        {
            Parameters parameters2 = camera.getParameters();

            this.has_zoom = parameters2.isZoomSupported();

            if (this.has_zoom) {
                try {
                    this.max_zoom_factor = parameters2.getMaxZoom();
                    this.zoom_ratios = parameters2.getZoomRatios();
                } catch (NumberFormatException e) {
                    e.printStackTrace();
                    this.has_zoom = false;
                    this.zoom_ratios = null;
                }
            }

            this.supports_face_detection = parameters2.getMaxNumDetectedFaces() > 0;

            sizes = parameters2.getSupportedPictureSizes();

            supported_flash_modes = parameters2.getSupportedFlashModes();

            supported_focus_modes = parameters2.getSupportedFocusModes();
        }

//		{
//		    SeekBar zoomSeekBar = (SeekBar) activity.findViewById(R.id.zoom_seekbar);
//
//			if( this.has_zoom ) {
//				zoomSeekBar.setMax(max_zoom_factor);
//				zoomSeekBar.setProgress(max_zoom_factor-zoom_factor);
//				zoomSeekBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
//					@Override
//					public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
//						zoomTo(max_zoom_factor-progress, false);
//					}
//
//					@Override
//					public void onStartTrackingTouch(SeekBar seekBar) {
//					}
//
//					@Override
//					public void onStopTrackingTouch(SeekBar seekBar) {
//					}
//				});
//			}
//		}

        {
            this.faces_detected = null;

            if (this.supports_face_detection) {
                this.using_face_detection = sharedPreferences.getBoolean("preference_face_detection", false);
            } else {
                this.using_face_detection = false;
            }

            if (this.using_face_detection) {
                class MyFaceDetectionListener implements Camera.FaceDetectionListener {
                    @Override
                    public void onFaceDetection(Face[] faces, Camera camera) {
                        faces_detected = new Face[faces.length];
                        System.arraycopy(faces, 0, faces_detected, 0, faces.length);
                    }
                }

                camera.setFaceDetectionListener(new MyFaceDetectionListener());
            }
        }

        {
            Parameters parameters2 = camera.getParameters();

            color_effects = parameters2.getSupportedColorEffects();

            String color_effect = setupValuesPref(color_effects, getColorEffectPreferenceKey(), Parameters.EFFECT_NONE);

            if (color_effect != null) {
                parameters2.setColorEffect(color_effect);
                setCameraParameters(parameters2);
            }
        }

        {
            Parameters parameters2 = camera.getParameters();

            white_balances = parameters2.getSupportedWhiteBalance();

            String white_balance = setupValuesPref(white_balances, getWhiteBalancePreferenceKey(), Parameters.WHITE_BALANCE_AUTO);

            if (white_balance != null) {
                parameters2.setWhiteBalance(white_balance);
                setCameraParameters(parameters2);
            }
        }

        {
            Parameters parameters2 = camera.getParameters();

            min_exposure = parameters2.getMinExposureCompensation();
            max_exposure = parameters2.getMaxExposureCompensation();

            if (min_exposure != 0 || max_exposure != 0) {
                exposures = new Vector<String>();

                for (int i = min_exposure; i <= max_exposure; i++) {
                    exposures.add("" + i);
                }

                String exposure_s = setupValuesPref(exposures, getExposurePreferenceKey(), "0");

                if (exposure_s != null) {
                    try {
                        int exposure = Integer.parseInt(exposure_s);
                        parameters2.setExposureCompensation(exposure);
                        setCameraParameters(parameters2);
                    } catch (NumberFormatException exception) {
                    }
                }
            }
        }

//        {
//            current_size_index = -1;
//
//            String resolution_value = sharedPreferences.getString(getResolutionPreferenceKey(cameraId), "");
//
//            if (resolution_value.length() > 0) {
//                int index = resolution_value.indexOf(' ');
//
//                if (index != -1) {
//                    String resolution_w_s = resolution_value.substring(0, index);
//                    String resolution_h_s = resolution_value.substring(index + 1);
//
//                    try {
//                        int resolution_w = Integer.parseInt(resolution_w_s);
//                        int resolution_h = Integer.parseInt(resolution_h_s);
//
//                        for (int i = 0; i < sizes.size() && current_size_index == -1; i++) {
//                            Size size = sizes.get(i);
//
//                            if (size.width == resolution_w && size.height == resolution_h) {
//                                current_size_index = i;
//                            }
//                        }
//
//                        if (current_size_index == -1) {
//                        }
//                    } catch (NumberFormatException exception) {
//                    }
//                }
//            }
//
//            if (current_size_index == -1) {
//                Size current_size = null;
//
//                for (int i = 0; i < sizes.size(); i++) {
//                    Size size = sizes.get(i);
//
//                    if (current_size == null || size.width * size.height > current_size.width * current_size.height) {
//                        current_size_index = i;
//                        current_size = size;
//                    }
//                }
//            }
//
//            if (current_size_index != -1) {
//                Size current_size = sizes.get(current_size_index);
//
//                resolution_value = current_size.width + " " + current_size.height;
//
//                SharedPreferences.Editor editor = sharedPreferences.edit();
//
//                editor.putString(getResolutionPreferenceKey(cameraId), resolution_value);
//                editor.apply();
//            }
//        }

//        {
//            Parameters parameters2 = camera.getParameters();
//
//            int image_quality = getImageQuality();
//
//            parameters2.setJpegQuality(image_quality);
//
//            setCameraParameters(parameters2);
//        }

    }

//    public void switchFlash(ImageView button_flash) {
//        if (camera != null) {
//            final Parameters parameters = camera.getParameters();
//
//
//            if (context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH)) {
//                if (parameters.getFlashMode().equals(Parameters.FLASH_MODE_ON) || parameters.getFlashMode().equals(Parameters.FLASH_MODE_AUTO)) {
//                    parameters.setFlashMode(Parameters.FLASH_MODE_OFF);
//
//                    button_flash.setImageResource(R.drawable.flash_off);
//                } else {
//                    List<String> supported_flash_modes = parameters.getSupportedFlashModes();
//
//                    if (supported_flash_modes.contains(Parameters.FLASH_MODE_ON)) {
//                        parameters.setFlashMode(Parameters.FLASH_MODE_ON);
//                    } else {
//                        parameters.setFlashMode(Parameters.FLASH_MODE_AUTO);
//                    }
//
//                    button_flash.setImageResource(R.drawable.flash_auto);
//                }
//                try {
//                    camera.cancelAutoFocus();
//                    camera.setParameters(parameters);
//
//                    tryAutoFocus(false, true);
//                } catch (Exception e) {
//                }
//            }
//        }
//    }


    private int getImageQuality() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this.getContext());

        String image_quality_s = sharedPreferences.getString("preference_quality", "90");

        int image_quality = 0;

        try {
            image_quality = Integer.parseInt(image_quality_s);
        } catch (NumberFormatException exception) {
            image_quality = 90;
        }

        return 90;
    }

    public static String getExposurePreferenceKey() {
        return "preference_exposure";
    }

    private List<String> convertFlashModesToValues(List<String> supported_flash_modes) {
        List<String> output_modes = new Vector<String>();

        if (supported_flash_modes != null) {
            if (supported_flash_modes.contains(Parameters.FLASH_MODE_AUTO)) {
                output_modes.add("flash_auto");
            }

            if (supported_flash_modes.contains(Parameters.FLASH_MODE_ON)) {
                output_modes.add("flash_on");
            }

            if (supported_flash_modes.contains(Parameters.FLASH_MODE_TORCH)) {
                output_modes.add("flash_torch");
            }

            if (supported_flash_modes.contains(Parameters.FLASH_MODE_OFF)) {
                output_modes.add("flash_off");
            }

            if (supported_flash_modes.contains(Parameters.FLASH_MODE_RED_EYE)) {
                output_modes.add("flash_red_eye");
            }
        }

        return output_modes;
    }

    public static String getFlashPreferenceKey(int cameraId) {
        return "flash_value_" + cameraId;
    }

    public static String getColorEffectPreferenceKey() {
        return "preference_color_effect";
    }

    public static String getWhiteBalancePreferenceKey() {
        return "preference_white_balance";
    }

    public static String getResolutionPreferenceKey(int cameraId) {
        return "camera_resolution_" + cameraId;
    }

    private void takePicture() {
        if (camera == null) {
            resetTakePictureStatus();
            return;
        }

        if (!this.has_surface) {
            resetTakePictureStatus();
            return;
        }

        setCameraFocus(true);
    }

    /**
     * Methode die aufgerufen wird, sobald ein Foto geschossen wurde.
     * <p>
     * Speichern der Foto-Daten als Bild, sowie der zurgeh&ouml;rigen Daten wie einem Foto-Thumb, Exif-Daten in der Datenbank und
     * Anzeige des Fotos als Preview in einem WebView.
     */
    private void takePictureWhenFocused() {
        if (mPhotoCaptureCallback != null)
            mPhotoCaptureCallback.onShowProgressBar();
        if (camera == null) {
            resetTakePictureStatus();
            return;
        }

        if (!this.has_surface) {
            resetTakePictureStatus();
            return;
        }

        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);

        MemoryInfo mi = new MemoryInfo();
        am.getMemoryInfo(mi);

        try {
            camera.takePicture(
                    new Camera.ShutterCallback() {
                        public void onShutter() {
                            zoomLocked = true;
                        }
                    },
                    null,
                    new Camera.PictureCallback() {
                        public void onPictureTaken(final byte[] photoData, final Camera camera) {
                            // final int currentDeviceOrientation = deviceOrientation;

                            Bitmap bitmap = BitmapFactory.decodeByteArray(photoData, 0, photoData.length);


                            exifwidth = 0;
                            exifheight = 0;
                            exifhasgps = false;
                            exifgpsx = 0.0d;
                            exifgpsy = 0.0d;
                            exifhasgpsdirection = false;
                            exifgpsdirection = 0;
                            exifdate = null;

                            if (camera != null) {
                                camera.stopPreview();

                            }

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
                            photoName = "PD_" + photoNameDateFormat.format(date2) + ".jpg";
                            try {
                                originalBitmap = getRotatedBitmap(bitmap);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }

                            FileOutputStream fOut = null;
                            try {
                                fOut = new FileOutputStream(photo);

                                SharedPrefsManager sharedPrefsManager = new SharedPrefsManager(context);
                                int photoQuality = 100;
                                if (!sharedPrefsManager.getStringValue(AppConstantsManager.USER_SELECTED_CAMERA_RESOLUTION, "100").equals("")) {
                                    photoQuality = Integer.valueOf(sharedPrefsManager.getStringValue(AppConstantsManager.USER_SELECTED_CAMERA_RESOLUTION, "100"));
                                }

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


                                switch (currentDeviceOrientation) {
                                    case 0:
                                        exifInterface.setAttribute(ExifInterface.TAG_ORIENTATION, String.valueOf(ExifInterface.ORIENTATION_NORMAL));
                                        break;
                                    case 90:
                                        exifInterface.setAttribute(ExifInterface.TAG_ORIENTATION, String.valueOf(ExifInterface.ORIENTATION_ROTATE_90));
                                        break;
                                    case 180:
                                        exifInterface.setAttribute(ExifInterface.TAG_ORIENTATION, String.valueOf(ExifInterface.ORIENTATION_ROTATE_180));
                                        break;
                                    case 270:
                                        exifInterface.setAttribute(ExifInterface.TAG_ORIENTATION, String.valueOf(ExifInterface.ORIENTATION_ROTATE_270));
                                        break;
                                }

                                exifInterface.saveAttributes();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }


                            if (mPhotoCaptureCallback != null)
//                                        mPhotoCaptureCallback.onFinishSuccess("/storage/emulated/0/Android/data/com.test.projectdocu.debug/files/projectDocu/captured_photos/PD_1589533842044.jpg");
                                mPhotoCaptureCallback.onFinishSuccess(photo.getAbsolutePath());


                            ProjectDocuCameraPreview.this.app_is_saving_photo = false;
                        }
                    }
            );
        } catch (RuntimeException e) {
            e.printStackTrace();
            startCameraPreview();
            if (mPhotoCaptureCallback != null)
                mPhotoCaptureCallback.onFinishFailure(null);

        }
    }

    private boolean isUserAndProjectDataStillValid() {
        return true;
    }


    /*public static boolean copyExifData(File sourceFile, File destFile) {
        String tempFileName = destFile.getAbsolutePath() + ".tmp";

        File tempFile = null;
        OutputStream tempStream = null;

        try {
            tempFile = new File (tempFileName);

            TiffOutputSet sourceSet = getSanselanOutputSet(sourceFile, TiffConstants.DEFAULT_TIFF_BYTE_ORDER);
            TiffOutputSet destSet = getSanselanOutputSet(destFile, sourceSet.byteOrder);

            if (sourceSet.byteOrder != destSet.byteOrder) return false;

            destSet.getOrCreateExifDirectory();

            List<?> sourceDirectories = sourceSet.getDirectories();

            for (int i=0; i<sourceDirectories.size(); i++) {
                TiffOutputDirectory sourceDirectory = (TiffOutputDirectory)sourceDirectories.get(i);
                TiffOutputDirectory destinationDirectory = getOrCreateExifDirectory(destSet, sourceDirectory);

                if (destinationDirectory == null) {
                    continue;
                }

                List<?> sourceFields = sourceDirectory.getFields();

                for (int j=0; j<sourceFields.size(); j++) {
                    TiffOutputField sourceField = (TiffOutputField) sourceFields.get(j);

                    destinationDirectory.removeField(sourceField.tagInfo);

                    destinationDirectory.add(sourceField);
                }
            }

            tempStream = new BufferedOutputStream(new FileOutputStream(tempFile));

            new ExifRewriter().updateExifMetadataLossless(destFile, tempStream, destSet);
            tempStream.close();

            if (destFile.delete()) {
                tempFile.renameTo(destFile);
            }

            return true;
        }
        catch (ImageReadException exception) {
            exception.printStackTrace();
        }
        catch (ImageWriteException exception) {
            exception.printStackTrace();
        }
        catch (IOException exception) {
            exception.printStackTrace();
        }
        finally {
            if (tempStream != null) {
                try {
                    tempStream.close();
                }
                catch (IOException e) {}
            }

            if (tempFile != null) {
                if (tempFile.exists()) tempFile.delete();
            }
        }

        return false;
    }


    private static TiffOutputSet getSanselanOutputSet(File jpegImageFile, int defaultByteOrder) throws IOException, ImageReadException, ImageWriteException {
        TiffImageMetadata exif = null;
        TiffOutputSet outputSet = null;

        IImageMetadata metadata = Sanselan.getMetadata(jpegImageFile);
        JpegImageMetadata jpegMetadata = (JpegImageMetadata) metadata;

        if (jpegMetadata != null) {
            exif = jpegMetadata.getExif();

            if (exif != null) {
                outputSet = exif.getOutputSet();
            }
        }

        if (outputSet == null) {
            outputSet = new TiffOutputSet((exif == null) ? defaultByteOrder : exif.contents.header.byteOrder);
        }

        return outputSet;
    }

    private static TiffOutputDirectory getOrCreateExifDirectory(TiffOutputSet outputSet, TiffOutputDirectory outputDirectory) {
        TiffOutputDirectory result = outputSet.findDirectory(outputDirectory.type);

        if (result != null) {
            return result;
        }

        result = new TiffOutputDirectory(outputDirectory.type);

        try {
            outputSet.addDirectory(result);
        }
        catch (ImageWriteException e) {
            return null;
        }

        return result;
    }
*/
//    public void removeProgressBar() {
//        progressBar.setVisibility(View.INVISIBLE);
//    }

/*
	public void showConsoleButtons(boolean switchButtons) {
		ImageView buttonFlash = (ImageView) ((Activity) context).findViewById(R.id.button_flash);
        SeekBar seekbarZoom = (SeekBar) ((Activity) context).findViewById(R.id.zoom_seekbar);

		ImageView buttonZoomIn= (ImageView) ((Activity) context).findViewById(R.id.zoom_in_button);
		ImageView buttonZoomOut = (ImageView) ((Activity) context).findViewById(R.id.zoom_out_button);

        LinearLayout layout_functionbuttons = (LinearLayout) ((Activity) context).findViewById(R.id.preview_function_buttons);

		if (switchButtons == false) {
			zoomLocked = true;

			// buttonFlash.setVisibility(GONE);
			seekbarZoom.setVisibility(GONE);
			buttonZoomIn.setVisibility(GONE);
			buttonZoomOut.setVisibility(GONE);
			// buttonPlan.setVisibility(GONE);

			((ProjectDocuMainActivity) context).deactivateMenuBarPlanButton();
		}
		else {
			zoomLocked = false;

			// buttonFlash.setVisibility(VISIBLE);
			seekbarZoom.setVisibility(VISIBLE);
			buttonZoomIn.setVisibility(VISIBLE);
			buttonZoomOut.setVisibility(VISIBLE);
			buttonTrash.setVisibility(GONE);
			// buttonPlan.setVisibility(VISIBLE);

			((ProjectDocuMainActivity) context).activateMenuBarPlanButton();

			// previewFunctionButtons.setVisibility(View.GONE);

			((ProjectDocuMainActivity) context).deactivateMenuBarButtons();
		}
	}
*/

    public void changeExposure(int change, boolean update_seek_bar) {
        if (change != 0 && camera != null && (min_exposure != 0 || max_exposure != 0)) {
            Parameters parameters = camera.getParameters();

            int current_exposure = parameters.getExposureCompensation();
            int new_exposure = current_exposure + change;

            setExposure(new_exposure, update_seek_bar);
        }
    }

    public void setExposure(int new_exposure, boolean update_seek_bar) {
        if (camera != null && (min_exposure != 0 || max_exposure != 0)) {
            cancelAutoFocus();

            Parameters parameters = camera.getParameters();

            int current_exposure = parameters.getExposureCompensation();

            if (new_exposure < min_exposure) {
                new_exposure = min_exposure;
            }

            if (new_exposure > max_exposure) {
                new_exposure = max_exposure;
            }

            if (new_exposure != current_exposure) {
                parameters.setExposureCompensation(new_exposure);

                setCameraParameters(parameters);

//	    		if( update_seek_bar ) {
//	    			SeekBar seek_bar = ((SeekBar) ((Activity) context).findViewById(R.id.zoom_seekbar));
//
//	    			final int min_exposure = getMinimumExposure();
//
//	    			seek_bar.setMax( getMaximumExposure() - min_exposure );
//	    			seek_bar.setProgress( getCurrentExposure() - min_exposure );
//	    		}
            }
        }
    }

    int getMinimumExposure() {
        return this.min_exposure;
    }

    int getMaximumExposure() {
        return this.max_exposure;
    }

    int getCurrentExposure() {
        if (camera == null) {
            return 0;
        }

        Parameters parameters = camera.getParameters();

        int current_exposure = parameters.getExposureCompensation();

        return current_exposure;
    }

    private void calculatePreviewToCameraMatrix() {
        calculateCameraToPreviewMatrix();

        if (!camera_to_preview_matrix.invert(preview_to_camera_matrix)) {
        }
    }

    // Camera preview calculation
    private void calculateCameraToPreviewMatrix() {
        camera_to_preview_matrix.reset();

        Camera.getCameraInfo(cameraId, camera_info);

        boolean mirror = (camera_info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT);

        camera_to_preview_matrix.setScale(mirror ? -1 : 1, 1);
        camera_to_preview_matrix.postRotate(display_orientation);
        camera_to_preview_matrix.postScale(this.getWidth() / 2000f, this.getHeight() / 2000f);
        camera_to_preview_matrix.postTranslate(this.getWidth() / 2f, this.getHeight() / 2f);
    }

    private ArrayList<Camera.Area> getAreas(float x, float y) {
        float[] coords = {x, y};

        calculatePreviewToCameraMatrix();

        preview_to_camera_matrix.mapPoints(coords);

        float focus_x = coords[0];
        float focus_y = coords[1];

        int focus_size = 50;

        Rect rect = new Rect();

        rect.left = (int) focus_x - focus_size;
        rect.right = (int) focus_x + focus_size;
        rect.top = (int) focus_y - focus_size;
        rect.bottom = (int) focus_y + focus_size;

        if (rect.left < -1000) {
            rect.left = -1000;
            rect.right = rect.left + 2 * focus_size;
        } else if (rect.right > 1000) {
            rect.right = 1000;
            rect.left = rect.right - 2 * focus_size;
        }

        if (rect.top < -1000) {
            rect.top = -1000;
            rect.bottom = rect.top + 2 * focus_size;
        } else if (rect.bottom > 1000) {
            rect.bottom = 1000;
            rect.top = rect.bottom - 2 * focus_size;
        }

        ArrayList<Camera.Area> areas = new ArrayList<Camera.Area>();

        areas.add(new Camera.Area(rect, 1000));

        return areas;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (app_is_in_capture_mode == false) {
            return true;
        }
        float x = event.getX();
        float y = event.getY();
//		((ProjectDocuMainActivity) context).activateMenuBarPlanButton();
//		((ProjectDocuMainActivity) context).activateMenuBarCameraButton();
//		((ProjectDocuMainActivity) context).activateMenuBarCameraConsole();

        scaleGestureDetector.onTouchEvent(event);

        if (camera == null) {
            this.openCamera();
            return true;
        }

        if (event.getPointerCount() != 1) {
            touch_was_multitouch = true;
            return true;
        }

        if (event.getAction() != MotionEvent.ACTION_UP) {
            if (event.getAction() == MotionEvent.ACTION_DOWN && event.getPointerCount() == 1) {
                touch_was_multitouch = false;

                // Canvas handling

                Rect touchRect = new Rect(
                        (int) (x - 100),
                        (int) (y - 100),
                        (int) (x + 100),
                        (int) (y + 100));


                final Rect targetFocusRect = new Rect(
                        touchRect.left * 2000 / this.getWidth() - 1000,
                        touchRect.top * 2000 / this.getHeight() - 1000,
                        touchRect.right * 2000 / this.getWidth() - 1000,
                        touchRect.bottom * 2000 / this.getHeight() - 1000);

                projectDocuCameraPreview.doTouchFocus(targetFocusRect);
//            if (drawingViewSet) {
                drawingView.setHaveTouch(true, touchRect);
                drawingView.invalidate();

//                // Remove the square indicator after 1000 msec
//                Handler handler = new Handler();
//                handler.postDelayed(new Runnable() {
//
//                    @Override
//                    public void run() {
//                        drawingView.setHaveTouch(false, new Rect(0,0,0,0));
//                        drawingView.invalidate();
//                    }
//                }, 1000);
            }

            return true;
        }

        if (touch_was_multitouch) {
            return true;
        }

        startCameraPreview();

        cancelAutoFocus();

        if (camera != null && !this.using_face_detection) {
            Parameters parameters = camera.getParameters();

            String focus_mode = parameters.getFocusMode();

            this.has_focus_area = false;

            if (parameters.getMaxNumFocusAreas() != 0 && focus_mode != null && (focus_mode.equals(Parameters.FOCUS_MODE_AUTO) || focus_mode.equals(Parameters.FOCUS_MODE_MACRO) || focus_mode.equals(Parameters.FOCUS_MODE_CONTINUOUS_PICTURE) || focus_mode.equals(Parameters.FOCUS_MODE_CONTINUOUS_VIDEO))) {
                this.has_focus_area = true;

                this.focus_screen_x = (int) event.getX();
                this.focus_screen_y = (int) event.getY();

                ArrayList<Camera.Area> areas = getAreas(event.getX(), event.getY());

                parameters.setFocusAreas(areas);

                if (parameters.getMaxNumMeteringAreas() != 0) {
                    parameters.setMeteringAreas(areas);
                }

                setCameraParameters(parameters);
            } else if (parameters.getMaxNumMeteringAreas() != 0) {
                ArrayList<Camera.Area> areas = getAreas(event.getX(), event.getY());

                parameters.setMeteringAreas(areas);

                setCameraParameters(parameters);
            }
        }

        tryAutoFocus(false, true);

        return true;
    }


    private void tryAutoFocus(final boolean startup, final boolean manual) {
        if (camera == null) {
        } else if (!this.has_surface) {
        } else {
            Parameters parameters = camera.getParameters();

            String focus_mode = parameters.getFocusMode();

            if (focus_mode != null && (focus_mode.equals(Parameters.FOCUS_MODE_AUTO) || focus_mode.equals(Parameters.FOCUS_MODE_MACRO))) {
                String old_flash = parameters.getFlashMode();

                set_flash_after_autofocus = "";

//                if (startup && old_flash != null && old_flash != Parameters.FLASH_MODE_OFF) {
//                    set_flash_after_autofocus = old_flash;
//
//                    parameters.setFlashMode(Parameters.FLASH_MODE_OFF);
//
//                    setCameraParameters(parameters);
//                }

                Camera.AutoFocusCallback autoFocusCallback = new Camera.AutoFocusCallback() {
                    @Override
                    public void onAutoFocus(boolean success, Camera camera) {
                        autoFocusCompleted(manual, success, false);
                    }
                };

                this.focus_success = FOCUS_WAITING;
                this.focus_complete_time = -1;
                this.successfully_focused = false;

                try {
                    camera.autoFocus(autoFocusCallback);
                    count_cameraAutoFocus++;
                } catch (RuntimeException e) {
                    autoFocusCallback.onAutoFocus(false, camera);

                    e.printStackTrace();
                }
            } else if (has_focus_area) {
                focus_success = FOCUS_SUCCESS;
                focus_complete_time = System.currentTimeMillis();
            }
        }
    }

    public void startCameraPreview() {
        if (camera == null) {
            return;
        }

//		if (projectDocuTagOverlay != null) {
//			projectDocuTagOverlay.hide();
//		}
//
//		setupPlanButton();

        try {
//
            camera.startPreview();
            Parameters parameters = camera.getParameters();

            List<Size> sizes = parameters.getSupportedPictureSizes();

            Size cs = sizes.get(0);

            parameters.set("orientation", "portrait");


            List<Size> size = parameters.getSupportedPreviewSizes();

            // parameters.setSceneMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
            // parameters.setSceneMode(Camera.Parameters.SCENE_MODE_ACTION);
            // parameters.setSceneMode(Camera.Parameters.SCENE_MODE_SPORTS);
            // parameters.setSceneMode(Camera.Parameters.FOCUS_MODE_AUTO);
            // parameters.setSceneMode(Camera.Parameters.SCENE_MODE_STEADYPHOTO);
            parameters.setSceneMode(Parameters.SCENE_MODE_AUTO);
            // parameters.setSceneMode(Camera.Parameters.WHITE_BALANCE_AUTO);
            // parameters.setSceneMode(Camera.Parameters.EFFECT_NONE);
            // parameters.setSceneMode(Camera.Parameters.SCENE_MODE_STEADYPHOTO);

            try {
                camera.setParameters(parameters);
            } catch (Exception e) {
            }

            this.app_is_in_capture_mode = true;
        } catch (RuntimeException e) {
            e.printStackTrace();
            return;
        }
    }

    public void zoomIn() {
        if (zoomLocked == true) {
            return;
        }

        if (camera != null) {
            final Parameters parameters = camera.getParameters();

            if (parameters.isZoomSupported()) {
                zoomTo(zoom_factor + 1, true);
            }
        }
    }

    public void zoomOut() {
        if (zoomLocked == true) {
            return;
        }

        if (camera != null) {
            zoomTo(zoom_factor - 1, true);
        }
    }

    public void setCameraFocus(final boolean makePhoto) {
        try {
            if (camera != null) {

                camera.cancelAutoFocus();
                if (camera.getParameters() != null) {
                    List<String> supportedFocusModes = camera.getParameters().getSupportedFocusModes();

                    if (supportedFocusModes != null && supportedFocusModes.contains(Parameters.FOCUS_MODE_AUTO)) {
                        camera.autoFocus(new Camera.AutoFocusCallback() {

                            @Override
                            public void onAutoFocus(boolean b, Camera camera) {
                                if (b)
                                    camera.cancelAutoFocus();

                                if (makePhoto == true) {
                                    takePictureWhenFocused();
                                }
                            }
                        });
                    } else {
                        if (makePhoto == true) {
                            takePictureWhenFocused();
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

//	private boolean isUserAndProjectDataStillValid () {
//		if (((ProjectDocuMainActivity) context).currentUserId == -1) {
//			((ProjectDocuMainActivity) context).showDialog(ProjectDocuMainActivity.DIALOG_UNVALID_USER_ERROR);
//			return false;
//		} else if (((ProjectDocuMainActivity) context).currentProjectId == -1) {
//			((ProjectDocuMainActivity) context).showDialog(ProjectDocuMainActivity.DIALOG_UNVALID_PROJECT_ERROR);
//			return false;
//		}
//
//		return true;
//	}
//
//	private void setAlwaysActiveTagsForPhoto (int localPhotoId) {
//		ProjectDocuDatabaseManager projectDocuDatabaseManager = new ProjectDocuDatabaseManager (context);
//
//		ArrayList<Tags> tagsList = projectDocuDatabaseManager.selectDataFromTags(((ProjectDocuMainActivity) context).currentProjectId);
//
//		for (Tags tag : tagsList) {
//			if (tag.tagisalwaysactive == 1) {
//				projectDocuDatabaseManager.updatePhotoTagMapping(localPhotoId, tag.localtagid, tag.type, "1");
//
//				projectDocuDatabaseManager.updateNeedUpdateStatusOfPDPhoto(localPhotoId, 1);
//			}
//		}
//	}
//
//	public void setTagIconStatus(boolean status) {
//		if (status == true){
//			((ProjectDocuMainActivity) context).setTagsButtonArrow(true);
//
//		}
//		else {
//			((ProjectDocuMainActivity) context).setTagsButtonArrow(false);
//		}
//	}
//
//	public ProjectDocuTagOverlay getProjectDocuTagOverlay () {
//		return projectDocuTagOverlay;
//	}

    public int getLocalPhotoId() {
        return localPhotoId;
    }

//	public void switchFlash() {
//		if (camera != null) {
//			final Parameters parameters = camera.getParameters();
//
//			final ImageView buttonFlash = (ImageView) ((Activity) context).findViewById(R.id.button_flash);
//
//			if (context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH)) {
//				if (parameters.getFlashMode().equals(Parameters.FLASH_MODE_ON) || parameters.getFlashMode().equals(Parameters.FLASH_MODE_AUTO)) {
//					parameters.setFlashMode(Parameters.FLASH_MODE_OFF);
//
////					((ProjectDocuMainActivity) context).setFlashOn(false);
//				}
//				else{
//					List<String> supported_flash_modes = parameters.getSupportedFlashModes();
//
//					if(supported_flash_modes.contains(Parameters.FLASH_MODE_ON)) {
//						parameters.setFlashMode(Parameters.FLASH_MODE_ON);
//					}
//					else {
//						parameters.setFlashMode(Parameters.FLASH_MODE_AUTO);
//					}
//
////					((ProjectDocuMainActivity) context).setFlashOn(true);
//				}
//				try {
//					camera.cancelAutoFocus();
//					camera.setParameters(parameters);
//
//					tryAutoFocus(false,true);
//				}
//				catch (Exception e) {}
//			}
//		}
//	}

    public int getTagOrientation() {
        return tagOrientation;
    }


    public void doTouchFocus(final Rect tfocusRect) {
        try {
            List<Camera.Area> focusList = new ArrayList<Camera.Area>();
            Camera.Area focusArea = new Camera.Area(tfocusRect, 1000);
            focusList.add(focusArea);

            Camera.Parameters param = camera.getParameters();
            param.setFocusAreas(focusList);
            param.setMeteringAreas(focusList);
            camera.setParameters(param);

            Camera.AutoFocusCallback autoFocusCallback = new Camera.AutoFocusCallback() {
                @Override
                public void onAutoFocus(boolean success, Camera camera) {
                    if (success)
                        camera.cancelAutoFocus();
                }
            };

            ///camera.autoFocus(myAutoFocusCallback);
        } catch (Exception e) {
            e.printStackTrace();
            Log.i(TAG, "Unable to autofocus");
        }
    }


    public void setDrawingView(DrawingViewCameraFocus dView) {
        drawingView = dView;
    }


}
