package com.kkoz.parallels.lab_3.segmentation.quatro_tree;

import com.kkoz.parallels.Presenter;
import com.kkoz.parallels.RGB;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class Lab3QuatroTreePresenter extends Presenter<Lab3QuatroTreeView> {

    public Lab3QuatroTreePresenter(Lab3QuatroTreeView view) {
        super(view);
    }

    public void segment(InputStream imageStream) {
        try {
            var bufferedImage = ImageIO.read(imageStream);

            var width = bufferedImage.getWidth();
            var height = bufferedImage.getHeight();

            var segmentsCount = new int[]{1};

            var oldMatrix = new int[width][height];
            var newMatrix = new int[width][height];
            var newMatrixBorders = new int[2 * width - 1][2 * height - 1];

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

            for (var x = 0; x < 2 * width - 1; x++) {
                newMatrixBorders[x] = new int[2 * height - 1];
            }

            bufferedImage = new BufferedImage(2 * width - 1, 2 * height - 1, BufferedImage.TYPE_INT_RGB);

            calculateSegment(oldMatrix, newMatrixBorders, segmentsCount, 0, width, 0, height);

            for (var x = 0; x < 2 * width - 1; x++) {
                for (var y = 0; y < 2 * height - 1; y++) {
                    var color = new Color(
                        RGB.checkBorderValues(newMatrixBorders[x][y]),
                        RGB.checkBorderValues(newMatrixBorders[x][y]),
                        RGB.checkBorderValues(newMatrixBorders[x][y])
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

    private void calculateSegment(int[][] oldMatrix,
                                  int[][] newMatrixBorders,
                                  int[] segmentsCount,
                                  int width0,
                                  int width1,
                                  int height0,
                                  int height1) {
        if (width1 - width0 == 1 && height1 - height0 == 1) {
            newMatrixBorders[2 * width0][2 * height0] = oldMatrix[width0][height0];
            return;
        }
        var min = oldMatrix[width0][height0];
        var max = oldMatrix[width0][height0];
        for (var x = width0; x < width1; x++) {
            for (var y = height0; y < height1; y++) {
                if (oldMatrix[x][y] < min) {
                    min = oldMatrix[x][y];
                }
                if (oldMatrix[x][y] > max) {
                    max = oldMatrix[x][y];
                }
                if (max - min > 10) {
                    segmentsCount[0] += 3;
                    var widthCenter = (width0 + width1) / 2;
                    var heightCenter = (height0 + height1) / 2;

                    for (var i = 2 * width0 + 1; i < 2 * width1 - 1; i++) {
                        newMatrixBorders[i][2 * heightCenter] = 0;
                    }

                    for (var i = 2 * height0 + 1; i < 2 * height1 - 1; i++) {
                        newMatrixBorders[2 * widthCenter][i] = 0;
                    }

                    calculateSegment(oldMatrix, newMatrixBorders, segmentsCount, width0, widthCenter, height0, heightCenter);
                    calculateSegment(oldMatrix, newMatrixBorders, segmentsCount, widthCenter, width1, height0, heightCenter);
                    calculateSegment(oldMatrix, newMatrixBorders, segmentsCount, width0, widthCenter, heightCenter, height1);
                    calculateSegment(oldMatrix, newMatrixBorders, segmentsCount, widthCenter, width1, heightCenter, height1);
                    return;
                }
            }
        }
        for (var x = 2 * width0; x < 2 * width1 - 1; x++) {
            for (var y = 2 * height0; y < 2 * height1 - 1; y++) {
                newMatrixBorders[x][y] = (max + min) / 2;
            }
        }
    }

    private InputStream getInputStreamFromBufferedImage(BufferedImage bufferedImage) throws IOException {
        var byteBuffer = new ByteArrayOutputStream();
        ImageIO.write(bufferedImage, "jpeg", byteBuffer);
        return new ByteArrayInputStream(byteBuffer.toByteArray());
    }
}
