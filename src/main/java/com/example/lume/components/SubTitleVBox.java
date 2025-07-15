package com.example.lume.components;

import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

public class SubTitleVBox extends VBox {

    private Label subTitleText = new Label("");

    public SubTitleVBox(double width, double height, String subTitleText) {
        // Set width, height, className, inner alignment of the VBox
        this.setPrefSize(width, height);
        this.getStyleClass().add("subtitle-pane");
        this.setAlignment(Pos.CENTER_LEFT);

        // Set label text, className
        this.subTitleText.setText(subTitleText);
        this.subTitleText.getStyleClass().add("subtitle-text");
        this.subTitleText.setStyle("""
                    -fx-font-weight: 700;
                    -fx-padding: 5px 0px;
                """);

        // Add label to the VBox
        this.getChildren().add(this.subTitleText);
    }

    public String getSubTitleText() {
        return subTitleText.getText();
    }
}
