package com.projectdocupro.mobile.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.widget.ContentLoadingProgressBar;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.Point;
import android.graphics.RectF;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.animation.RotateAnimation;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Toolbar;



import com.projectdocupro.mobile.ProjectsDatabase;
import com.projectdocupro.mobile.R;
import com.projectdocupro.mobile.interfaces.RetroApiInterface;
import com.projectdocupro.mobile.managers.AppConstantsManager;
import com.projectdocupro.mobile.managers.RetrofitManager;
import com.projectdocupro.mobile.managers.SharedPrefsManager;
import com.projectdocupro.mobile.models.PlansPhotoModel;
import com.projectdocupro.mobile.photoview.OnMatrixChangedListener;
import com.projectdocupro.mobile.photoview.OnPhotoTapListener;
import com.projectdocupro.mobile.photoview.OnSingleFlingListener;
import com.projectdocupro.mobile.photoview.PhotoView;
import com.projectdocupro.mobile.repos.PlansPhotoRepository;
import com.projectdocupro.mobile.utility.Utils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Date;
import java.util.Random;


import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AddPhotoDirectionActivity extends AppCompatActivity {
    RelativeLayout pbar;

    ContentLoadingProgressBar progressBar;

    String imagePath;
    //
//    @BindView(R.id.img_large)
//    ImageView imageView;
//
//    @BindView(R.id.rotation_arrow)
//    ImageView rotationArrow;
//
//    @BindView(R.id.tv_coordinate)
//    TextView tvCoordinates;
//
    private
    View progress_bar_view;

    private int x;
    private int y;

    // Sample Activity variables

    static final String PHOTO_TAP_TOAST_STRING = "Photo Tap! X: %.2f %% Y:%.2f %% ID: %d";
    static final String SCALE_TOAST_STRING = "Scaled to: %.2ff";
    static final String FLING_LOG_STRING = "Fling velocityX: %.2f, velocityY: %.2f";

    private PhotoView mPhotoView;
    private ImageView crosshair;
    private ImageView ivAddZoom;
    private ImageView ivReset;
    private TextView mCurrMatrixTv;

    private Toast mCurrentToast;
    private ImageView dialer;
    private Matrix mCurrentDisplayMatrix = null;
    int mWidth;
    int mHeight;
    private double mCurrAngle = 0;
    private double mPrevAngle = 0;
    ImageView wheel;
    private RelativeLayout rl_parent;
    private Button btnCancel;
    private Button btnSave;
    float xCenterArrowImage;
    float yCenterArrowImage;
    float xArrowImage;
    float yArrowImage;
    float xCrossImage;
    float yCrossImage;
    float imageScaleFactor;
   final float SCALE_FACTOR=2;

    private PlansPhotoModel plansPhotoModel;
    PlansPhotoRepository plansPhotoRepository;
    private Bitmap myBitmap;
    private float minScale;
    private float maxScale;
    private Toolbar toolbar;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_simple_sample);

        bindView();
        plansPhotoRepository = new PlansPhotoRepository(AddPhotoDirectionActivity.this);
        addEvent();

        new loadPlansImagesAsyncTask(this).execute(getIntent().getExtras().getString("fileId"));


    }

//    @OnClick(R.id.tv_coordinate)
//    void showCoordinates(View view) {
//        tvCoordinates.setText(x + "  " + y);
//    }
@Override
public boolean onOptionsItemSelected(MenuItem item) {
    switch (item.getItemId()){
        case android.R.id.home:
            onBackPressed();
            return true;
    }
    return super.onOptionsItemSelected(item);
}
    private void addEvent() {
//         toolbar = findViewById(R.id.toolbar);
        crosshair = (ImageView) findViewById(R.id.crosshair);
        ivAddZoom = (ImageView) findViewById(R.id.iv_zoom);
        ivReset = (ImageView) findViewById(R.id.iv_reset);
        btnCancel = (Button) findViewById(R.id.btn_cancel);
        btnSave = (Button) findViewById(R.id.btn_save);
        dialer = (ImageView) findViewById(R.id.rotation_arrow);
        rl_parent = (RelativeLayout) findViewById(R.id.rl_parent);


        wheel = dialer;

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setTitle(getResources().getString(R.string.plan_detail));
        toolbar.setNavigationIcon(R.drawable.ic_back);
        toolbar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });


        mWidth = this.getResources().getDisplayMetrics().widthPixels;
        mHeight = this.getResources().getDisplayMetrics().heightPixels;
        ivAddZoom.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Random r = new Random();
