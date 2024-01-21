package com.kkoz.parallels.lab_2.filters.kuwahara;

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

public class Lab2KuwaharaFilterPresenter extends Presenter<Lab2KuwaharaFilterView> {

    public Lab2KuwaharaFilterPresenter(Lab2KuwaharaFilterView view) {
        super(view);
    }

    public void splitImageToChannels(InputStream imageStream,
                                     SplitType type,
                                     String matrixWidthValue,
                                     String matrixHeightValue,
                                     boolean channel1Enabled,
                                     boolean channel2Enabled,
                                     boolean channel3Enabled) {
        try {
            var matrixWidth = Integer.parseInt(matrixWidthValue);
            var matrixHeight = Integer.parseInt(matrixHeightValue);

            var matrixWidthOffset = matrixWidth / 2;
            var matrixHeightOffset = matrixHeight / 2;

            var bufferedImage = ImageIO.read(imageStream);

            var width = bufferedImage.getWidth();
            var height = bufferedImage.getHeight();

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

            for (var xw = matrixWidthOffset; xw < width - matrixWidthOffset; xw++) {
                for (var yh = matrixHeightOffset; yh < height - matrixHeightOffset; yh++) {
                    switch (type) {
                        case RGB -> {
                            var subregions = new int[4][3];
                            for (int k = 0; k < 4; k++) {
                                subregions[k] = getSubregionMeans(redMatrix, greenMatrix, blueMatrix, xw, yh, matrixWidthOffset, matrixHeightOffset, k);
                            }
                            var variances = new int[4][3];
                            for (int k = 0; k < 4; k++) {
                                variances[k] = getVariance(subregions[k], redMatrix, greenMatrix, blueMatrix, xw, yh, matrixWidthOffset, matrixHeightOffset, k);
                            }

                            int minVarianceIndex = getMinVarianceIndex(variances);
                            var r = subregions[minVarianceIndex][0];
                            var g = subregions[minVarianceIndex][1];
                            var b = subregions[minVarianceIndex][2];

                            if (!channel1Enabled) {
                                r = redMatrix[xw][yh];
                            }
                            if (!channel2Enabled) {
                                g = greenMatrix[xw][yh];
                            }
                            if (!channel3Enabled) {
                                b = blueMatrix[xw][yh];
                            }

                            bufferedImage.setRGB(xw, yh, new RGB(r, g, b).getRGB());
                            bufferedImageFirst.setRGB(xw, yh, RGB.fullRed(r).getRGB());
                            bufferedImageSecond.setRGB(xw, yh, RGB.fullGreen(g).getRGB());
                            bufferedImageThird.setRGB(xw, yh, RGB.fullBlue(b).getRGB());
                        }
                        case YUV -> {
                            var red = redMatrix[xw][yh];
                            var green = greenMatrix[xw][yh];
                            var blue = blueMatrix[xw][yh];

                            var y = 0.299 * red + 0.587 * green + 0.114 * blue;
                            var u = -0.147 * red - 0.289 * green + 0.436 * blue;
                            var v = 0.615 * red - 0.515 * green - 0.1 * blue;

                            var subregions = new int[4][3];
                            for (int k = 0; k < 4; k++) {
                                subregions[k] = getSubregionMeansYUV(redMatrix, greenMatrix, blueMatrix, xw, yh, matrixWidthOffset, matrixHeightOffset, k);
                            }
                            var variances = new int[4][3];
                            for (int k = 0; k < 4; k++) {
                                variances[k] = getVarianceYUV(subregions[k], redMatrix, greenMatrix, blueMatrix, xw, yh, matrixWidthOffset, matrixHeightOffset, k);
                            }

                            int minVarianceIndex = getMinVarianceIndexYUV(variances);
                            var Y = subregions[minVarianceIndex][0];
                            var U = subregions[minVarianceIndex][1];
                            var V = subregions[minVarianceIndex][2];

                            if (!channel1Enabled) {
                                Y = (int) y;
                            }
                            if (!channel2Enabled) {
                                U = (int) u;
                            }
                            if (!channel3Enabled) {
                                V = (int) v;
                            }

                            var ur = 127;
                            var ug = (int) (127 - 0.395 * Y);
                            var ub = (int) (127 + 2.032 * U);
                            var vr = (int) (127 + 1.14 * V);
                            var vg = (int) (127 - 0.581 * V);
                            var vb = 127;

                            var newRed = Y + 1.14f * V;
                            var newGreen = Y - 0.395f * U - 0.581f * V;
                            var newBlue = Y + 2.032f * U;

                            bufferedImage.setRGB(xw, yh, new RGB((int) newRed, (int) newGreen, (int) newBlue).getRGB());
                            bufferedImageFirst.setRGB(xw, yh, RGB.grayScale(Y).getRGB());
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

    public static int[] getSubregionMeans(int[][] redMatrix, int[][] greenMatrix, int[][] blueMatrix, int x, int y, int widthOffset, int heightOffset, int k) {
        int[] means = new int[3];
        int startX = x - widthOffset + (k / 2) * widthOffset;
        int startY = y - heightOffset + (k % 2) * heightOffset;

        for (int i = startX; i < startX + widthOffset; i++) {
            for (int j = startY; j < startY + heightOffset; j++) {
                means[0] += redMatrix[i][j];
                means[1] += greenMatrix[i][j];
                means[2] += blueMatrix[i][j];
            }
        }

        for (int i = 0; i < 3; i++) {
            means[i] /= widthOffset * heightOffset; // Вычисление среднего значения
        }

        return means;
    }

    public static int[] getSubregionMeansYUV(int[][] redMatrix, int[][] greenMatrix, int[][] blueMatrix, int x, int y, int widthOffset, int heightOffset, int k) {
        int[] means = new int[3];
        int startX = x - widthOffset + (k / 2) * widthOffset;
        int startY = y - heightOffset + (k % 2) * heightOffset;

        for (int i = startX; i < startX + widthOffset; i++) {
            for (int j = startY; j < startY + heightOffset; j++) {
                means[0] += 0.299 * redMatrix[i][j] + 0.587 * greenMatrix[i][j] + 0.114 * blueMatrix[i][j];
                means[1] += -0.147 * redMatrix[i][j] - 0.289 * greenMatrix[i][j] + 0.436 * blueMatrix[i][j];
                means[2] += 0.615 * redMatrix[i][j] - 0.515 * greenMatrix[i][j] - 0.1 * blueMatrix[i][j];
            }
        }

        for (int i = 0; i < 3; i++) {
            means[i] /= widthOffset * heightOffset; // Вычисление среднего значения
        }

        return means;
    }

    public static int[] getVariance(int[] subregionMeans, int[][] redMatrix, int[][] greenMatrix, int[][] blueMatrix, int x, int y, int widthOffset, int heightOffset, int k) {
        int[] variance = new int[3];
        int startX = x - widthOffset + (k / 2) * widthOffset;
        int startY = y - heightOffset + (k % 2) * heightOffset;

        for (int i = startX; i < startX + widthOffset; i++) {
            for (int j = startY; j < startY + heightOffset; j++) {
                variance[0] += Math.pow((redMatrix[i][j] - subregionMeans[0]), 2);
                variance[1] += Math.pow((greenMatrix[i][j] - subregionMeans[1]), 2);
                variance[2] += Math.pow((blueMatrix[i][j] - subregionMeans[2]), 2);
            }
        }

        variance[0] /= widthOffset * heightOffset;
        variance[1] /= widthOffset * heightOffset;
        variance[2] /= widthOffset * heightOffset;

        return variance;
    }

    public static int[] getVarianceYUV(int[] subregionMeans, int[][] redMatrix, int[][] greenMatrix, int[][] blueMatrix, int x, int y, int widthOffset, int heightOffset, int k) {
        int[] variance = new int[3];
        int startX = x - widthOffset + (k / 2) * widthOffset;
        int startY = y - heightOffset + (k % 2) * heightOffset;

        for (int i = startX; i < startX + widthOffset; i++) {
            for (int j = startY; j < startY + heightOffset; j++) {
                variance[0] += Math.pow((0.299 * redMatrix[i][j] + 0.587 * greenMatrix[i][j] + 0.114 * blueMatrix[i][j] - subregionMeans[0]), 2);
                variance[1] += Math.pow((-0.147 * redMatrix[i][j] - 0.289 * greenMatrix[i][j] + 0.436 * blueMatrix[i][j] - subregionMeans[1]), 2);
                variance[2] += Math.pow((0.615 * redMatrix[i][j] - 0.515 * greenMatrix[i][j] - 0.1 * blueMatrix[i][j] - subregionMeans[2]), 2);
            }
        }

        variance[0] /= widthOffset * heightOffset;
        variance[1] /= widthOffset * heightOffset;
        variance[2] /= widthOffset * heightOffset;

        return variance;
    }

    public static int getMinVarianceIndex(int[][] variances) {
        int minIndex = 0;
        int minValue = variances[0][0];

        for (int i = 1; i < variances.length; i++) {
            for (var j = 0; j < 3; j++) {
                if (variances[i][j] < minValue) {
                    minValue = variances[i][j];
                    minIndex = i;
                }
            }
        }

        return minIndex;
    }

    public static int getMinVarianceIndexYUV(int[][] variances) {
        int minIndex = 0;
        int minValue = variances[0][0];

        for (int i = 1; i < variances.length; i++) {
            for (var j = 0; j < 3; j++) {
                var v = variances[i][j];
                if (j == 1) {
                    v = (v + 112) / 225 * 255;
                } else if (j == 2) {
                    v = (v + 157) / 315 * 255;
                }
                if (v < minValue) {
                    minValue = v;
                    minIndex = i;
                }
            }
        }

        return minIndex;
    }

    private InputStream getInputStreamFromBufferedImage(BufferedImage bufferedImage) throws IOException {
        var byteBuffer = new ByteArrayOutputStream();
        ImageIO.write(bufferedImage, "jpeg", byteBuffer);
        return new ByteArrayInputStream(byteBuffer.toByteArray());
    }
}
