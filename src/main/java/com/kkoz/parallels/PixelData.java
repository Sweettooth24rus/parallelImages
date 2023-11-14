package com.kkoz.parallels;

import lombok.AllArgsConstructor;
import lombok.Data;

@AllArgsConstructor
@Data
public class PixelData {
    private RGB[] rgb;
    private Integer[] channel;
}
