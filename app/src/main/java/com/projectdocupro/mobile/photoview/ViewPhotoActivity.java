/*
 Copyright 2011, 2012 Chris Banes.

 Licensed under the Apache License, Version 2.0 (the "License");
 you may not use this file except in compliance with the License.
 You may obtain a copy of the License at

 http://www.apache.org/licenses/LICENSE-2.0

 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.
 */
package com.projectdocupro.mobile.photoview;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.RectF;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.projectdocupro.mobile.R;
import com.projectdocupro.mobile.managers.SharedPrefsManager;
import com.projectdocupro.mobile.utility.Utils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Date;

import static com.projectdocupro.mobile.managers.AppConstantsManager.ACTIVATE_PHOTO_PATH;

public class ViewPhotoActivity extends AppCompatActivity {

    static final String PHOTO_TAP_TOAST_STRING = "Photo Tap! X: %.2f %% Y:%.2f %% ID: %d";
    static final String SCALE_TOAST_STRING = "Scaled to: %.2ff";
    static final String FLING_LOG_STRING = "Fling velocityX: %.2f, velocityY: %.2f";

    private PhotoView mPhotoView;
    private TextView mCurrMatrixTv;

    private Toast mCurrentToast;

    private Matrix mCurrentDisplayMatrix = null;
    private SharedPrefsManager sharedPrefsManager;
    private String imagePath;
    private Bitmap bitmap;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_simple_sample);

        Utils.showLogger("ViewPhotoActivity");
         sharedPrefsManager= new SharedPrefsManager(this);

        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("Simple Sample");
//        toolbar.setNavigationIcon(R.drawable.ic_arrow_back_white_24dp);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
//        toolbar.inflateMenu(R.menu.main_menu);

        if(!sharedPrefsManager.getStringValue(ACTIVATE_PHOTO_PATH,"").equals("")){

            bitmap = BitmapFactory.decodeFile(sharedPrefsManager.getStringValue(ACTIVATE_PHOTO_PATH,""));

        }


        mPhotoView = findViewById(R.id.iv_photo);
        mCurrMatrixTv = findViewById(R.id.tv_current_matrix);

//        Drawable bitmap = ContextCompat.getDrawable(this, R.drawable.wallpaper);
        mPhotoView.setImageBitmap(bitmap);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                mPhotoView.setScale(3.0f);
            }
        }, 100);

        // Lets attach some listeners, not required though!
        mPhotoView.setOnMatrixChangeListener(new MatrixChangeListener());
        mPhotoView.setOnPhotoTapListener(new PhotoTapListener());
        mPhotoView.setOnSingleFlingListener(new SingleFlingListener());

//        mPhotoView.setOnTouchListener(new View.OnTouchListener() {
//            @Override
//            public boolean onTouch(View v, MotionEvent event) {
//
//
//// touchPoint now contains x and y in image's coordinate system
//                return false;
//            }
//        });
    }

    private class PhotoTapListener implements OnPhotoTapListener {

        @Override
        public void onPhotoTap(ImageView view, float x, float y) {
            float xPercentage = x * 100f;
            float yPercentage = y * 100f;

            showToast(String.format(PHOTO_TAP_TOAST_STRING, xPercentage, yPercentage, view == null ? 0 : view.getId()));

            // calculate inverse matrix
            Matrix inverse = new Matrix();
            mPhotoView.getImageMatrix().invert(inverse);

// map touch point from ImageView to image
            float[] touchPoint = new float[] {xPercentage, yPercentage};
            inverse.mapPoints(touchPoint);
        }
    }


    private void mergStatusOnPhoto(Bitmap myBitmap,float x,float y) {

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



            canvas.drawBitmap(smallImage, (bigImage.getWidth() / 2) + x - 20, (bigImage.getHeight() / 2) + y - 50, null);

        writeBitMapToDisk(result, "456");


    }

    private boolean writeBitMapToDisk(Bitmap bitmap, String projectId) {

        try {
            // todo change the file location/name according to your needs
            File dir = getExternalFilesDir("/projectDocu/project_plans_" + projectId);
            if (dir == null) {
                dir =getFilesDir();
            }
//            File dir = new File(Environment.getExternalStorageDirectory() + "/projectDocu/project_plans_" + projectId);
            if (!dir.isDirectory()) {
                dir.mkdirs();
            }
            File photo = new File(dir, "/Download_" + new Date().getTime() + ".jpg");

            imagePath = photo.getAbsolutePath();
            sharedPrefsManager.setStringValue(ACTIVATE_PHOTO_PATH,imagePath);
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



    private void showToast(CharSequence text) {
        if (mCurrentToast != null) {
            mCurrentToast.cancel();
        }

        mCurrentToast = Toast.makeText(ViewPhotoActivity.this, text, Toast.LENGTH_SHORT);
        mCurrentToast.show();
    }

    private class MatrixChangeListener implements OnMatrixChangedListener {

        @Override
        public void onMatrixChanged(RectF rect) {
            mCurrMatrixTv.setText(rect.toString());
        }
    }

    private class SingleFlingListener implements OnSingleFlingListener {

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            Log.d("PhotoView", String.format(FLING_LOG_STRING, velocityX, velocityY));
            return true;
        }
    }
}
