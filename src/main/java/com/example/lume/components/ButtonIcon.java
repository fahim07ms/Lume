package com.example.lume.components;

import javafx.scene.control.Button;
import javafx.scene.control.ContentDisplay;
import javafx.scene.paint.Color;
import javafx.scene.shape.SVGPath;

public class ButtonIcon extends Button {
    private SVGPath svgIcon;
    public ButtonIcon(String svgPath) {
        // Set SVG Path
        this.svgIcon = new SVGPath();
        this.svgIcon.setContent(svgPath);

        // Set svg's color and scale it
        this.svgIcon.setFill(Color.rgb(250, 250, 250));
        this.svgIcon.setScaleX(1.4);
        this.svgIcon.setScaleY(1.4);

        // Set Button height and width
        this.setMaxSize(30, 30);

        // Add svg to button
        this.setGraphic(svgIcon);
        this.setContentDisplay(ContentDisplay.CENTER);

        // Button className
        this.getStyleClass().add("button-icon");
    }

    void setSvgIcon(String svgPath) {
        this.svgIcon.setContent(svgPath);
    }
}
