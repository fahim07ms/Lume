package com.example.lume;

// Javafx built-in
import com.example.lume.networking.Client;
import com.example.lume.networking.NetworkManager;
import com.example.lume.networking.Server;
import com.example.lume.scenes.LibraryLayout;
import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.application.Application;

import javafx.scene.Scene;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

// Errors
import java.io.File;
import java.io.IOException;
import java.net.BindException;


public class Main extends Application {
    // Stage sizes
    private final double stageWidth = Screen.getPrimary().getVisualBounds().getWidth();
    private final double stageHeight = Screen.getPrimary().getVisualBounds().getHeight();

    public static final NetworkManager  networkManager = new NetworkManager();

    @Override
    public void start(Stage stage) throws IOException {
        // Start networking
        networkManager.start();

        // Set stage properties
        stage.setTitle("Lume");
        stage.setResizable(false);
        stage.setWidth(stageWidth);
        stage.setHeight(stageHeight);
        stage.initStyle(StageStyle.UNDECORATED);

        LibraryLayout libraryLayout = new LibraryLayout(stage);

        // Home Scene
        Scene homeScene = new Scene(libraryLayout, stageWidth, stageHeight);
        try {
            File file = new File("src/main/resources/com/example/lume/styles.css");
            homeScene.getStylesheets().add("file:////" + file.getAbsolutePath().replace("\\", "/"));
        } catch (NullPointerException e) {
            System.out.println(e.getMessage());
        }

        stage.setOnCloseRequest(event -> {
            // Stop networking
            networkManager.stop();

            // Read data from metadata file
            ObjectMapper objectMapper = new ObjectMapper();
            try {
                objectMapper.writeValue(new File(System.getProperty("user.home") + "/.lume/metadata.json"), LibraryLayout.lumeMetadata);
            } catch (IOException ex) {
                System.out.println(ex.getMessage());
            }
        });

        stage.setScene(homeScene);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}
