package com.example.lume.scenes;

import com.example.lume.components.SidePanelOption;
import com.example.lume.components.SubTitleVBox;
import com.example.lume.layouts.BaseLayout;
import com.example.lume.layouts.BookSelf;
import com.example.lume.layouts.TitleBar;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.stage.Stage;

import java.io.File;

public class GutenbergScene extends BaseLayout {
    private final String allBooksSVGPath = "M9 3v15h3V3zm3 2l4 13l3-1l-4-13zM5 5v13h3V5zM3 19v2h18v-2z";
    private final String catalogSVGPath = "m12 14l-2-2l2-2l2 2zM9.875 8.471L8.183 6.78l2.68-2.681q.243-.242.54-.354q.299-.111.597-.111t.596.111t.54.354l2.681 2.68l-1.692 1.693L12 6.346zm-3.096 7.346l-2.681-2.68q-.242-.243-.354-.54q-.111-.299-.111-.597t.111-.596t.354-.54l2.68-2.681l1.693 1.692L6.346 12l2.125 2.125zm10.442 0l-1.692-1.692L17.654 12l-2.125-2.125l1.692-1.692l2.681 2.68q.242.243.354.54q.111.299.111.597t-.111.596t-.354.54zm-6.357 4.085l-2.681-2.68l1.692-1.693L12 17.654l2.125-2.125l1.692 1.692l-2.68 2.681q-.243.242-.54.354q-.299.111-.597.111t-.596-.111t-.54-.354";
    public Label titleText = new Label("Lume");
    public SubTitleVBox subTitleLibrary = new SubTitleVBox(400, 40, "Library");
    public SidePanelOption optionAllBooks = new SidePanelOption(
            400,
            50,
            allBooksSVGPath,
            "All Books");
    public SubTitleVBox subTitleCatalog = new SubTitleVBox(400, 40, "Catalog");

    Stage stage;
    public static BookSelf bookSelf = null;

    public GutenbergScene(Stage stage) {
        super();
        this.stage = stage;

        try {
            File file = new File("src/main/resources/com/example/lume/styles.css");
            this.getStylesheets().add("file:////" + file.getAbsolutePath().replace("\\", "/"));
        } catch (NullPointerException e) {
            System.out.println(e.getMessage());
        }

        // Title of Left side panel
        titleText.getStyleClass().add("title-text");
        titleText.setPadding(new Insets(0, 0, 20, 0));
        titleText.setStyle("""
                    -fx-font-size: 20px;
                    -fx-font-weight: 700;
                """);

        // Sub Title VBox - Library
        leftSidePanel.getChildren().addAll(titleText, subTitleLibrary, optionAllBooks);
        optionAllBooks.getIconButton().setOnAction(e -> {
            stage.setScene(new Scene(new LibraryLayout(stage), this.getHomeLayoutWidth(), this.getHomeLayoutHeight()));
        });

        // Sub Title Component - Catalog
        subTitleCatalog.setPadding(new Insets(30, 0, 10, 0));

        // Add Menu Option
        SidePanelOption optionGutenberg = new SidePanelOption(400, 50, catalogSVGPath, "Gutenberg");
        SidePanelOption optionStandardEbooks = new SidePanelOption(400, 50, catalogSVGPath, "Standard Ebooks");
//        SidePanelOption optionFeedBooks = new SidePanelOption(400, 50, catalogSVGPath, "Feedbooks");
        optionGutenberg.getIconButton().setStyle("-fx-background-color: rgba(54, 54, 56, 0.93);");
        optionGutenberg.getIconButton().setOnAction(e -> {
            stage.setScene(this.getScene());
        });

        leftSidePanel.getChildren().addAll(subTitleCatalog, optionGutenberg, optionStandardEbooks);

        TitleBar titleBar = new TitleBar(this.getRightSidePanelWidth(), 100, stage);;
        try {
            titleBar.setTitleBarText(subTitleLibrary.getSubTitleText());
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }

        // Add title bar to right side panel
        rightSidePanel.getChildren().add(titleBar);

        rightSidePanel.setPadding(new Insets(0, 25, 0, 25));

        // Add Plus Icon to title bar (On opening shows the library)
        titleBar.setLeftBtn("M8 4a.5.5 0 0 1 .5.5v3h3a.5.5 0 0 1 0 1h-3v3a.5.5 0 0 1-1 0v-3h-3a.5.5 0 0 1 0-1h3v-3A.5.5 0 0 1 8 4", "Open a Book");

    }


}
