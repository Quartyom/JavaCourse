package org.example;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class WebCourseServer {
    public static void main(String[] args) {
        try (ServerSocket serverSocket = new ServerSocket(8003)){
            System.out.println("Server started");
            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("accepted");
                clientSocket.setSoTimeout(10_000);
                new WebThread(clientSocket).start();
            }

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            System.out.println("Server stopped");
        }
    }
}
