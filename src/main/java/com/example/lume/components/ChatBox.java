package com.example.lume.components;

import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;

public class ChatBox extends HBox {

    private final Text messageText;

    public enum Sender {
        USER,
        AI
    }

    public ChatBox(String message, Sender sender) {
        this.getStyleClass().add("chat-box-container");

        messageText = new Text(message);
        messageText.getStyleClass().add("chat-box-text");

        TextFlow textFlow = new TextFlow(messageText);
        textFlow.getStyleClass().add("chat- box");

        if (sender == Sender.USER) {
            this.setAlignment(Pos.CENTER_RIGHT);
            textFlow.getStyleClass().add("user-box");
        } else {
            this.setAlignment(Pos.CENTER_LEFT);
            textFlow.getStyleClass().add("ai-box");
        }

        getChildren().add(textFlow);
    }

    public void setText(String newText) {
        messageText.setText(newText);
    }
}
