package com.kkoz.parallels.lab_3.segmentation.watershed;

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
import java.util.Objects;

public class Lab3WatershedPresenter extends Presenter<Lab3WatershedView> {

    public Lab3WatershedPresenter(Lab3WatershedView view) {
        super(view);
    }

    public void watershed(InputStream imageStream) {
        try {
            var bufferedImage = ImageIO.read(imageStream);

            var width = bufferedImage.getWidth();
            var height = bufferedImage.getHeight();

            var segmentsCount = new int[]{0};
            var pouredIndex = new int[]{1};

            var oldMatrix = new int[width][height];
            var pouredMatrix = new int[width][height];
            var visitedMinMatrix = new int[width][height];
            var visitedMaxMatrix = new int[width][height];
            var newMatrix = new int[width][height];

            for (var x = 0; x < width; x++) {
                var oldHeight = new int[height];

                for (var y = 0; y < height; y++) {
                    var rgb = bufferedImage.getRGB(x, y);
                    var color = new Color(rgb);

                    oldHeight[y] = (int) (0.2125 * color.getRed() + 0.7154 * color.getGreen() + 0.0721 * color.getBlue());
                    if (oldHeight[y] < 30) {
                        oldHeight[y] = 0;
                    }
                }
                oldMatrix[x] = oldHeight;
                newMatrix[x] = new int[height];
                pouredMatrix[x] = new int[height];
                visitedMinMatrix[x] = new int[height];
                visitedMaxMatrix[x] = new int[height];
            }

            for (var x = 0; x < width; x++) {
                for (var y = 0; y < height; y++) {
                    if (pouredMatrix[x][y] != 0) {
                        continue;
                    }
                    var localMin = findLocalMin(oldMatrix, x, y, width, height, oldMatrix[x][y], visitedMinMatrix, pouredMatrix);
                    var localMax = findLocalMax(oldMatrix, x, y, width, height, oldMatrix[x][y], visitedMaxMatrix);
                    var localMinX = localMin.getB().getA();
                    var localMinY = localMin.getB().getB();
                    if (pouredMatrix[localMinX][localMinY] != 0 || Objects.equals(localMin.getA(), localMax)) {
                        continue;
                    }
                    pour(oldMatrix, localMinX, localMinY, width, height, newMatrix, pouredMatrix, pouredIndex, localMax);
                    segmentsCount[0]++;
                    pouredIndex[0]++;
                }
            }

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

            view.getSegmentsCount().setValue(String.valueOf(segmentsCount[0]));
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private Pair<Integer, Pair<Integer, Integer>> findLocalMin(int[][] oldMatrix,
                                                               int x,
                                                               int y,
                                                               int width,
                                                               int height,
                                                               int localMin,
                                                               int[][] visitedMatrix,
                                                               int[][] pouredMatrix) {
        if (pouredMatrix[x][y] != 0) {
            return new Pair<>(255, new Pair<>(x, y));
        }
        visitedMatrix[x][y] = 1;
        var x0 = Math.max(x - 1, 0);
        var x1 = Math.min(x + 1, width - 1);
        var y0 = Math.max(y - 1, 0);
        var y1 = Math.min(y + 1, height - 1);

        var localMinX0 = new Pair<>(localMin, new Pair<>(x, y));
        var localMinX1 = new Pair<>(localMin, new Pair<>(x, y));
        var localMinY0 = new Pair<>(localMin, new Pair<>(x, y));
        var localMinY1 = new Pair<>(localMin, new Pair<>(x, y));

        if (visitedMatrix[x0][y] == 0 && oldMatrix[x0][y] < localMin) {
            localMinX0 = findLocalMin(oldMatrix, x0, y, width, height, oldMatrix[x0][y], visitedMatrix, pouredMatrix);
        }
        if (visitedMatrix[x1][y] == 0 && oldMatrix[x1][y] < localMin) {
            localMinX1 = findLocalMin(oldMatrix, x1, y, width, height, oldMatrix[x1][y], visitedMatrix, pouredMatrix);
        }
        if (visitedMatrix[x][y0] == 0 && oldMatrix[x][y0] < localMin) {
            localMinY0 = findLocalMin(oldMatrix, x, y0, width, height, oldMatrix[x][y0], visitedMatrix, pouredMatrix);
        }
        if (visitedMatrix[x][y1] == 0 && oldMatrix[x][y1] < localMin) {
            localMinY1 = findLocalMin(oldMatrix, x, y1, width, height, oldMatrix[x][y1], visitedMatrix, pouredMatrix);
        }

        var min = Math.min(localMinX0.getA(), Math.min(localMinX1.getA(), Math.min(localMinY0.getA(), Math.min(localMinY1.getA(), localMin))));
        if (min == localMinX0.getA()) {
            return localMinX0;
        }
        if (min == localMinX1.getA()) {
            return localMinX1;
        }
        if (min == localMinY0.getA()) {
            return localMinY0;
        }
        if (min == localMinY1.getA()) {
            return localMinY1;
        }
        return new Pair<>(localMin, new Pair<>(x, y));
    }

    private Integer findLocalMax(int[][] oldMatrix,
                                 int x,
                                 int y,
                                 int width,
                                 int height,
                                 int localMax,
                                 int[][] visitedMatrix) {
        visitedMatrix[x][y] = 1;

        var x0 = Math.max(x - 1, 0);
        var x1 = Math.min(x + 1, width - 1);
        var y0 = Math.max(y - 1, 0);
        var y1 = Math.min(y + 1, height - 1);

        var localMaxX0 = localMax;
        var localMaxX1 = localMax;
        var localMaxY0 = localMax;
        var localMaxY1 = localMax;

        if (visitedMatrix[x0][y] == 0 && oldMatrix[x0][y] > localMax) {
            localMaxX0 = findLocalMax(oldMatrix, x0, y, width, height, oldMatrix[x0][y], visitedMatrix);
        }
        if (visitedMatrix[x1][y] == 0 && oldMatrix[x1][y] > localMax) {
            localMaxX1 = findLocalMax(oldMatrix, x1, y, width, height, oldMatrix[x1][y], visitedMatrix);
        }
        if (visitedMatrix[x][y0] == 0 && oldMatrix[x][y0] > localMax) {
            localMaxY0 = findLocalMax(oldMatrix, x, y0, width, height, oldMatrix[x][y0], visitedMatrix);
        }
        if (visitedMatrix[x][y1] == 0 && oldMatrix[x][y1] > localMax) {
            localMaxY1 = findLocalMax(oldMatrix, x, y1, width, height, oldMatrix[x][y1], visitedMatrix);
        }

        return Math.min(localMaxX0, Math.min(localMaxX1, Math.min(localMaxY0, Math.min(localMaxY1, localMax))));
    }

    private void pour(int[][] oldMatrix, int x, int y, int width, int height, int[][] newMatrix, int[][] pouredMatrix, int[] pouredIndex, int localMax) {
        if (pouredMatrix[x][y] != 0 || oldMatrix[x][y] >= localMax) {
            return;
        }
        pouredMatrix[x][y] = pouredIndex[0];
        newMatrix[x][y] = 255;

        var x0 = x - 1;
        var x1 = x + 1;
        var y0 = y - 1;
        var y1 = y + 1;

        if (x0 >= 0 && checkWaters(x0, y, width, height, pouredMatrix, pouredIndex)) {
            pour(oldMatrix, x0, y, width, height, newMatrix, pouredMatrix, pouredIndex, localMax);
        }
        if (x1 < width && checkWaters(x1, y, width, height, pouredMatrix, pouredIndex)) {
            pour(oldMatrix, x1, y, width, height, newMatrix, pouredMatrix, pouredIndex, localMax);
        }
        if (y0 >= 0 && checkWaters(x, y0, width, height, pouredMatrix, pouredIndex)) {
            pour(oldMatrix, x, y0, width, height, newMatrix, pouredMatrix, pouredIndex, localMax);
        }
        if (y1 < height && checkWaters(x, y1, width, height, pouredMatrix, pouredIndex)) {
            pour(oldMatrix, x, y1, width, height, newMatrix, pouredMatrix, pouredIndex, localMax);
        }
    }

    private boolean checkWaters(int x, int y, int width, int height, int[][] pouredMatrix, int[] pouredIndex) {
        var x0 = x - 1;
        var x1 = x + 1;
        var y0 = y - 1;
        var y1 = y + 1;

        if (x0 > 0 && pouredMatrix[x0][y] != 0 && pouredMatrix[x0][y] != pouredIndex[0]) {
            return false;
        }
        if (x1 < width && pouredMatrix[x1][y] != 0 && pouredMatrix[x1][y] != pouredIndex[0]) {
            return false;
        }
        if (y0 > 0 && pouredMatrix[x][y0] != 0 && pouredMatrix[x][y0] != pouredIndex[0]) {
            return false;
        }
        if (y1 < height && pouredMatrix[x][y1] != 0 && pouredMatrix[x][y1] != pouredIndex[0]) {
            return false;
        }

        return true;
    }

    private InputStream getInputStreamFromBufferedImage(BufferedImage bufferedImage) throws IOException {
        var byteBuffer = new ByteArrayOutputStream();
        ImageIO.write(bufferedImage, "jpeg", byteBuffer);
        return new ByteArrayInputStream(byteBuffer.toByteArray());
    }
}
