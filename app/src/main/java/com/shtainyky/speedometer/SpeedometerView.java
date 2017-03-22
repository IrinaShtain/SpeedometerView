package com.shtainyky.speedometer;


import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;

public class SpeedometerView extends View {

    private Context mContext;

    private static final int DEFAULT_BACKGROUND_COLOR = R.color.grey;
    private static final int DEFAULT_DIGITS_COLOR = R.color.black;
    private static final int DEFAULT_MAIN_BORDER_COLOR = R.color.black;
    private static final int DEFAULT_BEFORE_SPEED_LINE_COLOR = R.color.light_blue;
    private static final int DEFAULT_AFTER_SPEED_LINE_COLOR = R.color.dark_blue;
    private static final int DEFAULT_SPEED_LINE_COLOR = R.color.black;

    private static final int DEFAULT_MAX_SPEED = 60;
    private static final int DEFAULT_CURRENT_SPEED = 20;
    private static final int DEFAULT_OUTER_RADIUS = 0;
    private static final int DEFAULT_SPEED_LINE_RADIUS = 0;
    private static final int DEFAULT_INNER_RADIUS = 0;

    private int background;
    private int digitsColor;
    private int colorMainBoder;
    private int colorBeforeSpeedLine;
    private int colorAfterSpeedLine;
    private int colorSpeedLine;

    private int radiusM;
    private int radiusS;
    private int radiusL;
    private int radiusSpeedArrow;
    private int currentSpeed;
    private int maxSpeed;
    private int angleSpeed;
    int width;
    int height;
    int center_x;
    int center_y;


    Paint paint;
    Path path;
    Path path_arrow;
    RectF rectF;
    Matrix matrix;
    Rect bounds;
    Bitmap b;

    public SpeedometerView(Context context) {
        super(context);
        mContext = context;
        initialization();
        Log.d("myLog", "SpeedometerView Constructor");


        background = ContextCompat.getColor(mContext, DEFAULT_BACKGROUND_COLOR);
        digitsColor = ContextCompat.getColor(mContext, DEFAULT_DIGITS_COLOR);
        colorMainBoder = ContextCompat.getColor(mContext, DEFAULT_MAIN_BORDER_COLOR);
        colorBeforeSpeedLine = ContextCompat.getColor(mContext, DEFAULT_BEFORE_SPEED_LINE_COLOR);
        colorAfterSpeedLine = ContextCompat.getColor(mContext, DEFAULT_AFTER_SPEED_LINE_COLOR);
        colorSpeedLine = ContextCompat.getColor(mContext, DEFAULT_SPEED_LINE_COLOR);
        maxSpeed = DEFAULT_MAX_SPEED;
        currentSpeed = DEFAULT_CURRENT_SPEED;
        radiusM = DEFAULT_INNER_RADIUS;
        radiusSpeedArrow = DEFAULT_SPEED_LINE_RADIUS;
        radiusL = DEFAULT_OUTER_RADIUS;
    }

    public SpeedometerView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initialization();
        TypedArray attributes = context.getTheme().obtainStyledAttributes(
                attrs, R.styleable.SpeedometerView, 0, 0);

