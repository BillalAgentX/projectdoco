package com.projectdocupro.mobile.activities;

import static com.projectdocupro.mobile.service.SyncLocalPhotosService.SHOW_RESULT;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.widget.ContentLoadingProgressBar;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.projectdocupro.mobile.ProjectNavigator;
import com.projectdocupro.mobile.dao.PhotoDao;
import com.projectdocupro.mobile.fragments.LocalPhotosFragment;
import com.projectdocupro.mobile.fragments.add_direction.ProjectDocuUtilities;
import com.projectdocupro.mobile.models.PhotoModel;
import com.projectdocupro.mobile.receivers.WorkerResultReceiver;
import com.projectdocupro.mobile.repos.LocalPhotosRepository;
import com.projectdocupro.mobile.service.SyncLocalPhotosService;
import com.projectdocupro.mobile.viewModels.LocalPhotosViewModel;

import com.projectdocupro.mobile.ProjectsDatabase;
import com.projectdocupro.mobile.R;
import com.projectdocupro.mobile.interfaces.RetroApiInterface;
import com.projectdocupro.mobile.managers.AppConstantsManager;
import com.projectdocupro.mobile.managers.RetrofitManager;
import com.projectdocupro.mobile.managers.SharedPrefsManager;
import com.projectdocupro.mobile.models.PlansPhotoModel;
import com.projectdocupro.mobile.photoview.PhotoView;
import com.projectdocupro.mobile.repos.PlansPhotoRepository;
import com.projectdocupro.mobile.utility.Utils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Date;
import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PlanDetailsActivity extends AppCompatActivity  {


    String imagePath;

    private Toolbar toolbar;

    private ImageView imageView;

    private View progress_bar_view;

//    @BindView(R.id.scrollView1)
//    FrameLayout scrollView;
//
//    @BindView(R.id.SeaingLayout)
//    ZoomViewCustom zoomViewCustom;

    //    @BindView(R.id.rotation_arrow)
//    ImageView dialer;
//
//    @BindView(R.id.tv_test_data)
//    TextView tvDisplay;
//    @BindView(R.id.iv_photo)
    PhotoView photoView;
    private int dialerHeight, dialerWidth;


    private int finalHeight;
    private int finalWidth;


    private static Bitmap imageOriginal, imageScaled;
    private static Matrix matrix;
    private double currentAngle;
    private float vectorX;
    private float vectorY;
    private ProjectsDatabase database;

    private SharedPrefsManager sharedPrefsManager;


    RelativeLayout pbar;

    ContentLoadingProgressBar progressBar;





    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_plan_details);

        Utils.showLogger("PlanDetailsActivity onCreate");

        pbar = findViewById(R.id.rl_pb_parent);

        progressBar = findViewById(R.id.progressBar);

        sharedPrefsManager = new SharedPrefsManager(this);


        bindView(progress_bar_view);

        new loadPlansImagesAsyncTask(this).execute(getIntent().getExtras().getString("fileId"));


    }

    ////        imageView.setLayoutParams(new ScrollView.LayoutParams(1920, 1542));
