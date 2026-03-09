package com.projectdocupro.mobile.interfaces;

import android.graphics.Bitmap;

public interface PhotoCaptureCallback {
    void onFinishSuccess(String photoPath);
    void onFinishFailure(Bitmap bitmap);
    void onShowProgressBar();
}