        try {
            radiusL = attributes.getInteger(R.styleable.SpeedometerView_outerSectorRadius, DEFAULT_OUTER_RADIUS);
            radiusM = attributes.getInteger(R.styleable.SpeedometerView_innerSectorRadius, DEFAULT_INNER_RADIUS);
            radiusSpeedArrow = attributes.getInteger(R.styleable.SpeedometerView_speedArrowRadius, DEFAULT_SPEED_LINE_RADIUS);

            maxSpeed = attributes.getInteger(R.styleable.SpeedometerView_maxSpeed, DEFAULT_MAX_SPEED);
            currentSpeed = attributes.getInteger(R.styleable.SpeedometerView_currentSpeed, DEFAULT_CURRENT_SPEED);

            background = attributes.getColor(R.styleable.SpeedometerView_backgroundColor, 0);
            digitsColor = attributes.getColor(R.styleable.SpeedometerView_digitsColor, 0);
            colorBeforeSpeedLine = attributes.getColor(R.styleable.SpeedometerView_colorBeforeSpeedLine, 0);
            colorAfterSpeedLine = attributes.getColor(R.styleable.SpeedometerView_colorAfterSpeedLine, 0);
            colorSpeedLine = attributes.getColor(R.styleable.SpeedometerView_colorSpeedLine, 0);
            colorMainBoder = attributes.getColor(R.styleable.SpeedometerView_colorMainBoder, 0);

        } finally {
            attributes.recycle();
        }
    }

    private void initialization() {
        path = new Path();
        path_arrow = new Path();
        paint = new Paint();
        rectF = new RectF();
        matrix = new Matrix();
        bounds = new Rect();
        b = BitmapFactory.decodeResource(getResources(), R.drawable.ic_oil);
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
        Log.d("myLog", "SpeedometerView onDraw");
        initRightRadius();
        fillBackgroundColor(canvas);
        drawMainBorder(canvas);

        int degreesRotate = 180 * maxSpeed / 100; //depends of max speed
        angleSpeed = degreesRotate * currentSpeed / maxSpeed ;
        Log.d("myLog", "angleSpeed = " + angleSpeed);

        drawSpeedArc(canvas);
        drawDigitsForSpeed(canvas);
        drawSpeedArrow(canvas);

        //draw oil image
        canvas.drawLine(center_x - b.getWidth() / 3, center_y - 6 * radiusM / 4 + b.getHeight() / 2,
                center_x - b.getWidth() / 3 + 2 * radiusS, center_y - 6 * radiusM / 4 + b.getHeight() / 2, paint);
        canvas.drawBitmap(b, center_x - b.getWidth(), center_y - 6 * radiusM / 4, paint);
    }

    private void initRightRadius(){
        Log.d("myLog", "initRightRadius = " +radiusL);
        if (getWidth() < getHeight()) {
            width = getWidth();
            height = getHeight();
            center_x = width / 2;
            center_y = height / 2;
        } else {
            width = 5 * getHeight() / 4;
            height = getWidth();
            center_x = height / 2;
            center_y = width / 2 + width / 4;
            width = 7 * getHeight() / 4;
        }
        Log.d("myLog", "width = " + width);
        if ((radiusL <= 0) || (radiusL > width / 2)) {
            radiusL = width / 2;
        }
        if ((radiusM <= 0) || (radiusM > radiusL)) {
            radiusM = width / 4;
        }
        if ((radiusSpeedArrow <= 0) || (radiusS > radiusM)) {
            radiusSpeedArrow = 3 * radiusL / 4;
        }
        radiusS = radiusL / 8;
        Log.d("myLog", "initRightRadius = " + radiusL);
    }

    private void fillBackgroundColor(Canvas canvas){
        // draw backgroundColor
        rectF.set(center_x - radiusL, center_y - radiusL, center_x + radiusL, center_y + radiusL);
        paint.setColor(background);
        paint.setStyle(Paint.Style.FILL);
        canvas.drawArc(rectF, 180, 180, true, paint);
    }

    private void drawMainBorder(Canvas canvas){
        // draw BigArc
        paint.setColor(colorMainBoder);
        paint.setStrokeWidth(10);
        paint.setStyle(Paint.Style.STROKE);
        canvas.drawArc(rectF, 180, 180, false, paint);
        // draw line for number of speed
        path.addRect(0, 2 * 10, 2, 0, Path.Direction.CW);
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
    }

    private void drawSpeedArc(Canvas canvas){
        paint.setStrokeWidth(radiusS);
        rectF.set(center_x - radiusM, center_y - radiusM, center_x + radiusM, center_y + radiusM);
        // draw AfterSpeedLine
        paint.setColor(colorAfterSpeedLine);
        canvas.drawArc(rectF, 180 + angleSpeed, 180 - angleSpeed, false, paint);
        // draw BeforeSpeedLine
        paint.setColor(colorBeforeSpeedLine);
        canvas.drawArc(rectF, 180 , angleSpeed, false, paint);
    }

    private void drawDigitsForSpeed(Canvas canvas){
        //draw numbers for speed
        paint.reset();
        paint.setColor(digitsColor);
        paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
        paint.setTextSize(radiusL/9);
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
        Log.d("myLog", "center_x= " + center_x);
        Log.d("myLog", "center_y= " + center_y);
    }

    private void drawSpeedArrow(Canvas canvas){
        // draw SpeedLineArrow
        rectF.set(center_x - radiusS, center_y - radiusS, center_x + radiusS, center_y + radiusS);
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(colorSpeedLine);
        canvas.drawArc(rectF, 180, 180, true, paint);

        paint.setStrokeWidth(10);
        path_arrow.moveTo(center_x, center_y);
        path_arrow.lineTo(center_x + radiusS / 2, center_y);
        path_arrow.lineTo(center_x + radiusS / 5, center_y - radiusSpeedArrow);
        path_arrow.lineTo(center_x - radiusS / 5, center_y - radiusSpeedArrow);
        path_arrow.lineTo(center_x, center_y);


        matrix.reset();
        matrix.setTranslate(0, 0);
        Log.d("myLog", "setTranslate angleSpeed= " + angleSpeed);
        matrix.postRotate(angleSpeed - 90, center_x, center_y);
        path_arrow.transform(matrix);
        canvas.drawPath(path_arrow, paint);
        // invalidate();
    }

}
