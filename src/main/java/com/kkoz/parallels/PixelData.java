package com.kkoz.parallels;

import lombok.AllArgsConstructor;
import lombok.Data;

@AllArgsConstructor
@Data
public class PixelData {
    private RGB[] rgb;
    private RGB newRGB;
    private Integer[] channel;

    public PixelData(RGB[] rgb, Integer[] channel) {
        this.rgb = rgb;
        this.channel = channel;
    }
}
