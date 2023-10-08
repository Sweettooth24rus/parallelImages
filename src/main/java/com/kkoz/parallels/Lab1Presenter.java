package com.kkoz.parallels;

import com.vaadin.flow.component.upload.SucceededEvent;
import com.vaadin.flow.component.upload.receivers.MultiFileMemoryBuffer;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class Lab1Presenter {
    private final Lab1View view;

    private List<List<Integer>> redMatrix;
    private List<List<Integer>> greenMatrix;
    private List<List<Integer>> blueMatrix;

    public Lab1Presenter(Lab1View view) {
        this.view = view;
    }

    public void onPhotoUploaded(SucceededEvent event, MultiFileMemoryBuffer buffer) {
        try {
            var inputStream = buffer.getInputStream(event.getFileName());
            view.refreshSourcePhotoSection(inputStream);
            var bufferedImage = ImageIO.read(buffer.getInputStream(event.getFileName()));
            var width = bufferedImage.getWidth();
            var height = bufferedImage.getHeight();
            redMatrix = new ArrayList<>(width);
            greenMatrix = new ArrayList<>(width);
            blueMatrix = new ArrayList<>(width);
            for (var x = 0; x < width; x++) {
                var redHeight = new ArrayList<Integer>(height);
                var greenHeight = new ArrayList<Integer>(height);
                var blueHeight = new ArrayList<Integer>(height);
                for (var y = 0; y < height; y++) {
                    var rgb = bufferedImage.getRGB(x, y);
                    var color = new java.awt.Color(rgb);
                    redHeight.add(color.getRed());
                    greenHeight.add(color.getGreen());
                    blueHeight.add(color.getBlue());
                }
                redMatrix.add(x, redHeight);
                greenMatrix.add(x, greenHeight);
                blueMatrix.add(x, blueHeight);
            }

            var bufferedImageRed = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
            var bufferedImageGreen = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
            var bufferedImageBlue = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

            for (var x = 0; x < width; x++) {
                var redHeight = redMatrix.get(x);
                var greenHeight = greenMatrix.get(x);
                var blueHeight = blueMatrix.get(x);
                for (var y = 0; y < height; y++) {
                    var red = redHeight.get(y);
                    var green = greenHeight.get(y);
                    var blue = blueHeight.get(y);
                    bufferedImageRed.setRGB(x, y, new Color(red, 0, 0).getRGB());
                    bufferedImageGreen.setRGB(x, y, new Color(0, green, 0).getRGB());
                    bufferedImageBlue.setRGB(x, y, new Color(0, 0, blue).getRGB());
                }
            }

            view.refreshChannelsPhotoSection(
                getInputStreamFromBufferedImage(bufferedImageRed),
                getInputStreamFromBufferedImage(bufferedImageGreen),
                getInputStreamFromBufferedImage(bufferedImageBlue)
            );

        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public InputStream getInputStreamFromBufferedImage(BufferedImage bufferedImage) throws IOException {
        var byteBuffer = new ByteArrayOutputStream();
        ImageIO.write(bufferedImage, "jpeg", byteBuffer);
        return new ByteArrayInputStream(byteBuffer.toByteArray());
    }
}
