package com.example.lume.networking;

import java.net.BindException;
import java.net.ServerSocket;
import java.net.Socket;

public class Server {
    private int port;
    private ServerSocket serverSocket;

    public Server(int port) {
        this.port = port;
    }

    public void start() {
        try {
            serverSocket = new ServerSocket(port);
            System.out.println("Server started on port " + port);

            // New thread to run the server
            new Thread(() -> {
               while (!serverSocket.isClosed()) {
                   try {
                       Socket clientSocket = serverSocket.accept();
                   } catch (Exception ex) {

                   }
               }
            });
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }
}
