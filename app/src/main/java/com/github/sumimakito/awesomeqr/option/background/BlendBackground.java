package com.github.sumimakito.awesomeqr.option.background;

import android.graphics.Bitmap;
import android.graphics.Rect;

public class BlendBackground extends Background {

    public int borderRadius = 10;

    public BlendBackground() {
    }

    public BlendBackground(int borderRadius, float alpha, Rect clippingRect, Bitmap bitmap) {
        super(alpha, clippingRect, bitmap);
        this.borderRadius = borderRadius;
    }

    @Override
    public BlendBackground duplicate() {
        return new BlendBackground(
                borderRadius,
                alpha,
                clippingRect,
                (bitmap != null ? bitmap.copy(Bitmap.Config.ARGB_8888, true) : null));
    }
}
