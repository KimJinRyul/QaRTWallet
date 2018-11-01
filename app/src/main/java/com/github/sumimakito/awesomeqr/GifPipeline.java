package com.github.sumimakito.awesomeqr;

import android.graphics.Bitmap;
import android.graphics.RectF;

import com.waynejo.androidndkgif.GifDecoder;
import com.waynejo.androidndkgif.GifEncoder;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.LinkedList;

public class GifPipeline {
    public File outputFile = null;
    public RectF clippingRect = null;
    public String errorInfo = null;

    private GifDecoder gifDecoder = null;
    private LinkedList<Bitmap> frameSequence = new LinkedList<>();
    private int currentFrame = 0;

    public boolean init(File file) {
        if(!file.exists()) {
            errorInfo = "ENOENT: File does not exist.";
            return false;
        } else if(file.isDirectory()) {
            errorInfo = "EISDIR: Target is a directory.";
            return false;
        }

        gifDecoder = new GifDecoder();
        boolean isSucceeded = gifDecoder.load(file.getAbsolutePath());
        if(!isSucceeded) {
            errorInfo = "Failed to decode input file as GIF";
            return false;
        }
        return true;
    }

    public Bitmap nextFrame() {
        if(gifDecoder == null) {
            errorInfo = "Not initialized yet.";
            return null;
        }

        if(gifDecoder.frameNum() == 0) {
            errorInfo = "GIF contains zero frames.";
            return null;
        }

        if(clippingRect == null) {
            errorInfo = "No cripping rect provied.";
            return null;
        }

        if(currentFrame < gifDecoder.frameNum()) {
            Bitmap frame = gifDecoder.frame(currentFrame);
            currentFrame++;

            if(clippingRect != null) {
                Bitmap cropped = Bitmap.createBitmap(frame,
                        Math.round(clippingRect.left),
                        Math.round(clippingRect.top),
                        Math.round(clippingRect.width()),
                        Math.round(clippingRect.height()));
                frame.recycle();
                return cropped;
            }
            return frame;
        } else {
            return null;
        }
    }

    public void pushRendered(Bitmap bitmap) {
        frameSequence.addLast(bitmap);
    }

    public boolean postRender() {
        if(outputFile == null) {
            errorInfo = "Output file is not yet set.";
            return false;
        }

        if(frameSequence.size() == 0) {
            errorInfo = "Zero frames in the sequence.";
            return false;
        }

        try {
            GifEncoder gifEncoder = new GifEncoder();
            gifEncoder.init(frameSequence.getFirst().getWidth(),
                    frameSequence.getFirst().getHeight(),
                    outputFile.getAbsolutePath(),
                    GifEncoder.EncodingType.ENCODING_TYPE_FAST);
            int frameIndex = 0;
            while(!frameSequence.isEmpty()) {
                gifEncoder.encodeFrame(frameSequence.removeFirst(), gifDecoder.delay(frameIndex));
            }
            gifEncoder.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            errorInfo = "FileNotFoundException. See stacktrace for more information.";
            return false;
        }
        return true;
    }

    public boolean release() {
        return true;
    }

}
