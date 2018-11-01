package com.github.sumimakito.awesomeqr.option.background;

import android.graphics.Bitmap;
import android.graphics.Rect;
import android.graphics.RectF;

import com.github.sumimakito.awesomeqr.util.RectUtils;

abstract public class Background {

    public float alpha = 0.6f;
    public Rect clippingRect = null;
    public Bitmap bitmap = null;

    public Background() {
    }

    public Background(float alpha, Rect clippingRect, Bitmap bitmap) {
        this.alpha = alpha;
        this.clippingRect = clippingRect;
        this.bitmap = bitmap;
    }

    public RectF getClippingRectF() {
        if(clippingRect == null)
            return null;
        else
            return new RectF(clippingRect);
    }

    public void setClippingRectF(RectF value) {
        if(value != null) {
            this.clippingRect = RectUtils.round(value);
        } else {
            this.clippingRect = null;
        }
    }

    public void recycle() {
        if(bitmap == null)
            return;
        if(bitmap.isRecycled())
            return;

        bitmap.recycle();
        bitmap = null;
    }

    public abstract Background duplicate();
}
