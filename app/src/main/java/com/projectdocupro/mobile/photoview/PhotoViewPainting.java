/*
 Copyright 2011, 2012 Chris Banes.
 <p>
 Licensed under the Apache License, Version 2.0 (the "License");
 you may not use this file except in compliance with the License.
 You may obtain a copy of the License at
 <p>
 http://www.apache.org/licenses/LICENSE-2.0
 <p>
 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.
 */
package com.projectdocupro.mobile.photoview;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.GestureDetector;
import android.view.ScaleGestureDetector;
import android.view.View;

import com.projectdocupro.mobile.R;

import java.util.ArrayList;
import java.util.List;

import androidx.appcompat.widget.AppCompatImageView;


/**
 * A zoomable ImageView. See {@link PhotoViewAttacher} for most of the details on how the zooming
 * is accomplished
 */
@SuppressWarnings("unused")
public class PhotoViewPainting extends AppCompatImageView {

    public static Context mcontext = null;
    private PhotoViewAttacherPainting attacher;
    private ScaleType pendingScaleType;
    private Bitmap canvasBitmap;
    private Canvas drawCanvas = null;


    //A matrix object
    private Matrix m = new Matrix();
    private Bitmap orignalBitmap;
    private int viewHeigh;
    private int viewWidth;

    public ArrayList<Bitmap> getBitmapArrayListStates() {
        return bitmapArrayListStates;
    }

    public void setBitmapArrayListStates(ArrayList<Bitmap> bitmapArrayListStates) {
        this.bitmapArrayListStates = bitmapArrayListStates;
    }

    private ArrayList<Bitmap> bitmapArrayListStates = new ArrayList<>();

    public PhotoViewPainting(Context context) {
        this(context, null);
    }

    public PhotoViewPainting(Context context, AttributeSet attr) {
        this(context, attr, 0);
    }

    public PhotoViewPainting(Context context, AttributeSet attr, int defStyle) {
        super(context, attr, defStyle);
        mcontext = context;
        init();

//        Bitmap mutableBitmap = workingBitmap.copy(Bitmap.Config.ARGB_8888, true);


    }

    private void init() {

        attacher = new PhotoViewAttacherPainting(this);
        holderList.add(new Holder(color, width));
        //We always pose as a Matrix scale type, though we can change to another scale type
        //via the attacher
        super.setScaleType(ScaleType.MATRIX);
        //apply the previously applied scale type
        if (pendingScaleType != null) {
            setScaleType(pendingScaleType);
            pendingScaleType = null;
        }
    }

    /**
     * Get the current {@link PhotoViewAttacher} for this view. Be wary of holding on to references
     * to this attacher, as it has a reference to this view, which, if a reference is held in the
     * wrong place, can cause memory leaks.
     *
     * @return the attacher.
     */
    public PhotoViewAttacherPainting getAttacher() {
        return attacher;
    }

    @Override
    public ScaleType getScaleType() {
        return attacher.getScaleType();
    }

    @Override
    public Matrix getImageMatrix() {
        return attacher.getImageMatrix();
    }

    @Override
    public void setOnLongClickListener(OnLongClickListener l) {
        attacher.setOnLongClickListener(l);
    }


    @Override
    public void setOnClickListener(OnClickListener l) {
        attacher.setOnClickListener(l);
    }

    @Override
    public void setScaleType(ScaleType scaleType) {
        if (attacher == null) {
            pendingScaleType = scaleType;
        } else {
            attacher.setScaleType(scaleType);
        }
    }

    @Override
    public void setImageDrawable(Drawable drawable) {
        super.setImageDrawable(drawable);
        // setImageBitmap calls through to this method
        if (attacher != null) {
            attacher.update();
        }
    }

    @Override
    public void setImageResource(int resId) {
        super.setImageResource(resId);
        if (attacher != null) {
            attacher.update();
        }
    }

    @Override
    public void setImageURI(Uri uri) {
        super.setImageURI(uri);
        if (attacher != null) {
            attacher.update();
        }
    }

    @Override
    protected boolean setFrame(int l, int t, int r, int b) {
        boolean changed = super.setFrame(l, t, r, b);
        if (changed) {
            attacher.update();
        }
        return changed;
    }

