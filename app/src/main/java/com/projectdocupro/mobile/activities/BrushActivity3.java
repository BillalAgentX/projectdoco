package com.projectdocupro.mobile.activities;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.PointF;
import android.graphics.drawable.GradientDrawable;
import android.media.ExifInterface;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.view.GestureDetector;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.OrientationEventListener;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.ScaleAnimation;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.ViewModelProviders;

import com.jaredrummler.android.colorpicker.ColorPickerDialogListener;
import com.projectdocupro.mobile.R;
import com.projectdocupro.mobile.canvasview.CanvasView;
import com.projectdocupro.mobile.fragments.add_direction.ProjectDocuUtilities;
import com.projectdocupro.mobile.managers.AppConstantsManager;
import com.projectdocupro.mobile.managers.SharedPrefsManager;
import com.projectdocupro.mobile.models.PhotoModel;
import com.projectdocupro.mobile.repos.LocalPhotosRepository;
import com.projectdocupro.mobile.utility.Utils;
import com.projectdocupro.mobile.viewModels.BrushViewModel;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;



public class BrushActivity3 extends AppCompatActivity implements ColorPickerDialogListener, View.OnTouchListener {

    //    PhotoEditor mPhotoEditor;
    BrushViewModel brushViewModel;
    //    @BindView(R.id.photoEditorView)
//    PhotoViewPainting mPhotoEditor;
    private
    Toolbar toolbar;


    private LinearLayout ll_icons;


    private LinearLayout ll_bottom_view;

    private LinearLayout line;


    private ImageView tv_edit;

    private ImageView tv_hold;

    private ImageView iv_brush_size_1;

    private ImageView iv_brush_size_2;

    private ImageView iv_brush_size_3;

    private ImageView iv_red_color;

    private ImageView iv_green_color;

    private ImageView iv_blue_color;

    private ImageView iv_yellow_color;

    private ImageView iv_black_color;


    private RelativeLayout frame;




//    @BindView(R.id.vScroll)
//    ScrollView vScroll;

//    @BindView(R.id.hScroll)
//    HorizontalScrollView hScroll;

    private float mx, my;
    private float curX, curY;
    SharedPrefsManager sharedPrefsManager;
    private boolean isPressed = false;
    private boolean isEditMode = true;

    private static final float MIN_ZOOM = 1f, MAX_ZOOM = 1f;
    private static final String TAG = "MainActivity";

    // These matrices will be used to scale points of the image
    Matrix matrix = new Matrix();
    Matrix savedMatrix = new Matrix();

    // The 3 states (events) which the user is trying to perform
    static final int NONE = 0;
    static final int DRAG = 1;
    static final int ZOOM = 2;
    int mode = NONE;

    // these PointF objects are used to record the point(s) the user is touching
    PointF start = new PointF();
    PointF mid = new PointF();
    float oldDist = 1f;

    private CanvasView mCanvas;
    private String currentPhotoPath;

    Bitmap mutableBitmap;

    private int brushSizeDefault = 10;
    private int brushSizeMedium = 20;
    private int brushSizeLarge = 30;
    private MenuItem lastSelectedRedoMenu;

//    private int brushSizeDefault;
//    private int brushSizeMedium;
//    private int brushSizeLarge;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_brush3);


        bindView();
        Utils.showLogger("BrushActivity3");
        setSupportActionBar(toolbar);
        sharedPrefsManager = new SharedPrefsManager(this);
        brushViewModel = ViewModelProviders.of(this).get(BrushViewModel.class);
        brushViewModel.setProjectId(getIntent().getStringExtra("projectId"));
        brushViewModel.initRepo(brushViewModel.getProjectId());
        brushViewModel.setImagePath(getIntent().getStringExtra("path"));
        brushViewModel.setPhotoModel((PhotoModel) getIntent().getSerializableExtra("photoModel"));
        brushViewModel.setOriginalPhotoId(getIntent().getLongExtra("photoId", 0L));
        Log.d("photoId", brushViewModel.getOriginalPhotoId() + "");
        brushViewModel.getPhotoModel().setPdphotolocalId(0);

        mCanvas = findViewById(R.id.canvasView);
        mCanvas.setLayerType(View.LAYER_TYPE_SOFTWARE, null);

        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        Bitmap bitmap = BitmapFactory.decodeFile(getIntent().getStringExtra("path"), bmOptions);
        try {
            bitmap = getRotatedBitmap(bitmap);
        } catch (IOException e) {
            e.printStackTrace();
        }
        mutableBitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true);


        mCanvas.post(new Runnable() {
            @Override
            public void run() {
                mCanvas.setImage(mutableBitmap);
                mCanvas.initCanvasView();
//                setupBrushSizes();
                addEvent();
            }
        });

        mCanvas.onRedoEnabled = new CanvasView.OnRedoEnabled() {
            @Override
            public void onRedoEnabled() {

                if (lastSelectedRedoMenu != null)
                    enableDisableMenu(lastSelectedRedoMenu, true);
            }
        };


