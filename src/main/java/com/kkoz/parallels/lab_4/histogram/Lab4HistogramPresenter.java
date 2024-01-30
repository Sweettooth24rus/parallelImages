package com.kkoz.parallels.lab_4.histogram;

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

public class Lab4HistogramPresenter extends Presenter<Lab4HistogramView> {

    public Lab4HistogramPresenter(Lab4HistogramView view) {
        super(view);
    }

    public void histogram(InputStream imageStream,
                          String threadsCountValue) {
        try {
            var threads = Integer.parseInt(threadsCountValue);

            var bufferedImage = ImageIO.read(imageStream);

            var width = bufferedImage.getWidth();
            var height = bufferedImage.getHeight();

            var oldMatrix = new int[width][height];
            var newMatrix = new int[width][height];
            var contrastMatrix = new int[width][height];
            var uniformityMatrix = new int[width][height];
            var entropyMatrix = new int[width][height];
            var energyMatrix = new int[width][height];

            for (var x = 0; x < width; x++) {
                var oldHeight = new int[height];

                for (var y = 0; y < height; y++) {
                    var rgb = bufferedImage.getRGB(x, y);
                    var color = new Color(rgb);

                    oldHeight[y] = (int) (0.2125 * color.getRed() + 0.7154 * color.getGreen() + 0.0721 * color.getBlue());
                }
                oldMatrix[x] = oldHeight;
                newMatrix[x] = new int[height];
                contrastMatrix[x] = new int[height];
                uniformityMatrix[x] = new int[height];
                entropyMatrix[x] = new int[height];
                energyMatrix[x] = new int[height];
            }

            var startTime = System.currentTimeMillis();

            var executor = Executors.newFixedThreadPool(threads);

            var tasks = new ArrayList<Future<Void>>();

            for (var i = 0; i < threads; i++) {
                var start = (width / threads) * i;
                var end = (width / threads) * (i + 1);
                tasks.add(
                    executor.submit(
                        () -> a(oldMatrix, contrastMatrix, uniformityMatrix, entropyMatrix, energyMatrix, start, end, width, height)
                    )
                );
            }

            for (var task : tasks) {
                task.get();
            }

            executor.shutdown();

            var time = (System.currentTimeMillis() - startTime) / 1000.0;

            var contrastBufferedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
            var uniformityBufferedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
            var entropyBufferedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
            var energyBufferedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
            for (var x = 0; x < width; x++) {
                for (var y = 0; y < height; y++) {
                    var color = new Color(
                        RGB.checkBorderValues(contrastMatrix[x][y]),
                        RGB.checkBorderValues(contrastMatrix[x][y]),
                        RGB.checkBorderValues(contrastMatrix[x][y])
                    );

                    contrastBufferedImage.setRGB(x, y, color.getRGB());

                    color = new Color(
                        RGB.checkBorderValues(uniformityMatrix[x][y]),
                        RGB.checkBorderValues(uniformityMatrix[x][y]),
                        RGB.checkBorderValues(uniformityMatrix[x][y])
                    );

                    uniformityBufferedImage.setRGB(x, y, color.getRGB());

                    color = new Color(
                        RGB.checkBorderValues(entropyMatrix[x][y]),
                        RGB.checkBorderValues(entropyMatrix[x][y]),
                        RGB.checkBorderValues(entropyMatrix[x][y])
                    );

                    entropyBufferedImage.setRGB(x, y, color.getRGB());

                    color = new Color(
                        RGB.checkBorderValues(energyMatrix[x][y]),
                        RGB.checkBorderValues(energyMatrix[x][y]),
                        RGB.checkBorderValues(energyMatrix[x][y])
                    );

                    energyBufferedImage.setRGB(x, y, color.getRGB());
                }
            }

            view.refreshFilterPhotosSection(getInputStreamFromBufferedImage(bufferedImage));

            view.createResultSection(time, null, null, null, null);

            view.refreshMapsSection(
                List.of(
                    getInputStreamFromBufferedImage(contrastBufferedImage),
                    getInputStreamFromBufferedImage(uniformityBufferedImage),
                    getInputStreamFromBufferedImage(entropyBufferedImage),
                    getInputStreamFromBufferedImage(energyBufferedImage)
                )
            );
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private Void a(int[][] oldMatrix,
                   int[][] contrastMatrix,
                   int[][] uniformityMatrix,
                   int[][] entropyMatrix,
                   int[][] energyMatrix,
                   int width0,
                   int width1,
                   int width,
                   int height) {
        for (var x = width0; x < width1; x++) {
            for (var y = 0; y < height; y++) {
                var histogram = new double[256];
                var sum = 0D;
                var count = 0;
                var xmin = Math.max(0, x - 2);
                var xmax = Math.min(x + 2, width - 1);
                var ymin = Math.max(0, y - 2);
                var ymax = Math.min(y + 2, height - 1);
                for (var x0 = xmin; x0 <= xmax; x0++) {
                    for (var y0 = ymin; y0 <= ymax; y0++) {
                        var value = oldMatrix[x0][y0];
                        sum += value;
                        histogram[value]++;
                        count++;
                    }
                }
                var average = (int) (sum / count);

                var contrastValue = 0D;
                var uniformityValue = 0D;
                var entropyValue = 0D;
                var energyValue = 0D;

                for (var i = 0; i < 256; i++) {
                    var prob = histogram[i] / count;

                    contrastValue += Math.pow(i - average, 2) * prob;
                    uniformityValue += Math.pow(prob, 2);
                    entropyValue -= prob == 0 ? 0 : (Math.log(prob) / Math.log(2)) * prob;
                    energyValue += Math.pow(prob, 2);
                }
                contrastMatrix[x][y] = (int) contrastValue;
                uniformityMatrix[x][y] = (int) (uniformityValue * 255);
                entropyMatrix[x][y] = (int) (Math.abs(entropyValue) * 63);
                energyMatrix[x][y] = (int) (Math.sqrt(energyValue) * 255);
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

            var avgTime1 = 0.;

            for (var i = 0; i < 10; i++) {
                avgTime1 += startParallel(
                    1,
                    oldMatrix,
                    newMatrix,
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
                    () -> a(oldMatrix, newMatrix, newMatrix, newMatrix, newMatrix, start, end, width, height)
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
