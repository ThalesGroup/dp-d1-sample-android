package com.thalesgroup.d1.templates.core.ui;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.FrameLayout;

/** Clips the complete card, including bitmap backgrounds, to its rounded outline. */
public class CardFrameLayout extends FrameLayout {
    public CardFrameLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        setClipToOutline(true);
    }

    @Override protected void onMeasure(int widthSpec, int heightSpec) {
        if (MeasureSpec.getMode(widthSpec) == MeasureSpec.EXACTLY
                && MeasureSpec.getMode(heightSpec) != MeasureSpec.EXACTLY) {
            int height = (int) (MeasureSpec.getSize(widthSpec) * 212f / 337f);
            heightSpec = MeasureSpec.makeMeasureSpec(height, MeasureSpec.EXACTLY);
        }
        super.onMeasure(widthSpec, heightSpec);
    }
}