//        mPhotoEditor.setImageBitmap(mutableBitmap);
//        mPhotoEditor.getLayoutParams().width=mutableBitmap.getWidth();
//        mPhotoEditor.getLayoutParams().height=mutableBitmap.getHeight();
//        mPhotoEditor.invalidate();

//        vScroll.getLayoutParams().width=mutableBitmap.getWidth();
//        vScroll.getLayoutParams().height=mutableBitmap.getHeight();
//        vScroll.invalidate();


//        new Handler().postDelayed(new Runnable() {
//            @Override
//            public void run() {
//                photoEditorView.setScaleY(2.0f);
//                photoEditorView.setScaleX(2.0f);
//            }
//        },50);


        toolbar.setNavigationOnClickListener(v -> onBackPressed());

        mCanvas.setZoomable(false);

        tv_edit.setSelected(true);
        tv_hold.setSelected(false);


        new OrientationEventListener(this) {
            @Override
            public void onOrientationChanged(int orientation) {
//                Log.d(TAG, "Orientation New: " + orientation);


                int currentOrientation = getWindowManager().getDefaultDisplay().getRotation();

                switch (currentOrientation) {
                    case 0:
                        //. SCREEN_ORIENTATION_PORTRAIT

                      //  setViewAccordingly(true);
                        //myorientation = MYORIENTATION.PORTRAIT;
                        break;
                    //----------------------------------------
                    case 2:
                        //. SCREEN_ORIENTATION_REVERSE_PORTRAIT

                    //    setViewAccordingly(true);
                        //myorientation = MYORIENTATION.PORTRAIT;
                        break;
                    //----------------------------------------
                    case 1:
                        //. SCREEN_ORIENTATION_LANDSCAPE

                        setViewAccordingly(false);
                        // myorientation = MYORIENTATION.LANDSCAPE;
                        break;
                    //----------------------------------------
                    case 3:
                        //. SCREEN_ORIENTATION_REVERSE_LANDSCAPE
                        setViewAccordingly(false);


                        break;
                    //----------------------------------------
                }

//                deviceOrientation = orientation;
            }
        }.enable();

//        addEvent();

    }

    private void setViewAccordingly(boolean portrait) {
        if (portrait) {
            ll_icons.setOrientation(LinearLayout.HORIZONTAL);
            line.setOrientation(LinearLayout.VERTICAL);





        } else
            ll_icons.setOrientation(LinearLayout.HORIZONTAL);
            line.setOrientation(LinearLayout.HORIZONTAL);
            ll_icons.setOrientation(LinearLayout.VERTICAL);

            ll_bottom_view.setVisibility(View.GONE);

        ViewGroup group = (ViewGroup) ll_icons;
        for (int idx = 0; idx < group.getChildCount(); idx++) {
            LinearLayout.LayoutParams param = new LinearLayout.LayoutParams(
                    convertDpToPx(this,35),
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    1
            );
            group.getChildAt(idx).setLayoutParams(param);
        }


        LinearLayout.LayoutParams param = new LinearLayout.LayoutParams(
                0,
                ViewGroup.LayoutParams.MATCH_PARENT,
                1
        );

        LinearLayout.LayoutParams llParam = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.MATCH_PARENT

        );

        ll_icons.setLayoutParams(llParam);

        frame.setLayoutParams(param);

        mCanvas.invalidate();


    }


    public int convertDpToPx(Context context, float dp) {
        Float f = dp * context.getResources().getDisplayMetrics().density;
        return f.intValue();
    }


    private void addEvent() {
        isEditMode = false;
        editModeActive();
        tv_hold.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mCanvas.setZoomable(true);
                scrollModeActive();
            }
        });
        tv_edit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mCanvas.setZoomable(false);
                editModeActive();
         /*       if (isEditMode) {
                    isEditMode = false;
                    scrollModeActive();
                } else {
                    isEditMode = true;

                    editModeActive();

                }*/

            }
        });

        mCanvas.changeStrokeWidth(brushSizeDefault);
        iv_brush_size_1.setBackground(getResources().getDrawable(R.drawable.btn_rounded_crnor_brush_size_white_background, getApplicationContext().getTheme()));
        iv_brush_size_1.setImageResource(R.drawable.black_line_thin);


