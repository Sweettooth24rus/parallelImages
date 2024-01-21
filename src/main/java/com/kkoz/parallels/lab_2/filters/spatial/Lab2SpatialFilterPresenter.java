package com.kkoz.parallels.lab_2.filters.spatial;

import com.kkoz.parallels.ChannelData;
import com.kkoz.parallels.Presenter;
import com.kkoz.parallels.RGB;
import com.kkoz.parallels.SplitType;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class Lab2SpatialFilterPresenter extends Presenter<Lab2SpatialFilterView> {

    public Lab2SpatialFilterPresenter(Lab2SpatialFilterView view) {
        super(view);
    }

    public void splitImageToChannels(InputStream imageStream,
                                     SplitType type,
                                     String matrixWidthValue,
                                     String matrixHeightValue,
                                     String coefValue,
                                     boolean channel1Enabled,
                                     boolean channel2Enabled,
                                     boolean channel3Enabled) {
        try {
            var matrixWidth = Integer.parseInt(matrixWidthValue);
            var matrixHeight = Integer.parseInt(matrixHeightValue);
            var coef = Double.parseDouble(coefValue);

            var matrixWidthOffset = matrixWidth / 2;
            var matrixHeightOffset = matrixHeight / 2;

            var bufferedImage = ImageIO.read(imageStream);

            var width = bufferedImage.getWidth();
            var height = bufferedImage.getHeight();

            var squareTabRGB = new int[256];
            var squareTabY = new int[256];
            var squareTabU = new int[225];
            var squareTabV = new int[315];

            for (var i = 0; i < 256; i++) {
                squareTabRGB[i] = (int) Math.pow((i - 255), 2);
                squareTabY[i] = (int) Math.pow((i - 255), 2);
            }
            for (var i = 0; i < 225; i++) {
                squareTabU[i] = (int) Math.pow((i - 225), 2);
            }
            for (var i = 0; i < 315; i++) {
                squareTabV[i] = (int) Math.pow((i - 315), 2);
            }

            var redMatrix = new int[width][height];
            var greenMatrix = new int[width][height];
            var blueMatrix = new int[width][height];

            for (var x = 0; x < width; x++) {
                var redHeight = new int[height];
                var greenHeight = new int[height];
                var blueHeight = new int[height];

                for (var y = 0; y < height; y++) {
                    var rgb = bufferedImage.getRGB(x, y);
                    var color = new Color(rgb);

                    redHeight[y] = color.getRed();
                    greenHeight[y] = color.getGreen();
                    blueHeight[y] = color.getBlue();
                }
                redMatrix[x] = redHeight;
                greenMatrix[x] = greenHeight;
                blueMatrix[x] = blueHeight;
            }

            var bufferedImageFirst = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
            var bufferedImageSecond = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
            var bufferedImageThird = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

            for (var xw = 0; xw < width; xw++) {
                for (var yh = 0; yh < height; yh++) {
                    switch (type) {
                        case RGB -> {
                            var rp = 255 - redMatrix[xw][yh];
                            var gp = 255 - greenMatrix[xw][yh];
                            var bp = 255 - blueMatrix[xw][yh];
                            var count = 0D;
                            var newRed = 0D;
                            var newGreen = 0D;
                            var newBlue = 0D;
                            for (var i = Math.max(0, xw - matrixWidthOffset); i <= Math.min(width - 1, xw + matrixWidthOffset); i++) {
                                for (var j = Math.max(0, yh - matrixHeightOffset); j <= Math.min(height - 1, yh + matrixHeightOffset); j++) {
                                    var nRed = redMatrix[i][j];
                                    var nGreen = greenMatrix[i][j];
                                    var nBlue = blueMatrix[i][j];
                                    var squareError = 16 - Math.min((squareTabRGB[Math.min(255, rp + nRed)] + squareTabRGB[Math.min(255, gp + nGreen)] + squareTabRGB[Math.min(255, bp + nBlue)]) / Math.pow(2, coef), 16);
                                    newRed += squareError * nRed;
                                    newGreen += squareError * nGreen;
                                    newBlue += squareError * nBlue;
                                    count += squareError;
                                }
                            }
                            newRed /= count;
                            newGreen /= count;
                            newBlue /= count;

                            if (!channel1Enabled) {
                                newRed = redMatrix[xw][yh];
                            }
                            if (!channel2Enabled) {
                                newGreen = greenMatrix[xw][yh];
                            }
                            if (!channel3Enabled) {
                                newBlue = blueMatrix[xw][yh];
                            }

                            bufferedImage.setRGB(xw, yh, new RGB((int) newRed, (int) newGreen, (int) newBlue).getRGB());
                            bufferedImageFirst.setRGB(xw, yh, RGB.fullRed((int) newRed).getRGB());
                            bufferedImageSecond.setRGB(xw, yh, RGB.fullGreen((int) newGreen).getRGB());
                            bufferedImageThird.setRGB(xw, yh, RGB.fullBlue((int) newBlue).getRGB());
                        }
                        case YUV -> {
                            var red = redMatrix[xw][yh];
                            var green = greenMatrix[xw][yh];
                            var blue = blueMatrix[xw][yh];

                            var y = 0.299 * red + 0.587 * green + 0.114 * blue;
                            var u = -0.147 * red - 0.289 * green + 0.436 * blue;
                            var v = 0.615 * red - 0.515 * green - 0.1 * blue;

                            var yp = 255 - y;
                            var up = 225 - (u + 112);
                            var vp = 315 - (v + 157);
                            var count = 0D;
                            var newY = 0D;
                            var newU = 0D;
                            var newV = 0D;
                            for (var i = Math.max(0, xw - matrixWidthOffset); i <= Math.min(width - 1, xw + matrixWidthOffset); i++) {
                                for (var j = Math.max(0, yh - matrixHeightOffset); j <= Math.min(height - 1, yh + matrixHeightOffset); j++) {
                                    var nRed = redMatrix[i][j];
                                    var nGreen = greenMatrix[i][j];
                                    var nBlue = blueMatrix[i][j];

                                    var nY = 0.299 * nRed + 0.587 * nGreen + 0.114 * nBlue;
                                    var nU = (-0.147 * nRed - 0.289 * nGreen + 0.436 * nBlue) + 112;
                                    var nV = (0.615 * nRed - 0.515 * nGreen - 0.1 * nBlue) + 157;
                                    var squareError = 16 - Math.min((squareTabY[(int) Math.min(255, yp + nY)] + squareTabU[(int) Math.min(224, up + nU)] + squareTabV[(int) Math.min(314, vp + nV)]) / Math.pow(2, coef), 16);
                                    newY += squareError * nY;
                                    newU += squareError * nU;
                                    newV += squareError * nV;
                                    count += squareError;
                                }
                            }
                            newY /= count;
                            newU /= count;
                            newV /= count;

                            newU -= 112;
                            newV -= 157;

                            if (!channel1Enabled) {
                                newY = y;
                            }
                            if (!channel2Enabled) {
                                newU = u;
                            }
                            if (!channel3Enabled) {
                                newV = v;
                            }

                            var ur = 127;
                            var ug = (int) (127 - 0.395 * newU);
                            var ub = (int) (127 + 2.032 * newU);
                            var vr = (int) (127 + 1.14 * newV);
                            var vg = (int) (127 - 0.581 * newV);
                            var vb = 127;

                            var newRed = (float) (newY + 1.14f * newV);
                            var newGreen = (float) (newY - 0.395f * newU - 0.581f * newV);
                            var newBlue = (float) (newY + 2.032f * newU);

                            bufferedImage.setRGB(xw, yh, new RGB((int) newRed, (int) newGreen, (int) newBlue).getRGB());
                            bufferedImageFirst.setRGB(xw, yh, RGB.grayScale((int) y).getRGB());
                            bufferedImageSecond.setRGB(xw, yh, new RGB(ur, ug, ub).getRGB());
                            bufferedImageThird.setRGB(xw, yh, new RGB(vr, vg, vb).getRGB());

                        }
                    }
                }
            }

            view.refreshPhotosSection(getInputStreamFromBufferedImage(bufferedImage));

            view.refreshChannelsSection(
                new ChannelData(
                    getInputStreamFromBufferedImage(bufferedImageFirst),
                    null
                ),
                new ChannelData(
                    getInputStreamFromBufferedImage(bufferedImageSecond),
                    null
                ),
                new ChannelData(
                    getInputStreamFromBufferedImage(bufferedImageThird),
                    null
                )
            );
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
