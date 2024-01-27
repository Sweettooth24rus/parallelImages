package com.kkoz.parallels.lab_3.contour;

import com.kkoz.parallels.ContourType;
import com.kkoz.parallels.Labs;
import com.kkoz.parallels.View;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.HasComponents;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.component.upload.receivers.MultiFileMemoryBuffer;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouteAlias;
import com.vaadin.flow.server.StreamResource;
import org.apache.commons.lang3.StringUtils;

import java.io.InputStream;

@Route("/labs/3/contour")
@RouteAlias("/labs/3")
public class Lab3ContourView extends View<Lab3ContourPresenter> {
    private final MultiFileMemoryBuffer buffer = new MultiFileMemoryBuffer();
    private final HorizontalLayout imageSection = new HorizontalLayout();
    private final TextField threadsCountField = new TextField();
    private final ComboBox<ContourType> contourTypeComboBox = new ComboBox<>();
    private final TextField thresholdTextField = new TextField();
    private final TextField gainTextField = new TextField();
    private final HorizontalLayout resultSection = new HorizontalLayout();

    private String photoFileName;

    public Lab3ContourView() {
        super(Lab3ContourPresenter.class, Labs.LAB_3);

        threadsCountField.setLabel("Количество потоков");
        threadsCountField.setValue("1");

        add(
            createUploadPhotoSection(),
            imageSection,
            createContourTypeComboBoxSection(),
            createContourParametersSection(),
            createResultSection(null, null, null, null, null)
        );
    }

    private Component createUploadPhotoSection() {
        var upload = new Upload(buffer);

        upload.addSucceededListener(event -> {
            photoFileName = event.getFileName();
            refreshFilterPhotosSection(buffer.getInputStream(photoFileName));
            upload.clearFileList();
            presenter.contour(
                buffer.getInputStream(photoFileName),
                contourTypeComboBox.getValue(),
                thresholdTextField.getValue(),
                gainTextField.getValue(),
                threadsCountField.getValue()
            );
        });

        return upload;
    }

    private ComboBox<ContourType> createContourTypeComboBoxSection() {
        contourTypeComboBox.setItems(ContourType.values());
        contourTypeComboBox.setValue(ContourType.ROBERTS);
        contourTypeComboBox.setItemLabelGenerator(ContourType::getName);
        contourTypeComboBox.addValueChangeListener(event -> {
            if (StringUtils.isNotBlank(photoFileName)) {
                presenter.contour(
                    buffer.getInputStream(photoFileName),
                    event.getValue(),
                    thresholdTextField.getValue(),
                    gainTextField.getValue(),
                    threadsCountField.getValue()
                );
            }
        });
        return contourTypeComboBox;
    }

    private Component createContourParametersSection() {
        var result = new HorizontalLayout();
        thresholdTextField.setLabel("Порог");
        thresholdTextField.setValue("0");
        gainTextField.setLabel("Коэффициент усиления");
        gainTextField.setValue("1");
        result.add(
            thresholdTextField,
            gainTextField,
            new Button(
                "Применить",
                e -> presenter.contour(
                    buffer.getInputStream(photoFileName),
                    contourTypeComboBox.getValue(),
                    thresholdTextField.getValue(),
                    gainTextField.getValue(),
                    threadsCountField.getValue()
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

    public Component createResultSection(Double time, Double avgTime1, Double avgTime2, Double avgTime3, Double avgTime4) {
        resultSection.removeAll();

//        resultSection.add(
//            threadsCountField,
//            new Button(
//                "Применить",
//                e -> presenter.contour(
//                    buffer.getInputStream(photoFileName),
//                    contourTypeComboBox.getValue(),
//                    thresholdTextField.getValue(),
//                    gainTextField.getValue(),
//                    threadsCountField.getValue()
//                )
//            ),
//            new Button(
//                "Вычислить среднее",
//                e -> presenter.calculateAverage(
//                    buffer.getInputStream(photoFileName),
//                    contourTypeComboBox.getValue(),
//                    thresholdTextField.getValue(),
//                    gainTextField.getValue(),
//                    threadsCountField.getValue()
//                )
//            )
//        );

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
