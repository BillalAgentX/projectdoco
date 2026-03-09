package com.projectdocupro.mobile.canvasview;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.projectdocupro.mobile.canvasview.utils.DrawAction;
import com.projectdocupro.mobile.canvasview.utils.EraseAction;
import com.projectdocupro.mobile.canvasview.utils.HistoricalAction;
import com.projectdocupro.mobile.canvasview.utils.PenMode;

import java.util.ArrayList;
import java.util.List;

public class CanvasView extends FrameLayout implements View.OnTouchListener {

    public OnRedoEnabled onRedoEnabled;
    private int penMode = PenMode.PEN;
    private float penSize = 50f;

    private Canvas mCanvas;
    private Bitmap mBitmap;

    private Bitmap mBackgroundImgBitmap;

    private Paint mPaint;

    private List<HistoricalAction> history = new ArrayList<>();
    public int curHistoryPtr = -1;

    private ScaleGestureDetector mScaleDetector;

    private int mActivePointerID; // while handling multi touch, we should check PointerID to prevent strokes being smashed.
    private boolean isMultiTouching = false;

    private boolean isZoomEnabled = true;

    private Matrix drawMatrix;
    private float mScaleFactor = 1f;
    private float lastFocusX, lastFocusY;

    private final int eraserSize = 50;
    private ImageView onEraserIcon;
    LayoutParams eraserLayout = new LayoutParams(eraserSize, eraserSize);

    private TextView mText;

    public CanvasView(Context context) {
        super(context);
    }

    public CanvasView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public CanvasView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void initCanvasView() {
        this.setWillNotDraw(false);

        Context mContext = getContext();

        setOnTouchListener(this);

        /* init eraser pointer */
        this.onEraserIcon = new ImageView(mContext);
//        this.onEraserIcon.setImageResource(R.drawable.ic_oneraser_black_24dp);
        this.onEraserIcon.setLayoutParams(eraserLayout);
        this.onEraserIcon.setVisibility(INVISIBLE);
        this.addView(this.onEraserIcon);


        this.mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        this.mPaint.setAntiAlias(true);
        this.mPaint.setStyle(Paint.Style.STROKE);
        this.mPaint.setStrokeWidth(getPenSize());
        this.mPaint.setStrokeCap(Paint.Cap.ROUND);
        this.mPaint.setStrokeJoin(Paint.Join.ROUND);

        this.mPaint.setColor(Color.BLACK);
        this.mPaint.setShadowLayer(0f, 0F, 0F, Color.BLACK);
        this.mPaint.setAlpha(255);
        this.mPaint.setPathEffect(null);

        this.drawMatrix = new Matrix();
        this.mScaleDetector = new ScaleGestureDetector(mContext, new ScaleGestureListener());

        getViewTreeObserver().addOnGlobalLayoutListener(
                new ViewTreeObserver.OnGlobalLayoutListener() {
                    @Override
                    public void onGlobalLayout() {
                        getViewTreeObserver().removeOnGlobalLayoutListener(this);

                        Bitmap init = Bitmap.createBitmap(mBackgroundImgBitmap.getWidth(), mBackgroundImgBitmap.getHeight(), Bitmap.Config.ARGB_8888);

                        mBitmap = init.copy(Bitmap.Config.ARGB_8888, true);

                        mCanvas = new Canvas(mBitmap);
                        init.recycle();
                    }
                });
    }

    private class ScaleGestureListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {
        @Override
        public boolean onScaleBegin(ScaleGestureDetector detector) {
            lastFocusX = detector.getFocusX();
            lastFocusY = detector.getFocusY();
            return super.onScaleBegin(detector);
        }

        @Override
        public void onScaleEnd(ScaleGestureDetector detector) {
            super.onScaleEnd(detector);
            if (mScaleFactor <= 1.0f) {
                mScaleFactor = 1.0f;
                CanvasView.this.drawMatrix = new Matrix();
                invalidate();
            }
        }