//
                 minScale = mPhotoView.getMinimumScale();
                 maxScale = mPhotoView.getMaximumScale();
                float randomScale = minScale + (r.nextFloat() * (maxScale - minScale));
                imageScaleFactor = randomScale;
//                        float randomScale = 0.5f;
                mPhotoView.setScale(randomScale);
            }
        });
        ivReset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                new loadPlansImagesAsyncTask(AddPhotoDirectionActivity.this).execute(getIntent().getExtras().getString("fileId"));

            }
        });

        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                finish();
            }
        });
        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                saveAction();
            }
        });


        mPhotoView = findViewById(R.id.iv_photo);
        mCurrMatrixTv = findViewById(R.id.tv_current_matrix);

//        Drawable bitmap = ContextCompat.getDrawable(this, R.drawable.dragon_image);
//        mPhotoView.setImageDrawable(bitmap);

        // Lets attach some listeners, not required though!
//        int array[] = {(int)mPhotoView.getPivotX(),(int)mPhotoView.getPivotY()};
//        mPhotoView.getLocationOnScreen(array);
        mPhotoView.setOnMatrixChangeListener(new MatrixChangeListener());
        mPhotoView.setOnPhotoTapListener(new PhotoTapListener());
        mPhotoView.setOnSingleFlingListener(new SingleFlingListener());
//        mPhotoView.setOnScaleChangeListener(new OnScaleChangedListener() {
//            @Override
//            public void onScaleChange(float scaleFactor, float focusX, float focusY) {
//                Log.d("onScroll","scrollX "+focusX+" scrollY "+focusY+" scaleFactor "+scaleFactor);
//                imageScaleFactor=scaleFactor;
////                placeImage(focusX,focusY);
//
//            }
//        });

        mPhotoView.setOnDoubleTapListener(new GestureDetector.OnDoubleTapListener() {
            @Override
            public boolean onSingleTapConfirmed(MotionEvent e) {

                placeImage(e.getX(), e.getY());
                return false;
            }

            @Override
            public boolean onDoubleTap(MotionEvent e) {
                return false;
            }

            @Override
            public boolean onDoubleTapEvent(MotionEvent e) {

                return false;
            }
        });
