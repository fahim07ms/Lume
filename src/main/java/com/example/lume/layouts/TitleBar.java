package com.example.lume.layouts;

import com.example.lume.components.ButtonIcon;
import com.example.lume.scenes.LibraryLayout;
import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.shape.Path;
import javafx.scene.shape.SVGPath;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;

public class TitleBar extends BorderPane {

    private ButtonIcon closeBtnIcon;
    private ButtonIcon minimizeBtnIcon;
    private ButtonIcon leftBtn;
    private Label titleBarText;

    static final String CLOSE_BTN_SVG_PATH = "M4.646 4.646a.5.5 0 0 1 .708 0L8 7.293l2.646-2.647a.5.5 0 0 1 .708.708L8.707 8l2.647 2.646a.5.5 0 0 1-.708.708L8 8.707l-2.646 2.647a.5.5 0 0 1-.708-.708L7.293 8 4.646 5.354a.5.5 0 0 1 0-.708";
    static final String MINIMIZE_BTN_SVG_PATH = "M4 8a.5.5 0 0 1 .5-.5h7a.5.5 0 0 1 0 1h-7A.5.5 0 0 1 4 8";

    public TitleBar(double width, double height, Stage stage) {
        // Set className, padding and width, height for the title bar
        this.getStyleClass().add("title-bar");
        this.setPadding(new Insets(10, 15, 10, 15));
        this.setMaxSize(width, height);

        // Set Button Icons SVG Paths
        closeBtnIcon = new ButtonIcon(CLOSE_BTN_SVG_PATH);
        minimizeBtnIcon = new ButtonIcon(MINIMIZE_BTN_SVG_PATH);

        // Set tooltips for buttons
        closeBtnIcon.setTooltip(new Tooltip("Close"));
        minimizeBtnIcon.setTooltip(new Tooltip("Minimize"));

        // Create deck of the close and minimize button
        // Place them at right of the title bar
        HBox btnDeck = new HBox();
        btnDeck.getChildren().addAll(minimizeBtnIcon, closeBtnIcon);
        btnDeck.setSpacing(15);
        this.setRight(btnDeck);

        // Set minimize and close button actions
        minimizeBtnIcon.setOnAction(e -> stage.setIconified(true));
        closeBtnIcon.setOnAction(e -> {
            ObjectMapper objectMapper = new ObjectMapper();
            try {
                objectMapper.writeValue(new File(System.getProperty("user.home") + "/.lume/metadata.json"), LibraryLayout.lumeMetadata);
            } catch (IOException ex) {
                System.out.println(ex.getMessage());
            }

            stage.close();
        });

        // Set title, className and alignment for the title bar label
        this.titleBarText = new Label();
//        this.titleBarText.getStyleClass().add("title-bar-text");
        this.titleBarText.setAlignment(Pos.CENTER);
        this.titleBarText.setStyle("""
                    -fx-font-size: 20px;
                    -fx-font-weight: bolder;
                """);

        // Set the title bar label at the center of the border pane
        this.setCenter(this.titleBarText);
    }

    public void setLeftBtn(String svgPath, String tooltipText) {
        this.leftBtn = new ButtonIcon(svgPath);
        this.leftBtn.setTooltip(new Tooltip(tooltipText));
        this.setLeft(leftBtn);
    }

    public ButtonIcon getLeftBtn() {
        return leftBtn;
    }

    public void setTitleBarText(String titleBarText) {
        this.titleBarText.setText(titleBarText);
    }
}
