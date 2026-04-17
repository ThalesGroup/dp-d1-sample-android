/*
 * Copyright © 2023 THALES. All rights reserved.
 */


package com.thalesgroup.d1.templates.core.ui.base;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

import com.thalesgroup.d1.core.R;

/**
 * An indicator of progress, similar to Android's ProgressBar.
 * Can be used in 'spin mode' or 'increment mode'.
 */
public class ProgressWheel extends View {

    int progress = 0;
    boolean isSpinning = false;
    //Sizes (with defaults)
    private int layoutHeight = 0;
    private int layoutWidth = 0;
    private int barLength = 60;
    private int barWidth = 20;
    private int rimWidth = 20;
    private int textSize = 20;
    private float contourSize = 0;
    //Padding (with defaults)
    private int paddingTop = 5;
    private int paddingBottom = 5;
    private int paddingLeft = 5;
    private int paddingRight = 5;
    //Colors (with defaults)
    private int barColor = 0xAA000000;
    private int contourColor = 0xAA000000;
    private int circleColor = 0x00000000;
    private int rimColor = 0xAADDDDDD;
    private int textColor = 0xFF000000;
    //Paints
    private final Paint barPaint = new Paint();
    private final Paint circlePaint = new Paint();
    private final Paint rimPaint = new Paint();
    private final Paint textPaint = new Paint();
    private final Paint contourPaint = new Paint();
    private RectF circleBounds = new RectF();
    private RectF circleOuterContour = new RectF();
    private RectF circleInnerContour = new RectF();
    //Animation
    //The amount of pixels to move the bar by on each draw
    private int spinSpeed = 2;
    //The number of milliseconds to wait inbetween each draw
    private int delayMillis = 0;
    private String[] splitText = {};

    /**
     * The constructor for the ProgressWheel.
     *
     * @param context Context.
     * @param attrs   AttributeSet.
     */
    public ProgressWheel(final Context context, final AttributeSet attrs) {
        super(context, attrs);

        parseAttributes(context.obtainStyledAttributes(attrs, R.styleable.ProgressWheel));
    }

    @Override
    protected void onMeasure(final int widthMeasureSpec, final int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        int size;
        final int width = getMeasuredWidth();
        final int height = getMeasuredHeight();
        final int widthWithoutPadding = width - getPaddingLeft() - getPaddingRight();
        final int heightWithoutPadding = height - getPaddingTop() - getPaddingBottom();

        size = Math.min(widthWithoutPadding, heightWithoutPadding);

        setMeasuredDimension(size + getPaddingLeft() + getPaddingRight(), size + getPaddingTop() + getPaddingBottom());
    }

    /**
     * Use onSizeChanged instead of onAttachedToWindow to get the dimensions of the view,
     * because this method is called after measuring the dimensions of MATCH_PARENT & WRAP_CONTENT.
     * Use this dimensions to setup the bounds and paints.
     */
    @Override
    protected void onSizeChanged(final int width, final int height, final int oldWidth, final int oldHeight) {
        super.onSizeChanged(width, height, oldWidth, oldHeight);

        // Share the dimensions
        layoutWidth = width;
        layoutHeight = height;

        setupBounds();
        setupPaints();
        invalidate();
    }

    /**
     * Set the properties of the paints we're using to
     * draw the progress wheel.
     */
    private void setupPaints() {
        barPaint.setColor(barColor);
        barPaint.setAntiAlias(true);
        barPaint.setStyle(Style.STROKE);
        barPaint.setStrokeWidth(barWidth);

        rimPaint.setColor(rimColor);
        rimPaint.setAntiAlias(true);
        rimPaint.setStyle(Style.STROKE);
        rimPaint.setStrokeWidth(rimWidth);

        circlePaint.setColor(circleColor);
        circlePaint.setAntiAlias(true);
        circlePaint.setStyle(Style.FILL);

        textPaint.setColor(textColor);
        textPaint.setStyle(Style.FILL);
        textPaint.setAntiAlias(true);
        textPaint.setTextSize(textSize);

        contourPaint.setColor(contourColor);
        contourPaint.setAntiAlias(true);
        contourPaint.setStyle(Style.STROKE);
        contourPaint.setStrokeWidth(contourSize);
    }

