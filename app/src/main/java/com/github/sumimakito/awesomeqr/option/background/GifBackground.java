package com.github.sumimakito.awesomeqr.option.background;

import android.graphics.Bitmap;
import android.graphics.Rect;

import java.io.File;

public class GifBackground extends Background {

    public File oFile = null;
    public File iFile = null;

    public GifBackground() {
    }

    public GifBackground(File oFile, File iFile, float alpha, Rect clippingRect, Bitmap bitmap) {
        super(alpha, clippingRect, bitmap);
        this.oFile = oFile;
        this.iFile = iFile;
    }

    @Override
    public GifBackground duplicate() {
        return new GifBackground(
                oFile,
                iFile,
                alpha,
                clippingRect,
                bitmap != null ? bitmap.copy(Bitmap.Config.ARGB_8888, true) : null);
    }
}
