package com.github.sumimakito.awesomeqr;

import android.graphics.Bitmap;

import java.io.File;

public class RenderResult {
    public Bitmap bitmap = null;
    public File gifOutputFile = null;
    public OutputType type = OutputType.Still;

    public RenderResult() {
    }

    public RenderResult(Bitmap bitmap, File gifOutputFile, OutputType type) {
        this.bitmap = bitmap;
        this.gifOutputFile = gifOutputFile;
        this.type = type;
    }

    public enum OutputType {
        Still, GIF, Blend
    }
}
