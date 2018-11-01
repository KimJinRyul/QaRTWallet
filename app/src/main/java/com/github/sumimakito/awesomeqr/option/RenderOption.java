package com.github.sumimakito.awesomeqr.option;

import com.github.sumimakito.awesomeqr.option.background.Background;
import com.github.sumimakito.awesomeqr.option.color.Color;
import com.github.sumimakito.awesomeqr.option.logo.Logo;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;

public class RenderOption {
    public String content = "Makes QR Codes Great Again";
    public int size = 600;
    public int borderWidth = 20;
    public boolean clearBorder = true;
    public float patternScale = 0.6f;
    public boolean roundedPatterns = false;
    public Color color = new Color();
    public ErrorCorrectionLevel ecl = ErrorCorrectionLevel.M;
    public Background background = null;
    public Logo logo = null;

    public void setBackground(Background background) {
        if(this.background != null)
            this.background.recycle();
        this.background = background;
    }

    public void setLogo(Logo logo) {
        if(this.logo != null) {
            this.logo.recycle();
        }
        this.logo = logo;
    }

    public void recycle() {
        if(background != null) {
            background.recycle();
            background = null;
        }
        if(logo != null) {
            logo.recycle();
            logo = null;
        }
    }

    public RenderOption duplicate() {
        RenderOption renderOption = new RenderOption();
        renderOption.content = content;
        renderOption.size = size;
        renderOption.borderWidth = borderWidth;
        renderOption.clearBorder = clearBorder;
        renderOption.patternScale = patternScale;
        renderOption.roundedPatterns = roundedPatterns;
        renderOption.color = color;
        renderOption.ecl = ecl;

        renderOption.background = background != null ? background.duplicate() : null;
        renderOption.logo = logo != null ? logo.duplicate() : null;
        return renderOption;
    }
}
