package com.example.lume;

// Javafx built-in
import com.example.lume.scenes.LibraryLayout;
import javafx.application.Application;

import javafx.scene.Scene;
import javafx.stage.Screen;
import javafx.stage.Stage;

// Errors
import java.io.IOException;


public class Main extends Application {
    // Stage sizes
    private final double stageWidth = Screen.getPrimary().getVisualBounds().getWidth();
    private final double stageHeight = Screen.getPrimary().getVisualBounds().getHeight();

    @Override
    public void start(Stage stage) throws IOException {
        // Set stage properties
        stage.setTitle("Lume");
        stage.setResizable(false);
        stage.setWidth(stageWidth);
        stage.setHeight(stageHeight);

        LibraryLayout libraryLayout = new LibraryLayout(stage);

        // Home Scene
        Scene homeScene = new Scene(libraryLayout, stageWidth, stageHeight);
        try {
            homeScene.getStylesheets().add(this.getClass().getResource("styles.css").toExternalForm());
        } catch (NullPointerException e) {
            System.out.println(e.getMessage());
        }

        stage.setScene(homeScene);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}
