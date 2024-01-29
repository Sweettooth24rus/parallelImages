package com.kkoz.parallels.lab_4.points.harris;

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

public class Lab4HarrisPointsPresenter extends Presenter<Lab4HarrisPointsView> {

    public Lab4HarrisPointsPresenter(Lab4HarrisPointsView view) {
        super(view);
    }

    public void harris(InputStream imageStream,
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

            var kernel = kernel1D(7);

            // 1. Calculate partial differences
            var diffx = new float[width][height];
            var diffy = new float[width][height];
            var diffxy = new float[width][height];

            var startTime = System.currentTimeMillis();

            var executor = Executors.newFixedThreadPool(threads);

            var tasks = new ArrayList<Future<Void>>();

            for (var i = 0; i < threads; i++) {
                var start = (width / threads) * i;
                var end = (width / threads) * (i + 1);
                tasks.add(
                    executor.submit(
                        () -> a(oldMatrix, diffx, diffy, diffxy, start, end, width, height)
                    )
                );
            }

            for (var task : tasks) {
                task.get();
            }

            executor.shutdown();

            // 2. Smooth the diff images
            var temp = new float[width][height];

            // Convolve with Gaussian kernel
            convolve(diffx, temp, kernel, threads);
            convolve(diffy, temp, kernel, threads);
            convolve(diffxy, temp, kernel, threads);

            // 3. Compute Harris Corner Response Map
            var map = new float[width][height];

            executor = Executors.newFixedThreadPool(threads);

            tasks = new ArrayList<>();

            for (var i = 0; i < threads; i++) {
                var start = (width / threads) * i;
                var end = (width / threads) * (i + 1);
                tasks.add(
                    executor.submit(
                        () -> b(diffx, diffy, diffxy, map, start, end, width, height)
                    )
                );
            }

            for (var task : tasks) {
                task.get();
            }

            executor.shutdown();

            // 4. Suppress non-maximum points
            var cornersList = new ArrayList<Pair<Integer, Integer>>();

            executor = Executors.newFixedThreadPool(threads);

            tasks = new ArrayList<>();

            for (var i = 0; i < threads; i++) {
                var start = (width / threads) * i;
                var end = (width / threads) * (i + 1);
                tasks.add(
                    executor.submit(
                        () -> c(map, cornersList, start, end, width, height)
                    )
                );
            }

            for (var task : tasks) {
                task.get();
            }

            executor.shutdown();

            var time = (System.currentTimeMillis() - startTime) / 1000.0;

            for (var point : cornersList) {
                for (var x = Math.max(0, point.getA() - 9); x < Math.min(width, point.getA() + 9); x++) {
                    for (var y = Math.max(0, point.getB() - 9); y < Math.min(height, point.getB() + 9); y++) {
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

    private Void a(int[][] oldMatrix,
                   float[][] diffx,
                   float[][] diffy,
                   float[][] diffxy,
                   int width0,
                   int width1,
                   int width,
                   int height) {
        for (var i = width0 + 1; i < width1 - 1; i++) {
            for (var j = 1; j < height - 1; j++) {
                int p1 = oldMatrix[i - 1][j + 1];
                int p2 = oldMatrix[i][j + 1];
                int p3 = oldMatrix[i + 1][j + 1];
                int p4 = oldMatrix[i - 1][j - 1];
                int p5 = oldMatrix[i][j - 1];
                int p6 = oldMatrix[i + 1][j - 1];
                int p7 = oldMatrix[i + 1][j];
                int p8 = oldMatrix[i - 1][j];

                var h = ((p1 + p2 + p3) - (p4 + p5 + p6)) * 0.166666667f;
                var v = ((p6 + p7 + p3) - (p4 + p8 + p1)) * 0.166666667f;

                diffx[i][j] = h * h;
                diffy[i][j] = v * v;
                diffxy[i][j] = h * v;
            }
        }
        return null;
    }

    private Void b(float[][] diffx,
                   float[][] diffy,
                   float[][] diffxy,
                   float[][] map,
                   int width0,
                   int width1,
                   int width,
                   int height) {
        float M, A, B, C;
        for (int i = width0; i < width1; i++) {
            for (int j = 0; j < height; j++) {
                A = diffx[i][j];
                B = diffy[i][j];
                C = diffxy[i][j];
                M = (A * B - C * C) - (0.04f * ((A + B) * (A + B)));

                if (M > 20000f)
                    map[i][j] = M;

            }
        }
        return null;
    }

    private Void c(float[][] map,
                   List<Pair<Integer, Integer>> cornersList,
                   int width0,
                   int width1,
                   int width,
                   int height) {
        for (int x = width0 + 3, maxX = width1 - 3; x < maxX; x++) {
            for (int y = 3, maxY = height - 3; y < maxY; y++) {
                float currentValue = map[x][y];

                // for each windows' row
                for (int i = -3; (currentValue != 0) && (i <= 3); i++) {

                    // for each windows' pixel
                    for (int j = -3; j <= 3; j++) {
                        if (map[x + i][y + j] > currentValue) {
                            currentValue = 0;
                            break;
                        }
                    }
                }

                // check if this point is really interesting
                if (currentValue != 0) {
                    cornersList.add(new Pair<>(x, y));
                }
            }
        }
        return null;
    }

    private void convolve(float[][] image, float[][] temp, float[] kernel, int threads) throws ExecutionException, InterruptedException {
        var width = image[0].length;
        var height = image.length;
        var radius = kernel.length / 2;

        var executor = Executors.newFixedThreadPool(threads);

        var tasks = new ArrayList<Future<Void>>();

        for (var i = 0; i < threads; i++) {
            var start = (height / threads) * i;
            var end = (height / threads) * (i + 1);
            tasks.add(
                executor.submit(
                    () -> {
                        for (var x = start; x < end; x++) {
                            for (var y = radius; y < width - radius; y++) {
                                var v = 0;
                                for (var k = 0; k < kernel.length; k++) {
                                    v += image[x][y + k - radius] * kernel[k];
                                }
                                temp[x][y] = v;
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

        executor = Executors.newFixedThreadPool(threads);

        tasks = new ArrayList<>();

        for (var i = 0; i < threads; i++) {
            var start = (width / threads) * i;
            var end = (width / threads) * (i + 1);
            tasks.add(
                executor.submit(
                    () -> {
                        for (var y = start; y < end; y++) {
                            for (var x = radius; x < height - radius; x++) {
                                var v = 0;
                                for (var k = 0; k < kernel.length; k++) {
                                    v += temp[x + k - radius][y] * kernel[k];
                                }

                                image[x][y] = v;
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
    }

    public float[] kernel1D(int size) {
        var r = size / 2;
        // kernel
        var kernel = new float[size];

        // compute kernel
        for (int x = -r, i = 0; i < size; x++, i++) {
            kernel[i] = function1D(x);
        }

        return kernel;
    }

    public float function1D(double x) {
        return (float) (Math.exp(x * x / (-2 * 1.44)) / (Math.sqrt(2 * Math.PI) * 1.2));
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

            var kernel = kernel1D(7);

            // 1. Calculate partial differences
            var diffx = new float[width][height];
            var diffy = new float[width][height];
            var diffxy = new float[width][height];

            var avgTime1 = 0.;

            for (var i = 0; i < 10; i++) {
                avgTime1 += startParallel(
                    1,
                    oldMatrix,
                    diffx,
                    diffy,
                    diffxy,
                    kernel,
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
                    diffx,
                    diffy,
                    diffxy,
                    kernel,
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
                    diffx,
                    diffy,
                    diffxy,
                    kernel,
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
                    diffx,
                    diffy,
                    diffxy,
                    kernel,
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
                                 float[][] diffx,
                                 float[][] diffy,
                                 float[][] diffxy,
                                 float[] kernel,
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
                    () -> a(oldMatrix, diffx, diffy, diffxy, start, end, width, height)
                )
            );
        }

        for (var task : tasks) {
            task.get();
        }

        executor.shutdown();

        // 2. Smooth the diff images
        var temp = new float[width][height];

        // Convolve with Gaussian kernel
        convolve(diffx, temp, kernel, threads);
        convolve(diffy, temp, kernel, threads);
        convolve(diffxy, temp, kernel, threads);

        // 3. Compute Harris Corner Response Map
        var map = new float[width][height];

        executor = Executors.newFixedThreadPool(threads);

        tasks = new ArrayList<>();

        for (var i = 0; i < threads; i++) {
            var start = (width / threads) * i;
            var end = (width / threads) * (i + 1);
            tasks.add(
                executor.submit(
                    () -> b(diffx, diffy, diffxy, map, start, end, width, height)
                )
            );
        }

        for (var task : tasks) {
            task.get();
        }

        executor.shutdown();

        // 4. Suppress non-maximum points
        var cornersList = new ArrayList<Pair<Integer, Integer>>();

        executor = Executors.newFixedThreadPool(threads);

        tasks = new ArrayList<>();

        for (var i = 0; i < threads; i++) {
            var start = (width / threads) * i;
            var end = (width / threads) * (i + 1);
            tasks.add(
                executor.submit(
                    () -> c(map, cornersList, start, end, width, height)
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
