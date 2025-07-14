package com.example.lume.components;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ContentDisplay;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.paint.Color;
import javafx.scene.shape.SVGPath;

public class SidePanelOption extends HBox {
    private SVGPath iconSVG =  new SVGPath();
    private Button iconButton;

    public SidePanelOption(double width, double height, String svgPath, String text) {
        // Set option width, height, className, alignment and padding
        this.setMaxSize(width, height);
        this.getStyleClass().add("option");
        this.setAlignment(Pos.CENTER_LEFT);
        this.setPadding(new Insets(10, 10, 10, 20));

        // Set Option Button's svg icon path and className
        this.iconSVG.setContent(svgPath);
        iconSVG.getStyleClass().add("option-icon");

        // Set Button's label with icon, className and add padding
        this.iconButton = new Button(text, iconSVG);
        iconButton.setContentDisplay(ContentDisplay.LEFT);
        iconButton.getStyleClass().add("option-btn");
        iconButton.setPadding(new Insets(10, 0, 10, 15));

        // Button text color and width, height
        iconButton.setTextFill(Color.rgb(250, 250, 250));
        iconButton.setMaxWidth(Double.MAX_VALUE);
        iconButton.setMaxHeight(Double.MAX_VALUE);

        // Align Button content to left
        iconButton.setAlignment(Pos.CENTER_LEFT);

        // Add the iconButton as child of the HBox and make its priority to always grow
        this.getChildren().add(iconButton);
        HBox.setHgrow(iconButton, Priority.ALWAYS);
    }

    public Button getIconButton() {
        return iconButton;
    }

    public SVGPath getSVGPath() {
        return iconSVG;
    }

    public void setButtonText(String text) { iconButton.setText(text); }

    public void setSVGPath(String svgPath) {
        iconSVG.setContent(svgPath);
    }
}