package com.kkoz.parallels;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.tuple.Pair;

import java.util.List;

@Getter
@RequiredArgsConstructor
public enum Labs {
    LAB_1("http://localhost:8080/labs/1"),
    LAB_2("http://localhost:8080/labs/2"),
    LAB_3("http://localhost:8080/labs/3");

    private final String url;

    public List<Pair<String, String>> getLabs() {
        var start = "http://localhost:8080/labs/";

        switch (this) {
            case LAB_1 -> {
                start += "1/";
                return List.of(
                    Pair.of("Каналы", start + "channels"),
                    Pair.of("Яркость/контрастность", start + "filters"),
                    Pair.of("Инверсия", start + "inversion"),
                    Pair.of("Серый мир", start + "grey_world")
                );
            }
            case LAB_2 -> {
                start += "2/";
                return List.of(
                    Pair.of("Импульсный шум", start + "noises/impulse"),
                    Pair.of("Аддитивный шум", start + "noises/additive"),
                    Pair.of("Мультипликативный шум", start + "noises/multiplicative"),
                    Pair.of("Линейная фильтрация", start + "filters/linear"),
                    Pair.of("Spatial smoother", start + "filters/spatial"),
                    Pair.of("Kuwahara", start + "filters/kuwahara"),
                    Pair.of("Рекурсивный среднеарифметический", start + "filters/recursive_avg"),
                    Pair.of("Быстрый медианный", start + "filters/fast_median")
                );
            }
            case LAB_3 -> {
                start += "3/";
                return List.of(
                    Pair.of("Контурное представление", start + "contour")
                );
            }
            default -> {
                return List.of();
            }
        }
    }
}