//        mPhotoEditor.setBrushColor(getResources().getColor(R.color.red_brush));
        mCanvas.setPenColor(getResources().getColor(R.color.red_brush));
        iv_red_color.setImageResource(R.drawable.red_border);

        iv_brush_size_1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mCanvas.changeStrokeWidth(brushSizeDefault);
                isEditMode = true;
                editModeActive();
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) { //>= API 21
                    iv_brush_size_1.setBackground(getResources().getDrawable(R.drawable.btn_rounded_crnor_brush_size_white_background, getApplicationContext().getTheme()));
                    iv_brush_size_2.setBackgroundColor(getResources().getColor(R.color.transparent));
                    iv_brush_size_3.setBackgroundColor(getResources().getColor(R.color.transparent));
                } else {
                    iv_brush_size_1.setBackground(getResources().getDrawable(R.drawable.btn_rounded_crnor_brush_size_white_background));
                    iv_brush_size_2.setBackgroundColor(getResources().getColor(R.color.transparent));
                    iv_brush_size_3.setBackgroundColor(getResources().getColor(R.color.transparent));
                }
                iv_brush_size_1.setImageResource(R.drawable.black_line_thin);
                iv_brush_size_2.setImageResource(R.drawable.white_line_medium);
                iv_brush_size_3.setImageResource(R.drawable.white_line_thick);
            }
        });

        iv_brush_size_2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isEditMode = true;
                editModeActive();
                mCanvas.changeStrokeWidth(brushSizeMedium);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) { //>= API 21
                    iv_brush_size_2.setBackground(getResources().getDrawable(R.drawable.btn_rounded_crnor_brush_size_white_background, getApplicationContext().getTheme()));
                    iv_brush_size_1.setBackgroundColor(getResources().getColor(R.color.transparent));
                    iv_brush_size_3.setBackgroundColor(getResources().getColor(R.color.transparent));

                } else {
                    iv_brush_size_2.setBackground(getResources().getDrawable(R.drawable.btn_rounded_crnor_brush_size_white_background));
                    iv_brush_size_1.setBackgroundColor(getResources().getColor(R.color.transparent));
                    iv_brush_size_3.setBackgroundColor(getResources().getColor(R.color.transparent));

                }
                iv_brush_size_1.setImageResource(R.drawable.white_line_thin);
                iv_brush_size_2.setImageResource(R.drawable.black_line_medium);
                iv_brush_size_3.setImageResource(R.drawable.white_line_thick);

            }
        });

        iv_brush_size_3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mCanvas.changeStrokeWidth(brushSizeLarge);
                isEditMode = true;
                editModeActive();
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) { //>= API 21
                    iv_brush_size_3.setBackground(getResources().getDrawable(R.drawable.btn_rounded_crnor_brush_size_white_background, getApplicationContext().getTheme()));
                    iv_brush_size_1.setBackgroundColor(getResources().getColor(R.color.transparent));
                    iv_brush_size_2.setBackgroundColor(getResources().getColor(R.color.transparent));

                } else {
                    iv_brush_size_3.setBackground(getResources().getDrawable(R.drawable.btn_rounded_crnor_brush_size_white_background));
                    iv_brush_size_1.setBackgroundColor(getResources().getColor(R.color.transparent));
                    iv_brush_size_2.setBackgroundColor(getResources().getColor(R.color.transparent));

                }
                iv_brush_size_1.setImageResource(R.drawable.white_line_thin);
                iv_brush_size_2.setImageResource(R.drawable.white_line_medium);
                iv_brush_size_3.setImageResource(R.drawable.black_line_thick);

            }
        });


        iv_red_color.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isEditMode = true;
                editModeActive();
//                mPhotoEditor.setBrushColor(getResources().getColor(R.color.red_brush));
                mCanvas.setPenColor(getResources().getColor(R.color.red_brush));
                iv_red_color.setImageResource(R.drawable.red_border);
                iv_green_color.setImageResource(R.drawable.green_circle);
                iv_blue_color.setImageResource(R.drawable.blue_circle);
                iv_yellow_color.setImageResource(R.drawable.yellow_cricle);
                iv_black_color.setImageResource(R.drawable.black_circle);
            }
        });


        iv_green_color.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isEditMode = true;
                editModeActive();
//                mPhotoEditor.setBrushColor(getResources().getColor(R.color.green_brush));
                mCanvas.setPenColor(R.color.green_brush);
                iv_red_color.setImageResource(R.drawable.red_circle);
                iv_green_color.setImageResource(R.drawable.green_border);
                iv_blue_color.setImageResource(R.drawable.blue_circle);
                iv_yellow_color.setImageResource(R.drawable.yellow_cricle);
                iv_black_color.setImageResource(R.drawable.black_circle);

            }
        });
        iv_yellow_color.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isEditMode = true;
                editModeActive();
