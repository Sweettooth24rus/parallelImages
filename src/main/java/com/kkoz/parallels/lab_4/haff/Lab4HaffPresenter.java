package com.kkoz.parallels.lab_4.haff;

import com.kkoz.parallels.HaffType;
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
import java.util.List;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class Lab4HaffPresenter extends Presenter<Lab4HaffView> {

    public Lab4HaffPresenter(Lab4HaffView view) {
        super(view);
    }

    public void haff(InputStream imageStream,
                     String threadsCountValue,
                     HaffType type) {
        try {
            var threads = Integer.parseInt(threadsCountValue);

            var sobelCoefficientsX = new int[][]{
                {1, 0, -1},
                {2, 0, -2},
                {1, 0, -1}
            };
            var sobelCoefficientsY = new int[][]{
                {2, 1, 0},
                {1, 0, -1},
                {0, -1, -2}
            };

            var bufferedImage = ImageIO.read(imageStream);

            var width = bufferedImage.getWidth();
            var height = bufferedImage.getHeight();

            var oldMatrix = new int[width][height];
            var oldMatrixRed = new int[width][height];
            var oldMatrixGreen = new int[width][height];
            var oldMatrixBlue = new int[width][height];
            var newMatrix = new int[width][height];
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
                newMatrix[x] = new int[height];
                newMatrixRed[x] = newHeightRed;
                newMatrixGreen[x] = newHeightGreen;
                newMatrixBlue[x] = newHeightBlue;
            }

            for (int x = 0; x < width; x++) {
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

                    newMatrix[x][y] = (int) Math.sqrt(Math.pow(newValueX, 2) + Math.pow(newValueY, 2));
                }
            }

            var startTime = System.currentTimeMillis();

            var executor = Executors.newFixedThreadPool(threads);

            var tasks = new ArrayList<Future<Void>>();

            for (var i = 0; i < threads; i++) {
                var start = (width / threads) * i;
                var end = (width / threads) * (i + 1);
                tasks.add(
                    executor.submit(
                        () -> {
                            if (type == HaffType.LINEAR) {
                                Map<Point, Integer> accumulator = houghTransform(newMatrix, start, end, width, height);
                                List<Point> lines = getTopNMax(accumulator, 10);
                                drawDetectedLines(oldMatrixRed, oldMatrixGreen, oldMatrixBlue, lines, width, height);
                            } else {
                                Map<Point, Integer> circles = houghCircle(newMatrix, Math.min(width, height) / 10, Math.min(width, height) / 2, start, end, width, height);
                                List<Point> result = getTopCircles(circles, 10);
                                drawCircles(oldMatrixRed, oldMatrixGreen, oldMatrixBlue, result, width, height);
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

            for (var x = 0; x < width; x++) {
                for (var y = 0; y < height; y++) {
                    var color = new Color(
                        RGB.checkBorderValues(oldMatrixRed[x][y]),
                        RGB.checkBorderValues(oldMatrixGreen[x][y]),
                        RGB.checkBorderValues(oldMatrixBlue[x][y])
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

    public static Map<Point, Integer> houghCircle(int[][] imageMatrix, int minRadius, int maxRadius, int start, int end, int width, int height) {
        Map<Point, Integer> circles = new HashMap<>();
        for (int x = start; x < end; x++) {
            for (int y = 0; y < height; y++) {
                if (imageMatrix[x][y] != 255) {
                    continue;
                }
                for (int r = minRadius; r <= maxRadius; r++) {
                    for (int angle = 0; angle < 360; angle++) {
                        int a = (int) (x - r * Math.cos(Math.toRadians(angle)));
                        int b = (int) (y - r * Math.sin(Math.toRadians(angle)));
                        if (0 <= a && a < width && 0 <= b && b < height) {
                            Point key = new Point(a, b, r);
                            circles.put(key, circles.getOrDefault(key, 1) + 1);
                        }
                    }
                }
            }
        }
        return circles;
    }

    public static void drawCircles(int[][] oldMatrixRed, int[][] oldMatrixGreen, int[][] oldMatrixBlue, List<Point> circles, int width, int height) {
        for (Point circle : circles) {
            int x = circle.x;
            int y = circle.y;
            int r = circle.z;
            for (int angle = 0; angle < 360; angle++) {
                int x1 = (int) (x + r * Math.cos(Math.toRadians(angle)));
                int y1 = (int) (y + r * Math.sin(Math.toRadians(angle)));
                if (0 <= x1 && x1 < width && 0 <= y1 && y1 < height) {
                    oldMatrixRed[x1][y1] = 255;
                    oldMatrixGreen[x1][y1] = 0;
                    oldMatrixBlue[x1][y1] = 255;
                }
            }
        }
    }

    public static List<Point> getTopCircles(Map<Point, Integer> circles, int count) {
        int topValue = circles.values().stream().sorted((a, b) -> b - a).limit(count).min(Integer::compare).orElse(0);
        return circles.entrySet().stream()
            .filter(entry -> entry.getValue() >= topValue)
            .map(Map.Entry::getKey)
            .toList();
    }

    public static List<Point> getTopNMax(Map<Point, Integer> matrix, int n) {
        List<Integer> topValues = new ArrayList<>(matrix.values());
        if (topValues.isEmpty()) {
            return Collections.emptyList();
        }
        Collections.sort(topValues, Collections.reverseOrder());
        int topValue = topValues.size() > n ? topValues.get(Math.max(0, n - 1)) : topValues.get(Math.max(0, topValues.size() - 1));

        return matrix.entrySet().stream()
            .filter(entry -> entry.getValue() >= topValue)
            .map(Map.Entry::getKey)
            .toList();
    }

    public static Map<Point, Integer> houghTransform(int[][] contour, int start, int end, int width, int height) {
        Map<Point, Integer> accumulator = new HashMap<>();
        for (int x = start; x < end; x++) {
            for (int y = 0; y < height; y++) {
                if (contour[x][y] != 255) {
                    continue;
                }
                for (int theta = 0; theta < 180; theta++) {
                    int rho = (int) (x * Math.cos(Math.toRadians(theta)) + y * Math.sin(Math.toRadians(theta)));
                    var key = new Point(rho, theta, 0);
                    accumulator.put(key, accumulator.getOrDefault(key, 1) + 1);
                }
            }
        }
        return accumulator;
    }

    public static void drawDetectedLines(int[][] oldMatrixRed, int[][] oldMatrixGreen, int[][] oldMatrixBlue, List<Point> detectedLines, int width, int height) {
        for (Point line : detectedLines) {
            int rho = line.x;
            int theta = line.y;
            double a = Math.cos(Math.toRadians(theta));
            double b = Math.sin(Math.toRadians(theta));
            if (Math.abs(b) < 1e-9) {
                for (int y = 0; y < height; y++) {
                    int x = (int) ((rho - y * b) / a);
                    if (0 <= x && x < width) {
                        oldMatrixRed[x][y] = 255;
                        oldMatrixGreen[x][y] = 0;
                        oldMatrixBlue[x][y] = 255;
                    }
                }
            } else {
                for (int x = 0; x < width; x++) {
                    int y = (int) ((rho - x * a) / b);
                    if (0 <= y && y < height) {
                        oldMatrixRed[x][y] = 255;
                        oldMatrixGreen[x][y] = 0;
                        oldMatrixBlue[x][y] = 255;
                    }
                }
            }
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

    public static class Point extends java.awt.Point {
        int z;

        public Point(int x, int y, int z) {
            super(x, y);
            this.z = z;
        }
    }
}
