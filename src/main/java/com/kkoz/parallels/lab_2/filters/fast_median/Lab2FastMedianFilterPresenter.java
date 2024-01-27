package com.kkoz.parallels.lab_2.filters.fast_median;

import com.kkoz.parallels.Presenter;
import com.kkoz.parallels.RGB;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class Lab2FastMedianFilterPresenter extends Presenter<Lab2FastMedianFilterView> {

    public Lab2FastMedianFilterPresenter(Lab2FastMedianFilterView view) {
        super(view);
    }

    public void filter(InputStream imageStream, String threadsCountValue, String matrixWidthValue, String matrixHeightValue) {
        try {
            var threads = Integer.parseInt(threadsCountValue);
            var matrixWidth = Integer.parseInt(matrixWidthValue);
            var matrixHeight = Integer.parseInt(matrixHeightValue);

            var bufferedImage = ImageIO.read(imageStream);

            var width = bufferedImage.getWidth();
            var height = bufferedImage.getHeight();

            var redMatrix = new int[width][height];
            var greenMatrix = new int[width][height];
            var blueMatrix = new int[width][height];
            var newRedMatrix = new int[width][height];
            var newGreenMatrix = new int[width][height];
            var newBlueMatrix = new int[width][height];

            for (var x = 0; x < width; x++) {
                var redHeight = new int[height];
                var greenHeight = new int[height];
                var blueHeight = new int[height];
                for (var y = 0; y < height; y++) {
                    var color = new Color(bufferedImage.getRGB(x, y));

                    redHeight[y] = color.getRed();
                    greenHeight[y] = color.getGreen();
                    blueHeight[y] = color.getBlue();
                }
                redMatrix[x] = redHeight;
                greenMatrix[x] = greenHeight;
                blueMatrix[x] = blueHeight;
                newRedMatrix[x] = new int[height];
                newGreenMatrix[x] = new int[height];
                newBlueMatrix[x] = new int[height];
            }

            var matrixWidthOffset = matrixWidth / 2;
            var matrixHeightOffset = matrixHeight / 2;

            var startTime = System.currentTimeMillis();

            var executor = Executors.newFixedThreadPool(threads);

            var tasks = new ArrayList<Future<Void>>();

            for (var i = 0; i < threads; i++) {
                var start = (width / threads) * i;
                var end = (width / threads) * (i + 1);
                tasks.add(
                    executor.submit(
                        () -> computeValues(
                            redMatrix,
                            greenMatrix,
                            blueMatrix,
                            newRedMatrix,
                            newGreenMatrix,
                            newBlueMatrix,
                            matrixWidthOffset,
                            matrixHeightOffset,
                            matrixWidth,
                            matrixHeight,
                            height,
                            width,
                            start,
                            end
                        )
                    )
                );
            }

            for (var task : tasks) {
                task.get();
            }

            executor.shutdown();

            var time = (System.currentTimeMillis() - startTime) / 1000.0;

            for (var x = 0; x < width; x++) {
                for (var y = 0; y < height; y++) {
                    var color = new Color(
                        RGB.checkBorderValues(newRedMatrix[x][y]),
                        RGB.checkBorderValues(newGreenMatrix[x][y]),
                        RGB.checkBorderValues(newBlueMatrix[x][y])
                    );

                    bufferedImage.setRGB(x, y, color.getRGB());
                }
            }

            view.refreshFilterPhotosSection(getInputStreamFromBufferedImage(bufferedImage));

            view.createResultSection(time, null, null, null, null);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private Void computeValues(int[][] redMatrix,
                               int[][] greenMatrix,
                               int[][] blueMatrix,
                               int[][] newRedMatrix,
                               int[][] newGreenMatrix,
                               int[][] newBlueMatrix,
                               int matrixWidthOffset,
                               int matrixHeightOffset,
                               int matrixWidth,
                               int matrixHeight,
                               int height,
                               int width,
                               int width0,
                               int width1) {
        for (int x = width0; x < width1; x++) {
            for (var y = 0; y < height; y++) {
                var rMatrix = new int[matrixWidth * matrixHeight];
                var gMatrix = new int[matrixWidth * matrixHeight];
                var bMatrix = new int[matrixWidth * matrixHeight];
                for (int i = 0; i < matrixWidth; i++) {
                    for (int j = 0; j < matrixHeight; j++) {
                        var newX = Math.min(Math.max(x + i - matrixWidthOffset, 0), width - 1);
                        var newY = Math.min(Math.max(y + j - matrixHeightOffset, 0), height - 1);
                        rMatrix[i * matrixHeight + j] = redMatrix[newX][newY];
                        gMatrix[i * matrixHeight + j] = greenMatrix[newX][newY];
                        bMatrix[i * matrixHeight + j] = blueMatrix[newX][newY];
                    }
                }
                Arrays.sort(rMatrix);
                Arrays.sort(gMatrix);
                Arrays.sort(bMatrix);
                newRedMatrix[x][y] = rMatrix[matrixWidth * matrixHeight / 2];
                newGreenMatrix[x][y] = gMatrix[matrixWidth * matrixHeight / 2];
                newBlueMatrix[x][y] = bMatrix[matrixWidth * matrixHeight / 2];
            }
        }
        return null;
    }

    private InputStream getInputStreamFromBufferedImage(BufferedImage bufferedImage) throws IOException {
        var byteBuffer = new ByteArrayOutputStream();
        ImageIO.write(bufferedImage, "jpeg", byteBuffer);
        return new ByteArrayInputStream(byteBuffer.toByteArray());
    }

    public void calculateAverage(InputStream imageStream, String matrixWidthValue, String matrixHeightValue) {
        try {
            var matrixWidth = Integer.parseInt(matrixWidthValue);
            var matrixHeight = Integer.parseInt(matrixHeightValue);

            var bufferedImage = ImageIO.read(imageStream);

            var width = bufferedImage.getWidth();
            var height = bufferedImage.getHeight();

            var redMatrix = new int[width][height];
            var greenMatrix = new int[width][height];
            var blueMatrix = new int[width][height];
            var newRedMatrix = new int[width][height];
            var newGreenMatrix = new int[width][height];
            var newBlueMatrix = new int[width][height];

            for (var x = 0; x < width; x++) {
                var redHeight = new int[height];
                var greenHeight = new int[height];
                var blueHeight = new int[height];
                for (var y = 0; y < height; y++) {
                    var color = new Color(bufferedImage.getRGB(x, y));

                    redHeight[y] = color.getRed();
                    greenHeight[y] = color.getGreen();
                    blueHeight[y] = color.getBlue();
                }
                redMatrix[x] = redHeight;
                greenMatrix[x] = greenHeight;
                blueMatrix[x] = blueHeight;
                newRedMatrix[x] = new int[height];
                newGreenMatrix[x] = new int[height];
                newBlueMatrix[x] = new int[height];
            }

            var matrixWidthOffset = matrixWidth / 2;
            var matrixHeightOffset = matrixHeight / 2;

            var avgTime1 = 0.;

            for (var i = 0; i < 10; i++) {
                avgTime1 += startParallel(
                    1,
                    redMatrix,
                    greenMatrix,
                    blueMatrix,
                    newRedMatrix,
                    newGreenMatrix,
                    newBlueMatrix,
                    matrixWidthOffset,
                    matrixHeightOffset,
                    matrixWidth,
                    matrixHeight,
                    height,
                    width
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
                    newRedMatrix,
                    newGreenMatrix,
                    newBlueMatrix,
                    matrixWidthOffset,
                    matrixHeightOffset,
                    matrixWidth,
                    matrixHeight,
                    height,
                    width
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
                    newRedMatrix,
                    newGreenMatrix,
                    newBlueMatrix,
                    matrixWidthOffset,
                    matrixHeightOffset,
                    matrixWidth,
                    matrixHeight,
                    height,
                    width
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
                    newRedMatrix,
                    newGreenMatrix,
                    newBlueMatrix,
                    matrixWidthOffset,
                    matrixHeightOffset,
                    matrixWidth,
                    matrixHeight,
                    height,
                    width
                );
            }

            avgTime4 /= 10;

            view.createResultSection(null, avgTime1, avgTime2, avgTime3, avgTime4);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private double startParallel(Integer threads,
                                 int[][] redMatrix,
                                 int[][] greenMatrix,
                                 int[][] blueMatrix,
                                 int[][] newRedMatrix,
                                 int[][] newGreenMatrix,
                                 int[][] newBlueMatrix,
                                 int matrixWidthOffset,
                                 int matrixHeightOffset,
                                 int matrixWidth,
                                 int matrixHeight,
                                 int height,
                                 int width) throws ExecutionException, InterruptedException {
        var startTime = System.currentTimeMillis();

        var executor = Executors.newFixedThreadPool(threads);

        var tasks = new ArrayList<Future<Void>>();

        for (var i = 0; i < threads; i++) {
            var start = (width / threads) * i;
            var end = (width / threads) * (i + 1);
            tasks.add(
                executor.submit(
                    () -> computeValues(
                        redMatrix,
                        greenMatrix,
                        blueMatrix,
                        newRedMatrix,
                        newGreenMatrix,
                        newBlueMatrix,
                        matrixWidthOffset,
                        matrixHeightOffset,
                        matrixWidth,
                        matrixHeight,
                        height,
                        width,
                        start,
                        end
                    )
                )
            );
        }

        for (var task : tasks) {
            task.get();
        }

        executor.shutdown();

        return (System.currentTimeMillis() - startTime) / 1000.0;
    }
}