//                mPhotoEditor.setBrushColor(getResources().getColor(R.color.yellow_brush));
                mCanvas.setPenColor(getResources().getColor(R.color.yellow_brush));
                iv_red_color.setImageResource(R.drawable.red_circle);
                iv_green_color.setImageResource(R.drawable.green_circle);
                iv_blue_color.setImageResource(R.drawable.blue_circle);
                iv_yellow_color.setImageResource(R.drawable.yellow_border);
                iv_black_color.setImageResource(R.drawable.black_circle);
            }
        });
        iv_blue_color.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isEditMode = true;
                editModeActive();
//                mPhotoEditor.setBrushColor(getResources().getColor(R.color.blue_brush));
                mCanvas.setPenColor(getResources().getColor(R.color.blue_brush));
                iv_red_color.setImageResource(R.drawable.red_circle);
                iv_green_color.setImageResource(R.drawable.green_circle);
                iv_blue_color.setImageResource(R.drawable.blue_border);
                iv_yellow_color.setImageResource(R.drawable.yellow_cricle);
                iv_black_color.setImageResource(R.drawable.black_circle);

            }
        });
        iv_black_color.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isEditMode = true;
                editModeActive();
//                mPhotoEditor.setBrushColor(getResources().getColor(R.color.black_brush));
                mCanvas.setPenColor(getResources().getColor(R.color.black_brush));
                iv_red_color.setImageResource(R.drawable.red_circle);
                iv_green_color.setImageResource(R.drawable.green_circle);
                iv_blue_color.setImageResource(R.drawable.blue_circle);
                iv_yellow_color.setImageResource(R.drawable.yellow_cricle);
                iv_black_color.setImageResource(R.drawable.black_border);

            }
        });


        //step 2: create instance from GestureDetector(this step sholude be place into onCreate())
        gestureDetector = new GestureDetector(this, new GestureListener());

