package com.example.fyptest;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.LightingColorFilter;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.util.TypedValue;
import android.view.View;

import androidx.annotation.ColorInt;
import androidx.annotation.Nullable;

public class FYPTabView extends View {

    private int mBackgroundColor;
    private Integer mForegroundColor;

    private Drawable mCircleDrawable;
    private Drawable mIconDrawable;
    private int mIconInsetLeft, mIconInsetTop, mIconInsetRight, mIconInsetBottom;

    public FYPTabView(Context context, Drawable backgroundDrawable, Drawable iconDrawable) {
        super(context);
        mCircleDrawable = backgroundDrawable;
        mIconDrawable = iconDrawable;
        init();
    }

    private void init() {
        int insetsDp = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 10, getContext().getResources().getDisplayMetrics());
        mIconInsetLeft = mIconInsetTop = mIconInsetRight = mIconInsetBottom = insetsDp;
    }

    public void setTabBackgroundColor(@ColorInt int backgroundColor) {
        mBackgroundColor = backgroundColor;
        //mCircleDrawable.setColorFilter(mBackgroundColor, PorterDuff.Mode.SRC_ATOP);
        mCircleDrawable.setColorFilter(new LightingColorFilter(mBackgroundColor, 0));
    }

    public void setTabForegroundColor(@ColorInt Integer foregroundColor) {
        mForegroundColor = foregroundColor;
        if (null != mForegroundColor) {
            mIconDrawable.setColorFilter(mForegroundColor, PorterDuff.Mode.SRC_OUT);
        } else {
            //mIconDrawable.setColorFilter(null);
            mIconDrawable.setColorFilter(new LightingColorFilter(mBackgroundColor, 0));
        }
    }

    public void setIcon(@Nullable Drawable icon) {
        mIconDrawable = icon;
        if (null != mForegroundColor && null != mIconDrawable) {
            mIconDrawable.setColorFilter(mForegroundColor, PorterDuff.Mode.SRC_ATOP);
        }
        updateIconBounds();

        invalidate();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        // Make circle as large as View minus padding.
        mCircleDrawable.setBounds(getPaddingLeft(), getPaddingTop(), w - getPaddingRight(), h - getPaddingBottom());

        // Re-size the icon as necessary.
        updateIconBounds();

        invalidate();
    }

    private void updateIconBounds() {
        if (null != mIconDrawable) {
            Rect bounds = new Rect(mCircleDrawable.getBounds());
            bounds.set(bounds.left + mIconInsetLeft, bounds.top + mIconInsetTop, bounds.right - mIconInsetRight, bounds.bottom - mIconInsetBottom);
            mIconDrawable.setBounds(bounds);
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        mCircleDrawable.draw(canvas);
        if (null != mIconDrawable) {
            mIconDrawable.draw(canvas);
        }
    }
}
