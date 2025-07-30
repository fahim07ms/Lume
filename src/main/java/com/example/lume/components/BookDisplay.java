package com.example.lume.components;

import com.example.lume.scenes.AboutBookScene;
import com.example.lume.scenes.BookViewScene;
import com.example.lume.scenes.LibraryLayout;
import io.documentnode.epub4j.domain.Book;
import io.documentnode.epub4j.domain.Identifier;
import io.documentnode.epub4j.epub.EpubReader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.image.ImageView;
import javafx.scene.input.ContextMenuEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.*;

import javafx.scene.image.Image;

import javafx.scene.control.Label;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.awt.event.MouseEvent;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;


import static com.example.lume.scenes.LibraryLayout.lumeMetadata;

public class BookDisplay extends VBox {
    private final BookMetadata bookMetadata;
    private final String id;

    static final String THREE_DOT_SVG = "M9.5 13a1.5 1.5 0 1 1-3 0 1.5 1.5 0 0 1 3 0m0-5a1.5 1.5 0 1 1-3 0 1.5 1.5 0 0 1 3 0m0-5a1.5 1.5 0 1 1-3 0 1.5 1.5 0 0 1 3 0";

    String rights = null;
    StringBuilder identifiers = new StringBuilder();
    StringBuilder description = new StringBuilder();
    String language = null;
    String publisher = null;
    Image coverImg = null;

    public BookDisplay(Stage stage, String id, BookMetadata bookMetadata) {
        super();
        this.bookMetadata = bookMetadata;
        this.id = id;

        this.getStyleClass().add("book-display-container");
        this.setPrefWidth(250);
        this.setSpacing(5);
        this.setPadding(new Insets(15, 15, 15, 15));
        this.setOnMouseClicked(e -> {
            Scene bookViewScene;
            try {
                bookViewScene = new Scene(new BookViewScene(stage, stage.getScene(), bookMetadata.getFilePath(), lumeMetadata), 1920, 1080);
            } catch (IOException ex) {
                System.out.println(ex.getMessage());
                return;
            }

            try {
                File file = new File("src/main/resources/com/example/lume/styles.css");
                bookViewScene.getStylesheets().add("file:////" + file.getAbsolutePath().replace("\\", "/"));
            } catch (NullPointerException ex) {
                System.out.println(ex.getMessage());
            }
            stage.setScene(bookViewScene);
        });


        Book book = null;
        try {
            EpubReader epubReader = new EpubReader();
            book = epubReader.readEpub(new FileInputStream(bookMetadata.getFilePath()));

            if (book != null) {
                coverImg = new Image(new ByteArrayInputStream(book.getCoverImage().getData()));
            }

            if (coverImg != null) {
                ImageView coverImageView = new ImageView();
                coverImageView.setImage(coverImg);
                coverImageView.getStyleClass().add("book-display-cover-image");
                this.getChildren().add(coverImageView);
                coverImageView.setPreserveRatio(true);
                coverImageView.setFitWidth(250);
            }
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }

        for (String description : book.getMetadata().getDescriptions()) {
            this.description.append(description).append("\n");
        }
        language = book.getMetadata().getLanguage();

        for (Identifier identifier : book.getMetadata().getIdentifiers()) {
            identifiers.append(identifier.getValue()).append(", ");
        }

        rights = String.join(", ", book.getMetadata().getRights());
        publisher = String.join(", ", book.getMetadata().getPublishers());

        VBox bookDetails = new VBox();
        bookDetails.setPrefWidth(250);
        bookDetails.setSpacing(5);

        HBox titleThreeDot = new HBox();
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        titleThreeDot.setPrefWidth(250);
        titleThreeDot.setAlignment(Pos.CENTER);

        titleThreeDot.setPadding(new Insets(10, 0, 0, 0));

        Label title = new Label(bookMetadata.getTitle());
        title.setStyle("""
                 -fx-font-weight: 700;
                 -fx-font-size: 20px;
                """);

        ButtonIcon threeDotBtn = new ButtonIcon(THREE_DOT_SVG);
        threeDotBtn.setStyle("-fx-max-width: 10px; -fx-max-height: 10px;");
        threeDotBtn.getSvgIcon().setScaleX(1);
        threeDotBtn.getSvgIcon().setScaleY(1);

        createBookDetailsContextMenu(threeDotBtn);

        titleThreeDot.getChildren().addAll(title, spacer, threeDotBtn);

        Label authors = new Label(String.join(", ", bookMetadata.getAuthors()));
        authors.setStyle("-fx-font-size: 16px;");


        float percent = ((float) bookMetadata.getCurrentSpread() /bookMetadata.getTotalSpread())*100;
        Label percentLabel = new Label(String.format("%.0f%%  Completed", percent));
        percentLabel.setStyle("""
                 -fx-font-weight: 500;
                 -fx-font-size: 15px;
                """);


        bookDetails.getChildren().addAll(titleThreeDot, authors, percentLabel);

        this.getChildren().add(bookDetails);
    }

    private void createBookDetailsContextMenu(ButtonIcon threeDotBtn) {
        ContextMenu bookOptionMenu = new ContextMenu();
        bookOptionMenu.getStyleClass().add("book-option-menu");
        bookOptionMenu.setStyle("-fx-font-size: 14px;");

        MenuItem removeBtn =  new MenuItem("Remove");
        MenuItem aboutBtn = new MenuItem("About Book");

        removeBtn.setOnAction(e -> {
           lumeMetadata.getBookMetaDataMap().remove(id);
           LibraryLayout.bookSelf.removeBookDisplay(this);
        });

        aboutBtn.setOnAction(e -> {
            Stage stage = new Stage();
            stage.initStyle(StageStyle.UNDECORATED);
            AboutBookScene aboutBookScene = new AboutBookScene(coverImg, stage, bookMetadata.getTitle(), description.toString(),
                                String.join(", ", bookMetadata.getAuthors()), rights,
                                identifiers.toString(), language, publisher);
            Scene scene = new Scene(aboutBookScene, 400, 700);

            try {
                File file = new File("src/main/resources/com/example/lume/styles.css");
                aboutBookScene.getStylesheets().add("file:////" + file.getAbsolutePath().replace("\\", "/"));
            } catch (NullPointerException npe) {
                System.out.println(npe.getMessage());
            }


            stage.setTitle("About the Book");
            stage.setScene(scene);
            stage.setScene(scene);
            stage.show();
        });

        bookOptionMenu.setAutoHide(true);
        bookOptionMenu.setHideOnEscape(true);
        bookOptionMenu.setPrefSize(100, 450);

        bookOptionMenu.getItems().addAll(removeBtn, aboutBtn);

        // Enable context menu on right clicking the Button
        threeDotBtn.setOnContextMenuRequested(event -> {
            bookOptionMenu.show(threeDotBtn, event.getSceneX(), event.getSceneY());
        });
    }

    public Node getNode() {
        return this;
    }
}
