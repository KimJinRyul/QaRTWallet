package com.github.sumimakito.awesomeqr.util;

import android.graphics.Rect;
import android.graphics.RectF;

public class RectUtils {
    public static Rect round (RectF rectF) {
        return new Rect(Math.round(rectF.left),
                Math.round(rectF.top),
                Math.round(rectF.right),
                Math.round(rectF.bottom));
    }
}