// animation for scalling
        mScaleDetector = new ScaleGestureDetector(this, new ScaleGestureDetector.SimpleOnScaleGestureListener() {
            @Override
            public boolean onScale(ScaleGestureDetector detector) {
                float scale = 1 - detector.getScaleFactor();

                float prevScale = mScale;
                mScale += scale;

                if (mScale < 1f) // Minimum scale condition:
                    mScale = 1f;

                if (mScale > 2f) // Maximum scale condition:
                    mScale = 2f;
                ScaleAnimation scaleAnimation = new ScaleAnimation(1f / prevScale, 1f / mScale, 1f / prevScale, 1f / mScale, detector.getFocusX(), detector.getFocusY());
                scaleAnimation.setDuration(0);
                scaleAnimation.setFillAfter(true);
//                ScrollView layout =(ScrollView) findViewById(R.id.scrollViewZoom);
//                vScroll.startAnimation(scaleAnimation);
                return true;
            }
        });


    }


    private void onBrushSettingsClick() {

        onBackPressed();
//        mPhotoEditor.setPaintingModeActive(false);

//        mPhotoEditor.undo();
//        mPhotoEditor.redo();
//        Dialog dialog = new Dialog(this, R.style.Dialog_Theme);
//        dialog.setContentView(R.layout.brush_settings_dialog);
//        dialog.setCancelable(true);
//        AppCompatSeekBar brushSize = dialog.findViewById(R.id.brush_size);
//        AppCompatSeekBar brushOpacity = dialog.findViewById(R.id.brush_opacity);
//
//        LinearLayout ll_size_1 = dialog.findViewById(R.id.ll_size_1);
//        LinearLayout ll_size_2 = dialog.findViewById(R.id.ll_size_2);
//        LinearLayout ll_size_3 = dialog.findViewById(R.id.ll_size_3);
//        LinearLayout ll_size_4 = dialog.findViewById(R.id.ll_size_4);
//        LinearLayout ll_size_5 = dialog.findViewById(R.id.ll_size_5);
//
//        brushSize.setProgress((int) mPhotoEditor.getBrushSize());
//        brushOpacity.setProgress(mPhotoEditor.getOpacity());
//
//
//        ll_size_1.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                mPhotoEditor.setBrushSize(10);
//                dialog.dismiss();
//
//            }
//        });
//        ll_size_2.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                mPhotoEditor.setBrushSize(20);
//                dialog.dismiss();
//            }
//        });
//        ll_size_3.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                mPhotoEditor.setBrushSize(30);
//                dialog.dismiss();
//            }
//        });
//        ll_size_4.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                mPhotoEditor.setBrushSize(40);
//                dialog.dismiss();
//            }
//        });
//        ll_size_5.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                mPhotoEditor.setBrushSize(50);
//                dialog.dismiss();
//            }
//        }); if (wordModels != null && sharedPrefsManager.getBooleanValue(AppConstantsManager.IS_OPEN_FIELD_KEYWORD_CLOCKED, false)) {
//                for (int i = 0; i < wordModels.size(); i++) {
//
//                    if (wordModels.get(i).getType() != null && wordModels.get(i).getType().equals("1")) {
//                        WordModel wordModel = wordModels.get(i);
//                        if (wordModels.get(i).getOpen_field_content() != null && wordModels.get(i).getOpen_field_content().contains(String.valueOf(photoId))) {
//
//                        } else {
//
//                            wordModel.setPhotoIds("," + photoId + "");
//                            if (!wordModel.getOpen_field_content().equals(""))
//                                wordModel.setOpen_field_content(wordModel.getOpen_field_content() + "," + photoId + "##" + wordModel.getValue()
//                                        .toString());
//                            else
//                                wordModel.setOpen_field_content(photoId + "##" + wordModel.getValue()
//                                        .toString());
//                            wordModel.setUseCount(wordModel.getUseCount() + 1);
//                            wordModel.setPhotoIds(wordModel.getPhotoIds() + "," + photoId);
//                            ProjectsDatabase.getDatabase(SavePictureActivity.this).wordDao().update(wordModel);
//                        }
//                    }
//                }
//
//                List<WordModel> wordModelSelected = ProjectsDatabase.getDatabase(SavePictureActivity.this).wordDao().getWordsListIncludesPhotoIdWithTypeZero("%," + photoId + "%", savePictureViewModel.getProjectId());
//                if (wordModelSelected != null && wordModelSelected.size() > 0) {
//                    savePictureViewModel.getPhotoModel().setWordAdded(true);
//                } else {
//                    savePictureViewModel.getPhotoModel().setWordAdded(false);
//                }
//            }
//        dialog.findViewById(R.id.brush_settings_done).setVisibility(View.GONE);
//        dialog.findViewById(R.id.brush_settings_done).setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                dialog.dismiss();
//            }
//        });
//
//        brushSize.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
//            @Override
//            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
//                mPhotoEditor.setBrushSize(progress);
//            }
//
//            @Override
//            public void onStartTrackingTouch(SeekBar seekBar) {
//
//            }
//
//            @Override
//            public void onStopTrackingTouch(SeekBar seekBar) {
//
//            }
//        });
//
//        brushOpacity.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
//            @Override
//            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
//                mPhotoEditor.setOpacity(progress);
//            }
//
//            @Override
//            public void onStartTrackingTouch(SeekBar seekBar) {
//
//            }
//
//            @Override
//            public void onStopTrackingTouch(SeekBar seekBar) {
//
//            }
//        });
//        dialog.show();
    }


    public static final int[] MATERIAL_COLORS = {
            0xFFF44336, // RED 500
            0xFF9C27B0, // PURPLE 500
            0xFF2196F3, // BLUE 500
            0xFF4CAF50, // GREEN 500
            0xFFFFEB3B, // YELLOW 500


    };

    private void onBrushColorClick() {
//        mPhotoEditor.setPaintingModeActive(true);
//        mPhotoEditor.setBrushColor(getResources().getColor(R.color.red_brush));
//        mPhotoEditor.setBrushSize(15f);
        if (!isPressed) {
            saveDuplicatePhotoClick(mCanvas.getImage());


        }

//        Dialog  dialog  =   new Dialog(this);
//        dialog.setContentView(R.layout.brush_color_dialog);
//        dialog.setCancelable(true);
//        ColorPicker colorPicker =   dialog.findViewById(R.id.color_picker);
//        colorPicker.setColorSelectionListener(new SimpleColorSelectionListener(){
//            @Override
//            public void onColorSelected(int color) {
//                // Do whatever you want with the color
//                mPhotoEditor.setBrushColor(color);
//                dialog.dismiss();
//            }
//        });

//        ColorPickerDialog.newBuilder().setPresets(MATERIAL_COLORS).setAllowCustom(false).show(this);

    }

    @Override
    protected void onDestroy() {

        super.onDestroy();
    }

    @Override
    public void onColorSelected(int dialogId, int color) {
//        mPhotoEditor.setBrushColor(color);
    }

    @Override
    public void onDialogDismissed(int dialogId) {

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.multi_options_action_menu, menu);

        enableDisableMenu(menu.getItem(0), false);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.


        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.save_action) {
            saveDuplicatePhotoClick(mCanvas.getImage());
            return true;
        } else if (id == R.id.undo_action) {
//            mPhotoEditor.undo();
//            mPhotoEditor.undoPhotoAndUpdateView();

            mCanvas.undo();
            Utils.showLogger("CursorLengh>>>" + mCanvas.curHistoryPtr);
            if (mCanvas.curHistoryPtr < 0)
                enableDisableMenu(item, false);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void enableDisableMenu(MenuItem item, boolean b) {

        lastSelectedRedoMenu = item;
        int[][] states = new int[][]{
                new int[]{android.R.attr.state_enabled}, // enabled
                new int[]{-android.R.attr.state_enabled}, // disabled
                new int[]{-android.R.attr.state_checked}, // unchecked
                new int[]{android.R.attr.state_pressed}  // pressed
        };


        int color = 0;
        if (b)
            color = Color.WHITE;
        else
            color = Color.GRAY;


        int[] colors = new int[]{
                color,
                color,
                color,
                color
        };

        ColorStateList myList = new ColorStateList(states, colors);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            item.setIconTintList(myList);
        }
    }

    private void saveDuplicatePhotoClick(Bitmap bitmap) {
        isPressed = true;
        Log.d("pressed", "pressed");

        File downDiretory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);


        File dir = new File(downDiretory, "/projectDocu/project_paint_photos" + brushViewModel.getProjectId());

