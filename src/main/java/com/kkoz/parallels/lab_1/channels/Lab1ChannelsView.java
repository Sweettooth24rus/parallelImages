package com.kkoz.parallels.lab_1.channels;

import com.kkoz.parallels.ChannelData;
import com.kkoz.parallels.Labs;
import com.kkoz.parallels.SplitType;
import com.kkoz.parallels.View;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.HasComponents;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.component.upload.receivers.MultiFileMemoryBuffer;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouteAlias;
import com.vaadin.flow.server.StreamResource;
import org.apache.commons.lang3.StringUtils;

import java.io.InputStream;
import java.util.Arrays;

@Route("/labs/1/channels")
@RouteAlias("/labs/1")
public class Lab1ChannelsView extends View<Lab1ChannelsPresenter> {
    private final MultiFileMemoryBuffer buffer = new MultiFileMemoryBuffer();
    private final HorizontalLayout sourceImageSection = new HorizontalLayout();
    private final HorizontalLayout sourceChannelsSection = new HorizontalLayout();

    private String photoFileName;
    private ComboBox<SplitType> splitTypeComboBox;

    public Lab1ChannelsView() {
        super(Lab1ChannelsPresenter.class, Labs.LAB_1);

        add(
            createUploadPhotoSection(),
            createSplitTypeComboBox(),
            sourceImageSection,
            sourceChannelsSection
        );
    }

    private Component createUploadPhotoSection() {
        var upload = new Upload(buffer);

        upload.addSucceededListener(event -> {
            photoFileName = event.getFileName();
            var inputStream = buffer.getInputStream(photoFileName);
            refreshSourcePhotoSection(inputStream);
            presenter.splitImageToChannels(
                buffer.getInputStream(photoFileName),
                splitTypeComboBox.getValue()
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
                    event.getValue()
                );
            }
        });
        return splitTypeComboBox;
    }

    public void refreshSourcePhotoSection(InputStream sourceImageStream) {
        sourceImageSection.removeAll();
        addPhotoToSection(sourceImageSection, sourceImageStream, "Исходное фото");
    }

    public void refreshChannelsSection(ChannelData firstChannel,
                                       ChannelData secondChannel,
                                       ChannelData thirdChannel) {
        sourceChannelsSection.removeAll();
        addToSection(sourceChannelsSection, firstChannel, "Первый канал");
        addToSection(sourceChannelsSection, secondChannel, "Второй канал");
        addToSection(sourceChannelsSection, thirdChannel, "Третий канал");
    }

    private void addToSection(HorizontalLayout section, ChannelData data, String name) {
        var channel = new VerticalLayout();
        channel.setWidthFull();

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
