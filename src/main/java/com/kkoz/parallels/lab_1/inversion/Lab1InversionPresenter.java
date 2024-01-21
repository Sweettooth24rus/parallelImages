package com.kkoz.parallels.lab_1.inversion;

import com.kkoz.parallels.ChannelData;
import com.kkoz.parallels.Presenter;
import com.kkoz.parallels.RGB;
import com.kkoz.parallels.SplitType;
import org.apache.commons.lang3.StringUtils;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class Lab1InversionPresenter extends Presenter<Lab1InversionView> {

    public Lab1InversionPresenter(Lab1InversionView view) {
        super(view);
    }

    public void splitImageToChannels(InputStream imageStream,
                                     SplitType type,
                                     boolean channel1Full,
                                     String channel1MinValue,
                                     String channel1MaxValue,
                                     boolean channel2Full,
                                     String channel2MinValue,
                                     String channel2MaxValue,
                                     boolean channel3Full,
                                     String channel3MinValue,
                                     String channel3MaxValue,
                                     String threadsCountValue) {
        var threadsCount = Integer.parseInt(threadsCountValue);

        Integer channel1Min;
        Integer channel1Max;
        Integer channel2Min;
        Integer channel2Max;
        Integer channel3Min;
        Integer channel3Max;

        if (channel1Full) {
            channel1Min = 0;
            channel1Max = type.getMaxValue(1);
        } else {
            channel1Min = StringUtils.isNotBlank(channel1MinValue) ? Integer.parseInt(channel1MinValue) : null;
            channel1Max = StringUtils.isNotBlank(channel1MaxValue) ? Integer.parseInt(channel1MaxValue) : null;
        }
        if (channel2Full) {
            channel2Min = 0;
            channel2Max = type.getMaxValue(2);
        } else {
            channel2Min = StringUtils.isNotBlank(channel2MinValue) ? Integer.parseInt(channel2MinValue) : null;
            channel2Max = StringUtils.isNotBlank(channel2MaxValue) ? Integer.parseInt(channel2MaxValue) : null;
        }
        if (channel3Full) {
            channel3Min = 0;
            channel3Max = type.getMaxValue(3);
        } else {
            channel3Min = StringUtils.isNotBlank(channel3MinValue) ? Integer.parseInt(channel3MinValue) : null;
            channel3Max = StringUtils.isNotBlank(channel3MaxValue) ? Integer.parseInt(channel3MaxValue) : null;
        }

        splitImageToChannels(
            imageStream,
            type,
            channel1Min,
            channel1Max,
            channel2Min,
            channel2Max,
            channel3Min,
            channel3Max,
            threadsCount
        );
    }

    public void splitImageToChannels(InputStream imageStream,
                                     SplitType type,
                                     Integer channel1Min,
                                     Integer channel1Max,
                                     Integer channel2Min,
                                     Integer channel2Max,
                                     Integer channel3Min,
                                     Integer channel3Max,
                                     Integer threads) {
        try {
            var bufferedImage = ImageIO.read(imageStream);

            var width = bufferedImage.getWidth();
            var height = bufferedImage.getHeight();

            var bufferedImageFirst = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
            var bufferedImageSecond = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
            var bufferedImageThird = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

            int[] channel1 = new int[type.getMaxValueCapacity(1)];
            int[] channel2 = new int[type.getMaxValueCapacity(2)];
            int[] channel3 = new int[type.getMaxValueCapacity(3)];

            var redMatrix = new int[width][height];
            var greenMatrix = new int[width][height];
            var blueMatrix = new int[width][height];

            var redResultMatrix = new int[width][height];
            var greenResultMatrix = new int[width][height];
            var blueResultMatrix = new int[width][height];

            var redChannel1Matrix = new int[width][height];
            var greenChannel1Matrix = new int[width][height];
            var blueChannel1Matrix = new int[width][height];

            var redChannel2Matrix = new int[width][height];
            var greenChannel2Matrix = new int[width][height];
            var blueChannel2Matrix = new int[width][height];

            var redChannel3Matrix = new int[width][height];
            var greenChannel3Matrix = new int[width][height];
            var blueChannel3Matrix = new int[width][height];

            for (var x = 0; x < width; x++) {
                var redHeight = new int[height];
                var greenHeight = new int[height];
                var blueHeight = new int[height];

                var redResultHeight = new int[height];
                var greenResultHeight = new int[height];
                var blueResultHeight = new int[height];

                var redChannel1Height = new int[height];
                var greenChannel1Height = new int[height];
                var blueChannel1Height = new int[height];

                var redChannel2Height = new int[height];
                var greenChannel2Height = new int[height];
                var blueChannel2Height = new int[height];

                var redChannel3Height = new int[height];
                var greenChannel3Height = new int[height];
                var blueChannel3Height = new int[height];

                for (var y = 0; y < height; y++) {
                    var color = new Color(bufferedImage.getRGB(x, y));

                    redHeight[y] = color.getRed();
                    greenHeight[y] = color.getGreen();
                    blueHeight[y] = color.getBlue();
                }
                redMatrix[x] = redHeight;
                greenMatrix[x] = greenHeight;
                blueMatrix[x] = blueHeight;

                redResultMatrix[x] = redResultHeight;
                greenResultMatrix[x] = greenResultHeight;
                blueResultMatrix[x] = blueResultHeight;

                redChannel1Matrix[x] = redChannel1Height;
                greenChannel1Matrix[x] = greenChannel1Height;
                blueChannel1Matrix[x] = blueChannel1Height;

                redChannel2Matrix[x] = redChannel2Height;
                greenChannel2Matrix[x] = greenChannel2Height;
                blueChannel2Matrix[x] = blueChannel2Height;

                redChannel3Matrix[x] = redChannel3Height;
                greenChannel3Matrix[x] = greenChannel3Height;
                blueChannel3Matrix[x] = blueChannel3Height;
            }

            var time = startParallel(
                threads,
                redMatrix,
                greenMatrix,
                blueMatrix,
                height,
                width,
                type,
                channel1Min,
                channel1Max,
                channel2Min,
                channel2Max,
                channel3Min,
                channel3Max,
                redResultMatrix,
                greenResultMatrix,
                blueResultMatrix,
                redChannel1Matrix,
                greenChannel1Matrix,
                blueChannel1Matrix,
                redChannel2Matrix,
                greenChannel2Matrix,
                blueChannel2Matrix,
                redChannel3Matrix,
                greenChannel3Matrix,
                blueChannel3Matrix,
                channel1,
                channel2,
                channel3
            );

            for (var x = 0; x < width; x++) {
                for (var y = 0; y < height; y++) {
                    bufferedImage.setRGB(x, y, new RGB(redResultMatrix[x][y], greenResultMatrix[x][y], blueResultMatrix[x][y]).getRGB());
                    bufferedImageFirst.setRGB(x, y, new RGB(redChannel1Matrix[x][y], greenChannel1Matrix[x][y], blueChannel1Matrix[x][y]).getRGB());
                    bufferedImageSecond.setRGB(x, y, new RGB(redChannel2Matrix[x][y], greenChannel2Matrix[x][y], blueChannel2Matrix[x][y]).getRGB());
                    bufferedImageThird.setRGB(x, y, new RGB(redChannel3Matrix[x][y], greenChannel3Matrix[x][y], blueChannel3Matrix[x][y]).getRGB());
                }
            }

            view.refreshSections(
                getInputStreamFromBufferedImage(bufferedImage),
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

            view.createResultSection(time, null, null, null, null);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private double startParallel(Integer threads,
                                 int[][] redMatrix,
                                 int[][] greenMatrix,
                                 int[][] blueMatrix,
                                 int height,
                                 int width,
                                 SplitType type,
                                 Integer channel1Min,
                                 Integer channel1Max,
                                 Integer channel2Min,
                                 Integer channel2Max,
                                 Integer channel3Min,
                                 Integer channel3Max,
                                 int[][] redResultMatrix,
                                 int[][] greenResultMatrix,
                                 int[][] blueResultMatrix,
                                 int[][] redChannel1Matrix,
                                 int[][] greenChannel1Matrix,
                                 int[][] blueChannel1Matrix,
                                 int[][] redChannel2Matrix,
                                 int[][] greenChannel2Matrix,
                                 int[][] blueChannel2Matrix,
                                 int[][] redChannel3Matrix,
                                 int[][] greenChannel3Matrix,
                                 int[][] blueChannel3Matrix,
                                 int[] channel1,
                                 int[] channel2,
                                 int[] channel3) throws ExecutionException, InterruptedException {
        var startTime = System.currentTimeMillis();

        var executor = Executors.newFixedThreadPool(threads);

        var valueTasks = new ArrayList<Future<Void>>();

        for (var i = 0; i < threads; i++) {
            var start = (width / threads) * i;
            var end = (width / threads) * (i + 1);
            valueTasks.add(
                executor.submit(
                    () -> compute(
                        redMatrix,
                        greenMatrix,
                        blueMatrix,
                        height,
                        start,
                        end,
                        type,
                        channel1Min,
                        channel1Max,
                        channel2Min,
                        channel2Max,
                        channel3Min,
                        channel3Max,
                        redResultMatrix,
                        greenResultMatrix,
                        blueResultMatrix,
                        redChannel1Matrix,
                        greenChannel1Matrix,
                        blueChannel1Matrix,
                        redChannel2Matrix,
                        greenChannel2Matrix,
                        blueChannel2Matrix,
                        redChannel3Matrix,
                        greenChannel3Matrix,
                        blueChannel3Matrix,
                        channel1,
                        channel2,
                        channel3
                    )
                )
            );
        }

        for (var task : valueTasks) {
            task.get();
        }

        executor.shutdown();

        return (System.currentTimeMillis() - startTime) / 1000.0;
    }

    private Void compute(int[][] redMatrix,
                         int[][] greenMatrix,
                         int[][] blueMatrix,
                         int height,
                         int start,
                         int end,
                         SplitType type,
                         Integer channel1Min,
                         Integer channel1Max,
                         Integer channel2Min,
                         Integer channel2Max,
                         Integer channel3Min,
                         Integer channel3Max,
                         int[][] redResultMatrix,
                         int[][] greenResultMatrix,
                         int[][] blueResultMatrix,
                         int[][] redChannel1Matrix,
                         int[][] greenChannel1Matrix,
                         int[][] blueChannel1Matrix,
                         int[][] redChannel2Matrix,
                         int[][] greenChannel2Matrix,
                         int[][] blueChannel2Matrix,
                         int[][] redChannel3Matrix,
                         int[][] greenChannel3Matrix,
                         int[][] blueChannel3Matrix,
                         int[] channel1,
                         int[] channel2,
                         int[] channel3) {
        for (int x = start; x < end; x++) {
            for (var xy = 0; xy < height; xy++) {
                var r = 0F;
                var g = 0F;
                var b = 0F;

                switch (type) {
                    case RGB -> {
                        r = type.invert(1, redMatrix[x][xy], channel1Min, channel1Max);
                        g = type.invert(2, greenMatrix[x][xy], channel2Min, channel2Max);
                        b = type.invert(3, blueMatrix[x][xy], channel3Min, channel3Max);

                        redChannel1Matrix[x][xy] = (int) r;
                        greenChannel1Matrix[x][xy] = 0;
                        blueChannel1Matrix[x][xy] = 0;

                        redChannel2Matrix[x][xy] = 0;
                        greenChannel2Matrix[x][xy] = (int) g;
                        blueChannel2Matrix[x][xy] = 0;

                        redChannel3Matrix[x][xy] = 0;
                        greenChannel3Matrix[x][xy] = 0;
                        blueChannel3Matrix[x][xy] = (int) b;

                        channel1[(int) r]++;
                        channel2[(int) g]++;
                        channel3[(int) b]++;
                    }
                    case HSV -> {
                        var red = redMatrix[x][xy];
                        var green = greenMatrix[x][xy];
                        var blue = blueMatrix[x][xy];

                        var cmax = Math.max(Math.max(red, green), blue);
                        var cmin = Math.min(Math.min(red, green), blue);

                        float saturation;
                        float hue;

                        if (cmax == 0) {
                            saturation = 0;
                        } else {
                            saturation = type.invert(2, (float) (cmax - cmin) / cmax * 255, channel2Min, channel2Max);
                        }

                        if (saturation == 0)
                            hue = 0;
                        else {
                            if (red == cmax) {
                                hue = (float) (green - blue) / (cmax - cmin);
                            } else if (green == cmax) {
                                hue = 2 + (float) (blue - red) / (cmax - cmin);
                            } else {
                                hue = 4 + (float) (red - green) / (cmax - cmin);
                            }
                            hue /= 6;
                            if (hue < 0)
                                hue++;
                        }

                        cmax = type.invert(3, cmax, channel3Min, channel3Max);
                        hue = type.invertHue(hue * 360, channel1Min, channel1Max) / 360;

                        var h = (hue - Math.floor(hue)) * 6.0;
                        var f = (int) ((h - Math.floor(h)) * 255 + 0.5);
                        var q = (255 - f);
                        switch ((int) h) {
                            case 0:
                                redChannel1Matrix[x][xy] = 255;
                                greenChannel1Matrix[x][xy] = f;
                                blueChannel1Matrix[x][xy] = 0;
                                break;
                            case 1:
                                redChannel1Matrix[x][xy] = q;
                                greenChannel1Matrix[x][xy] = 255;
                                blueChannel1Matrix[x][xy] = 0;
                                break;
                            case 2:
                                redChannel1Matrix[x][xy] = 0;
                                greenChannel1Matrix[x][xy] = 255;
                                blueChannel1Matrix[x][xy] = f;
                                break;
                            case 3:
                                redChannel1Matrix[x][xy] = 0;
                                greenChannel1Matrix[x][xy] = q;
                                blueChannel1Matrix[x][xy] = 255;
                                break;
                            case 4:
                                redChannel1Matrix[x][xy] = f;
                                greenChannel1Matrix[x][xy] = 0;
                                blueChannel1Matrix[x][xy] = 255;
                                break;
                            case 5:
                                redChannel1Matrix[x][xy] = 255;
                                greenChannel1Matrix[x][xy] = 0;
                                blueChannel1Matrix[x][xy] = q;
                                break;
                        }

                        redChannel2Matrix[x][xy] = (int) saturation;
                        greenChannel2Matrix[x][xy] = (int) saturation;
                        blueChannel2Matrix[x][xy] = (int) saturation;

                        redChannel3Matrix[x][xy] = cmax;
                        greenChannel3Matrix[x][xy] = cmax;
                        blueChannel3Matrix[x][xy] = cmax;

                        channel1[(int) (hue * 360)]++;
                        channel2[(int) saturation]++;
                        channel3[cmax]++;

                        var p = cmax * (255 - saturation) / 255;
                        q = (int) (cmax * (255 - saturation * f / 255) / 255);
                        var t = cmax * (255 - (saturation * (255 - f) / 255)) / 255;
                        switch ((int) h) {
                            case 0:
                                r = cmax;
                                g = t;
                                b = p;
                                break;
                            case 1:
                                r = q;
                                g = cmax;
                                b = p;
                                break;
                            case 2:
                                r = p;
                                g = cmax;
                                b = t;
                                break;
                            case 3:
                                r = p;
                                g = q;
                                b = cmax;
                                break;
                            case 4:
                                r = t;
                                g = p;
                                b = cmax;
                                break;
                            case 5:
                                r = cmax;
                                g = p;
                                b = q;
                                break;
                        }
                    }
                    case YUV -> {
                        var red = redMatrix[x][xy];
                        var green = greenMatrix[x][xy];
                        var blue = blueMatrix[x][xy];

                        var y = type.invert(1, (float) (0.299 * red + 0.587 * green + 0.114 * blue), channel1Min, channel1Max);
                        redChannel1Matrix[x][xy] = (int) y;
                        greenChannel1Matrix[x][xy] = (int) y;
                        blueChannel1Matrix[x][xy] = (int) y;

                        var u = type.invert(2, (float) (-0.147 * red - 0.289 * green + 0.436 * blue) + 112, channel2Min, channel2Max) - 112;
                        var ur = 127;
                        var ug = (int) (127 - 0.395 * u);
                        var ub = (int) (127 + 2.032 * u);
                        redChannel2Matrix[x][xy] = ur;
                        greenChannel2Matrix[x][xy] = ug;
                        blueChannel2Matrix[x][xy] = ub;

                        var v = type.invert(3, (float) (0.615 * red - 0.515 * green - 0.1 * blue) + 157, channel3Min, channel3Max) - 157;
                        var vr = (int) (127 + 1.14 * v);
                        var vg = (int) (127 - 0.581 * v);
                        var vb = 127;
                        redChannel1Matrix[x][xy] = vr;
                        greenChannel1Matrix[x][xy] = vg;
                        blueChannel1Matrix[x][xy] = vb;

                        channel1[(int) y]++;
                        channel2[(int) (u + 112)]++;
                        channel3[(int) v + 157]++;

                        r = y + 1.14f * v;
                        g = y - 0.395f * u - 0.581f * v;
                        b = y + 2.032f * u;
                    }
                }

                redResultMatrix[x][xy] = (int) r;
                greenResultMatrix[x][xy] = (int) g;
                blueResultMatrix[x][xy] = (int) b;
            }
        }

        return null;
    }

    private InputStream getInputStreamFromBufferedImage(BufferedImage bufferedImage) throws IOException {
        var byteBuffer = new ByteArrayOutputStream();
        ImageIO.write(bufferedImage, "jpeg", byteBuffer);
        return new ByteArrayInputStream(byteBuffer.toByteArray());
    }

    public void calculateAverage(InputStream imageStream, SplitType type) {
        try {

            Integer channel1Min = 0;
            Integer channel1Max = type.getMaxValue(1);
            Integer channel2Min = 0;
            Integer channel2Max = type.getMaxValue(2);
            Integer channel3Min = 0;
            Integer channel3Max = type.getMaxValue(3);

            var bufferedImage = ImageIO.read(imageStream);

            var width = bufferedImage.getWidth();
            var height = bufferedImage.getHeight();

            int[] channel1 = new int[type.getMaxValueCapacity(1)];
            int[] channel2 = new int[type.getMaxValueCapacity(2)];
            int[] channel3 = new int[type.getMaxValueCapacity(3)];

            var redMatrix = new int[width][height];
            var greenMatrix = new int[width][height];
            var blueMatrix = new int[width][height];

            var redResultMatrix = new int[width][height];
            var greenResultMatrix = new int[width][height];
            var blueResultMatrix = new int[width][height];

            var redChannel1Matrix = new int[width][height];
            var greenChannel1Matrix = new int[width][height];
            var blueChannel1Matrix = new int[width][height];

            var redChannel2Matrix = new int[width][height];
            var greenChannel2Matrix = new int[width][height];
            var blueChannel2Matrix = new int[width][height];

            var redChannel3Matrix = new int[width][height];
            var greenChannel3Matrix = new int[width][height];
            var blueChannel3Matrix = new int[width][height];

            for (var x = 0; x < width; x++) {
                var redHeight = new int[height];
                var greenHeight = new int[height];
                var blueHeight = new int[height];

                var redResultHeight = new int[height];
                var greenResultHeight = new int[height];
                var blueResultHeight = new int[height];

                var redChannel1Height = new int[height];
                var greenChannel1Height = new int[height];
                var blueChannel1Height = new int[height];

                var redChannel2Height = new int[height];
                var greenChannel2Height = new int[height];
                var blueChannel2Height = new int[height];

                var redChannel3Height = new int[height];
                var greenChannel3Height = new int[height];
                var blueChannel3Height = new int[height];

                for (var y = 0; y < height; y++) {
                    var color = new Color(bufferedImage.getRGB(x, y));

                    redHeight[y] = color.getRed();
                    greenHeight[y] = color.getGreen();
                    blueHeight[y] = color.getBlue();
                }
                redMatrix[x] = redHeight;
                greenMatrix[x] = greenHeight;
                blueMatrix[x] = blueHeight;

                redResultMatrix[x] = redResultHeight;
                greenResultMatrix[x] = greenResultHeight;
                blueResultMatrix[x] = blueResultHeight;

                redChannel1Matrix[x] = redChannel1Height;
                greenChannel1Matrix[x] = greenChannel1Height;
                blueChannel1Matrix[x] = blueChannel1Height;

                redChannel2Matrix[x] = redChannel2Height;
                greenChannel2Matrix[x] = greenChannel2Height;
                blueChannel2Matrix[x] = blueChannel2Height;

                redChannel3Matrix[x] = redChannel3Height;
                greenChannel3Matrix[x] = greenChannel3Height;
                blueChannel3Matrix[x] = blueChannel3Height;
            }

            var avgTime1 = 0.;

            for (var i = 0; i < 10; i++) {
                avgTime1 += startParallel(
                    1,
                    redMatrix,
                    greenMatrix,
                    blueMatrix,
                    height,
                    width,
                    type,
                    channel1Min,
                    channel1Max,
                    channel2Min,
                    channel2Max,
                    channel3Min,
                    channel3Max,
                    redResultMatrix,
                    greenResultMatrix,
                    blueResultMatrix,
                    redChannel1Matrix,
                    greenChannel1Matrix,
                    blueChannel1Matrix,
                    redChannel2Matrix,
                    greenChannel2Matrix,
                    blueChannel2Matrix,
                    redChannel3Matrix,
                    greenChannel3Matrix,
                    blueChannel3Matrix,
                    channel1,
                    channel2,
                    channel3
                );
            }

            avgTime1 /= 10;

            var avgTime2 = 0.;

            for (var i = 0; i < 10; i++) {
                avgTime2 += startParallel(
                    2,
                    redMatrix,
                    greenMatrix,
                    blueMatrix,
                    height,
                    width,
                    type,
                    channel1Min,
                    channel1Max,
                    channel2Min,
                    channel2Max,
                    channel3Min,
                    channel3Max,
                    redResultMatrix,
                    greenResultMatrix,
                    blueResultMatrix,
                    redChannel1Matrix,
                    greenChannel1Matrix,
                    blueChannel1Matrix,
                    redChannel2Matrix,
                    greenChannel2Matrix,
                    blueChannel2Matrix,
                    redChannel3Matrix,
                    greenChannel3Matrix,
                    blueChannel3Matrix,
                    channel1,
                    channel2,
                    channel3
                );
            }

            avgTime2 /= 10;

            var avgTime3 = 0.;

            for (var i = 0; i < 10; i++) {
                avgTime3 += startParallel(
                    3,
                    redMatrix,
                    greenMatrix,
                    blueMatrix,
                    height,
                    width,
                    type,
                    channel1Min,
                    channel1Max,
                    channel2Min,
                    channel2Max,
                    channel3Min,
                    channel3Max,
                    redResultMatrix,
                    greenResultMatrix,
                    blueResultMatrix,
                    redChannel1Matrix,
                    greenChannel1Matrix,
                    blueChannel1Matrix,
                    redChannel2Matrix,
                    greenChannel2Matrix,
                    blueChannel2Matrix,
                    redChannel3Matrix,
                    greenChannel3Matrix,
                    blueChannel3Matrix,
                    channel1,
                    channel2,
                    channel3
                );
            }

            avgTime3 /= 10;

            var avgTime4 = 0.;

            for (var i = 0; i < 10; i++) {
                avgTime4 += startParallel(
                    4,
                    redMatrix,
                    greenMatrix,
                    blueMatrix,
                    height,
                    width,
                    type,
                    channel1Min,
                    channel1Max,
                    channel2Min,
                    channel2Max,
                    channel3Min,
                    channel3Max,
                    redResultMatrix,
                    greenResultMatrix,
                    blueResultMatrix,
                    redChannel1Matrix,
                    greenChannel1Matrix,
                    blueChannel1Matrix,
                    redChannel2Matrix,
                    greenChannel2Matrix,
                    blueChannel2Matrix,
                    redChannel3Matrix,
                    greenChannel3Matrix,
                    blueChannel3Matrix,
                    channel1,
                    channel2,
                    channel3
                );
            }

            avgTime4 /= 10;

            view.createResultSection(null, avgTime1, avgTime2, avgTime3, avgTime4);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
