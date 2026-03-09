package com.projectdocupro.mobile.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.ViewModelProviders;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;

import com.jaredrummler.android.colorpicker.ColorPickerDialogListener;
import com.projectdocupro.mobile.R;
import com.projectdocupro.mobile.brushViewFiles.PhotoEditor;
import com.projectdocupro.mobile.brushViewFiles.PhotoEditorView;
import com.projectdocupro.mobile.fragments.add_direction.ProjectDocuUtilities;
import com.projectdocupro.mobile.managers.AppConstantsManager;
import com.projectdocupro.mobile.managers.SharedPrefsManager;
import com.projectdocupro.mobile.models.PhotoModel;
import com.projectdocupro.mobile.repos.LocalPhotosRepository;
import com.projectdocupro.mobile.utility.Utils;
import com.projectdocupro.mobile.viewModels.BrushViewModel;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;



public class BrushActivity extends AppCompatActivity implements ColorPickerDialogListener {

    PhotoEditor mPhotoEditor;
    BrushViewModel brushViewModel;

    private PhotoEditorView photoEditorView;

    private Toolbar toolbar;

    private ImageView iv_brush_size_1;

    private ImageView iv_brush_size_2;

    private ImageView iv_brush_size_3;

    private ImageView iv_red_color;

    private ImageView iv_green_color;

    private ImageView iv_blue_color;

    private ImageView iv_yellow_color;

    private ImageView iv_black_color;


    SharedPrefsManager sharedPrefsManager;
    private boolean isPressed = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_brush);
        bindView();

        Utils.showLogger("BrushActivity1");

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

        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        Bitmap bitmap = BitmapFactory.decodeFile(getIntent().getStringExtra("path"), bmOptions);
        try {
            bitmap = getRotatedBitmap(bitmap);
        } catch (IOException e) {
            e.printStackTrace();
        }
        photoEditorView.getSource().setImageBitmap(bitmap);

        mPhotoEditor = new PhotoEditor.Builder(this, photoEditorView)
                .setPinchTextScalable(false)
                .build();
        mPhotoEditor.setBrushDrawingMode(true);

