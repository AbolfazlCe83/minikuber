package org.example;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class Master {

    private static Worker currentWorker;
    public Master(int port) {
        System.out.println("Starting Master System...");
        try {
            ServerSocket serverSocket = new ServerSocket(port);
            while (true){
                Socket socket = serverSocket.accept();
                Connection connection = new Connection(socket);
                connection.start();
            }
        } catch (IOException e) {
            //TODO: try to reconnect...
        }
    }

    public static Worker getCurrentWorker() {
        return currentWorker;
    }

    public static void setCurrentWorker(Worker currentWorker) {
        Master.currentWorker = currentWorker;
    }
}