//        crosshair.setOnTouchListener(new View.OnTouchListener() {
//            @Override
//            public boolean onTouch(View v, MotionEvent event) {
////                mPhotoView.performLongClick(event.getX(),event.getY());
//                return false;
//            }
//        });


        wheel.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                final float xc = wheel.getWidth() / 2;
                final float yc = wheel.getHeight() / 2;

                final float x = event.getX();
                final float y = event.getY();

                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN: {
                        wheel.clearAnimation();
                        mCurrAngle = Math.toDegrees(Math.atan2(x - xc, yc - y));
                        break;
                    }
                    case MotionEvent.ACTION_MOVE: {
                        mPrevAngle = mCurrAngle;
                        mCurrAngle = Math.toDegrees(Math.atan2(x - xc, yc - y));
                        animate(mPrevAngle, mCurrAngle, 0);

                        Log.d("mCurrent ANGEL", mPrevAngle + "");
                        Log.d("mPrev ANGEL", mPrevAngle + "");
                        break;
                    }
                    case MotionEvent.ACTION_UP: {
                        mPrevAngle = mCurrAngle = 0;
                        break;
                    }
                }

                return true;
            }
        });
    }

    private void saveAction() {
        plansPhotoModel.setArrow_direction_center_x(String.valueOf(xCenterArrowImage));
        plansPhotoModel.setArrow_direction_center_y(String.valueOf(yCenterArrowImage));
        plansPhotoModel.setFocus_point_x(String.valueOf(xCrossImage));
        plansPhotoModel.setFocus_point_y(String.valueOf(yCrossImage));
        plansPhotoModel.setArrow_direction_x(String.valueOf(xArrowImage));
        plansPhotoModel.setArrow_direction_y(String.valueOf(yArrowImage));
        plansPhotoModel.setArrow_angel(String.valueOf(mCurrAngle));
        plansPhotoModel.setScale_factor(String.valueOf(imageScaleFactor));
        plansPhotoRepository.updatePhotosModel(plansPhotoModel);
        finish();

    }


    public void callGetPlanImageAPI(Context context, PlansPhotoModel plansPhotoModel) {
        showProgressbar();
        SharedPrefsManager sharedPrefsManager = new SharedPrefsManager(context);
        RetroApiInterface retroApiInterface = RetrofitManager.getInstance().create(RetroApiInterface.class);
        String authToken = sharedPrefsManager.getStringValue(AppConstantsManager.AUTH_API_TOKEN, "");

        if (authToken.length() > 2) {
            authToken = authToken.substring(1, authToken.length() - 1);
        }

        Call<ResponseBody> call = retroApiInterface.getPlanImage(authToken, Utils.DEVICE_ID, plansPhotoModel.getPlanId());
        Log.d("call url", call.request().url().toString());

        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                hideProgressbar();

                if (response.isSuccessful()) {
                    if (response.body() != null) {
                        Log.d("List", "Success : " + response.body());
                        if (writeResponseBodyToDisk(response.body())) {
                            Bitmap bitmap = BitmapFactory.decodeFile(imagePath);
                            mPhotoView.setImageBitmap(bitmap);
                            plansPhotoModel.setPohotPath(imagePath);
                            if (!imagePath.equals(""))
                                plansPhotoModel.setPhotoCached(true);//Add Photo Direction activity
                            plansPhotoRepository.insert(plansPhotoModel);
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
                hideProgressbar();
                Log.d("List", "failed : " + t.getMessage());
            }
        });
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        x = (int) event.getX();
        y = (int) event.getY();
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
            case MotionEvent.ACTION_MOVE:
            case MotionEvent.ACTION_UP:
        }
        return false;
    }

    private boolean writeResponseBodyToDisk(ResponseBody body) {
        try {
            // todo change the file location/name according to your needs

            File dir = new File(Environment.getExternalStorageDirectory() + "/projectDocu/project_" + getIntent().getStringExtra("projectId"));
            if (!dir.isDirectory()) {
                dir.mkdirs();
            }
            File photo = new File(dir, "/Download_" + new Date().getTime() + ".jpg");

            imagePath = photo.getAbsolutePath();

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

    private void bindView() {
        pbar = findViewById(R.id.rl_pb_parent);

        progressBar = findViewById(R.id.progressBar);

        progress_bar_view = findViewById(R.id.progress_bar_view);
        dialer = findViewById(R.id.rotation_arrow);
        toolbar = findViewById(R.id.toolbar);
    }


    private void showProgressbar() {

        pbar.setVisibility(View.VISIBLE);
        progressBar.show();

    }

    private void hideProgressbar() {

        pbar.setVisibility(View.GONE);

    }

    private class loadPlansImagesAsyncTask extends AsyncTask<String, Void, PlansPhotoModel> {
        private ProjectsDatabase database;

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


            if (plansPhotoModel.getPohotPath() != null && !plansPhotoModel.getPohotPath().equals("")) {

                File imgFile = new File(plansPhotoModel.getPohotPath());

                if (imgFile.exists()) {

                    myBitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
//                     Bitmap bitmap=myBitmap.createScaledBitmap(myBitmap,myBitmap.getWidth()*2,myBitmap.getHeight()*2,true);
                    mPhotoView.setImageBitmap(myBitmap);

                    ViewTreeObserver vto = mPhotoView.getViewTreeObserver();
                    vto.addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
                        public boolean onPreDraw() {
                            mPhotoView.getViewTreeObserver().removeOnPreDrawListener(this);
                            int finalHeight = mPhotoView.getMeasuredHeight();
                            int finalWidth = mPhotoView.getMeasuredWidth();
//                            mCurrMatrixTv.setX(mPhotoView.getWidth() / 2);
//                            mCurrMatrixTv.setY(mPhotoView.getHeight() / 2);
                            DisplayMetrics displayMetrics = new DisplayMetrics();
                            getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
                            int height = displayMetrics.heightPixels;
                            int width = displayMetrics.widthPixels;
                            return true;
                        }
                    });
                }
                populatePhotoParams(plansPhotoModel);
            } else {
                callGetPlanImageAPI(AddPhotoDirectionActivity.this, plansPhotoModel);
            }
        }
    }


    public static Point getLocationOnScreen(View view) {
        int[] location = new int[2];
        view.getLocationOnScreen(location);
        return new Point(location[0], location[1]);
    }

    private class PhotoTapListener implements OnPhotoTapListener {

        @Override
        public void onPhotoTap(ImageView view, float x, float y) {
            float xPercentage = x * 100f;
            float yPercentage = y * 100f;
//            RelativeLayout.LayoutParams param = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams)  LayoutParams(100, 100, Math.round(xPercentage) , Math.round(yPercentage));

//            crosshair.setLayoutParams(param);
//            crosshair.bringToFront();
//            int my = (int)y;
//            int mx = (int)x;
//
//            crosshair.layout(mx, my, mx+48, my+48);
            //  placeImage(view.getPivotX(),view.getPivotY());

            showToast(String.format(PHOTO_TAP_TOAST_STRING, xPercentage, yPercentage, view == null ? 0 : view.getId()));
        }
    }


    private void placeImage(float X, float Y) {
        int touchX = (int) X;
        int touchY = (int) Y;

        xCrossImage = X;
        yCrossImage = Y;
        //placing at center of touch+50
        int viewWidth = mPhotoView.getWidth();
        int viewHeight = mPhotoView.getHeight();
        viewWidth = viewWidth / 2;
        viewHeight = viewHeight / 2;

//        int width = 160;
//        int height = 160;
//        RelativeLayout.LayoutParams parms = new RelativeLayout.LayoutParams(width,height);
//        crosshair.setLayoutParams(parms);
        // placing at bottom right of touch
//        crosshair.getLayoutParams().height=1260;
//        crosshair.getLayoutParams().width=1260;
//        rl_parent.setX(touchX );
//        rl_parent.setY(touchY );
        rl_parent.setX(touchX );
        rl_parent.setY(touchY );

//        crosshair.layout(touchX, touchY, touchX+48, touchY+48);
//        crosshair.layout(touchX - viewWidth, touchY - viewHeight, touchX + viewWidth, touchY + viewHeight);
        rl_parent.setVisibility(View.VISIBLE);
//        wheel.setVisibility(View.VISIBLE);
    }

    private void showToast(CharSequence text) {
        if (mCurrentToast != null) {
            mCurrentToast.cancel();
        }
        mCurrentToast = Toast.makeText(AddPhotoDirectionActivity.this, text, Toast.LENGTH_SHORT);
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

    private void animate(double fromDegrees, double toDegrees, long durationMillis) {
        final RotateAnimation rotate = new RotateAnimation((float) fromDegrees, (float) toDegrees,
                RotateAnimation.RELATIVE_TO_SELF, 0.5f,
                RotateAnimation.RELATIVE_TO_SELF, 0.5f);
        rotate.setDuration(durationMillis);
        rotate.setFillEnabled(true);
        rotate.setFillAfter(true);
        wheel.startAnimation(rotate);
        System.out.println(mCurrAngle);
    }

    public void populatePhotoParams(PlansPhotoModel photoModel) {
        if (photoModel != null) {
//            if(photoModel.getScale_factor()!=null) {
//                mPhotoView.setScale(Float.valueOf(photoModel.getScale_factor()));
//            }
            if (photoModel.getFocus_point_x() != null && photoModel.getFocus_point_y() != null) {
//                placeImage(Float.valueOf(photoModel.getFocus_point_x()), Float.valueOf(photoModel.getFocus_point_y()));


                ViewTreeObserver vto = mPhotoView.getViewTreeObserver();
                vto.addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
                    public boolean onPreDraw() {
                        mPhotoView.getViewTreeObserver().removeOnPreDrawListener(this);
                        int finalHeight = mPhotoView.getMeasuredHeight();
                        int finalWidth = mPhotoView.getMeasuredWidth();
//                        mCurrMatrixTv.setX(finalWidth/2);
//                        mCurrMatrixTv.setY(finalHeight/2);

//                        rl_parent.setX(Float.valueOf(photoModel.getFocus_point_x()) - 230);
//                        rl_parent.setY(Float.valueOf(photoModel.getFocus_point_y()) - 230);

                        return true;
                    }
                });
            }

            if (photoModel.getArrow_angel() != null) {
                mPhotoView.setRotation(Float.valueOf(photoModel.getArrow_angel()));
            }
        }
    }
}
