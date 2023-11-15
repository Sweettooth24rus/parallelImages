package com.kkoz.parallels.lab_1;

import com.kkoz.parallels.ChannelData;
import com.kkoz.parallels.PixelData;
import com.kkoz.parallels.RGB;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class Lab1FiltersPresenter {
    private final Lab1FiltersView view;

    public Lab1FiltersPresenter(Lab1FiltersView view) {
        this.view = view;
    }

    private PixelData splitImage(RGB sourceRGB, Integer lightness, Double contrast) {
        var rgbArray = new RGB[3];
        var channelArray = new Integer[3];

        var red = sourceRGB.getRed();
        var green = sourceRGB.getGreen();
        var blue = sourceRGB.getBlue();

        var y = 0.299 * red + 0.587 * green + 0.114 * blue;

        if (lightness != null) {
            y += lightness;
        }
        if (contrast != null) {
            y = (contrast * (y - 127)) + 127;
        }

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

        channelArray[0] = RGB.checkBorderValues((int) y);
        channelArray[1] = (int) u + 112;
        channelArray[2] = (int) v + 157;

        var r = y + 1.14 * v;
        var g = y - 0.395 * u - 0.581 * v;
        var b = y + 2.032 * u;

        return new PixelData(rgbArray, new RGB(r, g, b), channelArray);
    }

    private InputStream getInputStreamFromBufferedImage(BufferedImage bufferedImage) throws IOException {
        var byteBuffer = new ByteArrayOutputStream();
        ImageIO.write(bufferedImage, "jpeg", byteBuffer);
        return new ByteArrayInputStream(byteBuffer.toByteArray());
    }

    public void applyYUVFilters(InputStream imageStream, String lightnessText, String contrastText) {
        try {
            var lightness = Integer.parseInt(lightnessText);
            var contrast = Double.parseDouble(contrastText);

            var bufferedImage = ImageIO.read(imageStream);

            var width = bufferedImage.getWidth();
            var height = bufferedImage.getHeight();

            var redMatrix = new ArrayList<List<Integer>>(width);
            var greenMatrix = new ArrayList<List<Integer>>(width);
            var blueMatrix = new ArrayList<List<Integer>>(width);

            var channel1 = new int[256];
            var channel2 = new int[225];
            var channel3 = new int[315];

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
                    var pixelData = splitImage(sourceRGB, lightness, contrast);
                    var rgbArray = pixelData.getRgb();
                    var channelArray = pixelData.getChannel();
                    var filteredRGB = pixelData.getNewRGB();

                    bufferedImage.setRGB(x, y, filteredRGB.getRGB());
                    bufferedImageFirst.setRGB(x, y, rgbArray[0].getRGB());
                    bufferedImageSecond.setRGB(x, y, rgbArray[1].getRGB());
                    bufferedImageThird.setRGB(x, y, rgbArray[2].getRGB());

                    channel1[channelArray[0]]++;
                    channel2[channelArray[1]]++;
                    channel3[channelArray[2]]++;
                }
            }

            view.refreshFilterPhotosSection(
                getInputStreamFromBufferedImage(bufferedImage)
            );

            view.refreshFilterChannelsSection(
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
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
