package com.github.sumimakito.awesomeqr;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;

import com.github.sumimakito.awesomeqr.option.RenderOption;
import com.github.sumimakito.awesomeqr.option.background.BlendBackground;
import com.github.sumimakito.awesomeqr.option.background.GifBackground;
import com.github.sumimakito.awesomeqr.option.background.StillBackground;
import com.github.sumimakito.awesomeqr.option.logo.Logo;
import com.github.sumimakito.awesomeqr.util.RectUtils;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import com.google.zxing.qrcode.encoder.ByteMatrix;
import com.google.zxing.qrcode.encoder.Encoder;
import com.google.zxing.qrcode.encoder.QRCode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class AwesomeQRRender {

    /**
     * EPT : Empty
     * DTA : Data
     * POS : Position
     * AGN : Align
     * TMG : Timing
     * PTC : Protector, translucent layer(custom block, this is not included in QR code's standards)
     */
    private static final int BYTE_EPT = 0x0;
    private static final int BYTE_DTA = 0x1;
    private static final int BYTE_POS = 0x2;
    private static final int BYTE_AGN = 0x3;
    private static final int BYTE_TMG = 0x4;
    private static final int BYTE_PTC = 0x5;

    public static RenderResult render(RenderOption renderOption) {
        if(renderOption.background instanceof GifBackground) {
            GifBackground background = (GifBackground)renderOption.background;
            if(background.oFile == null) {
                throw new IllegalArgumentException("Output file has not yet been set.");
            }
            GifPipeline gifPipeline = new GifPipeline();
            if(!gifPipeline.init(background.iFile)) {
                throw new IllegalArgumentException("GifPipeline failed to init : " + gifPipeline.errorInfo);
            }
            gifPipeline.clippingRect = background.getClippingRectF();
            gifPipeline.outputFile = background.oFile;
            Bitmap frame = null;
            Bitmap renderedFrame = null;
            Bitmap firstRenderedFrame = null;

            frame = gifPipeline.nextFrame();
            while(frame != null) {
                renderedFrame = renderFrame(renderOption, frame);
                gifPipeline.pushRendered(renderedFrame);
                if(firstRenderedFrame == null) {
                    firstRenderedFrame = renderedFrame.copy(Bitmap.Config.ARGB_8888, true);
                }
                frame = gifPipeline.nextFrame();
            }

            if(gifPipeline.errorInfo != null) {
                throw new IllegalArgumentException("GifPipeline failed to redner frames:" + gifPipeline.errorInfo);
            }
            if(!gifPipeline.postRender()) {
                throw new IllegalArgumentException("GifPipeline failed to do post render works: " + gifPipeline.errorInfo);
            }
            return new RenderResult(firstRenderedFrame, background.oFile, RenderResult.OutputType.GIF);
        } else if(renderOption.background instanceof BlendBackground && renderOption.background.bitmap != null) {
            BlendBackground background = (BlendBackground)renderOption.background;
            Bitmap clippedBackground = null;
            if(background.clippingRect != null) {
                clippedBackground = Bitmap.createBitmap(
                        background.bitmap,
                        Math.round((float)background.clippingRect.left),
                        Math.round((float)background.clippingRect.top),
                        Math.round((float)background.clippingRect.width()),
                        Math.round((float)background.clippingRect.height())
                );
            }
            Bitmap rendered = renderFrame(renderOption, clippedBackground != null ? clippedBackground : background.bitmap);
            if(clippedBackground != null)
                clippedBackground.recycle();

            ArrayList<Rect> scaledBoundingRects = scaleImageBoundingRectByClippingRect(background.bitmap, renderOption.size, background.clippingRect);
            Bitmap fullRendered = Bitmap.createScaledBitmap(background.bitmap, scaledBoundingRects.get(0).width(), scaledBoundingRects.get(0).height(), true);
            Canvas fullCanvas = new Canvas(fullRendered);
            Paint paint = new Paint();
            paint.setAntiAlias(true);
            paint.setColor(renderOption.color.background);
            paint.setFilterBitmap(true);

            fullCanvas.drawBitmap(rendered, new Rect(0, 0, rendered.getWidth(), rendered.getHeight()), scaledBoundingRects.get(1), paint);
            return new RenderResult(fullRendered, null, RenderResult.OutputType.Blend);
        } else if(renderOption.background instanceof StillBackground) {
            StillBackground background = (StillBackground)renderOption.background;
            Bitmap clippedBackground = null;
            if(background.clippingRect != null) {
                clippedBackground = Bitmap.createBitmap(
                        background.bitmap,
                        Math.round((float)background.clippingRect.left),
                        Math.round((float)background.clippingRect.top),
                        Math.round((float)background.clippingRect.width()),
                        Math.round((float)background.clippingRect.height())
                );
            }

            Bitmap rendered = renderFrame(renderOption, clippedBackground != null ? clippedBackground : background.bitmap);

            if(clippedBackground != null)
                clippedBackground.recycle();
            return new RenderResult(rendered, null, RenderResult.OutputType.Still);
        } else {
            return new RenderResult(renderFrame(renderOption, null), null, RenderResult.OutputType.Still);
        }
    }


    private static Bitmap renderFrame(RenderOption renderOption, Bitmap backgroundFrame) {

        Bitmap backgroundFrameTemp  =backgroundFrame;
        if(renderOption.content.isEmpty()) {
            throw new IllegalArgumentException("Error: Content is emtpy.");
        }

        if(renderOption.size < 0) {
            throw new IllegalArgumentException("Error: a negative size is given.");
        }

        if(renderOption.borderWidth < 0) {
            throw  new IllegalArgumentException("Error: a negative borderWidth is give.");
        }

        if(renderOption.size - (2 * renderOption.borderWidth) <= 0) {
            throw  new IllegalArgumentException(("Error: there is no space left for the QRCode."));
        }

        ByteMatrix byteMatrix = getByteMatrix(renderOption.content, renderOption.ecl);
        if(byteMatrix == null) {
            throw new NullPointerException("Error: ByteMatrix based on content is null");
        }

        int innerRenderSize = renderOption.size - (2 * renderOption.borderWidth);
        int nCount = byteMatrix.getWidth();
        int nSize = Math.round((float)innerRenderSize / nCount);
        int unscaledInnerRenderSize = nSize * nCount;
        int unscaledFullRenderSize = unscaledInnerRenderSize + (2 * renderOption.borderWidth);

        if(renderOption.size - 2 * renderOption.borderWidth < byteMatrix.getWidth()) {
            throw new IllegalArgumentException("Error: there is no space left for the QRCode");
        }

        if(renderOption.patternScale <= 0 || renderOption.patternScale > 1) {
            throw new IllegalArgumentException("Error: an illegal pattern scale is given");
        }

        if(renderOption.logo != null && renderOption.logo.bitmap != null) {
            Logo logo = renderOption.logo;
            if(logo.scale <= 0 || logo.scale > 0.5) {
                throw new IllegalArgumentException("Error: an illegal logo scale is given");
            }

            if(logo.borderWidth < 0 || logo.borderWidth * 2 >= unscaledInnerRenderSize) {
                throw new IllegalArgumentException("Error: an illegal logo border width is give.");
            }

            if(logo.borderRadius < 0) {
                throw new IllegalArgumentException("Error: a negative logo border radius is given");
            }

            int logoScaledSize = (int)(unscaledInnerRenderSize * logo.scale);
            if(logo.borderRadius * 2 > logoScaledSize) {
                throw new IllegalArgumentException("Error: an illegal logo border radius is given");
            }
        }

        Rect backgroundDrawingRect = new Rect(
                renderOption.clearBorder ? renderOption.borderWidth : 0,
                renderOption.clearBorder ? renderOption.borderWidth : 0,
                unscaledFullRenderSize - renderOption.borderWidth * (renderOption.clearBorder ? 1 : 0),
                unscaledFullRenderSize - renderOption.borderWidth * (renderOption.clearBorder ? 1 : 0));

        if(backgroundFrameTemp == null) {
            if(renderOption.background instanceof  StillBackground ||
                    renderOption.background instanceof  BlendBackground) {
                backgroundFrameTemp = renderOption.background.bitmap;
            }
        }

        Bitmap unscaledFullRenderedBitmap = Bitmap.createBitmap(unscaledFullRenderSize, unscaledFullRenderSize, Bitmap.Config.ARGB_8888);

        if(renderOption.color.auto && backgroundFrame != null) {
            renderOption.color.light = -0x1;
            renderOption.color.dark = getDominantColor(backgroundFrame);
        }

        Paint paint = new Paint();
        paint.setAntiAlias(true);

        Paint paintBackground = new Paint();
        paintBackground.setAntiAlias(true);
        paintBackground.setColor(renderOption.color.background);
        paintBackground.setStyle(Paint.Style.FILL);

        Paint paintDark = new Paint();
        paintDark.setColor(renderOption.color.dark);
        paintDark.setAntiAlias(true);
        paintDark.setStyle(Paint.Style.FILL);

        Paint paintLight = new Paint();
        paintLight.setColor(renderOption.color.light);
        paintLight.setAntiAlias(true);
        paintLight.setStyle(Paint.Style.FILL);

        Paint paintProtector = new Paint();
        paintProtector.setColor(Color.argb(120, 255, 255, 255));
        paintProtector.setAntiAlias(true);
        paintProtector.setStyle(Paint.Style.FILL);

        Canvas unscaledCanvas = new Canvas(unscaledFullRenderedBitmap);
        unscaledCanvas.drawColor(Color.WHITE);
        unscaledCanvas.drawRect(
                (float)(renderOption.clearBorder ? renderOption.borderWidth : 0),
                (float)(renderOption.clearBorder ? renderOption.borderWidth : 0),
                (float)(unscaledInnerRenderSize + (renderOption.clearBorder ? renderOption.borderWidth : renderOption.borderWidth * 2)),
                (float)(unscaledInnerRenderSize + (renderOption.clearBorder ? renderOption.borderWidth : renderOption.borderWidth * 2)),
                paintBackground);

        if(backgroundFrame != null && renderOption.background != null) {
            paint.setAlpha(Math.round(255 * renderOption.background.alpha));
            unscaledCanvas.drawBitmap(
                    backgroundFrame, null,
                    backgroundDrawingRect, paint);
        }
        paint.setAlpha(255);

        for(int row = 0; row < byteMatrix.getHeight(); row++) {
            for(int col = 0; col < byteMatrix.getWidth(); col++) {
                switch (byteMatrix.get(col, row)) {
                    case BYTE_AGN:
                    case BYTE_POS:
                    case BYTE_TMG:
                        unscaledCanvas.drawRect(
                                (float)(renderOption.borderWidth + col * nSize),
                                (float)(renderOption.borderWidth + row * nSize),
                                (float)(renderOption.borderWidth + (col + 1) * nSize),
                                (float)(renderOption.borderWidth + (row + 1) * nSize),
                                paintDark
                        );
                        break;

                    case BYTE_DTA:
                        if(renderOption.roundedPatterns) {
                            unscaledCanvas.drawCircle(
                                    renderOption.borderWidth + (col + 0.5f) * nSize,
                                    renderOption.borderWidth + (row + 0.5f) * nSize,
                                    renderOption.patternScale * (float)nSize * 0.5f,
                                    paintDark
                            );
                        } else {
                            unscaledCanvas.drawRect(
                                    renderOption.borderWidth + (col + 0.5f * (1 - renderOption.patternScale)) * nSize,
                                    renderOption.borderWidth + (row  + 0.5f * (1 - renderOption.patternScale)) * nSize,
                                    renderOption.borderWidth + (col + 0.5f * (1 + renderOption.patternScale)) * nSize,
                                    renderOption.borderWidth + (row + 0.5f * (1 + renderOption.patternScale)) * nSize,
                                    paintDark
                            );
                        }
                        break;
                    case BYTE_PTC:
                        unscaledCanvas.drawRect(
                                (float)(renderOption.borderWidth + col * nSize),
                                (float)(renderOption.borderWidth + row * nSize),
                                (float)(renderOption.borderWidth + (col + 1) * nSize),
                                (float)(renderOption.borderWidth + (row + 1) * nSize),
                                paintProtector
                        );
                        break;

                    case BYTE_EPT:
                        if(renderOption.roundedPatterns) {
                            unscaledCanvas.drawCircle(
                                    renderOption.borderWidth + (col + 0.5f) * nSize,
                                    renderOption.borderWidth + (row + 0.5f) * nSize,
                                    renderOption.patternScale * (float)nSize * 0.5f,
                                    paintLight
                            );
                        } else {
                            unscaledCanvas.drawRect(
                                    renderOption.borderWidth + (col + 0.5f * (1 - renderOption.patternScale)) * nSize,
                                    renderOption.borderWidth + (row  + 0.5f * (1 - renderOption.patternScale)) * nSize,
                                    renderOption.borderWidth + (col + 0.5f * (1 + renderOption.patternScale)) * nSize,
                                    renderOption.borderWidth + (row + 0.5f * (1 + renderOption.patternScale)) * nSize,
                                    paintLight
                            );
                        }
                        break;
                }
            }
        }

        if(renderOption.logo != null && renderOption.logo.bitmap != null) {
            Logo logo = renderOption.logo;
            int logoScaledSize = (int)(unscaledInnerRenderSize * logo.scale);
            Bitmap logoScaled = Bitmap.createScaledBitmap(logo.bitmap, logoScaledSize, logoScaledSize, true);
            Bitmap logoOpt = Bitmap.createBitmap(logoScaled.getWidth(), logoScaled.getHeight(), Bitmap.Config.ARGB_8888);
            Canvas logoCanvas = new Canvas(logoOpt);
            Rect logoRect = new Rect(0, 0, logoScaled.getWidth(), logoScaled.getHeight());
            RectF logoRectF = new RectF(logoRect);
            Paint logoPaint = new Paint();
            logoPaint.setAntiAlias(true);
            logoPaint.setColor(-0x1);
            logoPaint.setStyle(Paint.Style.FILL);
            logoCanvas.drawARGB(0, 0, 0, 0);
            logoCanvas.drawRoundRect(logoRectF, (float)logo.borderRadius, (float)logo.borderRadius, logoPaint);
            logoPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
            logoCanvas.drawBitmap(logoScaled, logoRect, logoRect, logoPaint);
            logoPaint.setColor(renderOption.color.light);
            logoPaint.setStyle(Paint.Style.STROKE);
            logoPaint.setStrokeWidth(logo.borderWidth);
            logoCanvas.drawRoundRect(logoRectF, logo.borderRadius, logo.borderRadius, logoPaint);
            unscaledCanvas.drawBitmap(
                    logoOpt,
                    (0.5f * (unscaledFullRenderedBitmap.getWidth() - logoOpt.getWidth())),
                    (0.5f * (unscaledFullRenderedBitmap.getHeight() - logoOpt.getHeight())),
                    paint);
        }

        Bitmap renderedScaledBitmap = Bitmap.createBitmap(
                renderOption.size,
                renderOption.size,
                Bitmap.Config.ARGB_8888
        );

        Canvas scaledCanvas = new Canvas(renderedScaledBitmap);
        scaledCanvas.drawBitmap(unscaledFullRenderedBitmap, null, new Rect(0, 0, renderedScaledBitmap.getWidth(), renderedScaledBitmap.getHeight()), paint);

        Bitmap renderedResultBitmap = null;
        if(renderOption.background instanceof BlendBackground) {
            renderedResultBitmap = Bitmap.createBitmap(
                    renderedScaledBitmap.getWidth(),
                    renderedScaledBitmap.getHeight(),
                    Bitmap.Config.ARGB_8888
            );

            Canvas finalRenderedCanvas = new Canvas(renderedResultBitmap);
            Rect finalClippingRect = new Rect(0, 0, renderedScaledBitmap.getWidth(), renderedScaledBitmap.getHeight());
            RectF finalClippingRectF = new RectF(finalClippingRect);
            Paint finalClippingPaint = new Paint();
            finalClippingPaint.setAntiAlias(true);
            finalClippingPaint.setColor(-0x1);
            finalClippingPaint.setStyle(Paint.Style.FILL);
            finalRenderedCanvas.drawARGB(0, 0, 0, 0);
            finalRenderedCanvas.drawRoundRect(finalClippingRectF,
                    ((BlendBackground)renderOption.background).borderRadius,
                    ((BlendBackground)renderOption.background).borderRadius,
                    finalClippingPaint
            );
            finalClippingPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
            finalRenderedCanvas.drawBitmap(renderedScaledBitmap, null, finalClippingRect, finalClippingPaint);

            renderedScaledBitmap.recycle();
        } else {
            renderedResultBitmap = renderedScaledBitmap;
        }
        unscaledFullRenderedBitmap.recycle();
        return renderedResultBitmap;

    }

    private static ByteMatrix getByteMatrix(String contents, ErrorCorrectionLevel errorCorrectionLevel) {
        try {
            QRCode qrCode = getProtoQRCode(contents, errorCorrectionLevel);
            int[] agnCenter = qrCode.getVersion().getAlignmentPatternCenters();
            ByteMatrix byteMatrix = qrCode.getMatrix();
            int matSize = byteMatrix.getWidth();

            for(int row = 0; row < matSize; row++) {
                for(int col = 0; col < matSize; col++) {
                    if(isTypeAGN(col, row, agnCenter, true)) {
                        if(byteMatrix.get(col, row) != BYTE_EPT) {
                            byteMatrix.set(col, row, BYTE_AGN);
                        } else {
                            byteMatrix.set(col, row, BYTE_PTC);
                        }
                    } else if (isTypePOS(col, row, matSize, true)) {
                        if (byteMatrix.get(col, row) != BYTE_EPT) {
                            byteMatrix.set(col, row, BYTE_POS);
                        } else {
                            byteMatrix.set(col, row, BYTE_PTC);
                        }
                    } else if (isTypeTMG(col, row, matSize)) {
                        if (byteMatrix.get(col, row) != BYTE_EPT) {
                            byteMatrix.set(col, row, BYTE_TMG);
                        } else {
                            byteMatrix.set(col, row, BYTE_PTC);
                        }
                    }

                    if (isTypePOS(col, row, matSize, false)) {
                        if (byteMatrix.get(col, row) == BYTE_EPT) {
                            byteMatrix.set(col, row, BYTE_PTC);
                        }
                    }
                }
            }
            return byteMatrix;

        } catch (WriterException e) {
            e.printStackTrace();
        }
        return null;
    }

    private static QRCode getProtoQRCode(String contents, ErrorCorrectionLevel errorCorrectionLevel) throws WriterException {
        if (contents.isEmpty()) {
            throw new IllegalArgumentException("Found empty contents");
        }
        Map<EncodeHintType, Object> hintMap = new HashMap<>();
        hintMap.put(EncodeHintType.CHARACTER_SET, "UTF-8");
        hintMap.put(EncodeHintType.ERROR_CORRECTION, errorCorrectionLevel);
        return Encoder.encode(contents, errorCorrectionLevel, hintMap);
    }

    private static boolean isTypeAGN(int x, int y, int[] agnCenter, boolean edgeOnly) {
        if (agnCenter.length == 0) return false;
        int edgeCenter = agnCenter[agnCenter.length - 1];
        for (int agnY : agnCenter) {
            for (int agnX : agnCenter) {
                if (edgeOnly && agnX != 6 && agnY != 6 && agnX != edgeCenter && agnY != edgeCenter)
                    continue;
                if ((agnX == 6 && agnY == 6) || (agnX == 6 && agnY == edgeCenter) || (agnY == 6 && agnX == edgeCenter))
                    continue;
                if (x >= agnX - 2 && x <= agnX + 2 && y >= agnY - 2 && y <= agnY + 2)
                    return true;
            }
        }
        return false;
    }

    private static boolean isTypePOS(int x, int y, int size, boolean inner) {
        if (inner) {
            return ((x < 7 && (y < 7 || y >= size - 7)) || (x >= size - 7 && y < 7));
        } else {
            return ((x <= 7 && (y <= 7 || y >= size - 8)) || (x >= size - 8 && y <= 7));
        }
    }

    private static boolean isTypeTMG(int x, int y, int size) {
        return (y == 6 && x >= 8 && x < size - 8) || (x == 6 && y >= 8 && y < size - 8);
    }

    private static void scaleBitmap(Bitmap src, Bitmap dst) {
        Paint cPaint = new Paint();
        cPaint.setAntiAlias(true);
        cPaint.setDither(true);
        cPaint.setFilterBitmap(true);

        float ratioX = dst.getWidth() / (float)src.getWidth();
        float ratioY = dst.getHeight() / (float)src.getHeight();
        float middleX = dst.getWidth() * 0.5f;
        float middleY = dst.getHeight() * 0.5f;

        Matrix scaleMatrix = new Matrix();
        scaleMatrix.setScale(ratioX, ratioY, middleX, middleY);
        Canvas canvas = new Canvas(dst);
        canvas.setMatrix(scaleMatrix);
        canvas.drawBitmap(src, middleX - src.getWidth() / 2,
                middleY - src.getHeight() / 2,
                cPaint);
    }

    private static int getDominantColor(Bitmap bitmap) {
        Bitmap newBitmap = Bitmap.createScaledBitmap(bitmap, 8, 8, true);
        int red = 0;
        int green = 0;
        int blue = 0;
        int c = 0;
        int r, g, b;
        int color;
        for(int y = 0; y < newBitmap.getHeight(); y++) {
            for(int x = 0; x < newBitmap.getWidth(); x++) {
                color = newBitmap.getPixel(x, y);
                r = (color >> 16) & 0xFF;
                g = (color >> 8) & 0xFF;
                b = color & 0xFF;

                if(r > 200 || g > 200 || b > 200)
                    continue;

                red += r;
                green += g;
                blue += b;
                c++;
            }
        }

        newBitmap.recycle();
        if(c == 0) {
            return -0x1000000;
        } else {
            red = Math.max(0, Math.min(0xFF, red / c));
            green = Math.max(0, Math.min(0xFF, green / c));
            blue = Math.max(0, Math.min(0xFF, blue / c));

            float [] hsv = new float[3];
            Color.RGBToHSV(red, green, blue, hsv);
            hsv[2] = Math.max(hsv[2], 0.7f);
            return 0xFF000000 | Color.HSVToColor(hsv);
        }
    }


    private static ArrayList<Rect> scaleImageBoundingRectByClippingRect(Bitmap bitmap, int size, Rect clippingRect) {

        ArrayList<Rect> arrayList = new ArrayList<>();
        if(clippingRect == null)
            return scaleImageBoundingRectByClippingRect(bitmap, size, new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight()));

        if(clippingRect.width() != clippingRect.height() || clippingRect.width() <= size) {
            arrayList.add(new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight()));
            arrayList.add(clippingRect);
            return arrayList;
        }

        float clippingSize = clippingRect.width();
        float scalingRatio = size / clippingSize;

        arrayList.add(RectUtils.round(new RectF(0.f, 0.f, bitmap.getWidth() * scalingRatio, bitmap.getHeight() * scalingRatio)));
        arrayList.add(RectUtils.round(new RectF(clippingRect.left * scalingRatio, clippingRect.top * scalingRatio, clippingRect.right * scalingRatio, clippingRect.bottom * scalingRatio)));
        return arrayList;
    }
}
