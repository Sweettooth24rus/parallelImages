package com.kkoz.parallels.lab_3.segmentation.quatro_tree;

import com.kkoz.parallels.Labs;
import com.kkoz.parallels.View;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.HasComponents;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.component.upload.receivers.MultiFileMemoryBuffer;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.StreamResource;
import lombok.Getter;

import java.io.InputStream;

@Route("/labs/3/quatro_tree")
public class Lab3QuatroTreeView extends View<Lab3QuatroTreePresenter> {
    private final MultiFileMemoryBuffer buffer = new MultiFileMemoryBuffer();
    private final HorizontalLayout imageSection = new HorizontalLayout();
    @Getter
    private final TextField segmentsCount = new TextField();

    private String photoFileName;

    public Lab3QuatroTreeView() {
        super(Lab3QuatroTreePresenter.class, Labs.LAB_3);

        segmentsCount.setLabel("Количество сегментов");
        segmentsCount.setReadOnly(true);

        add(
            createUploadPhotoSection(),
            imageSection,
            segmentsCount
        );
    }

    private Component createUploadPhotoSection() {
        var upload = new Upload(buffer);

        upload.addSucceededListener(event -> {
            photoFileName = event.getFileName();
            refreshFilterPhotosSection(buffer.getInputStream(photoFileName));
            upload.clearFileList();
            presenter.segment(
                buffer.getInputStream(photoFileName)
            );
        });

        return upload;
    }

    private void addPhotoToSection(HasComponents section, InputStream imageStream, String name) {
        var image = new Image(
            new StreamResource(name, () -> imageStream),
            name
        );
        image.setWidthFull();
        section.add(image);
    }

    public void refreshFilterPhotosSection(InputStream imageStream) {
        imageSection.removeAll();
        addPhotoToSection(imageSection, buffer.getInputStream(photoFileName), "До фильтра");
        addPhotoToSection(imageSection, imageStream, "После фильтра");
    }
}