        @Override
        public boolean onScale(ScaleGestureDetector detector) {
            if (isMultiTouching && isZoomEnabled) {
                final float scaleFactor = detector.getScaleFactor();

                float[] values = new float[9];
                drawMatrix.getValues(values);

                Matrix transformationMatrix = new Matrix();
                float focusX = detector.getFocusX();
                float focusY = detector.getFocusY();

                float focusShiftX = focusX - lastFocusX;
                float focusShiftY = focusY - lastFocusY;

                /* after translated coordinate */
                float afterX = values[Matrix.MTRANS_X] + (-1 * focusX * scaleFactor + focusX + focusShiftX);
                float afterY = values[Matrix.MTRANS_Y] + (-1 * focusY * scaleFactor + focusY + focusShiftY);

                mScaleFactor *= scaleFactor;

                /* translation coordinate must be 0 if translated coordinate is larger than 0 : fixing top-left coordinate of canvas */
                transformationMatrix.postTranslate(afterX < 0 ? -focusX : 0, afterY < 0 ? -focusY : 0);

                transformationMatrix.postScale(scaleFactor, scaleFactor);

                /* translation coordinate must be 0 if translated coordinate is larger than 0 : fixing top-left coordinate of canvas */
                transformationMatrix.postTranslate(afterX < 0 ? focusX + focusShiftX : 0, afterY < 0 ? focusY + focusShiftY : 0);

                drawMatrix.postConcat(transformationMatrix);

                lastFocusX = focusX;
                lastFocusY = focusY;

                invalidate();
            }
            return true;
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.save();
        if (mBackgroundImgBitmap != null) {
            canvas.drawBitmap(mBackgroundImgBitmap, drawMatrix, null);
        }
        canvas.drawBitmap(this.mBitmap, drawMatrix, null);
        canvas.restore();
        super.onDraw(canvas);
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        mScaleDetector.onTouchEvent(event);

        if(!isZoomEnabled) {
            if (event.getPointerCount() == 1) {
                /* we should translated touch coordinate for translated & scaled canvas */
                Matrix invertMatrix = new Matrix();
                drawMatrix.invert(invertMatrix);

                float[] translated_xy = {event.getX(), event.getY()};
                invertMatrix.mapPoints(translated_xy);

                float p = event.getPressure();

                switch (event.getActionMasked()) {
                    case MotionEvent.ACTION_UP:
                        if (this.penMode == PenMode.ERASER) {
                            onEraserIcon.setVisibility(INVISIBLE);
                            break;
                        }
                        // if pen mode is not eraser, same with ACTION_MOVE
                    case MotionEvent.ACTION_MOVE:

                        if (this.mActivePointerID == event.getPointerId(event.getActionIndex()) ) {

                            for (int i = 0; i < event.getHistorySize(); i++) {
                                float[] hTranslated_xy = {event.getHistoricalX(i), event.getHistoricalY(i)};

                                invertMatrix.mapPoints(hTranslated_xy);
                                if (penMode == PenMode.ERASER) {
                                    onEraserIcon.setX(event.getHistoricalX(i) - eraserSize / 2f);
                                    onEraserIcon.setY(event.getHistoricalY(i) - eraserSize / 2f);
                                    ((EraseAction) this.history.get(curHistoryPtr)).addPoint(hTranslated_xy[0], hTranslated_xy[1]);

                                } else {
                                    ((DrawAction) this.history.get(curHistoryPtr)).addPoint(hTranslated_xy[0], hTranslated_xy[1]);
                                }
                            }

                            if (penMode == PenMode.ERASER) {
                                onEraserIcon.setX(event.getX() - eraserSize / 2f);
                                onEraserIcon.setY(event.getY() - eraserSize / 2f);
                                ((EraseAction) this.history.get(curHistoryPtr)).addPoint(translated_xy[0], translated_xy[1]);

                            } else {
                                ((DrawAction) this.history.get(curHistoryPtr)).addPoint(translated_xy[0], translated_xy[1]);
                            }
                        }
                        break;

                    case MotionEvent.ACTION_DOWN:

                        this.mActivePointerID = event.getPointerId(0); // save first touch pointer
                        this.isMultiTouching = false;
                        if (this.penMode == PenMode.ERASER) {
                            onEraserIcon.setX(event.getX() - eraserSize / 2f);
                            onEraserIcon.setY(event.getY() - eraserSize / 2f);
                            onEraserIcon.setVisibility(VISIBLE);
                            addNewEraseInfo(translated_xy[0], translated_xy[1], p);
                            break;
                        }
                        addNewDrawInfo(translated_xy[0], translated_xy[1], p);
                        break;
                    default:
                        break;
                }

                if (history.size() > 0) {
                    Rect mInvalidateRect = new Rect(
                            (int) (translated_xy[0] - (this.mPaint.getStrokeWidth() * 2)),
                            (int) (translated_xy[1] - (this.mPaint.getStrokeWidth() * 2)),
                            (int) (translated_xy[0] + (this.mPaint.getStrokeWidth() * 2)),
                            (int) (translated_xy[1] + (this.mPaint.getStrokeWidth() * 2)));
                    /* call invalidate(Rect) to renew screen */
                    this.invalidate(mInvalidateRect.left, mInvalidateRect.top, mInvalidateRect.right, mInvalidateRect.bottom);
                } else {
                    invalidate();
                }
            }
        } else {
            this.isMultiTouching = true;
        }

        return true;  /* return true for serial touch event */
    }

