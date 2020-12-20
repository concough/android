package com.concough.android.general;

/**
 * Created by FaridM on 1/17/2018.
 */


import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.Shader;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;

/**
 * Created by noman on 9/14/15.
 */
public class ImageMagnifier extends androidx.appcompat.widget.AppCompatImageView {
    private PointF zoomPos;
    private boolean zooming = false;
    private Matrix matrix;
    private Paint paint;
    private Bitmap bitmap;
    private BitmapShader shader;
    private int sizeOfMagnifier = 200;
    public ImageMagnifier.OnTouchListener touchEventInterface = null;
    private Canvas convasConfigurer;
    private final Handler handler = new Handler();


    public ImageMagnifier(Context context) {
        super(context);
        init();
    }

    public ImageMagnifier(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    public ImageMagnifier(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        zoomPos = new PointF(0, 0);
        matrix = new Matrix();
        paint = new Paint();

    }

    public interface OnTouchListener {
        void OnTouch();

        void OnRelease();
    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int action = event.getAction();

        zoomPos.x = event.getX();
        zoomPos.y = event.getY();

        Log.d("IMAGE","X="+event.getX());
        Log.d("IMAGE","Y="+event.getY());


        switch (action) {
            case MotionEvent.ACTION_DOWN:
            case MotionEvent.ACTION_MOVE:
                zooming = true;
                this.invalidate();
                if(touchEventInterface!=null) touchEventInterface.OnTouch();
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                zooming = false;
                this.invalidate();
                if(touchEventInterface!=null) touchEventInterface.OnRelease();
                break;

            default:
                break;
        }

        return true;
    }

    public void configurer(Canvas v) {
        this.convasConfigurer = v;
    }




    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if(this.convasConfigurer!=null) {
            canvas = this.convasConfigurer;
        }

        Log.d("IMAGE_SIZE","X="+canvas.getWidth());
        Log.d("IMAGE_SIZE","Y="+canvas.getHeight());


        if (!zooming) {
            buildDrawingCache();
        } else {

            bitmap = getDrawingCache();
            shader = new BitmapShader(bitmap, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP);

            paint = new Paint();
            paint.setShader(shader);
            matrix.reset();


            if(zoomPos.y  < 250) {
                if(zoomPos.x< 250) {
                    canvas.drawCircle(zoomPos.x+450, zoomPos.y+250, sizeOfMagnifier, paint);
                } else {
                    canvas.drawCircle(zoomPos.x-300, zoomPos.y+250, sizeOfMagnifier, paint);
                }
                matrix.postScale(2f, 2f, zoomPos.x, zoomPos.y-160);
                paint.getShader().setLocalMatrix(matrix);
            } else {
                canvas.drawCircle(zoomPos.x, zoomPos.y-250, sizeOfMagnifier, paint);
                matrix.postScale(2f, 2f, zoomPos.x, zoomPos.y+150);
                paint.getShader().setLocalMatrix(matrix);
            }

        }
    }


}