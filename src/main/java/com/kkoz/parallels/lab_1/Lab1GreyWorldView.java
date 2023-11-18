package com.kkoz.parallels.lab_1;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.HasComponents;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.component.upload.receivers.MultiFileMemoryBuffer;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.StreamResource;

import java.io.InputStream;

@Route("/labs/1/grey_world")
public class Lab1GreyWorldView extends VerticalLayout {
    private final Lab1GreyWorldPresenter presenter;
    private final MultiFileMemoryBuffer buffer = new MultiFileMemoryBuffer();
    private final HorizontalLayout imageSection = new HorizontalLayout();
    private final HorizontalLayout resultSection = new HorizontalLayout();
    private final TextField threadsCountField = new TextField();

    private String photoFileName;

    Lab1GreyWorldView() {
        presenter = new Lab1GreyWorldPresenter(this);

        threadsCountField.setLabel("Количество потоков");
        threadsCountField.setValue("1");

        add(createUploadPhotoSection(), createResultSection(null, null, null, null, null), imageSection);
    }

    private Component createUploadPhotoSection() {
        var upload = new Upload(buffer);

        upload.addSucceededListener(event -> {
            photoFileName = event.getFileName();
            presenter.makeGray(buffer.getInputStream(photoFileName), threadsCountField.getValue());
            upload.clearFileList();
        });

        return upload;
    }

    public Component createResultSection(Double time, Double avgTime1, Double avgTime2, Double avgTime3, Double avgTime4) {
        resultSection.removeAll();

        resultSection.add(
            threadsCountField,
            new Button(
                "Применить",
                e -> presenter.makeGray(
                    buffer.getInputStream(photoFileName),
                    threadsCountField.getValue()
                )
            ),
            new Button(
                "Вычислить среднее",
                e -> presenter.calculateAverage(
                    buffer.getInputStream(photoFileName)
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
