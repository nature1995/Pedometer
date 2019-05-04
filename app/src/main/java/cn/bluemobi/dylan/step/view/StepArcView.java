package cn.bluemobi.dylan.step.view;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.view.View;

import cn.bluemobi.dylan.step.R;

public class StepArcView extends View {

    private float borderWidth = dipToPx(14);
    private float numberTextSize = 0;
    private String stepNumber = "0";
    private float startAngle = 135;
    private float angleLength = 270;
    private float currentAngleLength = 0;
    private int animationLength = 3000;

    public StepArcView(Context context) {
        super(context);


    }

    public StepArcView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public StepArcView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }


    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        float centerX = (getWidth()) / 2;
        RectF rectF = new RectF(0 + borderWidth, borderWidth, 2 * centerX - borderWidth, 2 * centerX - borderWidth);
        drawArcYellow(canvas, rectF);
        drawArcRed(canvas, rectF);
        drawTextNumber(canvas, centerX);
        drawTextStepString(canvas, centerX);
    }

    private void drawArcYellow(Canvas canvas, RectF rectF) {
        Paint paint = new Paint();
        paint.setColor(getResources().getColor(R.color.yellow));
        paint.setStrokeJoin(Paint.Join.ROUND);
        paint.setStrokeCap(Paint.Cap.ROUND);
        paint.setStyle(Paint.Style.STROKE);
        paint.setAntiAlias(true);
        paint.setStrokeWidth(borderWidth);

        canvas.drawArc(rectF, startAngle, angleLength, false, paint);

    }

    private void drawArcRed(Canvas canvas, RectF rectF) {
        Paint paintCurrent = new Paint();
        paintCurrent.setStrokeJoin(Paint.Join.ROUND);
        paintCurrent.setStrokeCap(Paint.Cap.ROUND);
        paintCurrent.setStyle(Paint.Style.STROKE);
        paintCurrent.setAntiAlias(true);
        paintCurrent.setStrokeWidth(borderWidth);
        paintCurrent.setColor(getResources().getColor(R.color.red));
        canvas.drawArc(rectF, startAngle, currentAngleLength, false, paintCurrent);
    }

    private void drawTextNumber(Canvas canvas, float centerX) {
        Paint vTextPaint = new Paint();
        vTextPaint.setTextAlign(Paint.Align.CENTER);
        vTextPaint.setAntiAlias(true);
        vTextPaint.setTextSize(numberTextSize);
        Typeface font = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL);
        vTextPaint.setTypeface(font);
        vTextPaint.setColor(getResources().getColor(R.color.red));
        Rect bounds_Number = new Rect();
        vTextPaint.getTextBounds(stepNumber, 0, stepNumber.length(), bounds_Number);
        canvas.drawText(stepNumber, centerX, getHeight() / 2 + bounds_Number.height() / 2, vTextPaint);

    }

    private void drawTextStepString(Canvas canvas, float centerX) {
        Paint vTextPaint = new Paint();
        vTextPaint.setTextSize(dipToPx(16));
        vTextPaint.setTextAlign(Paint.Align.CENTER);
        vTextPaint.setAntiAlias(true);
        vTextPaint.setColor(getResources().getColor(R.color.grey));
        String stepString = "Steps";
        Rect bounds = new Rect();
        vTextPaint.getTextBounds(stepString, 0, stepString.length(), bounds);
        canvas.drawText(stepString, centerX, getHeight() / 2 + bounds.height() + getFontHeight(numberTextSize), vTextPaint);
    }

    public int getFontHeight(float fontSize) {
        Paint paint = new Paint();
        paint.setTextSize(fontSize);
        Rect bounds_Number = new Rect();
        paint.getTextBounds(stepNumber, 0, stepNumber.length(), bounds_Number);
        return bounds_Number.height();
    }

    private int dipToPx(float dip) {
        float density = getContext().getResources().getDisplayMetrics().density;
        return (int) (dip * density + 0.5f * (dip >= 0 ? 1 : -1));
    }

    public void setCurrentCount(int totalStepNum, int currentCounts) {
        if (currentCounts > totalStepNum) {
            currentCounts = totalStepNum;
        }

        float scalePrevious = (float) Integer.valueOf(stepNumber) / totalStepNum;
        float previousAngleLength = scalePrevious * angleLength;

        float scale = (float) currentCounts / totalStepNum;
        float currentAngleLength = scale * angleLength;
        setAnimation(previousAngleLength, currentAngleLength, animationLength);

        stepNumber = String.valueOf(currentCounts);
        setTextSize(currentCounts);
    }

    private void setAnimation(float start, float current, int length) {
        ValueAnimator progressAnimator = ValueAnimator.ofFloat(start, current);
        progressAnimator.setDuration(length);
        progressAnimator.setTarget(currentAngleLength);
        progressAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                currentAngleLength = (float) animation.getAnimatedValue();
                invalidate();
            }
        });
        progressAnimator.start();
    }

    public void setTextSize(int num) {
        String s = String.valueOf(num);
        int length = s.length();
        if (length <= 4) {
            numberTextSize = dipToPx(50);
        } else if (length > 4 && length <= 6) {
            numberTextSize = dipToPx(40);
        } else if (length > 6 && length <= 8) {
            numberTextSize = dipToPx(30);
        } else if (length > 8) {
            numberTextSize = dipToPx(25);
        }
    }

}

