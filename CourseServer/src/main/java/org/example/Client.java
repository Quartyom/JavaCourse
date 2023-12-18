package org.example;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.quartyom.QuPackets.AdvPlatform;
import com.quartyom.QuPackets.LoginData;
import com.quartyom.QuPackets.QuPacket;

import java.io.*;
import java.net.Socket;
import java.util.Scanner;

public class Client {
    public static String host = "localhost";
    public static int port = 4004;
    public static Scanner scanner = new Scanner(System.in);

    public static void main(String[] args) {
        try (Socket socket = new Socket(host, port);
             ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
             ObjectInputStream in = new ObjectInputStream(socket.getInputStream());
        ){
            socket.setSoTimeout(3000);

            System.out.println("Connected to server");

            while (true) {
                QuPacket packet = new QuPacket();
                System.out.print("action> ");
                String action = scanner.nextLine();
                if (action.equals("auth")) {
                    packet.type = "auth";
                    LoginData data = new LoginData();
                    System.out.print("login> ");
                    data.login = scanner.nextLine();
                    System.out.print("password> ");
                    data.password = scanner.nextLine();
                    packet.data = data;
                } else if (action.equals("info")) {
                    System.out.print("session id> ");
                    packet.type = "info";
                    try {
                        packet.sessionId = Long.parseLong(scanner.nextLine());
                    }
                    catch (Exception ex){}
                } else if (action.equals("exit")) {
                    break;
                }
                else if (action.equals("show")) {
                    packet.type = "showPlatforms";
                }
                else if (action.equals("orders")) {
                    packet.type = "getOrdersForAdvPlatform";
                    AdvPlatform platform = new AdvPlatform();
                    platform.id = scanner.nextLong();
                    packet.data = platform;
                }
                else {
                    System.out.println("wrong action");
                    continue;
                }

                out.writeObject(packet);
                out.flush();

                // по-хорошему, тут должен быть интервал ожидания
                try {
                    QuPacket receivedPacket = (QuPacket) in.readObject();
                    System.out.println("type: " + receivedPacket.type);
                    System.out.println("data: " + receivedPacket.data);
                    System.out.println("sessionId: " + receivedPacket.sessionId);
                } catch (ClassNotFoundException e) {
                    System.out.println("Undefined response");
                }
                System.out.println();

            }


        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("disconnected from server");
    }

    public static void help(){
        System.out.println("/help /exit");
    }
}