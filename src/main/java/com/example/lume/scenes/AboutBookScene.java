package com.example.lume.scenes;

import com.example.lume.layouts.TitleBar;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;

public class AboutBookScene extends BorderPane {

    public AboutBookScene(Image coverImage, Stage stage, String title, String description, String authors, String rights, String identifiers, String language, String publisher) {
        super();
        this.getStyleClass().add("about-book-pane");

        TitleBar titleBar = new TitleBar(400, 50, stage);
        titleBar.getStyleClass().add("about-book-title-bar");
        titleBar.setTitleBarText("About This Book");
        this.setTop(titleBar);

        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setFitToWidth(true); // Ensures content resizes horizontally
        scrollPane.getStyleClass().add("about-book-scroll-pane");

        VBox contentBox = new VBox();
        contentBox.getStyleClass().add("about-book-content-box");
        contentBox.setAlignment(Pos.TOP_CENTER);

        if (coverImage != null) {
            ImageView coverImageView = new ImageView(coverImage);
            coverImageView.setFitHeight(250);
            coverImageView.setPreserveRatio(true);
            coverImageView.getStyleClass().add("about-book-cover");
            VBox.setMargin(coverImageView, new Insets(0, 0, 20, 0)); // Add some space below the cover
            contentBox.getChildren().add(coverImageView);
        }

        Label titleLabel = new Label(title);
        titleLabel.getStyleClass().add("about-book-title");
        titleLabel.setWrapText(true);
        titleLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: 800;");


        Label authorsLabel = new Label(authors);
        authorsLabel.getStyleClass().add("about-book-authors");
        authorsLabel.setWrapText(true);
        titleLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: 600;");

        contentBox.getChildren().addAll(titleLabel, authorsLabel);

        contentBox.getChildren().add(createDetailSection("Description", description));
        contentBox.getChildren().add(createDetailSection("Publisher", publisher));
        contentBox.getChildren().add(createDetailSection("Language", language));
        contentBox.getChildren().add(createDetailSection("Identifier(s)", identifiers));
        contentBox.getChildren().add(createDetailSection("Copyright", rights));

        scrollPane.setContent(contentBox);
        this.setCenter(scrollPane);
    }

    private Node createDetailSection(String title, String content) {
        VBox sectionBox = new VBox();
        sectionBox.getStyleClass().add("detail-section");

        Label titleLabel = new Label(title);
        titleLabel.getStyleClass().add("detail-title");
        titleLabel.setStyle("""
                -fx-font-size: 18px;
                -fx-font-weight: 700;
                -fx-padding: 5px 0px 0px 0px;
                """);

        Text contentText = new Text(content);
        contentText.getStyleClass().add("detail-content");
        contentText.setWrappingWidth(340); // Wrap text within the pane's padding

        sectionBox.getChildren().addAll(titleLabel, contentText);
        return sectionBox;
    }
}
