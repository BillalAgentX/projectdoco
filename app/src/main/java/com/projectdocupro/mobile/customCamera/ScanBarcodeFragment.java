package com.projectdocupro.mobile.customCamera;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.hardware.Camera;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;

import com.projectdocupro.mobile.R;

import java.io.ByteArrayOutputStream;



public class ScanBarcodeFragment extends Fragment{


    private CameraPreview mCameraPreview;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_scan_barcode, container, false);
        bindView(view);
        return view;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Inject views


        setupCameraPreview();
    }

    private void setupCameraPreview() {
        mCameraPreview.setCameraReadyListener(new CameraPreview.CameraReadyListener() {

            @Override
            public void onCameraReady(Camera camera) {
                camera.setPreviewCallback(new Camera.PreviewCallback() {

                    @Override
                    public void onPreviewFrame(byte[] data, Camera camera) {
                        Camera.Parameters parameters = camera.getParameters();
                        Camera.Size size = parameters.getPreviewSize();

                        // Convert uncompressed data to a JPEG
                        YuvImage yuvimage = new YuvImage(data, ImageFormat.NV21, size.width, size.height, null);
                        ByteArrayOutputStream baos = new ByteArrayOutputStream();
                        yuvimage.compressToJpeg(new Rect(0, 0, size.width, size.height), 80, baos);
                        byte[] jpegData = baos.toByteArray();

                        // Create a Bitmap of the JPEG
                        Bitmap frame = BitmapFactory.decodeByteArray(jpegData, 0, jpegData.length);

                        if (frame != null) {
                            // Do something with frame,e.g. pass to ZXING for barcode analysis.

                        }
                    }
                });

            }

        });
    }


    private void bindView(View bindSource) {
        mCameraPreview = bindSource.findViewById(R.id.camera_preview);
    }
}

