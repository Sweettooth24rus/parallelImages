package com.kkoz.parallels;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ContourType {
    ROBERTS("Метод Робертса"),
    SOBEL("Метод Собела"),
    LAPLAS("Метод Лапласа");

    private final String name;
}
