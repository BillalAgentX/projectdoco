package com.projectdocupro.mobile.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import com.projectdocupro.mobile.R;
import com.projectdocupro.mobile.models.WordModel;
import com.projectdocupro.mobile.viewModels.RecentUsedWordsViewModel;

import java.io.IOException;
import java.util.List;



public class RecentUsedWordsActivity extends AppCompatActivity {


    RecentUsedWordsViewModel    recentUsedWordsViewModel;

    private ImageView   image;

    private RecyclerView    recyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recent_used_words);
        bindView();
        recentUsedWordsViewModel  =   ViewModelProviders.of(this).get(RecentUsedWordsViewModel.class);
        recentUsedWordsViewModel.setPhotoId(getIntent().getLongExtra("photoId",0L));
        recentUsedWordsViewModel.InitRepo(getIntent().getStringExtra("projectId"),String.valueOf(getIntent().getLongExtra("photoId",0L)));

        try {
            BitmapFactory.Options bmOptions = new BitmapFactory.Options();
            Bitmap bitmap = BitmapFactory.decodeFile(getIntent().getStringExtra("path"),bmOptions);
            bitmap  =   getRotatedBitmap(bitmap,getIntent().getStringExtra("path"));
            image.setImageBitmap(bitmap);
        } catch (IOException e) {
            e.printStackTrace();
        }

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recentUsedWordsViewModel.getWordsList().observe(this, new Observer<List<WordModel>>() {
            @Override
            public void onChanged(List<WordModel> list) {
                recentUsedWordsViewModel.initAdapter(list);
                recyclerView.setAdapter(recentUsedWordsViewModel.getAdapter());
                recentUsedWordsViewModel.getWordsList().removeObserver(this);
            }
        });




    }

    @Override
    public void onBackPressed() {
        setResult(1122);
        finish();
//        super.onBackPressed();

    }

    private Bitmap  getRotatedBitmap(Bitmap    bitmap, String    path) throws IOException {
        ExifInterface ei = new ExifInterface(path);
        int orientation = ei.getAttributeInt(ExifInterface.TAG_ORIENTATION,
                ExifInterface.ORIENTATION_UNDEFINED);
        Bitmap rotatedBitmap = null;
        switch(orientation) {
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
        image = findViewById(R.id.captured_image);
        recyclerView = findViewById(R.id.recent_words_rv);
    }
}
