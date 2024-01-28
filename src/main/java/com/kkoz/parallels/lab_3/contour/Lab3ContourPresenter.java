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

            var oldMatrix = new int[width][height];
            var newMatrix = new int[width][height];

            for (var x = 0; x < width; x++) {
                var oldHeight = new int[height];

                for (var y = 0; y < height; y++) {
                    var rgb = bufferedImage.getRGB(x, y);
                    var color = new Color(rgb);

                    oldHeight[y] = (int) (0.2125 * color.getRed() + 0.7154 * color.getGreen() + 0.0721 * color.getBlue());
                }
                oldMatrix[x] = oldHeight;
                newMatrix[x] = new int[height];
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
                                    oldMatrix,
                                    newMatrix,
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
                                    oldMatrix,
                                    newMatrix,
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
                                    oldMatrix,
                                    newMatrix,
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
                        RGB.checkBorderValues(newMatrix[x][y]),
                        RGB.checkBorderValues(newMatrix[x][y]),
                        RGB.checkBorderValues(newMatrix[x][y])
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

    private Void computeRoberts(int[][] oldMatrix,
                                int[][] newMatrix,
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

                var newValueX = oldMatrix[x][y] - oldMatrix[minX][minY];

                var newValueY = oldMatrix[minX][y] - oldMatrix[x][minY];

                var newValue = Math.sqrt(Math.pow(newValueX, 2) + Math.pow(newValueY, 2));

                newValue *= gain;

                if (newValue < threshold) {
                    newValue = 0;
                }

                newMatrix[x][y] = (int) newValue;
            }
        }
        return null;
    }

    private Void computeSobel(int[][] oldMatrix,
                              int[][] newMatrix,
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
                var newValueX = 0;
                var newValueY = 0;

                for (int i = 0; i < 3; i++) {
                    for (int j = 0; j < 3; j++) {
                        var newX = Math.min(Math.max(x + i - 1, 0), width - 1);
                        var newY = Math.min(Math.max(y + j - 1, 0), height - 1);

                        newValueX += (oldMatrix[newX][newY] * sobelCoefficientsX[i][j]);
                        newValueY += (oldMatrix[newX][newY] * sobelCoefficientsY[i][j]);
                    }
                }

                var newValue = Math.sqrt(Math.pow(newValueX, 2) + Math.pow(newValueY, 2));

                newValue *= gain;

                if (newValue < threshold) {
                    newValue = 0;
                }

                newMatrix[x][y] = (int) newValue;
            }
        }
        return null;
    }

    private Void computeLaplas(int[][] oldMatrix,
                               int[][] newMatrix,
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
                var newValue = 0;

                for (int i = 0; i < laplasWidth; i++) {
                    for (int j = 0; j < laplasHeight; j++) {
                        var newX = Math.min(Math.max(x + i - laplasWidthOffset, 0), width - 1);
                        var newY = Math.min(Math.max(y + j - laplasHeightOffset, 0), height - 1);

                        newValue += (oldMatrix[newX][newY] * laplasCoefficients[i][j]);
                    }
                }

                newValue *= gain;

                if (newValue < threshold) {
                    newValue = 0;
                }

                newMatrix[x][y] = newValue;
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

            var oldMatrix = new int[width][height];
            var newMatrix = new int[width][height];

            for (var x = 0; x < width; x++) {
                var oldHeight = new int[height];
                for (var y = 0; y < height; y++) {
                    var color = new Color(bufferedImage.getRGB(x, y));

                    oldHeight[y] = (int) (0.2125 * color.getRed() + 0.7154 * color.getGreen() + 0.0721 * color.getBlue());
                }
                oldMatrix[x] = oldHeight;
                newMatrix[x] = new int[height];
            }

            var avgTime1 = 0.;

            for (var i = 0; i < 10; i++) {
                avgTime1 += startParallel(
                    1,
                    oldMatrix,
                    newMatrix,
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
                    oldMatrix,
                    newMatrix,
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
                    oldMatrix,
                    newMatrix,
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
                    oldMatrix,
                    newMatrix,
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
                                 int[][] oldMatrix,
                                 int[][] newMatrix,
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
                                oldMatrix,
                                newMatrix,
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
                                oldMatrix,
                                newMatrix,
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
                                oldMatrix,
                                newMatrix,
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