    public void setRotationTo(float rotationDegree) {
        attacher.setRotationTo(rotationDegree);
    }

    public void setRotationBy(float rotationDegree) {
        attacher.setRotationBy(rotationDegree);
    }

    public boolean isZoomable() {
        return attacher.isZoomable();
    }

    public void setZoomable(boolean zoomable) {
        attacher.setZoomable(zoomable);
    }

    public RectF getDisplayRect() {
        return attacher.getDisplayRect();
    }

    public void getDisplayMatrix(Matrix matrix) {
        attacher.getDisplayMatrix(matrix);
    }

    @SuppressWarnings("UnusedReturnValue")
    public boolean setDisplayMatrix(Matrix finalRectangle) {
        return attacher.setDisplayMatrix(finalRectangle);
    }

    public void getSuppMatrix(Matrix matrix) {
        attacher.getSuppMatrix(matrix);
    }

    public boolean setSuppMatrix(Matrix matrix) {
        return attacher.setDisplayMatrix(matrix);
    }

    public float getMinimumScale() {
        return attacher.getMinimumScale();
    }

    public float getMediumScale() {
        return attacher.getMediumScale();
    }

    public float getMaximumScale() {
        return attacher.getMaximumScale();
    }

    public float getScale() {
        return attacher.getScale();
    }

    public void setAllowParentInterceptOnEdge(boolean allow) {
        attacher.setAllowParentInterceptOnEdge(allow);
    }

    public void setMinimumScale(float minimumScale) {
        attacher.setMinimumScale(minimumScale);
    }

    public void setMediumScale(float mediumScale) {
        attacher.setMediumScale(mediumScale);
    }

    public void setMaximumScale(float maximumScale) {
        attacher.setMaximumScale(maximumScale);
    }

    public void setScaleLevels(float minimumScale, float mediumScale, float maximumScale) {
        attacher.setScaleLevels(minimumScale, mediumScale, maximumScale);
    }

    public void setOnMatrixChangeListener(OnMatrixChangedListener listener) {
        attacher.setOnMatrixChangeListener(listener);
    }

    public void setOnPhotoTapListener(OnPhotoTapListener listener) {
        attacher.setOnPhotoTapListener(listener);
    }

    public void setOnOutsidePhotoTapListener(OnOutsidePhotoTapListener listener) {
        attacher.setOnOutsidePhotoTapListener(listener);
    }

    public void setOnViewTapListener(OnViewTapListener listener) {
        attacher.setOnViewTapListener(listener);
    }

    public void setOnViewDragListener(OnViewDragListener listener) {
        attacher.setOnViewDragListener(listener);
    }

    public void setScale(float scale) {
        attacher.setScale(scale);
    }

    public void setScale(float scale, boolean animate) {
        attacher.setScale(scale, animate);
    }

    public void setScale(float scale, float focalX, float focalY, boolean animate) {
        attacher.setScale(scale, focalX, focalY, animate);
    }

    public void setZoomTransitionDuration(int milliseconds) {
        attacher.setZoomTransitionDuration(milliseconds);
    }

    public void setOnDoubleTapListener(GestureDetector.OnDoubleTapListener onDoubleTapListener) {
        attacher.setOnDoubleTapListener(onDoubleTapListener);
    }

    public void setOnScaleChangeListener(OnScaleChangedListener onScaleChangedListener) {
        attacher.setOnScaleChangeListener(onScaleChangedListener);
    }

    public void setOnSingleFlingListener(OnSingleFlingListener onSingleFlingListener) {
        attacher.setOnSingleFlingListener(onSingleFlingListener);
    }

    boolean isMerge = false;
    boolean isDrawCalled = false;

    Canvas myCanvas = null;

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        viewHeigh = getHeight();
        viewWidth = getWidth();

