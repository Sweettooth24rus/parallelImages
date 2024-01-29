package com.kkoz.parallels.lab_4.lavs;

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

public class Lab4LavsPresenter extends Presenter<Lab4LavsView> {

    public Lab4LavsPresenter(Lab4LavsView view) {
        super(view);
    }

    public void lavs(InputStream imageStream,
                     String threadsCountValue) {
        try {
            var threads = Integer.parseInt(threadsCountValue);

            var LESWR = new int[][]{
                {1, 4, 6, 4, 1},
                {-1, -2, 0, 2, 1},
                {-1, 0, 2, 0, -1},
                {-1, 2, 0, -2, 1},
                {1, -4, 6, -4, 1}
            };

            var lavsMatrix = new int[25][5][5];

            var index = 0;
            for (var i = 0; i < 5; i++) {
                for (var j = 0; j < 5; j++) {
                    var lavsValue = new int[5][5];
                    for (var x = 0; x < 5; x++) {
                        var lavsRow = new int[5];
                        for (var y = 0; y < 5; y++) {
                            lavsRow[y] = LESWR[i][x] * LESWR[j][y];
                        }
                        lavsValue[x] = lavsRow;
                    }
                    lavsMatrix[index++] = lavsValue;
                }
            }

            var bufferedImage = ImageIO.read(imageStream);

            var width = bufferedImage.getWidth();
            var height = bufferedImage.getHeight();

            var oldMatrix = new int[width][height];
            var newMatrix = new int[width][height];
            var newMatrixses = new int[25][width][height];

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

            for (var k = 0; k < 25; k++) {
                var matrixNew = new int[width][height];
                for (var x = 0; x < width; x++) {
                    matrixNew[x] = new int[height];
                }
                newMatrixses[k] = matrixNew;
            }

            var startTime = System.currentTimeMillis();

            var executor = Executors.newFixedThreadPool(threads);

            var tasks = new ArrayList<Future<Void>>();

            for (var i = 0; i < threads; i++) {
                var start = (width / threads) * i;
                var end = (width / threads) * (i + 1);
                tasks.add(
                    executor.submit(
                        () -> a(oldMatrix, newMatrix, start, end, width, height)
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
                        () -> b(newMatrix, newMatrixses, lavsMatrix, start, end, width, height)
                    )
                );
            }

            for (var task : tasks) {
                task.get();
            }

            executor.shutdown();

            var time = (System.currentTimeMillis() - startTime) / 1000.0;

            var bufferedImageMatrix = new BufferedImage[25];

            for (var k = 0; k < 25; k++) {
                bufferedImageMatrix[k] = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
                for (var x = 0; x < width; x++) {
                    for (var y = 0; y < height; y++) {
                        var color = new Color(
                            RGB.checkBorderValues(newMatrixses[k][x][y]),
                            RGB.checkBorderValues(newMatrixses[k][x][y]),
                            RGB.checkBorderValues(newMatrixses[k][x][y])
                        );

                        bufferedImageMatrix[k].setRGB(x, y, color.getRGB());
                    }
                }
            }

            view.refreshFilterPhotosSection(getInputStreamFromBufferedImage(bufferedImage));

            view.createResultSection(time, null, null, null, null);

            List<InputStream> list = new ArrayList<>();
            for (BufferedImage image : bufferedImageMatrix) {
                InputStream inputStreamFromBufferedImage = getInputStreamFromBufferedImage(image);
                list.add(inputStreamFromBufferedImage);
            }
            view.refreshMapsSection(list);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private Void a(int[][] oldMatrix,
                   int[][] newMatrix,
                   int width0,
                   int width1,
                   int width,
                   int height) {
        for (var x = width0; x < width1; x++) {
            for (var y = 0; y < height; y++) {
                var sum = 0D;
                var count = 0;
                for (var x0 = Math.max(0, x - 7); x0 <= Math.min(x + 7, width - 1); x0++) {
                    for (var y0 = Math.max(0, y - 7); y0 <= Math.min(y + 7, height - 1); y0++) {
                        sum += oldMatrix[x0][y0];
                        count++;
                    }
                }
                newMatrix[x][y] = oldMatrix[x][y] - (int) (sum / count);
            }
        }
        return null;
    }

    private Void b(int[][] newMatrix,
                   int[][][] newMatrixses,
                   int[][][] lavsMatrix,
                   int width0,
                   int width1,
                   int width,
                   int height) {
        for (int x = width0; x < width1; x++) {
            for (var y = 0; y < height; y++) {

                for (var k = 0; k < 25; k++) {
                    var newValue = 0;
                    for (int i = 0; i < 5; i++) {
                        for (int j = 0; j < 5; j++) {
                            var newX = x + i - 2;
                            var newY = y + j - 2;

                            if (newX < 0 || newX >= width || newY < 0 || newY >= height) {
                                continue;
                            }

                            newValue += (newMatrix[newX][newY] * lavsMatrix[k][i][j]);
                        }
                    }
                    newMatrixses[k][x][y] = newValue;
                }
            }
        }
        return null;
    }

    private Void c(int[][] newMatrix,
                   int[][][] newMatrixses,
                   int[][][] mapsMatrixses,
                   int[] Ks,
                   int[] K1s,
                   int width0,
                   int width1,
                   int height) {
        for (var x = width0; x < width1; x++) {
            for (var y = 0; y < height; y++) {
                for (var m = 0; m < 10; m++) {
                    var k = Ks[m];
                    var k1 = K1s[m];
                    mapsMatrixses[m][x][y] = (newMatrixses[k][x][y] + newMatrixses[k1][x][y]) / 2;
                    newMatrix[x][y] += mapsMatrixses[m][x][y];
                }
            }
        }
        return null;
    }

    private InputStream getInputStreamFromBufferedImage(BufferedImage bufferedImage) throws IOException {
        var byteBuffer = new ByteArrayOutputStream();
        ImageIO.write(bufferedImage, "jpeg", byteBuffer);
        return new ByteArrayInputStream(byteBuffer.toByteArray());
    }

    public void calculateAverage(InputStream imageStream) {
        try {
            var LESWR = new int[][]{
                {1, 4, 6, 4, 1},
                {-1, -2, 0, 2, 1},
                {-1, 0, 2, 0, -1},
                {-1, 2, 0, -2, 1},
                {1, -4, 6, -4, 1}
            };

            var lavsMatrix = new int[20][5][5];

            var index = 0;
            for (var i = 0; i < 5; i++) {
                for (var j = 0; j < 5; j++) {
                    if (i == j) {
                        continue;
                    }
                    var lavsValue = new int[5][5];
                    for (var x = 0; x < 5; x++) {
                        var lavsRow = new int[5];
                        for (var y = 0; y < 5; y++) {
                            lavsRow[y] = LESWR[i][x] * LESWR[j][y];
                        }
                        lavsValue[x] = lavsRow;
                    }
                    lavsMatrix[index++] = lavsValue;
                }
            }

            var bufferedImage = ImageIO.read(imageStream);

            var width = bufferedImage.getWidth();
            var height = bufferedImage.getHeight();

            var oldMatrix = new int[width][height];
            var newMatrix = new int[width][height];
            var newMatrixses = new int[20][width][height];
            var mapsMatrixses = new int[10][width][height];

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

            for (var k = 0; k < 20; k++) {
                var matrixNew = new int[width][height];
                for (var x = 0; x < width; x++) {
                    matrixNew[x] = new int[height];
                }
                newMatrixses[k] = matrixNew;
                if (k % 2 == 0) {
                    mapsMatrixses[k / 2] = new int[width][height];
                }
            }

            var avgTime1 = 0.;

            for (var i = 0; i < 10; i++) {
                avgTime1 += startParallel(
                    1,
                    oldMatrix,
                    newMatrix,
                    newMatrixses,
                    lavsMatrix,
                    mapsMatrixses,
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
                    newMatrixses,
                    lavsMatrix,
                    mapsMatrixses,
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
                    newMatrixses,
                    lavsMatrix,
                    mapsMatrixses,
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
                    newMatrixses,
                    lavsMatrix,
                    mapsMatrixses,
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
                                 int[][][] newMatrixses,
                                 int[][][] lavsMatrix,
                                 int[][][] mapsMatrixses,
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
                    () -> a(oldMatrix, newMatrix, start, end, width, height)
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
                    () -> b(newMatrix, newMatrixses, lavsMatrix, start, end, width, height)
                )
            );
        }

        for (var task : tasks) {
            task.get();
        }

        executor.shutdown();

        var Ks = new int[]{0, 1, 2, 3, 5, 6, 7, 10, 11, 15};
        var K1s = new int[]{4, 7, 10, 13, 4, 7, 10, 4, 7, 4};

        for (var x = 0; x < width; x++) {
            newMatrix[x] = new int[height];
        }

        executor = Executors.newFixedThreadPool(threads);

        tasks = new ArrayList<>();

        for (var i = 0; i < threads; i++) {
            var start = (width / threads) * i;
            var end = (width / threads) * (i + 1);
            tasks.add(
                executor.submit(
                    () -> c(newMatrix, newMatrixses, mapsMatrixses, Ks, K1s, start, end, height)
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
