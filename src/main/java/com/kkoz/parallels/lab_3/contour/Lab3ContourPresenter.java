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
                        String threadsCountValue) {
        try {
            var threads = Integer.parseInt(threadsCountValue);
            var threshold = Double.parseDouble(thresholdValue);
            var gain = Double.parseDouble(gainValue);

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

    private InputStream getInputStreamFromBufferedImage(BufferedImage bufferedImage) throws IOException {
        var byteBuffer = new ByteArrayOutputStream();
        ImageIO.write(bufferedImage, "jpeg", byteBuffer);
        return new ByteArrayInputStream(byteBuffer.toByteArray());
    }
}
