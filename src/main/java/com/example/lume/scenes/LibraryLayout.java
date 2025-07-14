package com.example.lume.scenes;

import com.example.lume.components.SidePanelOption;
import com.example.lume.components.SubTitleVBox;
import com.example.lume.layouts.BaseLayout;
import com.example.lume.layouts.TitleBar;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

public class LibraryLayout extends BaseLayout {

    private final String allBooksSVGPath = "M9 3v15h3V3zm3 2l4 13l3-1l-4-13zM5 5v13h3V5zM3 19v2h18v-2z";
    private final String catalogSVGPath = "m12 14l-2-2l2-2l2 2zM9.875 8.125l-2.5-2.5L12 1l4.625 4.625l-2.5 2.5L12 6zm-4.25 8.5L1 12l4.625-4.625l2.5 2.5L6 12l2.125 2.125zm12.75 0l-2.5-2.5L18 12l-2.125-2.125l2.5-2.5L23 12zM12 23l-4.625-4.625l2.5-2.5L12 18l2.125-2.125l2.5 2.5z";
    public Label titleText = new Label("Lume");
    public SubTitleVBox subTitleLibrary = new SubTitleVBox(400, 40, "Library");
    public SidePanelOption optionAllBooks = new SidePanelOption(
            400,
            50,
            allBooksSVGPath,
            "All Books");
    public SubTitleVBox subTitleCatalog = new SubTitleVBox(400, 40, "Catalog");

    public LibraryLayout(Stage stage) {
        super();

        // Title of Left side panel
        titleText.getStyleClass().add("title-text");
        titleText.setPadding(new Insets(0, 0, 20, 0));

        // Sub Title VBox - Library
        leftSidePanel.getChildren().addAll(titleText, subTitleLibrary, optionAllBooks);

        // Sub Title Component - Catalog
        subTitleCatalog.setPadding(new Insets(30, 0, 10, 0));

        // Add Menu Option
        SidePanelOption optionGutenberg = new SidePanelOption(400, 50, catalogSVGPath, "Gutenberg");
        SidePanelOption optionStandardEbooks = new SidePanelOption(400, 50, catalogSVGPath, "Standard Ebooks");
        SidePanelOption optionFeedBooks = new SidePanelOption(400, 50, catalogSVGPath, "Feedbooks");

        leftSidePanel.getChildren().addAll(subTitleCatalog, optionGutenberg, optionFeedBooks, optionStandardEbooks);

        TitleBar titleBar = new TitleBar(this.getRightSidePanelWidth(), 100, stage);;
        try {
            titleBar.setTitleBarText(subTitleLibrary.getSubTitleText());
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }

        // Add title bar to right side panel
        rightSidePanel.getChildren().add(titleBar);

        // Add Plus Icon to title bar (On opening shows the library)
        titleBar.setLeftBtn("M8 4a.5.5 0 0 1 .5.5v3h3a.5.5 0 0 1 0 1h-3v3a.5.5 0 0 1-1 0v-3h-3a.5.5 0 0 1 0-1h3v-3A.5.5 0 0 1 8 4", "Open a Book");
        titleBar.getLeftBtn().setOnAction(e -> showBookViewScene(stage));
    }

    private String fileChooser(Stage stage) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("EPUB Files", "*.epub")
        );

        try {
            File file = fileChooser.showOpenDialog(stage);
            Path path = file.toPath();
            return path.toString();
        } catch (NullPointerException err) {
            System.out.println(err.getMessage());
            return null;
        }
    }

    private void showBookViewScene(Stage stage) {
        Scene bookViewScene;
        try {
            String filePath = fileChooser(stage);
            bookViewScene = new Scene(new BookViewScene(stage, filePath), this.getHomeLayoutWidth(), this.getHomeLayoutHeight());
        } catch (IOException e) {
            System.out.println(e.getMessage());
            return;
        }

        try {
            File file = new File("src/main/resources/com/example/lume/styles.css");
            bookViewScene.getStylesheets().add("file://" + file.getAbsolutePath().replace("\\", "/"));
        } catch (NullPointerException e) {
            System.out.println(e.getMessage());
        }
        stage.setScene(bookViewScene);
    }
}