    /**
     * Set the bounds of the component.
     */
    private void setupBounds() {
        // Width should equal to Height, find the min value to setup the circle
        final int minValue = Math.min(layoutWidth, layoutHeight);

        // Calc the Offset if needed
        final int xOffset = layoutWidth - minValue;
        final int yOffset = layoutHeight - minValue;

        // Add the offset
        paddingTop = this.getPaddingTop() + (yOffset / 2);
        paddingBottom = this.getPaddingBottom() + (yOffset / 2);
        paddingLeft = this.getPaddingLeft() + (xOffset / 2);
        paddingRight = this.getPaddingRight() + (xOffset / 2);

        final int width = getWidth();
        final int height = getHeight();

        circleBounds = new RectF(paddingLeft + barWidth,
                paddingTop + barWidth,
                width - paddingRight - barWidth,
                height - paddingBottom - barWidth);
        circleInnerContour = new RectF(circleBounds.left + (rimWidth / 2.0f) + (contourSize / 2.0f), circleBounds.top + (rimWidth / 2.0f) + (contourSize / 2.0f), circleBounds.right - (rimWidth / 2.0f) - (contourSize / 2.0f), circleBounds.bottom - (rimWidth / 2.0f) - (contourSize / 2.0f));
        circleOuterContour = new RectF(circleBounds.left - (rimWidth / 2.0f) - (contourSize / 2.0f), circleBounds.top - (rimWidth / 2.0f) - (contourSize / 2.0f), circleBounds.right + (rimWidth / 2.0f) + (contourSize / 2.0f), circleBounds.bottom + (rimWidth / 2.0f) + (contourSize / 2.0f));
    }

    /**
     * Parse the attributes passed to the view from the XML.
     *
     * @param typedArray the attributes to parse
     */
    private void parseAttributes(final TypedArray typedArray) {
        barWidth = (int) typedArray.getDimension(R.styleable.ProgressWheel_barWidth,
                barWidth);

        rimWidth = (int) typedArray.getDimension(R.styleable.ProgressWheel_rimWidth,
                rimWidth);

        spinSpeed = (int) typedArray.getDimension(R.styleable.ProgressWheel_spinSpeed,
                spinSpeed);

        delayMillis = typedArray.getInteger(R.styleable.ProgressWheel_delayMillis,
                delayMillis);
        if (delayMillis < 0) {
            delayMillis = 0;
        }

        barColor = typedArray.getColor(R.styleable.ProgressWheel_barColor, barColor);

        barLength = (int) typedArray.getDimension(R.styleable.ProgressWheel_barLen,
                barLength);

        textSize = (int) typedArray.getDimension(R.styleable.ProgressWheel_textSize,
                textSize);

        textColor = typedArray.getColor(R.styleable.ProgressWheel_textColor,
                textColor);

        //if the text is empty , so ignore it
        if (typedArray.hasValue(R.styleable.ProgressWheel_text)) {
            setText(typedArray.getString(R.styleable.ProgressWheel_text));
        }

        rimColor = typedArray.getColor(R.styleable.ProgressWheel_rimColor,
                rimColor);

        circleColor = typedArray.getColor(R.styleable.ProgressWheel_circleColor,
                circleColor);

        contourColor = typedArray.getColor(R.styleable.ProgressWheel_contourColor, contourColor);
        contourSize = typedArray.getDimension(R.styleable.ProgressWheel_contourSize, contourSize);

        typedArray.recycle();
    }

    protected void onDraw(final Canvas canvas) {
        super.onDraw(canvas);
        //Draw the inner circle
        canvas.drawArc(circleBounds, 360, 360, false, circlePaint);
        //Draw the rim
        canvas.drawArc(circleBounds, 360, 360, false, rimPaint);
        canvas.drawArc(circleOuterContour, 360, 360, false, contourPaint);
        canvas.drawArc(circleInnerContour, 360, 360, false, contourPaint);
        //Draw the bar
        if (isSpinning) {
            canvas.drawArc(circleBounds, progress - 90, barLength, false,
                    barPaint);
        } else {
            canvas.drawArc(circleBounds, -90, progress, false, barPaint);
        }
        //Draw the text (attempts to center it horizontally and vertically)
        final float textHeight = textPaint.descent() - textPaint.ascent();
        final float verticalTextOffset = textHeight / 2 - textPaint.descent();

        for (final String string : splitText) {
            final float horizontalTextOffset = textPaint.measureText(string) / 2;
            canvas.drawText(string, this.getWidth() / 2f - horizontalTextOffset,
                    this.getHeight() / 2f + verticalTextOffset, textPaint);
        }
        if (isSpinning) {
            scheduleRedraw();
        }
    }

    private void scheduleRedraw() {
        progress += spinSpeed;
        if (progress > 360) {
            progress = 0;
        }
        postInvalidateDelayed(delayMillis);
    }

    /**
     * Puts the view on spin mode.
     */
    public void spin() {
        isSpinning = true;
        postInvalidate();
    }

    /**
     * @param text String.
     */
    public void setText(final String text) {
        splitText = text.split("\n");
    }

    /**
     * @return Integer.
     */
    public int getTextSize() {
        return textSize;
    }

    /**
     * @param textSize Integer.
     */
    public void setTextSize(final int textSize) {
        this.textSize = textSize;

        this.textPaint.setTextSize(this.textSize);
    }

    public int getPaddingTop() {
        return paddingTop;
    }

    public int getPaddingBottom() {
        return paddingBottom;
    }

    public int getPaddingLeft() {
        return paddingLeft;
    }

    public int getPaddingRight() {
        return paddingRight;
    }
}
