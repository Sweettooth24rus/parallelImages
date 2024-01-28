package com.kkoz.parallels.lab_3.binarisation;

import com.kkoz.parallels.BinarisationType;
import com.kkoz.parallels.Presenter;
import com.kkoz.parallels.RGB;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class Lab3BinarisationPresenter extends Presenter<Lab3BinarisationView> {

    public Lab3BinarisationPresenter(Lab3BinarisationView view) {
        super(view);
    }

    public void binarise(InputStream imageStream,
                         BinarisationType type,
                         String thresholdValue,
                         String bradleyAreaValue) {
        try {
            var threshold = 0D;
            if (type == null) {
                threshold = Double.parseDouble(thresholdValue);
            }
            var bradleyArea = Integer.parseInt(bradleyAreaValue);

            var bufferedImage = ImageIO.read(imageStream);

            var width = bufferedImage.getWidth();
            var height = bufferedImage.getHeight();

            var oldMatrix = new int[width][height];
            var tmpMatrix = new int[width][height];
            var newMatrix = new int[width][height];

            for (var x = 0; x < width; x++) {
                var oldHeight = new int[height];

                for (var y = 0; y < height; y++) {
                    var rgb = bufferedImage.getRGB(x, y);
                    var color = new Color(rgb);

                    oldHeight[y] = (int) (0.2125 * color.getRed() + 0.7154 * color.getGreen() + 0.0721 * color.getBlue());
                }
                oldMatrix[x] = oldHeight;
                tmpMatrix[x] = new int[height];
                newMatrix[x] = new int[height];
            }

            if (type == null) {
                for (var x = 0; x < width; x++) {
                    for (var y = 0; y < height; y++) {
                        if (oldMatrix[x][y] < threshold) {
                            newMatrix[x][y] = 0;
                        } else {
                            newMatrix[x][y] = 255;
                        }
                    }
                }
            } else if (type == BinarisationType.AVERAGE) {
                var maxAmp = 0;
                var minAmp = 255;
                for (var x = 0; x < width; x++) {
                    for (var y = 0; y < height; y++) {
                        if (oldMatrix[x][y] < minAmp) {
                            minAmp = oldMatrix[x][y];
                        }
                        if (oldMatrix[x][y] > maxAmp) {
                            maxAmp = oldMatrix[x][y];
                        }
                    }
                }
                threshold = (maxAmp - minAmp) / 2.;
                for (var x = 0; x < width; x++) {
                    for (var y = 0; y < height; y++) {
                        if (oldMatrix[x][y] < threshold) {
                            newMatrix[x][y] = 0;
                        } else {
                            newMatrix[x][y] = 255;
                        }
                    }
                }
            } else {
                var S = width / bradleyArea;
                var s2 = S / 2;
                var t = 0.15;
                var sum = 0;
                var count = 0;
                var x1 = 0;
                var y1 = 0;
                var x2 = 0;
                var y2 = 0;

                for (var x = 0; x < width; x++) {
                    sum = 0;
                    for (var y = 0; y < height; y++) {
                        sum += oldMatrix[x][y];
                        if (x == 0)
                            tmpMatrix[x][y] = sum;
                        else
                            tmpMatrix[x][y] = (tmpMatrix[x - 1][y] + sum);
                    }
                }

                for (int x = 0; x < width; x++) {
                    for (int y = 0; y < height; y++) {
                        x1 = x - s2;
                        x2 = x + s2;
                        y1 = y - s2;
                        y2 = y + s2;

                        if (x1 < 0)
                            x1 = 0;
                        if (x2 >= width)
                            x2 = width - 1;
                        if (y1 < 0)
                            y1 = 0;
                        if (y2 >= height)
                            y2 = height - 1;

                        count = (x2 - x1) * (y2 - y1);

                        sum = tmpMatrix[x2][y2] - tmpMatrix[x2][y1] - tmpMatrix[x1][y2] + tmpMatrix[x1][y1];
                        if ((oldMatrix[x][y] * count) < (sum * (1.0 - t)))
                            newMatrix[x][y] = 0;
                        else
                            newMatrix[x][y] = 255;
                    }
                }
            }

            for (var x = 0; x < width; x++) {
                for (var y = 0; y < height; y++) {
                    var color = new Color(
                        RGB.checkBorderValues(newMatrix[x][y]),
                        RGB.checkBorderValues(newMatrix[x][y]),
                        RGB.checkBorderValues(newMatrix[x][y])
                    );

                    bufferedImage.setRGB(x, y, color.getRGB());
                }
            }

            view.refreshFilterPhotosSection(getInputStreamFromBufferedImage(bufferedImage));
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private InputStream getInputStreamFromBufferedImage(BufferedImage bufferedImage) throws IOException {
        var byteBuffer = new ByteArrayOutputStream();
        ImageIO.write(bufferedImage, "jpeg", byteBuffer);
        return new ByteArrayInputStream(byteBuffer.toByteArray());
    }
}
