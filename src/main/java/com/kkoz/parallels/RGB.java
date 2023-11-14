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
        return new RGB(rgb.red, 0, 0);
    }

    public static RGB fullGreen(RGB rgb) {
        return new RGB(0, rgb.green, 0);
    }

    public static RGB fullBlue(RGB rgb) {
        return new RGB(0, 0, rgb.blue);
    }

    public static RGB grayScale(Integer value) {
        return new RGB(value, value, value);
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
}
