package com.kkoz.parallels.lab_1;

import com.kkoz.parallels.ChannelData;
import com.kkoz.parallels.SplitType;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.HasComponents;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.component.upload.receivers.MultiFileMemoryBuffer;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.StreamResource;
import org.apache.commons.lang3.StringUtils;

import java.io.InputStream;
import java.util.Arrays;

@Route("/labs/1/inversion")
public class Lab1InversionView extends VerticalLayout {
    private final Lab1InversionPresenter presenter;
    private final MultiFileMemoryBuffer buffer = new MultiFileMemoryBuffer();
    private final HorizontalLayout imageSection = new HorizontalLayout();
    private final HorizontalLayout channelsSection = new HorizontalLayout();
    private final HorizontalLayout resultSection = new HorizontalLayout();
    private final Checkbox channel1FullCheckbox = new Checkbox();
    private final Checkbox channel2FullCheckbox = new Checkbox();
    private final Checkbox channel3FullCheckbox = new Checkbox();
    private final TextField channel1MinField = new TextField();
    private final TextField channel1MaxField = new TextField();
    private final TextField channel2MinField = new TextField();
    private final TextField channel2MaxField = new TextField();
    private final TextField channel3MinField = new TextField();
    private final TextField channel3MaxField = new TextField();
    private final TextField threadsCountField = new TextField();

    private String photoFileName;
    private ComboBox<SplitType> splitTypeComboBox;

    Lab1InversionView() {
        presenter = new Lab1InversionPresenter(this);

        channel1FullCheckbox.setLabel("Инвертировать полностью");
        channel1FullCheckbox.setValue(true);
        channel2FullCheckbox.setLabel("Инвертировать полностью");
        channel2FullCheckbox.setValue(true);
        channel3FullCheckbox.setLabel("Инвертировать полностью");
        channel3FullCheckbox.setValue(true);

        channel1MinField.setLabel("Минимальное значение");
        channel1MaxField.setLabel("Максимальное значение");
        channel2MinField.setLabel("Минимальное значение");
        channel2MaxField.setLabel("Максимальное значение");
        channel3MinField.setLabel("Минимальное значение");
        channel3MaxField.setLabel("Максимальное значение");

        threadsCountField.setLabel("Количество потоков");
        threadsCountField.setValue("1");
        threadsCountField.setValueChangeMode(ValueChangeMode.EAGER);

        add(
            createUploadPhotoSection(),
            createSplitTypeComboBox(),
            createResultSection(null, null, null, null, null),
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
                channel1FullCheckbox.getValue(),
                channel1MinField.getValue(),
                channel1MaxField.getValue(),
                channel2FullCheckbox.getValue(),
                channel2MinField.getValue(),
                channel2MaxField.getValue(),
                channel3FullCheckbox.getValue(),
                channel3MinField.getValue(),
                channel3MaxField.getValue(),
                threadsCountField.getValue()
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
                    channel1FullCheckbox.getValue(),
                    channel1MinField.getValue(),
                    channel1MaxField.getValue(),
                    channel2FullCheckbox.getValue(),
                    channel2MinField.getValue(),
                    channel2MaxField.getValue(),
                    channel3FullCheckbox.getValue(),
                    channel3MinField.getValue(),
                    channel3MaxField.getValue(),
                    threadsCountField.getValue()
                );
            }
        });
        return splitTypeComboBox;
    }

    public void refreshSections(InputStream imageStream,
                                ChannelData firstChannel,
                                ChannelData secondChannel,
                                ChannelData thirdChannel) {
        refreshPhotosSection(imageStream);
        refreshChannelsSection(firstChannel, secondChannel, thirdChannel);
    }

    public void refreshChannelsSection(ChannelData firstChannel,
                                       ChannelData secondChannel,
                                       ChannelData thirdChannel) {
        channelsSection.removeAll();
        addToSection(
            channelsSection,
            firstChannel,
            "Первый канал",
            channel1FullCheckbox,
            channel1MinField,
            channel1MaxField
        );
        addToSection(
            channelsSection,
            secondChannel,
            "Второй канал",
            channel2FullCheckbox,
            channel2MinField,
            channel2MaxField
        );
        addToSection(
            channelsSection,
            thirdChannel,
            "Третий канал",
            channel3FullCheckbox,
            channel3MinField,
            channel3MaxField
        );
    }

    public void refreshPhotosSection(InputStream imageStream) {
        imageSection.removeAll();
        addPhotoToSection(imageSection, buffer.getInputStream(photoFileName), "До фильтра");
        addPhotoToSection(imageSection, imageStream, "После фильтра");
    }

    private void addToSection(HorizontalLayout section,
                              ChannelData data,
                              String name,
                              Checkbox channelFullCheckbox,
                              TextField channelMinField,
                              TextField channelMaxField) {
        var channel = new VerticalLayout();
        channel.setWidthFull();

        var inversionButton = new Button("Инвертировать");
        inversionButton.addClickListener(e -> {
            if (StringUtils.isNotBlank(photoFileName)) {
                presenter.splitImageToChannels(
                    buffer.getInputStream(photoFileName),
                    splitTypeComboBox.getValue(),
                    channel1FullCheckbox.getValue(),
                    channel1MinField.getValue(),
                    channel1MaxField.getValue(),
                    channel2FullCheckbox.getValue(),
                    channel2MinField.getValue(),
                    channel2MaxField.getValue(),
                    channel3FullCheckbox.getValue(),
                    channel3MinField.getValue(),
                    channel3MaxField.getValue(),
                    threadsCountField.getValue()
                );
            }
        });

        channel.add(
            new HorizontalLayout(
                channelFullCheckbox,
                channelMinField,
                channelMaxField,
                inversionButton
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

    public Component createResultSection(Double time, Double avgTime1, Double avgTime2, Double avgTime3, Double avgTime4) {
        resultSection.removeAll();

        resultSection.add(
            threadsCountField,
            new Button(
                "Применить",
                e -> presenter.splitImageToChannels(
                    buffer.getInputStream(photoFileName),
                    splitTypeComboBox.getValue(),
                    channel1FullCheckbox.getValue(),
                    channel1MinField.getValue(),
                    channel1MaxField.getValue(),
                    channel2FullCheckbox.getValue(),
                    channel2MinField.getValue(),
                    channel2MaxField.getValue(),
                    channel3FullCheckbox.getValue(),
                    channel3MinField.getValue(),
                    channel3MaxField.getValue(),
                    threadsCountField.getValue()
                )
            ),
            new Button(
                "Вычислить среднее",
                e -> presenter.calculateAverage(
                    buffer.getInputStream(photoFileName),
                    splitTypeComboBox.getValue()
                )
            )
        );

        if (time != null) {
            resultSection.add(new Span("Время выполнения: " + time + " с"));
        }

        if (avgTime1 != null) {
            resultSection.add(new Span("Среднее время 1: " + avgTime1 + " с"));
        }

        if (avgTime2 != null) {
            resultSection.add(new Span("Среднее время 2: " + avgTime2 + " с"));
        }

        if (avgTime3 != null) {
            resultSection.add(new Span("Среднее время 3: " + avgTime3 + " с"));
        }

        if (avgTime4 != null) {
            resultSection.add(new Span("Среднее время 4: " + avgTime4 + " с"));
        }

        return resultSection;
    }
}
