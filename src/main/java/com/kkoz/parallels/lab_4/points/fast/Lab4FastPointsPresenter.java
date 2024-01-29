package com.kkoz.parallels.lab_4.points.fast;

import com.kkoz.parallels.Presenter;
import com.kkoz.parallels.RGB;
import oshi.util.tuples.Pair;

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

public class Lab4FastPointsPresenter extends Presenter<Lab4FastPointsView> {

    public Lab4FastPointsPresenter(Lab4FastPointsView view) {
        super(view);
    }

    public void fast(InputStream imageStream,
                     String threadsCountValue) {
        try {
            var threads = Integer.parseInt(threadsCountValue);

            var bufferedImage = ImageIO.read(imageStream);

            var width = bufferedImage.getWidth();
            var height = bufferedImage.getHeight();

            var oldMatrix = new int[width][height];
            var oldMatrixRed = new int[width][height];
            var oldMatrixGreen = new int[width][height];
            var oldMatrixBlue = new int[width][height];
            var newMatrixRed = new int[width][height];
            var newMatrixGreen = new int[width][height];
            var newMatrixBlue = new int[width][height];

            for (var x = 0; x < width; x++) {
                var oldHeight = new int[height];
                var oldHeightRed = new int[height];
                var oldHeightGreen = new int[height];
                var oldHeightBlue = new int[height];
                var newHeightRed = new int[height];
                var newHeightGreen = new int[height];
                var newHeightBlue = new int[height];

                for (var y = 0; y < height; y++) {
                    var rgb = bufferedImage.getRGB(x, y);
                    var color = new Color(rgb);

                    oldHeight[y] = (int) (0.2125 * color.getRed() + 0.7154 * color.getGreen() + 0.0721 * color.getBlue());
                    oldHeightRed[y] = color.getRed();
                    oldHeightGreen[y] = color.getGreen();
                    oldHeightBlue[y] = color.getBlue();
                    newHeightRed[y] = color.getRed();
                    newHeightGreen[y] = color.getGreen();
                    newHeightBlue[y] = color.getBlue();
                }
                oldMatrix[x] = oldHeight;
                oldMatrixRed[x] = oldHeightRed;
                oldMatrixGreen[x] = oldHeightGreen;
                oldMatrixBlue[x] = oldHeightBlue;
                newMatrixRed[x] = newHeightRed;
                newMatrixGreen[x] = newHeightGreen;
                newMatrixBlue[x] = newHeightBlue;
            }

            var cornersList = new ArrayList<Pair<Integer, Integer>>();

            var startTime = System.currentTimeMillis();

            var executor = Executors.newFixedThreadPool(threads);

            var tasks = new ArrayList<Future<Void>>();

            for (var i = 0; i < threads; i++) {
                var start = (width / threads) * i + 3;
                var end = (width / threads) * (i + 1) - 3;
                tasks.add(
                    executor.submit(
                        () -> {
                            for (var x = start; x < end; x++) {
                                for (var y = 3; y < height - 3; y++) {
                                    var threshold_min = oldMatrix[x][y] - 30;
                                    var threshold_max = oldMatrix[x][y] + 30;

                                    var circle_pixels = new int[][]{
                                        {x, y - 3}, {x + 1, y - 3}, {x + 2, y - 2}, {x + 3, y - 1},
                                        {x + 3, y}, {x + 3, y + 1}, {x + 2, y + 2}, {x + 1, y + 3},
                                        {x, y + 3}, {x - 1, y + 3}, {x - 2, y + 2}, {x - 3, y + 1},
                                        {x - 3, y}, {x - 3, y - 1}, {x - 2, y - 2}, {x - 1, y - 3}
                                    };

                                    var consecutive_brighter = 0;
                                    var consecutive_darker = 0;

                                    for (var j = 0; j < circle_pixels.length; j++) {
                                        var value = oldMatrix[circle_pixels[j][0]][circle_pixels[j][1]];
                                        if (value > threshold_max) {
                                            consecutive_brighter++;
                                        } else if (value < threshold_min) {
                                            consecutive_darker++;
                                        }
                                    }

                                    if (consecutive_brighter >= 9 || consecutive_darker >= 9) {
                                        cornersList.add(new Pair<>(x, y));
                                    }
                                }
                            }
                            return null;
                        }
                    )
                );
            }

            for (var task : tasks) {
                task.get();
            }

            executor.shutdown();

            var time = (System.currentTimeMillis() - startTime) / 1000.0;

            for (var point : cornersList) {
                for (var x = Math.max(0, point.getA() - 3); x < Math.min(width, point.getA() + 3); x++) {
                    for (var y = Math.max(0, point.getB() - 3); y < Math.min(height, point.getB() + 3); y++) {
                        newMatrixRed[x][y] = 255 - oldMatrixRed[x][y];
                        newMatrixGreen[x][y] = 255 - oldMatrixGreen[x][y];
                        newMatrixBlue[x][y] = 255 - oldMatrixBlue[x][y];
                    }
                }
            }

            for (var x = 0; x < width; x++) {
                for (var y = 0; y < height; y++) {
                    var color = new Color(
                        RGB.checkBorderValues(newMatrixRed[x][y]),
                        RGB.checkBorderValues(newMatrixGreen[x][y]),
                        RGB.checkBorderValues(newMatrixBlue[x][y])
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

    private InputStream getInputStreamFromBufferedImage(BufferedImage bufferedImage) throws IOException {
        var byteBuffer = new ByteArrayOutputStream();
        ImageIO.write(bufferedImage, "jpeg", byteBuffer);
        return new ByteArrayInputStream(byteBuffer.toByteArray());
    }

    public void calculateAverage(InputStream imageStream) {
        try {
            var bufferedImage = ImageIO.read(imageStream);

            var width = bufferedImage.getWidth();
            var height = bufferedImage.getHeight();

            var oldMatrix = new int[width][height];
            var oldMatrixRed = new int[width][height];
            var oldMatrixGreen = new int[width][height];
            var oldMatrixBlue = new int[width][height];
            var newMatrixRed = new int[width][height];
            var newMatrixGreen = new int[width][height];
            var newMatrixBlue = new int[width][height];

            for (var x = 0; x < width; x++) {
                var oldHeight = new int[height];
                var oldHeightRed = new int[height];
                var oldHeightGreen = new int[height];
                var oldHeightBlue = new int[height];
                var newHeightRed = new int[height];
                var newHeightGreen = new int[height];
                var newHeightBlue = new int[height];

                for (var y = 0; y < height; y++) {
                    var rgb = bufferedImage.getRGB(x, y);
                    var color = new Color(rgb);

                    oldHeight[y] = (int) (0.2125 * color.getRed() + 0.7154 * color.getGreen() + 0.0721 * color.getBlue());
                    oldHeightRed[y] = color.getRed();
                    oldHeightGreen[y] = color.getGreen();
                    oldHeightBlue[y] = color.getBlue();
                    newHeightRed[y] = color.getRed();
                    newHeightGreen[y] = color.getGreen();
                    newHeightBlue[y] = color.getBlue();
                }
                oldMatrix[x] = oldHeight;
                oldMatrixRed[x] = oldHeightRed;
                oldMatrixGreen[x] = oldHeightGreen;
                oldMatrixBlue[x] = oldHeightBlue;
                newMatrixRed[x] = newHeightRed;
                newMatrixGreen[x] = newHeightGreen;
                newMatrixBlue[x] = newHeightBlue;
            }

            var avgTime1 = 0.;

            for (var i = 0; i < 10; i++) {
                avgTime1 += startParallel(
                    1,
                    oldMatrix,
                    new ArrayList<>(),
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
                    new ArrayList<>(),
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
                    new ArrayList<>(),
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
                    new ArrayList<>(),
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
                                 List<Pair<Integer, Integer>> cornersList,
                                 int height,
                                 int width) throws ExecutionException, InterruptedException {
        var startTime = System.currentTimeMillis();

        var executor = Executors.newFixedThreadPool(threads);

        var tasks = new ArrayList<Future<Void>>();

        for (var i = 0; i < threads; i++) {
            var start = (width / threads) * i + 3;
            var end = (width / threads) * (i + 1) - 3;
            tasks.add(
                executor.submit(
                    () -> {
                        for (var x = start; x < end; x++) {
                            for (var y = 3; y < height - 3; y++) {
                                var threshold_min = oldMatrix[x][y] - 30;
                                var threshold_max = oldMatrix[x][y] + 30;

                                var circle_pixels = new int[][]{
                                    {x, y - 3}, {x + 1, y - 3}, {x + 2, y - 2}, {x + 3, y - 1},
                                    {x + 3, y}, {x + 3, y + 1}, {x + 2, y + 2}, {x + 1, y + 3},
                                    {x, y + 3}, {x - 1, y + 3}, {x - 2, y + 2}, {x - 3, y + 1},
                                    {x - 3, y}, {x - 3, y - 1}, {x - 2, y - 2}, {x - 1, y - 3}
                                };

                                var consecutive_brighter = 0;
                                var consecutive_darker = 0;

                                for (var j = 0; j < circle_pixels.length; j++) {
                                    var value = oldMatrix[circle_pixels[j][0]][circle_pixels[j][1]];
                                    if (value > threshold_max) {
                                        consecutive_brighter++;
                                    } else if (value < threshold_min) {
                                        consecutive_darker++;
                                    }
                                }

                                if (consecutive_brighter >= 9 || consecutive_darker >= 9) {
                                    cornersList.add(new Pair<>(x, y));
                                }
                            }
                        }
                        return null;
                    }
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
