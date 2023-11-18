package com.kkoz.parallels;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;

public class View<P extends Presenter> extends VerticalLayout {

    protected final P presenter;

    public View(Class<P> presenterClass, Labs lab) {
        P p = null;
        try {
            p = (P) presenterClass.getConstructors()[0].newInstance(this);
        } catch (Exception e) {
            e.printStackTrace();
        }

        this.presenter = p;

        add(createLabsButtons(), createInLabButtons(lab));
    }

    private Component createLabsButtons() {
        var container = new HorizontalLayout();
        for (var lab : Labs.values()) {
            container.add(createAnchorButton(String.valueOf(lab.ordinal() + 1), lab.getUrl()));
        }
        return container;
    }

    private Component createInLabButtons(Labs lab) {
        var container = new HorizontalLayout();
        for (var pair : lab.getLabs()) {
            container.add(createAnchorButton(pair.getLeft(), pair.getRight()));
        }
        return container;
    }

    public Anchor createAnchorButton(String humanName, String href) {
        var button = new Button(humanName);
        var link = new Anchor(href, "");
        link.add(button);
        link.getStyle()
            .set("text-decoration", "none")
            .set("color", "transparent");
        return link;
    }
}
