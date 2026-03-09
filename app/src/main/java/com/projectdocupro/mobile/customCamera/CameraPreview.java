package com.projectdocupro.mobile.customCamera;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.ImageFormat;
import android.hardware.Camera;
import android.hardware.Camera.Parameters;
import android.util.AttributeSet;
import android.view.ScaleGestureDetector;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import androidx.core.content.ContextCompat;

import java.io.IOException;
import java.util.List;

/**
 * A SurfaceView that'll show a preview from the device camera.
 */
@SuppressWarnings("deprecation")
public class CameraPreview extends SurfaceView implements SurfaceHolder.Callback {
    private static final int AUTO_FOCUS_DELAY = 250;
    private Camera mCamera;
    private CameraReadyListener mListener = null;
    private boolean has_zoom = false;

    private Runnable doAutoFocusRunnable = new Runnable() {
        @Override
        public void run() {
            mCamera.cancelAutoFocus();
            mCamera.autoFocus(new Camera.AutoFocusCallback() {
                @Override
                public void onAutoFocus(boolean success, Camera camera) {
                    postDelayed(doAutoFocusRunnable, AUTO_FOCUS_DELAY);
                }
            });
        }
    };

    public interface CameraReadyListener {
        void onCameraReady(Camera camera);
    }

    public CameraPreview(Context context) {
        super(context);
        initView();
    }

    public CameraPreview(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView();
    }

    private void initView() {
        getHolder().setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        getHolder().addCallback(this);
        setFocusable(true);
        setFocusableInTouchMode(true);
        requestFocus();
    }

    private class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {
        @Override
        public boolean onScale(ScaleGestureDetector detector) {
            if( mCamera != null  ) {
//                scaleZoom(detector.getScaleFactor());
            }
            return true;
        }
    }



    public void setCameraReadyListener(CameraReadyListener listener) {
        this.mListener = listener;
    }

    @Override
    public void surfaceChanged(SurfaceHolder sh, int format, int width, int height) {
        // Now that the size is known, set up the camera parameters and begin
        // the preview.

        // Application is hard-coded to portrait so rotate landscape camera by 90
        // degrees.
        mCamera.setDisplayOrientation(90);

        Parameters parameters = mCamera.getParameters();

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

        mCamera.setParameters(parameters);
        mCamera.startPreview();

        if (autoFocus) {
            postDelayed(doAutoFocusRunnable, AUTO_FOCUS_DELAY);
        }
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        // The Surface has been created, acquire the camera and tell it where
        // to draw.

        if (mCamera != null) {
            mCamera.release();
            mCamera = null;
        }

        // API 23 requires us to check for permissions at runtime.
        int permissionCheck = ContextCompat.checkSelfPermission(getContext(), Manifest.permission.CAMERA);
        if (permissionCheck == PackageManager.PERMISSION_GRANTED) {
            mCamera = Camera.open(Camera.CameraInfo.CAMERA_FACING_BACK);
            try {
                mCamera.setPreviewDisplay(holder);
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (mListener != null) {
                mListener.onCameraReady(mCamera);
            }
        }
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        // Surface will be destroyed when we return, so stop the preview.
        // Because the CameraDevice object is not a shared resource, it's very
        // important to release it when the activity is paused.
        cleanUpCamera();
    }

    public void cleanUpCamera() {
        if (mCamera != null) {

            removeCallbacks(doAutoFocusRunnable);

            mCamera.cancelAutoFocus();
            mCamera.setPreviewCallback(null);
            mCamera.stopPreview();
            mCamera.release();
            mCamera = null;
        }
    }
}