        //if(myCanvas==null)
//           if(bitmapArrayListStates!=null&&bitmapArrayListStates.size()==1){
//               myCanvas = new Canvas(orignalBitmap);
//               bitmapArrayListStates.set(0,orignalBitmap);
//           }else
        if (bitmapArrayListStates.size() > 1) {
            BitmapDrawable drawable = (BitmapDrawable) attacher.mImageView.getDrawable();
            Bitmap bitmap = drawable.getBitmap();
            myCanvas = new Canvas(bitmap);
        } else if (bitmapArrayListStates.size() == 1) {

            Bitmap bitmap = Bitmap.createBitmap(orignalBitmap);
            myCanvas = new Canvas(bitmap);
            bitmapArrayListStates.set(0, orignalBitmap);
            attacher.mImageView.setImageBitmap(bitmap);
        }


        for (Holder holder : holderList) {
//            canvas.drawPath(holder.path, holder.paint);
            myCanvas.drawPath(holder.path, holder.paint);
        }


//        canvas.drawBitmap(orignalBitmap, 0, 0,null);


//        drawCanvas=canvas;
//        drawCanvas.drawLine(250, 250, 500, 1800, new Holder(getColor(),getPaintWidth()).paint);

        //  canvas.drawBitmap(mBitmap, 0, 0, null);
        // Create a circular path.
//        final float halfWidth = canvas.getWidth()/2;
//        final float halfHeight = canvas.getHeight()/2;
//        final float radius = Math.max(halfWidth, halfHeight);
//        final Path path = new Path();
//        path.addCircle(halfWidth, halfHeight, radius, Path.Direction.CCW);
//
//        final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
//        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_IN));
//        canvas.drawPath(path, paint);

        /*for (Holder holder : holderList) {
            canvas.drawPath(holder.path, holder.paint);
        }*/

      /*  for (Holder holder : holderList) {
//     canvas.drawBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.ic_launcher), 0, 0, holder.paint);
//
//     canvas.drawBitmap(canvasBitmap, 0, 0, holder.paint);
            if(drawCanvas!=null)
                drawCanvas.drawPath(holder.path, holder.paint);
//     drawCanvas.drawLine(bitmapArrayListStates.get(0),holder.eventX,holder.eventY, holder.paint);
        }*/

//        canvas.setBitmap(workingBitmap);
        if (!isDrawCalled) {

//            drawCanvas.drawColor(R.color.orange);
            isDrawCalled = true;
        }
//        Log.d("bitmap_size",workingBitmap.getWidth()+" "+workingBitmap.getHeight());
//        Log.d("canvas_size",canvas.getWidth()+" "+canvas.getHeight());

//        canvas.drawPath(drawPath, drawPaint);

