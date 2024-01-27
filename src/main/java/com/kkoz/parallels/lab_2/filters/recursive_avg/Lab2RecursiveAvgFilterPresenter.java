package com.kkoz.parallels.lab_2.filters.recursive_avg;

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
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class Lab2RecursiveAvgFilterPresenter extends Presenter<Lab2RecursiveAvgFilterView> {

    public Lab2RecursiveAvgFilterPresenter(Lab2RecursiveAvgFilterView view) {
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
                               int height,
                               int width,
                               int width0,
                               int width1) {
        for (int xw = width0; xw < width1; xw++) {
            for (var yh = 0; yh < height; yh++) {
//                if (y == 0) {
//                    addRecursiveZero(
//                        redMatrix,
//                        greenMatrix,
//                        blueMatrix,
//                        newRedMatrix,
//                        newGreenMatrix,
//                        newBlueMatrix,
//                        matrixWidthOffset,
//                        matrixHeightOffset,
//                        height,
//                        width,
//                        x,
//                        y
//                    );
//                } else {
//                    addRecursive(
//                        redMatrix,
//                        greenMatrix,
//                        blueMatrix,
//                        newRedMatrix,
//                        newGreenMatrix,
//                        newBlueMatrix,
//                        matrixWidthOffset,
//                        matrixHeightOffset,
//                        height,
//                        width,
//                        x,
//                        y
//                    );
//                }
                var iter = 0;
                for (var x = Math.max(0, xw - matrixWidthOffset); x < Math.min(width, xw + matrixWidthOffset); x++) {
                    for (var y = Math.max(0, yh - matrixHeightOffset); y < Math.min(height, yh + matrixHeightOffset); y++) {
                        newRedMatrix[xw][yh] += redMatrix[x][y];
                        newGreenMatrix[xw][yh] += greenMatrix[x][y];
                        newBlueMatrix[xw][yh] += blueMatrix[x][y];
                        iter++;
                    }
                }
                newRedMatrix[xw][yh] /= iter;
                newGreenMatrix[xw][yh] /= iter;
                newBlueMatrix[xw][yh] /= iter;
            }
        }
        return null;
    }

    private void addRecursiveZero(int[][] redMatrix,
                                  int[][] greenMatrix,
                                  int[][] blueMatrix,
                                  int[][] newRedMatrix,
                                  int[][] newGreenMatrix,
                                  int[][] newBlueMatrix,
                                  int matrixWidthOffset,
                                  int matrixHeightOffset,
                                  int height,
                                  int width,
                                  int xw,
                                  int yh
    ) {
        var iter = 0;
        for (var x = Math.max(0, xw - matrixWidthOffset); x < Math.min(width, xw + matrixWidthOffset); x++) {
            for (var y = Math.max(0, yh - matrixHeightOffset); y < Math.min(height, yh + matrixHeightOffset); y++) {
                newRedMatrix[xw][yh] += redMatrix[x][y];
                newGreenMatrix[xw][yh] += greenMatrix[x][y];
                newBlueMatrix[xw][yh] += blueMatrix[x][y];
                iter++;
            }
        }
        newRedMatrix[xw][yh] /= iter;
        newGreenMatrix[xw][yh] /= iter;
        newBlueMatrix[xw][yh] /= iter;
    }

    private void addRecursive(int[][] redMatrix,
                              int[][] greenMatrix,
                              int[][] blueMatrix,
                              int[][] newRedMatrix,
                              int[][] newGreenMatrix,
                              int[][] newBlueMatrix,
                              int matrixWidthOffset,
                              int matrixHeightOffset,
                              int height,
                              int width,
                              int xw,
                              int yh
    ) {
        var iter = 0;
        var minusRedValue = 0D;
        var minusGreenValue = 0D;
        var minusBlueValue = 0D;
        var plusRedValue = 0D;
        var plusGreenValue = 0D;
        var plusBlueValue = 0D;

        for (var x = Math.max(0, xw - matrixWidthOffset); x < Math.min(width, xw + matrixWidthOffset); x++) {
            var maxY = Math.max(0, yh - matrixHeightOffset - 1);
            minusRedValue += redMatrix[x][maxY];
            minusGreenValue += greenMatrix[x][maxY];
            minusBlueValue += blueMatrix[x][maxY];
            iter++;
        }
        minusRedValue /= (iter * 2);
        minusGreenValue /= (iter * 2);
        minusBlueValue /= (iter * 2);
        iter = 0;
        for (var x = Math.max(0, xw - matrixWidthOffset); x < Math.min(width, xw + matrixWidthOffset); x++) {
            var minY = Math.min(height - 1, yh + matrixHeightOffset);
            plusRedValue += redMatrix[x][minY];
            plusGreenValue += greenMatrix[x][minY];
            plusBlueValue += blueMatrix[x][minY];
            iter++;
        }

        plusRedValue /= iter;
        plusGreenValue /= iter;
        plusBlueValue /= iter;

        newRedMatrix[xw][yh] = (int) (newRedMatrix[xw][yh - 1] - minusRedValue + plusRedValue);
        newGreenMatrix[xw][yh] = (int) (newGreenMatrix[xw][yh - 1] - minusGreenValue + plusGreenValue);
        newBlueMatrix[xw][yh] = (int) (newBlueMatrix[xw][yh - 1] - minusBlueValue + plusBlueValue);
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
