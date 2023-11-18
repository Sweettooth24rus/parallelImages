package com.kkoz.parallels.lab_2;

import com.kkoz.parallels.*;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class Lab2AdditiveNoisePresenter extends Presenter<Lab2AdditiveNoiseView> {

    public Lab2AdditiveNoisePresenter(Lab2AdditiveNoiseView view) {
        super(view);
    }

    public void splitImageToChannels(InputStream imageStream,
                                     SplitType type,
                                     String channel1NoisePercentValue,
                                     String channel2NoisePercentValue,
                                     String channel3NoisePercentValue,
                                     String channel1MaximumDeviationValue,
                                     String channel2MaximumDeviationValue,
                                     String channel3MaximumDeviationValue) {
        try {
            var channel1NoisePercent = Integer.parseInt(channel1NoisePercentValue);
            var channel2NoisePercent = Integer.parseInt(channel2NoisePercentValue);
            var channel3NoisePercent = Integer.parseInt(channel3NoisePercentValue);
            var channel1MaximumDeviation = Integer.parseInt(channel1MaximumDeviationValue);
            var channel2MaximumDeviation = Integer.parseInt(channel2MaximumDeviationValue);
            var channel3MaximumDeviation = Integer.parseInt(channel3MaximumDeviationValue);

            var bufferedImage = ImageIO.read(imageStream);

            var width = bufferedImage.getWidth();
            var height = bufferedImage.getHeight();

            var redMatrix = new ArrayList<List<Integer>>(width);
            var greenMatrix = new ArrayList<List<Integer>>(width);
            var blueMatrix = new ArrayList<List<Integer>>(width);

            int[] channel1 = new int[0];
            int[] channel2 = new int[0];
            int[] channel3 = new int[0];

            switch (type) {
                case RGB -> {
                    channel1 = new int[256];
                    channel2 = new int[256];
                    channel3 = new int[256];
                }
                case HSV -> {
                    channel1 = new int[360];
                    channel2 = new int[256];
                    channel3 = new int[256];
                }
                case YUV -> {
                    channel1 = new int[256];
                    channel2 = new int[225];
                    channel3 = new int[315];
                }
            }

            for (var x = 0; x < width; x++) {
                var redHeight = new ArrayList<Integer>(height);
                var greenHeight = new ArrayList<Integer>(height);
                var blueHeight = new ArrayList<Integer>(height);

                for (var y = 0; y < height; y++) {
                    var rgb = bufferedImage.getRGB(x, y);
                    var color = new Color(rgb);

                    redHeight.add(color.getRed());
                    greenHeight.add(color.getGreen());
                    blueHeight.add(color.getBlue());
                }
                redMatrix.add(x, redHeight);
                greenMatrix.add(x, greenHeight);
                blueMatrix.add(x, blueHeight);
            }

            var bufferedImageFirst = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
            var bufferedImageSecond = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
            var bufferedImageThird = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

            for (var x = 0; x < width; x++) {
                var redHeight = redMatrix.get(x);
                var greenHeight = greenMatrix.get(x);
                var blueHeight = blueMatrix.get(x);

                for (var y = 0; y < height; y++) {
                    var sourceRGB = new RGB(redHeight.get(y), greenHeight.get(y), blueHeight.get(y));
                    var pixelData = splitImage(
                        sourceRGB,
                        type,
                        channel1NoisePercent,
                        channel2NoisePercent,
                        channel3NoisePercent,
                        channel1MaximumDeviation,
                        channel2MaximumDeviation,
                        channel3MaximumDeviation
                    );
                    var rgbArray = pixelData.getRgb();
                    var channelArray = pixelData.getChannel();
                    var newRGB = pixelData.getNewRGB();

                    bufferedImage.setRGB(x, y, newRGB.getRGB());
                    bufferedImageFirst.setRGB(x, y, rgbArray[0].getRGB());
                    bufferedImageSecond.setRGB(x, y, rgbArray[1].getRGB());
                    bufferedImageThird.setRGB(x, y, rgbArray[2].getRGB());

                    channel1[channelArray[0]]++;
                    channel2[channelArray[1]]++;
                    channel3[channelArray[2]]++;
                }
            }

            view.refreshPhotosSection(getInputStreamFromBufferedImage(bufferedImage));

            view.refreshChannelsSection(
                new ChannelData(
                    getInputStreamFromBufferedImage(bufferedImageFirst),
                    channel1
                ),
                new ChannelData(
                    getInputStreamFromBufferedImage(bufferedImageSecond),
                    channel2
                ),
                new ChannelData(
                    getInputStreamFromBufferedImage(bufferedImageThird),
                    channel3
                )
            );
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private PixelData splitImage(RGB sourceRGB,
                                 SplitType type,
                                 Integer channel1NoisePercent,
                                 Integer channel2NoisePercent,
                                 Integer channel3NoisePercent,
                                 Integer channel1MaximumDeviation,
                                 Integer channel2MaximumDeviation,
                                 Integer channel3MaximumDeviation) {
        var rgbArray = new RGB[3];
        var channelArray = new Integer[3];

        var r = 0f;
        var g = 0f;
        var b = 0f;

        switch (type) {
            case RGB -> {
                r = sourceRGB.getRed();
                g = sourceRGB.getGreen();
                b = sourceRGB.getBlue();

                if (Math.random() * 100 <= channel1NoisePercent) {
                    r += channel1MaximumDeviation;
                    r = Math.max(0, Math.min(255, r));
                }

                if (Math.random() * 100 <= channel2NoisePercent) {
                    g += channel2MaximumDeviation;
                    g = Math.max(0, Math.min(255, g));
                }

                if (Math.random() * 100 <= channel3NoisePercent) {
                    b += channel3MaximumDeviation;
                    b = Math.max(0, Math.min(255, b));
                }

                rgbArray[0] = RGB.fullRed((int) r);
                rgbArray[1] = RGB.fullGreen((int) g);
                rgbArray[2] = RGB.fullBlue((int) b);

                channelArray[0] = (int) r;
                channelArray[1] = (int) g;
                channelArray[2] = (int) b;
            }
            case HSV -> {
                var red = sourceRGB.getRed();
                var green = sourceRGB.getGreen();
                var blue = sourceRGB.getBlue();

                var cmax = Math.max(Math.max(red, green), blue);
                var cmin = Math.min(Math.min(red, green), blue);

                double saturation;
                double hue;

                if (cmax == 0) {
                    saturation = 0;
                } else {
                    saturation = (double) (cmax - cmin) / cmax;
                }

                if (saturation == 0)
                    hue = 0;
                else {
                    if (red == cmax) {
                        hue = (double) (green - blue) / (cmax - cmin);
                    } else if (green == cmax) {
                        hue = 2 + (double) (blue - red) / (cmax - cmin);
                    } else {
                        hue = 4 + (double) (red - green) / (cmax - cmin);
                    }
                    hue /= 6;
                    if (hue < 0)
                        hue++;
                }

                if (Math.random() * 100 <= channel1NoisePercent) {
                    hue *= 360;
                    hue += channel1MaximumDeviation;
                    hue = hue % 360;
                    hue /= 360;
                }

                if (Math.random() * 100 <= channel2NoisePercent) {
                    saturation *= 255;
                    saturation += channel2MaximumDeviation;
                    saturation = Math.min(255, Math.max(0, saturation));
                    saturation /= 255;
                }

                if (Math.random() * 100 <= channel3NoisePercent) {
                    cmax = Math.min(255, Math.max(0, cmax + channel3MaximumDeviation));
                }

                var h = (hue - Math.floor(hue)) * 6.0;
                var f = (int) ((h - Math.floor(h)) * 255 + 0.5);
                var q = (255 - f);
                switch ((int) h) {
                    case 0:
                        rgbArray[0] = new RGB(255, f, 0);
                        break;
                    case 1:
                        rgbArray[0] = new RGB(q, 255, 0);
                        break;
                    case 2:
                        rgbArray[0] = new RGB(0, 255, f);
                        break;
                    case 3:
                        rgbArray[0] = new RGB(0, q, 255);
                        break;
                    case 4:
                        rgbArray[0] = new RGB(f, 0, 255);
                        break;
                    case 5:
                        rgbArray[0] = new RGB(255, 0, q);
                        break;
                }

                rgbArray[1] = RGB.grayScale((int) (saturation * 255));

                rgbArray[2] = RGB.grayScale(cmax);

                channelArray[0] = (int) (hue * 360);
                channelArray[1] = (int) (saturation * 255);
                channelArray[2] = cmax;

                saturation *= 255;

                var p = cmax * (255 - saturation) / 255;
                q = (int) (cmax * (255 - saturation * f / 255) / 255);
                var t = cmax * (255 - (saturation * (255 - f) / 255)) / 255;
                switch ((int) h) {
                    case 0:
                        r = cmax;
                        g = (float) t;
                        b = (float) p;
                        break;
                    case 1:
                        r = q;
                        g = cmax;
                        b = (float) p;
                        break;
                    case 2:
                        r = (float) p;
                        g = cmax;
                        b = (float) t;
                        break;
                    case 3:
                        r = (float) p;
                        g = q;
                        b = cmax;
                        break;
                    case 4:
                        r = (float) t;
                        g = (float) p;
                        b = cmax;
                        break;
                    case 5:
                        r = cmax;
                        g = (float) p;
                        b = q;
                        break;
                }
            }
            case YUV -> {
                var red = sourceRGB.getRed();
                var green = sourceRGB.getGreen();
                var blue = sourceRGB.getBlue();

                var y = 0.299 * red + 0.587 * green + 0.114 * blue;

                if (Math.random() * 100 <= channel1NoisePercent) {
                    y += channel1MaximumDeviation;
                    y = Math.max(0, Math.min(255, y));
                }

                rgbArray[0] = RGB.grayScale((int) y);

                var u = -0.147 * red - 0.289 * green + 0.436 * blue;

                if (Math.random() * 100 <= channel2NoisePercent) {
                    u += channel2MaximumDeviation;
                    u = Math.max(112, Math.min(-112, u));
                }

                var ur = 127;
                var ug = (int) (127 - 0.395 * u);
                var ub = (int) (127 + 2.032 * u);

                rgbArray[1] = new RGB(ur, ug, ub);

                var v = 0.615 * red - 0.515 * green - 0.1 * blue;
                if (Math.random() * 100 <= channel3NoisePercent) {
                    v += channel3MaximumDeviation;
                    v = Math.max(157, Math.min(-157, v));
                }

                var vr = (int) (127 + 1.14 * v);
                var vg = (int) (127 - 0.581 * v);
                var vb = 127;

                rgbArray[2] = new RGB(vr, vg, vb);

                channelArray[0] = (int) y;
                channelArray[1] = (int) u + 112;
                channelArray[2] = (int) v + 157;

                r = (float) (y + 1.14f * v);
                g = (float) (y - 0.395f * u - 0.581f * v);
                b = (float) (y + 2.032f * u);
            }
        }

        return new PixelData(rgbArray, new RGB((int) r, (int) g, (int) b), channelArray);
    }

    private InputStream getInputStreamFromBufferedImage(BufferedImage bufferedImage) throws IOException {
        var byteBuffer = new ByteArrayOutputStream();
        ImageIO.write(bufferedImage, "jpeg", byteBuffer);
        return new ByteArrayInputStream(byteBuffer.toByteArray());
    }
}
