package com.kkoz.parallels;

import lombok.AllArgsConstructor;

import java.awt.*;

@AllArgsConstructor
public class RGB {
    private Integer red;
    private Integer green;
    private Integer blue;

    public static RGB fullRed(RGB rgb) {
        return new RGB(rgb.red, 0, 0);
    }

    public static RGB fullGreen(RGB rgb) {
        return new RGB(0, rgb.green, 0);
    }

    public static RGB fullBlue(RGB rgb) {
        return new RGB(0, 0, rgb.blue);
    }

    public int getRGB() {
        return new Color(red, green, blue).getRGB();
    }
}