//        new Handler().postDelayed(new Runnable() {
//            @Override
//            public void run() {
//                photoEditorView.setScaleY(2.0f);
//                photoEditorView.setScaleX(2.0f);
//            }
//        },50);


        toolbar.setNavigationOnClickListener(v -> onBackPressed());
        addEvent();

    }

    private void addEvent() {
        mPhotoEditor.setBrushSize(13);
        iv_brush_size_1.setBackground(getResources().getDrawable(R.drawable.btn_rounded_crnor_brush_size_white_background, getApplicationContext().getTheme()));
        iv_brush_size_1.setImageResource(R.drawable.black_line_thin);


        mPhotoEditor.setBrushColor(getResources().getColor(R.color.red_brush));
        iv_red_color.setImageResource(R.drawable.red_border);

        iv_brush_size_1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mPhotoEditor.setBrushSize(13);

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
                mPhotoEditor.setBrushSize(20);
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
                mPhotoEditor.setBrushSize(40);
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
                mPhotoEditor.setBrushColor(getResources().getColor(R.color.red_brush));
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
                mPhotoEditor.setBrushColor(getResources().getColor(R.color.green_brush));

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
                mPhotoEditor.setBrushColor(getResources().getColor(R.color.yellow_brush));

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
                mPhotoEditor.setBrushColor(getResources().getColor(R.color.blue_brush));

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
                mPhotoEditor.setBrushColor(getResources().getColor(R.color.black_brush));

                iv_red_color.setImageResource(R.drawable.red_circle);
                iv_green_color.setImageResource(R.drawable.green_circle);
                iv_blue_color.setImageResource(R.drawable.blue_circle);
                iv_yellow_color.setImageResource(R.drawable.yellow_cricle);
                iv_black_color.setImageResource(R.drawable.black_border);

            }
        });

    }


    private void onBrushSettingsClick() {

        onBackPressed();

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
    private View mBrushSettings;
    private View mBrushColor;

    private void onBrushColorClick() {
        if (!isPressed) {
            saveDuplicatePhotoClick();


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
        mPhotoEditor.setBrushColor(color);
    }

    @Override
    public void onDialogDismissed(int dialogId) {

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.multi_options_action_menu, menu);
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
            saveDuplicatePhotoClick();
            return true;
        } else if (id == R.id.undo_action) {
            mPhotoEditor.undo();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void saveDuplicatePhotoClick() {
        isPressed = true;
        Log.d("pressed","pressed" );
        File dir = this.getExternalFilesDir("/projectDocu/project_paint_photos" + brushViewModel.getProjectId());
        if (dir == null) {
            dir = this.getFilesDir();
        }
        if (!dir.isDirectory()) {
            dir.mkdirs();
        }
        File photo = new File(dir, "/Image_" + new Date().getTime() + ".jpg");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    Activity#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for Activity#requestPermissions for more details.
                return;
            }
        }
        mPhotoEditor.saveAsFile(photo.getAbsolutePath(), new PhotoEditor.OnSaveListener() {
            @Override
            public void onSuccess(@NonNull String imagePath) {

                brushViewModel.getPhotoModel().setPath(imagePath);

                SimpleDateFormat simpleDateFormat = null;

                if (sharedPrefsManager.getStringValue(AppConstantsManager.APP_LANGUAGE, "de").equals("de")) {
                    simpleDateFormat = new SimpleDateFormat("dd-MM-yyyy");
                } else {
                    simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
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
                brushViewModel.getPhotoModel().setPhotoTime(currentTime);
                brushViewModel.getPhotoModel().setCreated_df(created_date);
                brushViewModel.getPhotoModel().setPhotoDate(photoDate);
                brushViewModel.getPhotoModel().setCreated(photoDate);
                brushViewModel.getPhotoModel().setPdphotoid("");
//                brushViewModel.getPhotoModel().setPdphotolocalId(null);
                brushViewModel.getPhotoModel().setQuality(LocalPhotosRepository.MISSING_PHOTO_QUALITY);
                brushViewModel.insert(brushViewModel.getPhotoModel());

                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {

                        if (brushViewModel != null && brushViewModel.getmRepository().getPhotoModel().getPdphotolocalId() > 0) {


                            Intent intent = new Intent(BrushActivity.this, SavePictureActivity.class);
                            intent.putExtra("isBackCamera", true);
                            intent.putExtra("projectId", brushViewModel.getProjectId());
                            intent.putExtra("photoId", brushViewModel.getmRepository().getPhotoModel().getPdphotolocalId());
                            intent.putExtra("path", brushViewModel.getmRepository().getPhotoModel().getPath());
                            intent.putExtra("photoModel", brushViewModel.getmRepository().getPhotoModel());
                            startActivity(intent);

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


            }

            @Override
            public void onFailure(@NonNull Exception exception) {
                isPressed = false;
            }
        });
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

    private void bindView() {
        photoEditorView =   findViewById(R.id.photoEditorView);
        toolbar =   findViewById(R.id.toolbar);
        iv_brush_size_1 =   findViewById(R.id.iv_brush_size_1);
        iv_brush_size_2 =   findViewById(R.id.iv_brush_size_2);
        iv_brush_size_3 =   findViewById(R.id.iv_brush_size_3);
        iv_red_color =   findViewById(R.id.iv_red_color);
        iv_green_color =   findViewById(R.id.iv_green_color);
        iv_blue_color =   findViewById(R.id.iv_blue_color);
        iv_yellow_color =   findViewById(R.id.iv_yellow_color);
        iv_black_color =   findViewById(R.id.iv_black_color);
        mBrushSettings =   findViewById(R.id.brush_settings);
        mBrushColor =   findViewById(R.id.brush_color);
        mBrushSettings.setOnClickListener(v -> {
            onBrushSettingsClick();
        });
        mBrushColor.setOnClickListener(v -> {
            onBrushColorClick();
        });
    }
}