 /*for (Holder holder : holderList) {
//     canvas.drawBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.ic_launcher), 0, 0, holder.paint);
//
//     canvas.drawBitmap(canvasBitmap, 0, 0, holder.paint);
     if(drawCanvas!=null)
         drawCanvas.drawPath(holder.path, holder.paint);
//     drawCanvas.drawLine(bitmapArrayListStates.get(0),holder.eventX,holder.eventY, holder.paint);
        }*/

//        canvas.drawBitmap(canvasBitmap, 0, 0, null);
        if (isDrawCalled) {
//            Bitmap bm= getBitmap();
//            takeScreenshotForView(this);
//            isDrawCalled=false;
        }


//        int x=200;
//        int y=200;
//        int radius=40;
//        Paint paint=new Paint();
//        // Use Color.parseColor to define HTML colors
//        paint.setColor(Color.parseColor("#CD5C5C"));
//        canvas.drawCircle(x, y, radius, paint);

//        if(!isMerge){
//            BitmapDrawable drawable = (BitmapDrawable) attacher.mImageView.getDrawable();
//            Bitmap bitmap = drawable.getBitmap();
//            Bitmap smallImage = BitmapFactory.decodeResource(getResources(), R.drawable.green_circle_selected);
//            Bitmap largeImage = BitmapFactory.decodeResource(getResources(), R.drawable.green_circle_selected);
//
//            attacher.mImageView.setImageBitmap( createSingleImageFromMultipleImages(bitmap,smallImage));
//            isMerge=true;
//        }


    }

    public Bitmap takeScreenshotForView(View view) {
        view.measure(MeasureSpec.makeMeasureSpec(view.getWidth(), MeasureSpec.EXACTLY), MeasureSpec.makeMeasureSpec(view.getHeight(), MeasureSpec.EXACTLY));
        view.layout((int) view.getX(), (int) view.getY(), (int) view.getX() + view.getMeasuredWidth(), (int) view.getY() + view.getMeasuredHeight());

        view.setDrawingCacheEnabled(true);
        view.buildDrawingCache(true);
        Bitmap bitmap = Bitmap.createBitmap(view.getDrawingCache());
        view.setDrawingCacheEnabled(false);

        return bitmap;
    }

    private Bitmap createSingleImageFromMultipleImages(Bitmap bigImage, Bitmap smallImage) {

        Bitmap result = Bitmap.createBitmap(bigImage.getWidth(), bigImage.getHeight(), bigImage.getConfig());
        Canvas canvas = new Canvas(result);

//        Bitmap resultn=   scaleDown(bigImage,flag.scale_factor,false);

        canvas.drawBitmap(bigImage, 0, 0
                , null);
//        canvas.drawBitmap(smallImage, 21, 254, null);

//        EnhancedWebView enhancedWebView= new EnhancedWebView(getActivity());
//        enhancedWebView. planResizeFactor=flag.scale_factor;
//        enhancedWebView.calculateSizes(flag.xcoord,flag.ycoord,true);
        Matrix matrix = new Matrix();
        matrix.reset();
        matrix.postTranslate((bigImage.getWidth() / 2), (bigImage.getHeight() / 2)); // Centers image
        matrix.postRotate(0);
//        matrix.postScale()
        canvas.drawBitmap(smallImage, (bigImage.getWidth() / 2), (bigImage.getHeight() / 2), null);

        Bitmap orgImage = BitmapFactory.decodeResource(getResources(), R.drawable.yellow_circle_selected);

        canvas.drawBitmap(orgImage, (bigImage.getWidth() / 2), (bigImage.getHeight() / 2), null);
        return result;
    }


    private int color = Color.BLACK;
    private float width = 4f;
    private boolean isPaintingModeActive = false;

    public boolean isPaintingModeActive() {
        return isPaintingModeActive;
    }

    public void setPaintingModeActive(boolean paintingModeActive) {
        isPaintingModeActive = paintingModeActive;
        attacher.isPaintingModeActive = paintingModeActive;
    }

    public List<Holder> holderList = new ArrayList<Holder>();

    public static class Holder {
        private float arcLeft = 0f;
        private float arcTop = 0f;
        private float arcRight = 0f;
        private float arcBottom = 0f;
        Path path;
        float eventX = 0f;
        float eventY = 0f;

        public void setPath(Path path) {
            this.path = path;
        }

        public void setPaint(Paint paint) {
            this.paint = paint;
        }

        Paint paint;

        Holder(int color, float width) {
            path = new Path();
            paint = new Paint();
            paint.setAntiAlias(true);
            paint.setStrokeWidth(width);
            paint.setColor(color);
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeJoin(Paint.Join.ROUND);
            paint.setStrokeCap(Paint.Cap.ROUND);

        }

        Holder(int color, float width, float x, float y) {
            path = new Path();
            paint = new Paint();
            paint.setAntiAlias(true);
            paint.setStrokeWidth(width);
            paint.setColor(color);
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeJoin(Paint.Join.ROUND);
            paint.setStrokeCap(Paint.Cap.ROUND);
            eventX = x;
            eventY = y;


            DisplayMetrics displayMetrics = new DisplayMetrics();

            ((Activity) mcontext).getWindowManager()
                    .getDefaultDisplay()
                    .getMetrics(displayMetrics);


            int screenWidth = displayMetrics.widthPixels;
            int screenHeight = displayMetrics.heightPixels;

            arcLeft = pxFromDp(mcontext, 20);
            arcTop = pxFromDp(mcontext, 20);
            arcRight = pxFromDp(mcontext, 100);
            arcBottom = pxFromDp(mcontext, 100);
        }
    }

    public void resetPaths() {
        for (Holder holder : holderList) {
            holder.path.reset();
        }
        invalidate();
    }

    public void setBrushColor(int color) {
        this.color = color;
    }

    public void setBrushSize(float width) {
        this.width = width;
    }

    public int getColor() {
        return color;
    }


    public float getPaintWidth() {
        return width;
    }


    public Bitmap getBitmap() {
        //this.measure(100, 100);
        //this.layout(0, 0, 100, 100);
        this.setDrawingCacheEnabled(true);
        this.buildDrawingCache();
        Bitmap bmp = Bitmap.createBitmap(attacher.mImageView.getDrawingCache());
        this.setDrawingCacheEnabled(false);


        return bmp;
    }

    @Override
    public void setImageBitmap(Bitmap bm) {
//        orignalBitmap=bm;
        super.setImageBitmap(bm);
        if (bitmapArrayListStates.size() == 0) {
            Bitmap resultt = Bitmap.createBitmap(bm);
            bitmapArrayListStates.add(resultt);
            orignalBitmap = resultt;
        }


//        if(drawCanvas==null){
//            drawCanvas = new Canvas(orignalBitmap);
//        }
    }

    public void capturePhotoAndUpdateView() {

        if (bitmapArrayListStates != null && bitmapArrayListStates.size() > 0) {
            BitmapDrawable drawable = (BitmapDrawable) attacher.mImageView.getDrawable();
            Bitmap bitmap = drawable.getBitmap();

         /*   for (Holder holder : holderList) {
//     canvas.drawBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.ic_launcher), 0, 0, holder.paint);
//
//     canvas.drawBitmap(canvasBitmap, 0, 0, holder.paint);
                if(drawCanvas!=null)
                    drawCanvas.drawPath(holder.path, holder.paint);
//     drawCanvas.drawLine(bitmapArrayListStates.get(0),holder.eventX,holder.eventY, holder.paint);
            }*/
//            Bitmap immutableBitmap=null;
//
//                immutableBitmap = bitmap.copy(Bitmap.Config.ARGB_8888, false);
            Bitmap result = Bitmap.createBitmap(bitmap);
            if (result != null)
                bitmapArrayListStates.add(result);
            setImageBitmap(bitmapArrayListStates.get(bitmapArrayListStates.size() - 1));
        }
//        Toast.makeText(getContext(),bitmapArrayListStates.size()+" Size",Toast.LENGTH_SHORT).show();
        resetPaths();

    }

    public void undoPhotoAndUpdateView() {

        if (bitmapArrayListStates != null && bitmapArrayListStates.size() > 1) {
            bitmapArrayListStates.remove(bitmapArrayListStates.size() - 1);
//            if (bitmapArrayListStates != null && bitmapArrayListStates.size() > 1)
//                bitmapArrayListStates.remove(bitmapArrayListStates.size() - 1);

            setImageBitmap(bitmapArrayListStates.get(bitmapArrayListStates.size() - 1));
        } else if (bitmapArrayListStates.size() == 1) {
            bitmapArrayListStates.set(0, orignalBitmap);
            setImageBitmap(bitmapArrayListStates.get(bitmapArrayListStates.size() - 1));
        }
//        Toast.makeText(getContext(),bitmapArrayListStates.size()+" Size",Toast.LENGTH_SHORT).show();
        resetPaths();

    }

    //size assigned to view
    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
//        BitmapDrawable drawable = (BitmapDrawable) attacher.mImageView.getDrawable();
//        Bitmap bitmap = drawable.getBitmap();
//        canvasBitmap = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Bitmap.Config.ARGB_8888);
//        canvasBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
//        drawCanvas = new Canvas(canvasBitmap);
//        drawCanvas.drawColor(R.color.orange_color);
    }

    public static float pxFromDp(final Context context, final float dp) {
        return dp * context.getResources().getDisplayMetrics().density;
    }

    private class ScalingListener extends ScaleGestureDetector.SimpleOnScaleGestureListener{
        @Override
        public boolean onScaleBegin(ScaleGestureDetector detector) {
            return super.onScaleBegin(detector);
        }

        @Override
        public boolean onScale(ScaleGestureDetector detector) {
            return super.onScale(detector);
        }
    }


}
