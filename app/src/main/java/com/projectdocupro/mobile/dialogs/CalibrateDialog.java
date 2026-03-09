package com.projectdocupro.mobile.dialogs;

import android.app.Activity;
import android.app.Dialog;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bumptech.glide.Glide;
import com.projectdocupro.mobile.R;

public class CalibrateDialog extends Dialog {
    Activity activity;

    public CalibrateDialog(@NonNull Activity context) {
        super(context);
        activity = context;

    }

    public CalibrateDialog(@NonNull Activity context, int themeResId) {
        super(context, themeResId);
        activity = context;
    }

    protected CalibrateDialog(@NonNull Activity context, boolean cancelable, @Nullable OnCancelListener cancelListener) {
        super(context, cancelable, cancelListener);
        activity = context;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setBackgroundDrawableResource(android.R.color.transparent);

        setContentView(R.layout.calibation_dialog);

        ImageView img = findViewById(R.id.img_helper);


        Glide.with(activity).load(R.raw.calibation_gif).into(img);
    }




}
