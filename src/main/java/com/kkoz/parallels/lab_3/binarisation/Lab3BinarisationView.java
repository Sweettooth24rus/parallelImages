package com.kkoz.parallels.lab_3.binarisation;

import com.kkoz.parallels.BinarisationType;
import com.kkoz.parallels.Labs;
import com.kkoz.parallels.View;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.HasComponents;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.component.upload.receivers.MultiFileMemoryBuffer;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.StreamResource;
import org.apache.commons.lang3.StringUtils;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

@Route("/labs/3/binarisation")
public class Lab3BinarisationView extends View<Lab3BinarisationPresenter> {
    private final MultiFileMemoryBuffer buffer = new MultiFileMemoryBuffer();
    private final HorizontalLayout imageSection = new HorizontalLayout();
    private final TextField thresholdTextField = new TextField();
    private final TextField bradleyAreaTextField = new TextField();
    private final VerticalLayout dilationErosionMatrixSection = new VerticalLayout();
    private final TextField dilationErosionWidthTextField = new TextField();
    private final TextField dilationErosionHeightTextField = new TextField();
    private final List<List<TextField>> dilationErosionCoefficients = new ArrayList<>();

    private BufferedImage filteredBufferedImage;
    private String photoFileName;

    public Lab3BinarisationView() {
        super(Lab3BinarisationPresenter.class, Labs.LAB_3);

        add(
            createUploadPhotoSection(),
            imageSection,
            createBinarisationTypeSection(),
            createDilationErosionSection()
        );
    }

    private Component createUploadPhotoSection() {
        var upload = new Upload(buffer);

        upload.addSucceededListener(event -> {
            photoFileName = event.getFileName();
            try {
                refreshFilterPhotosSection(ImageIO.read(buffer.getInputStream(photoFileName)), buffer.getInputStream(photoFileName));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
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

    private Component createDilationErosionSection() {
        dilationErosionWidthTextField.setLabel("Ширина");
        dilationErosionWidthTextField.setValue("3");
        dilationErosionWidthTextField.addValueChangeListener(e -> {
            if (e.isFromClient()) {
                updateDilationErosionMatrixSection(false);
            }
        });

        dilationErosionHeightTextField.setLabel("Высота");
        dilationErosionHeightTextField.setValue("3");
        dilationErosionHeightTextField.addValueChangeListener(e -> {
            if (e.isFromClient()) {
                updateDilationErosionMatrixSection(false);
            }
        });

        dilationErosionCoefficients.addAll(
            List.of(
                List.of(
                    new TextField("", "0", ""),
                    new TextField("", "1", ""),
                    new TextField("", "0", "")
                ),
                List.of(
                    new TextField("", "1", ""),
                    new TextField("", "2", ""),
                    new TextField("", "1", "")
                ),
                List.of(
                    new TextField("", "0", ""),
                    new TextField("", "1", ""),
                    new TextField("", "0", "")
                )
            )
        );

        for (var dilationErosionRow : dilationErosionCoefficients) {
            var matrixRow = new HorizontalLayout();
            for (var dilationErosionCell : dilationErosionRow) {
                matrixRow.add(dilationErosionCell);
            }
            dilationErosionMatrixSection.add(matrixRow);
        }

        return new HorizontalLayout(
            dilationErosionWidthTextField,
            dilationErosionHeightTextField,
            dilationErosionMatrixSection,
            new Button(
                "Расширить",
                e -> presenter.dilation(
                    filteredBufferedImage,
                    dilationErosionCoefficients.stream().map(row -> row.stream().map(TextField::getValue).toList()).toList()
                )
            ),
            new Button(
                "Расширить быстро",
                e -> presenter.dilationFast(
                    filteredBufferedImage,
                    dilationErosionCoefficients.stream().map(row -> row.stream().map(TextField::getValue).toList()).toList()
                )
            ),
            new Button(
                "Сузить",
                e -> presenter.erosion(
                    filteredBufferedImage,
                    dilationErosionCoefficients.stream().map(row -> row.stream().map(TextField::getValue).toList()).toList()
                )
            ),
            new Button(
                "Сузить быстро",
                e -> presenter.erosionFast(
                    filteredBufferedImage,
                    dilationErosionCoefficients.stream().map(row -> row.stream().map(TextField::getValue).toList()).toList()
                )
            ),
            new Button(
                "Базовая маска",
                e -> {
                    dilationErosionCoefficients.clear();
                    dilationErosionCoefficients.addAll(
                        List.of(
                            List.of(
                                new TextField("", "0", ""),
                                new TextField("", "1", ""),
                                new TextField("", "0", "")
                            ),
                            List.of(
                                new TextField("", "1", ""),
                                new TextField("", "2", ""),
                                new TextField("", "1", "")
                            ),
                            List.of(
                                new TextField("", "0", ""),
                                new TextField("", "1", ""),
                                new TextField("", "0", "")
                            )
                        )
                    );
                    updateDilationErosionMatrixSection(true);
                }
            )
        );
    }

    private void updateDilationErosionMatrixSection(boolean preset) {
        if (preset) {
            dilationErosionWidthTextField.setValue("3");
            dilationErosionHeightTextField.setValue("3");
        } else {
            dilationErosionCoefficients.clear();
            for (var i = 0; i < Integer.parseInt(dilationErosionHeightTextField.getValue()); i++) {
                dilationErosionCoefficients.add(new ArrayList<>());
                for (var j = 0; j < Integer.parseInt(dilationErosionWidthTextField.getValue()); j++) {
                    dilationErosionCoefficients.get(i).add(new TextField("", "0", ""));
                }
            }
        }

        dilationErosionMatrixSection.removeAll();
        for (var dilationErosionRow : dilationErosionCoefficients) {
            var matrixRow = new HorizontalLayout();
            for (var dilationErosionCell : dilationErosionRow) {
                matrixRow.add(dilationErosionCell);
            }
            dilationErosionMatrixSection.add(matrixRow);
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

    public void refreshFilterPhotosSection(BufferedImage filteredBufferedImage, InputStream imageStream) {
        this.filteredBufferedImage = filteredBufferedImage;
        imageSection.removeAll();
        addPhotoToSection(imageSection, buffer.getInputStream(photoFileName), "До фильтра");
        addPhotoToSection(imageSection, imageStream, "После фильтра");
    }

    public void refreshFilterPhotosSection(InputStream sourceImageStream, BufferedImage filteredBufferedImage, InputStream imageStream) {
        this.filteredBufferedImage = filteredBufferedImage;
        imageSection.removeAll();
        addPhotoToSection(imageSection, sourceImageStream, "До фильтра");
        addPhotoToSection(imageSection, imageStream, "После фильтра");
    }
}
