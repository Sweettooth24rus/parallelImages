package com.kkoz.parallels;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.component.upload.receivers.MultiFileMemoryBuffer;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.StreamResource;

import java.io.InputStream;

@Route("/labs/1")
public class Lab1View extends VerticalLayout {
    private final Lab1Presenter presenter;

    private HorizontalLayout sourceImageSection = new HorizontalLayout();
    private HorizontalLayout channelsImageSection = new HorizontalLayout();

    Lab1View() {
        presenter = new Lab1Presenter(this);

        add(
            createUploadPhotoSection(),
            sourceImageSection,
            channelsImageSection
        );
    }

    private Component createUploadPhotoSection() {
        var buffer = new MultiFileMemoryBuffer();
        var upload = new Upload(buffer);

        upload.addSucceededListener(event -> {
            presenter.onPhotoUploaded(event, buffer);
            upload.clearFileList();
        });

        return upload;
    }

    private void addPhotoToSection(HorizontalLayout imageSection, InputStream imageStream, String name) {
        var image = new Image(
            new StreamResource(name, () -> imageStream),
            ""
        );
        image.setWidthFull();
        imageSection.add(image);
    }

    public void refreshSourcePhotoSection(InputStream sourceImageStream) {
        sourceImageSection.removeAll();
        addPhotoToSection(sourceImageSection, sourceImageStream, "Исходное фото");
    }

    public void refreshChannelsPhotoSection(InputStream firstChannel,
                                            InputStream secondChannel,
                                            InputStream thirdChannel) {
        channelsImageSection.removeAll();
        addPhotoToSection(channelsImageSection, firstChannel, "Первый канал");
        addPhotoToSection(channelsImageSection, secondChannel, "Второй канал");
        addPhotoToSection(channelsImageSection, thirdChannel, "Третий канал");
    }
}
