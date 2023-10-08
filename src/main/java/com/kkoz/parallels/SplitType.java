package com.kkoz.parallels;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum SplitType {
    RGB("RGB"),
    HSV("HSV");

    private final String name;
}