////        scrollView.setX(1920);
////        scrollView.setY(1542);
////         ImageView iv=imageView;
////        ViewTreeObserver vto = iv.getViewTreeObserver();
////        vto.addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
////            public boolean onPreDraw() {
////                iv.getViewTreeObserver().removeOnPreDrawListener(this);
////                finalHeight = iv.getMeasuredHeight();
////                finalWidth = iv.getMeasuredWidth();
//////                tv.setText("Height: " + finalHeight + " Width: " + finalWidth);
////                Log.d("IMAGE_DIME","Height: " + finalHeight + " Width: " + finalWidth);
////
////                return true;
////            }
////        });
//
//
//
////        int width = imageView.getDrawable().getIntrinsicWidth();
////        int height = imageView.getDrawable().getIntrinsicHeight();
//////        imageView.setX(width);
//////        imageView.setY(height);
////
////      //  scrollView.setLayoutParams(new ConstraintLayout.LayoutParams(width, height));
////        imageView.setOnTouchListener(new View.OnTouchListener() {
////            @Override
////            public boolean onTouch(View v, MotionEvent event) {
////
////
////                  vectorX = event.getX();
////                 vectorY = event.getY();
////
////                Log.d("Coordinates",event.getX()+"  X   "+event.getY()+"   Y    ");
////                return false;
////            }
////        });
//        tvDisplay.setOnClickListener(new View.OnClickListener() {
//                                         @Override
//                                         public void onClick(View v) {
//                                             tvDisplay.setText(finalWidth/2+" W "+finalHeight/2+" H "+currentAngle+"  angel "+vectorX+" X "+vectorY+" Y ");
//                                         }
//                                     }
//        );
////        View myView =imageView; /* view you want to position offscreen */
////        int amountOffscreen = (int)(myView.getWidth() * 0.8); /* or whatever */
////        boolean offscreen =true ; /* true or false */
////
////
////        int xOffset = (offscreen) ? amountOffscreen : 0;
////        RelativeLayout.LayoutParams rlParams =
////                (RelativeLayout.LayoutParams)myView.getLayoutParams();
////        rlParams.setMargins(-1*xOffset, 0, xOffset, 0);
////        myView.setLayoutParams(rlParams);
//// load the image only once
//        if (imageOriginal == null) {
//            imageOriginal = BitmapFactory.decodeResource(getResources(), R.drawable.rotation_arrow_top);
//        }
//
//        // initialize the matrix only once
//        if (matrix == null) {
//            matrix = new Matrix();
//        } else {
//            // not needed, you can also post the matrix immediately to restore the old state
//            matrix.reset();
//        }
//
//        dialer.setOnTouchListener(new MyOnTouchListener());
//        dialer.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
//
//            @Override
//            public void onGlobalLayout() {
//                // method called more than once, but the values only need to be initialized one time
//                if (dialerHeight == 0 || dialerWidth == 0) {
//                    dialerHeight = dialer.getHeight();
//                    dialerWidth = dialer.getWidth();
//
//                    // resize
//                    Matrix resize = new Matrix();
//                    resize.postScale((float)Math.min(dialerWidth, dialerHeight) / (float)imageOriginal.getWidth(), (float)Math.min(dialerWidth, dialerHeight) / (float)imageOriginal.getHeight());
//                    imageScaled = Bitmap.createBitmap(imageOriginal, 0, 0, imageOriginal.getWidth(), imageOriginal.getHeight(), resize, false);
//                }
//            }
//        });
//
//    }
//
//
//    private class MyOnTouchListener implements View.OnTouchListener {
//
//        private double startAngle;
//
//        @Override
//        public boolean onTouch(View v, MotionEvent event) {
//
//            switch (event.getAction()) {
//
//                case MotionEvent.ACTION_DOWN:
//                    startAngle = getAngle(event.getX(), event.getY());
//                    break;
//
//                case MotionEvent.ACTION_MOVE:
//                     currentAngle = getAngle(event.getX(), event.getY());
//                    rotateDialer((float) (startAngle - currentAngle));
//                    startAngle = currentAngle;
//
//                    Log.d("ANGEL",currentAngle+"");
//
//                    break;
//
//                case MotionEvent.ACTION_UP:
//
//                    break;
//            }
//
//            return true;
//        }
//
//    }
//
//    /**
//     * @return The angle of the unit circle with the image view's center
//     */
//    private double getAngle(double xTouch, double yTouch) {
//        double x = xTouch - (dialerWidth / 2d);
//        double y = dialerHeight - yTouch - (dialerHeight / 2d);
//
//        switch (getQuadrant(x, y)) {
//            case 1:
//                return Math.asin(y / Math.hypot(x, y)) * 180 / Math.PI;
//            case 2:
//                return 180 - Math.asin(y / Math.hypot(x, y)) * 180 / Math.PI;
//            case 3:
//                return 180 + (-1 * Math.asin(y / Math.hypot(x, y)) * 180 / Math.PI);
//            case 4:
//                return 360 + Math.asin(y / Math.hypot(x, y)) * 180 / Math.PI;
//            default:
//                return 0;
//        }
//    }
//
//    /**
//     * @return The selected quadrant.
//     */
//    private static int getQuadrant(double x, double y) {
//        if (x >= 0) {
//            return y >= 0 ? 1 : 4;
//        } else {
//            return y >= 0 ? 2 : 3;
//        }
//    }
//
//    private void rotateDialer(float degrees) {
//        matrix.postRotate(degrees);
//        dialer.setImageBitmap(Bitmap.createBitmap(imageScaled, 0, 0, imageScaled.getWidth(), imageScaled.getHeight(), matrix, true));
//    }
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
                            Utils.showLogger("setBitmapOne");
                            Bitmap bitmap = BitmapFactory.decodeFile(imagePath);
                            imageView.setImageBitmap(bitmap);
                            plansPhotoModel.setPohotPath(imagePath);//Plan detail activity
                            if (!imagePath.equals(""))
                                plansPhotoModel.setPhotoCached(true);//Plan detail activity
                            PlansPhotoRepository plansPhotoRepository = new PlansPhotoRepository(PlanDetailsActivity.this);
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

    @Override
    protected void onDestroy() {

        super.onDestroy();
    }

    private void bindView(View bindSource) {
        toolbar = bindSource.findViewById(R.id.toolbar);
        imageView = bindSource.findViewById(R.id.img_large);
        progress_bar_view = bindSource.findViewById(R.id.progress_bar_view);
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


            if (plansPhotoModel.getPohotPath() != null && !plansPhotoModel.getPohotPath().equals("")) {

                File imgFile = new File(plansPhotoModel.getPohotPath());

                if (imgFile.exists()) {

                    Bitmap myBitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());

                    imageView.setImageBitmap(myBitmap);

                }
            } else {
                callGetPlanImageAPI(PlanDetailsActivity.this, plansPhotoModel);
            }


        }


    }






}
