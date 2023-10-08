package com.kkoz.parallels;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.component.upload.receivers.MultiFileMemoryBuffer;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.StreamResource;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

@Route("/labs/1")
public class Lab1View extends VerticalLayout {
    private final Lab1Presenter presenter;
    private Component sourceImageSection = new Div();
    private Component redImageSection = new Div();
    private Component greenImageSection = new Div();
    private Component blueImageSection = new Div();
    private List<List<Integer>> redMatrix;
    private List<List<Integer>> greenMatrix;
    private List<List<Integer>> blueMatrix;

    Lab1View() {
        presenter = new Lab1Presenter(this);

        add(
            createUploadPhotoSection(),
            sourceImageSection,
            new HorizontalLayout(redImageSection, greenImageSection, blueImageSection)
        );
    }

    private Component createUploadPhotoSection() {
        var buffer = new MultiFileMemoryBuffer();
        var upload = new Upload(buffer);

        upload.addSucceededListener(e -> {
            var inputStream = buffer.getInputStream(e.getFileName());
            upload.clearFileList();
            createSourcePhotoSection(inputStream);
            try {
                var bufferedImage = ImageIO.read(buffer.getInputStream(e.getFileName()));
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

                var bufferedImageRed = new java.awt.image.BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
                var bufferedImageGreen = new java.awt.image.BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
                var bufferedImageBlue = new java.awt.image.BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
                for (var x = 0; x < width; x++) {
                    var redHeight = redMatrix.get(x);
                    var greenHeight = greenMatrix.get(x);
                    var blueHeight = blueMatrix.get(x);
                    for (var y = 0; y < height; y++) {
                        var red = redHeight.get(y);
                        var green = greenHeight.get(y);
                        var blue = blueHeight.get(y);
                        bufferedImageRed.setRGB(x, y, new java.awt.Color(red, 0, 0).getRGB());
                        bufferedImageGreen.setRGB(x, y, new java.awt.Color(0, green, 0).getRGB());
                        bufferedImageBlue.setRGB(x, y, new java.awt.Color(0, 0, blue).getRGB());
                    }
                }

                var redByteBuffer = new ByteArrayOutputStream();
                ImageIO.write(bufferedImageRed, "jpeg", redByteBuffer);
                var redInputStream = new ByteArrayInputStream(redByteBuffer.toByteArray());
                var tmpRedImageSection = new Image(
                    new StreamResource("Красный", () -> redInputStream),
                    ""
                );
                replace(redImageSection, tmpRedImageSection);
                redImageSection = tmpRedImageSection;

                var greenByteBuffer = new ByteArrayOutputStream();
                ImageIO.write(bufferedImageGreen, "jpeg", greenByteBuffer);
                var greenInputStream = new ByteArrayInputStream(greenByteBuffer.toByteArray());
                var tmpGreenImageSection = new Image(
                    new StreamResource("Зелёный", () -> greenInputStream),
                    ""
                );
                replace(greenImageSection, tmpGreenImageSection);
                greenImageSection = tmpGreenImageSection;

                var blueByteBuffer = new ByteArrayOutputStream();
                ImageIO.write(bufferedImageBlue, "jpeg", blueByteBuffer);
                var blueInputStream = new ByteArrayInputStream(blueByteBuffer.toByteArray());
                var tmpBlueImageSection = new Image(
                    new StreamResource("Синий", () -> blueInputStream),
                    ""
                );
                replace(blueImageSection, tmpBlueImageSection);
                blueImageSection = tmpBlueImageSection;

            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        });

        return upload;
    }

    private void createSourcePhotoSection(InputStream imageStream) {
        var tmpImageSection = new Image(
            new StreamResource("Исходное фото", () -> imageStream),
            ""
        );
        replace(sourceImageSection, tmpImageSection);
        sourceImageSection = tmpImageSection;
    }
}