    private void addNewEraseInfo(float x, float y, float p) {
        EraseAction eInfo = new EraseAction(x, y, p, this.mPaint, this.mCanvas, this.penMode);
        if (curHistoryPtr >= -1 && this.curHistoryPtr < this.history.size() - 1) {
            this.history = this.history.subList(0, this.curHistoryPtr + 1);
        }
        this.history.add(eInfo);
        this.curHistoryPtr += 1;
        if(onRedoEnabled!=null)
            onRedoEnabled.onRedoEnabled();

    }

    private void addNewDrawInfo(float x, float y, float p) {
        DrawAction dInfo = new DrawAction(x, y, p, this.mPaint, this.mCanvas, this.penMode);
        if (curHistoryPtr >= -1 && this.curHistoryPtr < this.history.size() - 1) {

            this.history = this.history.subList(0, this.curHistoryPtr + 1);
        }
        this.history.add(dInfo);
        this.curHistoryPtr += 1;
        if(onRedoEnabled!=null)
            onRedoEnabled.onRedoEnabled();
    }

    public void undo() {
        // clean up canvas, and re draw & invalidate
        if (this.curHistoryPtr < 0) {
//            Toast.makeText(getContext(), "Cannot Undo", Toast.LENGTH_SHORT).show();
        } else {
            this.mBitmap.eraseColor(Color.TRANSPARENT);
            for (int i = 0; i < curHistoryPtr; i++) {
                ((DrawAction) history.get(i)).redraw();
            }
            this.curHistoryPtr--;
            invalidate();
        }
    }

    public void redo() {
        if (curHistoryPtr < history.size() - 1) {
            curHistoryPtr++;
            if(onRedoEnabled!=null)
                onRedoEnabled.onRedoEnabled();
            this.mBitmap.eraseColor(Color.TRANSPARENT);
            for (int i = 0; i < curHistoryPtr + 1; i++) {
                ((DrawAction) history.get(i)).redraw();
            }
            invalidate();
        } else {
            Toast.makeText(getContext(), "Cannot Redo", Toast.LENGTH_SHORT).show();
        }
    }

    public void setPenColor(int color) {
        mPaint.setColor(color);
    }

    public void setZoomable(boolean isZoomEnabled) {
        this.isZoomEnabled = isZoomEnabled;
    }

    public Bitmap getImage() {
        return overlay(mBackgroundImgBitmap, mBitmap);
    }


    public void changeStrokeWidth(float strokeWidth) {
        CanvasView.this.penSize = (float) strokeWidth;
        if( CanvasView.this.mPaint != null) {
            CanvasView.this.mPaint.setStrokeWidth(getPenSize());
        }
    }

    public static Bitmap overlay(Bitmap bmp1, Bitmap bmp2) {
        try {
            int maxWidth = (Math.max(bmp1.getWidth(), bmp2.getWidth()));
            int maxHeight = (Math.max(bmp1.getHeight(), bmp2.getHeight()));
            Bitmap bmOverlay = Bitmap.createBitmap(maxWidth, maxHeight, bmp1.getConfig());
            Canvas canvas = new Canvas(bmOverlay);
            canvas.drawBitmap(bmp1, 0, 0, null);
            canvas.drawBitmap(bmp2, 0, 0, null);
            return bmOverlay;

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public void setImage(Bitmap bitmapOrg) {
        double imageHeight = bitmapOrg.getHeight();
        double imageWidth = bitmapOrg.getWidth();

        double screenWidth = getWidth();
        double screenHeight = getHeight();

        double newRatio = screenHeight / imageHeight;
        if (imageWidth / imageHeight > screenWidth / screenHeight) {
            newRatio = screenWidth / imageWidth;
        }

        double newRatio1 = newRatio * imageHeight;
        double newRatio2 = newRatio * (imageWidth - 10); // 10 margin in width

        int mainHeight = (int) newRatio1;
        int mainWidth = (int) newRatio2;

        // Here you will get the scaled bitmap
        Bitmap new_ScaledBitmap = Bitmap.createScaledBitmap(bitmapOrg, mainWidth, mainHeight, true);
        // Use this bitmap as wallpaper

        mBackgroundImgBitmap = new_ScaledBitmap.copy(Bitmap.Config.ARGB_8888, true);
    }

    public void setPenMode(int penMode) {
        this.penMode = penMode;
        switch (penMode) {
            case PenMode.ERASER:
                this.mPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
                this.mPaint.setStrokeWidth((float) this.eraserSize);
                break;
            case PenMode.PEN:
                this.mPaint.setXfermode(null);
                this.mPaint.setStrokeWidth(getPenSize());
                break;
            default:
                break;
        }
    }

    private float getPenSize(){
        DisplayMetrics dm = getResources().getDisplayMetrics() ;
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, penSize, dm);
    }

    public interface OnRedoEnabled{
        void onRedoEnabled();
    }
}