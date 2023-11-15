package com.kkoz.parallels.lab_1;

import com.kkoz.parallels.RGB;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;

public class Lab1GreyWorldPresenter {
    private final Lab1GreyWorldView view;

    public Lab1GreyWorldPresenter(Lab1GreyWorldView view) {
        this.view = view;
    }

    public void makeGray(InputStream imageStream) {
        try {
            var bufferedImage = ImageIO.read(imageStream);

            var width = bufferedImage.getWidth();
            var height = bufferedImage.getHeight();

            var redBigValue = BigInteger.ZERO;
            var greenBigValue = BigInteger.ZERO;
            var blueBigValue = BigInteger.ZERO;

            for (var x = 0; x < width; x++) {
                for (var y = 0; y < height; y++) {
                    var rgb = bufferedImage.getRGB(x, y);
                    var color = new Color(rgb);

                    redBigValue = redBigValue.add(BigInteger.valueOf(color.getRed()));
                    greenBigValue = greenBigValue.add(BigInteger.valueOf(color.getGreen()));
                    blueBigValue = blueBigValue.add(BigInteger.valueOf(color.getBlue()));
                }
            }

            redBigValue = redBigValue.divide(BigInteger.valueOf((long) width * height));
            greenBigValue = greenBigValue.divide(BigInteger.valueOf((long) width * height));
            blueBigValue = blueBigValue.divide(BigInteger.valueOf((long) width * height));

            var avg = redBigValue.add(greenBigValue).add(blueBigValue).divide(BigInteger.valueOf(3)).doubleValue();

            var redValue = redBigValue.doubleValue();
            var greenValue = greenBigValue.doubleValue();
            var blueValue = blueBigValue.doubleValue();

            for (var x = 0; x < width; x++) {
                for (var y = 0; y < height; y++) {
                    var rgb = bufferedImage.getRGB(x, y);
                    var color = new Color(rgb);

                    var greyColor = new Color(
                        RGB.checkBorderValues(color.getRed() * avg / redValue),
                        RGB.checkBorderValues(color.getGreen() * avg / greenValue),
                        RGB.checkBorderValues(color.getBlue() * avg / blueValue)
                    );

                    bufferedImage.setRGB(x, y, greyColor.getRGB());
                }
            }

            view.refreshFilterPhotosSection(getInputStreamFromBufferedImage(bufferedImage));
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    private InputStream getInputStreamFromBufferedImage(BufferedImage bufferedImage) throws IOException {
        var byteBuffer = new ByteArrayOutputStream();
        ImageIO.write(bufferedImage, "jpeg", byteBuffer);
        return new ByteArrayInputStream(byteBuffer.toByteArray());
    }
}
