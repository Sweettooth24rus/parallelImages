package com.kkoz.parallels.lab_1;

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

public class Lab1GreyWorldPresenter {
    private final Lab1GreyWorldView view;

    public Lab1GreyWorldPresenter(Lab1GreyWorldView view) {
        this.view = view;
    }

    public void makeGray(InputStream imageStream, String threadsValue) {
        try {
            var threads = Integer.parseInt(threadsValue);

            var bufferedImage = ImageIO.read(imageStream);

            var width = bufferedImage.getWidth();
            var height = bufferedImage.getHeight();

            var redMatrix = new Integer[width][height];
            var greenMatrix = new Integer[width][height];
            var blueMatrix = new Integer[width][height];

            for (var x = 0; x < width; x++) {
                var redHeight = new Integer[height];
                var greenHeight = new Integer[height];
                var blueHeight = new Integer[height];
                for (var y = 0; y < height; y++) {
                    var color = new Color(bufferedImage.getRGB(x, y));

                    redHeight[y] = color.getRed();
                    greenHeight[y] = color.getGreen();
                    blueHeight[y] = color.getBlue();
                }
                redMatrix[x] = redHeight;
                greenMatrix[x] = greenHeight;
                blueMatrix[x] = blueHeight;
            }

            var startTime = System.currentTimeMillis();

            var executor = Executors.newFixedThreadPool(threads);

            var valueTasks = new ArrayList<Future<Integer[]>>();

            for (var i = 0; i < threads; i++) {
                var start = (width / threads) * i;
                var end = (width / threads) * (i + 1);
                valueTasks.add(
                    executor.submit(
                        () -> computeValues(
                            redMatrix,
                            greenMatrix,
                            blueMatrix,
                            height,
                            start,
                            end
                        )
                    )
                );
            }

            var valueResults = new ArrayList<Integer[]>();

            for (var task : valueTasks) {
                valueResults.add(task.get());
            }

            var redValue = 0D;
            var greenValue = 0D;
            var blueValue = 0D;

            for (var result : valueResults) {
                redValue += result[0];
                greenValue += result[1];
                blueValue += result[2];
            }

            redValue /= width * height;
            greenValue /= (width * height);
            blueValue /= (width * height);

            var avg = (redValue + greenValue + blueValue) / 3;

            executor = Executors.newFixedThreadPool(threads);

            var greyWorldTasks = new ArrayList<Future<Void>>();

            for (var i = 0; i < threads; i++) {
                var start = (width / threads) * i;
                var end = (width / threads) * (i + 1);
                double finalRedValue = redValue;
                double finalBlueValue = blueValue;
                double finalGreenValue = greenValue;
                greyWorldTasks.add(
                    executor.submit(
                        () -> computeGreyValue(
                            redMatrix,
                            greenMatrix,
                            blueMatrix,
                            finalRedValue,
                            finalGreenValue,
                            finalBlueValue,
                            avg,
                            height,
                            start,
                            end
                        )
                    )
                );
            }

            for (var task : greyWorldTasks) {
                task.get();
            }

            var time = (System.currentTimeMillis() - startTime) / 1000.0;

            for (var x = 0; x < width; x++) {
                for (var y = 0; y < height; y++) {
                    var greyWorldColor = new Color(
                        RGB.checkBorderValues(redMatrix[x][y]),
                        RGB.checkBorderValues(greenMatrix[x][y]),
                        RGB.checkBorderValues(blueMatrix[x][y])
                    );

                    bufferedImage.setRGB(x, y, greyWorldColor.getRGB());
                }
            }

            view.refreshFilterPhotosSection(getInputStreamFromBufferedImage(bufferedImage));

            view.createResultSection(time);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private Integer[] computeValues(Integer[][] redMatrix, Integer[][] greenMatrix, Integer[][] blueMatrix, int height, int i, int i1) {
        var redValue = 0;
        var greenValue = 0;
        var blueValue = 0;

        for (int x = i; x < i1; x++) {
            for (var y = 0; y < height; y++) {
                redValue += redMatrix[x][y];
                greenValue += greenMatrix[x][y];
                blueValue += blueMatrix[x][y];
            }
        }

        return new Integer[]{redValue, greenValue, blueValue};
    }

    private Void computeGreyValue(Integer[][] redMatrix, Integer[][] greenMatrix, Integer[][] blueMatrix, double redValue, double greenValue, double blueValue, double avg, int height, int i, int i1) {
        for (int x = i; x < i1; x++) {
            for (var y = 0; y < height; y++) {
                redMatrix[x][y] = (int) (redMatrix[x][y] * avg / redValue);
                greenMatrix[x][y] = (int) (greenMatrix[x][y] * avg / greenValue);
                blueMatrix[x][y] = (int) (blueMatrix[x][y] * avg / blueValue);
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
