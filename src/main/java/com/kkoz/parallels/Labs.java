package com.kkoz.parallels;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Map;

@Getter
@RequiredArgsConstructor
public enum Labs {
    LAB_1("http://localhost:8080/labs/1"),
    LAB_2("http://localhost:8080/labs/2");

    private final String url;

    public Map<String, String> getLabs() {
        var start = "http://localhost:8080/labs/";

        switch (this) {
            case LAB_1 -> {
                start += "1/";
                return Map.of(
                    "Каналы", start + "channels",
                    "Яркость/контрастность", start + "filters",
                    "Инверсия", start + "inversion",
                    "Серый мир", start + "grey_world"
                );
            }
            case LAB_2 -> {
                start += "2/";
                return Map.of(
                    "Импульсный шум", start + "noises/impulse",
                    "Аддитивный шум", start + "noises/additive",
                    "Мультипликативный шум", start + "noises/multiplicative",
                    "Линейная фильтрация", start + "filters/linear",
                    "Spatial smoother", start + "filters/spatial",
                    "Kuwahara", start + "filters/kuwahara",
                    "Рекурсивный среднеарифметический", start + "filters/recursive_avg",
                    "Быстрый медианный", start + "filters/fast_median"
                );
            }
            default -> {
                return Map.of();
            }
        }
    }
}
