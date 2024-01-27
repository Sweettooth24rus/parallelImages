package com.kkoz.parallels.lab_3.contour;

import com.kkoz.parallels.ContourType;
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
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class Lab3ContourPresenter extends Presenter<Lab3ContourView> {

    public Lab3ContourPresenter(Lab3ContourView view) {
        super(view);
    }

    public void contour(InputStream imageStream,
                        ContourType type,
                        String thresholdValue,
                        String gainValue,
                        List<List<String>> sobelCoefficientsXValues,
                        List<List<String>> sobelCoefficientsYValues,
                        List<List<String>> laplasCoefficientsValues,
                        String threadsCountValue) {
        try {
            var threads = Integer.parseInt(threadsCountValue);
            var threshold = Double.parseDouble(thresholdValue);
            var gain = Double.parseDouble(gainValue);

            var sobelCoefficientsX = new int[3][3];
            var sobelCoefficientsY = new int[3][3];

            for (var i = 0; i < 3; i++) {
                sobelCoefficientsX[i] = new int[3];
                sobelCoefficientsY[i] = new int[3];
                for (var j = 0; j < 3; j++) {
                    sobelCoefficientsX[i][j] = Integer.parseInt(sobelCoefficientsXValues.get(i).get(j));
                    sobelCoefficientsY[i][j] = Integer.parseInt(sobelCoefficientsYValues.get(i).get(j));
                }
            }

            var laplasCoefficients = new int[laplasCoefficientsValues.get(0).size()][laplasCoefficientsValues.size()];

            for (var i = 0; i < laplasCoefficientsValues.get(0).size(); i++) {
                laplasCoefficients[i] = new int[laplasCoefficientsValues.size()];
                for (var j = 0; j < laplasCoefficientsValues.size(); j++) {
                    laplasCoefficients[i][j] = Integer.parseInt(laplasCoefficientsValues.get(i).get(j));
                }
            }

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
                    var rgb = bufferedImage.getRGB(x, y);
                    var color = new Color(rgb);

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

            var startTime = System.currentTimeMillis();

            var executor = Executors.newFixedThreadPool(threads);

            var tasks = new ArrayList<Future<Void>>();

            for (var i = 0; i < threads; i++) {
                var start = (width / threads) * i;
                var end = (width / threads) * (i + 1);
                switch (type) {
                    case ROBERTS -> {
                        tasks.add(
                            executor.submit(
                                () -> computeRoberts(
                                    redMatrix,
                                    greenMatrix,
                                    blueMatrix,
                                    newRedMatrix,
                                    newGreenMatrix,
                                    newBlueMatrix,
                                    threshold,
                                    gain,
                                    height,
                                    width,
                                    start,
                                    end
                                )
                            )
                        );
                    }
                    case SOBEL -> {
                        tasks.add(
                            executor.submit(
                                () -> computeSobel(
                                    redMatrix,
                                    greenMatrix,
                                    blueMatrix,
                                    newRedMatrix,
                                    newGreenMatrix,
                                    newBlueMatrix,
                                    threshold,
                                    gain,
                                    sobelCoefficientsX,
                                    sobelCoefficientsY,
                                    height,
                                    width,
                                    start,
                                    end
                                )
                            )
                        );
                    }
                    case LAPLAS -> {
                        tasks.add(
                            executor.submit(
                                () -> computeLaplas(
                                    redMatrix,
                                    greenMatrix,
                                    blueMatrix,
                                    newRedMatrix,
                                    newGreenMatrix,
                                    newBlueMatrix,
                                    threshold,
                                    gain,
                                    laplasCoefficients,
                                    height,
                                    width,
                                    start,
                                    end
                                )
                            )
                        );
                    }
                }
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

    private Void computeRoberts(int[][] redMatrix,
                                int[][] greenMatrix,
                                int[][] blueMatrix,
                                int[][] newRedMatrix,
                                int[][] newGreenMatrix,
                                int[][] newBlueMatrix,
                                double threshold,
                                double gain,
                                int height,
                                int width,
                                int width0,
                                int width1) {
        for (int x = width0; x < width1; x++) {
            for (var y = 0; y < height; y++) {
                var minX = Math.min(x + 1, width - 1);
                var minY = Math.min(y + 1, height - 1);

                var newValueRedX = redMatrix[x][y] - redMatrix[minX][minY];
                var newValueGreenX = greenMatrix[x][y] - greenMatrix[minX][minY];
                var newValueBlueX = blueMatrix[x][y] - blueMatrix[minX][minY];

                var newValueRedY = redMatrix[minX][y] - redMatrix[x][minY];
                var newValueGreenY = greenMatrix[minX][y] - greenMatrix[x][minY];
                var newValueBlueY = blueMatrix[minX][y] - blueMatrix[x][minY];

                var newValueRed = Math.sqrt(Math.pow(newValueRedX, 2) + Math.pow(newValueRedY, 2));
                var newValueGreen = Math.sqrt(Math.pow(newValueGreenX, 2) + Math.pow(newValueGreenY, 2));
                var newValueBlue = Math.sqrt(Math.pow(newValueBlueX, 2) + Math.pow(newValueBlueY, 2));

                var newValue = (newValueRed + newValueGreen + newValueBlue) / 3;

                newValue *= gain;

                if (newValue < threshold) {
                    newValue = 0;
                }

                newRedMatrix[x][y] = (int) newValue;
                newGreenMatrix[x][y] = (int) newValue;
                newBlueMatrix[x][y] = (int) newValue;
            }
        }
        return null;
    }

    private Void computeSobel(int[][] redMatrix,
                              int[][] greenMatrix,
                              int[][] blueMatrix,
                              int[][] newRedMatrix,
                              int[][] newGreenMatrix,
                              int[][] newBlueMatrix,
                              double threshold,
                              double gain,
                              int[][] sobelCoefficientsX,
                              int[][] sobelCoefficientsY,
                              int height,
                              int width,
                              int width0,
                              int width1) {
        for (int x = width0; x < width1; x++) {
            for (var y = 0; y < height; y++) {
                var newValueRedX = 0;
                var newValueGreenX = 0;
                var newValueBlueX = 0;

                var newValueRedY = 0;
                var newValueGreenY = 0;
                var newValueBlueY = 0;

                for (int i = 0; i < 3; i++) {
                    for (int j = 0; j < 3; j++) {
                        var newX = Math.min(Math.max(x + i - 1, 0), width - 1);
                        var newY = Math.min(Math.max(y + j - 1, 0), height - 1);

                        newValueRedX += (redMatrix[newX][newY] * sobelCoefficientsX[i][j]);
                        newValueGreenX += (greenMatrix[newX][newY] * sobelCoefficientsX[i][j]);
                        newValueBlueX += (blueMatrix[newX][newY] * sobelCoefficientsX[i][j]);

                        newValueRedY += (redMatrix[newX][newY] * sobelCoefficientsY[i][j]);
                        newValueGreenY += (greenMatrix[newX][newY] * sobelCoefficientsY[i][j]);
                        newValueBlueY += (blueMatrix[newX][newY] * sobelCoefficientsY[i][j]);
                    }
                }

                var newValueRed = Math.sqrt(Math.pow(newValueRedX, 2) + Math.pow(newValueRedY, 2));
                var newValueGreen = Math.sqrt(Math.pow(newValueGreenX, 2) + Math.pow(newValueGreenY, 2));
                var newValueBlue = Math.sqrt(Math.pow(newValueBlueX, 2) + Math.pow(newValueBlueY, 2));

                var newValue = (newValueRed + newValueGreen + newValueBlue) / 3;

                newValue *= gain;

                if (newValue < threshold) {
                    newValue = 0;
                }

                newRedMatrix[x][y] = (int) newValue;
                newGreenMatrix[x][y] = (int) newValue;
                newBlueMatrix[x][y] = (int) newValue;
            }
        }
        return null;
    }

    private Void computeLaplas(int[][] redMatrix,
                               int[][] greenMatrix,
                               int[][] blueMatrix,
                               int[][] newRedMatrix,
                               int[][] newGreenMatrix,
                               int[][] newBlueMatrix,
                               double threshold,
                               double gain,
                               int[][] laplasCoefficients,
                               int height,
                               int width,
                               int width0,
                               int width1) {
        var laplasWidth = laplasCoefficients.length;
        var laplasHeight = laplasCoefficients[0].length;

        var laplasWidthOffset = laplasWidth / 2;
        var laplasHeightOffset = laplasHeight / 2;

        for (int x = width0; x < width1; x++) {
            for (var y = 0; y < height; y++) {
                var newValueRed = 0;
                var newValueGreen = 0;
                var newValueBlue = 0;

                for (int i = 0; i < laplasWidth; i++) {
                    for (int j = 0; j < laplasHeight; j++) {
                        var newX = Math.min(Math.max(x + i - laplasWidthOffset, 0), width - 1);
                        var newY = Math.min(Math.max(y + j - laplasHeightOffset, 0), height - 1);

                        newValueRed += (redMatrix[newX][newY] * laplasCoefficients[i][j]);
                        newValueGreen += (greenMatrix[newX][newY] * laplasCoefficients[i][j]);
                        newValueBlue += (blueMatrix[newX][newY] * laplasCoefficients[i][j]);
                    }
                }

                var newValue = (newValueRed + newValueGreen + newValueBlue) / 3;

                newValue *= gain;

                if (newValue < threshold) {
                    newValue = 0;
                }

                newRedMatrix[x][y] = newValue;
                newGreenMatrix[x][y] = newValue;
                newBlueMatrix[x][y] = newValue;
            }
        }
        return null;
    }

    private InputStream getInputStreamFromBufferedImage(BufferedImage bufferedImage) throws IOException {
        var byteBuffer = new ByteArrayOutputStream();
        ImageIO.write(bufferedImage, "jpeg", byteBuffer);
        return new ByteArrayInputStream(byteBuffer.toByteArray());
    }

    public void calculateAverage(InputStream imageStream,
                                 ContourType type,
                                 String thresholdValue,
                                 String gainValue,
                                 List<List<String>> sobelCoefficientsXValues,
                                 List<List<String>> sobelCoefficientsYValues,
                                 List<List<String>> laplasCoefficientsValues) {
        try {
            var threshold = Double.parseDouble(thresholdValue);
            var gain = Double.parseDouble(gainValue);

            var sobelCoefficientsX = new int[3][3];
            var sobelCoefficientsY = new int[3][3];

            for (var i = 0; i < 3; i++) {
                sobelCoefficientsX[i] = new int[3];
                sobelCoefficientsY[i] = new int[3];
                for (var j = 0; j < 3; j++) {
                    sobelCoefficientsX[i][j] = Integer.parseInt(sobelCoefficientsXValues.get(i).get(j));
                    sobelCoefficientsY[i][j] = Integer.parseInt(sobelCoefficientsYValues.get(i).get(j));
                }
            }

            var laplasCoefficients = new int[laplasCoefficientsValues.get(0).size()][laplasCoefficientsValues.size()];

            for (var i = 0; i < laplasCoefficientsValues.get(0).size(); i++) {
                laplasCoefficients[i] = new int[laplasCoefficientsValues.size()];
                for (var j = 0; j < laplasCoefficientsValues.size(); j++) {
                    laplasCoefficients[i][j] = Integer.parseInt(laplasCoefficientsValues.get(i).get(j));
                }
            }

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
                    type,
                    threshold,
                    gain,
                    sobelCoefficientsX,
                    sobelCoefficientsY,
                    laplasCoefficients,
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
                    type,
                    threshold,
                    gain,
                    sobelCoefficientsX,
                    sobelCoefficientsY,
                    laplasCoefficients,
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
                    type,
                    threshold,
                    gain,
                    sobelCoefficientsX,
                    sobelCoefficientsY,
                    laplasCoefficients,
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
                    type,
                    threshold,
                    gain,
                    sobelCoefficientsX,
                    sobelCoefficientsY,
                    laplasCoefficients,
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
                                 ContourType type,
                                 double threshold,
                                 double gain,
                                 int[][] sobelCoefficientsX,
                                 int[][] sobelCoefficientsY,
                                 int[][] laplasCoefficients,
                                 int height,
                                 int width) throws ExecutionException, InterruptedException {
        var startTime = System.currentTimeMillis();

        var executor = Executors.newFixedThreadPool(threads);

        var tasks = new ArrayList<Future<Void>>();

        for (var i = 0; i < threads; i++) {
            var start = (width / threads) * i;
            var end = (width / threads) * (i + 1);
            switch (type) {
                case ROBERTS -> {
                    tasks.add(
                        executor.submit(
                            () -> computeRoberts(
                                redMatrix,
                                greenMatrix,
                                blueMatrix,
                                newRedMatrix,
                                newGreenMatrix,
                                newBlueMatrix,
                                threshold,
                                gain,
                                height,
                                width,
                                start,
                                end
                            )
                        )
                    );
                }
                case SOBEL -> {
                    tasks.add(
                        executor.submit(
                            () -> computeSobel(
                                redMatrix,
                                greenMatrix,
                                blueMatrix,
                                newRedMatrix,
                                newGreenMatrix,
                                newBlueMatrix,
                                threshold,
                                gain,
                                sobelCoefficientsX,
                                sobelCoefficientsY,
                                height,
                                width,
                                start,
                                end
                            )
                        )
                    );
                }
                case LAPLAS -> {
                    tasks.add(
                        executor.submit(
                            () -> computeLaplas(
                                redMatrix,
                                greenMatrix,
                                blueMatrix,
                                newRedMatrix,
                                newGreenMatrix,
                                newBlueMatrix,
                                threshold,
                                gain,
                                laplasCoefficients,
                                height,
                                width,
                                start,
                                end
                            )
                        )
                    );
                }
            }
        }

        for (var task : tasks) {
            task.get();
        }

        executor.shutdown();

        return (System.currentTimeMillis() - startTime) / 1000.0;
    }
}
