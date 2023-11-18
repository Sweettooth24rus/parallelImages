package com.kkoz.parallels;

public class Presenter<V extends View> {

    protected final V view;

    public Presenter(V view) {
        this.view = view;
    }
}