///        File dir = this.getExternalFilesDir("/projectDocu/project_paint_photos" + brushViewModel.getProjectId());
        if (dir == null) {
            dir = this.getFilesDir();
        }
        if (!dir.isDirectory()) {
            dir.mkdirs();
        }

        PhotoModel createDate = brushViewModel.getPhotoModel();
        File photo = new File(dir, "/Image_" + new Date().getTime() + ".jpg");
 /*       if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
              Utils.showLogger("writeExternalStorageNotAllowed");
                // TODO: Consider calling
                //    Activity#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for Activity#requestPermissions for more details.
                return;
            }
        }*/
//        Bitmap originalBitmap=null;
//        BitmapDrawable drawable = (BitmapDrawable)mPhotoEditor.getDrawable();
//        Bitmap originalBitmap = drawable.getBitmap();
//        if(mPhotoEditor.getBitmapArrayListStates()!=null&&mPhotoEditor.getBitmapArrayListStates().size()>0){
//             originalBitmap = mPhotoEditor.getBitmapArrayListStates().get(mPhotoEditor.getBitmapArrayListStates().size()-1);
//        }

        FileOutputStream fOut = null;
        try {
            fOut = new FileOutputStream(photo);

            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fOut);

//            originalBitmap.compress(Bitmap.CompressFormat.JPEG, 100, fOut);
            fOut.flush();
            fOut.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        String imagePath = photo.getAbsolutePath();


/*//        mPhotoEditor.saveAsFile(photo.getAbsolutePath(), new PhotoEditor.OnSaveListener() {
//            @Override
//            public void onSuccess(@NonNull String imagePath) {*/

        brushViewModel.getPhotoModel().setPath(imagePath);

        SimpleDateFormat simpleDateFormat = null;

        if (sharedPrefsManager.getStringValue(AppConstantsManager.APP_LANGUAGE, "de").equals("de")) {
            simpleDateFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
        } else {
            simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        }

        String photoDate = simpleDateFormat.format(new Date());

        long created_date = 0;
        if (photoDate != null && !photoDate.equals("")) {
            created_date = Utils.getCurrentTimeStamp().getTime();
        }
        String currentTime = new SimpleDateFormat("hh:mm", Locale.getDefault()).format(new Date());

        PhotoModel photoModel = new PhotoModel("", brushViewModel.getProjectId(), sharedPrefsManager.getStringValue(AppConstantsManager.PD_USER_ID, ""), ProjectDocuUtilities.givenFile_MD5_Hash(imagePath), "", "", "", photoDate, "", "", photoDate,
                "", "", "", "", "", "", "", photoDate, "", "", "", LocalPhotosRepository.MISSING_PHOTO_QUALITY, "", "", "",
                "", "", "",
                "", created_date, currentTime, LocalPhotosRepository.TYPE_LOCAL_PHOTO);

        brushViewModel.getPhotoModel().setHash(ProjectDocuUtilities.givenFile_MD5_Hash(imagePath));
        brushViewModel.getPhotoModel().setPhoto_type(LocalPhotosRepository.TYPE_LOCAL_PHOTO);
        brushViewModel.getPhotoModel().setPath(imagePath);
        brushViewModel.getPhotoModel().setPohotPath(imagePath);
//                brushViewModel.getPhotoModel().setPhotoTime(currentTime);
        brushViewModel.getPhotoModel().setCreated_df(created_date);
