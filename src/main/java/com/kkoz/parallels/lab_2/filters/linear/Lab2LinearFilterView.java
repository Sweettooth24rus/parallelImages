package com.kkoz.parallels.lab_2.filters.linear;

import com.kkoz.parallels.Labs;
import com.kkoz.parallels.View;
import com.vaadin.flow.component.AbstractField;
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
import com.vaadin.flow.router.RouteAlias;
import com.vaadin.flow.server.StreamResource;
import org.apache.commons.lang3.StringUtils;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

@Route("/labs/2/filters/linear")
@RouteAlias("/labs/2/filters")
public class Lab2LinearFilterView extends View<Lab2LinearFilterPresenter> {
    private final MultiFileMemoryBuffer buffer = new MultiFileMemoryBuffer();
    private final HorizontalLayout imageSection = new HorizontalLayout();
    private final TextField matrixSizeTextField = new TextField();
    private final VerticalLayout matrixSection = new VerticalLayout();
    private final TextField threadsCountField = new TextField();
    private final HorizontalLayout resultSection = new HorizontalLayout();

    private String photoFileName;
    private List<List<TextField>> matrixCoefficients = List.of();

    public Lab2LinearFilterView() {
        super(Lab2LinearFilterPresenter.class, Labs.LAB_2);

        threadsCountField.setLabel("Количество потоков");
        threadsCountField.setValue("1");

        add(
            createUploadPhotoSection(),
            imageSection,
            createMatrixSection(),
            createResultSection(null, null, null, null, null)
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

    private Component createMatrixSection() {
        var section = new VerticalLayout();
        section.setWidthFull();

        matrixSizeTextField.addValueChangeListener(this::changeMatrix);
        matrixSizeTextField.setValue("3");

        var submitButton = new Button("Применить");
        submitButton.addClickListener(e -> {
            if (StringUtils.isNotBlank(photoFileName)) {
                presenter.filter(
                    buffer.getInputStream(photoFileName),
                    matrixSizeTextField.getValue(),
                    matrixCoefficients.stream().map(list -> list.stream().map(TextField::getValue).toList()).toList()
                );
            }
        });

        section.add(
            matrixSizeTextField,
            matrixSection,
            submitButton
        );

        return section;
    }

    private void changeMatrix(AbstractField.ComponentValueChangeEvent<TextField, String> textFieldStringComponentValueChangeEvent) {
        var size = Integer.parseInt(matrixSizeTextField.getValue());
        matrixCoefficients = new ArrayList<>(size);
        for (var i = 0; i < size; i++) {
            var matrixLine = new ArrayList<TextField>(size);
            for (var j = 0; j < size; j++) {
                matrixLine.add(new TextField("", "1", ""));
            }
            matrixCoefficients.add(matrixLine);
        }
        matrixSection.removeAll();
        for (var i = 0; i < size; i++) {
            var matrixLine = new HorizontalLayout();
            for (var j = 0; j < size; j++) {
                matrixLine.add(matrixCoefficients.get(i).get(j));
            }
            matrixSection.add(matrixLine);
        }
    }

    private void addPhotoToSection(HasComponents section, InputStream imageStream, String name) {
        var image = new Image(
            new StreamResource(name, () -> imageStream),
            name
        );
        image.setWidthFull();
        section.add(image);
    }

    public Component createResultSection(Double time, Double avgTime1, Double avgTime2, Double avgTime3, Double avgTime4) {
        resultSection.removeAll();

        resultSection.add(
            threadsCountField
//            new Button(
//                "Применить",
//                e -> presenter.makeGray(
//                    buffer.getInputStream(photoFileName),
//                    threadsCountField.getValue()
//                )
//            ),
//            new Button(
//                "Вычислить среднее",
//                e -> presenter.calculateAverage(
//                    buffer.getInputStream(photoFileName)
//                )
//            )
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

    public void refreshFilterPhotosSection(InputStream imageStream) {
        imageSection.removeAll();
        addPhotoToSection(imageSection, buffer.getInputStream(photoFileName), "До фильтра");
        addPhotoToSection(imageSection, imageStream, "После фильтра");
    }
}
