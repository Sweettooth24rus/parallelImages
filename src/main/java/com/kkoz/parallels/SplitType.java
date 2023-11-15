package com.kkoz.parallels;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum SplitType {
    RGB("RGB"),
    HSV("HSV"),
    YUV("YUV");

    private final String name;

    public Integer getMaxValue(Integer channel) {
        switch (this) {
            case RGB:
                return 255;
            case HSV:
                if (channel == 1) {
                    return 360;
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

    public Integer invert(Integer channel, Integer value, Integer minValue, Integer maxValue) {
        if (minValue == null) {
            return value;
        }
        if (minValue < value && maxValue > value) {
            return getMaxValue(channel) - value;
        }
        return value;
    }
}
