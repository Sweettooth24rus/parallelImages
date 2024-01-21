package com.kkoz.parallels.lab_1.channels;

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

public class Lab1ChannelsPresenter extends Presenter<Lab1ChannelsView> {

    public Lab1ChannelsPresenter(Lab1ChannelsView view) {
        super(view);
    }

    public void splitImageToChannels(InputStream imageStream, SplitType type) {
        try {
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
                    var pixelData = splitImage(sourceRGB, type);
                    var rgbArray = pixelData.getRgb();
                    var channelArray = pixelData.getChannel();

                    bufferedImageFirst.setRGB(x, y, rgbArray[0].getRGB());
                    bufferedImageSecond.setRGB(x, y, rgbArray[1].getRGB());
                    bufferedImageThird.setRGB(x, y, rgbArray[2].getRGB());

                    channel1[channelArray[0]]++;
                    channel2[channelArray[1]]++;
                    channel3[channelArray[2]]++;
                }
            }

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
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    private PixelData splitImage(RGB sourceRGB, SplitType type) {
        var rgbArray = new RGB[3];
        var channelArray = new Integer[3];
        switch (type) {
            case RGB -> {
                rgbArray[0] = RGB.fullRed(sourceRGB);
                rgbArray[1] = RGB.fullGreen(sourceRGB);
                rgbArray[2] = RGB.fullBlue(sourceRGB);

                channelArray[0] = sourceRGB.getRed();
                channelArray[1] = sourceRGB.getGreen();
                channelArray[2] = sourceRGB.getBlue();
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
            }
            case YUV -> {
                var red = sourceRGB.getRed();
                var green = sourceRGB.getGreen();
                var blue = sourceRGB.getBlue();

                var y = 0.299 * red + 0.587 * green + 0.114 * blue;
                rgbArray[0] = RGB.grayScale((int) y);

                var u = -0.147 * red - 0.289 * green + 0.436 * blue;
                var ur = 127;
                var ug = (int) (127 - 0.395 * u);
                var ub = (int) (127 + 2.032 * u);
                rgbArray[1] = new RGB(ur, ug, ub);

                var v = 0.615 * red - 0.515 * green - 0.1 * blue;
                var vr = (int) (127 + 1.14 * v);
                var vg = (int) (127 - 0.581 * v);
                var vb = 127;
                rgbArray[2] = new RGB(vr, vg, vb);

                channelArray[0] = (int) y;
                channelArray[1] = (int) u + 112;
                channelArray[2] = (int) v + 157;
            }
        }

        return new PixelData(rgbArray, channelArray);
    }

    private InputStream getInputStreamFromBufferedImage(BufferedImage bufferedImage) throws IOException {
        var byteBuffer = new ByteArrayOutputStream();
        ImageIO.write(bufferedImage, "jpeg", byteBuffer);
        return new ByteArrayInputStream(byteBuffer.toByteArray());
    }
}
