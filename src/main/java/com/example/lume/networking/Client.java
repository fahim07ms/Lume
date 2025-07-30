package com.example.lume.networking;

import java.io.IOException;
import java.net.Socket;
import java.util.function.Consumer;

public class Client {
    public String host;
    public int port;
    private SocketWrapper socketWrapper;
    private final Consumer<Message> onMessageReceived;

    public Client(String host, int port, Consumer<Message> onMessageReceived) {
        this.host = host;
        this.port = port;
        this.onMessageReceived = onMessageReceived;
    }

    public void start() throws Exception {
        socketWrapper = new SocketWrapper(host, port);
        new ReadThreadClient(socketWrapper);
    }

    public void sendMessage(Message message) throws IOException {
        if (socketWrapper != null) {
            socketWrapper.write(message);
        }
    }


    public class ReadThreadClient implements Runnable {
        private SocketWrapper socketWrapper;
        private Thread thr;

        public ReadThreadClient(SocketWrapper socketWrapper) {
            this.socketWrapper = socketWrapper;
            this.thr = new Thread(this);
            this.thr.start();
        }

        @Override
        public void run() {
            try {
                while (true) {
                    Object o = socketWrapper.read();
                    if (o instanceof Message) {
                        Message message = (Message) o;
                        System.out.println("Client received: " + message.getAnnotation().toString());
                        onMessageReceived.accept(message);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
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
            if  (socketWrapper != null) {
                socketWrapper.closeConnection();
            }
        } catch (Exception ex) {
            System.err.println("Error stopping client: " + ex.getMessage());
        }
    }
}
