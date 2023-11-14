package com.kkoz.parallels.lab_1;

import com.kkoz.parallels.ChannelData;
import com.kkoz.parallels.SplitType;
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
import com.vaadin.flow.server.StreamResource;
import org.apache.commons.lang3.StringUtils;

import java.io.InputStream;
import java.util.Arrays;

@Route("/labs/1")
public class Lab1View extends VerticalLayout {
    private final Lab1Presenter presenter;
    private final MultiFileMemoryBuffer buffer = new MultiFileMemoryBuffer();
    private final HorizontalLayout sourceImageSection = new HorizontalLayout();
    private final HorizontalLayout sourceChannelsSection = new HorizontalLayout();
    private final HorizontalLayout filterImageSection = new HorizontalLayout();
    private final HorizontalLayout filterChannelsSection = new HorizontalLayout();

    private String photoFileName;
    private ComboBox<SplitType> splitTypeComboBox;

    Lab1View() {
        presenter = new Lab1Presenter(this);

        add(
            createUploadPhotoSection(),
            createSplitTypeComboBox(),
            sourceImageSection,
            sourceChannelsSection,
            createFilterSection(),
            filterImageSection,
            filterChannelsSection
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

    private Component createFilterSection() {
        var container = new HorizontalLayout();
        container.getStyle().set("align-items", "end");

        var lightnessField = new TextField("Яркость", "Введите величину изменения яркости");
        container.add(lightnessField);

        var submitButton = new Button(
            "Применить",
            e -> presenter.applyYUVFilters(
                buffer.getInputStream(photoFileName),
                lightnessField.getValue()
            )
        );
        container.add(submitButton);

        return container;
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

    public void refreshFilterPhotosSection(InputStream imageStream) {
        filterImageSection.removeAll();
        addPhotoToSection(filterImageSection, buffer.getInputStream(photoFileName), "До фильтра");
        addPhotoToSection(filterImageSection, imageStream, "После фильтра");
    }

    public void refreshFilterChannelsSection(ChannelData firstChannel,
                                             ChannelData secondChannel,
                                             ChannelData thirdChannel) {
        filterChannelsSection.removeAll();
        addToSection(filterChannelsSection, firstChannel, "Первый канал");
        addToSection(filterChannelsSection, secondChannel, "Второй канал");
        addToSection(filterChannelsSection, thirdChannel, "Третий канал");
    }
}
