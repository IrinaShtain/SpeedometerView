package com.shtainyky.speedometer;


import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.support.v4.content.ContextCompat;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;

public class SpeedometerView extends View {

    private Context mContext;

    private int background = R.color.grey;
    private int numberColor = R.color.black;
    private int colorMainBoder = R.color.black;
    private int colorBeforeSpeedLine = R.color.light_blue;
    private int colorAfterSpeedLine = R.color.dark_blue;
    private int colorBorderSpeedLine = R.color.black;
    private int colorSpeedLine = R.color.black;
    private int radiusM = 0;
    private int radiusS = 0;
    private int radiusL = 0;
    private int mRradiusL = 0;

    Paint paint;
    Path path;
    RectF rectF;
    Matrix matrix;
    Rect bounds;

    public SpeedometerView(Context context) {
        super(context);
        mContext = context;
        path = new Path();
        paint = new Paint();
        rectF = new RectF();
        matrix = new Matrix();
        bounds = new Rect();
    }

//    @Override
//    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
//        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
//        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
//        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
//        int heightSize = MeasureSpec.getSize(heightMeasureSpec);
////        if (widthMode == MeasureSpec.EXACTLY) {
////            radiusL = widthSize;
////        } else if (widthMode == MeasureSpec.AT_MOST) {
////            radiusL = Math.min(radiusL, widthSize);
////        } else {
////            radiusL = desiredWidth;
////        }
//    }

    @Override
    protected void onDraw(Canvas canvas) {
        int width;
        int height;
        int center_x;
        int center_y;
        if (getWidth() < getHeight()) {
            width = getWidth();
            height = getHeight();
            center_x = width / 2;
            center_y = height / 2;
        } else {
            width = 5*getHeight()/4;
            height = getWidth();
            center_x = height / 2;
            center_y = width / 2 + width / 4;
            width = 7*getHeight()/4;
        }

        if ((radiusL <= 0) || (radiusL > width)) {
            radiusL = width / 2;
        }
        if ((radiusM <= 0) || (radiusM > radiusL)) {
            radiusM = width / 4;
        }
        if ((radiusS <= 0) || (radiusS > radiusM)) {
            radiusS = width / 16;
        }
        // draw backgroundColor
        rectF.set(center_x - radiusL, center_y - radiusL, center_x + radiusL, center_y + radiusL);
        paint.setColor(getResources().getColor(background));
        paint.setStyle(Paint.Style.FILL);
        canvas.drawArc(rectF, 180, 180, true, paint);
        // draw BigArc
        paint.setColor(getResources().getColor(colorMainBoder));
        paint.setStrokeWidth(10);
        paint.setStyle(Paint.Style.STROKE);
        canvas.drawArc(rectF, 180, 180, false, paint);

        // draw line for number of speed
        path.addRect(0, 2*10, 2, 0, Path.Direction.CW);
        int step = 180 / 10;
        matrix.reset();
        matrix.setTranslate(center_x, center_y - radiusL);
        matrix.postRotate(step - 90, center_x, center_y);
        path.transform(matrix);
        canvas.drawPath(path, paint);
        matrix.reset();
        matrix.setRotate(step, center_x, center_y);
        for (int i = -160 + step; i < 0; i += step) {
            path.transform(matrix);
            canvas.drawPath(path, paint);
        }
        matrix.reset();

        // draw AfterSpeedLine
        paint.setStrokeWidth(35);
        paint.setColor(getResources().getColor(colorAfterSpeedLine));

        rectF.set(center_x - radiusM, center_y - radiusM, center_x + radiusM, center_y + radiusM);
        canvas.drawArc(rectF, 180, 180, false, paint);

        //draw numbers for speed
        paint.reset();
        paint.setColor(getResources().getColor(colorMainBoder));
        paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
        paint.setTextSize(30);
        double hip = 3.1 * radiusL / 4;
        for (int i = 1; i <= 9; i++) {
            float x_cat = (int) (hip * Math.cos(i * Math.PI / 10));
            float y_cat = (int) (hip * Math.sin(i * Math.PI / 10));
            int speed = 10 * i;
            if (i <= 4) {
                paint.getTextBounds(String.valueOf(speed), 0, String.valueOf(speed).length(), bounds);
                canvas.drawText(String.valueOf(speed), center_x - bounds.width() - x_cat, center_y - y_cat, paint);
            } else if (i == 5) {
                paint.getTextBounds(String.valueOf(speed), 0, String.valueOf(speed).length(), bounds);
                canvas.drawText(String.valueOf(speed), center_x - bounds.width() / 3 - x_cat, center_y - y_cat, paint);
            } else if (i > 5) {
                canvas.drawText(String.valueOf(speed), center_x - x_cat, center_y - y_cat, paint);
        }

    }
        Log.d("myLog","center_x= "+center_x);
        Log.d("myLog","center_y= "+center_y);


    // draw SpeedLineEnd
        rectF.set(center_x -radiusS,center_y -radiusS,center_x +radiusS,center_y +radiusS);
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(getResources().getColor(colorSpeedLine));
        canvas.drawArc(rectF,180,180,true,paint);

}

}
