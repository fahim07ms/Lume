package com.example.lume.components;

import javafx.scene.control.ScrollPane;
import javafx.scene.layout.VBox;

public class ChatPane extends ScrollPane {

    private final VBox chatContainer;

    public ChatPane() {
        this.getStyleClass().add("scroll-pane");
        chatContainer = new VBox();
        chatContainer.getStyleClass().add("chat-container");

        // Make the VBox grow with the ScrollPane's width
        chatContainer.prefWidthProperty().bind(this.widthProperty().subtract(20));

        this.setContent(chatContainer);
        this.setFitToWidth(true);
        this.getStyleClass().add("chat-scroll-pane");

        // Automatically scroll to the bottom when the content size changes
        chatContainer.heightProperty().addListener((obs, oldVal, newVal) -> {
            this.setVvalue(1.0);
        });
    }

    public void addBubble(ChatBox chatBox) {
        chatContainer.getChildren().add(chatBox);
    }
}
