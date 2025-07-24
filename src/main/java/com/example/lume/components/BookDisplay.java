package com.example.lume.components;

import io.documentnode.epub4j.domain.Book;
import io.documentnode.epub4j.epub.EpubReader;
import javafx.geometry.Insets;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;

import javafx.scene.image.Image;

import javafx.scene.control.Label;
import javafx.scene.paint.Color;

import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.IOException;

public class BookDisplay extends VBox {
    private BookMetadata bookMetadata;


    public BookDisplay(BookMetadata bookMetadata) {
        super();
        this.bookMetadata = bookMetadata;

        this.getStyleClass().add("book-container");
        this.setPrefSize(400, 600);
        this.setSpacing(10);
        this.setPadding(new Insets(10, 10, 10, 10));

        Book book = null;
        Image coverImg = null;
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
                coverImageView.setFitWidth(380);
            }
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }

        VBox bookDetails = new VBox();
        bookDetails.setPrefSize(300, 250);
        bookDetails.setSpacing(10);
        Label title = new Label(bookMetadata.getTitle());
        title.setStyle("""
                 -fx-font-weight: 700;
                 -fx-font-size: 20px;
                """);

        Label authors = new Label(bookMetadata.getAuthors().toString());

        bookDetails.getChildren().addAll(title, authors);

        this.getChildren().add(bookDetails);
    }
}