//                brushViewModel.getPhotoModel().setPhotoDate(photoDate);
        brushViewModel.getPhotoModel().setCreated(photoDate);
        brushViewModel.getPhotoModel().setPdphotoid("");
        //brushViewModel.getPhotoModel().setBrushImageAdded(true);//added by billal
//                brushViewModel.getPhotoModel().setPdphotolocalId(null);
        brushViewModel.getPhotoModel().setQuality(LocalPhotosRepository.MISSING_PHOTO_QUALITY);
        brushViewModel.insert(brushViewModel.getPhotoModel());

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {

                if (brushViewModel != null && brushViewModel.getmRepository().getPhotoModel().getPdphotolocalId() > 0) {

                    Intent intent = new Intent(BrushActivity3.this, SavePictureActivity.class);
                    intent.putExtra("isBackCamera", true);
                    intent.putExtra("projectId", brushViewModel.getProjectId());
                    intent.putExtra("photoId", brushViewModel.getmRepository().getPhotoModel().getPdphotolocalId());
                    intent.putExtra("path", brushViewModel.getmRepository().getPhotoModel().getPath());
                    intent.putExtra("photoModel", brushViewModel.getmRepository().getPhotoModel());
                    startActivity(intent);

                    Utils.showLogger("startActivitySavePicture");

                    Intent intentt = new Intent("updateProfile");
                    sendBroadcast(intentt);

                    Intent intent1 = new Intent();
                    intent1.putExtra(SavePictureActivity.SKETCH_ATTACH_TO_PHOTO_KEY, true);
                    setResult(106, intent1);

                    onBackPressed();
                    isPressed = false;
                }
            }
        }, 30);


//            }
//
//            @Override
//            public void onFailure(@NonNull Exception exception) {
//                isPressed = false;
//            }
//        });
    }

    private Bitmap getRotatedBitmap(Bitmap bitmap) throws IOException {
        ExifInterface ei = new ExifInterface(brushViewModel.getImagePath());
        int orientation = ei.getAttributeInt(ExifInterface.TAG_ORIENTATION,
                ExifInterface.ORIENTATION_UNDEFINED);
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

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float curX, curY;

        switch (event.getAction()) {

            case MotionEvent.ACTION_DOWN:
                mx = event.getX();
                my = event.getY();
                break;
            case MotionEvent.ACTION_MOVE:
                curX = event.getX();
                curY = event.getY();
//                if(!mPhotoEditor.isPaintingModeActive()) {
//                    vScroll.scrollBy((int) (mx - curX), (int) (my - curY));
////                    hScroll.scrollBy((int) (mx - curX), (int) (my - curY));
//
//                }
                mx = curX;
                my = curY;
                break;
            case MotionEvent.ACTION_UP:
                curX = event.getX();
                curY = event.getY();
//                if(!mPhotoEditor.isPaintingModeActive()) {
//                    vScroll.scrollBy((int) (mx - curX), (int) (my - curY));
////                    hScroll.scrollBy((int) (mx - curX), (int) (my - curY));
//                }
                break;
        }

        return true;
    }


    // step 1: add some instance
    private float mScale = 1f;
    private ScaleGestureDetector mScaleDetector;
    GestureDetector gestureDetector;
    private View mBrushSettings;
    private View mBrushColor;

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        float eventX = event.getX();
        float eventY = event.getY();
        boolean handled = false;

        if (!isEditMode) {
            ImageView view = (ImageView) v;
            view.setScaleType(ImageView.ScaleType.MATRIX);
            float scale;

            // Handle touch events here...

            switch (event.getAction() & MotionEvent.ACTION_MASK) {
                case MotionEvent.ACTION_DOWN:   // first finger down only
                    matrix.set(view.getImageMatrix());
                    savedMatrix.set(matrix);
                    start.set(event.getX(), event.getY());
                    Log.d(TAG, "mode=DRAG"); // write to LogCat
                    mode = DRAG;
                    break;

                case MotionEvent.ACTION_UP: // first finger lifted

                case MotionEvent.ACTION_POINTER_UP: // second finger lifted

                    mode = NONE;
                    Log.d(TAG, "mode=NONE");
                    break;

                case MotionEvent.ACTION_POINTER_DOWN: // first and second finger down
                    oldDist = spacing(event);
                    Log.d(TAG, "oldDist=" + oldDist);
                    if (oldDist > 5f) {
                        savedMatrix.set(matrix);
                        midPoint(mid, event);
                        mode = ZOOM;
                        Log.d(TAG, "mode=ZOOM");
                    }
                    break;

                case MotionEvent.ACTION_MOVE:

                    if (mode == DRAG) {
                        matrix.set(savedMatrix);
                        matrix.postTranslate(event.getX() - start.x, event.getY() - start.y); // create the transformation in the matrix  of points
                    } else if (mode == ZOOM) {
                        // pinch zooming
                        float newDist = spacing(event);
                        Log.d(TAG, "newDist=" + newDist);
                        if (newDist > 5f) {
                            matrix.set(savedMatrix);
                            scale = newDist / oldDist; // setting the scaling of the
                            // matrix...if scale > 1 means
                            // zoom in...if scale < 1 means
                            // zoom out
                            matrix.postScale(scale, scale, mid.x, mid.y);
                        }
                    }
                    break;
            }


            view.setImageMatrix(matrix);

        }

        return true; // indicate event was handled
    }

    private float spacing(MotionEvent event) {
        float x = event.getX(0) - event.getX(1);
        float y = event.getY(0) - event.getY(1);
        return (float) Math.sqrt(x * x + y * y);
    }

    /*
     * --------------------------------------------------------------------------
     * Method: midPoint Parameters: PointF object, MotionEvent Returns: void
     * Description: calculates the midpoint between the two fingers
     * ------------------------------------------------------------
     */

    private void midPoint(PointF point, MotionEvent event) {
        float x = event.getX(0) + event.getX(1);
        float y = event.getY(0) + event.getY(1);
        point.set(x / 2, y / 2);
    }

    private void bindView() {
        toolbar =  findViewById(R.id.toolbar);
        ll_icons =  findViewById(R.id.ll_icons);
        ll_bottom_view =  findViewById(R.id.ll_bottom_view);
        line =  findViewById(R.id.line);
        tv_edit =  findViewById(R.id.tv_edit);
        tv_hold =  findViewById(R.id.tv_hold);
        iv_brush_size_1 =  findViewById(R.id.iv_brush_size_1);
        iv_brush_size_2 =  findViewById(R.id.iv_brush_size_2);
        iv_brush_size_3 =  findViewById(R.id.iv_brush_size_3);
        iv_red_color =  findViewById(R.id.iv_red_color);
        iv_green_color =  findViewById(R.id.iv_green_color);
        iv_blue_color =  findViewById(R.id.iv_blue_color);
        iv_yellow_color =  findViewById(R.id.iv_yellow_color);
        iv_black_color =  findViewById(R.id.iv_black_color);
        frame =  findViewById(R.id.frame);
        mBrushSettings =  findViewById(R.id.brush_settings);
        mBrushColor =  findViewById(R.id.brush_color);
        mBrushSettings.setOnClickListener(v -> {
            onBrushSettingsClick();
        });
        mBrushColor.setOnClickListener(v -> {
            onBrushColorClick();
        });
    }


