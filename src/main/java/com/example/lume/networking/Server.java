package com.example.lume.networking;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

public class Server {
    private int port;
    private ServerSocket serverSocket;
    private final Consumer<Message> onMessageReceived;
    private final List<SocketWrapper> clients = Collections.synchronizedList(new ArrayList<SocketWrapper>());

    public Server(int port, Consumer<Message> onMessageReceived) {
        this.port = port;
        this.onMessageReceived = onMessageReceived;
    }

    public void start() throws Exception {
        serverSocket = new ServerSocket(port);
        System.out.println("Server started on port " + port);
        // New thread to run the server
        new Thread(() -> {
           while (!serverSocket.isClosed()) {
               try {
                   Socket clientSocket = serverSocket.accept();
                   System.out.println("Accepted connection from " + clientSocket.getInetAddress());
                   serve(clientSocket);
               } catch (Exception ex) {
                   if (serverSocket.isClosed()) {
                       System.out.println("Server socket closed!");
                   } else {
                       System.out.println(ex);
                   }
               }
           }
        }).start();
    }

    public void serve(Socket clientSocket) throws IOException {
        SocketWrapper socketWrapper = new SocketWrapper(clientSocket);
        clients.add(socketWrapper);
        new ReadThreadServer(socketWrapper);
    }

    public void broadcast(Message message) {
        synchronized (clients) {
            clients.removeIf(client -> {
                try {
                    System.out.println("Writing message to client...");
                    client.write(message);
                    return false;
                } catch (IOException ex) {
                    System.out.println("Error writing to client: " + ex.getMessage());
                    return true;
                }
            });
        }
    }

    private class ReadThreadServer implements Runnable {
        private Thread thr;
        private SocketWrapper socketWrapper;

        public ReadThreadServer(SocketWrapper socketWrapper) {
            this.socketWrapper = socketWrapper;
            thr = new Thread(this);
            thr.start();
        }

        @Override
        public void run() {
            try {
                while (true) {
                    Object o = socketWrapper.read();
                    if (o instanceof Message) {
                        Message message = (Message) o;
                        System.out.println("Server received: " + message.getAnnotation().toString());
                        onMessageReceived.accept(message);
                        broadcast(message);
                    }
                }
            } catch (Exception ex) {
                System.out.println("Error reading from serverSocket: " + ex.getMessage());
                clients.remove(socketWrapper);
            } finally {
                try {
                    socketWrapper.closeConnection();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public void stop() {
        try {
            if (serverSocket != null) {
                serverSocket.close();
            }
        } catch (Exception ex) {
            System.err.println("Error stopping server: " + ex.getMessage());
        }
    }
}
