package com.kkoz.parallels;

import lombok.Data;

import java.awt.*;

@Data
public class RGB {
    private Integer red;
    private Integer green;
    private Integer blue;

    public RGB(Integer red, Integer green, Integer blue) {
        this.red = checkBorderValues(red);
        this.green = checkBorderValues(green);
        this.blue = checkBorderValues(blue);
    }

    public RGB(Double red, Double green, Double blue) {
        this(red.intValue(), green.intValue(), blue.intValue());
    }

    public static RGB fullRed(RGB rgb) {
        return fullRed(rgb.red);
    }

    public static RGB fullRed(Integer red) {
        return new RGB(red, 0, 0);
    }

    public static RGB fullGreen(RGB rgb) {
        return fullGreen(rgb.green);
    }

    public static RGB fullGreen(Integer green) {
        return new RGB(0, green, 0);
    }

    public static RGB fullBlue(RGB rgb) {
        return fullBlue(rgb.blue);
    }

    public static RGB fullBlue(Integer blue) {
        return new RGB(0, 0, blue);
    }

    public static RGB grayScale(Integer value) {
        return new RGB(value, value, value);
    }

    public static RGB invertRGB(RGB rgb) {
        return new RGB(
            255 - rgb.red,
            255 - rgb.green,
            255 - rgb.blue
        );

    }

    public int getRGB() {
        return new Color(red, green, blue).getRGB();
    }

    public static Integer checkBorderValues(Integer value) {
        if (value > 255) {
            return 255;
        }
        if (value < 0) {
            return 0;
        }
        return value;
    }

    public static Integer checkBorderValues(Double value) {
        return checkBorderValues(value.intValue());
    }
}
