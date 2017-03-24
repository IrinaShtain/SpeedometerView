package com.shtainyky.speedometer;


import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

public class SpeedometerView extends View {

    private static final int MSG_SEND_DATA = 0;
    private static final long DELAY_INTERVAl = 1000 / 24;
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
    private static final float DEFAULT_COEFFICIENT = 0.05f;

    private int backgroundColor;
    private int digitsColor;
    private int colorMainBorder;
    private int colorBeforeSpeedLine;
    private int colorAfterSpeedLine;
    private int colorSpeedLine;

    private int radiusM;
    private int radiusS;
    private int radiusL;
    private int radiusSpeedArrow;
    private float currentSpeed;
    private int maxSpeed;
    private int angleSpeed;
    private OnSpeedChangedListener mOnSpeedChangedListener;
    private float currentCoefficient;

    private boolean isGasPressed;
    private boolean isBrakePressed;

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

    private final Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.what == MSG_SEND_DATA) {
                invalidate();
                sendEmptyMessageDelayed(MSG_SEND_DATA, DELAY_INTERVAl);
                if (mOnSpeedChangedListener != null)
                    mOnSpeedChangedListener.onSpeedChanged((int) currentSpeed);
            }
        }
    };

    private void sendSpeed() {
        handler.removeMessages(MSG_SEND_DATA);
        handler.sendEmptyMessage(MSG_SEND_DATA);
    }

    public SpeedometerView(Context context) {
        super(context);
        mContext = context;
        initialization();
        Log.d("myLog", "SpeedometerView Constructor");
        backgroundColor = ContextCompat.getColor(mContext, DEFAULT_BACKGROUND_COLOR);
        digitsColor = ContextCompat.getColor(mContext, DEFAULT_DIGITS_COLOR);
        colorMainBorder = ContextCompat.getColor(mContext, DEFAULT_MAIN_BORDER_COLOR);
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

            backgroundColor = attributes.getColor(R.styleable.SpeedometerView_backgroundColor, 0);
            digitsColor = attributes.getColor(R.styleable.SpeedometerView_digitsColor, 0);
            colorBeforeSpeedLine = attributes.getColor(R.styleable.SpeedometerView_colorBeforeSpeedLine, 0);
            colorAfterSpeedLine = attributes.getColor(R.styleable.SpeedometerView_colorAfterSpeedLine, 0);
            colorSpeedLine = attributes.getColor(R.styleable.SpeedometerView_colorSpeedLine, 0);
            colorMainBorder = attributes.getColor(R.styleable.SpeedometerView_colorMainBoder, 0);

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

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);

        int width;
        int height;

        //Measure Width
        if (widthMode == MeasureSpec.EXACTLY || widthMode == MeasureSpec.AT_MOST) {
            //Must be this size
            width = widthSize;
        } else {
            width = -1;
        }

        //Measure Height
        if (heightMode == MeasureSpec.EXACTLY || heightMode == MeasureSpec.AT_MOST) {
            //Must be this size
            height = heightSize;
        } else {
            height = -1;
        }

        if (height >= 0 && width >= 0) {
            width = Math.min(height, width);
            height = width / 2;
        } else if (width >= 0) {
            height = width / 2;
        } else if (height >= 0) {
            width = height * 2;
        } else {
            width = 0;
            height = 0;
        }

        //MUST CALL THIS
        setMeasuredDimension(width, height);
    }


    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        initRightRadius();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        //  Log.d("myLog", "SpeedometerView onDraw");
        updateCurrentSpeed();
        fillBackgroundColor(canvas);
        drawMainBorder(canvas);
        drawDigitsForSpeed(canvas);

        calculateAngleSpeed();
        drawSpeedArc(canvas);
        drawSpeedArrow(canvas);

        //draw oil image
        canvas.drawLine(center_x - b.getWidth() / 3, center_y - 6 * radiusM / 4 + b.getHeight() / 2,
                center_x - b.getWidth() / 3 + 2 * radiusS, center_y - 6 * radiusM / 4 + b.getHeight() / 2, paint);
        canvas.drawBitmap(b, center_x - b.getWidth(), center_y - 6 * radiusM / 4, paint);
    }

    private void calculateAngleSpeed() {
        int degreesRotate = 180 * maxSpeed / 100; //depends of max speed
        angleSpeed = (int) (degreesRotate * currentSpeed / maxSpeed);
        //  Log.d("myLog", "angleSpeed = " + angleSpeed);
    }


    private void initRightRadius() {
        //  Log.d("myLog", "initRightRadius = " + radiusL);
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
        //  Log.d("myLog", "width = " + width);
        if ((radiusL <= 0) || (radiusL > width / 2)) {
            radiusL = width / 2;
        }
        if ((radiusM <= 0) || (radiusM > radiusL)) {
            radiusM = width / 4;
        }
        if ((radiusSpeedArrow <= 0) || (radiusSpeedArrow > radiusL)) {
            radiusSpeedArrow = 3 * radiusL / 4;
        }
        radiusS = radiusL / 8;
        //  Log.d("myLog", "initRightRadius = " + radiusL);
    }

    private void fillBackgroundColor(Canvas canvas) {
        // draw backgroundColor
        rectF.set(center_x - radiusL, center_y - radiusL, center_x + radiusL, center_y + radiusL);
        paint.setColor(backgroundColor);
        paint.setStyle(Paint.Style.FILL);
        canvas.drawArc(rectF, 180, 180, true, paint);
    }

    private void drawMainBorder(Canvas canvas) {
        // draw BigArc
        paint.setColor(colorMainBorder);
        paint.setStrokeWidth(10);
        paint.setStyle(Paint.Style.STROKE);
        canvas.drawArc(rectF, 180, 180, false, paint);
        canvas.drawLine(center_x - radiusL, center_y, center_x + radiusL, center_y, paint);
        // draw line for number of speed
        path.reset();
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

    private void drawSpeedArc(Canvas canvas) {
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(radiusS);
        rectF.set(center_x - radiusM, center_y - radiusM, center_x + radiusM, center_y + radiusM);
        // draw AfterSpeedLine
        paint.setColor(colorAfterSpeedLine);
        canvas.drawArc(rectF, 180 + angleSpeed, 180 - angleSpeed, false, paint);
        // draw BeforeSpeedLine
        paint.setColor(colorBeforeSpeedLine);
        canvas.drawArc(rectF, 180, angleSpeed, false, paint);

    }

    private void drawDigitsForSpeed(Canvas canvas) {
        //draw numbers for speed
        paint.reset();
        paint.setColor(digitsColor);
        paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
        paint.setTextSize(radiusL / 9);
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
//        Log.d("myLog", "center_x= " + center_x);
//        Log.d("myLog", "center_y= " + center_y);
    }

    private void drawSpeedArrow(Canvas canvas) {
        // draw SpeedLineArrow
        rectF.set(center_x - radiusS, center_y - radiusS, center_x + radiusS, center_y + radiusS);
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(colorSpeedLine);
        canvas.drawArc(rectF, 180, 180, true, paint);
        path_arrow.reset();
        paint.setStyle(Paint.Style.FILL_AND_STROKE);
        paint.setStrokeWidth(10);
        path_arrow.moveTo(center_x, center_y);
        path_arrow.lineTo(center_x - 2 * radiusS / 10, center_y);
        path_arrow.lineTo(center_x - radiusS / 10, center_y - radiusSpeedArrow);
        path_arrow.lineTo(center_x + radiusS / 10, center_y - radiusSpeedArrow);
        path_arrow.lineTo(center_x + 2 * radiusS / 10, center_y);

        matrix.reset();
        matrix.setTranslate(center_x, center_y);
        matrix.setRotate(angleSpeed - 90, center_x, center_y);
        path_arrow.transform(matrix);
        canvas.drawPath(path_arrow, paint);

        invalidate();
    }

    /////////////////////////**************************************////////////////////////////////////

    public void pressGas() {
        if (isBrakePressed) isBrakePressed = false;
        isGasPressed = true;
        currentCoefficient = 8 * DEFAULT_COEFFICIENT;
        sendSpeed();
    }

    public void releaseGas() {
        isGasPressed = false;
        if (!isBrakePressed) {
            relax();
            sendSpeed();
        }

    }

    public void pressBrake() {
        if (isGasPressed) isGasPressed = false;
        isBrakePressed = true;
        currentCoefficient = -20 * DEFAULT_COEFFICIENT;
        sendSpeed();
    }

    public void releaseBrake() {
        if (isBrakePressed) {
            isBrakePressed = false;
            relax();
            sendSpeed();
        }
    }

    private void relax() {
        currentCoefficient = -2 * DEFAULT_COEFFICIENT;
    }


    private void updateCurrentSpeed() {
        if (currentCoefficient > 0) {
            if (currentSpeed < maxSpeed && currentSpeed >= 0) {
                currentSpeed = currentSpeed + currentCoefficient;
            } else
                currentSpeed = maxSpeed;
        } else {
            if (currentSpeed <= maxSpeed && currentSpeed > 0) {
                currentSpeed = currentSpeed + currentCoefficient;
            } else
                currentSpeed = 0;
        }

    }

    /////////////////////////************************************************////////////////////////////////////
    public float getCurrentSpeed() {
        return currentSpeed;
    }

    public void setCurrentSpeed(int currentSpeed) {
        if (currentSpeed > this.maxSpeed)
            this.currentSpeed = maxSpeed;
        else if (currentSpeed < 0)
            this.currentSpeed = 0;
        else
            this.currentSpeed = currentSpeed;
        invalidate();

    }

    public int getMaxSpeed() {
        return maxSpeed;
    }

    public void setMaxSpeed(int maxSpeed) {
        this.maxSpeed = maxSpeed;
        invalidate();
    }

    public int getRadiusSpeedArc() {
        return radiusM;
    }

    public void setRadiusSpeedArc(int radius) {
        if ((radius <= 0) || (radius > radiusL)) {
            radius = width / 4;
        }
        this.radiusM = radius;
        invalidate();
    }

    public int getRadiusMainBorder() {
        return radiusL;
    }

    public void setRadiusMainBorder(int radius) {
        if ((radius <= 0) || (radius > width / 2)) {
            radius = width / 2;
        }
        this.radiusL = radius;
        invalidate();
    }

    public int getRadiusSpeedArrow() {
        return radiusSpeedArrow;
    }

    public void setRadiusSpeedArrow(int radiusSpeedArrow) {
        if ((radiusSpeedArrow <= 0) || (radiusSpeedArrow > radiusL)) {
            radiusSpeedArrow = 3 * radiusL / 4;
        }
        this.radiusSpeedArrow = radiusSpeedArrow;
        invalidate();
    }

    public int getBackgroundColor() {
        return backgroundColor;
    }

    public void setBackground(int backgroundColor) {
        this.backgroundColor = backgroundColor;
        invalidate();
    }

    public int getDigitsColor() {
        return digitsColor;
    }

    public void setDigitsColor(int digitsColor) {
        this.digitsColor = digitsColor;
        invalidate();
    }

    public int getColorMainBorder() {
        return colorMainBorder;
    }

    public void setColorMainBorder(int colorMainBoder) {
        this.colorMainBorder = colorMainBoder;
        invalidate();
    }

    public int getColorBeforeSpeedLine() {
        return colorBeforeSpeedLine;
    }

    public void setColorBeforeSpeedLine(int colorBeforeSpeedLine) {
        this.colorBeforeSpeedLine = colorBeforeSpeedLine;
        invalidate();
    }

    public int getColorAfterSpeedLine() {
        return colorAfterSpeedLine;
    }

    public void setColorAfterSpeedLine(int colorAfterSpeedLine) {
        this.colorAfterSpeedLine = colorAfterSpeedLine;
        invalidate();
    }

    public int getColorSpeedLine() {
        return colorSpeedLine;
    }

    public void setColorSpeedLine(int colorSpeedLine) {
        this.colorSpeedLine = colorSpeedLine;
        invalidate();
    }

    public interface OnSpeedChangedListener {
        void onSpeedChanged(int value);
    }

    public void setOnSpeedChangedListener(OnSpeedChangedListener onSpeedChangedListener) {
        mOnSpeedChangedListener = onSpeedChangedListener;
    }
}
