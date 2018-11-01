package com.github.sumimakito.awesomeqr.option.color;

public class Color {
    public boolean auto = false;
    public int background = 0xffffbbaa;
    public int light = 0xffffffff;
    public int dark = 0xffe57373;

    public Color() {
    }

    public Color(boolean auto, int background, int light, int dark) {
        this.auto = auto;
        this.background = background;
        this.light = light;
        this.dark = dark;
    }

    public Color duplicate() {
        return new Color(auto, background, light, dark);
    }
}
