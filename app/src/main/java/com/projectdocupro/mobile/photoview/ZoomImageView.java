package com.projectdocupro.mobile.photoview;

import android.content.Context;
import android.graphics.Matrix;
import android.graphics.PointF;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import androidx.appcompat.widget.AppCompatImageView;

public class ZoomImageView extends AppCompatImageView {

    private Matrix matrix = new Matrix();
    private final float[] matrixValues = new float[9];
    private PointF lastTouch = new PointF();
    private int viewWidth, viewHeight;
    private float origWidth, origHeight;
    private float minScale = 1.0f;
    private float maxScale = 4.0f;
    private ScaleGestureDetector scaleDetector;
    private GestureDetector gestureDetector;

    public ZoomImageView(Context context) {
        super(context);
        init(context);
    }

    public ZoomImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    private void init(Context context) {
        super.setScaleType(ScaleType.MATRIX);

        scaleDetector = new ScaleGestureDetector(context, new ScaleListener());
        gestureDetector = new GestureDetector(context, new GestureListener());

        this.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                scaleDetector.onTouchEvent(event);
                gestureDetector.onTouchEvent(event);

                PointF curr = new PointF(event.getX(), event.getY());

                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        lastTouch.set(curr);
                        break;
                    case MotionEvent.ACTION_MOVE:
                        if (!scaleDetector.isInProgress()) {
                            float deltaX = curr.x - lastTouch.x;
                            float deltaY = curr.y - lastTouch.y;
                            pan(deltaX, deltaY);
                            lastTouch.set(curr.x, curr.y);
                        }
                        break;
                }
                return true;
            }
        });
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        viewWidth = MeasureSpec.getSize(widthMeasureSpec);
        viewHeight = MeasureSpec.getSize(heightMeasureSpec);

        // Reset image on rotation
        if (getDrawable() != null) {
            resetImage();
        }
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        resetImage();
    }

    private void resetImage() {
        Drawable drawable = getDrawable();
        if (drawable == null) return;

        origWidth = drawable.getIntrinsicWidth();
        origHeight = drawable.getIntrinsicHeight();

        float scale = Math.min(
                viewWidth / origWidth,
                viewHeight / origHeight
        );

        minScale = scale;
        maxScale = minScale * 4;

        matrix.reset();
        matrix.postScale(scale, scale);
        centerImage();
        setImageMatrix(matrix);
    }

    private void centerImage() {
        float[] matrixValues = new float[9];
        matrix.getValues(matrixValues);
        float scale = matrixValues[Matrix.MSCALE_X];

        float scaledWidth = origWidth * scale;
        float scaledHeight = origHeight * scale;

        float transX = (viewWidth - scaledWidth) / 2;
        float transY = (viewHeight - scaledHeight) / 2;

        matrix.postTranslate(transX, transY);
    }

    private void pan(float deltaX, float deltaY) {
        float[] values = new float[9];
        matrix.getValues(values);
        float scale = values[Matrix.MSCALE_X];

        float scaledWidth = origWidth * scale;
        float scaledHeight = origHeight * scale;

        // Calculate current translation boundaries
        float minTransX = -(scaledWidth - viewWidth);
        float maxTransX = 0;
        float minTransY = -(scaledHeight - viewHeight);
        float maxTransY = 0;

        float transX = values[Matrix.MTRANS_X] + deltaX;
        float transY = values[Matrix.MTRANS_Y] + deltaY;

        // Apply boundaries
        if (scaledWidth > viewWidth) {
            transX = Math.max(minTransX, Math.min(transX, maxTransX));
        } else {
            transX = (viewWidth - scaledWidth) / 2;
        }

        if (scaledHeight > viewHeight) {
            transY = Math.max(minTransY, Math.min(transY, maxTransY));
        } else {
            transY = (viewHeight - scaledHeight) / 2;
        }

        matrix.setScale(scale, scale);
        matrix.postTranslate(transX, transY);
        setImageMatrix(matrix);
    }

    private class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {
        @Override
        public boolean onScale(ScaleGestureDetector detector) {
            float scaleFactor = detector.getScaleFactor();
            float currentScale = getScale();
            float newScale = currentScale * scaleFactor;

            // Apply scale limits
            if (newScale < minScale) newScale = minScale;
            if (newScale > maxScale) newScale = maxScale;

            float focusX = detector.getFocusX();
            float focusY = detector.getFocusY();

            matrix.postScale(
                    newScale / currentScale,
                    newScale / currentScale,
                    focusX,
                    focusY
            );

            // Prevent empty space around image
            adjustTranslation();
            setImageMatrix(matrix);
            return true;
        }
    }

    private class GestureListener extends GestureDetector.SimpleOnGestureListener {
        @Override
        public boolean onDoubleTap(MotionEvent e) {
            float currentScale = getScale();
            float targetScale = (currentScale == minScale) ? maxScale : minScale;

            matrix.postScale(
                    targetScale / currentScale,
                    targetScale / currentScale,
                    e.getX(),
                    e.getY()
            );

            adjustTranslation();
            setImageMatrix(matrix);
            return true;
        }
    }

    private float getScale() {
        matrix.getValues(matrixValues);
        return matrixValues[Matrix.MSCALE_X];
    }

    public void setScale(float scale) {
        setZoomScale(scale, viewWidth / 2f, viewHeight / 2f);
    }

    public void setZoomScale(float scale, float focusX, float focusY) {
        float currentScale = getCurrentScale();
        scale = Math.max(minScale, Math.min(scale, maxScale));
        float scaleFactor = scale / currentScale;

        matrix.postScale(scaleFactor, scaleFactor, focusX, focusY);
        adjustTranslation();
        setImageMatrix(matrix);
    }


    public float getCurrentScale() {
        matrix.getValues(matrixValues);
        return matrixValues[Matrix.MSCALE_X];
    }

    public void resetZoom() {
        resetImage();
    }

    private void adjustTranslation() {
        float[] values = new float[9];
        matrix.getValues(values);
        float scale = values[Matrix.MSCALE_X];

        float scaledWidth = origWidth * scale;
        float scaledHeight = origHeight * scale;

        // Calculate translation boundaries
        float transX = values[Matrix.MTRANS_X];
        float transY = values[Matrix.MTRANS_Y];

        float minTransX = -(scaledWidth - viewWidth);
        float maxTransX = 0;
        float minTransY = -(scaledHeight - viewHeight);
        float maxTransY = 0;

        // Adjust X translation
        if (scaledWidth < viewWidth) {
            transX = (viewWidth - scaledWidth) / 2;
        } else {
            transX = Math.max(minTransX, Math.min(transX, maxTransX));
        }

        // Adjust Y translation
        if (scaledHeight < viewHeight) {
            transY = (viewHeight - scaledHeight) / 2;
        } else {
            transY = Math.max(minTransY, Math.min(transY, maxTransY));
        }

        matrix.postTranslate(transX - values[Matrix.MTRANS_X], transY - values[Matrix.MTRANS_Y]);
    }
}