//step 4: add private class GestureListener

    private class GestureListener extends GestureDetector.SimpleOnGestureListener {
        @Override
        public boolean onDown(MotionEvent e) {
            return true;
        }

        // event when double tap occurs
        @Override
        public boolean onDoubleTap(MotionEvent e) {
            // double tap fired.
            return true;
        }
    }


    // step 3: override dispatchTouchEvent()
    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        super.dispatchTouchEvent(event);
        mScaleDetector.onTouchEvent(event);
        gestureDetector.onTouchEvent(event);
        return gestureDetector.onTouchEvent(event);
    }

    public void editModeActive() {
//        mPhotoEditor.setPaintingModeActive(isEditMode);
        tv_edit.setSelected(true);
        tv_hold.setSelected(false);
//        vScroll.setEnabled(false);
        ll_icons.setAlpha(1f);
        ll_icons.setEnabled(true);


        enableDisableView(ll_icons, true);
    }

    public void scrollModeActive() {
//        mPhotoEditor.setPaintingModeActive(isEditMode);
        tv_edit.setSelected(false);
        tv_hold.setSelected(true);


        ll_icons.setAlpha(0.5f);

        enableDisableView(ll_icons, false);
//        vScroll.setEnabled(true);
    }


//    private void setupBrushSizes() {
//        brushSizeDefault = (int)(getResources().getDimension(R.dimen.brush_size_one) / getResources().getDisplayMetrics().density);
//        brushSizeMedium = (int) (20 * getResources().getDisplayMetrics().density);
//        brushSizeLarge = (int) (30 * getResources().getDisplayMetrics().density);
//    }


    public static void enableDisableView(View view, boolean enabled) {
        view.setEnabled(enabled);
        if (view instanceof ViewGroup) {
            ViewGroup group = (ViewGroup) view;

            for (int idx = 0; idx < group.getChildCount(); idx++) {
                enableDisableView(group.getChildAt(idx), enabled);
            }
        }
    }

}

