package com.kkoz.parallels.lab_2.noises.impulse_noise;

import com.kkoz.parallels.ChannelData;
import com.kkoz.parallels.Labs;
import com.kkoz.parallels.SplitType;
import com.kkoz.parallels.View;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.HasComponents;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.component.upload.receivers.MultiFileMemoryBuffer;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouteAlias;
import com.vaadin.flow.server.StreamResource;
import org.apache.commons.lang3.StringUtils;

import java.io.InputStream;
import java.util.Arrays;

@Route("/labs/2/noises/impulse")
@RouteAlias("/labs/2/noises")
@RouteAlias("/labs/2")
public class Lab2ImpulseNoiseView extends View<Lab2ImpulseNoisePresenter> {
    private final MultiFileMemoryBuffer buffer = new MultiFileMemoryBuffer();
    private final HorizontalLayout imageSection = new HorizontalLayout();
    private final HorizontalLayout channelsSection = new HorizontalLayout();
    private final TextField channel1NoisePercentTextField = new TextField();
    private final TextField channel2NoisePercentTextField = new TextField();
    private final TextField channel3NoisePercentTextField = new TextField();
    private final TextField channel1ImpulseProportionTextField = new TextField();
    private final TextField channel2ImpulseProportionTextField = new TextField();
    private final TextField channel3ImpulseProportionTextField = new TextField();

    private String photoFileName;
    private ComboBox<SplitType> splitTypeComboBox;

    public Lab2ImpulseNoiseView() {
        super(Lab2ImpulseNoisePresenter.class, Labs.LAB_2);

        channel1NoisePercentTextField.setLabel("Процент шума");
        channel1NoisePercentTextField.setValue("0");
        channel2NoisePercentTextField.setLabel("Процент шума");
        channel2NoisePercentTextField.setValue("0");
        channel3NoisePercentTextField.setLabel("Процент шума");
        channel3NoisePercentTextField.setValue("0");

        channel1ImpulseProportionTextField.setLabel("Вероятность импульса");
        channel1ImpulseProportionTextField.setValue("50");
        channel2ImpulseProportionTextField.setLabel("Вероятность импульса");
        channel2ImpulseProportionTextField.setValue("50");
        channel3ImpulseProportionTextField.setLabel("Вероятность импульса");
        channel3ImpulseProportionTextField.setValue("50");

        add(
            createUploadPhotoSection(),
            createSplitTypeComboBox(),
            imageSection,
            channelsSection
        );
    }

    private Component createUploadPhotoSection() {
        var upload = new Upload(buffer);

        upload.addSucceededListener(event -> {
            photoFileName = event.getFileName();
            presenter.splitImageToChannels(
                buffer.getInputStream(photoFileName),
                splitTypeComboBox.getValue(),
                channel1NoisePercentTextField.getValue(),
                channel2NoisePercentTextField.getValue(),
                channel3NoisePercentTextField.getValue(),
                channel1ImpulseProportionTextField.getValue(),
                channel2ImpulseProportionTextField.getValue(),
                channel3ImpulseProportionTextField.getValue()
            );
            upload.clearFileList();
        });

        return upload;
    }

    private ComboBox<SplitType> createSplitTypeComboBox() {
        splitTypeComboBox = new ComboBox<>();
        splitTypeComboBox.setItems(SplitType.values());
        splitTypeComboBox.setValue(SplitType.RGB);
        splitTypeComboBox.addValueChangeListener(event -> {
            if (StringUtils.isNotBlank(photoFileName)) {
                presenter.splitImageToChannels(
                    buffer.getInputStream(photoFileName),
                    event.getValue(),
                    channel1NoisePercentTextField.getValue(),
                    channel2NoisePercentTextField.getValue(),
                    channel3NoisePercentTextField.getValue(),
                    channel1ImpulseProportionTextField.getValue(),
                    channel2ImpulseProportionTextField.getValue(),
                    channel3ImpulseProportionTextField.getValue()
                );
            }
        });
        return splitTypeComboBox;
    }

    public void refreshPhotosSection(InputStream imageStream) {
        imageSection.removeAll();
        addPhotoToSection(imageSection, buffer.getInputStream(photoFileName), "До фильтра");
        addPhotoToSection(imageSection, imageStream, "После фильтра");
    }

    public void refreshChannelsSection(ChannelData firstChannel,
                                       ChannelData secondChannel,
                                       ChannelData thirdChannel) {
        channelsSection.removeAll();
        addToSection(channelsSection, firstChannel, "Первый канал", channel1NoisePercentTextField, channel1ImpulseProportionTextField);
        addToSection(channelsSection, secondChannel, "Второй канал", channel2NoisePercentTextField, channel2ImpulseProportionTextField);
        addToSection(channelsSection, thirdChannel, "Третий канал", channel3NoisePercentTextField, channel3ImpulseProportionTextField);
    }

    private void addToSection(HorizontalLayout section,
                              ChannelData data,
                              String name,
                              TextField channelNoisePercentTextField,
                              TextField channelImpulseProportionTextField) {
        var channel = new VerticalLayout();
        channel.setWidthFull();

        var submitButton = new Button("Применить");
        submitButton.addClickListener(e -> {
            if (StringUtils.isNotBlank(photoFileName)) {
                presenter.splitImageToChannels(
                    buffer.getInputStream(photoFileName),
                    splitTypeComboBox.getValue(),
                    channel1NoisePercentTextField.getValue(),
                    channel2NoisePercentTextField.getValue(),
                    channel3NoisePercentTextField.getValue(),
                    channel1ImpulseProportionTextField.getValue(),
                    channel2ImpulseProportionTextField.getValue(),
                    channel3ImpulseProportionTextField.getValue()
                );
            }
        });

        channel.add(
            new HorizontalLayout(
                channelNoisePercentTextField,
                channelImpulseProportionTextField,
                submitButton
            )
        );

        addPhotoToSection(channel, data.getImageStream(), name);

        addHistogramToSection(channel, data.getHistogram());

        section.add(channel);
    }

    private void addPhotoToSection(HasComponents section, InputStream imageStream, String name) {
        var image = new Image(
            new StreamResource(name, () -> imageStream),
            name
        );
        image.setWidthFull();
        section.add(image);
    }

    private void addHistogramToSection(HasComponents section, int[] channelValues) {
        var histogram = new HorizontalLayout();
        histogram.setWidthFull();
        histogram.setSpacing(false);
        histogram.getStyle().set("align-items", "end");

        var maxValue = Arrays.stream(channelValues).max().orElse(0);

        for (var value : channelValues) {
            var line = new VerticalLayout();
            line.setPadding(false);
            line.setSpacing(false);
            line.setWidth("1px");
            line.setHeight(String.format("%spx", (value * 500) / maxValue));
            line.getStyle().set("background-color", "black");
            line.getStyle().set("padding", "1px 0 0 0");
            histogram.add(line);
        }
        section.add(histogram);
    }
}
