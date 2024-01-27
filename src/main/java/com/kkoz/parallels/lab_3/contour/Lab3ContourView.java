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
    private final HorizontalLayout sobelCoefficientsSection = new HorizontalLayout();
    private final List<List<TextField>> sobelCoefficientsX = new ArrayList<>();
    private final List<List<TextField>> sobelCoefficientsY = new ArrayList<>();
    private final HorizontalLayout laplasCoefficientsSection = new HorizontalLayout();
    private final VerticalLayout laplasMatrixSection = new VerticalLayout();
    private final TextField laplasWidthTextField = new TextField();
    private final TextField laplasHeightTextField = new TextField();
    private final List<List<TextField>> laplasCoefficients = new ArrayList<>();

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
            createSobelCoefficientsSection(),
            createLaplasCoefficientsSection(),
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
                sobelCoefficientsX.stream().map(row -> row.stream().map(TextField::getValue).toList()).toList(),
                sobelCoefficientsY.stream().map(row -> row.stream().map(TextField::getValue).toList()).toList(),
                laplasCoefficients.stream().map(row -> row.stream().map(TextField::getValue).toList()).toList(),
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
                    sobelCoefficientsX.stream().map(row -> row.stream().map(TextField::getValue).toList()).toList(),
                    sobelCoefficientsY.stream().map(row -> row.stream().map(TextField::getValue).toList()).toList(),
                    laplasCoefficients.stream().map(row -> row.stream().map(TextField::getValue).toList()).toList(),
                    threadsCountField.getValue()
                );
            }
            sobelCoefficientsSection.setVisible(event.getValue() == ContourType.SOBEL);
            laplasCoefficientsSection.setVisible(event.getValue() == ContourType.LAPLAS);
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
                    sobelCoefficientsX.stream().map(row -> row.stream().map(TextField::getValue).toList()).toList(),
                    sobelCoefficientsY.stream().map(row -> row.stream().map(TextField::getValue).toList()).toList(),
                    laplasCoefficients.stream().map(row -> row.stream().map(TextField::getValue).toList()).toList(),
                    threadsCountField.getValue()
                )
            )
        );
        return result;
    }

    private Component createSobelCoefficientsSection() {
        sobelCoefficientsX.addAll(
            List.of(
                List.of(
                    new TextField("", "1", ""),
                    new TextField("", "0", ""),
                    new TextField("", "-1", "")
                ),
                List.of(
                    new TextField("", "2", ""),
                    new TextField("", "0", ""),
                    new TextField("", "-2", "")
                ),
                List.of(
                    new TextField("", "1", ""),
                    new TextField("", "0", ""),
                    new TextField("", "-1", "")
                )
            )
        );

        var matrixSectionX = new VerticalLayout();
        for (var sobelRow : sobelCoefficientsX) {
            var matrixRow = new HorizontalLayout();
            for (var sobelCell : sobelRow) {
                matrixRow.add(sobelCell);
            }
            matrixSectionX.add(matrixRow);
        }

        sobelCoefficientsY.addAll(
            List.of(
                List.of(
                    new TextField("", "2", ""),
                    new TextField("", "1", ""),
                    new TextField("", "0", "")
                ),
                List.of(
                    new TextField("", "1", ""),
                    new TextField("", "0", ""),
                    new TextField("", "-1", "")
                ),
                List.of(
                    new TextField("", "0", ""),
                    new TextField("", "-1", ""),
                    new TextField("", "-2", "")
                )
            )
        );

        var matrixSectionY = new VerticalLayout();
        for (var sobelRow : sobelCoefficientsY) {
            var matrixRow = new HorizontalLayout();
            for (var sobelCell : sobelRow) {
                matrixRow.add(sobelCell);
            }
            matrixSectionY.add(matrixRow);
        }

        sobelCoefficientsSection.add(
            matrixSectionX,
            matrixSectionY,
            new Button(
                "Применить",
                e -> presenter.contour(
                    buffer.getInputStream(photoFileName),
                    contourTypeComboBox.getValue(),
                    thresholdTextField.getValue(),
                    gainTextField.getValue(),
                    sobelCoefficientsX.stream().map(row -> row.stream().map(TextField::getValue).toList()).toList(),
                    sobelCoefficientsY.stream().map(row -> row.stream().map(TextField::getValue).toList()).toList(),
                    laplasCoefficients.stream().map(row -> row.stream().map(TextField::getValue).toList()).toList(),
                    threadsCountField.getValue()
                )
            )
        );
        sobelCoefficientsSection.setVisible(false);
        return sobelCoefficientsSection;
    }

    private Component createLaplasCoefficientsSection() {
        laplasWidthTextField.setLabel("Ширина");
        laplasWidthTextField.setValue("3");
        laplasWidthTextField.addValueChangeListener(e -> {
            if (e.isFromClient()) {
                updateLaplasMatrixSection(false);
            }
        });

        laplasHeightTextField.setLabel("Высота");
        laplasHeightTextField.setValue("3");
        laplasHeightTextField.addValueChangeListener(e -> {
            if (e.isFromClient()) {
                updateLaplasMatrixSection(false);
            }
        });

        laplasCoefficients.addAll(
            List.of(
                List.of(
                    new TextField("", "0", ""),
                    new TextField("", "-1", ""),
                    new TextField("", "0", "")
                ),
                List.of(
                    new TextField("", "-1", ""),
                    new TextField("", "4", ""),
                    new TextField("", "-1", "")
                ),
                List.of(
                    new TextField("", "0", ""),
                    new TextField("", "-1", ""),
                    new TextField("", "0", "")
                )
            )
        );

        for (var laplasRow : laplasCoefficients) {
            var matrixRow = new HorizontalLayout();
            for (var laplasCell : laplasRow) {
                matrixRow.add(laplasCell);
            }
            laplasMatrixSection.add(matrixRow);
        }

        laplasCoefficientsSection.add(
            laplasWidthTextField,
            laplasHeightTextField,
            laplasMatrixSection,
            new Button(
                "Применить",
                e -> presenter.contour(
                    buffer.getInputStream(photoFileName),
                    contourTypeComboBox.getValue(),
                    thresholdTextField.getValue(),
                    gainTextField.getValue(),
                    sobelCoefficientsX.stream().map(row -> row.stream().map(TextField::getValue).toList()).toList(),
                    sobelCoefficientsY.stream().map(row -> row.stream().map(TextField::getValue).toList()).toList(),
                    laplasCoefficients.stream().map(row -> row.stream().map(TextField::getValue).toList()).toList(),
                    threadsCountField.getValue()
                )
            ),
            new Button(
                "Для положительного ядра",
                e -> {
                    laplasCoefficients.clear();
                    laplasCoefficients.addAll(
                        List.of(
                            List.of(
                                new TextField("", "0", ""),
                                new TextField("", "-1", ""),
                                new TextField("", "0", "")
                            ),
                            List.of(
                                new TextField("", "-1", ""),
                                new TextField("", "4", ""),
                                new TextField("", "-1", "")
                            ),
                            List.of(
                                new TextField("", "0", ""),
                                new TextField("", "-1", ""),
                                new TextField("", "0", "")
                            )
                        )
                    );
                    updateLaplasMatrixSection(true);
                }
            ),
            new Button(
                "Для отрицательного ядра",
                e -> {
                    laplasCoefficients.clear();
                    laplasCoefficients.addAll(
                        List.of(
                            List.of(
                                new TextField("", "0", ""),
                                new TextField("", "1", ""),
                                new TextField("", "0", "")
                            ),
                            List.of(
                                new TextField("", "1", ""),
                                new TextField("", "-4", ""),
                                new TextField("", "1", "")
                            ),
                            List.of(
                                new TextField("", "0", ""),
                                new TextField("", "1", ""),
                                new TextField("", "0", "")
                            )
                        )
                    );
                    updateLaplasMatrixSection(true);
                }
            )
        );
        laplasCoefficientsSection.setVisible(false);
        return laplasCoefficientsSection;
    }

    private void updateLaplasMatrixSection(boolean preset) {
        if (preset) {
            laplasWidthTextField.setValue("3");
            laplasHeightTextField.setValue("3");
        } else {
            laplasCoefficients.clear();
            for (var i = 0; i < Integer.parseInt(laplasHeightTextField.getValue()); i++) {
                laplasCoefficients.add(new ArrayList<>());
                for (var j = 0; j < Integer.parseInt(laplasWidthTextField.getValue()); j++) {
                    laplasCoefficients.get(i).add(new TextField("", "0", ""));
                }
            }
        }

        laplasMatrixSection.removeAll();
        for (var laplasRow : laplasCoefficients) {
            var matrixRow = new HorizontalLayout();
            for (var laplasCell : laplasRow) {
                matrixRow.add(laplasCell);
            }
            laplasMatrixSection.add(matrixRow);
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
            threadsCountField,
            new Button(
                "Применить",
                e -> presenter.contour(
                    buffer.getInputStream(photoFileName),
                    contourTypeComboBox.getValue(),
                    thresholdTextField.getValue(),
                    gainTextField.getValue(),
                    sobelCoefficientsX.stream().map(row -> row.stream().map(TextField::getValue).toList()).toList(),
                    sobelCoefficientsY.stream().map(row -> row.stream().map(TextField::getValue).toList()).toList(),
                    laplasCoefficients.stream().map(row -> row.stream().map(TextField::getValue).toList()).toList(),
                    threadsCountField.getValue()
                )
            ),
            new Button(
                "Вычислить среднее",
                e -> presenter.calculateAverage(
                    buffer.getInputStream(photoFileName),
                    contourTypeComboBox.getValue(),
                    thresholdTextField.getValue(),
                    gainTextField.getValue(),
                    sobelCoefficientsX.stream().map(row -> row.stream().map(TextField::getValue).toList()).toList(),
                    sobelCoefficientsY.stream().map(row -> row.stream().map(TextField::getValue).toList()).toList(),
                    laplasCoefficients.stream().map(row -> row.stream().map(TextField::getValue).toList()).toList()
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

    public void refreshFilterPhotosSection(InputStream imageStream) {
        imageSection.removeAll();
        addPhotoToSection(imageSection, buffer.getInputStream(photoFileName), "До фильтра");
        addPhotoToSection(imageSection, imageStream, "После фильтра");
    }
}
