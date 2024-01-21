package com.kkoz.parallels.lab_2.filters.spatial;

import com.kkoz.parallels.ChannelData;
import com.kkoz.parallels.Labs;
import com.kkoz.parallels.SplitType;
import com.kkoz.parallels.View;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.HasComponents;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.checkbox.Checkbox;
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
import java.util.List;

@Route("/labs/2/filters/spatial")
public class Lab2SpatialFilterView extends View<Lab2SpatialFilterPresenter> {
    private final MultiFileMemoryBuffer buffer = new MultiFileMemoryBuffer();
    private final HorizontalLayout imageSection = new HorizontalLayout();
    private final HorizontalLayout channelsSection = new HorizontalLayout();
    private final TextField matrixWidthTextField = new TextField();
    private final TextField matrixHeightTextField = new TextField();
    private final TextField coefTextField = new TextField();
    private final Checkbox channel1EnableCheckbox = new Checkbox();
    private final Checkbox channel2EnableCheckbox = new Checkbox();
    private final Checkbox channel3EnableCheckbox = new Checkbox();

    private String photoFileName;
    private ComboBox<SplitType> splitTypeComboBox;

    public Lab2SpatialFilterView() {
        super(Lab2SpatialFilterPresenter.class, Labs.LAB_2);

        coefTextField.setLabel("Коэффициент");
        coefTextField.setValue("1");
        matrixWidthTextField.setLabel("Ширина");
        matrixWidthTextField.setValue("3");
        matrixHeightTextField.setLabel("Высота");
        matrixHeightTextField.setValue("3");
        channel1EnableCheckbox.setLabel("Включить");
        channel1EnableCheckbox.setValue(true);
        channel2EnableCheckbox.setLabel("Включить");
        channel2EnableCheckbox.setValue(true);
        channel3EnableCheckbox.setLabel("Включить");
        channel3EnableCheckbox.setValue(true);

        add(
            createUploadPhotoSection(),
            createSplitTypeComboBox(),
            imageSection,
            matrixWidthTextField,
            matrixHeightTextField,
            coefTextField,
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
                matrixWidthTextField.getValue(),
                matrixHeightTextField.getValue(),
                coefTextField.getValue(),
                channel1EnableCheckbox.getValue(),
                channel2EnableCheckbox.getValue(),
                channel3EnableCheckbox.getValue()
            );
            upload.clearFileList();
        });

        return upload;
    }

    private ComboBox<SplitType> createSplitTypeComboBox() {
        splitTypeComboBox = new ComboBox<>();
        splitTypeComboBox.setItems(List.of(SplitType.RGB, SplitType.YUV));
        splitTypeComboBox.setValue(SplitType.RGB);
        splitTypeComboBox.addValueChangeListener(event -> {
            if (StringUtils.isNotBlank(photoFileName)) {
                presenter.splitImageToChannels(
                    buffer.getInputStream(photoFileName),
                    event.getValue(),
                    matrixWidthTextField.getValue(),
                    matrixHeightTextField.getValue(),
                    coefTextField.getValue(),
                    channel1EnableCheckbox.getValue(),
                    channel2EnableCheckbox.getValue(),
                    channel3EnableCheckbox.getValue()
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
        addToSection(channelsSection, firstChannel, "Первый канал", channel1EnableCheckbox);
        addToSection(channelsSection, secondChannel, "Второй канал", channel2EnableCheckbox);
        addToSection(channelsSection, thirdChannel, "Третий канал", channel3EnableCheckbox);
    }

    private void addToSection(HorizontalLayout section,
                              ChannelData data,
                              String name,
                              Checkbox channelEnableCheckbox) {
        var channel = new VerticalLayout();
        channel.setWidthFull();

        var submitButton = new Button("Применить");
        submitButton.addClickListener(e -> {
            if (StringUtils.isNotBlank(photoFileName)) {
                presenter.splitImageToChannels(
                    buffer.getInputStream(photoFileName),
                    splitTypeComboBox.getValue(),
                    matrixWidthTextField.getValue(),
                    matrixHeightTextField.getValue(),
                    coefTextField.getValue(),
                    channel1EnableCheckbox.getValue(),
                    channel2EnableCheckbox.getValue(),
                    channel3EnableCheckbox.getValue()
                );
            }
        });

        channel.add(
            new HorizontalLayout(
                channelEnableCheckbox,
                submitButton
            )
        );

        addPhotoToSection(channel, data.getImageStream(), name);

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
}
