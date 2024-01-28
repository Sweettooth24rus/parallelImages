package com.kkoz.parallels.lab_3.binarisation;

import com.kkoz.parallels.BinarisationType;
import com.kkoz.parallels.Labs;
import com.kkoz.parallels.View;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.HasComponents;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.component.upload.receivers.MultiFileMemoryBuffer;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.StreamResource;
import org.apache.commons.lang3.StringUtils;

import java.io.InputStream;

@Route("/labs/3/binarisation")
public class Lab3BinarisationView extends View<Lab3BinarisationPresenter> {
    private final MultiFileMemoryBuffer buffer = new MultiFileMemoryBuffer();
    private final HorizontalLayout imageSection = new HorizontalLayout();
    private final TextField thresholdTextField = new TextField();
    private final TextField bradleyAreaTextField = new TextField();

    private String photoFileName;

    public Lab3BinarisationView() {
        super(Lab3BinarisationPresenter.class, Labs.LAB_3);

        add(
            createUploadPhotoSection(),
            imageSection,
            createBinarisationTypeSection()
        );
    }

    private Component createUploadPhotoSection() {
        var upload = new Upload(buffer);

        upload.addSucceededListener(event -> {
            photoFileName = event.getFileName();
            refreshFilterPhotosSection(buffer.getInputStream(photoFileName));
            upload.clearFileList();
        });

        return upload;
    }

    private Component createBinarisationTypeSection() {
        var result = new HorizontalLayout();

        thresholdTextField.setLabel("Порог");
        bradleyAreaTextField.setLabel("Количество областей по ширине");
        bradleyAreaTextField.setValue("8");

        result.add(
            thresholdTextField,
            new Button(
                "Применить глобальный порог",
                e -> presenter.binarise(
                    buffer.getInputStream(photoFileName),
                    StringUtils.isBlank(thresholdTextField.getValue()) ? BinarisationType.AVERAGE : null,
                    thresholdTextField.getValue(),
                    bradleyAreaTextField.getValue()
                )
            ),
            bradleyAreaTextField,
            new Button(
                "Применить локальный порог",
                e -> presenter.binarise(
                    buffer.getInputStream(photoFileName),
                    BinarisationType.BRADLEY,
                    thresholdTextField.getValue(),
                    bradleyAreaTextField.getValue()
                )
            )
        );

        return result;
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
