package com.kkoz.parallels;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class Lab1Presenter {
    private final Lab1View view;

    public Lab1Presenter(Lab1View view) {
        this.view = view;
    }

    public void splitImageToChannels(InputStream imageStream, SplitType type) {
        try {
            var bufferedImage = ImageIO.read(imageStream);

            var width = bufferedImage.getWidth();
            var height = bufferedImage.getHeight();

            var redMatrix = new ArrayList<List<Integer>>(width);
            var greenMatrix = new ArrayList<List<Integer>>(width);
            var blueMatrix = new ArrayList<List<Integer>>(width);

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
                    var arrayRGB = splitImage(sourceRGB, type);

                    bufferedImageFirst.setRGB(x, y, arrayRGB[0].getRGB());
                    bufferedImageSecond.setRGB(x, y, arrayRGB[1].getRGB());
                    bufferedImageThird.setRGB(x, y, arrayRGB[2].getRGB());
                }
            }

            view.refreshChannelsPhotoSection(
                getInputStreamFromBufferedImage(bufferedImageFirst),
                getInputStreamFromBufferedImage(bufferedImageSecond),
                getInputStreamFromBufferedImage(bufferedImageThird)
            );
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    private RGB[] splitImage(RGB sourceRGB, SplitType type) {
        var result = new RGB[3];
        switch (type) {
            case RGB -> {
                result[0] = RGB.fullRed(sourceRGB);
                result[1] = RGB.fullGreen(sourceRGB);
                result[2] = RGB.fullBlue(sourceRGB);
            }
            case HSV -> {
                result[0] = RGB.fullBlue(sourceRGB);
                result[1] = RGB.fullGreen(sourceRGB);
                result[2] = RGB.fullRed(sourceRGB);
            }
        }
        return result;
    }

    private InputStream getInputStreamFromBufferedImage(BufferedImage bufferedImage) throws IOException {
        var byteBuffer = new ByteArrayOutputStream();
        ImageIO.write(bufferedImage, "jpeg", byteBuffer);
        return new ByteArrayInputStream(byteBuffer.toByteArray());
    }
}
