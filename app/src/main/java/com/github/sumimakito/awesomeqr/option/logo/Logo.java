package com.github.sumimakito.awesomeqr.option.logo;

import android.graphics.Bitmap;
import android.graphics.RectF;

public class Logo {
    public Bitmap bitmap = null;
    public float scale = 0.2f;
    public int borderRadius = 8;
    public int borderWidth = 10;
    public RectF clippingRect = null;

    public Logo() {
    }

    public Logo(Bitmap bitmap, float scale, int borderRadius, int borderWidth, RectF clippingRect) {
        this.bitmap = bitmap;
        this.scale = scale;
        this.borderRadius = borderRadius;
        this.borderWidth = borderWidth;
        this.clippingRect = clippingRect;
    }

    public void recycle() {
        if(bitmap == null)
            return;
        if(bitmap.isRecycled())
            return;
        bitmap.recycle();
        bitmap = null;
    }

    public Logo duplicate() {
        return new Logo(bitmap != null ? bitmap.copy(Bitmap.Config.ARGB_8888, true) : null,
                scale,
                borderRadius,
                borderWidth,
                clippingRect);
    }
}
