package com.kkoz.parallels;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.InputStream;

@AllArgsConstructor
@Data
public class ChannelData {
    private InputStream imageStream;
    private int[] histogram;
}
