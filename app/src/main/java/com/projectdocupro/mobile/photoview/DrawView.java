package com.projectdocupro.mobile.photoview;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;

import java.util.ArrayList;
import java.util.List;

import androidx.appcompat.widget.AppCompatImageView;

public class DrawView extends AppCompatImageView implements GestureDetector.OnDoubleTapListener {

    private int color = Color.BLACK;
    private float width = 4f;
    private List<Holder> holderList = new ArrayList<Holder>();
    private Bitmap orignalBitmap;
    private Canvas drawCanvas=null;
    private int viewHeigh;
    private int viewWidth;

    public ArrayList<Bitmap> getBitmapArrayListStates() {
        return bitmapArrayListStates;
    }

    public void setBitmapArrayListStates(ArrayList<Bitmap> bitmapArrayListStates) {
        this.bitmapArrayListStates = bitmapArrayListStates;
    }

    private ArrayList<Bitmap> bitmapArrayListStates = new ArrayList<>();

    @Override
    public boolean onSingleTapConfirmed(MotionEvent e) {
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

    private class Holder {
        Path path;

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
    }

    public DrawView(Context context) {
        super(context);
        init();
    }

    public DrawView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public DrawView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    private void init() {
        holderList.add(new Holder(color, width));
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);


        Canvas myCanvas = new Canvas(orignalBitmap);
        for (Holder holder : holderList) {
            myCanvas.drawPath(holder.path, holder.paint);
        }


//        canvas.drawBitmap(orignalBitmap, 0, 0,null);

//Insert all the rest of the drawing commands here
//        screenCanvas.drawBitmap(myBitmap, 0, 0);
    }

    private boolean isPaintingModeActive = false;

    public boolean isPaintingModeActive() {
        return isPaintingModeActive;
    }

    public void setPaintingModeActive(boolean paintingModeActive) {
        isPaintingModeActive = paintingModeActive;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float eventX = event.getX();
        float eventY = event.getY();

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                holderList.add(new Holder(color,width));
                holderList.get(holderList.size() - 1).path.moveTo(eventX, eventY);
                return true;
            case MotionEvent.ACTION_MOVE:
                holderList.get(holderList.size() - 1).path.lineTo(eventX, eventY);
                break;
            case MotionEvent.ACTION_UP:
//                capturePhotoAndUpdateView();
                break;
            default:
                return false;
        }

        invalidate();
        return true;
    }

    public void resetPaths() {
        for (Holder holder : holderList) {
            holder.path.reset();
        }
        invalidate();
    }

    @Override
    public void setImageBitmap(Bitmap bm) {
        super.setImageBitmap(bm);
        orignalBitmap=bm;
        bitmapArrayListStates.add(bm);
//        if(drawCanvas==null){
//        drawCanvas = new Canvas(orignalBitmap);
//        }
    }

    public void setBrushColor(int color) {
        this.color = color;
    }

    public void setBrushSize(float width) {
        this.width = width;
    }


    public void capturePhotoAndUpdateView() {

        if (bitmapArrayListStates != null && bitmapArrayListStates.size() > 0) {
//            BitmapDrawable drawable = (BitmapDrawable) attacher.mImageView.getDrawable();
//            Bitmap bitmap = drawable.getBitmap();

            for (Holder holder : holderList) {
//     canvas.drawBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.ic_launcher), 0, 0, holder.paint);
//
//     canvas.drawBitmap(canvasBitmap, 0, 0, holder.paint);
                if(drawCanvas!=null)
                    drawCanvas.drawPath(holder.path, holder.paint);
//     drawCanvas.drawLine(bitmapArrayListStates.get(0),holder.eventX,holder.eventY, holder.paint);
            }
            bitmapArrayListStates.add(orignalBitmap);

            setImageBitmap(bitmapArrayListStates.get(bitmapArrayListStates.size() - 1));
        }
//        Toast.makeText(getContext(),bitmapArrayListStates.size()+" Size",Toast.LENGTH_SHORT).show();
        resetPaths();

    }

    public void undoPhotoAndUpdateView() {

        if (bitmapArrayListStates != null && bitmapArrayListStates.size() > 1) {
            bitmapArrayListStates.remove(bitmapArrayListStates.size() - 1);
            if (bitmapArrayListStates != null && bitmapArrayListStates.size() > 1)
                bitmapArrayListStates.remove(bitmapArrayListStates.size() - 1);

            setImageBitmap(bitmapArrayListStates.get(bitmapArrayListStates.size() - 1));
        }
//        Toast.makeText(getContext(),bitmapArrayListStates.size()+" Size",Toast.LENGTH_SHORT).show();
        resetPaths();

    }


}