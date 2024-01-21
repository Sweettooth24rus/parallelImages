package com.kkoz.parallels;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public enum SplitType {
    RGB("RGB"),
    HSV("HSV"),
    YUV("YUV"),
    GREYSCALE("Grayscale");

    private final String name;

    public static final List<SplitType> RGB_HSV_YUV = List.of(RGB, HSV, YUV);

    public Integer getMaxValue(Integer channel) {
        switch (this) {
            case RGB:
            case GREYSCALE:
                return 255;
            case HSV:
                if (channel == 1) {
                    return 359;
                }
                return 255;
            case YUV:
                if (channel == 1) {
                    return 255;
                }
                if (channel == 2) {
                    return 224;
                }
                return 314;
            default:
                return 0;
        }
    }

    public Integer getMaxValueCapacity(Integer channel) {
        return getMaxValue(channel) + 1;
    }

    public int invert(Integer channel, int value, Integer minValue, Integer maxValue) {
        if (minValue == null) {
            return value;
        }
        if (minValue <= value && maxValue >= value) {
            return getMaxValue(channel) - value;
        }
        return value;
    }

    public float invert(Integer channel, float value, Integer minValue, Integer maxValue) {
        if (minValue == null) {
            return value;
        }
        if (minValue <= value && maxValue >= value) {
            return getMaxValue(channel) - value;
        }
        return value;
    }

    public float invertHue(float value, Integer minValue, Integer maxValue) {
        if (minValue == null) {
            return value;
        }
        if (minValue <= value && maxValue >= value) {
            return (value + 180) % 360;
        }
        return value;
    }
}
