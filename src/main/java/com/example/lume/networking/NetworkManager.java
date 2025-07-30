package com.example.lume.networking;

import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.application.Platform;

import java.io.IOException;
import java.net.BindException;
import java.util.function.Consumer;

public class NetworkManager {
    private Server server;
    private Client client;
    private static final int PORT = 4041;
    private Consumer<Message> onMessageReceivedCallback;

    public void start() {

        Consumer<Message> uiCallback = (message) -> {
            if (onMessageReceivedCallback != null) {
                Platform.runLater(() -> onMessageReceivedCallback.accept(message));
            }
        };

        try {
            server = new Server(PORT, uiCallback);
            server.start();
        } catch (Exception e) {
            if (e.getClass().equals(BindException.class)) {
                System.out.println("Server is already running. Starting a client");
                client = new Client("127.0.0.1", PORT, uiCallback);
                try {
                    client.start();
                    server = null;
                } catch (Exception ce) {
                    System.out.println(ce.getMessage());
                }
            }
        }
    }

    public void setOnMessageReceived(Consumer<Message> callback) {
        this.onMessageReceivedCallback = callback;
    }

    public void sendMessage(Message message) {
        try {
            if (server != null) {
                server.broadcast(message);
            } else if (client != null) {
                client.sendMessage(message);
            }
        } catch (IOException ex) {
            System.out.println("Failed sending message: " + ex.getMessage());
        }
    }

    public void stop() {
        if (server != null) {
            server.stop();
        }
        if (client != null) {
            client.stop();
        }
    }

}
