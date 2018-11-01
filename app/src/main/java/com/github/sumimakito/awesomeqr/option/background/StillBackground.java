package com.github.sumimakito.awesomeqr.option.background;

import android.graphics.Bitmap;
import android.graphics.Rect;

public class StillBackground extends Background {

    public StillBackground() {
    }

    public StillBackground(float alpha, Rect clippingRect, Bitmap bitmap) {
        super(alpha, clippingRect, bitmap);
    }

    @Override
    public StillBackground duplicate() {
        return new StillBackground(
                alpha,
                clippingRect,
                bitmap != null ? bitmap.copy(Bitmap.Config.ARGB_8888, true) : null);
    }
}
