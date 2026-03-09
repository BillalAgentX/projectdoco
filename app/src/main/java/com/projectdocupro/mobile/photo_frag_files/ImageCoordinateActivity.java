package com.projectdocupro.mobile.photo_frag_files;

import android.app.Activity;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;

import androidx.annotation.Nullable;

import com.projectdocupro.mobile.R;

public class ImageCoordinateActivity extends Activity {

    private ImageView imageView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.photo_coordinate_test);
        imageView = (ImageView) findViewById(R.id.captured_image);
        Drawable drawable = imageView.getDrawable();
        Rect imageBounds = drawable.getBounds();

//original height and width of the bitmap
        int intrinsicHeight = drawable.getIntrinsicHeight();
        int intrinsicWidth = drawable.getIntrinsicWidth();

//height and width of the visible (scaled) image
        int scaledHeight = imageBounds.height();
        int scaledWidth = imageBounds.width();

//Find the ratio of the original image to the scaled image
//Should normally be equal unless a disproportionate scaling
//(e.g. fitXY) is used.
        float heightRatio = intrinsicHeight / scaledHeight;
        float widthRatio = intrinsicWidth / scaledWidth;

//do whatever magic to get your touch point
//MotionEvent event;

//get the distance from the left and top of the image bounds
//        int scaledImageOffsetX = event.getX() - imageBounds.left;
//        int scaledImageOffsetY = event.getY() - imageBounds.top;

//scale these distances according to the ratio of your scaling
//For example, if the original image is 1.5x the size of the scaled
//image, and your offset is (10, 20), your original image offset
//values should be (15, 30).
//        int originalImageOffsetX = scaledImageOffsetX * widthRatio;
//        int originalImageOffsetY = scaledImageOffsetY * heightRatio;
    }

    final float[] getPointerCoords(ImageView view, MotionEvent e) {
        final int index = e.getActionIndex();
        final float[] coords = new float[]{e.getX(index), e.getY(index)};
        Matrix matrix = new Matrix();
        view.getImageMatrix().invert(matrix);
        matrix.postTranslate(view.getScrollX(), view.getScrollY());
        matrix.mapPoints(coords);
        return coords;
    }



    public boolean onTouch(View v, MotionEvent event) {
        ImageView view = (ImageView) v;
        switch (event.getAction() & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN:
                getPointerCoords(imageView, event);
                break;
            case MotionEvent.ACTION_POINTER_DOWN:
                getPointerCoords(imageView, event);

                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_POINTER_UP:
                getPointerCoords(imageView, event);

                break;
            case MotionEvent.ACTION_MOVE:
                getPointerCoords(imageView, event);

                break;


        }
        return true;
    }

}
